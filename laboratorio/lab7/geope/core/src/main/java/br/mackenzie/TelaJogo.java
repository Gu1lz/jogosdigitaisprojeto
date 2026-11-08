package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class TelaJogo extends ScreenAdapter {

    private static final float LARGURA = Tema.LARGURA_MUNDO;
    private static final float ALTURA  = Tema.ALTURA_MUNDO;
    private static final float LIMITE  = CenarioCidade.LIMITE;

    private static final float Y_JOGADOR          = 3.6f;
    private static final float RAIO_COLISAO       = 0.34f;
    private static final float VELOCIDADE_LATERAL = 10f;

    private static final float VELOCIDADE_BASE    = 5f;
    private static final float VELOCIDADE_MAX     = 10f;
    private static final int   PONTOS_POR_METRO   = 10;

    private final Main jogo;
    private final SpriteBatch lote;
    private final ShapeRenderer formas;
    private final Assets assets;
    private final FitViewport viewport = new FitViewport(LARGURA, ALTURA);
    private final CenarioCidade cenario = new CenarioCidade(2026L);
    private final Vector2 toque = new Vector2();
    private final Array<Obstaculo> obstaculos = new Array<>();
    private final SistemaParticulas particulas = new SistemaParticulas();
    private final GeradorPista gerador = new GeradorPista();
    private final EstadoJogo estado;
    private final Chefe chefe = new Chefe();
    private final PainelJogo painel;
    
    private final ArduinoReaderThread leitorArduino;

    private final float[] cronometroRastro = new float[1];

    private float velocidadeRolagem;
    private float xJogador;
    private float velocidadeX;
    private float tempo;
    private float tempoIntroducao;
    private float tempoDica;
    
    private int fase;

    public TelaJogo(Main jogo) {
        this.jogo = jogo;
        this.lote = jogo.obterLote();
        this.formas = jogo.obterFormas();
        this.assets = jogo.obterAssets();
        this.estado = new EstadoJogo(jogo);
        this.painel = new PainelJogo(jogo, chefe, assets);

        // INICIA O ARDUINO (Padrão Linux /dev/ttyUSB0 ou Windows COM3)
        String porta = System.getProperty("arduino.port", "/dev/ttyUSB0");
        leitorArduino = new ArduinoReaderThread(porta);
        if (leitorArduino.isValido()) {
            leitorArduino.start();
        }

        iniciarNovaPartida();
    }

    private void iniciarNovaPartida() {
        estado.iniciarPartida();
        cenario.reiniciar();
        gerador.definirPerfil(GeradorPista.PERFIL_PADRAO);
        gerador.reiniciar(14f);
        chefe.reiniciar();

        obstaculos.clear();
        particulas.limpar();

        velocidadeRolagem = VELOCIDADE_BASE;
        xJogador = LARGURA / 2f;
        velocidadeX = 0f;
        tempoIntroducao = 2f;
        fase = 1;

        if (estado.tentativa() == 1) {
            tempoDica = 6f;
        } else {
            tempoDica = 0f;
        }

        cronometroRastro[0] = 0f;
        estado.definirPontos(0);

        painel.reiniciarMenuPausa();
        painel.reiniciarMenuMorte();

        gerarAdiante();
    }

    private void gerarAdiante() {
        if (fase == 2 || fase == 4) {
            chefe.gerarAte(cenario.obterRolagem() + 20f, obstaculos);
        } else {
            gerador.gerarAdiante(obstaculos, cenario.obterRolagem());
        }
    }

    @Override
    public void render(float delta) {
        if (delta > 1f / 30f) {
            delta = 1f / 30f;
        }

        if (tratarEntrada()) {
            return;
        }

        estado.atualizar(delta);

        if (!estado.estaPausado()) {
            tempo += delta;
            if (estado.estaExecutando()) {
                atualizar(delta);
            }
            particulas.atualizar(delta);
        }

        ScreenUtils.clear(Tema.VOID);
        viewport.apply();
        formas.setProjectionMatrix(viewport.getCamera().combined);
        lote.setProjectionMatrix(viewport.getCamera().combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        boolean morto = estado.estaMorto();
        float flutuacao = DesenhoCabeca.flutuacao(tempo, morto);

        formas.begin(ShapeRenderer.ShapeType.Filled);
        cenario.desenhar(formas);
        DesenhoObstaculos.desenharTodos(formas, obstaculos,
            cenario.obterRolagem(), Y_JOGADOR, ALTURA);
        particulas.desenhar(formas);
        if (!morto) {
            DesenhoCabeca.desenharBrilho(formas, xJogador, Y_JOGADOR + flutuacao, 1f);
        }
        formas.end();

        lote.begin();
        DesenhoCabeca.desenharJogador(lote, assets,
            xJogador, Y_JOGADOR, velocidadeX, tempo, estado.tempoEstado(), morto);
        chefe.draw(lote, assets, LARGURA);
        lote.end();

        formas.begin(ShapeRenderer.ShapeType.Filled);
        cenario.desenharChuva(formas);
        if (estado.estaExecutando()) {
            painel.desenharBotaoPausa(formas);
        }
        if (estado.estaPausado()) {
            painel.desenharEscurecimentoPausa(formas);
        }
        formas.end();

        lote.begin();
        painel.desenharHud(estado, tempo, tempoIntroducao, tempoDica, fase);
        lote.end();
    }

    @Override
    public void resize(int largura, int altura) {
        viewport.update(largura, altura, true);
    }
    
    @Override
    public void dispose() {
        if (leitorArduino != null) {
            leitorArduino.stopReading();
            try {
                leitorArduino.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean tratarEntrada() {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.P)) {
            if (estado.estaExecutando()) {
                estado.pausar();
            } else if (estado.estaPausado()) {
                estado.continuar();
            }
        }

        if (estado.estaExecutando() && Gdx.input.justTouched()) {
            viewport.unproject(toque.set(Gdx.input.getX(), Gdx.input.getY()));
            if (PainelJogo.dentroZonaPausa(toque.x, toque.y)) {
                estado.pausar();
            }
        }

        if (Gdx.input.isKeyJustPressed(Keys.R)) {
            iniciarNovaPartida();
            return false;
        }

        if (!estado.estaExecutando() && Gdx.input.isKeyJustPressed(Keys.M)) {
            jogo.setScreen(new TelaMenu(jogo));
            return true;
        }

        int escolha = painel.atualizarMenu(estado, viewport);
        if (escolha < 0) {
            return false;
        }

        boolean irParaMenu;
        if (estado.estaPausado()) {
            irParaMenu = escolha == 2;
        } else {
            irParaMenu = escolha == 1;
        }

        if (irParaMenu) {
            jogo.setScreen(new TelaMenu(jogo));
            return true;
        }

        if (estado.estaPausado() && escolha == 0) {
            estado.continuar();
        } else {
            iniciarNovaPartida();
        }
        return false;
    }

    private void atualizar(float delta) {
        if (tempoDica > 0f) {
            tempoDica -= delta;
            if (tempoDica < 0f) tempoDica = 0f;
        }
        if (tempoIntroducao > 0f) {
            tempoIntroducao -= delta;
            if (tempoIntroducao < 0f) tempoIntroducao = 0f;
        }

        float direcao = 0f;
        
        // Teclado
        if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A)) {
            direcao -= 1f;
        }
        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) {
            direcao += 1f;
        }
        
        // Toque na tela
        if (Gdx.input.isTouched()) {
            viewport.unproject(toque.set(Gdx.input.getX(), Gdx.input.getY()));
            if (!PainelJogo.dentroZonaPausa(toque.x, toque.y)) {
                if (toque.x < LARGURA / 2f) {
                    direcao -= 1f;
                } else {
                    direcao += 1f;
                }
            }
        }
        
        // Arduino (Acelerômetro)
        if (leitorArduino != null) {
            String dado = leitorArduino.getLastData();
            if (dado.equals("TILT:1")) {
                direcao += 1f;
            } else if (dado.equals("TILT:-1")) {
                direcao -= 1f;
            }
        }

        if (direcao < -1f) direcao = -1f;
        if (direcao > 1f) direcao = 1f;

        float fator = 16f * delta;
        if (fator > 1f) fator = 1f;
        
        velocidadeX += (direcao * VELOCIDADE_LATERAL - velocidadeX) * fator;
        xJogador += velocidadeX * delta;

        float minimoX = LIMITE + RAIO_COLISAO;
        float maximoX = LARGURA - LIMITE - RAIO_COLISAO;
        if (xJogador < minimoX) {
            xJogador = minimoX;
            velocidadeX = 0f;
        } else if (xJogador > maximoX) {
            xJogador = maximoX;
            velocidadeX = 0f;
        }

        velocidadeRolagem = VELOCIDADE_BASE + cenario.obterRolagem() * 0.01f;
        if (velocidadeRolagem > VELOCIDADE_MAX) {
            velocidadeRolagem = VELOCIDADE_MAX;
        }
        if (fase == 2 || fase == 4) {
            velocidadeRolagem += chefe.bonusVelocidade();
        }

        cenario.avancar(delta, velocidadeRolagem);
        estado.definirPontos((int) (cenario.obterRolagem() * PONTOS_POR_METRO));

        chefe.update(delta, estado.pontos(), cenario.obterRolagem());

        if (chefe.assumiuControle()) {
            obstaculos.clear();
            if (chefe.indiceChefeAtual() == 0) {
                fase = 2;
            } else {
                fase = 4;
                chefe.mudarPerfilGerador(GeradorPista.PERFIL_TRALALERO);
            }
        }

        if (chefe.liberouEspaco()) {
            obstaculos.clear();
            if (chefe.indiceChefeAtual() == 0) {
                fase = 3;
                gerador.definirPerfil(GeradorPista.PERFIL_AVANCADO);
            } else {
                fase = 5;
                gerador.definirPerfil(GeradorPista.PERFIL_FINAL);
            }
            gerador.reiniciar(cenario.obterRolagem() + 15f);
        }

        gerarAdiante();
        gerador.descartar(obstaculos, cenario.obterRolagem());

        // Movimento das barreiras
        for (Obstaculo o : obstaculos) {
            if (o.tipo == Obstaculo.TIPO_BARREIRA) {
                o.x += o.velocidadeX * delta;
                if (o.x < CenarioCidade.LIMITE) {
                    o.x = CenarioCidade.LIMITE;
                    o.velocidadeX *= -1f;
                } else if (o.x + o.largura > LARGURA - CenarioCidade.LIMITE) {
                    o.x = LARGURA - CenarioCidade.LIMITE - o.largura;
                    o.velocidadeX *= -1f;
                }
            }
        }

        particulas.rastro(delta, xJogador, Y_JOGADOR, velocidadeRolagem,
            cronometroRastro, Tema.CYAN, Tema.MAGENTA);

        if (Colisao.jogadorBateu(xJogador, Y_JOGADOR, RAIO_COLISAO,
                obstaculos, cenario.obterRolagem())) {
            morrer();
        }
    }

    private void morrer() {
        estado.morrer();
        velocidadeX = 0f;
        particulas.explosao(xJogador, Y_JOGADOR, Tema.CYAN, Tema.MAGENTA, Tema.YELLOW);
    }
}
