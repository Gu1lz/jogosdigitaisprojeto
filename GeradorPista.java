package br.mackenzie;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import java.util.Random;

public final class GeradorPista {

    private static final float PRIMEIRA_FILEIRA = 14f;
    private static final float GERAR_ADIANTE   = 20f;
    private static final float RAIO_ACERTO     = 0.34f;
    private static final float VELOCIDADE_LATERAL = 10f;

    public static final int PERFIL_PADRAO    = 0;
    public static final int PERFIL_CHEFE     = 1;
    public static final int PERFIL_TRALALERO = 2;
    public static final int PERFIL_AVANCADO  = 3;
    public static final int PERFIL_FINAL     = 4;

    private final Random aleatorio = new Random();

    private float posicao;
    private float centro;
    private float ultimaPosicao;
    private float alturaAnterior;
    private boolean primeiro;
    private boolean duploProximo;

    private float vaoMinimo;
    private float vaoMaximo;
    private float chanceEspinhoBase;
    private int perfil;

    public GeradorPista() {
        definirPerfil(PERFIL_PADRAO);
        reiniciar(PRIMEIRA_FILEIRA);
    }

    public void definirPerfil(int p) {
        perfil = p;
        if (p == PERFIL_CHEFE) {
            vaoMinimo = 2.8f;
            vaoMaximo = 3.5f;
            chanceEspinhoBase = 0.1f;
        } else if (p == PERFIL_TRALALERO) {
            vaoMinimo = 2.8f;
            vaoMaximo = 3.5f;
            chanceEspinhoBase = 1.0f;
        } else {
            vaoMinimo = 2.8f;
            vaoMaximo = 4.4f;
            chanceEspinhoBase = 0f;
        }
    }

    public void reiniciar(float posicaoInicial) {
        posicao = posicaoInicial;
        centro = Tema.LARGURA_MUNDO / 2f;
        ultimaPosicao = 0f;
        alturaAnterior = 0f;
        primeiro = true;
        duploProximo = false;
    }

    public void gerarAdiante(Array<Obstaculo> saida, float rolagem) {
        float limite = rolagem + GERAR_ADIANTE;
        while (posicao < limite) {
            adicionarFileira(saida, posicao);
        }
    }

    public void gerarAte(Array<Obstaculo> saida, float limite) {
        while (posicao < limite) {
            adicionarFileira(saida, posicao);
        }
    }

    public void descartar(Array<Obstaculo> obstaculos, float rolagem) {
        float limite = rolagem - 4f;
        while (obstaculos.size > 0
               && obstaculos.first().posicao + obstaculos.first().altura < limite) {
            obstaculos.removeIndex(0);
        }
    }

    private float velocidadeEm(float d) {
        float v = 5f + d * 0.01f;
        if (v > 10f) v = 10f;
        return v;
    }

    private float vaoEm(float d) {
        if (perfil == PERFIL_CHEFE || perfil == PERFIL_TRALALERO) return MathUtils.random(vaoMinimo, vaoMaximo);
        float v = 4.4f - d * 0.002f;
        if (v < 2.8f) v = 2.8f;
        return v;
    }

    private float espacamentoEm(float d) {
        if (perfil == PERFIL_CHEFE || perfil == PERFIL_TRALALERO) return MathUtils.random(6f, 7f); 
        float v = 12f - d * 0.004f;
        if (v < 8f) v = 8f;
        return v;
    }

    private void adicionarFileira(Array<Obstaculo> saida, float p) {
        float largura = Tema.LARGURA_MUNDO;
        float limite = CenarioCidade.LIMITE;

        float vao = vaoEm(p);

        float alturaFileira = 0.8f;
        int tipo = Obstaculo.TIPO_BLOCO;

        if (perfil == PERFIL_TRALALERO) {
            tipo = Obstaculo.TIPO_ESPINHO;
            alturaFileira = 0.9f;
        } else if (perfil == PERFIL_AVANCADO) {
            float r = aleatorio.nextFloat();
            if (r < 0.2f) {
                tipo = Obstaculo.TIPO_LASER;
                alturaFileira = 3.0f;
            } else if (r < 0.4f) {
                tipo = Obstaculo.TIPO_BARREIRA;
                alturaFileira = 0.6f;
            }
        } else if (perfil == PERFIL_FINAL) {
            float r = aleatorio.nextFloat();
            if (r < 0.3f) {
                tipo = Obstaculo.TIPO_LASER;
                alturaFileira = 3.0f;
            } else if (r < 0.6f) {
                tipo = Obstaculo.TIPO_BARREIRA;
                alturaFileira = 0.6f;
            } else if (r < 0.8f) {
                tipo = Obstaculo.TIPO_ESPINHO;
                alturaFileira = 0.9f;
            }
        } else if (perfil == PERFIL_PADRAO && aleatorio.nextFloat() < 0.1f) {
            tipo = Obstaculo.TIPO_ESPINHO;
            alturaFileira = 0.9f;
        }

        boolean espinho = (tipo == Obstaculo.TIPO_ESPINHO);
        boolean daChefe = (perfil == PERFIL_CHEFE || perfil == PERFIL_TRALALERO);
        boolean doTralalero = (perfil == PERFIL_TRALALERO);

        float minimo = limite + vao / 2f + 0.05f;
        float maximo = largura - limite - vao / 2f - 0.05f;

        if (primeiro) {
            centro = largura / 2f + (aleatorio.nextFloat() - 0.5f) * 2f;
        } else {
            float tempoLivre = Math.max(0.1f, (p - ultimaPosicao - alturaAnterior) / velocidadeEm(p));
            float deslocamentoMax = VELOCIDADE_LATERAL * tempoLivre * 0.8f - (vao - 2f * RAIO_ACERTO);
            if (deslocamentoMax < 0.4f) deslocamentoMax = 0.4f;
            centro += (aleatorio.nextFloat() * 2f - 1f) * deslocamentoMax;
        }
        centro = MathUtils.clamp(centro, minimo, maximo);

        float vaoEsquerda = centro - vao / 2f;
        float vaoDireita = centro + vao / 2f;

        if (vaoEsquerda - limite > 0.05f) {
            Obstaculo obs = new Obstaculo(limite, p, vaoEsquerda - limite, alturaFileira, espinho, daChefe, doTralalero, tipo);
            if (tipo == Obstaculo.TIPO_BARREIRA) {
                obs.velocidadeX = aleatorio.nextBoolean() ? 1.5f : -1.5f;
            }
            saida.add(obs);
        }
        if ((largura - limite) - vaoDireita > 0.05f) {
            Obstaculo obs = new Obstaculo(vaoDireita, p, (largura - limite) - vaoDireita, alturaFileira, espinho, daChefe, doTralalero, tipo);
            if (tipo == Obstaculo.TIPO_BARREIRA) {
                obs.velocidadeX = aleatorio.nextBoolean() ? 1.5f : -1.5f;
            }
            saida.add(obs);
        }

        ultimaPosicao = p;
        alturaAnterior = alturaFileira;
        primeiro = false;

        posicao = p + espacamentoEm(p) + aleatorio.nextFloat() * 1.2f;
    }
}
