package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

public final class MenuOpcoes {

    private static final float MEIA_LARGURA_ACERTO = 3.4f;

    private final String[] rotulos;
    private final float topoY;
    private final float passo;
    private final float tamanho;
    private final Vector2 temporario = new Vector2();

    private int selecionado;

    public MenuOpcoes(float topoY, float passo, float tamanho, String... rotulos) {
        this.topoY = topoY;
        this.passo = passo;
        this.tamanho = tamanho;
        this.rotulos = rotulos;
    }

    public void reset() {
        selecionado = 0;
    }

    public int update(Viewport viewport) {
        int n = rotulos.length;

        if (Gdx.input.isKeyJustPressed(Keys.UP) || Gdx.input.isKeyJustPressed(Keys.W)) {
            selecionado = (selecionado + n - 1) % n;
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN) || Gdx.input.isKeyJustPressed(Keys.S)) {
            selecionado = (selecionado + 1) % n;
        }

        if (Gdx.input.justTouched()) {
            int atingido = verificarToque(viewport);
            if (atingido >= 0) {
                selecionado = atingido;
                return atingido;
            }
        }

        if (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            return selecionado;
        }

        return -1;
    }

    private int verificarToque(Viewport viewport) {
        viewport.unproject(temporario.set(Gdx.input.getX(), Gdx.input.getY()));

        if (Math.abs(temporario.x - Tema.LARGURA_MUNDO / 2f) > MEIA_LARGURA_ACERTO) {
            return -1;
        }

        for (int i = 0; i < rotulos.length; i++) {
            float topo = topoY - i * passo;
            if (temporario.y <= topo + tamanho * 0.3f && temporario.y >= topo - tamanho * 1.3f) {
                return i;
            }
        }
        return -1;
    }

    public void draw(Main jogo, float tempo) {
        for (int i = 0; i < rotulos.length; i++) {
            boolean selecionadoAqui = i == selecionado;

            String texto;
            if (selecionadoAqui) {
                texto = "> " + rotulos[i] + " <";
            } else {
                texto = rotulos[i];
            }

            Color cor;
            float alpha;
            if (selecionadoAqui) {
                cor = Tema.CYAN;
                alpha = 0.8f + 0.2f * MathUtils.sin(tempo * 6f);
            } else {
                cor = Tema.WHITE;
                alpha = 0.65f;
            }

            jogo.drawText(texto, tamanho, cor, alpha, 0f,
                topoY - i * passo, Tema.LARGURA_MUNDO, Align.center);
        }
    }
}
