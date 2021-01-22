package bms.player.beatoraja.audio;

import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import com.portaudio.*;

import bms.player.beatoraja.Config;

class MyDeviceInfo extends DeviceInfo {
    public int id;
}

/**
 * PortAudioドライバ
 * 
 * @author exch
 */
public class PortAudioDriver extends AbstractAudioDriver<PCM> implements Runnable {

	private static MyDeviceInfo[] devices;
	
	private BlockingStream stream;

	/**
	 * ミキサー入力
	 */
	private final MixerInput[] inputs;

	private long idcount;
	
	private boolean stop = false;
	
	private final float[] buffer;
	
	private final Thread mixer;

	public static MyDeviceInfo[] getDevices() {
		if(devices == null) {
			PortAudio.initialize();

			List<MyDeviceInfo> result = new ArrayList<MyDeviceInfo>(PortAudio.getDeviceCount());
			devices = new MyDeviceInfo[PortAudio.getDeviceCount()];
			for(int i = 0;i < devices.length;i++) {
				DeviceInfo info = PortAudio.getDeviceInfo(i);
				if (info.maxOutputChannels >= 2) {
				    	MyDeviceInfo myInfo = new MyDeviceInfo();
				    	myInfo.id = i;
				    	myInfo.version = info.version;
				    	myInfo.name = info.name + ' ' + (int) info.defaultSampleRate + "Hz";
				    	myInfo.hostApi = info.hostApi;
				    	myInfo.maxInputChannels = info.maxInputChannels;
				    	myInfo.maxOutputChannels = info.maxOutputChannels;
				    	myInfo.defaultLowInputLatency = info.defaultLowInputLatency;
				    	myInfo.defaultHighInputLatency = info.defaultHighInputLatency;
				    	myInfo.defaultLowOutputLatency = info.defaultLowOutputLatency;
				    	myInfo.defaultHighOutputLatency = info.defaultHighOutputLatency;
				    	myInfo.defaultSampleRate = info.defaultSampleRate;
				    	result.add(myInfo);
				}
				
				/*System.out.println(String.format("version=%d", info.version));
				System.out.println(String.format("name=%s", info.name));
				System.out.println(String.format("hostApi=%d", info.hostApi));
				System.out.println(String.format("maxInputChannels=%d", info.maxInputChannels));
				System.out.println(String.format("maxOutputChannels=%d", info.maxOutputChannels));
				System.out.println(String.format("defaultLowInputLatency=%f", info.defaultLowInputLatency));
				System.out.println(String.format("defaultHighInputLatency=%f", info.defaultHighInputLatency));
				System.out.println(String.format("defaultLowOutputLatency=%f", info.defaultLowOutputLatency));
				System.out.println(String.format("defaultHighOutputLatency=%f", info.defaultHighOutputLatency));
				System.out.println(String.format("defaultSampleRate=%f", info.defaultSampleRate));
				System.out.println();*/
			}

			devices = result.toArray(new MyDeviceInfo[result.size()]);
		}
		return devices;
	}

	public PortAudioDriver(Config config) {
		super(config.getSongResourceGen());
		MyDeviceInfo[] devices = getDevices();
		// Get the default device and setup the stream parameters.
		int deviceId = 0;
		DeviceInfo deviceInfo = null;
		for(int i = 0;i < devices.length;i++) {
			if(devices[i].name.equals(config.getAudioDriverName())) {
				deviceId = devices[i].id;
				deviceInfo = devices[i];
				break;
			}
		}
		if (deviceInfo == null && devices != null && devices.length > 0) {
		    deviceInfo = devices[0];
		}
		sampleRate = (int)deviceInfo.defaultSampleRate;
		channels = 2;
//		System.out.println( "  deviceId    = " + deviceId );
//		System.out.println( "  sampleRate  = " + sampleRate );
//		System.out.println( "  device name = " + deviceInfo.name );

		StreamParameters streamParameters = new StreamParameters();
		streamParameters.channelCount = channels;
		streamParameters.device = deviceId;
		int framesPerBuffer = config.getAudioDeviceBufferSize();
		streamParameters.suggestedLatency = framesPerBuffer / deviceInfo.defaultSampleRate;
//		System.out.println( "  suggestedLatency = " + streamParameters.suggestedLatency );

		int flags = 0;
		if (!config.isAudioClipping()) {
		        flags |= PortAudio.FLAG_CLIP_OFF;
		}
		if (!config.isAudioDithering()) {
		        flags |= PortAudio.FLAG_DITHER_OFF;
		}
		
		// Open a stream for output.
		stream = PortAudio.openStream( null, streamParameters, sampleRate, framesPerBuffer, flags, config.isWASAPIExclusive() ? 1 : 0 );

		stream.start();

		mixer = new Thread(this);
		buffer = new float[framesPerBuffer * channels];
		inputs = new MixerInput[config.getAudioDeviceSimultaneousSources()];
		for (int i = 0; i < inputs.length; i++) {
			inputs[i] = new MixerInput();
		}
		mixer.start();
	}

	@Override
	protected PCM getKeySound(Path p) {
		return PCM.load(p.toString(), this);
	}

	@Override
	protected PCM getKeySound(PCM pcm) {
		return pcm;
	}

	@Override
	protected void play(PCM pcm, int channel, float volume, float pitch) {
		put(pcm, channel, volume, pitch, false);
	}

	@Override
	protected void play(AudioElement<PCM> id, float volume, boolean loop) {
		id.id = put(id.audio, -1, volume, 1.0f, loop);
	}

	@Override
	protected void setVolume(AudioElement<PCM> id, float volume) {
		for (MixerInput input : inputs) {
			if (input.id == id.id) {
				input.volume = volume;
				break;
			}
		}
	}

	@Override
	protected void disposeKeySound(PCM pcm) {
	}

	private long put(PCM pcm, int channel, float volume, float pitch, boolean loop) {
		synchronized (inputs) {
			for (MixerInput input : inputs) {
				if (input.pos == -1) {
					input.pcm = pcm;
					input.volume = volume;
					input.pitch = pitch;
					input.loop = loop;
					input.id = idcount++;
					input.channel = channel;
					input.pos = 0;
					return input.id;
				}
			}
		}
		return -1;
	}

	@Override
	protected void stop(PCM id) {
		synchronized (inputs) {
			for (MixerInput input : inputs) {
				if (input.pcm == id) {
					input.pos = -1;
				}
			}				
		}
	}

	@Override
	protected void stop(PCM id, int channel) {
		synchronized (inputs) {
			for (MixerInput input : inputs) {
				if (input.pcm == id && input.channel == channel) {
					input.pos = -1;
				}
			}
		}
	}

	public void run() {
		while(!stop) {
			final float gpitch = getGlobalPitch();
			synchronized (inputs) {
				for (int i = 0; i < buffer.length; i+=2) {
					float wav_l = 0;
					float wav_r = 0;
					for (MixerInput input : inputs) {
						if (input.pos != -1) {
							if(input.pcm instanceof FloatPCM) {
								final float[] sample = (float[]) input.pcm.sample;
								wav_l += sample[input.pos + input.pcm.start] * input.volume;
								wav_r += sample[input.pos+1 + input.pcm.start] * input.volume;																
							} else if(input.pcm instanceof ShortDirectPCM) {
								final ByteBuffer sample = (ByteBuffer) input.pcm.sample;
								wav_l += ((float) sample.getShort((input.pos + input.pcm.start) * 2)) * input.volume / Short.MAX_VALUE;
								wav_r += ((float) sample.getShort((input.pos+1 + input.pcm.start) * 2)) * input.volume / Short.MAX_VALUE;																
							} else if(input.pcm instanceof ShortPCM) {
								final short[] sample = (short[]) input.pcm.sample;
								wav_l += ((float) sample[input.pos + input.pcm.start]) * input.volume / Short.MAX_VALUE;
								wav_r += ((float) sample[input.pos+1 + input.pcm.start]) * input.volume / Short.MAX_VALUE;																
							} else if(input.pcm instanceof BytePCM) {
								final byte[] sample = (byte[]) input.pcm.sample;
								wav_l += ((float) (sample[input.pos + input.pcm.start] - 128)) * input.volume / Byte.MAX_VALUE;
								wav_r += ((float) (sample[input.pos+1 + input.pcm.start] - 128)) * input.volume / Byte.MAX_VALUE;																
							}
							input.posf += gpitch * input.pitch;
							int inc = (int)input.posf;
							if (inc > 0) {
								input.pos += 2 * inc;
								input.posf -= (float)inc;
							}
							if (input.pos >= input.pcm.len) {
								input.pos = input.loop ? 0 : -1;
							}
						}
					}
					buffer[i] = wav_l;
					buffer[i+1] = wav_r;
				}						
			}
			
			try {
				stream.write( buffer, buffer.length / 2);
			} catch(Throwable e) {
				e.printStackTrace();
			}
			
		}
	}		

	public void dispose() {
		super.dispose();
		if(stream != null) {
			stop = true;
			long l = System.currentTimeMillis();
			while(mixer.isAlive() && System.currentTimeMillis() - l < 1000);
			stream.stop();
			stream.close();
			
			stream = null;

			PortAudio.terminate();
//			System.out.println( "JPortAudio test complete." );			
		}
	}

	static class MixerInput {
		public PCM pcm;
		public float volume;
		public float pitch;
		public int pos = -1;
		public float posf = 0.0f;
		public boolean loop;
		public long id;
		public int channel = -1;
	}
}
