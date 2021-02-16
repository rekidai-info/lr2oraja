package bms.player.beatoraja.select;

import bms.player.beatoraja.select.bar.Bar;
import bms.player.beatoraja.select.bar.FolderBar;
import bms.player.beatoraja.select.bar.SongBar;

import java.util.Comparator;

/**
 * バーのソートアルゴリズム
 *
 * @author exch
 */
public enum BarSorter implements Comparator<Bar> {

	/**
	 * 楽曲/タイトル名ソート
	 */
	NAME_SORTER("TITLE") {
		@Override
		public int compare(Bar o1, Bar o2) {
			if (!(o1 instanceof SongBar || o1 instanceof FolderBar) && !(o2 instanceof SongBar || o2 instanceof FolderBar)) {
				return 0;
			}
			if (!(o1 instanceof SongBar || o1 instanceof FolderBar)) {
				return 1;
			}
			if (!(o2 instanceof SongBar || o2 instanceof FolderBar)) {
				return -1;
			}
			return o1.getTitle().compareToIgnoreCase(o2.getTitle());
		}
	},
	/**
	 * アーティスト名ソート
	 */
	ARTIST_SORTER("ARTIST") {
		@Override
		public int compare(Bar o1, Bar o2) {
			if (!(o1 instanceof SongBar) || !(o2 instanceof SongBar)) {
				return NAME_SORTER.compare(o1, o2);
			}
			return o1.getArtist().compareToIgnoreCase(o2.getArtist());
		}
	},
	/**
	 * 楽曲のBPMソート
	 */
	BPM_SORTER("BPM") {
		@Override
		public int compare(Bar o1, Bar o2) {
			if (!(o1 instanceof SongBar) || !(o2 instanceof SongBar)) {
				return NAME_SORTER.compare(o1, o2);
			}
			if (!((SongBar)o1).existsSong() && !((SongBar)o2).existsSong()) {
				return 0;
			}
			if (!((SongBar)o1).existsSong()) {
				return 1;
			}
			if (!((SongBar)o2).existsSong()) {
				return -1;
			}
			return ((SongBar) o1).getSongData().getMaxbpm() - ((SongBar) o2).getSongData().getMaxbpm();
		}
	},
	/**
	 * 楽曲の長さソート
	 */
	LENGTH_SORTER("LENGTH") {
		@Override
		public int compare(Bar o1, Bar o2) {
			if (!(o1 instanceof SongBar) || !(o2 instanceof SongBar)) {
				return NAME_SORTER.compare(o1, o2);
			}
			if (!((SongBar)o1).existsSong() && !((SongBar)o2).existsSong()) {
				return 0;
			}
			if (!((SongBar)o1).existsSong()) {
				return 1;
			}
			if (!((SongBar)o2).existsSong()) {
				return -1;
			}
			return ((SongBar) o1).getSongData().getLength() - ((SongBar) o2).getSongData().getLength();
		}
	},
	/**
	 * レベルソート
	 */
	LEVEL_SORTER("LEVEL") {
		@Override
		public int compare(Bar o1, Bar o2) {
			if (!(o1 instanceof SongBar) || !(o2 instanceof SongBar)) {
				return NAME_SORTER.compare(o1, o2);
			}
			if (!((SongBar)o1).existsSong() && !((SongBar)o2).existsSong()) {
				return 0;
			}
			if (!((SongBar)o1).existsSong()) {
				return 1;
			}
			if (!((SongBar)o2).existsSong()) {
				return -1;
			}
			return ((SongBar) o1).getSongData().getLevel() - ((SongBar) o2).getSongData().getLevel();
		}
	},
	/**
	 * クリアランプソート
	 */
	LAMP_SORTER("CLEAR LAMP") {
		@Override
		public int compare(Bar o1, Bar o2) {
			if (!(o1 instanceof SongBar) || !(o2 instanceof SongBar)) {
				return NAME_SORTER.compare(o1, o2);
			}
			if (o1.getScore() == null && o2.getScore() == null) {
				return 0;
			}
			if (o1.getScore() == null) {
				return 1;
			}
			if (o2.getScore() == null) {
				return -1;
			}
			return o1.getScore().getClear() - o2.getScore().getClear();
		}
	},
	/**
	 * スコアレートソート
	 */
	SCORE_SORTER("SCORE RATE") {
		@Override
		public int compare(Bar o1, Bar o2) {
			if (!(o1 instanceof SongBar) || !(o2 instanceof SongBar)) {
				return NAME_SORTER.compare(o1, o2);
			}
			final int n1 = o1.getScore() != null ? o1.getScore().getNotes() : 0;
			final int n2 = o2.getScore() != null ? o2.getScore().getNotes() : 0;
			if (n1 == 0 && n2 == 0) {
				return 0;
			}
			if (n1 == 0) {
				return 1;
			}
			if (n2 == 0) {
				return -1;
			}
			return o1.getScore().getExscore() * 1000 / n1 - o2.getScore().getExscore() * 1000 / n2;
		}
	},
	/**
	 * ミスカウントソート
	 */
	MISSCOUNT_SORTER("MISS COUNT") {
		@Override
		public int compare(Bar o1, Bar o2) {
			if (!(o1 instanceof SongBar) || !(o2 instanceof SongBar)) {
				return NAME_SORTER.compare(o1, o2);
			}
			if (o1.getScore() == null && o2.getScore() == null) {
				return 0;
			}
			if (o1.getScore() == null) {
				return 1;
			}
			if (o2.getScore() == null) {
				return -1;
			}
			return o1.getScore().getMinbp() - o2.getScore().getMinbp();
		}
	},
	/**
     * 楽曲の判定ソート
     */
    JUDGE_SORTER("JUDGE") {
        @Override
        public int compare(Bar o1, Bar o2) {
            if (!(o1 instanceof SongBar) || !(o2 instanceof SongBar)) {
                return NAME_SORTER.compare(o1, o2);
            }
            if (!((SongBar)o1).existsSong() && !((SongBar)o2).existsSong()) {
                return 0;
            }
            if (!((SongBar)o1).existsSong()) {
                return 1;
            }
            if (!((SongBar)o2).existsSong()) {
                return -1;
            }
            return ((SongBar) o1).getSongData().getJudge() - ((SongBar) o2).getSongData().getJudge();
        }
    },
    /**
     * 楽曲のプレイカウントソート
     */
    PLAY_COUNT_SORTER("PLAY COUNT") {
        @Override
        public int compare(Bar o1, Bar o2) {
            if (!(o1 instanceof SongBar) || !(o2 instanceof SongBar)) {
                return NAME_SORTER.compare(o1, o2);
            }
            if (o1.getScore() == null && o2.getScore() == null) {
                return 0;
            }
            if (o1.getScore() == null) {
                return 1;
            }
            if (o2.getScore() == null) {
                return -1;
            }
            return o1.getScore().getPlaycount() - o2.getScore().getPlaycount();
        }
    },
    /**
     * 楽曲の更新日付ソート
     */
    PLAY_DATE_SORTER("PLAY DATE") {
        @Override
        public int compare(Bar o1, Bar o2) {
            if (!(o1 instanceof SongBar) || !(o2 instanceof SongBar)) {
                return NAME_SORTER.compare(o1, o2);
            }
            if (o1.getScore() == null && o2.getScore() == null) {
                return 0;
            }
            if (o1.getScore() == null) {
                return 1;
            }
            if (o2.getScore() == null) {
                return -1;
            }
            return (int) (o1.getScore().getDate() - o2.getScore().getDate());
        }
    }
	;

	/**
	 * ソート名称
	 */
	public final String name;

	private BarSorter(String name) {
		this.name = name;
	}
}
