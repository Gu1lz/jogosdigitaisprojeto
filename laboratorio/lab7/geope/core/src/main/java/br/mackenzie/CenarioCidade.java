package br.mackenzie;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.FloatArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CenarioCidade {

    public static final float LIMITE = 0.65f;

    private static final float LARGURA    = Tema.LARGURA_MUNDO;
    private static final float ALTURA     = Tema.ALTURA_MUNDO;
    private static final float BLOCO      = 24f;
    private static final float GRADE      = 2f;
    private static final int   RASTROS    = 60;

    private final List<Predio> predios = new ArrayList<>();
    private final float[] rx = new float[RASTROS];
    private final float[] ry = new float[RASTROS];
    private final float[] rv = new float[RASTROS];

    private float rolagem;

    public CenarioCidade(long semente) {
        Random aleatorio = new Random(semente);

        adicionarPredios(aleatorio, 0.9f, 2.4f, 2.5f, 6f,
            0.12f, Tema.BLOCK_BODY, Tema.PURPLE, 0.45f);

        adicionarPredios(aleatorio, 0.35f, 0.5f, 1.5f, 4f,
            0.35f, Tema.VOID, Tema.CYAN, 0.85f);

        for (int i = 0; i < RASTROS; i++) {
            reposicionarRastro(i, true);
        }
    }

    public void avancar(float delta, float velocidade) {
        rolagem += velocidade * delta;

        for (int i = 0; i < RASTROS; i++) {
            ry[i] -= (rv[i] + velocidade * 1.5f) * delta;
            if (ry[i] < -1f) {
                reposicionarRastro(i, false);
            }
        }
    }

    public void reiniciar() {
        rolagem = 0f;
        for (int i = 0; i < RASTROS; i++) {
            reposicionarRastro(i, true);
        }
    }

    public float obterRolagem() {
        return rolagem;
    }

    public void desenhar(ShapeRenderer s) {
        s.rect(0f, 0f, LARGURA, ALTURA,
            Tema.SKY_BOTTOM, Tema.SKY_BOTTOM,
            Tema.SKY_TOP, Tema.SKY_TOP);

        tingir(s, Tema.PURPLE, 0.2f);

        float faixa = (LARGURA - 2f * LIMITE) / 6f;

        for (int k = 0; k <= 6; k++) {
            s.rect(LIMITE + k * faixa - 0.01f, 0f, 0.02f, ALTURA);
        }

        for (float y = -(rolagem % GRADE); y < ALTURA; y += GRADE) {
            s.rect(0f, y, LARGURA, 0.02f);
        }

        for (Predio p : predios) {
            desenharPredio(s, p);
        }

        tingir(s, Tema.CYAN, 0.18f);
        s.rect(LIMITE - 0.12f, 0f, 0.24f, ALTURA);
        s.rect(LARGURA - LIMITE - 0.12f, 0f, 0.24f, ALTURA);

        s.setColor(Tema.CYAN);
        s.rect(LIMITE - 0.03f, 0f, 0.06f, ALTURA);
        s.rect(LARGURA - LIMITE - 0.03f, 0f, 0.06f, ALTURA);
    }

    public void desenharChuva(ShapeRenderer s) {
        tingir(s, Tema.CYAN, 0.25f);
        for (int i = 0; i < RASTROS; i++) {
            s.rect(rx[i], ry[i], 0.02f, 0.6f);
        }
    }

    private void desenharPredio(ShapeRenderer s, Predio p) {
        float desloc = (rolagem * p.paralaxe) % BLOCO;

        for (int copia = 0; copia < 2; copia++) {
            float y = copia * BLOCO - desloc + p.y;

            if (y > ALTURA) {
                continue;
            }
            if (y + p.h < 0f) {
                continue;
            }

            s.setColor(p.corpo);
            s.rect(p.x, y, p.w, p.h);

            tingir(s, p.borda, 0.7f);

            if (p.esquerdo) {
                s.rect(p.x + p.w - 0.05f, y, 0.05f, p.h);
            } else {
                s.rect(p.x, y, 0.05f, p.h);
            }

            s.setColor(p.corJanela);

            for (int i = 0; i < p.janelas.length; i += 2) {
                s.rect(p.x + p.janelas[i], y + p.janelas[i + 1], 0.1f, 0.16f);
            }
        }
    }

    private void adicionarPredios(Random aleatorio,
                                  float larguraMin, float larguraMax,
                                  float alturaMin, float alturaMax,
                                  float paralaxe,
                                  Color corpo, Color borda, float alphaJanela) {
        for (int lado = 0; lado < 2; lado++) {
            float y = 0f;

            while (y < BLOCO - 0.6f) {
                float w = larguraMin + aleatorio.nextFloat() * (larguraMax - larguraMin);
                float h = alturaMin + aleatorio.nextFloat() * (alturaMax - alturaMin);
                if (h > BLOCO - y) {
                    h = BLOCO - y;
                }

                float x;
                if (lado == 0) {
                    x = 0f;
                } else {
                    x = LARGURA - w;
                }

                boolean esquerdo = lado == 0;

                predios.add(new Predio(aleatorio, x, y, w, h, esquerdo,
                    paralaxe, corpo, borda, alphaJanela));

                y += h + aleatorio.nextFloat() * 0.3f;
            }
        }
    }

    private void reposicionarRastro(int i, boolean qualquerLugar) {
        rx[i] = MathUtils.random(LIMITE, LARGURA - LIMITE);

        if (qualquerLugar) {
            ry[i] = MathUtils.random(0f, ALTURA);
        } else {
            ry[i] = ALTURA + MathUtils.random(0f, 2f);
        }

        rv[i] = MathUtils.random(8f, 16f);
    }

    private static void tingir(ShapeRenderer s, Color cor, float alpha) {
        s.setColor(cor.r, cor.g, cor.b, alpha);
    }

    private static class Predio {

        final float x;
        final float y;
        final float w;
        final float h;
        final float paralaxe;
        final boolean esquerdo;
        final Color corpo;
        final Color borda;
        final Color corJanela;
        final float[] janelas;

        Predio(Random aleatorio,
               float x, float y, float w, float h,
               boolean esquerdo, float paralaxe,
               Color corpo, Color borda, float alphaJanela) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.esquerdo = esquerdo;
            this.paralaxe = paralaxe;
            this.corpo = corpo;
            this.borda = borda;

            Color[] paleta = new Color[] { Tema.CYAN, Tema.YELLOW, Tema.MAGENTA };
            Color base = paleta[aleatorio.nextInt(3)];

            this.corJanela = new Color(base.r, base.g, base.b, alphaJanela);

            FloatArray lista = new FloatArray();

            int colunas = (int) ((w - 0.1f) / 0.22f);
            int linhas  = (int) ((h - 0.3f) / 0.45f);

            for (int c = 0; c < colunas; c++) {
                for (int l = 0; l < linhas; l++) {
                    if (aleatorio.nextFloat() < 0.18f) {
                        lista.add(0.08f + c * 0.22f);
                        lista.add(0.2f + l * 0.45f);
                    }
                }
            }

            this.janelas = lista.toArray();
        }
    }
}
