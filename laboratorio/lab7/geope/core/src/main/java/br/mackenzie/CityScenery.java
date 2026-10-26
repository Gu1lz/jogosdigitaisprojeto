package br.mackenzie;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.FloatArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class CityScenery {

    public static final float GROUND_TOP = 2f;

    private static final float W = Theme.WORLD_W;
    private static final float H = Theme.WORLD_H;

    private static final float GRID = 2f;       
    private static final int RAIN_DROPS = 90;
    private static final int SUN_SLICES = 20;

    private final Layer far;
    private final Layer near;

    private final float[] rainX = new float[RAIN_DROPS];
    private final float[] rainY = new float[RAIN_DROPS];
    private final float[] rainSpeed = new float[RAIN_DROPS];

    private final Color tmp = new Color();
    private final Color hazeBottom = new Color(Theme.MAGENTA.r, Theme.MAGENTA.g, Theme.MAGENTA.b, 0.38f);
    private final Color hazeTop = new Color(Theme.MAGENTA.r, Theme.MAGENTA.g, Theme.MAGENTA.b, 0f);
    private final Color glowBottom = new Color(Theme.CYAN.r, Theme.CYAN.g, Theme.CYAN.b, 0f);
    private final Color glowTop = new Color(Theme.CYAN.r, Theme.CYAN.g, Theme.CYAN.b, 0.30f);
    private float scroll;

    public CityScenery(long seed) {
        Random rnd = new Random(seed);
        far = buildLayer(rnd, 24f, 0.12f, 1.2f, 2.4f, 2.5f, 6.0f, Theme.BUILDING_FAR, Theme.PURPLE, false, 0.45f);
        near = buildLayer(rnd, 20f, 0.35f, 1.4f, 2.8f, 1.5f, 4.2f, Theme.BUILDING_NEAR, Theme.CYAN, true, 0.85f);
        for (int i = 0; i < RAIN_DROPS; i++) {
            respawnDrop(i, true);
        }
    }

    public void advance(float delta, float speed) {
        scroll += speed * delta;
        for (int i = 0; i < RAIN_DROPS; i++) {
            rainY[i] -= rainSpeed[i] * delta;
            rainX[i] -= (1.2f + speed * 0.3f) * delta;
            if (rainY[i] < GROUND_TOP || rainX[i] < -0.5f) {
                respawnDrop(i, false);
            }
        }
    }

    public void reset() {
        scroll = 0f;
    }

    public float getScroll() {
        return scroll;
    }

    private void respawnDrop(int i, boolean anywhere) {
        rainX[i] = MathUtils.random(0f, W + 3f);
        rainY[i] = anywhere ? MathUtils.random(GROUND_TOP, H) : H + MathUtils.random(0f, 2f);
        rainSpeed[i] = MathUtils.random(9f, 14f);
    }

    public void draw(ShapeRenderer s) {
        drawSky(s);
        drawSun(s);
        drawLayer(s, far);
        drawHaze(s);
        drawLayer(s, near);
        drawGround(s);
    }

    public void drawRain(ShapeRenderer s) {
        s.setColor(Theme.CYAN.r, Theme.CYAN.g, Theme.CYAN.b, 0.35f);
        for (int i = 0; i < RAIN_DROPS; i++) {
            if (rainX[i] > W) continue;
            s.rectLine(rainX[i], rainY[i], rainX[i] + 0.1f, rainY[i] + 0.4f, 0.015f);
        }
    }

    private void drawSky(ShapeRenderer s) {
        s.rect(0, GROUND_TOP, W, H - GROUND_TOP,
            Theme.SKY_BOTTOM, Theme.SKY_BOTTOM, Theme.SKY_TOP, Theme.SKY_TOP);
    }

    private void drawSun(ShapeRenderer s) {
        float cx = 11.8f;
        float cy = 5.3f;
        float r = 2.1f;
        s.setColor(Theme.MAGENTA.r, Theme.MAGENTA.g, Theme.MAGENTA.b, 0.10f);
        s.circle(cx, cy, r * 1.4f, 48);
        float step = 2f * r / SUN_SLICES;
        for (int i = 0; i < SUN_SLICES; i++) {
            float t = i / (float) (SUN_SLICES - 1);        
            float y0 = cy - r + i * step;
            float dy = y0 + step / 2f - cy;
            float halfWidth = (float) Math.sqrt(Math.max(0f, r * r - dy * dy));
            float fill = t < 0.55f ? 0.3f + t * 1.25f : 1f;

            tmp.set(Theme.MAGENTA).lerp(Theme.YELLOW, t);
            s.setColor(tmp.r, tmp.g, tmp.b, 0.95f);
            s.rect(cx - halfWidth, y0, halfWidth * 2f, step * fill);
        }
    }

    private void drawHaze(ShapeRenderer s) {
        s.rect(0, GROUND_TOP, W, 2.4f, hazeBottom, hazeBottom, hazeTop, hazeTop);
    }

    private void drawLayer(ShapeRenderer s, Layer layer) {
        float offset = (scroll * layer.parallax) % layer.tileWidth;
        for (int copy = 0; copy < 2; copy++) {
            float base = copy * layer.tileWidth - offset;
            for (Building b : layer.buildings) {
                float bx = base + b.x;
                if (bx > W || bx + b.w < 0f) continue;

                s.setColor(layer.body);
                s.rect(bx, GROUND_TOP, b.w, b.h);
                s.setColor(layer.edge.r, layer.edge.g, layer.edge.b, 0.7f);
                s.rect(bx, GROUND_TOP + b.h - 0.05f, b.w, 0.05f);
                s.setColor(b.windowColor);
                for (int i = 0; i < b.windows.length; i += 2) {
                    s.rect(bx + b.windows[i], GROUND_TOP + b.windows[i + 1], 0.14f, 0.2f);
                }

                if (b.hasSign) {
                    float sx = bx + b.w * 0.82f;
                    float sy = GROUND_TOP + b.signY;
                    s.setColor(b.signColor.r, b.signColor.g, b.signColor.b, 0.25f);
                    s.rect(sx - 0.08f, sy - 0.05f, 0.28f, b.signLen + 0.1f);
                    s.setColor(b.signColor);
                    s.rect(sx, sy, 0.12f, b.signLen);
                }
            }
        }
    }

    private void drawGround(ShapeRenderer s) {
        s.setColor(Theme.GROUND);
        s.rect(0, 0, W, GROUND_TOP);
        s.rect(0, GROUND_TOP - 0.7f, W, 0.7f, glowBottom, glowBottom, glowTop, glowTop);
        s.setColor(Theme.PURPLE.r, Theme.PURPLE.g, Theme.PURPLE.b, 0.45f);
        float off = scroll % GRID;
        for (float x = -off; x < W; x += GRID) {
            s.rect(x, 0, 0.03f, GROUND_TOP);
        }
        s.rect(0, GROUND_TOP * 0.5f, W, 0.02f);
        s.setColor(Theme.CYAN);
        s.rect(0, GROUND_TOP - 0.04f, W, 0.08f);
    }

    private static Layer buildLayer(Random rnd, float tileWidth, float parallax,
                                    float minW, float maxW, float minH, float maxH,
                                    Color body, Color edge, boolean signs, float windowAlpha) {
        List<Building> list = new ArrayList<>();
        float x = 0f;
        while (x < tileWidth - 0.6f) {
            float w = Math.min(minW + rnd.nextFloat() * (maxW - minW), tileWidth - x);
            float h = minH + rnd.nextFloat() * (maxH - minH);
            list.add(new Building(rnd, x, w, h, signs, windowAlpha));
            x += w + rnd.nextFloat() * 0.35f;
        }
        return new Layer(list.toArray(new Building[0]), tileWidth, parallax, body, edge);
    }

    private static final class Layer {
        final Building[] buildings;
        final float tileWidth;
        final float parallax;
        final Color body;
        final Color edge;

        Layer(Building[] buildings, float tileWidth, float parallax, Color body, Color edge) {
            this.buildings = buildings;
            this.tileWidth = tileWidth;
            this.parallax = parallax;
            this.body = body;
            this.edge = edge;
        }
    }

    private static final class Building {
        final float x, w, h;
        final float[] windows;          
        final Color windowColor;
        final boolean hasSign;
        final Color signColor;
        final float signY, signLen;

        Building(Random rnd, float x, float w, float h, boolean withSign, float windowAlpha) {
            this.x = x;
            this.w = w;
            this.h = h;

            int pick = rnd.nextInt(3);
            Color base = pick == 0 ? Theme.CYAN : pick == 1 ? Theme.YELLOW : Theme.MAGENTA;
            windowColor = new Color(base.r, base.g, base.b, windowAlpha);

            FloatArray win = new FloatArray();
            int cols = (int) ((w - 0.3f) / 0.34f);
            int rows = (int) ((h - 0.4f) / 0.5f);
            for (int c = 0; c < cols; c++) {
                for (int r = 0; r < rows; r++) {
                    if (rnd.nextFloat() < 0.16f) {
                        win.add(0.15f + c * 0.34f);
                        win.add(0.25f + r * 0.5f);
                    }
                }
            }
            windows = win.toArray();

            hasSign = withSign && rnd.nextFloat() < 0.4f;
            signColor = rnd.nextBoolean() ? Theme.MAGENTA : Theme.CYAN;
            signLen = Math.min(0.9f + rnd.nextFloat() * 0.8f, h * 0.6f);
            signY = 0.2f + rnd.nextFloat() * Math.max(0f, h - signLen - 0.5f);
        }
    }
}