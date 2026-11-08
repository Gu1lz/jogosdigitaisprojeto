package br.mackenzie;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public final class Colisao {

    private Colisao() {}

    public static boolean circuloColideRetangulo(float cx, float cy, float raio,
                                                 float rx, float ry, float rw, float rh) {
        float dx = cx - MathUtils.clamp(cx, rx, rx + rw);
        float dy = cy - MathUtils.clamp(cy, ry, ry + rh);
        return dx * dx + dy * dy <= raio * raio;
    }

    public static boolean jogadorBateu(float xJogador, float yJogador, float raio,
                                       Array<Obstaculo> obstaculos, float rolagem) {
        for (Obstaculo o : obstaculos) {
            float y0 = yJogador + (o.posicao - rolagem);
            if (y0 > yJogador + 2f || y0 + o.altura < yJogador - 2f) continue;

            float rx = o.x, ry = y0, rw = o.largura, rh = o.altura;

            if (o.tipo == Obstaculo.TIPO_LASER) {
                // O laser só machuca se bater no feixe central
                rx += o.largura * 0.3f;
                rw -= o.largura * 0.6f;
                ry += 0.2f;
                rh -= 0.4f;
            } else if (o.tipo == Obstaculo.TIPO_ESPINHO) {
                rx += 0.05f;
                rw -= 0.1f;
                ry += rh * 0.3f;
                rh *= 0.7f;
            }

            if (circuloColideRetangulo(xJogador, yJogador, raio, rx, ry, rw, rh)) return true;
        }
        return false;
    }
}
