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
        prefs = Gdx.app.getPreferences(Tema.NOME_PREFS);

        font = new BitmapFont();
        font.setUseIntegerPositions(false);
        font.getRegion().getTexture().setFilter(
            Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fontBaseLineHeight = font.getLineHeight();

        setScreen(new TelaMenu(this));
    }

    public SpriteBatch obterLote() {
        return batch;
    }

    public ShapeRenderer obterFormas() {
        return shapes;
    }

    public Assets obterAssets() {
        return assets;
    }

    public int obterMelhorPontuacao() {
        return prefs.getInteger("best", 0);
    }

    public void definirMelhorPontuacao(int pontos) {
        prefs.putInteger("best", pontos);
        prefs.flush();
    }

    public void drawText(String texto, float tamanho, Color cor, float alpha,
                         float x, float y, float largura, int alinhamento) {
        font.getData().setScale(tamanho / fontBaseLineHeight);
        font.setColor(cor.r, cor.g, cor.b, alpha);
        font.draw(batch, texto, x, y, largura, alinhamento, false);
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
