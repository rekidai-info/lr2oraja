package bms.player.beatoraja.skin;

import bms.player.beatoraja.MainController;
import bms.player.beatoraja.MainState;
import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.skin.Skin.SkinObjectRenderer;
import bms.player.beatoraja.skin.property.IntegerProperty;
import bms.player.beatoraja.skin.property.IntegerPropertyFactory;
import bms.player.beatoraja.select.BarSorter;

import bms.player.beatoraja.skin.property.TimerProperty;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Array;

/**
 * スキンイメージ
 * 
 * @author exch
 */
public class SkinImage extends SkinObject {
	
	/**
	 * イメージ
	 */
	private SkinSource[] image;

	private IntegerProperty ref;

	private TextureRegion currentImage;
	
	private BitmapFont bitmapFont; // JUDGE/PLAY COUNT/PLAY DATE でのソートに対応していないスキンへの対応として独自に表示するためのスキン（何も表示されないよりはマシになる）
	
	public MainController main;
	
	private Array<SkinSource> removedSources = new Array<SkinSource>();
	
	public SkinImage() {
		
	}

	public SkinImage(int imageid) {
		this.image = new SkinSource[1];
		this.image[0] = new SkinSourceReference(imageid);
	}

	public SkinImage(TextureRegion image) {
		setImage(new TextureRegion[]{image}, 0, 0);
	}

	public SkinImage(TextureRegion[] image, int timer, int cycle) {
		setImage(image, timer, cycle);
	}

	public SkinImage(TextureRegion[][] image, int timer, int cycle) {
		setImage(image, timer, cycle);
	}

	public SkinImage(TextureRegion[] image, TimerProperty timer, int cycle) {
		setImage(image, timer, cycle);
	}

	public SkinImage(TextureRegion[][] image, TimerProperty timer, int cycle) {
		setImage(image, timer, cycle);
	}

	public SkinImage(SkinSourceMovie image) {
		this.image = new SkinSource[1];
		this.image[0] = image;
		this.setImageType(SkinObjectRenderer.TYPE_FFMPEG);
	}

	public SkinImage(SkinSourceImage[] image) {
		this.image = image;
	}

	public TextureRegion getImage(long time, MainState state) {
		return getImage(0 ,time, state);
	}

	public TextureRegion getImage(int value, long time, MainState state) {
		final SkinSource source = image[value];
		return source != null ? source.getImage(time, state) : null;
	}

	public void setImage(TextureRegion[] image, int timer, int cycle) {
		this.image = new SkinSource[1];
		this.image[0] = new SkinSourceImage(image, timer, cycle);
	}

	public void setImage(TextureRegion[][] image, int timer, int cycle) {
		this.image = new SkinSource[image.length];
		for(int i = 0;i < image.length;i++) {
			this.image[i] = new SkinSourceImage(image[i], timer, cycle);
		}		
	}

	public void setImage(TextureRegion[] image, TimerProperty timer, int cycle) {
		this.image = new SkinSource[1];
		this.image[0] = new SkinSourceImage(image, timer, cycle);
	}

	public void setImage(TextureRegion[][] image, TimerProperty timer, int cycle) {
		this.image = new SkinSource[image.length];
		for(int i = 0;i < image.length;i++) {
			this.image[i] = new SkinSourceImage(image[i], timer, cycle);
		}
	}
	
	public boolean validate() {
		if(image == null) {
			return false;
		}
		
		boolean exist = false;
    	for(int i = 0;i < image.length;i++) {
    		if(image[i] != null) {
    			if(image[i].validate()) {
    				exist = true;
    			} else {
        			removedSources.add(image[i]);
        			image[i] = null;
    			}
    		}
    	}
    	
    	if(!exist) {
    		return false;
    	}

		return super.validate();
	}

	public void prepare(long time, MainState state) {
        prepare(time, state, 0, 0);
	}
	
	public void prepare(long time, MainState state, float offsetX, float offsetY) {		
        prepare(time, state, ref != null ? ref.get(state) : 0, offsetX, offsetY);
	}
	
	public void prepare(long time, MainState state, int value, float offsetX, float offsetY) {		
	    main = state.main;
        if(image == null || value < 0) {
            draw = false;
            return;
        }
		super.prepare(time, state, offsetX, offsetY);
        if(value >= image.length) {
            if (bitmapFont == null && state instanceof MusicSelector) {
                final int sort = ((MusicSelector) state).getSort();

                if (sort == BarSorter.JUDGE_SORTER.ordinal() || sort == BarSorter.PLAY_COUNT_SORTER.ordinal() || sort == BarSorter.PLAY_DATE_SORTER.ordinal()) {
                    final FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                            Gdx.files.internal("skin/default/VL-Gothic-Regular.ttf"));
                    final FreeTypeFontParameter parameter = new FreeTypeFontParameter();
                    parameter.size = (int) (30 * state.getSkin().getScaleY());
                    parameter.borderWidth = (int) (3 * state.getSkin().getScaleY());
                    parameter.borderColor = Color.valueOf("FFFFFF");
                    bitmapFont = generator.generateFont(parameter);
                    generator.dispose();
                }
            }
            value = 0;
        }
        currentImage = getImage(value, time, state);
        if(currentImage == null) {
            draw = false;
            return;        	
        }
	}

	public void draw(SkinObjectRenderer sprite) {
	    if (main != null && bitmapFont != null && main.getCurrentState() instanceof MusicSelector && (((MusicSelector) main.getCurrentState()).getSort() == BarSorter.JUDGE_SORTER.ordinal() || ((MusicSelector) main.getCurrentState()).getSort() == BarSorter.PLAY_COUNT_SORTER.ordinal() || ((MusicSelector) main.getCurrentState()).getSort() == BarSorter.PLAY_DATE_SORTER.ordinal())) {
	        if (((MusicSelector) main.getCurrentState()).getSort() == BarSorter.JUDGE_SORTER.ordinal()) {
	            sprite.draw(bitmapFont, BarSorter.JUDGE_SORTER.name, region.x, region.y + region.height / 2, Color.valueOf("FFFFFF"));
	        } else if (((MusicSelector) main.getCurrentState()).getSort() == BarSorter.PLAY_COUNT_SORTER.ordinal()) {
                sprite.draw(bitmapFont, BarSorter.PLAY_COUNT_SORTER.name, region.x, region.y + region.height / 2, Color.valueOf("FFFFFF"));
            } else {
                sprite.draw(bitmapFont, BarSorter.PLAY_DATE_SORTER.name, region.x, region.y + region.height / 2, Color.valueOf("FFFFFF"));
            }
	    } else if(image[0] instanceof SkinSourceMovie) {
    		setImageType(3);
            draw(sprite, currentImage, region.x, region.y, region.width, region.height);
    		setImageType(0);
    	} else {
            draw(sprite, currentImage, region.x, region.y, region.width, region.height);
    	}                    				
	}

	public void draw(SkinObjectRenderer sprite, float offsetX, float offsetY) {
	    if (main != null && bitmapFont != null && main.getCurrentState() instanceof MusicSelector && (((MusicSelector) main.getCurrentState()).getSort() == BarSorter.JUDGE_SORTER.ordinal() || ((MusicSelector) main.getCurrentState()).getSort() == BarSorter.PLAY_COUNT_SORTER.ordinal() || ((MusicSelector) main.getCurrentState()).getSort() == BarSorter.PLAY_DATE_SORTER.ordinal())) {
            if (((MusicSelector) main.getCurrentState()).getSort() == BarSorter.JUDGE_SORTER.ordinal()) {
                sprite.draw(bitmapFont, BarSorter.JUDGE_SORTER.name, region.x, region.y + region.height / 2, Color.valueOf("FFFFFF"));
            } else if (((MusicSelector) main.getCurrentState()).getSort() == BarSorter.PLAY_COUNT_SORTER.ordinal()) {
                sprite.draw(bitmapFont, BarSorter.PLAY_COUNT_SORTER.name, region.x, region.y + region.height / 2, Color.valueOf("FFFFFF"));
            } else {
                sprite.draw(bitmapFont, BarSorter.PLAY_DATE_SORTER.name, region.x, region.y + region.height / 2, Color.valueOf("FFFFFF"));
            }
        } else if(image[0] instanceof SkinSourceMovie) {
			setImageType(3);
			draw(sprite, currentImage, region.x + offsetX, region.y + offsetY, region.width, region.height);
			setImageType(0);
		} else {
			draw(sprite, currentImage, region.x + offsetX, region.y + offsetY, region.width, region.height);
		}
	}

	public void draw(SkinObjectRenderer sprite, long time, MainState state, float offsetX, float offsetY) {
		prepare(time, state, offsetX, offsetY);
		if(draw) {
			draw(sprite);
		}
	}

    public void draw(SkinObjectRenderer sprite, long time, MainState state, int value, float offsetX, float offsetY) {
		prepare(time, state, value, offsetX, offsetY);
		if(draw) {
			draw(sprite);
		}
    }

    public void dispose() {
    	disposeAll(removedSources.toArray(SkinSource.class));
		if(image != null) {
			for(SkinSource tr : image) {
				if(tr != null) {
					tr.dispose();					
				}
			}
			image = null;
		}
		if (bitmapFont != null) {
		    bitmapFont.dispose();
		    bitmapFont = null;
		}
		main = null;
	}

	public void setReference(IntegerProperty property) {
		ref = property;
	}

	public void setReferenceID(int id) {
		ref = IntegerPropertyFactory.getImageIndexProperty(id);
	}
}