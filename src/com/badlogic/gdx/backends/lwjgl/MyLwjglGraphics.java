package com.badlogic.gdx.backends.lwjgl;

import java.awt.Canvas;
import java.util.ArrayList;
import java.util.List;

import bms.player.beatoraja.Config;

public class MyLwjglGraphics extends LwjglGraphics {
	public MyLwjglGraphics (LwjglApplicationConfiguration config) {
		super(config);
	}

	public MyLwjglGraphics (Canvas canvas) {
		super(canvas);
	}

	public MyLwjglGraphics (Canvas canvas, LwjglApplicationConfiguration config) {
		super(canvas, config);
	}

	@Override
	public DisplayMode[] getDisplayModes () {
	        DisplayMode[] modes = super.getDisplayModes();
	        Config config = Config.read();
	        
	    	if (config.getDisplaymode() != bms.player.beatoraja.Config.DisplayMode.FULLSCREEN || config.getRefreshRate() <= 0) {
	    	        return modes;
	    	} else {
	    	        List<DisplayMode> list = new ArrayList<DisplayMode>();
	    	    
	    	        for (DisplayMode mode : modes) {
	    		        if (mode.refreshRate == config.getRefreshRate()) {
	    		                list.add(mode);
	    		        }
	    	        }
	    	        
	    	        return list.toArray(new DisplayMode[list.size()]);
	    	}
	}
}