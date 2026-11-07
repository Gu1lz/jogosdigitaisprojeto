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

    public static final class BossSprites {
        public String name;
        public TextureRegion normal;
        public TextureRegion attack;
        public TextureRegion scream;
        public TextureRegion hit;
        public TextureRegion death;
    }

    public final Animation<TextureRegion> neutral;
    public final Animation<TextureRegion> hit;
    public final Array<BossSprites> bosses = new Array<>();

    private final Array<Texture> textures = new Array<>();

    public Assets() {
        neutral = load(0.12f, Animation.PlayMode.LOOP,
            "neutro/sprite_001.png",
            "neutro/sprite_002.png",
            "neutro/sprite_003.png",
            "neutro/sprite_006.png",
            "neutro/sprite_009.png");

        hit = load(0.08f, Animation.PlayMode.NORMAL,
            "hit/1.png",
            "hit/2.png",
            "hit/3.png",
            "hit/4.png",
            "hit/5.png");

        bosses.add(loadBoss("Tung Tung Tung Sahur", "boss1"));
    }

    private BossSprites loadBoss(String name, String folder) {
        BossSprites b = new BossSprites();
        b.name   = name;
        b.normal = loadOne(folder + "/normal.png");
        b.attack = loadOne(folder + "/ataque.png");
        b.scream = loadOne(folder + "/grito.png");
        b.hit    = loadOne(folder + "/hit.png");
        b.death  = loadOne(folder + "/morte.png");
        return b;
    }

    private TextureRegion loadOne(String path) {
        FileHandle file = Gdx.files.internal(path);
        if (!file.exists()) {
            throw new GdxRuntimeException("Sprite nao encontrado: " + path
                + " (confira se a pasta assets/ foi commitada e se o working dir do lwjgl3 aponta para ela)");
        }
        Texture t = new Texture(file);
        Texture.TextureFilter filter = PIXEL_ART ? Texture.TextureFilter.Nearest : Texture.TextureFilter.Linear;
        t.setFilter(filter, filter);
        textures.add(t);
        return new TextureRegion(t);
    }

    private Animation<TextureRegion> load(float frameDuration, Animation.PlayMode mode, String... paths) {
        Array<TextureRegion> frames = new Array<>();
        for (String path : paths) {
            frames.add(loadOne(path));
        }
        return new Animation<TextureRegion>(frameDuration, frames, mode);
    }

    @Override
    public void dispose() {
        for (Texture t : textures) t.dispose();
        textures.clear();
    }
}