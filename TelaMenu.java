package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class TelaMenu extends ScreenAdapter {

    private static final float LARGURA = Tema.LARGURA_MUNDO;
    private static final float ALTURA  = Tema.ALTURA_MUNDO;

    private static final int OP_JOGAR = 0;
    private static final int OP_SAIR  = 1;

    private final Main jogo;
    private final SpriteBatch lote;
    private final ShapeRenderer formas;
    private final Assets assets;
    
    private final FitViewport viewport = new FitViewport(LARGURA, ALTURA);
    private final CenarioCidade cenario = new CenarioCidade(777L);
    private final MenuOpcoes menu = new MenuOpcoes(5.0f, 1.0f, 0.55f, "JOGAR", "SAIR");

    private float tempo;

    public TelaMenu(Main jogo) {
        this.jogo = jogo;
        this.lote = jogo.obterLote();
        this.formas = jogo.obterFormas();
        this.assets = jogo.obterAssets();
    }

    @Override
    public void render(float delta) {
        if (delta > 1f / 30f) {
            delta = 1f / 30f;
        }
        tempo += delta;
        cenario.avancar(delta, 5f);

        int escolha = menu.update(viewport);

        if (escolha == OP_JOGAR) {
            jogo.setScreen(new TelaJogo(jogo));
            return;
        }
        if (escolha == OP_SAIR || Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }

        ScreenUtils.clear(Tema.VOID);
        viewport.apply();
        formas.setProjectionMatrix(viewport.getCamera().combined);
        lote.setProjectionMatrix(viewport.getCamera().combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        float hy = ALTURA * 0.5f + MathUtils.sin(tempo * 2f) * 0.25f;

        formas.begin(ShapeRenderer.ShapeType.Filled);
        cenario.desenhar(formas);
        cenario.desenharChuva(formas);
        DesenhoCabeca.desenharBrilho(formas, LARGURA / 2f, hy, 2.4f);
        formas.end();

        lote.begin();
        DesenhoCabeca.desenharSprite(lote, assets.neutral.getKeyFrame(tempo),
            LARGURA / 2f, hy, 3.0f, MathUtils.sin(tempo * 1.5f) * 8f);

        jogo.drawText(Tema.TITULO_JOGO, 1.3f, Tema.CYAN, 1f,
            0f, ALTURA * 0.82f, LARGURA, Align.center);
        jogo.drawText("sobreviva o maximo que puder", 0.4f, Tema.MAGENTA, 0.9f,
            0f, ALTURA * 0.82f - 1.7f, LARGURA, Align.center);

        int melhor = jogo.obterMelhorPontuacao();
        if (melhor > 0) {
            jogo.drawText("RECORDE: " + melhor, 0.5f, Tema.YELLOW, 1f,
                0f, 6.0f, LARGURA, Align.center);
        }

        menu.draw(jogo, tempo);

        jogo.drawText("SETAS / W S + ENTER, ou toque na opcao", 0.32f, Tema.WHITE, 0.6f,
            0f, 2.2f, LARGURA, Align.center);
        jogo.drawText("No jogo: SETAS / A D / TOQUE NAS LATERAIS", 0.32f, Tema.WHITE, 0.5f,
            0f, 1.6f, LARGURA, Align.center);
        jogo.drawText("P / ESC: pausar     R: reiniciar", 0.32f, Tema.WHITE, 0.5f,
            0f, 1.1f, LARGURA, Align.center);
        lote.end();
    }

    @Override
    public void resize(int largura, int altura) {
        viewport.update(largura, altura, true);
    }
}
