package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Lista vertical de opcoes navegavel por teclado (SETAS / W S + ENTER / ESPACO)
 * e por toque/clique direto na opcao.
 *
 * Posicoes em unidades de mundo; topY = topo do texto da primeira opcao.
 */
public final class OptionMenu {

    /** Meia-largura da area de toque, centrada na tela. */
    private static final float HIT_HALF_W = 3.4f;

    private final String[] labels;
    private final float topY;
    private final float step;
    private final float size;
    private final Vector2 tmp = new Vector2();

    private int selected;

    public OptionMenu(float topY, float step, float size, String... labels) {
        this.topY = topY;
        this.step = step;
        this.size = size;
        this.labels = labels;
    }

    public void reset() {
        selected = 0;
    }

    /**
     * Processa a entrada.
     *
     * @return indice da opcao confirmada neste frame, ou -1 se nenhuma.
     */
    public int update(Viewport viewport) {
        int n = labels.length;

        if (Gdx.input.isKeyJustPressed(Keys.UP) || Gdx.input.isKeyJustPressed(Keys.W)) {
            selected = (selected + n - 1) % n;
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN) || Gdx.input.isKeyJustPressed(Keys.S)) {
            selected = (selected + 1) % n;
        }

        if (Gdx.input.justTouched()) {
            int hit = hitTest(viewport);
            if (hit >= 0) {
                selected = hit;
                return hit;
            }
        }

        if (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            return selected;
        }
        return -1;
    }

    private int hitTest(Viewport viewport) {
        viewport.unproject(tmp.set(Gdx.input.getX(), Gdx.input.getY()));
        if (Math.abs(tmp.x - Theme.WORLD_W / 2f) > HIT_HALF_W) return -1;

        for (int i = 0; i < labels.length; i++) {
            float top = topY - i * step;
            if (tmp.y <= top + size * 0.3f && tmp.y >= top - size * 1.3f) return i;
        }
        return -1;
    }

    /** Usar entre batch.begin() / batch.end(). */
    public void draw(Main game, float time) {
        for (int i = 0; i < labels.length; i++) {
            boolean sel = i == selected;
            String text = sel ? "> " + labels[i] + " <" : labels[i];
            Color color = sel ? Theme.CYAN : Theme.WHITE;
            float alpha = sel ? 0.8f + 0.2f * MathUtils.sin(time * 6f) : 0.65f;
            game.drawText(text, size, color, alpha, 0f, topY - i * step, Theme.WORLD_W, Align.center);
        }
    }
}