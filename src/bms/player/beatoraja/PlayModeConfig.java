package bms.player.beatoraja;

import java.util.Arrays;

import com.badlogic.gdx.Input.Keys;

import bms.model.Mode;
import bms.player.beatoraja.input.BMControllerInputProcessor.BMKeys;
import com.badlogic.gdx.math.MathUtils;

/**
 * プレイコンフィグ。モード毎に保持するべき値についてはこちらに格納する
 *
 * @author exch
 */
public class PlayModeConfig {

    private PlayConfig playconfig = new PlayConfig();
    /**
     * キーボード設定
     */
    private KeyboardConfig keyboard = new KeyboardConfig();
    /**
     * コントローラー設定
     */
    private ControllerConfig[] controller = new ControllerConfig[] { new ControllerConfig() };
    /**
     * MIDI設定
     */
    private MidiConfig midi = new MidiConfig();

    public PlayModeConfig() {
        this(Mode.BEAT_7K);
    }

    public PlayModeConfig(Mode mode) {
        boolean midi = (mode == Mode.KEYBOARD_24K || mode == Mode.KEYBOARD_24K_DOUBLE);
        this.keyboard = new KeyboardConfig(mode, !midi);
        controller = new ControllerConfig[mode.player];
        for(int i = 0;i < controller.length;i++) {
            controller[i] = new ControllerConfig(mode, i, false);
        }
        this.midi = new MidiConfig(mode, midi);
    }

    public PlayModeConfig(KeyboardConfig keyboard, ControllerConfig[] controllers, MidiConfig midi) {
        this.keyboard = keyboard;
        this.controller = controllers.clone();
        this.midi = midi;
    }

    public KeyboardConfig getKeyboardConfig() {
        return keyboard;
    }

    public void setKeyboardConfig(KeyboardConfig keyboard) {
        this.keyboard = keyboard;
    }

    public ControllerConfig[] getController() {
        return controller;
    }

    public MidiConfig getMidiConfig() {
        return midi;
    }

    public void setController(ControllerConfig[] controllerassign) {
        this.controller = controllerassign;
    }

    public void validate(int keys) {
        if(playconfig == null) {
            playconfig = new PlayConfig();
        }

        playconfig.validate();

        if (keyboard.keys == null) {
            keyboard.keys = new int[] { Keys.Z, Keys.S, Keys.X, Keys.D, Keys.C, Keys.F, Keys.V, Keys.SHIFT_LEFT,
                    Keys.CONTROL_LEFT };
        }
        if (keyboard.keys.length != keys) {
            keyboard.keys = Arrays.copyOf(keyboard.keys, keys);
        }
        keyboard.duration = MathUtils.clamp(keyboard.duration, 0, 100);

        int index = 0;
        for (ControllerConfig c : controller) {
            if (c.keys == null) {
                c.keys = new int[] { BMKeys.BUTTON_4, BMKeys.BUTTON_7, BMKeys.BUTTON_3, BMKeys.BUTTON_8,
                        BMKeys.BUTTON_2, BMKeys.BUTTON_5, BMKeys.LEFT, BMKeys.UP, BMKeys.DOWN };
            }
            if (c.keys.length != keys) {
                int[] newkeys = new int[keys];
                Arrays.fill(newkeys, -1);
                for (int i = 0; i < c.keys.length && index < newkeys.length; i++, index++) {
                    newkeys[index] = c.keys[i];
                }
                c.keys = newkeys;
            }
            c.duration = MathUtils.clamp(c.duration, 0, 100);
        }

        if (midi.keys == null) {
            midi.keys = new MidiConfig().keys;
        }
        if (midi.keys.length != keys) {
            midi.keys = Arrays.copyOf(midi.keys, keys);
        }

        // KB, コントローラー, Midiの各ボタンについて排他的処理を実施
        boolean[] exclusive = new boolean[keyboard.keys.length];
        validate0(keyboard.keys,  exclusive);
        for(int i = 0;i < controller.length;i++) {
            validate0(controller[i].keys,  exclusive);
        }

        for(int i = 0;i < midi.getKeys().length;i++) {
            if(exclusive[i]) {
                midi.getKeys()[i] = null;
            }
        }
    }

    private void validate0(int[] keys, boolean[] exclusive) {
        for(int i = 0;i < exclusive.length;i++) {
            if(exclusive[i]) {
                keys[i] = -1;
            } else if(keys[i] != -1){
                exclusive[i] = true;
            }
        }
    }

    public PlayConfig getPlayconfig() {
        return playconfig;
    }

    public void setPlayconfig(PlayConfig playconfig) {
        this.playconfig = playconfig;
    }

    /**
     * キーボード設定定義用クラス
     *
     * @author exch
     */
    public static class KeyboardConfig {

        private int[] keys;

        private int start;

        private int select;
        
        private int effect;

        private int vefx;

        private int duration = 16;

        public KeyboardConfig() {
            this(Mode.BEAT_14K, true);
        }

        public KeyboardConfig(Mode mode, boolean enable) {
            this.setKeyAssign(mode, enable);
        }

        public KeyboardConfig(int[] keys, int start, int select) {
            this.keys = keys;
            this.start = start;
            this.select = select;
        }
        
        public KeyboardConfig(int[] keys, int start, int select, int effect, int vefx) {
            this.keys = keys;
            this.start = start;
            this.select = select;
            this.effect = effect;
            this.vefx = vefx;
        }

        public int[] getKeyAssign() {
            return keys;
        }

        public int getStart() {
            return start;
        }

        public int getSelect() {
            return select;
        }
        
        public int getEffect() {
            return effect;
        }

        public int getVEFX() {
            return vefx;
        }

        public void setKeyAssign(Mode mode, boolean enable) {
            switch (mode) {
                case BEAT_5K:
                    keys = new int[] { Keys.Z, Keys.S, Keys.X, Keys.D, Keys.C, Keys.SHIFT_LEFT,
                            Keys.CONTROL_LEFT };
                    break;
                case BEAT_7K:
                    keys = new int[] { Keys.Z, Keys.S, Keys.X, Keys.D, Keys.C, Keys.F, Keys.V, Keys.SHIFT_LEFT,
                            Keys.CONTROL_LEFT };
                    break;
                case BEAT_10K:
                    keys = new int[] { Keys.Z, Keys.S, Keys.X, Keys.D, Keys.C, Keys.SHIFT_LEFT,
                            Keys.CONTROL_LEFT, Keys.COMMA, Keys.L, Keys.PERIOD, Keys.SEMICOLON, Keys.SLASH, Keys.SHIFT_RIGHT, Keys.CONTROL_RIGHT };
                    break;
                case BEAT_14K:
                default:
                    keys = new int[] { Keys.Z, Keys.S, Keys.X, Keys.D, Keys.C, Keys.F, Keys.V, Keys.SHIFT_LEFT,
                            Keys.CONTROL_LEFT, Keys.COMMA, Keys.L, Keys.PERIOD, Keys.SEMICOLON, Keys.SLASH, Keys.APOSTROPHE,
                            Keys.UNKNOWN, Keys.SHIFT_RIGHT, Keys.CONTROL_RIGHT };
                    break;
                case POPN_5K:
                case POPN_9K:
                    keys = new int[] { Keys.Z, Keys.S, Keys.X, Keys.D, Keys.C, Keys.F, Keys.V, Keys.G, Keys.B };
                    break;
                case KEYBOARD_24K:
                    keys = new int[] { Keys.Z, Keys.S, Keys.X, Keys.D, Keys.C, Keys.F, Keys.V, Keys.SHIFT_LEFT,
                            Keys.CONTROL_LEFT, Keys.COMMA, Keys.L, Keys.PERIOD, Keys.SEMICOLON, Keys.SLASH, Keys.APOSTROPHE,
                            Keys.UNKNOWN, Keys.SHIFT_RIGHT, Keys.CONTROL_RIGHT };
                    keys = Arrays.copyOf(keys, 26);
                    break;
                case KEYBOARD_24K_DOUBLE:
                    keys = new int[] { Keys.Z, Keys.S, Keys.X, Keys.D, Keys.C, Keys.F, Keys.V, Keys.SHIFT_LEFT,
                            Keys.CONTROL_LEFT, Keys.COMMA, Keys.L, Keys.PERIOD, Keys.SEMICOLON, Keys.SLASH, Keys.APOSTROPHE,
                            Keys.UNKNOWN, Keys.SHIFT_RIGHT, Keys.CONTROL_RIGHT };
                    keys = Arrays.copyOf(keys, 52);
                    break;
            }
            if(!enable) {
                Arrays.fill(keys, -1);
            }
            start = Keys.Q;
            select = Keys.W;
            effect = Keys.E;
            vefx = Keys.R;
        }

        public void setKeyAssign(int[] keys) {
            this.keys = keys;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public void setSelect(int select) {
            this.select = select;
        }
        
        public void setEffect(int effect) {
            this.effect = effect;
        }

        public void setVEFX(int vefx) {
            this.vefx = vefx;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int inputduration) {
            this.duration = inputduration;
        }
    }

    /**
     * コントローラー設定定義用クラス
     *
     * @author exch
     */
    public static class ControllerConfig {

        public static final int ANALOG_SCRATCH_VER_2 = 0;
        
        public static final int ANALOG_SCRATCH_VER_1 = 1;

        private String name = "";

        private int[] keys;

        private int start;

        private int select;

        private int effect;

        private int vefx;
        
        private int duration = 16;
        /**
         * JKOC Hack (boolean) private variable
         */
        private boolean jkoc_hack = false;
        /**
         * アナログスクラッチを利用するか(INFINITASコントローラの場合true)
         */
        private boolean analogScratch = false;
        /**
         * アナログスクラッチモード
         */
        private int analogScratchMode = 0;
        /**
         * アナログスクラッチ停止閾値
         */
        private int analogScratchThreshold = 100;
        /**
         * Whether players use mouse to perform scratch
         */
        private boolean mouseScratch = false;
        /**
         * Keep mouse scratch state high for (ms)
         */
        private int mouseScratchDuration = 150;

        public ControllerConfig() {
            this(Mode.BEAT_7K, 0, true);
        }

        public ControllerConfig(Mode mode, int player, boolean enable) {
            this.setKeyAssign(mode, player, enable);
        }

        public ControllerConfig(int[] keys, int start, int select) {
            this.keys = keys;
            this.start = start;
            this.select = select;
        }
        
        public ControllerConfig(int[] keys, int start, int select, int effect, int vefx) {
            this.keys = keys;
            this.start = start;
            this.select = select;
            this.effect = effect;
            this.vefx = vefx;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int[] getKeyAssign() {
            return keys;
        }

        public int getStart() {
            return start;
        }

        public int getSelect() {
            return select;
        }
        
        public int getEffect() {
            return effect;
        }

        public int getVEFX() {
            return vefx;
        }

        public void setKeyAssign(Mode mode, int player, boolean enable) {
            if(player == 0) {
                switch (mode) {
                    case BEAT_5K:
                        keys = new int[]{ BMKeys.BUTTON_4, BMKeys.BUTTON_7, BMKeys.BUTTON_3, BMKeys.BUTTON_8, BMKeys.BUTTON_2,
                                BMKeys.UP, BMKeys.DOWN };
                        break;
                    case BEAT_7K:
                    case POPN_5K:
                    case POPN_9K:
                    default:
                        keys = new int[]{ BMKeys.BUTTON_4, BMKeys.BUTTON_7, BMKeys.BUTTON_3, BMKeys.BUTTON_8, BMKeys.BUTTON_2,
                                BMKeys.BUTTON_5, BMKeys.LEFT, BMKeys.UP, BMKeys.DOWN };
                        break;
                    case BEAT_10K:
                        keys = new int[]{ BMKeys.BUTTON_4, BMKeys.BUTTON_7, BMKeys.BUTTON_3, BMKeys.BUTTON_8, BMKeys.BUTTON_2,
                                BMKeys.UP, BMKeys.DOWN, -1,-1,-1,-1,-1,-1,-1 };
                        break;
                    case BEAT_14K:
                        keys = new int[]{ BMKeys.BUTTON_4, BMKeys.BUTTON_7, BMKeys.BUTTON_3, BMKeys.BUTTON_8, BMKeys.BUTTON_2,
                                BMKeys.BUTTON_5, BMKeys.LEFT, BMKeys.UP, BMKeys.DOWN, -1,-1,-1,-1,-1,-1,-1,-1,-1 };
                        break;
                    case KEYBOARD_24K:
                        keys = new int[]{ BMKeys.BUTTON_4, BMKeys.BUTTON_7, BMKeys.BUTTON_3, BMKeys.BUTTON_8, BMKeys.BUTTON_2,
                                BMKeys.BUTTON_5, BMKeys.LEFT, BMKeys.UP, BMKeys.DOWN };
                        keys = Arrays.copyOf(keys, 26);
                        break;
                    case KEYBOARD_24K_DOUBLE:
                        keys = new int[]{ BMKeys.BUTTON_4, BMKeys.BUTTON_7, BMKeys.BUTTON_3, BMKeys.BUTTON_8, BMKeys.BUTTON_2,
                                BMKeys.BUTTON_5, BMKeys.LEFT, BMKeys.UP, BMKeys.DOWN };
                        keys = Arrays.copyOf(keys, 52);
                        break;
                }
            } else {
                switch (mode) {
                    case BEAT_5K:
                    case BEAT_7K:
                    case POPN_5K:
                    case POPN_9K:
                    default:
                        keys = new int[9];
                        Arrays.fill(keys, -1);
                        break;
                    case BEAT_10K:
                        keys = new int[]{-1,-1,-1,-1,-1,-1,-1,BMKeys.BUTTON_4, BMKeys.BUTTON_7, BMKeys.BUTTON_3, BMKeys.BUTTON_8, BMKeys.BUTTON_2,
                                BMKeys.UP, BMKeys.DOWN};
                        break;
                    case BEAT_14K:
                        keys = new int[]{-1,-1,-1,-1,-1,-1,-1,-1,-1,BMKeys.BUTTON_4, BMKeys.BUTTON_7, BMKeys.BUTTON_3, BMKeys.BUTTON_8, BMKeys.BUTTON_2,
                                BMKeys.BUTTON_5, BMKeys.LEFT, BMKeys.UP, BMKeys.DOWN};
                        break;
                    case KEYBOARD_24K:
                        keys = new int[]{-1,-1,-1,-1,-1,-1,-1,-1,-1,BMKeys.BUTTON_4, BMKeys.BUTTON_7, BMKeys.BUTTON_3, BMKeys.BUTTON_8, BMKeys.BUTTON_2,
                                BMKeys.BUTTON_5, BMKeys.LEFT, BMKeys.UP, BMKeys.DOWN};
                        keys = Arrays.copyOf(keys, 26);
                        break;
                    case KEYBOARD_24K_DOUBLE:
                        keys = new int[]{-1,-1,-1,-1,-1,-1,-1,-1,-1,BMKeys.BUTTON_4, BMKeys.BUTTON_7, BMKeys.BUTTON_3, BMKeys.BUTTON_8, BMKeys.BUTTON_2,
                                BMKeys.BUTTON_5, BMKeys.LEFT, BMKeys.UP, BMKeys.DOWN};
                        keys = Arrays.copyOf(keys, 52);
                        break;
                }
            }
            if(!enable) {
                Arrays.fill(keys, -1);
            }
            start = BMKeys.BUTTON_9;
            select = BMKeys.BUTTON_10;
            effect = BMKeys.BUTTON_11;
            vefx = BMKeys.BUTTON_12;
        }

        public void setKeyAssign(int[] keys) {
            this.keys = keys;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public void setSelect(int select) {
            this.select = select;
        }
        
        public void setEffect(int effect) {
            this.effect = effect;
        }

        public void setVEFX(int vefx) {
            this.vefx = vefx;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int inputduration) {
            this.duration = inputduration;
        }

        public boolean getJKOC()  {
            return jkoc_hack;
        }

        public void setJKOC(boolean jkoc)  {
            this.jkoc_hack = jkoc;
        }

        public boolean isAnalogScratch() {
            return analogScratch;
        }

        public void setAnalogScratch(boolean analogScratch) {
            this.analogScratch = analogScratch;
        }
        
        public int getAnalogScratchMode() {
            return analogScratchMode;
        }

        public void setAnalogScratchMode(int analogScratchMode) {
            this.analogScratchMode = analogScratchMode;
        }
        
        public int getAnalogScratchThreshold() {
            return this.analogScratchThreshold;
        }

        public void setAnalogScratchThreshold(int analogScratchThreshold) {
            this.analogScratchThreshold = 
            	analogScratchThreshold > 0 ? 
            		analogScratchThreshold <= 100 ? analogScratchThreshold : 100 
    			:1;
        }

        public boolean isMouseScratch() {
            return mouseScratch;
        }

        public void setMouseScratch(boolean mouseScratch) {
            this.mouseScratch = mouseScratch;
        }

        public int getMouseScratchDuration() {
            return mouseScratchDuration;
        }

        public void setMouseScratchDuration(int mouseScratchDuration) {
            this.mouseScratchDuration = mouseScratchDuration > 0 ? mouseScratchDuration : 1;
        }
    }

    public static class MidiConfig {

        public static class Input {
            public enum Type {
                NOTE, PITCH_BEND, CONTROL_CHANGE,
            }

            public Type type;
            public int value;

            public Input() {
                this.type = Type.NOTE;
                this.value = 0;
            }

            public Input(Input input) {
                this.type = input.type;
                this.value = input.value;
            }

            public Input(Type type, int value) {
                this.type = type;
                this.value = value;
            }

            public String toString() {
                switch (type) {
                    case NOTE:
                        return "NOTE " + value;
                    case PITCH_BEND:
                        return "PITCH " + (value > 0 ? "+" : "-");
                    case CONTROL_CHANGE:
                        return "CC " + value;
                    default:
                        return null;
                }
            }
        }

        private Input[] keys;
        private Input start;
        private Input select;
        private Input effect;
        private Input vefx;

        public Input[] getKeys() {
            return keys;
        }

        public void setKeys(Input[] keys) {
            this.keys = keys;
        }

        public Input getStart() {
            return start;
        }

        public Input getSelect() {
            return select;
        }
        
        public Input getEffect() {
            return effect;
        }

        public Input getVEFX() {
            return vefx;
        }

        public void setStart(Input input) {
            start = input;
        }

        public void setSelect(Input input) {
            select = input;
        }
        
        public void setEffect(Input input) {
            effect = input;
        }

        public void setVEFX(Input input) {
            vefx = input;
        }

        public MidiConfig() {
            this(Mode.BEAT_7K, true);
        }

        public MidiConfig(Mode mode, boolean enable) {
            this.setKeyAssign(mode, enable);
        }

        public Input getKeyAssign(int index) {
            return keys[index];
        }

        public void setKeyAssign(Mode mode, boolean enable) {
            switch (mode) {
                case BEAT_5K:
                    // 5keys
                    keys = new Input[7];
                    for (int i = 0; i < 5; i++) {
                        keys[i] = new Input(Input.Type.NOTE, 53 + i);
                    }
                    keys[5] = new Input(Input.Type.NOTE, 49);
                    keys[6] = new Input(Input.Type.NOTE, 51);
                    start = new Input(Input.Type.NOTE, 47);
                    select = new Input(Input.Type.NOTE, 48);
                    effect = new Input(Input.Type.NOTE, 49);
                    vefx = new Input(Input.Type.NOTE, 50);
                    break;
                case BEAT_7K:
                default:
                    // 7keys
                    keys = new Input[9];
                    for (int i = 0; i < 7; i++) {
                        keys[i] = new Input(Input.Type.NOTE, 53 + i);
                    }
                    keys[7] = new Input(Input.Type.NOTE, 49);
                    keys[8] = new Input(Input.Type.NOTE, 51);
                    start = new Input(Input.Type.NOTE, 47);
                    select = new Input(Input.Type.NOTE, 48);
                    effect = new Input(Input.Type.NOTE, 49);
                    vefx = new Input(Input.Type.NOTE, 50);
                    break;
                case BEAT_10K:
                    keys = new Input[14];
                    for (int i = 0; i < 5; i++) {
                        // 1P keys
                        keys[i] = new Input(Input.Type.NOTE, 53 + i);
                        // 2P keys
                        keys[7 + i] = new Input(Input.Type.NOTE, 65 + i);
                    }
                    // 1P turntables
                    keys[5] = new Input(Input.Type.NOTE, 49);
                    keys[6] = new Input(Input.Type.NOTE, 51);
                    // 2P turntables
                    keys[12] = new Input(Input.Type.NOTE, 73);
                    keys[13] = new Input(Input.Type.NOTE, 75);
                    start = new Input(Input.Type.NOTE, 47);
                    select = new Input(Input.Type.NOTE, 48);
                    effect = new Input(Input.Type.NOTE, 49);
                    vefx = new Input(Input.Type.NOTE, 50);
                    break;
                case BEAT_14K:
                    keys = new Input[18];
                    for (int i = 0; i < 7; i++) {
                        // 1P keys
                        keys[i] = new Input(Input.Type.NOTE, 53 + i);
                        // 2P keys
                        keys[9 + i] = new Input(Input.Type.NOTE, 65 + i);
                    }
                    // 1P turntables
                    keys[7] = new Input(Input.Type.NOTE, 49);
                    keys[8] = new Input(Input.Type.NOTE, 51);
                    // 2P turntables
                    keys[16] = new Input(Input.Type.NOTE, 73);
                    keys[17] = new Input(Input.Type.NOTE, 75);
                    start = new Input(Input.Type.NOTE, 47);
                    select = new Input(Input.Type.NOTE, 48);
                    effect = new Input(Input.Type.NOTE, 49);
                    vefx = new Input(Input.Type.NOTE, 50);
                    break;
                case POPN_5K:
                case POPN_9K:
                    keys = new Input[9];
                    for (int i = 0; i < 9; i++) {
                        keys[i] = new Input(Input.Type.NOTE, 52 + i);
                    }
                    start = new Input(Input.Type.NOTE, 47);
                    select = new Input(Input.Type.NOTE, 48);
                    effect = new Input(Input.Type.NOTE, 49);
                    vefx = new Input(Input.Type.NOTE, 50);
                    break;
                case KEYBOARD_24K:
                    keys = new Input[26];
                    for (int i = 0; i < 24; i++) {
                        keys[i] = new Input(Input.Type.NOTE, 48 + i);
                    }
                    keys[24] = new Input(Input.Type.PITCH_BEND, 1);
                    keys[25] = new Input(Input.Type.PITCH_BEND, -1);
                    start = new Input(Input.Type.NOTE, 44);
                    select = new Input(Input.Type.NOTE, 46);
                    effect = new Input(Input.Type.NOTE, 48);
                    vefx = new Input(Input.Type.NOTE, 50);
                    break;
                case KEYBOARD_24K_DOUBLE:
                    keys = new Input[52];
                    for (int i = 0; i < 24; i++) {
                        keys[i] = new Input(Input.Type.NOTE, 48 + i);
                        keys[i + 26] = new Input(Input.Type.NOTE, 72 + i);
                    }
                    keys[24] = new Input(Input.Type.PITCH_BEND, 1);
                    keys[25] = new Input(Input.Type.PITCH_BEND, -1);
                    keys[50] = new Input(Input.Type.NOTE, 99);
                    keys[51] = new Input(Input.Type.NOTE, 97);
                    start = new Input(Input.Type.NOTE, 44);
                    select = new Input(Input.Type.NOTE, 46);
                    effect = new Input(Input.Type.NOTE, 48);
                    vefx = new Input(Input.Type.NOTE, 50);
                    break;
            }
            if(!enable) {
                Arrays.fill(keys, null);
            }
        }

        public void setKeyAssign(int index, Input input) {
            keys[index] = input;
        }
    }
}
