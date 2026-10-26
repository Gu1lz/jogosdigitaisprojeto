package br.mackenzie;

import static br.mackenzie.Main.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.FloatArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Cenario vertical: o jogador sobe, o mundo desce. */
public class CityScenery {

    private static final float TILE = 24f;
    private static final float GRID = 2f;
    private static final int STREAKS = 60;

    private final List<Building> buildings = new ArrayList<>();
    private final float[] sx = new float[STREAKS];
    private final float[] sy = new float[STREAKS];
    private final float[] sv = new float[STREAKS];
    private float scroll;

    public CityScenery(long seed) {
        Random rnd = new Random(seed);
        addBuildings(rnd, 0.9f, 2.4f, 2.5f, 6f, 0.12f, DARK, PURPLE, 0.45f);   // fundo
        addBuildings(rnd, 0.35f, 0.5f, 1.5f, 4f, 0.35f, VOID, CYAN, 0.85f);    // frente
        for (int i = 0; i < STREAKS; i++) respawn(i, true);
    }

    public void advance(float delta, float speed) {
        scroll += speed * delta;
        for (int i = 0; i < STREAKS; i++) {
            sy[i] -= (sv[i] + speed * 1.5f) * delta;
            if (sy[i] < -1f) respawn(i, false);
        }
    }

    public void reset() {
        scroll = 0f;
    }

    public float getScroll() {
        return scroll;
    }

    public void draw(ShapeRenderer s) {
        s.rect(0, 0, W, H, SKY_BOTTOM, SKY_BOTTOM, SKY_TOP, SKY_TOP);

        tint(s, PURPLE, 0.2f);
        float lane = (W - 2f * BOUND) / 6f;
        for (int k = 0; k <= 6; k++) s.rect(BOUND + k * lane - 0.01f, 0, 0.02f, H);
        for (float y = -(scroll % GRID); y < H; y += GRID) s.rect(0, y, W, 0.02f);

        for (Building b : buildings) drawBuilding(s, b);

        tint(s, CYAN, 0.18f);
        s.rect(BOUND - 0.12f, 0, 0.24f, H);
        s.rect(W - BOUND - 0.12f, 0, 0.24f, H);
        s.setColor(CYAN);
        s.rect(BOUND - 0.03f, 0, 0.06f, H);
        s.rect(W - BOUND - 0.03f, 0, 0.06f, H);

        tint(s, CYAN, 0.25f);
        for (int i = 0; i < STREAKS; i++) s.rect(sx[i], sy[i], 0.02f, 0.6f);
    }

    private void drawBuilding(ShapeRenderer s, Building b) {
        float off = (scroll * b.parallax) % TILE;
        for (int copy = 0; copy < 2; copy++) {
            float y = copy * TILE - off + b.y;
            if (y > H || y + b.h < 0f) continue;

            s.setColor(b.body);
            s.rect(b.x, y, b.w, b.h);

            tint(s, b.edge, 0.7f);
            s.rect(b.left ? b.x + b.w - 0.05f : b.x, y, 0.05f, b.h);

            s.setColor(b.windowColor);
            for (int i = 0; i < b.windows.length; i += 2) {
                s.rect(b.x + b.windows[i], y + b.windows[i + 1], 0.1f, 0.16f);
            }
        }
    }

    private void addBuildings(Random rnd, float minW, float maxW, float minH, float maxH,
                              float parallax, Color body, Color edge, float windowAlpha) {
        for (int side = 0; side < 2; side++) {
            float y = 0f;
            while (y < TILE - 0.6f) {
                float w = minW + rnd.nextFloat() * (maxW - minW);
                float h = Math.min(minH + rnd.nextFloat() * (maxH - minH), TILE - y);
                float x = side == 0 ? 0f : W - w;
                buildings.add(new Building(rnd, x, y, w, h, side == 0, parallax, body, edge, windowAlpha));
                y += h + rnd.nextFloat() * 0.3f;
            }
        }
    }

    private void respawn(int i, boolean anywhere) {
        sx[i] = MathUtils.random(BOUND, W - BOUND);
        sy[i] = anywhere ? MathUtils.random(0f, H) : H + MathUtils.random(0f, 2f);
        sv[i] = MathUtils.random(8f, 16f);
    }

    private static class Building {
        final float x, y, w, h, parallax;
        final boolean left;
        final Color body, edge, windowColor;
        final float[] windows; // pares (x, y) relativos ao predio

        Building(Random rnd, float x, float y, float w, float h, boolean left,
                 float parallax, Color body, Color edge, float windowAlpha) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.left = left;
            this.parallax = parallax;
            this.body = body;
            this.edge = edge;

            Color base = new Color[] { CYAN, YELLOW, MAGENTA }[rnd.nextInt(3)];
            this.windowColor = new Color(base.r, base.g, base.b, windowAlpha);

            FloatArray win = new FloatArray();
            int cols = (int) ((w - 0.1f) / 0.22f);
            int rows = (int) ((h - 0.3f) / 0.45f);
            for (int c = 0; c < cols; c++) {
                for (int r = 0; r < rows; r++) {
                    if (rnd.nextFloat() < 0.18f) {
                        win.add(0.08f + c * 0.22f);
                        win.add(0.2f + r * 0.45f);
                    }
                }
            }
            this.windows = win.toArray();
        }
    }
}