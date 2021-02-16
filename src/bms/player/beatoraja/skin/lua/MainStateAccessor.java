package bms.player.beatoraja.skin.lua;

import bms.player.beatoraja.MainState;
import bms.player.beatoraja.ScoreData;
import bms.player.beatoraja.play.BMSPlayer;
import bms.player.beatoraja.skin.SkinObject;
import bms.player.beatoraja.skin.SkinPropertyMapper;
import bms.player.beatoraja.skin.property.*;

import java.util.Calendar;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;

/**
 * 実行時にスキンからMainStateの数値などにアクセスできる関数を提供する
 */
public class MainStateAccessor {

	private final MainState state;

	public MainStateAccessor(MainState state) {
		this.state = state;
	}

	public void export(LuaTable table) {
		// 汎用関数(ID指定での取得・設定など)
		table.set("option", this.new option());
		table.set("number", this.new number());
		table.set("float_number", this.new float_number());
		table.set("text", this.new text());
		table.set("offset", this.new offset());
		table.set("timer", this.new timer());
		table.set("timer_off_value", MainStateAccessor.timer_off_value);
		table.set("time", this.new time());
		table.set("set_timer", this.new set_timer());
		table.set("event_exec", this.new event_exec());

		// 具体的な数値の取得・設定など
		table.set("rate", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaDouble.valueOf(state.getScoreDataProperty().getNowRate());
			}
		});
		table.set("exscore", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaDouble.valueOf(state.getScoreDataProperty().getNowEXScore());
			}
		});
		table.set("rate_best", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaDouble.valueOf(state.getScoreDataProperty().getNowBestScoreRate());
			}
		});
		table.set("bestscore", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaDouble.valueOf(state.getScoreDataProperty().getNowBestScore());
            }
        });
		table.set("score", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaDouble.valueOf(state.getScoreDataProperty().getNowScore());
            }
        });
		table.set("score_rival", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaDouble.valueOf(state.getScoreDataProperty().getNowRivalScore());
            }
        });
		table.set("exscore_best", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaDouble.valueOf(state.getScoreDataProperty().getBestScore());
			}
		});
		table.set("rate_rival_now", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaDouble.valueOf(state.getScoreDataProperty().getNowRivalScoreRate());
            }
        });
		table.set("rate_rival", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaDouble.valueOf(state.getScoreDataProperty().getRivalScoreRate());
			}
		});
		table.set("exscore_rival", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaDouble.valueOf(state.getScoreDataProperty().getRivalScore());
			}
		});
		table.set("volume_sys", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaDouble.valueOf(state.main.getConfig().getSystemvolume());
			}
		});
		table.set("set_volume_sys", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue value) {
				state.main.getConfig().setSystemvolume(value.tofloat());
				return LuaBoolean.TRUE;
			}
		});
		table.set("volume_key", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaDouble.valueOf(state.main.getConfig().getKeyvolume());
			}
		});
		table.set("set_volume_key", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue value) {
				state.main.getConfig().setKeyvolume(value.tofloat());
				return LuaBoolean.TRUE;
			}
		});
		table.set("volume_bg", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaDouble.valueOf(state.main.getConfig().getBgvolume());
			}
		});
		table.set("set_volume_bg", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue value) {
				state.main.getConfig().setBgvolume(value.tofloat());
				return LuaBoolean.TRUE;
			}
		});
		table.set("judge", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue value) {
				return LuaInteger.valueOf(state.getJudgeCount(value.toint(), true) + state.getJudgeCount(value.toint(), false));
			}
		});
		table.set("gauge", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				if (state instanceof BMSPlayer) {
					BMSPlayer player = (BMSPlayer) state;
					return LuaDouble.valueOf(player.getGauge().getValue());
				}
				return LuaInteger.ZERO;
			}
		});
		table.set("gauge_type", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				if (state instanceof BMSPlayer) {
					BMSPlayer player = (BMSPlayer) state;
					return LuaDouble.valueOf(player.getGauge().getType());
				}
				return LuaInteger.ZERO;
			}
		});
		
		final Calendar lastPlayCal = Calendar.getInstance();
		lastPlayCal.setTimeInMillis(state.main.getPlayerConfig().getLastPlayTime() / 1000);
		
		table.set("last_play_beatoraja_micro_sec_time", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaNumber.valueOf(state.main.getPlayerConfig().getLastPlayTime());
            }
        });
		table.set("last_play_beatoraja_year", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaNumber.valueOf(lastPlayCal.get(Calendar.YEAR));
            }
        });
		table.set("last_play_beatoraja_month", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaNumber.valueOf(lastPlayCal.get(Calendar.MONTH) + 1);
            }
        });
        table.set("last_play_beatoraja_day", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaNumber.valueOf(lastPlayCal.get(Calendar.DAY_OF_MONTH));
            }
        });
        table.set("last_play_beatoraja_hour", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaNumber.valueOf(lastPlayCal.get(Calendar.HOUR_OF_DAY));
            }
        });
        table.set("last_play_beatoraja_minute", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaNumber.valueOf(lastPlayCal.get(Calendar.MINUTE));
            }
        });
        table.set("last_play_beatoraja_second", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaNumber.valueOf(lastPlayCal.get(Calendar.SECOND));
            }
        });
		
		// とりあえず NUMBER_ プロパティには追加せず関数として追加しておく
		final ScoreData score = state.getScoreDataProperty().getScoreData();
		final ScoreData rivalScore = state.getScoreDataProperty().getRivalScoreData();
		final Calendar scoreCal = score == null ? null : Calendar.getInstance();
		final Calendar rivalScoreCal = rivalScore == null ? null : Calendar.getInstance();

		if (scoreCal != null) {
		    scoreCal.setTimeInMillis(score.getDate() * 1000);
		}
		if (rivalScoreCal != null) {
		    rivalScoreCal.setTimeInMillis(rivalScore.getDate() * 1000);
        }
		
		// 自己スコアの日付
	    table.set("score_date_sec_time", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                if (score != null) {
                    return LuaNumber.valueOf(score.getDate());
                }
                return LuaInteger.ZERO;
            }
        });
	    table.set("score_date_year", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                if (scoreCal != null) {
                    return LuaNumber.valueOf(scoreCal.get(Calendar.YEAR));
                }
                return LuaInteger.ZERO;
            }
        });
	    table.set("score_date_month", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                if (scoreCal != null) {
                    return LuaNumber.valueOf(scoreCal.get(Calendar.MONTH) + 1);
                }
                return LuaInteger.ZERO;
            }
        });
	    table.set("score_date_day", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                if (scoreCal != null) {
                    return LuaNumber.valueOf(scoreCal.get(Calendar.DAY_OF_MONTH));
                }
                return LuaInteger.ZERO;
            }
        });
	    table.set("score_date_hour", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                if (scoreCal != null) {
                    return LuaNumber.valueOf(scoreCal.get(Calendar.HOUR_OF_DAY));
                }
                return LuaInteger.ZERO;
            }
        });
	    table.set("score_date_minute", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                if (scoreCal != null) {
                    return LuaNumber.valueOf(scoreCal.get(Calendar.MINUTE));
                }
                return LuaInteger.ZERO;
            }
        });
	    table.set("score_date_second", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                if (scoreCal != null) {
                    return LuaNumber.valueOf(scoreCal.get(Calendar.SECOND));
                }
                return LuaInteger.ZERO;
            }
        });
	    
	    // ライバルスコアの日付
	    table.set("score_date_rival_sec_time", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                if (rivalScore != null) {
                    return LuaNumber.valueOf(rivalScore.getDate());
                }
                return LuaInteger.ZERO;
            }
        });
        table.set("score_date_rival_year", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                if (rivalScoreCal != null) {
                    return LuaNumber.valueOf(rivalScoreCal.get(Calendar.YEAR));
                }
                return LuaInteger.ZERO;
            }
        });
        table.set("score_date_rival_month", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                if (rivalScoreCal != null) {
                    return LuaNumber.valueOf(rivalScoreCal.get(Calendar.MONTH) + 1);
                }
                return LuaInteger.ZERO;
            }
        });
        table.set("score_date_rival_day", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                if (rivalScoreCal != null) {
                    return LuaNumber.valueOf(rivalScoreCal.get(Calendar.DAY_OF_MONTH));
                }
                return LuaInteger.ZERO;
            }
        });
        table.set("score_date_rival_hour", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                if (rivalScoreCal != null) {
                    return LuaNumber.valueOf(rivalScoreCal.get(Calendar.HOUR_OF_DAY));
                }
                return LuaInteger.ZERO;
            }
        });
        table.set("score_date_rival_minute", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                if (rivalScoreCal != null) {
                    return LuaNumber.valueOf(rivalScoreCal.get(Calendar.MINUTE));
                }
                return LuaInteger.ZERO;
            }
        });
        table.set("score_date_rival_second", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                if (rivalScoreCal != null) {
                    return LuaNumber.valueOf(rivalScoreCal.get(Calendar.SECOND));
                }
                return LuaInteger.ZERO;
            }
        });
        
        // 完走した場合にのみカウントされる起動後からのプレイ回数とノーツ数（総プレイ回数と総ノーツ数の仕様も完走時のみのカウントのため）
        table.set("total_play_counts_in_session", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaNumber.valueOf(state.main.getPlayerResource().getTotalPlayCountsInSession());

            }
        });
        table.set("total_play_notes_in_session", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaNumber.valueOf(state.main.getPlayerResource().getTotalPlayNotesInSession());

            }
        });
	}

	/**
	 * ID指定で真理値(OPTION_*)を取得する関数
	 * NOTE: 呼び出しの度にBooleanPropertyを生成しており効率が悪いため非推奨
	 */
	private class option extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue luaValue) {
			BooleanProperty prop = BooleanPropertyFactory.getBooleanProperty(luaValue.toint());
			return LuaBoolean.valueOf(prop.get(state));
		}
	}

	/**
	 * ID指定で整数値(NUMBER_*)を取得する関数
	 * NOTE: 呼び出しの度にIntegerPropertyを生成しており効率が悪いため非推奨
	 */
	private class number extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue luaValue) {
			IntegerProperty prop = IntegerPropertyFactory.getIntegerProperty(luaValue.toint());
			return LuaNumber.valueOf(prop.get(state));
		}
	}

	/**
	 * ID指定で小数値(SLIDER_* | BARGRAPH_*)を取得する関数
	 * NOTE: 呼び出しの度にFloatPropertyを生成しており効率が悪いため非推奨
	 */
	private class float_number extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue luaValue) {
			FloatProperty prop = FloatPropertyFactory.getFloatProperty(luaValue.toint());
			return LuaDouble.valueOf(prop.get(state));
		}
	}

	/**
	 * ID指定で文字列(STRING_*)を取得する関数
	 * NOTE: 呼び出しの度にStringPropertyを生成しており効率が悪いため非推奨
	 */
	private class text extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue luaValue) {
			StringProperty prop = StringPropertyFactory.getStringProperty(luaValue.toint());
			return LuaString.valueOf(prop.get(state));
		}
	}

	/**
	 * ID指定でオフセット(OFFSET_*)を取得する関数
	 */
	private class offset extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue value) {
			SkinObject.SkinOffset offset = state.getOffsetValue(value.toint());
			LuaTable offsetTable = new LuaTable();
			offsetTable.set("x", offset.x);
			offsetTable.set("y", offset.y);
			offsetTable.set("w", offset.w);
			offsetTable.set("h", offset.h);
			offsetTable.set("r", offset.r);
			offsetTable.set("a", offset.a);
			return offsetTable;
		}
	}

	/**
	 * ID指定でタイマー(TIMER_* またはカスタムタイマー)の値を取得する関数
	 * return: ONになった時刻 (micro sec) | timer_off_value (OFFのとき)
	 */
	private class timer extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue value) {
			return LuaNumber.valueOf(state.main.getMicroTimer(value.toint()));
		}
	}

	/**
	 * タイマーがOFFの状態を表す定数
	 */
	private static final Long timer_off_value = Long.MIN_VALUE;

	/**
	 * 現在時刻を取得する関数
	 * return: 時刻 (micro sec)
	 */
	private class time extends ZeroArgFunction {
		@Override
		public LuaValue call() {
			return LuaNumber.valueOf(state.main.getNowMicroTime());
		}
	}

	/**
	 * ID指定でタイマーの値を設定する関数
	 * ゲームプレイに影響するタイマーは設定不可
	 * param timerValue: ONになった時刻 (micro sec) | timer_off_value (OFFにする場合)
	 */
	private class set_timer extends TwoArgFunction {
		@Override
		public LuaValue call(LuaValue timerId, LuaValue timerValue) {
			int id = timerId.toint();
			if (!SkinPropertyMapper.isTimerWritableBySkin(id))
				throw new IllegalArgumentException("指定されたタイマーはスキンから変更できません");
			state.main.setMicroTimer(id, timerValue.tolong());
			return LuaBoolean.TRUE;
		}
	}

	/**
	 * ID指定でイベント(BUTTON_* またはカスタムイベント)を実行する関数
	 * ゲームプレイに影響するイベントは実行不可
	 */
	private class event_exec extends VarArgFunction {
		@Override
		public LuaValue call(LuaValue luaValue) {
			state.executeEvent(getId(luaValue));
			return LuaBoolean.TRUE;
		}

		@Override
		public LuaValue call(LuaValue luaValue, LuaValue arg1) {
			state.executeEvent(getId(luaValue), arg1.toint());
			return LuaBoolean.TRUE;
		}

		@Override
		public LuaValue call(LuaValue luaValue, LuaValue arg1, LuaValue arg2) {
			state.executeEvent(getId(luaValue), arg1.toint(), arg2.toint());
			return LuaBoolean.TRUE;
		}

		private int getId(LuaValue luaValue) {
			int id = luaValue.toint();
			if (!SkinPropertyMapper.isEventRunnableBySkin(id))
				throw new IllegalArgumentException("指定されたイベントはスキンから実行できません");
			return id;
		}
	}
}
