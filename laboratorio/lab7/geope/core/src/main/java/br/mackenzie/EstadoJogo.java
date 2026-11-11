package br.mackenzie;

public final class EstadoJogo {

    public static final int EXECUTANDO = 0;
    public static final int PAUSADO    = 1;
    public static final int MORTO      = 2;

    private final Main jogo;

    private int estado;
    private float tempoEstado;
    private int tentativa;
    private int pontos;
    private int recordeInicial;
    private boolean novoRecorde;

    public EstadoJogo(Main jogo) {
        this.jogo = jogo;
    }

    public void iniciarPartida() {
        tentativa++;
        estado = EXECUTANDO;
        tempoEstado = 0f;
        pontos = 0;
        novoRecorde = false;
        recordeInicial = jogo.obterMelhorPontuacao();
    }

    public void atualizar(float delta) {
        tempoEstado += delta;
    }

    public void pausar() {
        if (estado == EXECUTANDO) {
            entrar(PAUSADO);
        }
    }

    public void continuar() {
        if (estado == PAUSADO) {
            entrar(EXECUTANDO);
        }
    }

    public void morrer() {
        entrar(MORTO);
        if (pontos > jogo.obterMelhorPontuacao()) {
            jogo.definirMelhorPontuacao(pontos);
            novoRecorde = true;
        }
    }

    public void definirPontos(int p) {
        pontos = p;
    }

    private void entrar(int e) {
        estado = e;
        tempoEstado = 0f;
    }

    public boolean estaExecutando() { return estado == EXECUTANDO; }
    public boolean estaPausado()    { return estado == PAUSADO; }
    public boolean estaMorto()      { return estado == MORTO; }

    public int estado()             { return estado; }
    public float tempoEstado()      { return tempoEstado; }
    public int tentativa()          { return tentativa; }
    public int pontos()             { return pontos; }
    public int recordeInicial()     { return recordeInicial; }
    public boolean novoRecorde()    { return novoRecorde; }

    public int melhorParaExibir() {
        int melhor = recordeInicial;
        if (pontos > melhor) {
            melhor = pontos;
        }
        return melhor;
    }
}
