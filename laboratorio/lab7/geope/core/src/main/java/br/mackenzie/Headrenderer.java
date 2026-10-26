package br.mackenzie;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class HeadRenderer {

    private HeadRenderer() {}

    public static void drawGlow(ShapeRenderer s, float cx, float cy, float size) {
        float r = size / 2f;
        s.setColor(Theme.CYAN.r, Theme.CYAN.g, Theme.CYAN.b, 0.10f);
        s.circle(cx, cy, r * 1.8f, 40);
        s.setColor(Theme.CYAN.r, Theme.CYAN.g, Theme.CYAN.b, 0.16f);
        s.circle(cx, cy, r * 1.35f, 40);
    }

    public static void drawSprite(SpriteBatch b, TextureRegion region,
                                  float cx, float cy, float size, float tiltDeg) {
        float rw = region.getRegionWidth();
        float rh = region.getRegionHeight();
        float scale = size / Math.max(rw, rh);
        float w = rw * scale;
        float h = rh * scale;

        b.setColor(Color.WHITE);
        b.draw(region, cx - w / 2f, cy - h / 2f, w / 2f, h / 2f, w, h, 1f, 1f, tiltDeg);
    }
}