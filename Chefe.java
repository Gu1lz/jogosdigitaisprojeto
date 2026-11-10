package br.mackenzie;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.util.Random;

public final class Chefe {

    public static final int ST_ESCONDIDO = 0;
    public static final int ST_NORMAL    = 1;
    public static final int ST_ATAQUE    = 2;
    public static final int ST_GRITO     = 3;
    public static final int ST_ATINGIDO  = 4;
    public static final int ST_MORTE     = 5;

    private static final float TEMPO_ATINGIDO = 0.45f;
    private static final float TEMPO_ATAQUE   = 1.2f;
    private static final float TEMPO_GRITO    = 1.0f;
    private static final float TEMPO_MORTE    = 2.0f;

    private static final float Y_CHEFE    = Tema.ALTURA_MUNDO - 4.0f;
    private static final float TAM_CHEFE  = 5.5f;

    // Novas aparições:
    // Tung Tung: aparece em 3067, morre em 6767
    // Tralalero: aparece em 106767, morre em 107467
    private static final int[] APARECE_EM   = { 3067, 106767 };
    private static final int[] PONTOS_MORTE = { 6767, 107467 };
    private static final int CICLO          = 250;
    private static final int DESLOC_ACERTO  = 100;

    private final Random aleatorio = new Random();
    private final GeradorPista gerador = new GeradorPista();

    private int estado;
    private float tempoEstado;
    private float tempo;
    private int indiceChefeAtual = -1;
    private int ultimoAcerto;
    private int proximoAleatorio;
    private boolean ativo;
    private boolean morteDisparada;
    private boolean assumiuControle;
    private boolean liberouEspaco;
    private float cooldownAposGrito;

    public Chefe() {
        gerador.definirPerfil(GeradorPista.PERFIL_CHEFE);
        reiniciar();
    }

    public void reiniciar() {
        estado = ST_ESCONDIDO;
        tempoEstado = 0f;
        tempo = 0f;
        indiceChefeAtual = -1;
        ultimoAcerto = 0;
        proximoAleatorio = 0;
        ativo = false;
        morteDisparada = false;
        assumiuControle = false;
        liberouEspaco = false;
        cooldownAposGrito = 0f;
    }

    public void mudarPerfilGerador(int perfil) {
        gerador.definirPerfil(perfil);
    }

    public boolean assumiuControle() {
        if (assumiuControle) {
            assumiuControle = false;
            return true;
        }
        return false;
    }

    public boolean liberouEspaco() {
        if (liberouEspaco) {
            liberouEspaco = false;
            return true;
        }
        return false;
    }

    public void update(float delta, int pontos, float rolagem) {
        tempo += delta;
        tempoEstado += delta;

        if (cooldownAposGrito > 0f) {
            cooldownAposGrito -= delta;
        }

        int alvo = indiceChefeParaPontos(pontos);

        if (alvo < 0) {
            if (estado != ST_ESCONDIDO) estado = ST_ESCONDIDO;
            ativo = false;
            return;
        }

        if (indiceChefeAtual != alvo) {
            indiceChefeAtual = alvo;
            ultimoAcerto = 0;
            morteDisparada = false;
            ativo = true;
            assumiuControle = true;
            gerador.reiniciar(rolagem + 25f);
            definirEstado(ST_NORMAL);
            proximoAleatorio = pontos + aleatorio.nextInt(60 - 30) + 30;
        }

        if (!morteDisparada && pontos >= PONTOS_MORTE[indiceChefeAtual]) {
            morteDisparada = true;
            ativo = false;
            liberouEspaco = true;
            definirEstado(ST_MORTE);
            return;
        }

        if (estado == ST_MORTE) {
            if (tempoEstado >= TEMPO_MORTE) {
                estado = ST_ESCONDIDO;
            }
            return;
        }

        if (ativo && ehPontuacaoDeAcerto(pontos) && pontos > ultimoAcerto) {
            if (cooldownAposGrito <= 0f) {
                ultimoAcerto = pontos;
                definirEstado(ST_ATINGIDO);
            }
        }

        if (ativo && estado == ST_NORMAL && pontos >= proximoAleatorio) {
            if (aleatorio.nextFloat() < 0.8f) {
                definirEstado(ST_ATAQUE);
            } else {
                definirEstado(ST_GRITO);
                cooldownAposGrito = 2.5f;
            }
            proximoAleatorio = pontos + aleatorio.nextInt(60 - 30) + 30;
        }

        if (estado == ST_ATAQUE) {
            if (tempoEstado >= TEMPO_ATAQUE) definirEstado(ST_NORMAL);
        } else if (estado == ST_ATINGIDO) {
            if (tempoEstado >= TEMPO_ATINGIDO) definirEstado(ST_NORMAL);
        } else if (estado == ST_GRITO) {
            if (tempoEstado >= TEMPO_GRITO) definirEstado(ST_NORMAL);
        }
    }

    private int indiceChefeParaPontos(int pontos) {
        int indice = -1;
        for (int i = 0; i < APARECE_EM.length; i++) {
            if (pontos >= APARECE_EM[i]) indice = i;
        }
        return indice;
    }

    private boolean ehPontuacaoDeAcerto(int pontos) {
        int base = APARECE_EM[indiceChefeAtual];
        if (pontos < base) return false;
        return (pontos - base) % CICLO == DESLOC_ACERTO;
    }

    private void definirEstado(int e) {
        estado = e;
        tempoEstado = 0f;
    }

    public void draw(SpriteBatch batch, Assets assets, float larguraMundo) {
        if (estado == ST_ESCONDIDO) return;
        if (indiceChefeAtual < 0) return;
        if (indiceChefeAtual >= assets.chefes.size) return;

        Assets.SpritesChefe sp = assets.chefes.get(indiceChefeAtual);
        TextureRegion quadro = regiaoPara(sp);
        if (quadro == null) return;

        float cx = larguraMundo / 2f;

        batch.setColor(1f, 0f, 0f, 0.15f);
        batch.draw(quadro, cx - TAM_CHEFE * 0.75f, Y_CHEFE - TAM_CHEFE * 0.75f, TAM_CHEFE * 1.5f, TAM_CHEFE * 1.5f);

        batch.setColor(Color.WHITE);
        batch.draw(quadro, cx - TAM_CHEFE / 2f, Y_CHEFE - TAM_CHEFE / 2f, TAM_CHEFE, TAM_CHEFE);
    }

    private TextureRegion regiaoPara(Assets.SpritesChefe sp) {
        if (estado == ST_NORMAL)   return sp.normal;
        if (estado == ST_ATAQUE)   return sp.attack;
        if (estado == ST_GRITO)    return sp.scream;
        if (estado == ST_ATINGIDO) return sp.hit;
        if (estado == ST_MORTE)    return sp.death;
        return null;
    }

    public boolean controlaGeracao() {
        if (!ativo) return false;
        if (estado == ST_ESCONDIDO) return false;
        if (estado == ST_MORTE) return false;
        return true;
    }

    public int estado() {
        return estado;
    }

    public int indiceChefeAtual() {
        return indiceChefeAtual;
    }

    public String nomeChefeAtual(Assets assets) {
        if (indiceChefeAtual < 0) return "";
        if (indiceChefeAtual >= assets.chefes.size) return "";
        return assets.chefes.get(indiceChefeAtual).name;
    }

    public float bonusVelocidade() {
        if (ativo) return 2.0f;
        return 0f;
    }

    public void gerarAte(float limite, Array<Obstaculo> saida) {
        gerador.gerarAte(saida, limite);
    }
}
