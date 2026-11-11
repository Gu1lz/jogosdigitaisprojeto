package br.mackenzie;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public final class SistemaParticulas {

    private static final int MAXIMO = 220;

    private final Particula[] conjunto = new Particula[MAXIMO];
    private int cabeca;

    public SistemaParticulas() {
        for (int i = 0; i < MAXIMO; i++) {
            conjunto[i] = new Particula();
        }
    }

    public void limpar() {
        for (Particula p : conjunto) {
            p.vida = 0f;
        }
    }

    public void emitir(float x, float y, float vx, float vy,
                       float vida, float tamanho, Color cor) {
        Particula p = conjunto[cabeca];
        cabeca = (cabeca + 1) % MAXIMO;
        p.x = x;
        p.y = y;
        p.vx = vx;
        p.vy = vy;
        p.vida = vida;
        p.vidaMax = vida;
        p.tamanho = tamanho;
        p.r = cor.r;
        p.g = cor.g;
        p.b = cor.b;
    }

    public void rastro(float delta, float x, float y, float velocidadeRolagem,
                       float[] cronometro, Color corA, Color corB) {
        cronometro[0] -= delta;
        while (cronometro[0] <= 0f) {
            cronometro[0] += 0.035f;

            Color cor;
            if (MathUtils.randomBoolean()) {
                cor = corA;
            } else {
                cor = corB;
            }

            emitir(x + MathUtils.random(-0.12f, 0.12f), y - 0.4f,
                   MathUtils.random(-0.3f, 0.3f),
                   -velocidadeRolagem * 0.5f - MathUtils.random(0f, 1.5f),
                   0.45f, 0.09f, cor);
        }
    }

    public void explosao(float x, float y, Color c1, Color c2, Color c3) {
        for (int i = 0; i < 40; i++) {
            float a = MathUtils.random(360f);
            float vel = MathUtils.random(2f, 7f);

            Color cor;
            if (i % 3 == 0) {
                cor = c1;
            } else if (i % 3 == 1) {
                cor = c2;
            } else {
                cor = c3;
            }

            emitir(x, y,
                   MathUtils.cosDeg(a) * vel,
                   MathUtils.sinDeg(a) * vel,
                   MathUtils.random(0.6f, 1.2f),
                   MathUtils.random(0.07f, 0.17f),
                   cor);
        }
    }

    public void atualizar(float delta) {
        for (Particula p : conjunto) {
            if (p.vida <= 0f) {
                continue;
            }
            p.vida -= delta;
            p.x += p.vx * delta;
            p.y += p.vy * delta;
        }
    }

    public void desenhar(ShapeRenderer s) {
        for (Particula p : conjunto) {
            if (p.vida <= 0f) {
                continue;
            }
            float a = p.vida / p.vidaMax;
            s.setColor(p.r, p.g, p.b, a);
            s.circle(p.x, p.y, p.tamanho * (0.5f + 0.5f * a), 8);
        }
    }

    private static final class Particula {
        float x;
        float y;
        float vx;
        float vy;
        float vida;
        float vidaMax;
        float tamanho;
        float r;
        float g;
        float b;
    }
}
