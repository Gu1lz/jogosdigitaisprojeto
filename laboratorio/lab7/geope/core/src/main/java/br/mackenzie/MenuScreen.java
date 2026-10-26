package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MenuScreen extends ScreenAdapter {

    private static final float W = Theme.WORLD_W;
    private static final float H = Theme.WORLD_H;

    private final Main game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final Assets assets;
    private final FitViewport viewport = new FitViewport(W, H);
    private final CityScenery scenery = new CityScenery(777L);

    private float time;

    public MenuScreen(Main game) {
        this.game = game;
        this.batch = game.getBatch();
        this.shapes = game.getShapes();
        this.assets = game.getAssets();
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1f / 30f);
        time += delta;
        scenery.advance(delta, 5f);

        boolean start = Gdx.input.isKeyJustPressed(Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Keys.SPACE)
            || Gdx.input.justTouched();
        if (start) {
            game.setScreen(new GameScreen(game));
            return;
        }
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }

        ScreenUtils.clear(Theme.VOID);
        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        batch.setProjectionMatrix(viewport.getCamera().combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        float hy = H * 0.45f + MathUtils.sin(time * 2f) * 0.25f;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        scenery.draw(shapes);
        scenery.drawRain(shapes);
        HeadRenderer.drawGlow(shapes, W / 2f, hy, 2.4f);
        shapes.end();

        batch.begin();
        HeadRenderer.drawSprite(batch, assets.neutral.getKeyFrame(time), W / 2f, hy, 3.0f,
            MathUtils.sin(time * 1.5f) * 8f);

        game.drawText("HEAD DASH", 1.3f, Theme.CYAN, 1f, 0f, H * 0.82f, W, Align.center);
        game.drawText("sobreviva o maximo que puder", 0.4f, Theme.MAGENTA, 0.9f, 0f, H * 0.82f - 1.7f, W, Align.center);

        int best = game.getBestScore();
        if (best > 0) {
            game.drawText("RECORDE: " + best, 0.5f, Theme.YELLOW, 1f, 0f, 6.4f, W, Align.center);
        }

        float blink = 0.55f + 0.45f * MathUtils.sin(time * 4f);
        game.drawText("TOQUE / ENTER / ESPACO para comecar", 0.4f, Theme.WHITE, blink, 0f, 4.2f, W, Align.center);
        game.drawText("SETAS / A D / TOQUE NAS LATERAIS: mover", 0.32f, Theme.WHITE, 0.6f, 0f, 3.2f, W, Align.center);
        game.drawText("ESC: sair", 0.32f, Theme.WHITE, 0.4f, 0f, 2.6f, W, Align.center);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
}