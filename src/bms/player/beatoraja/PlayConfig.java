package bms.player.beatoraja;

import bms.player.beatoraja.play.JudgeAlgorithm;
import com.badlogic.gdx.math.MathUtils;

/**
 * プレイコンフィグ。モード毎に保持するべき値についてはこちらに格納する
 *
 * @author exch
 */
public class PlayConfig implements Cloneable {

	/**
	 * ハイスピード。1.0で等速
	 */
	private float hispeed = 1.0f;
	/**
	 * デュレーション(ノーツ表示時間)
	 */
	private int duration = 500;
	/**
	 * ハイスピード固定。固定する場合はデュレーションが有効となり、固定しない場合はハイスピードが有効になる
	 */
	private int fixhispeed = FIX_HISPEED_MAINBPM;

	public static final int FIX_HISPEED_OFF = 0;
	public static final int FIX_HISPEED_STARTBPM = 1;
	public static final int FIX_HISPEED_MAXBPM = 2;
	public static final int FIX_HISPEED_MAINBPM = 3;
	public static final int FIX_HISPEED_MINBPM = 4;

	/**
	 * ハイスピード変化間隔
	 */
	public float hispeedmargin = 0.25f;

	/**
	 * レーンカバー表示量(0-1)
	 */
	private float lanecover = 0.2f;
	/**
	 * レーンカバー使用
	 */
	private boolean enablelanecover = true;
	/**
	 * リフト表示量(0-1)
	 */
	private float lift = 0.1f;
	/**
	 * リフト使用
	 */
	private boolean enablelift = false;
	/**
	 * HIDDEN表示量(0-1)
	 */
	private float hidden = 0.1f;
	/**
	 * HIDDEN使用
	 */
	private boolean enablehidden = false;

	/**
	 * レーンカバー変化間隔(低速)
	 */
	private float lanecovermarginlow = 0.001f;
	/**
	 * レーンカバー変化間隔(高速)
	 */
	private float lanecovermarginhigh = 0.01f;
	/**
	 * レーンカバー変化速度切り替え時間
	 */
	private int lanecoverswitchduration = 500;
	/**
	 * HI-SPEED固定自動調整：レーンカバーを変化するとHI-SPEED固定を現在のBPMに自動的に調整する（皿チョン）
	 */
	private boolean hispeedautoadjust = false;
	/**
	 * Auto Judge Timing
	 */
	private int autojudgetiming = 0;
	/**
	 * 判定アルゴリズム
	 */
	private String judgetype = JudgeAlgorithm.Combo.name();

	public PlayConfig() {
	}

	public float getHispeed() {
		return hispeed;
	}

	public void setHispeed(float hispeed) {
		this.hispeed = hispeed;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public float getHispeedMargin() {
		return hispeedmargin;
	}

	public void setHispeedMargin(float hispeedmargin) {
		this.hispeedmargin = hispeedmargin;
	}

	public int getFixhispeed() {
		return fixhispeed;
	}

	public void setFixhispeed(int fixhispeed) {
		this.fixhispeed = fixhispeed;
	}

	public float getLanecover() {
		return lanecover;
	}

	public void setLanecover(float lanecover) {
		this.lanecover = lanecover;
	}

	public boolean isEnablelanecover() {
		return enablelanecover;
	}

	public void setEnablelanecover(boolean enablelanecover) {
		this.enablelanecover = enablelanecover;
	}

	public float getLift() {
		return lift;
	}

	public void setLift(float lift) {
		this.lift = lift;
	}

	public boolean isEnablelift() {
		return enablelift;
	}

	public void setEnablelift(boolean enablelift) {
		this.enablelift = enablelift;
	}

	public float getHidden() {
		return hidden;
	}

	public void setHidden(float hidden) {
		this.hidden = hidden;
	}

	public boolean isEnablehidden() {
		return enablehidden;
	}

	public void setEnablehidden(boolean enablehidden) {
		this.enablehidden = enablehidden;
	}

	public float getLanecovermarginlow() {
		return lanecovermarginlow;
	}

	public void setLanecovermarginlow(float lanecovermarginlow) {
		this.lanecovermarginlow = lanecovermarginlow;
	}

	public float getLanecovermarginhigh() {
		return lanecovermarginhigh;
	}

	public void setLanecovermarginhigh(float lanecovermarginhigh) {
		this.lanecovermarginhigh = lanecovermarginhigh;
	}

	public int getLanecoverswitchduration() {
		return lanecoverswitchduration;
	}

	public void setLanecoverswitchduration(int lanecoverswitchduration) {
		this.lanecoverswitchduration = lanecoverswitchduration;
	}

	public boolean isEnableHispeedAutoAdjust() {
		return hispeedautoadjust;
	}

	public void setHispeedAutoAdjust(boolean hispeedautoadjust) {
		this.hispeedautoadjust = hispeedautoadjust;
	}
	
	public int getAutoJudgeTiming() {
		return autojudgetiming;
	}

	public void setAutoJudgeTiming(int autojudgetiming) {
		this.autojudgetiming = autojudgetiming;
	}
	
	public String getJudgetype() {
		for(JudgeAlgorithm type : JudgeAlgorithm.values()) {
			if(type.name().equals(judgetype)) {
				return judgetype;
			}
		}
		judgetype = JudgeAlgorithm.Combo.name();
		return judgetype;
	}

	public void setJudgetype(String judgetype) {
		this.judgetype = judgetype;
	}

	public void validate() {
		hispeed = MathUtils.clamp(hispeed, 0.01f, 20);
		duration = MathUtils.clamp(duration, 1, 10000);
		hispeedmargin = MathUtils.clamp(hispeedmargin, 0f, 10f);
		fixhispeed = MathUtils.clamp(fixhispeed, 0, FIX_HISPEED_MINBPM);
		lanecover = MathUtils.clamp(lanecover, 0f, 1f);
		lift = MathUtils.clamp(lift, 0f, 1f);
		hidden = MathUtils.clamp(hidden, 0f, 1f);
		lanecovermarginlow = MathUtils.clamp(lanecovermarginlow, 0f, 1f);
		lanecovermarginhigh = MathUtils.clamp(lanecovermarginhigh, 0f, 1f);
		lanecoverswitchduration = MathUtils.clamp(lanecoverswitchduration, 0, 1000000);
		if(JudgeAlgorithm.getIndex(judgetype) == -1) {
			judgetype = JudgeAlgorithm.Combo.name();
		}
	}

	public PlayConfig clone() {
		try {
			return (PlayConfig) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
