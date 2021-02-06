package bms.player.beatoraja.select;

import bms.player.beatoraja.Config;
import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.Resolution;
import bms.player.beatoraja.ScoreDatabaseAccessor;
import bms.player.beatoraja.select.bar.SearchWordBar;
import bms.player.beatoraja.select.bar.SongBar;
import bms.player.beatoraja.song.SongData;

import java.io.File;
import java.util.logging.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * 楽曲検索用テキストフィールド
 *
 * @author exch
 */
public class SearchTextField extends Stage {
	/**
	 * フォント生成用クラス
	 */
	private FreeTypeFontGenerator generator;
	/**
	 * フォント
	 */
	private BitmapFont searchfont;

	private TextField search;
	/**
	 * 画面クリック感知用Actor
	 */
	private Group screen;

	public SearchTextField(MusicSelector selector, Resolution resolution) {
		super(new FitViewport(resolution.width, resolution.height));

		final Rectangle r = ((MusicSelectSkin) selector.getSkin()).getSearchTextRegion();

		try {
			generator = new FreeTypeFontGenerator(Gdx.files.internal("skin/default/VL-Gothic-Regular.ttf"));
			FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
			parameter.size = (int) r.height;
			parameter.incremental = true;
			searchfont = generator.generateFont(parameter);

			final TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle(); // background
			textFieldStyle.font = searchfont;
			textFieldStyle.fontColor = Color.WHITE;
			textFieldStyle.cursor = new TextureRegionDrawable(new TextureRegion(new Texture("skin/default/system.png"), 0, 8, 8, 8));
			textFieldStyle.selection = new TextureRegionDrawable(new TextureRegion(new Texture("skin/default/system.png"), 0, 8, 2, 8));
			textFieldStyle.messageFont = searchfont;
			textFieldStyle.messageFontColor = Color.GRAY;

			search = new TextField("", textFieldStyle);
			search.setMessageText("search song");
			search.setTextFieldListener(new TextFieldListener() {

				public void keyTyped(TextField textField, char key) {
					if (key == '\n' || key == 13) {
						boolean searched = false;
						if (textField.getText().length() > 0) {
						    if ("/deletescore".equals(textField.getText())) {
						        if (selector.getSelectedBar() instanceof SongBar) {
						            final SongBar songBar = SongBar.class.cast(selector.getSelectedBar());
						            final SongData songData = songBar.getSongData();
						            final Config config = selector.main.getConfig();
						            final PlayerConfig playerConfig = selector.main.getPlayerResource().getPlayerConfig();
						            
						            try {
						                final ScoreDatabaseAccessor scoredb = new ScoreDatabaseAccessor(config.getPlayerpath() + File.separatorChar + config.getPlayername() + File.separatorChar + "score.db");
						                
						                selector.getScoreDataCache().remove(songData, songData.hasAnyLongNote() ? playerConfig.getLnmode() : 0);
						                scoredb.deleteScoreData(songData.getSha256(), songData.hasAnyLongNote() ? playerConfig.getLnmode() : 0);

						                selector.main.getMessageRenderer().addMessage("Score deleted", 3000, Color.GREEN, 1);
						                
						                textField.setText("");
	                                    textField.setMessageText("score deleted");
	                                    textFieldStyle.messageFontColor = Color.DARK_GRAY;

	                                    selector.getBarRender().updateBar(songBar);
						            } catch (final Exception e) {
						                e.printStackTrace();
						                Logger.getGlobal().warning("スコア削除失敗。" + e.getLocalizedMessage());
						            }
						        }
						    } else {
    							SearchWordBar swb = new SearchWordBar(selector, textField.getText());
    							int count = swb.getChildren().length;
    							if (count > 0) {
    								selector.getBarRender().addSearch(swb);
    								selector.getBarRender().updateBar(null);
    								selector.getBarRender().setSelected(swb);
    								textField.setText("");
    								textField.setMessageText(count + " song(s) found");
    								textFieldStyle.messageFontColor = Color.valueOf("00c0c0");
    								searched = true;
    							} else {
    								textField.setText("");
    								textField.setMessageText("no song found");
    								textFieldStyle.messageFontColor = Color.DARK_GRAY;
    							}
						    }
						}
						if (!searched) {
							// Enter入力がTextFieldとInputProcessorで2回発生するので、後者のEnter入力を一時的にロックする
							selector.main.getInputProcessor().lockEnterPress();
						}
						textField.getOnscreenKeyboard().show(false);
						setKeyboardFocus(null);
					}
					if (!searchfont.getData().hasGlyph(key)) {
						FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
						parameter.size = (int) r.height;
						parameter.characters += textField.getText() + key;
						BitmapFont newsearchfont = generator.generateFont(parameter);
						textFieldStyle.font = newsearchfont;
						textFieldStyle.messageFont = newsearchfont;
						searchfont.dispose();
						searchfont = newsearchfont;
						textField.appendText(String.valueOf(key));
					}

				}

			});
			search.setBounds(r.x, r.y, r.width, r.height);
			search.setMaxLength(50);
			search.setFocusTraversal(false);

			search.setVisible(true);
			search.addListener(new EventListener() {
				@Override
				public boolean handle(Event e) {
					if (e.isHandled()) {
						selector.main.getInputProcessor().getKeyBoardInputProcesseor()
								.setEnable(getKeyboardFocus() == null);
					}
					return false;
				}
			});			

			screen = new Group();
			screen.setBounds(0, 0, resolution.width, resolution.height);
			screen.addListener(new ClickListener() {
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					if (getKeyboardFocus() != null && !r.contains(x, y)) {
						unfocus(selector);
					}
					return false;
				}
			});
			screen.addActor(search);
			addActor(screen);
		} catch (GdxRuntimeException e) {
			Logger.getGlobal().warning("Search Text読み込み失敗");
		}
	}

	public void unfocus(MusicSelector selector) {
		search.setText("");
		search.setMessageText("search song");
		search.getStyle().messageFontColor = Color.GRAY;
		search.getOnscreenKeyboard().show(false);
		setKeyboardFocus(null);
		selector.main.getInputProcessor().getKeyBoardInputProcesseor().setEnable(true);
	}

	public void dispose() {
//		super.dispose();
		if(generator != null) {
			generator.dispose();
			generator = null;
		}
		if(searchfont != null) {
			searchfont.dispose();
			searchfont = null;
		}
	}

	public Rectangle getSearchBounds() {
		return new Rectangle(search.getX(), search.getY(), search.getWidth(), search.getHeight());
	}
}
