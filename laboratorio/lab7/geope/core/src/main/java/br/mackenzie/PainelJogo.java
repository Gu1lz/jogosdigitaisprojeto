package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

public final class PainelJogo {

    private static final float LARGURA = Tema.LARGURA_MUNDO;
    private static final float ALTURA  = Tema.ALTURA_MUNDO;

    private static final float PAUSA_CX      = LARGURA / 2f;
    private static final float PAUSA_CY      = ALTURA - 0.95f;
    private static final float PAUSA_TAMANHO = 0.8f;
    private static final float PAUSA_ACERTO  = 0.7f;

    private final Main jogo;
    private final Chefe chefe;
    private final Assets assets;

    private final MenuOpcoes menuPausa = new MenuOpcoes(10.2f, 1.1f, 0.5f,
        "CONTINUAR", "REINICIAR", "MENU PRINCIPAL");

    private final MenuOpcoes menuMorte = new MenuOpcoes(7.8f, 0.9f, 0.45f,
        "TENTAR DE NOVO", "MENU PRINCIPAL");

    private int ultimoPonto67 = 0;
    private float tempoTexto67 = 0f;
    private int indiceCor67 = 0;

    public PainelJogo(Main jogo, Chefe chefe, Assets assets) {
        this.jogo = jogo;
        this.chefe = chefe;
        this.assets = assets;
    }

    public void reiniciarMenuPausa() {
        menuPausa.reset();
    }

    public void reiniciarMenuMorte() {
        menuMorte.reset();
        ultimoPonto67 = 0;
        tempoTexto67 = 0f;
        indiceCor67 = 0;
    }

    public static boolean dentroZonaPausa(float x, float y) {
        if (Math.abs(x - PAUSA_CX) > PAUSA_ACERTO) return false;
        if (Math.abs(y - PAUSA_CY) > PAUSA_ACERTO) return false;
        return true;
    }

    public void desenharBotaoPausa(ShapeRenderer s) {
        float t = PAUSA_TAMANHO;

        s.setColor(Tema.WHITE.r, Tema.WHITE.g, Tema.WHITE.b, 0.10f);
        s.rect(PAUSA_CX - t / 2f, PAUSA_CY - t / 2f, t, t);

        s.setColor(Tema.CYAN.r, Tema.CYAN.g, Tema.CYAN.b, 0.8f);
        s.rect(PAUSA_CX - t * 0.22f, PAUSA_CY - t * 0.28f, t * 0.14f, t * 0.56f);
        s.rect(PAUSA_CX + t * 0.08f, PAUSA_CY - t * 0.28f, t * 0.14f, t * 0.56f);
    }

    public void desenharEscurecimentoPausa(ShapeRenderer s) {
        s.setColor(0f, 0f, 0.03f, 0.65f);
        s.rect(0f, 0f, LARGURA, ALTURA);
    }

    public void desenharHud(EstadoJogo gs, float tempo, float tempoIntroducao, float tempoDica, int fase) {
        if (gs.pontos() > 0 && gs.pontos() % 67 == 0 && gs.pontos() != ultimoPonto67) {
            ultimoPonto67 = gs.pontos();
            tempoTexto67 = 0.3f;
            indiceCor67 = (indiceCor67 + 1) % 7;
        }
        if (tempoTexto67 > 0f) {
            tempoTexto67 -= Gdx.graphics.getDeltaTime();
        }

        if (tempoTexto67 > 0f) {
            Color cor67 = obterCorArcoIris(indiceCor67);
            float alpha67 = Math.min(1f, tempoTexto67);
            float y67 = ALTURA * 0.5f + MathUtils.sin(tempo * 10f) * 0.2f;
            jogo.drawText("67", 2.0f, cor67, alpha67, 0f, y67, LARGURA, Align.center);
        }

        boolean superando = gs.recordeInicial() > 0 && gs.pontos() > gs.recordeInicial();
        Color corPontos = superando ? Tema.YELLOW : Tema.WHITE;

        jogo.drawText(String.valueOf(gs.pontos()), 0.75f, corPontos, 1f,
            0f, ALTURA - 0.3f, LARGURA - 0.4f, Align.right);
        jogo.drawText("PONTOS", 0.28f, Tema.WHITE, 0.7f,
            0f, ALTURA - 1.2f, LARGURA - 0.4f, Align.right);

        String textoFase;
        if (fase == 2) {
            textoFase = "FASE 2";
        } else if (fase == 3) {
            textoFase = "FASE 3";
        } else if (fase == 4) {
            textoFase = "FASE 4";
        } else if (fase == 5) {
            textoFase = "FASE 5";
        } else {
            textoFase = "FASE 1";
        }
        
        jogo.drawText(textoFase, 0.35f, Tema.CYAN, 1f,
            0f, ALTURA - 1.7f, LARGURA - 0.4f, Align.right);

        jogo.drawText("RECORDE", 0.28f, Tema.WHITE, 0.7f,
            0.4f, ALTURA - 0.3f, 4f, Align.left);
        jogo.drawText(String.valueOf(gs.melhorParaExibir()), 0.5f, Tema.CYAN, 1f,
            0.4f, ALTURA - 0.7f, 4f, Align.left);

        if ((fase == 2 || fase == 4) && chefe.indiceChefeAtual() >= 0) {
            String nome = chefe.nomeChefeAtual(assets);
            if (nome.length() > 0) {
                centralizar(nome, 0.4f, Tema.RED, 1f, ALTURA - 2.5f);
            }
        }

        if (gs.estaExecutando()) {
            desenharExecutando(gs, tempoIntroducao, tempoDica);
        } else if (gs.estaPausado()) {
            desenharPausado(gs, tempo);
        } else {
            desenharMorto(gs, tempo);
        }
    }

    private Color obterCorArcoIris(int indice) {
        switch (indice) {
            case 0: return Color.RED;
            case 1: return Color.ORANGE;
            case 2: return Color.YELLOW;
            case 3: return Color.GREEN;
            case 4: return Color.CYAN;
            case 5: return Color.BLUE;
            case 6: return Color.PURPLE;
            default: return Color.WHITE;
        }
    }

    public void desenharControleArduino(String status, boolean aguardandoSensor) {
        centralizar(status, 0.27f, Tema.CYAN, 0.9f, 0.55f);
        if (aguardandoSensor) {
            centralizar("AGUARDANDO SENSOR", 0.55f, Tema.YELLOW, 1f, 8.8f);
            centralizar("A partida continua ao receber uma leitura", 0.27f,
                Tema.WHITE, 0.9f, 8.1f);
        }
    }

    private void desenharExecutando(EstadoJogo gs, float tempoIntroducao, float tempoDica) {
        if (tempoIntroducao > 0f) {
            float alpha = tempoIntroducao;
            if (alpha > 1f) alpha = 1f;
            if (alpha < 0f) alpha = 0f;
            centralizar("TENTATIVA " + gs.tentativa(), 1.0f, Tema.MAGENTA, alpha, ALTURA * 0.62f);
        }

        if (tempoDica > 0f) {
            float alpha = tempoDica;
            if (alpha > 1f) alpha = 1f;
            if (alpha < 0f) alpha = 0f;
            alpha = alpha * 0.8f;

            centralizar("SETAS / A D / TOQUE NAS LATERAIS: mover",
                0.32f, Tema.WHITE, alpha, 1.6f);
            centralizar("P ou botao do topo: pausar     R: reiniciar",
                0.32f, Tema.WHITE, alpha, 1.1f);
        }
    }

    private void desenharPausado(EstadoJogo gs, float tempo) {
        centralizar("PAUSADO", 1.0f, Tema.MAGENTA, 1f, 11.8f);
        menuPausa.draw(jogo, tempo);
        centralizar("SETAS + ENTER, ou toque     P / ESC: continuar",
            0.3f, Tema.WHITE, 0.6f, 6.4f);
    }

    private void desenharMorto(EstadoJogo gs, float tempo) {
        centralizar("VOCE BATEU!", 0.9f, Tema.RED, 1f, 10.8f);
        centralizar("PONTOS: " + gs.pontos(), 0.5f, Tema.WHITE, 1f, 9.7f);

        if (gs.novoRecorde()) {
            centralizar("NOVO RECORDE!", 0.5f, Tema.YELLOW, 1f, 9.0f);
        } else {
            centralizar("RECORDE: " + jogo.obterMelhorPontuacao(), 0.4f, Tema.CYAN, 1f, 9.0f);
        }

        if (gs.tempoEstado() > 0.4f) {
            menuMorte.draw(jogo, tempo);
        }
    }

    public int atualizarMenu(EstadoJogo gs, Viewport viewport) {
        if (gs.estaPausado()) {
            return menuPausa.update(viewport);
        }
        if (gs.estaMorto() && gs.tempoEstado() > 0.4f) {
            return menuMorte.update(viewport);
        }
        return -1;
    }

    private void centralizar(String texto, float tamanho, Color cor, float alpha, float y) {
        jogo.drawText(texto, tamanho, cor, alpha, 0f, y, LARGURA, Align.center);
    }
}
