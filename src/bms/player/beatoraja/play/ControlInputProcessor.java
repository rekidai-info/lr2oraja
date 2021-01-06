package bms.player.beatoraja.play;

import static bms.player.beatoraja.skin.SkinProperty.*;

import java.util.Arrays;

import bms.model.Mode;
import bms.player.beatoraja.PlayConfig;
import bms.player.beatoraja.PlayerResource.PlayMode;
import bms.player.beatoraja.input.BMSPlayerInputProcessor;

public class ControlInputProcessor {

        private final BMSPlayer player;

        private boolean[] hschanged;
        private long startpressedtime;
        private boolean startpressed = false;
        private boolean selectpressed = false;
        private boolean startAndSelectPressed = false;
        private boolean cursorpressed;
        private long lanecovertiming;
        private long laneCoverStartTiming = Long.MIN_VALUE;
        private long exitpressedtime;

        private boolean enableControl = true;
        private boolean enableCursor = true;

        private final PlayMode autoplay;

        private Runnable processStart;
        private Runnable processSelect;

        private boolean isChangeLift = true;

        private float coverChangeMarginLow = 0.001f;
        private float coverChangeMarginHigh = 0.01f;
        private long coverSpeedSwitchDuration = 500;

        private int durationAtStart;

        public ControlInputProcessor(BMSPlayer player, PlayMode autoplay) {
                this.player = player;
                this.autoplay = autoplay;
                hschanged = new boolean[player.main.getInputProcessor().getKeystate().length];
                Arrays.fill(hschanged, true);

                final PlayConfig playConfig = player.main.getPlayerResource().getPlayerConfig().getPlayConfig(player.getMode()).getPlayconfig();
                coverChangeMarginLow = playConfig.getLanecovermarginlow();
                coverChangeMarginHigh = playConfig.getLanecovermarginhigh();
                coverSpeedSwitchDuration = playConfig.getLanecoverswitchduration();

                switch (this.player.getMode()) {
                case POPN_9K:
                        processStart = () -> processStart9key();
                        processSelect = () -> processSelect9key();
                        break;
                case KEYBOARD_24K:
                case KEYBOARD_24K_DOUBLE:
                        processStart = () -> processStart24key();
                        processSelect = () -> processSelect24key();
                        break;
                case BEAT_5K:
                case BEAT_10K:
                        processStart = () -> processStart5key();
                        processSelect = () -> processSelect5key();
                        break;
                default:
                        processStart = () -> processStart7key();
                        processSelect = () -> processSelect7key();
                }
        }

        public void setEnableControl(boolean b) {
                enableControl = b;
        }

        public void setEnableCursor(boolean b) {
                enableCursor = b;
        }

        public void input() {
                final LaneRenderer lanerender = player.getLanerender();
                final BMSPlayerInputProcessor input = player.main.getInputProcessor();
                // 各種コントロール入力判定
                if (enableControl) {
                        if (enableCursor) {
                                if (input.getCursorState()[0]) {
                                        if (!cursorpressed) {
                                                setCoverValue(-0.01f);
                                                cursorpressed = true;
                                        }
                                } else if (input.getCursorState()[1]) {
                                        if (!cursorpressed) {
                                                setCoverValue(0.01f);
                                                cursorpressed = true;
                                        }
                                } else {
                                        cursorpressed = false;
                                }
                        }
                        // move lane cover by mouse wheel
                        if (input.getScroll() != 0) {
                                setCoverValue(- input.getScroll() * 0.005f);
                                input.resetScroll();
                        }
                        if ((input.startPressed() && !input.isSelectPressed())
                                        || (player.main.getPlayerResource().getPlayerConfig().isWindowHold() && player.main.isTimerOn(TIMER_PLAY) && !player.isNoteEnd())) {
                                if ((autoplay == PlayMode.PLAY || autoplay == PlayMode.PRACTICE) && startpressed) {
                                        processStart.run();
                                } else if ((autoplay == PlayMode.PLAY || autoplay == PlayMode.PRACTICE) && !startpressed) {
                                        Arrays.fill(hschanged, true);
                                }
                                // show-hide lane cover by double-press START
                                if (!startpressed) {
                                        long stime = System.currentTimeMillis();
                                        if (stime < startpressedtime + 500) {
                                                lanerender.setEnableLanecover(!lanerender.isEnableLanecover());
                                                startpressedtime = 0;
                                        } else {
                                                startpressedtime = stime;
                                        }
                                }
                                startpressed = true;
                        } else {
                                startpressed = false;
                        }
                        if(input.isSelectPressed() && !input.startPressed()){
                                if ((autoplay == PlayMode.PLAY || autoplay == PlayMode.PRACTICE) && selectpressed) {
                                        processSelect.run();
                                } else if ((autoplay == PlayMode.PLAY || autoplay == PlayMode.PRACTICE) && !selectpressed) {
                                        Arrays.fill(hschanged, true);
                                }
                                selectpressed = true;
                        } else {
                                selectpressed = false;
                        }
                        if ((input.startPressed() && input.isSelectPressed())) {
                                if(!startAndSelectPressed) {
                                        isChangeLift = !isChangeLift;
                                }
                                startAndSelectPressed = true;
                        } else {
                                startAndSelectPressed = false;
                        }
                }
                long now = System.currentTimeMillis();
                if((input.startPressed() && input.isSelectPressed() && now - exitpressedtime > 1000 )||
                                (player.isNoteEnd() && (input.startPressed() || input.isSelectPressed()))){
                        input.startChanged(false);
                        input.setSelectPressed(false);
                        player.stopPlay();
                }else if(!(input.startPressed() && input.isSelectPressed())){
                        exitpressedtime = now;
                }
                // stop playing
                if (input.isExitPressed()) {
                        input.setExitPressed(false);
                        player.stopPlay();
                }
                // play speed change (autoplay or replay only)
                if (autoplay.isAutoPlayMode() || autoplay.isReplayMode()) {
                        if (input.getNumberState()[1]) {
                                player.setPlaySpeed(25);
                        } else if (input.getNumberState()[2]) {
                                player.setPlaySpeed(50);
                        } else if (input.getNumberState()[3]) {
                                player.setPlaySpeed(200);
                        } else if (input.getNumberState()[4]) {
                                player.setPlaySpeed(300);
                        } else {
                                player.setPlaySpeed(100);
                        }
                }
        }

        /*
         * 状況に応じてレーンカバー/リフト/HIDDENの表示量を変える
         * レーンカバー: 「レーンカバーがオン」もしくは「リフトとHIDDENが共にオフ」
         * リフト: 「レーンカバーがオフ」
         * HIDDEN: 「レーンカバーがオフ」かつ「リフトがオフ」
         * 「レーンカバーがオフ」で「リフトとHIDDENが共にオン」の時は「START+SELECT短押し」で切り替え
         */
        private void setCoverValue(float value) {
                final LaneRenderer lanerender = player.getLanerender();
                if(lanerender.isEnableLanecover() || (!lanerender.isEnableLift() && !lanerender.isEnableHidden())) {
                        lanerender.setLanecover(lanerender.getLanecover() + value);
                } else if(lanerender.isEnableLift() && (!lanerender.isEnableHidden() || isChangeLift)) {
                        lanerender.setLiftRegion(lanerender.getLiftRegion() - value);
                } else {
                        lanerender.setHiddenCover(lanerender.getHiddenCover() - value);
                }
        }

        /*
         * レーンカバー移動
         * 一定時間入力で高速移動モードに切り替え
         */
        private void coverValueChange(boolean sign) {
                long l = System.currentTimeMillis();
                if(laneCoverStartTiming == Long.MIN_VALUE) laneCoverStartTiming = l;
                if (l - lanecovertiming > 50) {
                        setCoverValue((sign ? 1 : -1) * (l - laneCoverStartTiming > coverSpeedSwitchDuration ? coverChangeMarginHigh : coverChangeMarginLow));
                        lanecovertiming = l;
                }

                final LaneRenderer lanerender = player.getLanerender();
                if (lanerender.getNowBPM() > 0 && getDurationAtStart() > 0) {
                    final PlayConfig playconfig = lanerender.getPlayConfig();
                    lanerender.setGreenValue(getDurationAtStart());
                    if (playconfig.getFixhispeed() != PlayConfig.FIX_HISPEED_OFF) {
                        playconfig.setHispeed((float) ((2400f / (lanerender.getNowBPM() / 100) / getDurationAtStart()) * (1 - (playconfig.isEnablelanecover() ? playconfig.getLanecover() : 0))));
                    }
                }
        }

        void processStart7key() {
                final LaneRenderer lanerender = player.getLanerender();
                final BMSPlayerInputProcessor input = player.main.getInputProcessor();
                boolean[] key = input.getKeystate();

                // change hi speed by START + Keys
                for(int i = 0; i <= 15; i++) {
                        if ((i == 0 || i == 2 || i == 4 || i == 6 || i == 9 || i == 11 || i == 13 || i == 15) && key[i]) {
                                if(!hschanged[i]) {
                                        lanerender.changeHispeed(false);
                                        hschanged[i] = true;
                                }
                        } else if ((i == 1 || i == 3 || i == 5 || i == 10 || i == 12 || i == 14) && key[i]) {
                                if(!hschanged[i]) {
                                        lanerender.changeHispeed(true);
                                        hschanged[i] = true;
                                }
                        } else {
                                hschanged[i] = false;
                        }
                }

                // move lane cover by START + Scratch
                if (key[7] || key[8] || key[16] || key[17]) {
                        coverValueChange(key[7] || key[16]);
                } else if(laneCoverStartTiming != Long.MIN_VALUE) {
                        laneCoverStartTiming = Long.MIN_VALUE;
                }
        }

        void processSelect7key() {
                final LaneRenderer lanerender = player.getLanerender();
                final BMSPlayerInputProcessor input = player.main.getInputProcessor();
                boolean[] key = input.getKeystate();

                // change duration by SELECT + Scratch
                if (key[7] || key[8] || key[16] || key[17]) {
                        long l = System.currentTimeMillis();
                        if (l - lanecovertiming > 50) {
                                lanerender.setGreenValue(lanerender.getGreenValue() + (key[7] || key[16] ? 1 : -1));
                                lanecovertiming = l;
                        }
                }

                // change duration by SELECT + Keys
                for(int i = 0; i <= 15; i++) {
                        if ((i == 1 || i == 3 || i == 5 || i == 10 || i == 12 || i == 14) && key[i]) {
                                if(!hschanged[i]) {
                                        lanerender.setGreenValue(lanerender.getGreenValue() -1);
                                        hschanged[i] = true;
                                }
                        } else if ((i == 0 || i == 2 || i == 4 || i == 6 || i == 9 || i == 11 || i == 13 || i == 15) && key[i])  {
                                if(!hschanged[i]) {
                                        lanerender.setGreenValue(lanerender.getGreenValue() +1);
                                        hschanged[i] = true;
                                }
                        } else {
                                hschanged[i] = false;
                        }
                }
        }

        void processStart5key() {
                final LaneRenderer lanerender = player.getLanerender();
                final BMSPlayerInputProcessor input = player.main.getInputProcessor();
                boolean[] key = input.getKeystate();

                // change hi speed by START + Keys
                for(int i = 0; i <= 11; i++) {
                        if ((i == 0 || i == 2 || i == 4 || i == 7 || i == 9 || i == 11) && key[i]) {
                                if(!hschanged[i]) {
                                        lanerender.changeHispeed(false);
                                        hschanged[i] = true;
                                }
                        } else if ((i == 1 || i == 3 || i == 8 || i == 10) && key[i]) {
                                if(!hschanged[i]) {
                                        lanerender.changeHispeed(true);
                                        hschanged[i] = true;
                                }
                        } else {
                                hschanged[i] = false;
                        }
                }

                // move lane cover by START + Scratch
                if (key[5] || key[6] || key[12] || key[13]) {
                        coverValueChange(key[5] || key[12]);
                } else if(laneCoverStartTiming != Long.MIN_VALUE) {
                        laneCoverStartTiming = Long.MIN_VALUE;
                }
        }

        void processSelect5key() {
                final LaneRenderer lanerender = player.getLanerender();
                final BMSPlayerInputProcessor input = player.main.getInputProcessor();
                boolean[] key = input.getKeystate();

                // change duration by SELECT + Scratch
                if (key[5] || key[6] || key[12] || key[13]) {
                        long l = System.currentTimeMillis();
                        if (l - lanecovertiming > 50) {
                                lanerender.setGreenValue(lanerender.getGreenValue() + (key[5] || key[12] ? 1 : -1));
                                lanecovertiming = l;
                        }
                }

                // change duration by SELECT + Keys
                for(int i = 0; i <= 11; i++) {
                        if ((i == 1 || i == 3 || i == 8 || i == 10) && key[i]) {
                                if(!hschanged[i]) {
                                        lanerender.setGreenValue(lanerender.getGreenValue() -1);
                                        hschanged[i] = true;
                                }
                        } else if ((i == 0 || i == 2 || i == 4 || i == 7 || i == 9 || i == 11) && key[i])  {
                                if(!hschanged[i]) {
                                        lanerender.setGreenValue(lanerender.getGreenValue() +1);
                                        hschanged[i] = true;
                                }
                        } else {
                                hschanged[i] = false;
                        }
                }
        }

        void processStart9key() {
                final LaneRenderer lanerender = player.getLanerender();
                final BMSPlayerInputProcessor input = player.main.getInputProcessor();
                boolean[] key = input.getKeystate();

                // change hi speed by START + Keys(0-6)
                for(int i = 0; i <= 6; i++) {
                        if (i % 2 == 1 && key[i]) {
                                if(!hschanged[i]) {
                                        lanerender.changeHispeed(true);
                                        hschanged[i] = true;
                                }
                        } else if (i % 2 == 0 && key[i])  {
                                if(!hschanged[i]) {
                                        lanerender.changeHispeed(false);
                                        hschanged[i] = true;
                                }
                        } else {
                                hschanged[i] = false;
                        }
                }

                // move lane cover by START + Keys(7-8)
                if (key[7] || key[8]) {
                        coverValueChange(key[8]);
                } else if(laneCoverStartTiming != Long.MIN_VALUE) {
                        laneCoverStartTiming = Long.MIN_VALUE;
                }
        }

        void processSelect9key() {
                final LaneRenderer lanerender = player.getLanerender();
                final BMSPlayerInputProcessor input = player.main.getInputProcessor();
                boolean[] key = input.getKeystate();

                // change duration by SELECT + Keys
                for(int i = 0; i <= 8; i++) {
                        if (i % 2 == 1 && key[i]) {
                                if(!hschanged[i]) {
                                        lanerender.setGreenValue(lanerender.getGreenValue() -1);
                                        hschanged[i] = true;
                                }
                        } else if (i % 2 == 0 && key[i])  {
                                if(!hschanged[i]) {
                                        lanerender.setGreenValue(lanerender.getGreenValue() +1);
                                        hschanged[i] = true;
                                }
                        } else {
                                hschanged[i] = false;
                        }
                }
        }

        void processStart24key() {
                final LaneRenderer lanerender = player.getLanerender();
                final BMSPlayerInputProcessor input = player.main.getInputProcessor();
                boolean[] key = input.getKeystate();
                boolean wheel = false;

                // change duration by START + Keys/Wheel
                for(int i = 0; i < 52; i++) {
                        int j = i % 26;
                        if (key[i] && j < 24) {
                                int k = j % 12;
                                if (k <= 4 ? k % 2 == 1 : k % 2 == 0) {
                                        if (!hschanged[i]) {
                                                lanerender.changeHispeed(true);
                                                hschanged[i] = true;
                                        }
                                } else {
                                        if (!hschanged[i]) {
                                                lanerender.changeHispeed(false);
                                                hschanged[i] = true;
                                        }
                                }
                        } else if (key[i] && j >= 24) {
                                coverValueChange(j == 25);
                                hschanged[i] = false;
                                wheel = true;
                        } else {
                                hschanged[i] = false;
                        }
                }
                if(!wheel && laneCoverStartTiming != Long.MIN_VALUE) laneCoverStartTiming = Long.MIN_VALUE;
        }

        void processSelect24key() {
                final LaneRenderer lanerender = player.getLanerender();
                final BMSPlayerInputProcessor input = player.main.getInputProcessor();
                boolean[] key = input.getKeystate();

                // change duration by SELECT + Keys/Wheel
                for(int i = 0; i < 52; i++) {
                        int j = i % 26;
                        if (key[i] && j < 24) {
                                int k = j % 12;
                                if (k <= 4 ? k % 2 == 1 : k % 2 == 0) {
                                        if (!hschanged[i]) {
                                                lanerender.setGreenValue(lanerender.getGreenValue() - 1);
                                                hschanged[i] = true;
                                        }
                                } else {
                                        if (!hschanged[i]) {
                                                lanerender.setGreenValue(lanerender.getGreenValue() + 1);
                                                hschanged[i] = true;
                                        }
                                }
                        } else if (key[i] && j >= 24) {
                                long l = System.currentTimeMillis();
                                if (l - lanecovertiming > 50) {
                                        lanerender.setGreenValue(lanerender.getGreenValue() + (j == 24 ? 1 : -1));
                                        lanecovertiming = l;
                                }
                                hschanged[i] = false;
                        } else {
                                hschanged[i] = false;
                        }
                }
        }

        public void setDurationAtStart(int durationAtStart) {
            this.durationAtStart = durationAtStart;
        }

        public int getDurationAtStart() {
            return this.durationAtStart;
        }
}
