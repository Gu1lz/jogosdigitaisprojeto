package br.mackenzie;

import com.badlogic.gdx.Game;
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

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();

        font = new BitmapFont();
        font.setUseIntegerPositions(false);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        fontBaseLineHeight = font.getLineHeight(); // altura da linha com escala 1

        setScreen(new MenuScreen(this));
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public ShapeRenderer getShapes() {
        return shapes;
    }

    /**
     * Desenha texto com tamanho dado em unidades do mundo (altura da linha).
     * Deve ser chamado entre batch.begin() e batch.end().
     *
     * @param align constante de com.badlogic.gdx.utils.Align (Align.center, Align.left, ...)
     * @param y     posição do TOPO do texto
     */
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
    }
}