package br.mackenzie;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Main extends Game {

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private float fontBaseLineHeight;
    private Assets assets;
    private Preferences prefs;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        assets = new Assets();
        prefs = Gdx.app.getPreferences("headdash");

        font = new BitmapFont();
        font.setUseIntegerPositions(false);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fontBaseLineHeight = font.getLineHeight();

        setScreen(new MenuScreen(this));
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public ShapeRenderer getShapes() {
        return shapes;
    }

    public Assets getAssets() {
        return assets;
    }

    public int getBestScore() {
        return prefs.getInteger("best", 0);
    }

    public void setBestScore(int score) {
        prefs.putInteger("best", score);
        prefs.flush();
    }

    /** Texto com altura de linha em unidades de mundo; y = topo do texto. Usar entre batch.begin()/end(). */
    public void drawText(String text, float size, Color color, float alpha,
                         float x, float y, float width, int align) {
        font.getData().setScale(size / fontBaseLineHeight);
        font.setColor(color.r, color.g, color.b, alpha);
        font.draw(batch, text, x, y, width, align, false);
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        shapes.dispose();
        font.dispose();
        assets.dispose();
    }
}