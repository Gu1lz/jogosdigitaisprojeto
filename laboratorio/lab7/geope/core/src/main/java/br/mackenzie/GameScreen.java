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

    private enum State { RUNNING, PAUSED, DEAD }

    private static final float W = Theme.WORLD_W;
    private static final float H = Theme.WORLD_H;
    private static final float BOUND = CityScenery.BOUND;

    private static final float PLAYER_Y = 3.6f;
    private static final float PLAYER_SIZE = 1f;       // tamanho do halo
    private static final float HIT_R = 0.34f;          // raio da hitbox
    private static final float LATERAL_SPEED = 10f;
    private static final float HEAD_DRAW_SIZE = 1.3f;  // tamanho do sprite na tela

    private static final float BASE_SPEED = 7f;
    private static final float MAX_SPEED = 13f;
    private static final float FIRST_ROW = 14f;
    private static final float GEN_AHEAD = 20f;        // metros gerados a frente
    private static final int POINTS_PER_METER = 10;

    private static final int MAX_PARTICLES = 220;

    // botao de pausa (toque), no topo central
    private static final float PAUSE_CX = W / 2f;
    private static final float PAUSE_CY = H - 0.95f;
    private static final float PAUSE_SIZE = 0.8f;
    private static final float PAUSE_HIT = 0.7f; // meia-largura da area de toque

    private final Main game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final Assets assets;
    private final FitViewport viewport = new FitViewport(W, H);
    private final CityScenery scenery = new CityScenery(2026L);
    private final Vector2 touch = new Vector2();
    private final Array<Obstacle> obstacles = new Array<>();
    private final OptionMenu pauseMenu = new OptionMenu(10.2f, 1.1f, 0.5f,
        "CONTINUAR", "REINICIAR", "MENU PRINCIPAL");
    private final OptionMenu deadMenu = new OptionMenu(7.8f, 0.9f, 0.45f,
        "TENTAR DE NOVO", "MENU PRINCIPAL");

    private final float[] px = new float[MAX_PARTICLES];
    private final float[] py = new float[MAX_PARTICLES];
    private final float[] pvx = new float[MAX_PARTICLES];
    private final float[] pvy = new float[MAX_PARTICLES];
    private final float[] plife = new float[MAX_PARTICLES];
    private final float[] pmax = new float[MAX_PARTICLES];
    private final float[] psize = new float[MAX_PARTICLES];
    private final float[] pr = new float[MAX_PARTICLES];
    private final float[] pg = new float[MAX_PARTICLES];
    private final float[] pb = new float[MAX_PARTICLES];
    private int pHead;

    // gerador de obstaculos
    private Random genRnd;
    private float genPos;
    private float genCenter;
    private float genLastPos;
    private float genPrevH;
    private boolean genFirst;

    private State state;
    private float stateTimer;
    private float scrollSpeed;

    private int attempt;
    private int score;
    private int bestAtStart;
    private boolean newRecord;

    private float playerX;
    private float velX;
    private float time;
    private float trailTimer;
    private float introTimer;
    private float hintTimer;

    public GameScreen(Main game) {
        this.game = game;
        this.batch = game.getBatch();
        this.shapes = game.getShapes();
        this.assets = game.getAssets();
        resetRun();
    }

    // ---------------------------------------------------------------- dificuldade (em funcao da distancia, em metros)

    private static float speedAt(float d) {
        return Math.min(MAX_SPEED, BASE_SPEED + d * 0.01f);
    }

    private static float gapAt(float d) {
        return Math.max(2.8f, 4.4f - d * 0.002f);
    }

    private static float spacingAt(float d) {
        return Math.max(6f, 9f - d * 0.004f);
    }

    private static float spikeChanceAt(float d) {
        return d < 150f ? 0f : Math.min(0.5f, 0.1f + (d - 150f) * 0.0006f);
    }

    // ---------------------------------------------------------------- partida

    private void resetRun() {
        attempt++;
        scenery.reset();
        scrollSpeed = BASE_SPEED;
        playerX = W / 2f;
        velX = 0f;
        trailTimer = 0f;
        introTimer = 2f;
        hintTimer = attempt == 1 ? 6f : 0f;
        score = 0;
        newRecord = false;
        bestAtStart = game.getBestScore();
        for (int i = 0; i < MAX_PARTICLES; i++) plife[i] = 0f;

        obstacles.clear();
        genRnd = new Random();
        genPos = FIRST_ROW;
        genCenter = W / 2f;
        genLastPos = 0f;
        genPrevH = 0f;
        genFirst = true;
        generateAhead();

        setState(State.RUNNING);
    }

    private void generateAhead() {
        float limit = scenery.getScroll() + GEN_AHEAD;
        while (genPos < limit) addRow();
    }

    // Parede com uma abertura. O deslocamento da abertura respeita o tempo de reacao
    // na velocidade daquele ponto, entao nunca gera trecho impossivel.
    private void addRow() {
        float pos = genPos;
        float gap = gapAt(pos);
        float lo = BOUND + gap / 2f + 0.05f;
        float hi = W - BOUND - gap / 2f - 0.05f;
        boolean spike = genRnd.nextFloat() < spikeChanceAt(pos);
        float rowH = spike ? 0.9f : 0.8f;

        if (genFirst) {
            genCenter = MathUtils.clamp(W / 2f + (genRnd.nextFloat() - 0.5f) * 2f, lo, hi);
        } else {
            float freeTime = Math.max(0.1f, (pos - genLastPos - genPrevH) / speedAt(pos));
            float maxShift = Math.max(0.4f, LATERAL_SPEED * freeTime * 0.8f - (gap - 2f * HIT_R));
            genCenter += (genRnd.nextFloat() * 2f - 1f) * maxShift;
            genCenter = MathUtils.clamp(genCenter, lo, hi);
        }

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

    private void setState(State s) {
        state = s;
        stateTimer = 0f;
        if (s == State.PAUSED) pauseMenu.reset();
        if (s == State.DEAD) deadMenu.reset();
    }

    // ---------------------------------------------------------------- loop

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1f / 30f);

        if (handleInput()) return;

        stateTimer += delta;
        if (state != State.PAUSED) time += delta;
        if (state == State.RUNNING) update(delta);
        if (state != State.PAUSED) updateParticles(delta);

        ScreenUtils.clear(Theme.VOID);
        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        batch.setProjectionMatrix(viewport.getCamera().combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        scenery.draw(shapes);
        for (int i = 0; i < obstacles.size; i++) drawObstacle(obstacles.get(i));
        drawParticles();
        if (state != State.DEAD) HeadRenderer.drawGlow(shapes, playerX, PLAYER_Y + bob(), PLAYER_SIZE);
        shapes.end();

        batch.begin();
        drawPlayer();
        batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        scenery.drawRain(shapes);
        if (state == State.RUNNING) drawPauseButton();
        if (state == State.PAUSED) {
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

    // ---------------------------------------------------------------- entrada

    private boolean handleInput() {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.P)) {
            if (state == State.RUNNING) setState(State.PAUSED);
            else if (state == State.PAUSED) setState(State.RUNNING);
        }
        if (state == State.RUNNING && Gdx.input.justTouched()) {
            viewport.unproject(touch.set(Gdx.input.getX(), Gdx.input.getY()));
            if (inPauseZone(touch.x, touch.y)) setState(State.PAUSED);
        }
        if (Gdx.input.isKeyJustPressed(Keys.R)) {
            resetRun();
            return false;
        }
        if (state != State.RUNNING && Gdx.input.isKeyJustPressed(Keys.M)) {
            game.setScreen(new MenuScreen(game));
            return true;
        }

        int choice = -1;
        if (state == State.PAUSED) choice = pauseMenu.update(viewport);
        else if (state == State.DEAD && stateTimer > 0.4f) choice = deadMenu.update(viewport);
        if (choice < 0) return false;

        if (state == State.PAUSED) {
            if (choice == 0) {
                setState(State.RUNNING);
            } else if (choice == 1) {
                resetRun();
            } else {
                game.setScreen(new MenuScreen(game));
                return true;
            }
        } else { // DEAD
            if (choice == 0) {
                resetRun();
            } else {
                game.setScreen(new MenuScreen(game));
                return true;
            }
        }
        return false;
    }

    private static boolean inPauseZone(float x, float y) {
        return Math.abs(x - PAUSE_CX) <= PAUSE_HIT && Math.abs(y - PAUSE_CY) <= PAUSE_HIT;
    }

    // ---------------------------------------------------------------- logica

    private void update(float delta) {
        hintTimer = Math.max(0f, hintTimer - delta);
        introTimer = Math.max(0f, introTimer - delta);

        float dir = 0f;
        if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A)) dir -= 1f;
        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) dir += 1f;
        if (Gdx.input.isTouched()) {
            viewport.unproject(touch.set(Gdx.input.getX(), Gdx.input.getY()));
            if (!inPauseZone(touch.x, touch.y)) dir += touch.x < W / 2f ? -1f : 1f;
        }
        dir = MathUtils.clamp(dir, -1f, 1f);

        velX += (dir * LATERAL_SPEED - velX) * Math.min(1f, 16f * delta);
        playerX += velX * delta;

        float minX = BOUND + HIT_R;
        float maxX = W - BOUND - HIT_R;
        if (playerX < minX) { playerX = minX; velX = 0f; }
        if (playerX > maxX) { playerX = maxX; velX = 0f; }

        scrollSpeed = speedAt(scenery.getScroll());
        scenery.advance(delta, scrollSpeed);
        score = (int) (scenery.getScroll() * POINTS_PER_METER);

        generateAhead();
        cullObstacles();
        emitTrail(delta);

        if (hitsObstacle()) die();
    }

    private boolean hitsObstacle() {
        float scroll = scenery.getScroll();
        for (int i = 0; i < obstacles.size; i++) {
            Obstacle o = obstacles.get(i);
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
        float nx = MathUtils.clamp(cx, rx, rx + rw);
        float ny = MathUtils.clamp(cy, ry, ry + rh);
        float dx = cx - nx;
        float dy = cy - ny;
        return dx * dx + dy * dy <= r * r;
    }

    private void die() {
        setState(State.DEAD);
        velX = 0f;

        if (score > game.getBestScore()) {
            game.setBestScore(score);
            newRecord = true;
        }

        for (int i = 0; i < 40; i++) {
            float a = MathUtils.random(360f);
            float sp = MathUtils.random(2f, 7f);
            Color c = i % 3 == 0 ? Theme.CYAN : i % 3 == 1 ? Theme.MAGENTA : Theme.YELLOW;
            emit(playerX, PLAYER_Y, MathUtils.cosDeg(a) * sp, MathUtils.sinDeg(a) * sp,
                MathUtils.random(0.6f, 1.2f), MathUtils.random(0.07f, 0.17f), c);
        }
    }

    // ---------------------------------------------------------------- particulas

    private void emitTrail(float delta) {
        trailTimer -= delta;
        while (trailTimer <= 0f) {
            trailTimer += 0.035f;
            Color c = MathUtils.randomBoolean() ? Theme.CYAN : Theme.MAGENTA;
            emit(playerX + MathUtils.random(-0.12f, 0.12f), PLAYER_Y - 0.4f,
                MathUtils.random(-0.3f, 0.3f), -scrollSpeed * 0.5f - MathUtils.random(0f, 1.5f),
                0.45f, 0.09f, c);
        }
    }

    private void emit(float x, float y, float vx, float vy, float life, float size, Color c) {
        int i = pHead;
        pHead = (pHead + 1) % MAX_PARTICLES;
        px[i] = x;
        py[i] = y;
        pvx[i] = vx;
        pvy[i] = vy;
        plife[i] = life;
        pmax[i] = life;
        psize[i] = size;
        pr[i] = c.r;
        pg[i] = c.g;
        pb[i] = c.b;
    }

    private void updateParticles(float delta) {
        for (int i = 0; i < MAX_PARTICLES; i++) {
            if (plife[i] <= 0f) continue;
            plife[i] -= delta;
            px[i] += pvx[i] * delta;
            py[i] += pvy[i] * delta;
        }
    }

    private void drawParticles() {
        for (int i = 0; i < MAX_PARTICLES; i++) {
            if (plife[i] <= 0f) continue;
            float a = plife[i] / pmax[i];
            shapes.setColor(pr[i], pg[i], pb[i], a);
            shapes.circle(px[i], py[i], psize[i] * (0.5f + 0.5f * a), 8);
        }
    }

    // ---------------------------------------------------------------- desenho

    private float bob() {
        return state == State.DEAD ? 0f : MathUtils.sin(time * 5f) * 0.05f;
    }

    private void drawPlayer() {
        boolean dead = state == State.DEAD;
        TextureRegion frame = dead
            ? assets.hit.getKeyFrame(stateTimer)
            : assets.neutral.getKeyFrame(time);
        float tilt = dead ? 0f : -velX * 2.2f;
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

    private void drawHud() {
        boolean beating = bestAtStart > 0 && score > bestAtStart;
        Color scoreColor = beating ? Theme.YELLOW : Theme.WHITE;

        // canto superior direito: pontos
        game.drawText(String.valueOf(score), 0.75f, scoreColor, 1f, 0f, H - 0.3f, W - 0.4f, Align.right);
        game.drawText("PONTOS", 0.28f, Theme.WHITE, 0.7f, 0f, H - 1.2f, W - 0.4f, Align.right);

        // canto superior esquerdo: recorde
        game.drawText("RECORDE", 0.28f, Theme.WHITE, 0.7f, 0.4f, H - 0.3f, 4f, Align.left);
        game.drawText(String.valueOf(Math.max(bestAtStart, score)), 0.5f, Theme.CYAN, 1f,
            0.4f, H - 0.7f, 4f, Align.left);

        if (state == State.RUNNING && introTimer > 0f) {
            float a = MathUtils.clamp(introTimer, 0f, 1f);
            game.drawText("TENTATIVA " + attempt, 1.0f, Theme.MAGENTA, a, 0f, H * 0.62f, W, Align.center);
        }

        if (state == State.RUNNING && hintTimer > 0f) {
            float a = MathUtils.clamp(hintTimer, 0f, 1f) * 0.8f;
            game.drawText("SETAS / A D / TOQUE NAS LATERAIS: mover", 0.32f, Theme.WHITE, a,
                0f, 1.6f, W, Align.center);
            game.drawText("P ou botao do topo: pausar     R: reiniciar", 0.32f, Theme.WHITE, a,
                0f, 1.1f, W, Align.center);
        }

        if (state == State.PAUSED) {
            game.drawText("PAUSADO", 1.0f, Theme.MAGENTA, 1f, 0f, 11.8f, W, Align.center);
            pauseMenu.draw(game, stateTimer);
            game.drawText("SETAS + ENTER, ou toque     P / ESC: continuar", 0.3f, Theme.WHITE, 0.6f,
                0f, 6.4f, W, Align.center);
        }

        if (state == State.DEAD) {
            game.drawText("VOCE BATEU!", 0.9f, Theme.RED, 1f, 0f, 10.8f, W, Align.center);
            game.drawText("PONTOS: " + score, 0.5f, Theme.WHITE, 1f, 0f, 9.7f, W, Align.center);
            if (newRecord) {
                game.drawText("NOVO RECORDE!", 0.5f, Theme.YELLOW, 1f, 0f, 9.0f, W, Align.center);
            } else {
                game.drawText("RECORDE: " + game.getBestScore(), 0.4f, Theme.CYAN, 1f, 0f, 9.0f, W, Align.center);
            }
            if (stateTimer > 0.4f) {
                deadMenu.draw(game, stateTimer);
            }
        }
    }

    // ---------------------------------------------------------------- tipos

    private static final class Obstacle {
        final float x, pos, w, h;
        final boolean spike;

        Obstacle(float x, float pos, float w, float h, boolean spike) {
            this.x = x;
            this.pos = pos;
            this.w = w;
            this.h = h;
            this.spike = spike;
        }
    }
}