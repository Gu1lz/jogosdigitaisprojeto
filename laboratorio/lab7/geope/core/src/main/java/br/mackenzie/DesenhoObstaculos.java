package br.mackenzie;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

public final class DesenhoObstaculos {

    private static final Color MARROM_PAULADA = new Color(0.45f, 0.25f, 0.1f, 1f);
    private static final Color MARROM_ESCURO  = new Color(0.25f, 0.12f, 0.05f, 1f);
    private static final Color AZUL_TRALALERO = new Color(0.1f, 0.4f, 1f, 1f);
    private static final Color LASER_VERDE    = new Color(0.1f, 1f, 0.2f, 1f);
    private static final Color BARREIRA_AMARELA = new Color(1f, 0.8f, 0.1f, 1f);

    private DesenhoObstaculos() {}

    public static void desenharTodos(ShapeRenderer s, Array<Obstaculo> obstaculos,
                                     float rolagem, float yJogador, float alturaMundo) {
        for (Obstaculo o : obstaculos) {
            float y0 = yJogador + (o.posicao - rolagem);
            if (y0 > alturaMundo + 0.3f) continue;
            if (y0 + o.altura < -0.3f) continue;
            
            if (o.tipo == Obstaculo.TIPO_LASER) {
                desenharLaser(s, o, y0);
            } else if (o.tipo == Obstaculo.TIPO_BARREIRA) {
                desenharBarreira(s, o, y0);
            } else if (o.espinho) {
                desenharEspinhos(s, o, y0);
            } else {
                desenharBloco(s, o, y0);
            }
        }
    }

    private static void desenharLaser(ShapeRenderer s, Obstaculo o, float y0) {
        // Efeito de brilho
        s.setColor(LASER_VERDE.r, LASER_VERDE.g, LASER_VERDE.b, 0.2f);
        s.rect(o.x - 0.1f, y0, o.largura + 0.2f, o.altura);
        s.setColor(LASER_VERDE.r, LASER_VERDE.g, LASER_VERDE.b, 0.4f);
        s.rect(o.x - 0.05f, y0, o.largura + 0.1f, o.altura);
        
        // Feixe central
        s.setColor(Color.WHITE);
        s.rect(o.x + o.largura/2f - 0.05f, y0, 0.1f, o.altura);
        
        // Emissores (Base e Topo)
        s.setColor(Tema.BLOCK_BODY);
        s.rect(o.x, y0, o.largura, 0.2f);
        s.rect(o.x, y0 + o.altura - 0.2f, o.largura, 0.2f);
        s.setColor(LASER_VERDE);
        s.rect(o.x, y0, o.largura, 0.1f);
        s.rect(o.x, y0 + o.altura - 0.1f, o.largura, 0.1f);
    }

    private static void desenharBarreira(ShapeRenderer s, Obstaculo o, float y0) {
        s.setColor(BARREIRA_AMARELA.r, BARREIRA_AMARELA.g, BARREIRA_AMARELA.b, 0.2f);
        s.rect(o.x - 0.1f, y0 - 0.1f, o.largura + 0.2f, o.altura + 0.2f);
        
        s.setColor(Tema.BLOCK_BODY);
        s.rect(o.x, y0, o.largura, o.altura);

        // Listras de perigo
        s.setColor(BARREIRA_AMARELA);
        float step = 0.4f;
        for (float gx = o.x; gx < o.x + o.largura; gx += step) {
            float w = Math.min(0.2f, o.x + o.largura - gx);
            s.rect(gx, y0, w, o.altura);
        }
        s.setColor(BARREIRA_AMARELA);
        s.rect(o.x, y0, o.largura, 0.05f);
        s.rect(o.x, y0 + o.altura - 0.05f, o.largura, 0.05f);
    }

    private static void desenharBloco(ShapeRenderer s, Obstaculo o, float y0) {
        Color corBase = o.doTralalero ? AZUL_TRALALERO : (o.daChefe ? MARROM_PAULADA : Tema.MAGENTA);
        Color corCorpo = o.daChefe ? MARROM_ESCURO : Tema.BLOCK_BODY;

        s.setColor(corBase.r, corBase.g, corBase.b, 0.16f);
        s.rect(o.x - 0.12f, y0 - 0.12f, o.largura + 0.24f, o.altura + 0.24f);

        s.setColor(corCorpo);
        s.rect(o.x, y0, o.largura, o.altura);

        s.setColor(corBase.r, corBase.g, corBase.b, 0.5f);
        for (float gx = o.x + 1f; gx < o.x + o.largura - 0.05f; gx += 1f) {
            s.rect(gx - 0.015f, y0, 0.03f, o.altura);
        }

        s.setColor(corBase);
        s.rect(o.x, y0, o.largura, 0.06f);
        s.rect(o.x, y0 + o.altura - 0.06f, o.largura, 0.06f);
        s.rect(o.x, y0, 0.06f, o.altura);
        s.rect(o.x + o.largura - 0.06f, y0, 0.06f, o.altura);
    }

    private static void desenharEspinhos(ShapeRenderer s, Obstaculo o, float y0) {
        Color cor = o.doTralalero ? AZUL_TRALALERO : Tema.RED;
        s.setColor(cor.r, cor.g, cor.b, 0.12f);
        s.rect(o.x - 0.1f, y0 - 0.1f, o.largura + 0.2f, o.altura + 0.2f);

        int n = Math.round(o.largura / 0.6f);
        if (n < 1) n = 1;
        float larguraFatia = o.largura / n;
        float topo = y0 + o.altura;

        for (int i = 0; i < n; i++) {
            float x1 = o.x + i * larguraFatia;
            float x2 = x1 + larguraFatia;
            float xm = (x1 + x2) / 2f;

            s.setColor(Tema.BLOCK_BODY);
            s.triangle(x1, topo, x2, topo, xm, y0);

            s.setColor(cor);
            s.rectLine(x1, topo, xm, y0, 0.05f);
            s.rectLine(xm, y0, x2, topo, 0.05f);
        }

        s.setColor(cor);
        s.rect(o.x, topo - 0.08f, o.largura, 0.08f);
    }
}
