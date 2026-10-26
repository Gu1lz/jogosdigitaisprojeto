package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameScreen extends ScreenAdapter {

    private enum State { RUNNING, PAUSED }

    private static final float W = Theme.WORLD_W;
    private static final float H = Theme.WORLD_H;

    private static final float GROUND_Y = CityScenery.GROUND_TOP;
    private static final float PLAYER_SIZE = 1f;
    private static final float PLAYER_X = 3.5f;

    private static final float GRAVITY = -38f;
    private static final float JUMP_VELOCITY = 12f;
    private static final float SPIN_SPEED = 380f;       

    private static final float LEVEL_LENGTH = 250f;     
    private static final int TOTAL_LEVELS = 6;

    private final Main game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final FitViewport viewport = new FitViewport(W, H);
    private final CityScenery scenery = new CityScenery(2026L);

    private State state;
    private int level = 1;

    private float scrollSpeed = 7f;

    private float playerY;
    private float velY;
    private float rotation;
    private boolean grounded;
    private float hintTimer;

    public GameScreen(Main game) {
        this.game = game;
        this.batch = game.getBatch();
        this.shapes = game.getShapes();
        resetRun();
    }

    private void resetRun() {
        scenery.reset();
        playerY = GROUND_Y;
        velY = 0f;
        rotation = 0f;
        grounded = true;
        hintTimer = 6f;
        state = State.RUNNING;
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1f / 30f); 

        if (handleInput()) return;
        if (state == State.RUNNING) update(delta);

        ScreenUtils.clear(Theme.VOID);
        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        batch.setProjectionMatrix(viewport.getCamera().combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        scenery.draw(shapes);
        scenery.drawRain(shapes);
        drawPlayer();
        drawProgressBar();
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

    private boolean handleInput() {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.P)) {
            state = state == State.RUNNING ? State.PAUSED : State.RUNNING;
        }
        if (Gdx.input.isKeyJustPressed(Keys.R)) {
            resetRun();
        }
        if (state == State.PAUSED && Gdx.input.isKeyJustPressed(Keys.M)) {
            game.setScreen(new MenuScreen(game));
            return true;
        }
        return false;
    }

    private boolean jumpRequested() {
        return Gdx.input.isKeyPressed(Keys.SPACE)
            || Gdx.input.isKeyPressed(Keys.UP)
            || Gdx.input.isKeyPressed(Keys.W)
            || Gdx.input.justTouched();
    }

    private void update(float delta) {
        scenery.advance(delta, scrollSpeed);
        hintTimer = Math.max(0f, hintTimer - delta);

        if (grounded && jumpRequested()) {
            velY = JUMP_VELOCITY;
            grounded = false;
        }

        if (!grounded) {
            velY += GRAVITY * delta;
            playerY += velY * delta;
            rotation -= SPIN_SPEED * delta; 

            if (playerY <= GROUND_Y) {
                playerY = GROUND_Y;
                velY = 0f;
                grounded = true;
            }
        } else {
            if (Math.abs(rotation) >= 360f) rotation %= 360f;
            float target = Math.round(rotation / 90f) * 90f;
            rotation += (target - rotation) * Math.min(1f, 25f * delta);
        }
    }

    private void drawPlayer() {
        float s = PLAYER_SIZE;
        float cx = PLAYER_X + s / 2f;
        float cy = playerY + s / 2f;

        rotatedRect(cx, cy, 0f, 0f, s * 1.45f, s * 1.45f, Theme.CYAN, 0.14f);          // brilho externo
        rotatedRect(cx, cy, 0f, 0f, s * 1.2f, s * 1.2f, Theme.CYAN, 0.20f);            // brilho interno
        rotatedRect(cx, cy, 0f, 0f, s, s, Theme.CYAN, 1f);                             // borda neon
        rotatedRect(cx, cy, 0f, 0f, s * 0.74f, s * 0.74f, Theme.PLAYER_CORE, 1f);      // miolo escuro
        rotatedRect(cx, cy, 0.06f * s, 0.04f * s, s * 0.5f, s * 0.12f, Theme.MAGENTA, 1f); // visor
        rotatedRect(cx, cy, -0.22f * s, 0.22f * s, s * 0.14f, s * 0.14f, Theme.YELLOW, 1f); // "sensor" na cabeça
    }

    private void rotatedRect(float cx, float cy, float ox, float oy,
                             float w, float h, Color c, float alpha) {
        shapes.setColor(c.r, c.g, c.b, alpha);
        shapes.rect(cx + ox - w / 2f, cy + oy - h / 2f,
            w / 2f - ox, h / 2f - oy,   // origem da rotação relativa ao canto do retângulo
            w, h, 1f, 1f, rotation);
    }

    private void drawProgressBar() {
        float barW = 8f;
        float x = (W - barW) / 2f;
        float y = H - 0.55f;
        float progress = Math.min(scenery.getScroll() / LEVEL_LENGTH, 1f);

        shapes.setColor(1f, 1f, 1f, 0.15f);
        shapes.rect(x, y, barW, 0.14f);
        shapes.setColor(Theme.CYAN);
        shapes.rect(x, y, barW * progress, 0.14f);
    }

    private void drawHud() {
        float top = H - 0.3f;

        game.drawText("FASE " + level + "/" + TOTAL_LEVELS, 0.42f, Theme.CYAN, 1f,
            0.4f, top, 4f, Align.left);
        game.drawText("DISTÂNCIA " + (int) scenery.getScroll() + " m", 0.42f, Theme.WHITE, 1f,
            0f, top, W - 0.4f, Align.right);

        if (hintTimer > 0f && state == State.RUNNING) {
            float alpha = MathUtils.clamp(hintTimer, 0f, 1f);
            game.drawText("ESPAÇO / SETA PARA CIMA / CLIQUE: pular     P: pausar     R: reiniciar",
                0.34f, Theme.WHITE, alpha * 0.8f, 0f, 0.9f, W, Align.center);
        }

        if (state == State.PAUSED) {
            game.drawText("PAUSADO", 1.1f, Theme.MAGENTA, 1f, 0f, 6.6f, W, Align.center);
            game.drawText("ESC / P   continuar", 0.4f, Theme.WHITE, 1f, 0f, 4.9f, W, Align.center);
            game.drawText("R   reiniciar", 0.4f, Theme.WHITE, 1f, 0f, 4.3f, W, Align.center);
            game.drawText("M   menu principal", 0.4f, Theme.WHITE, 1f, 0f, 3.7f, W, Align.center);
        }
    }
}