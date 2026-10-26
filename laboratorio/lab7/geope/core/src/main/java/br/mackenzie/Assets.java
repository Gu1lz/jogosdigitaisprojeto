package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public final class Assets implements Disposable {

    private static final boolean PIXEL_ART = false; // true = filtro Nearest

    public final Animation<TextureRegion> neutral;
    public final Animation<TextureRegion> hit;

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
    }

    private Animation<TextureRegion> load(float frameDuration, Animation.PlayMode mode, String... paths) {
        Array<TextureRegion> frames = new Array<>();
        Texture.TextureFilter filter = PIXEL_ART ? Texture.TextureFilter.Nearest : Texture.TextureFilter.Linear;
        for (String path : paths) {
            Texture t = new Texture(Gdx.files.internal(path));
            t.setFilter(filter, filter);
            textures.add(t);
            frames.add(new TextureRegion(t));
        }
        return new Animation<TextureRegion>(frameDuration, frames, mode);
    }

    @Override
    public void dispose() {
        for (Texture t : textures) t.dispose();
        textures.clear();
    }
}