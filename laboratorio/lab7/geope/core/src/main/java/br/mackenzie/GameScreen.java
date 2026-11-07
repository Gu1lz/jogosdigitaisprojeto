package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.Random;

public class GameScreen extends ScreenAdapter {

    private static final int ST_RUNNING = 0;
    private static final int ST_PAUSED  = 1;
    private static final int ST_DEAD    = 2;

    private static final float W = Theme.WORLD_W;
    private static final float H = Theme.WORLD_H;
    private static final float BOUND = CityScenery.BOUND;

    private static final float PLAYER_Y = 3.6f;
    private static final float PLAYER_SIZE = 1f;
    private static final float HIT_R = 0.34f;
    private static final float LATERAL_SPEED = 10f;
    private static final float HEAD_DRAW_SIZE = 1.3f;

    private static final float BASE_SPEED = 7f;
    private static final float MAX_SPEED = 13f;
    private static final float FIRST_ROW = 14f;
    private static final float GEN_AHEAD = 20f;
    private static final int POINTS_PER_METER = 10;

    private static final int MAX_PARTICLES = 220;

    private static final float PAUSE_CX = W / 2f;
    private static final float PAUSE_CY = H - 0.95f;
    private static final float PAUSE_SIZE = 0.8f;
    private static final float PAUSE_HIT = 0.7f;

    private final Main game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final Assets assets;
    private final FitViewport viewport = new FitViewport(W, H);
    private final CityScenery scenery = new CityScenery(2026L);
    private final Vector2 touch = new Vector2();
    private final Array<Obstacle> obstacles = new Array<>();
    private final Particle[] particles = new Particle[MAX_PARTICLES];
    private final OptionMenu pauseMenu = new OptionMenu(10.2f, 1.1f, 0.5f,
        "CONTINUAR", "REINICIAR", "MENU PRINCIPAL");
    private final OptionMenu deadMenu = new OptionMenu(7.8f, 0.9f, 0.45f,
        "TENTAR DE NOVO", "MENU PRINCIPAL");
    private final Boss boss = new Boss();
    private int pHead;

    private Random genRnd;
    private float genPos, genCenter, genLastPos, genPrevH;
    private boolean genFirst;

    private int state;
    private float stateTimer, scrollSpeed;
    private int attempt, score, bestAtStart;
    private boolean newRecord;
    private float playerX, velX, time, trailTimer, introTimer, hintTimer;

    public GameScreen(Main game) {
        this.game = game;
        this.batch = game.getBatch();
        this.shapes = game.getShapes();
        this.assets = game.getAssets();
        for (int i = 0; i < MAX_PARTICLES; i++) particles[i] = new Particle();
        resetRun();
    }

    private static float speedAt(float d) {
        float v = BASE_SPEED + d * 0.01f;
        if (v > MAX_SPEED) return MAX_SPEED;
        return v;
    }

    private static float gapAt(float d) {
        float v = 4.4f - d * 0.002f;
        if (v < 2.8f) return 2.8f;
        return v;
    }

    private static float spacingAt(float d) {
        float v = 9f - d * 0.004f;
        if (v < 6f) return 6f;
        return v;
    }

    private static float spikeChanceAt(float d) {
        if (d < 150f) return 0f;
        float v = 0.1f + (d - 150f) * 0.0006f;
        if (v > 0.5f) return 0.5f;
        return v;
    }

    private void resetRun() {
        attempt++;
        scenery.reset();
        scrollSpeed = BASE_SPEED;
        playerX = W / 2f;
        velX = 0f;
        trailTimer = 0f;
        introTimer = 2f;
        if (attempt == 1) {
            hintTimer = 6f;
        } else {
            hintTimer = 0f;
        }
        score = 0;
        newRecord = false;
        bestAtStart = game.getBestScore();
        for (Particle p : particles) p.life = 0f;

        obstacles.clear();
        boss.reset();

        genRnd = new Random();
        genPos = FIRST_ROW;
        genCenter = W / 2f;
        genLastPos = 0f;
        genPrevH = 0f;
        genFirst = true;
        generateAhead();

        setState(ST_RUNNING);
    }

    private void generateAhead() {
        float limit = scenery.getScroll() + GEN_AHEAD;
        while (genPos < limit) addRow();
    }

    private void addRow() {
        float pos = genPos;
        float gap = gapAt(pos);
        float lo = BOUND + gap / 2f + 0.05f;
        float hi = W - BOUND - gap / 2f - 0.05f;
        boolean spike;
        if (genRnd.nextFloat() < spikeChanceAt(pos)) {
            spike = true;
        } else {
            spike = false;
        }
        float rowH;
        if (spike) {
            rowH = 0.9f;
        } else {
            rowH = 0.8f;
        }

        if (genFirst) {
            genCenter = W / 2f + (genRnd.nextFloat() - 0.5f) * 2f;
        } else {
            float freeTime = Math.max(0.1f, (pos - genLastPos - genPrevH) / speedAt(pos));
            float maxShift = Math.max(0.4f, LATERAL_SPEED * freeTime * 0.8f - (gap - 2f * HIT_R));
            genCenter += (genRnd.nextFloat() * 2f - 1f) * maxShift;
        }
        genCenter = MathUtils.clamp(genCenter, lo, hi);

        float gapL = genCenter - gap / 2f;
        float gapR = genCenter + gap / 2f;
        if (gapL - BOUND > 0.05f) {
            obstacles.add(new Obstacle(BOUND, pos, gapL - BOUND, rowH, spike));
        }
        if ((W - BOUND) - gapR > 0.05f) {
            obstacles.add(new Obstacle(gapR, pos, (W - BOUND) - gapR, rowH, spike));
        }

        genLastPos = pos;
        genPrevH = rowH;
        genFirst = false;
        genPos = pos + spacingAt(pos) + genRnd.nextFloat() * 1.2f;
    }

    private void cullObstacles() {
        float limit = scenery.getScroll() - 4f;
        while (obstacles.size > 0 && obstacles.first().pos + obstacles.first().h < limit) {
            obstacles.removeIndex(0);
        }
    }

    private void setState(int s) {
        state = s;
        stateTimer = 0f;
        if (s == ST_PAUSED) pauseMenu.reset();
        if (s == ST_DEAD) deadMenu.reset();
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1f / 30f);
        if (handleInput()) return;

        stateTimer += delta;
        if (state != ST_PAUSED) {
            time += delta;
            if (state == ST_RUNNING) update(delta);
            updateParticles(delta);
        }

        ScreenUtils.clear(Theme.VOID);
        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        batch.setProjectionMatrix(viewport.getCamera().combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        scenery.draw(shapes);
        for (Obstacle o : obstacles) drawObstacle(o);
        drawParticles();
        if (state != ST_DEAD) HeadRenderer.drawGlow(shapes, playerX, PLAYER_Y + bob(), PLAYER_SIZE);
        shapes.end();

        batch.begin();
        drawPlayer();
        boss.draw(batch, assets, W);
        batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        scenery.drawRain(shapes);
        if (boss.state() != Boss.ST_HIDDEN && boss.currentBossIndex() >= 0) {
            String nome = boss.currentBossName(assets);
            if (nome.length() > 0) {
                center(nome, 0.4f, Theme.RED, 1f, H - 1.6f);
            }
        }
        if (state == ST_RUNNING) drawPauseButton();
        if (state == ST_PAUSED) {
            shapes.setColor(0f, 0f, 0.03f, 0.65f);
            shapes.rect(0, 0, W, H);
        }
        shapes.end();

        batch.begin();
        drawHud();
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    private boolean handleInput() {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.P)) {
            if (state == ST_RUNNING) {
                setState(ST_PAUSED);
            } else if (state == ST_PAUSED) {
                setState(ST_RUNNING);
            }
        }
        if (state == ST_RUNNING && Gdx.input.justTouched()) {
            viewport.unproject(touch.set(Gdx.input.getX(), Gdx.input.getY()));
            if (inPauseZone(touch.x, touch.y)) setState(ST_PAUSED);
        }
        if (Gdx.input.isKeyJustPressed(Keys.R)) {
            resetRun();
            return false;
        }
        if (state != ST_RUNNING && Gdx.input.isKeyJustPressed(Keys.M)) {
            game.setScreen(new MenuScreen(game));
            return true;
        }

        int choice = -1;
        if (state == ST_PAUSED) {
            choice = pauseMenu.update(viewport);
        } else if (state == ST_DEAD && stateTimer > 0.4f) {
            choice = deadMenu.update(viewport);
        }
        if (choice < 0) return false;

        boolean toMenu;
        if (state == ST_PAUSED) {
            toMenu = choice == 2;
        } else {
            toMenu = choice == 1;
        }
        if (toMenu) {
            game.setScreen(new MenuScreen(game));
            return true;
        }
        if (state == ST_PAUSED && choice == 0) {
            setState(ST_RUNNING);
        } else {
            resetRun();
        }
        return false;
    }

    private static boolean inPauseZone(float x, float y) {
        return Math.abs(x - PAUSE_CX) <= PAUSE_HIT && Math.abs(y - PAUSE_CY) <= PAUSE_HIT;
    }

    private void update(float delta) {
        hintTimer = Math.max(0f, hintTimer - delta);
        introTimer = Math.max(0f, introTimer - delta);

        float dir = 0f;
        if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A)) dir -= 1f;
        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) dir += 1f;
        if (Gdx.input.isTouched()) {
            viewport.unproject(touch.set(Gdx.input.getX(), Gdx.input.getY()));
            if (!inPauseZone(touch.x, touch.y)) {
                if (touch.x < W / 2f) dir -= 1f;
                else dir += 1f;
            }
        }
        dir = MathUtils.clamp(dir, -1f, 1f);

        velX += (dir * LATERAL_SPEED - velX) * Math.min(1f, 16f * delta);
        playerX += velX * delta;

        float minX = BOUND + HIT_R;
        float maxX = W - BOUND - HIT_R;
        if (playerX < minX || playerX > maxX) {
            playerX = MathUtils.clamp(playerX, minX, maxX);
            velX = 0f;
        }

        scrollSpeed = speedAt(scenery.getScroll());
        if (boss.controlsSpawn()) scrollSpeed += boss.speedBonus();
        scenery.advance(delta, scrollSpeed);
        score = (int) (scenery.getScroll() * POINTS_PER_METER);

        boss.update(delta, score, scenery.getScroll());
        if (boss.controlsSpawn()) {
            boss.generateUntil(scenery.getScroll() + GEN_AHEAD, obstacles);
        } else {
            generateAhead();
        }

        cullObstacles();
        emitTrail(delta);

        if (hitsObstacle()) die();
    }

    private boolean hitsObstacle() {
        float scroll = scenery.getScroll();
        for (Obstacle o : obstacles) {
            float y0 = PLAYER_Y + (o.pos - scroll);
            if (y0 > PLAYER_Y + 2f || y0 + o.h < PLAYER_Y - 2f) continue;

            float rx = o.x, ry = y0, rw = o.w, rh = o.h;
            if (o.spike) {
                rx += 0.05f;
                rw -= 0.1f;
                ry += rh * 0.3f;
                rh *= 0.7f;
            }
            if (circleHitsRect(playerX, PLAYER_Y, HIT_R, rx, ry, rw, rh)) return true;
        }
        return false;
    }

    private static boolean circleHitsRect(float cx, float cy, float r,
                                          float rx, float ry, float rw, float rh) {
        float dx = cx - MathUtils.clamp(cx, rx, rx + rw);
        float dy = cy - MathUtils.clamp(cy, ry, ry + rh);
        return dx * dx + dy * dy <= r * r;
    }

    private void die() {
        setState(ST_DEAD);
        velX = 0f;

        if (score > game.getBestScore()) {
            game.setBestScore(score);
            newRecord = true;
        }

        for (int i = 0; i < 40; i++) {
            float a = MathUtils.random(360f);
            float sp = MathUtils.random(2f, 7f);
            Color c;
            if (i % 3 == 0) {
                c = Theme.CYAN;
            } else if (i % 3 == 1) {
                c = Theme.MAGENTA;
            } else {
                c = Theme.YELLOW;
            }
            emit(playerX, PLAYER_Y, MathUtils.cosDeg(a) * sp, MathUtils.sinDeg(a) * sp,
                MathUtils.random(0.6f, 1.2f), MathUtils.random(0.07f, 0.17f), c);
        }
    }

    private void emitTrail(float delta) {
        trailTimer -= delta;
        while (trailTimer <= 0f) {
            trailTimer += 0.035f;
            Color c;
            if (MathUtils.randomBoolean()) {
                c = Theme.CYAN;
            } else {
                c = Theme.MAGENTA;
            }
            emit(playerX + MathUtils.random(-0.12f, 0.12f), PLAYER_Y - 0.4f,
                MathUtils.random(-0.3f, 0.3f), -scrollSpeed * 0.5f - MathUtils.random(0f, 1.5f),
                0.45f, 0.09f, c);
        }
    }

    private void emit(float x, float y, float vx, float vy, float life, float size, Color c) {
        Particle p = particles[pHead];
        pHead = (pHead + 1) % MAX_PARTICLES;
        p.x = x;
        p.y = y;
        p.vx = vx;
        p.vy = vy;
        p.life = p.max = life;
        p.size = size;
        p.r = c.r;
        p.g = c.g;
        p.b = c.b;
    }

    private void updateParticles(float delta) {
        for (Particle p : particles) {
            if (p.life <= 0f) continue;
            p.life -= delta;
            p.x += p.vx * delta;
            p.y += p.vy * delta;
        }
    }

    private void drawParticles() {
        for (Particle p : particles) {
            if (p.life <= 0f) continue;
            float a = p.life / p.max;
            shapes.setColor(p.r, p.g, p.b, a);
            shapes.circle(p.x, p.y, p.size * (0.5f + 0.5f * a), 8);
        }
    }

    private float bob() {
        if (state == ST_DEAD) return 0f;
        return MathUtils.sin(time * 5f) * 0.05f;
    }

    private void drawPlayer() {
        boolean dead = state == ST_DEAD;
        TextureRegion frame;
        if (dead) {
            frame = assets.hit.getKeyFrame(stateTimer);
        } else {
            frame = assets.neutral.getKeyFrame(time);
        }
        float tilt;
        if (dead) {
            tilt = 0f;
        } else {
            tilt = -velX * 2.2f;
        }
        HeadRenderer.drawSprite(batch, frame, playerX, PLAYER_Y + bob(), HEAD_DRAW_SIZE, tilt);
    }

    private void drawObstacle(Obstacle o) {
        float y0 = PLAYER_Y + (o.pos - scenery.getScroll());
        if (y0 > H + 0.3f || y0 + o.h < -0.3f) return;
        if (o.spike) drawSpikes(o, y0);
        else drawBlock(o, y0);
    }

    private void drawBlock(Obstacle o, float y0) {
        Color c = Theme.MAGENTA;
        shapes.setColor(c.r, c.g, c.b, 0.16f);
        shapes.rect(o.x - 0.12f, y0 - 0.12f, o.w + 0.24f, o.h + 0.24f);
        shapes.setColor(Theme.BLOCK_BODY);
        shapes.rect(o.x, y0, o.w, o.h);

        shapes.setColor(c.r, c.g, c.b, 0.5f);
        for (float gx = o.x + 1f; gx < o.x + o.w - 0.05f; gx += 1f) {
            shapes.rect(gx - 0.015f, y0, 0.03f, o.h);
        }
        shapes.setColor(c);
        shapes.rect(o.x, y0, o.w, 0.06f);
        shapes.rect(o.x, y0 + o.h - 0.06f, o.w, 0.06f);
        shapes.rect(o.x, y0, 0.06f, o.h);
        shapes.rect(o.x + o.w - 0.06f, y0, 0.06f, o.h);
    }

    private void drawSpikes(Obstacle o, float y0) {
        Color c = Theme.RED;
        shapes.setColor(c.r, c.g, c.b, 0.12f);
        shapes.rect(o.x - 0.1f, y0 - 0.1f, o.w + 0.2f, o.h + 0.2f);

        int n = Math.max(1, Math.round(o.w / 0.6f));
        float tw = o.w / n;
        float top = y0 + o.h;
        for (int i = 0; i < n; i++) {
            float x1 = o.x + i * tw;
            float x2 = x1 + tw;
            float xm = (x1 + x2) / 2f;
            shapes.setColor(Theme.BLOCK_BODY);
            shapes.triangle(x1, top, x2, top, xm, y0);
            shapes.setColor(c);
            shapes.rectLine(x1, top, xm, y0, 0.05f);
            shapes.rectLine(xm, y0, x2, top, 0.05f);
        }
        shapes.setColor(c);
        shapes.rect(o.x, top - 0.08f, o.w, 0.08f);
    }

    private void drawPauseButton() {
        float s = PAUSE_SIZE;
        shapes.setColor(Theme.WHITE.r, Theme.WHITE.g, Theme.WHITE.b, 0.10f);
        shapes.rect(PAUSE_CX - s / 2f, PAUSE_CY - s / 2f, s, s);
        shapes.setColor(Theme.CYAN.r, Theme.CYAN.g, Theme.CYAN.b, 0.8f);
        shapes.rect(PAUSE_CX - s * 0.22f, PAUSE_CY - s * 0.28f, s * 0.14f, s * 0.56f);
        shapes.rect(PAUSE_CX + s * 0.08f, PAUSE_CY - s * 0.28f, s * 0.14f, s * 0.56f);
    }

    private void center(String text, float size, Color color, float alpha, float y) {
        game.drawText(text, size, color, alpha, 0f, y, W, Align.center);
    }

    private void drawHud() {
        boolean beating = bestAtStart > 0 && score > bestAtStart;

        Color scoreColor;
        if (beating) {
            scoreColor = Theme.YELLOW;
        } else {
            scoreColor = Theme.WHITE;
        }

        game.drawText(String.valueOf(score), 0.75f, scoreColor, 1f,
            0f, H - 0.3f, W - 0.4f, Align.right);
        game.drawText("PONTOS", 0.28f, Theme.WHITE, 0.7f, 0f, H - 1.2f, W - 0.4f, Align.right);

        game.drawText("RECORDE", 0.28f, Theme.WHITE, 0.7f, 0.4f, H - 0.3f, 4f, Align.left);
        game.drawText(String.valueOf(Math.max(bestAtStart, score)), 0.5f, Theme.CYAN, 1f,
            0.4f, H - 0.7f, 4f, Align.left);

        if (state == ST_RUNNING) {
            if (introTimer > 0f) {
                center("TENTATIVA " + attempt, 1.0f, Theme.MAGENTA, MathUtils.clamp(introTimer, 0f, 1f), H * 0.62f);
            }
            if (hintTimer > 0f) {
                float a = MathUtils.clamp(hintTimer, 0f, 1f) * 0.8f;
                center("SETAS / A D / TOQUE NAS LATERAIS: mover", 0.32f, Theme.WHITE, a, 1.6f);
                center("P ou botao do topo: pausar     R: reiniciar", 0.32f, Theme.WHITE, a, 1.1f);
            }
        } else if (state == ST_PAUSED) {
            center("PAUSADO", 1.0f, Theme.MAGENTA, 1f, 11.8f);
            pauseMenu.draw(game, stateTimer);
            center("SETAS + ENTER, ou toque     P / ESC: continuar", 0.3f, Theme.WHITE, 0.6f, 6.4f);
        } else {
            center("VOCE BATEU!", 0.9f, Theme.RED, 1f, 10.8f);
            center("PONTOS: " + score, 0.5f, Theme.WHITE, 1f, 9.7f);
            if (newRecord) {
                center("NOVO RECORDE!", 0.5f, Theme.YELLOW, 1f, 9.0f);
            } else {
                center("RECORDE: " + game.getBestScore(), 0.4f, Theme.CYAN, 1f, 9.0f);
            }
            if (stateTimer > 0.4f) deadMenu.draw(game, stateTimer);
        }
    }

    private static final class Particle {
        float x, y, vx, vy, life, max, size, r, g, b;
    }
}