package br.mackenzie;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public final class DesenhoCabeca {

    public static final float TAMANHO_DESENHO = 1.3f;

    private DesenhoCabeca() {}

    public static void desenharBrilho(ShapeRenderer s, float cx, float cy, float tamanho) {
        float r = tamanho / 2f;

        s.setColor(Tema.CYAN.r, Tema.CYAN.g, Tema.CYAN.b, 0.10f);
        s.circle(cx, cy, r * 1.8f, 40);

        s.setColor(Tema.CYAN.r, Tema.CYAN.g, Tema.CYAN.b, 0.16f);
        s.circle(cx, cy, r * 1.35f, 40);
    }

    public static void desenharSprite(SpriteBatch b, TextureRegion regiao,
                                      float cx, float cy, float tamanho, float inclinacaoGraus) {
        float larguraRegiao = regiao.getRegionWidth();
        float alturaRegiao = regiao.getRegionHeight();

        float base = larguraRegiao;
        if (alturaRegiao > base) {
            base = alturaRegiao;
        }

        float escala = tamanho / base;
        float largura = larguraRegiao * escala;
        float altura = alturaRegiao * escala;

        b.setColor(Color.WHITE);
        b.draw(regiao,
               cx - largura / 2f,
               cy - altura / 2f,
               largura / 2f,
               altura / 2f,
               largura,
               altura,
               1f, 1f,
               inclinacaoGraus);
    }

    public static float flutuacao(float tempo, boolean morto) {
        if (morto) {
            return 0f;
        }
        return MathUtils.sin(tempo * 5f) * 0.05f;
    }

    public static float inclinacao(float velocidadeX, boolean morto) {
        if (morto) {
            return 0f;
        }
        return -velocidadeX * 2.2f;
    }

    public static void desenharJogador(SpriteBatch batch, Assets assets,
                                       float x, float y, float velocidadeX,
                                       float tempo, float tempoEstado, boolean morto) {
        TextureRegion quadro;
        if (morto) {
            quadro = assets.hit.getKeyFrame(tempoEstado);
        } else {
            quadro = assets.neutral.getKeyFrame(tempo);
        }

        float inc = inclinacao(velocidadeX, morto);
        float flut = flutuacao(tempo, morto);

        desenharSprite(batch, quadro, x, y + flut, TAMANHO_DESENHO, inc);
    }
}
