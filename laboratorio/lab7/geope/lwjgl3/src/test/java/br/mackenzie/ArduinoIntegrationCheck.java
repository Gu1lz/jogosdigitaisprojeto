package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

/** Exercita o menu, a renderizacao e a partida reais com uma porta serial virtual. */
public final class ArduinoIntegrationCheck extends Main {
    private static Throwable falha;
    private final Set<Integer> teclas = new HashSet<>();
    private final Set<Integer> teclasNovas = new HashSet<>();
    private float delta;
    private boolean tocando;
    private boolean toqueNovo;
    private float yToque = 5f;

    @Override
    public int obterMelhorPontuacao() {
        return 0;
    }

    @Override
    public void definirMelhorPontuacao(int pontos) {
        // O teste nao altera o recorde salvo pelo jogador.
    }

    @Override
    public void create() {
        super.create();
        Input entradaOriginal = Gdx.input;
        Graphics graficosOriginais = Gdx.graphics;
        Gdx.input = (Input) Proxy.newProxyInstance(Input.class.getClassLoader(),
            new Class<?>[] { Input.class }, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "isKeyPressed": return teclas.contains((Integer) args[0]);
                    case "isKeyJustPressed": return teclasNovas.contains((Integer) args[0]);
                    case "isTouched": return tocando;
                    case "justTouched": return toqueNovo;
                    case "getX": return graficosOriginais.getWidth() / 2;
                    case "getY": return (int) (graficosOriginais.getHeight() * (16f - yToque) / 16f);
                    default: return method.invoke(entradaOriginal, args);
                }
            });
        Gdx.graphics = (Graphics) Proxy.newProxyInstance(Graphics.class.getClassLoader(),
            new Class<?>[] { Graphics.class }, (proxy, method, args) -> {
                if (method.getName().equals("getDeltaTime")) return delta;
                return method.invoke(graficosOriginais, args);
            });
        try {
            verificar();
            System.out.println("PASS ALL");
        } catch (Throwable erro) {
            falha = erro;
            erro.printStackTrace();
        } finally {
            Gdx.input = entradaOriginal;
            Gdx.graphics = graficosOriginais;
            Gdx.app.exit();
        }
    }

    private void verificar() throws Exception {
        String porta = System.clearProperty("arduino.port");
        try {
            verificar(ArduinoReaderThread.getPortaConfigurada().equals("/dev/ttyACM0"),
                "porta padrao coincide com o teste do quadrado verde");
        } finally {
            System.setProperty("arduino.port", porta);
        }
        ArduinoReaderThread leitor = obterLeitorArduino();
        esperar(leitor::isValido, "leitor conectado no menu inicial");
        verificar(getScreen() instanceof TelaMenu, "entrada pela tela inicial real");

        tocando = true;
        toqueNovo = true;
        quadro(1f / 60f);
        verificar(getScreen() instanceof TelaJogo, "toque em JOGAR abre a partida");
        TelaJogo tela = (TelaJogo) getScreen();
        verificar(campo(tela, "leitorArduino") == leitor, "partida usa a conexao aberta no menu");
        CenarioCidade cenario = (CenarioCidade) campo(tela, "cenario");
        EstadoJogo estado = (EstadoJogo) campo(tela, "estado");
        quadros(180);
        verificar(estado.estaExecutando() && cenario.obterRolagem() == 0f,
            "partida aguarda a primeira leitura sem gerar colisoes");
        capturar("aguardando-sensor");

        System.out.println("REQUEST partial");
        Thread.sleep(120);
        verificar(leitor.isAguardandoSensor(), "linha parcial nao libera a pista");
        receber(leitor, "left", "TILT:-1");
        float antes = x(tela);
        teclas.add(Keys.RIGHT);
        quadro(1f / 60f);
        verificar(x(tela) < antes && velocidade(tela) == -10f,
            "sensor move para esquerda mesmo com tecla oposta e toque do menu pressionados");
        teclas.clear();
        verificar(!leitor.isAguardandoSensor(), "leitura valida libera a pista");

        receber(leitor, "center", "TILT:0");
        antes = x(tela);
        quadros(2);
        verificar(x(tela) == antes && velocidade(tela) == 0f,
            "centro para imediatamente e o toque que abriu o menu nao movimenta o jogador");
        tocando = false;
        receber(leitor, "right", "TILT:1");
        antes = x(tela);
        teclas.add(Keys.LEFT);
        quadro(1f / 60f);
        verificar(x(tela) > antes && velocidade(tela) == 10f,
            "sensor move para direita mesmo com tecla oposta");
        teclas.clear();
        quadros(60);
        verificar(Math.abs(x(tela) - 8.01f) < 0.001f, "movimento respeita a borda direita");
        capturar("sensor-direita");

        System.out.println("REQUEST noise");
        Thread.sleep(120);
        verificar(leitor.getLastData().equals("TILT:1"), "ruido e INFO preservam a ultima direcao valida");
        System.out.println("REQUEST error");
        esperar(() -> leitor.getStatus().equals("Sensor sem leitura"), "erro do sensor identificado");
        antes = x(tela);
        float rolagem = cenario.obterRolagem();
        quadros(180);
        verificar(x(tela) == antes && cenario.obterRolagem() == rolagem && estado.estaExecutando(),
            "erro do sensor para o personagem e suspende a pista sem matar o jogador");
        capturar("sensor-sem-leitura");

        receber(leitor, "recover", "TILT:0");
        teclas.add(Keys.LEFT);
        antes = x(tela);
        quadro(1f / 60f);
        verificar(x(tela) < antes, "teclado continua utilizavel com sensor centralizado");
        teclas.clear();
        tecla(Keys.P);
        verificar(estado.estaPausado(), "pausa pelo fluxo normal de entrada");
        receber(leitor, "right_paused", "TILT:1");
        antes = x(tela);
        quadros(10);
        verificar(x(tela) == antes, "pausa preserva a posicao enquanto a serial continua recebendo");
        tecla(Keys.P);
        verificar(estado.estaExecutando(), "retomada da partida");
        tecla(Keys.R);
        verificar(campo(tela, "leitorArduino") == leitor && leitor.isValido(),
            "reiniciar preserva a conexao serial");

        @SuppressWarnings("unchecked")
        Array<Obstaculo> obstaculos = (Array<Obstaculo>) campo(tela, "obstaculos");
        obstaculos.add(new Obstaculo(0f, cenario.obterRolagem(), 9f, 1f,
            false, false, false, Obstaculo.TIPO_BLOCO));
        quadro(1f / 60f);
        verificar(estado.estaMorto(), "colisao real entra no estado de morte");
        receber(leitor, "left_dead", "TILT:-1");
        antes = x(tela);
        quadro(1f / 60f);
        verificar(x(tela) == antes, "estado de morte interrompe o movimento");
        receber(leitor, "dead_center", "TILT:0");
        quadros(30);
        tocando = true;
        toqueNovo = true;
        yToque = 7.6f;
        quadro(1f / 60f);
        quadros(2);
        verificar(estado.estaExecutando() && x(tela) == 4.5f,
            "toque em TENTAR DE NOVO reinicia sem deslocar o personagem centralizado");
        tocando = false;
        yToque = 5f;
        receber(leitor, "left_restart", "TILT:-1");
        tecla(Keys.R);
        verificar(estado.estaExecutando() && x(tela) < 4.5f,
            "R reinicia e aplica o comando que esta chegando do sensor");

        tecla(Keys.P);
        tecla(Keys.M);
        verificar(getScreen() instanceof TelaMenu && leitor.isValido() && leitor.isAlive(),
            "retorno ao menu preserva a mesma conexao");
        tecla(Keys.ENTER);
        tela = (TelaJogo) getScreen();
        verificar(campo(tela, "leitorArduino") == leitor, "segunda partida reutiliza o mesmo leitor");
        quadro(1f / 60f);
        verificar(x(tela) < 4.5f, "segunda partida recebe comandos imediatamente");

        System.out.println("REQUEST disconnect");
        esperar(() -> !leitor.isValido(), "desconexao detectada");
        cenario = (CenarioCidade) campo(tela, "cenario");
        rolagem = cenario.obterRolagem();
        quadros(180);
        verificar(cenario.obterRolagem() == rolagem && leitor.getLastData().equals("TILT:0"),
            "desconexao USB neutraliza o comando e suspende a pista");
        System.out.println("REQUEST reconnect");
        esperar(leitor::isValido, "reconexao automatica");
        receber(leitor, "after_reconnect", "TILT:1");
        antes = x(tela);
        quadro(1f / 60f);
        verificar(x(tela) > antes, "partida retoma movimento depois de reconectar");

        System.out.println("REQUEST ready");
        esperar(leitor::isAguardandoSensor, "READY aguarda o primeiro TILT");
        receber(leitor, "final_center", "TILT:0");
        quadro(1f / 60f);
        verificar(velocidade(tela) == 0f, "centro depois da reconexao para o personagem");
    }

    private void quadro(float passo) {
        delta = passo;
        super.render();
        teclasNovas.clear();
        toqueNovo = false;
    }

    private void quadros(int quantidade) {
        for (int i = 0; i < quantidade; i++) quadro(1f / 60f);
    }

    private void tecla(int tecla) {
        teclasNovas.add(tecla);
        quadro(1f / 60f);
    }

    private static Object campo(Object objeto, String nome) throws Exception {
        Field campo = objeto.getClass().getDeclaredField(nome);
        campo.setAccessible(true);
        return campo.get(objeto);
    }

    private static float x(TelaJogo tela) throws Exception {
        return (Float) campo(tela, "xJogador");
    }

    private static float velocidade(TelaJogo tela) throws Exception {
        return (Float) campo(tela, "velocidadeX");
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
        System.out.println("PASS " + mensagem);
    }

    private static void esperar(BooleanSupplier condicao, String mensagem) throws Exception {
        long limite = System.nanoTime() + 5_000_000_000L;
        while (!condicao.getAsBoolean() && System.nanoTime() < limite) Thread.sleep(5);
        verificar(condicao.getAsBoolean(), mensagem);
    }

    private static void receber(ArduinoReaderThread leitor, String pedido, String esperado) throws Exception {
        System.out.println("REQUEST " + pedido);
        esperar(() -> leitor.getLastData().equals(esperado) && !leitor.isAguardandoSensor(),
            pedido + " recebido pela serial");
    }

    private void capturar(String nome) {
        String pasta = System.getProperty("arduino.check.output");
        if (pasta == null) return;
        Pixmap imagem = ScreenUtils.getFrameBufferPixmap(0, 0,
            Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        PixmapIO.writePNG(Gdx.files.absolute(pasta + "/" + nome + ".png"), imagem, -1, true);
        imagem.dispose();
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration configuracao = new Lwjgl3ApplicationConfiguration();
        configuracao.setTitle("Verificacao da integracao Arduino");
        configuracao.setWindowedMode(540, 960);
        configuracao.setInitialVisible(false);
        configuracao.disableAudio(true);
        new Lwjgl3Application(new ArduinoIntegrationCheck(), configuracao);
        if (falha != null) throw new AssertionError("Falha na integracao Arduino", falha);
    }
}
