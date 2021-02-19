package bms.player.beatoraja.pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import com.badlogic.gdx.utils.IntArray;

import bms.model.BMSModel;
import bms.model.LongNote;
import bms.model.MineNote;
import bms.model.Mode;
import bms.model.Note;
import bms.model.TimeLine;
import bms.player.beatoraja.PlayerConfig;

/**
 * 譜面オプションの抽象クラス
 *
 * @author exch
 */
public abstract class PatternModifier {

	/**
	 * 譜面変更のアシストレベル
	 */
	private AssistLevel assist = AssistLevel.NONE;

	/**
	 * 1P側、2P側どちらの譜面を変更するか
	 */
	private int modifyTargetSide;

	public static final int SIDE_1P = 0;
	public static final int SIDE_2P = 1;

	public PatternModifier() {

	}

	public PatternModifier(int assist) {
		this.assist = AssistLevel.values()[assist];
	}

	public abstract List<PatternModifyLog> modify(BMSModel model);

	/**
	 * 譜面変更ログの通りに譜面オプションをかける
	 *
	 * @param model
	 *            譜面オプションをかける対象のBMSModel
	 * @param log
	 *            譜面変更ログ
	 */
	public static void modify(BMSModel model, List<PatternModifyLog> log) {
		for (TimeLine tl : model.getAllTimeLines()) {
			PatternModifyLog pm = null;
			for (PatternModifyLog pms : log) {
				if (pms.section == tl.getSection()) {
					pm = pms;
					break;
				}
			}
			if (pm != null) {
				int lanes = model.getMode().key;
				Note[] notes = new Note[lanes];
				Note[] hnotes = new Note[lanes];
				for (int i = 0; i < lanes; i++) {
					final int mod = i < pm.modify.length ? pm.modify[i] : i;
					notes[i] = tl.getNote(mod);
					hnotes[i] = tl.getHiddenNote(mod);
				}
				for (int i = 0; i < lanes; i++) {
					tl.setNote(i, notes[i]);
					tl.setHiddenNote(i, hnotes[i]);
				}
			}
		}
	}
	
	public static int[] order(final BMSModel model, final List<PatternModifyLog> log) {
	    if (log == null) {
	        return null;
	    }
	    
	    final int lanes = model.getMode().key;
	    final int[] result = IntStream.rangeClosed(1, lanes).toArray();
	    
	    if (log.isEmpty()) {
            return result;
        }

	    for (TimeLine tl : model.getAllTimeLines()) {
	        PatternModifyLog pm = null;
            for (PatternModifyLog pms : log) {
                if (pms.section == tl.getSection()) {
                    pm = pms;
                    break;
                }
	        }

            if (pm != null) {
                final int[] original = IntStream.rangeClosed(1, lanes).toArray();
        
                for (int i = 0; i < lanes; i++) {
                    final int mod = i < pm.modify.length ? pm.modify[i] : i;
                    result[i] = original[mod];
                }
            }
	    }
        
        return result;
    }

	public static List<PatternModifyLog> merge(List<PatternModifyLog> log, List<PatternModifyLog> log2) {
		List<PatternModifyLog> result = new ArrayList(Math.max(log.size(), log2.size()));
		for (PatternModifyLog pml : log) {
			boolean b = true;
			for (PatternModifyLog pml2 : log2) {
				if (pml.section == pml2.section) {
					int[] newmod = new int[Math.max(pml.modify.length, pml2.modify.length)];
					for (int i = 0; i < newmod.length; i++) {
						if (i >= pml.modify.length) {
							newmod[i] = pml2.modify[i];
						} else if (i >= pml2.modify.length) {
							newmod[i] = pml.modify[i];
						} else {
							newmod[i] = pml.modify[pml2.modify[i]];
						}
					}
					result.add(new PatternModifyLog(pml.section, newmod));
					b = true;
					break;
				}
			}
			if (b) {
				result.add(pml);
			}
		}

		for (PatternModifyLog pml2 : log2) {
			boolean b = true;
			for (PatternModifyLog pml : log) {
				if (pml2.section == pml.section) {
					b = false;
					break;
				}
			}
			if (b) {
				for (int index = 0; index < result.size(); index++) {
					if (pml2.section < result.get(index).section) {
						result.add(index, pml2);
						b = false;
						break;
					}
				}
				if (b) {
					result.add(pml2);
				}
			}
		}
		return result;
	}

	public AssistLevel getAssistLevel() {
		return assist;
	}

	protected void setAssistLevel(AssistLevel assist) {
		this.assist = (assist != null ? assist : AssistLevel.NONE);
	}

	public int getModifyTarget() {
		return modifyTargetSide;
	}

	public void setModifyTarget(int type) {
		this.modifyTargetSide = type;
	}

    public static PatternModifier create(int id, int side, Mode mode, PlayerConfig config) {
        return create(id, side, mode, config, null);
    }
    
    public static PatternModifier create(int id, List<String> randomOrder, int side, Mode mode, PlayerConfig config) {
        return create(id, randomOrder, side, mode, config, null);
    }

    /**
     *
     * @param id 譜面オプションID
     * @param side 譜面オプションサイド(1P or 2P)
     * @param mode 譜面のモード
     * @param seed 譜面オプションの固定用変数(LaneShuffleのみ)
     * @return
     */
    public static PatternModifier create(int id, int side, Mode mode, PlayerConfig config, int[] seed) {
        return create(id, null, side, mode, config, seed);
    }
    
    public static PatternModifier create(int id, List<String> randomOrder, int side, Mode mode, PlayerConfig config, int[] seed) {
		PatternModifier pm = null;
		Random r = Random.getRandom(id);
		switch (r) {
			case IDENTITY:
			pm = new DummyModifier();
			break;
			case MIRROR:
			case R_RANDOM:
			case RANDOM_EX:
			case CROSS:
			pm = new LaneShuffleModifier(r);
			break;
			case RANDOM:
			pm = new LaneShuffleModifier(r, randomOrder);
			break;
			case S_RANDOM:
			case SPIRAL:
			case H_RANDOM:
			case S_RANDOM_EX:
			pm = new NoteShuffleModifier(r, mode, config);
			break;
			case ALL_SCR:
			pm = new NoteShuffleModifier(r, side, mode, config);
			break;
		}

		if (pm != null) {
			pm.setModifyTarget(side);
		}
		return pm;
	}

	/**
	 * 変更対象のレーン番号が格納された配列を返す
	 * @param mode
	 * プレイモード
	 * @param containsScratch
	 * スクラッチレーンを含むか
	 * @return レーン番号の配列
	 */
	protected int[] getKeys(Mode mode, boolean containsScratch) {
		int key = (modifyTargetSide == SIDE_2P)
				? mode.key / mode.player
				: 0;
		if (key == mode.key) {
			return new int[0];
		} else {
			IntArray keys = new IntArray();
			for (int i = 0; i < mode.key / mode.player; i++) {
				if (containsScratch || !mode.isScratchKey(key + i)) {
					keys.add(key + i);
				}
			}
			return keys.toArray();
		}
	}

	protected static int[] shuffle(int[] keys, List<String> randomOrder) {
		List<Integer> l = new ArrayList<Integer>(keys.length);
		for (int key : keys) {
			l.add(key);
		}
		int max = 0;
		for (int key : keys) {
			max = Math.max(max, key);
		}
		int[] result = new int[max + 1];
		for (int i = 0; i < result.length; i++) {
			result[i] = i;
		}
		for (int lane = 0; lane < keys.length; lane++) {
			if (randomOrder != null && keys.length == randomOrder.size()) {
			    int r = Integer.parseInt(randomOrder.get(lane)) - 1;
			    result[keys[lane]] = l.get(r);
			} else {
			    int r = (int) (ThreadLocalRandom.current().nextDouble() * l.size());
			    result[keys[lane]] = l.get(r);
	            l.remove(r);
			}
		}

		return result;
	}

	protected static int[] rotate(int[] keys) {
		boolean inc = (int) (ThreadLocalRandom.current().nextDouble() * 2) == 1;
		int start = (int) (ThreadLocalRandom.current().nextDouble() * (keys.length - 1)) + (inc ? 1 : 0);
		return rotate(keys, start, inc);
	}

	protected static int[] rotate(int[] keys, int start, boolean inc) {
		int max = 0;
		for (int key : keys) {
			max = Math.max(max, key);
		}
		int[] result = new int[max + 1];
		for (int i = 0; i < result.length; i++) {
			result[i] = i;
		}
		for (int lane = 0, rlane = start; lane < keys.length; lane++) {
			result[keys[lane]] = keys[rlane];
			rlane = inc ? (rlane + 1) % keys.length : (rlane + keys.length - 1) % keys.length;
		}
		return result;
	}

	protected static void moveToBackground(TimeLine[] tls, TimeLine tl, int lane) {
		Note n = tl.getNote(lane);
		if(n == null) {
			return;
		}
		if(n instanceof LongNote) {
			LongNote pln = ((LongNote) tl.getNote(lane)).getPair();
			for(TimeLine tl2 : tls) {
				if(tl2.getNote(lane) == pln) {
					tl2.addBackGroundNote(pln);
					tl2.setNote(lane, null);
					break;
				}
			}
		}

		if(!(n instanceof MineNote)) {
			tl.addBackGroundNote(tl.getNote(lane));
		}
		tl.setNote(lane, null);

	}

	static class DummyModifier extends PatternModifier {

		public DummyModifier() {
			super(0);
		}

		@Override
		public List<PatternModifyLog> modify(BMSModel model) {
			return Collections.emptyList();
		}

	}

	public enum AssistLevel {
		NONE, LIGHT_ASSIST, ASSIST;
	}
}
