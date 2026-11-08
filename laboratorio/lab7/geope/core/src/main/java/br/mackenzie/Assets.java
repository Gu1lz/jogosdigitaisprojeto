package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

public final class Assets implements Disposable {

    private static final boolean PIXEL_ART = false;

    public static final class SpritesChefe {
        public String name;
        public TextureRegion normal;
        public TextureRegion attack;
        public TextureRegion scream;
        public TextureRegion hit;
        public TextureRegion death;
    }

    public final Animation<TextureRegion> neutral;
    public final Animation<TextureRegion> hit;
    public final Array<SpritesChefe> chefes = new Array<>();

    private final Array<Texture> texturas = new Array<>();

    public Assets() {
        neutral = carregarAnimacao(0.12f, Animation.PlayMode.LOOP,
            "neutro/sprite_001.png",
            "neutro/sprite_002.png",
            "neutro/sprite_003.png",
            "neutro/sprite_006.png",
            "neutro/sprite_009.png");

        hit = carregarAnimacao(0.08f, Animation.PlayMode.NORMAL,
            "hit/1.png",
            "hit/2.png",
            "hit/3.png",
            "hit/4.png",
            "hit/5.png");

        chefes.add(carregarChefe("Tung Tung Tung Sahur", "boss1"));
        chefes.add(carregarChefe("Tralalero Tralala", "boss2"));
    }

    private SpritesChefe carregarChefe(String nome, String pasta) {
        SpritesChefe c = new SpritesChefe();
        c.name   = nome;
        c.normal = carregarUma(pasta + "/normal.png");
        c.attack = carregarUma(pasta + "/ataque.png");
        c.scream = carregarUma(pasta + "/grito.png");
        c.hit    = carregarUma(pasta + "/hit.png");
        c.death  = carregarUma(pasta + "/morte.png");
        return c;
    }

    private TextureRegion carregarUma(String caminho) {
        FileHandle arquivo = Gdx.files.internal(caminho);
        if (!arquivo.exists()) {
            throw new GdxRuntimeException("Sprite nao encontrado: " + caminho);
        }
        Texture t = new Texture(arquivo);
        Texture.TextureFilter filtro;
        if (PIXEL_ART) {
            filtro = Texture.TextureFilter.Nearest;
        } else {
            filtro = Texture.TextureFilter.Linear;
        }
        t.setFilter(filtro, filtro);
        texturas.add(t);
        return new TextureRegion(t);
    }

    private Animation<TextureRegion> carregarAnimacao(float duracaoQuadro,
                                                      Animation.PlayMode modo,
                                                      String... caminhos) {
        Array<TextureRegion> quadros = new Array<>();
        for (String caminho : caminhos) {
            quadros.add(carregarUma(caminho));
        }
        return new Animation<TextureRegion>(duracaoQuadro, quadros, modo);
    }

    @Override
    public void dispose() {
        for (Texture t : texturas) {
            t.dispose();
        }
        texturas.clear();
    }
}
