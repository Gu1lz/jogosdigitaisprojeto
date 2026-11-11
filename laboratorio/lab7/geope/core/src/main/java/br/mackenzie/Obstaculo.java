package br.mackenzie;

public final class Obstaculo {
    public static final int TIPO_BLOCO = 0;
    public static final int TIPO_ESPINHO = 1;
    public static final int TIPO_LASER = 2;
    public static final int TIPO_BARREIRA = 3;

    public float x;
    public final float posicao;
    public final float largura;
    public final float altura;
    public final boolean espinho;
    public boolean daChefe;
    public boolean doTralalero;
    public final int tipo;
    public float velocidadeX;

    public Obstaculo(float x, float posicao, float largura, float altura, boolean espinho, boolean daChefe, boolean doTralalero, int tipo) {
        this.x = x;
        this.posicao = posicao;
        this.largura = largura;
        this.altura = altura;
        this.espinho = espinho;
        this.daChefe = daChefe;
        this.doTralalero = doTralalero;
        this.tipo = tipo;
        this.velocidadeX = 0f;
    }
}
