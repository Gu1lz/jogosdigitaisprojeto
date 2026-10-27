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

    private static final int OPT_PLAY = 0;
    private static final int OPT_EXIT = 1;

    private final Main game;
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final Assets assets;
    private final FitViewport viewport = new FitViewport(W, H);
    private final CityScenery scenery = new CityScenery(777L);
    private final OptionMenu menu = new OptionMenu(5.0f, 1.0f, 0.55f, "JOGAR", "SAIR");

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

        int choice = menu.update(viewport);
        if (choice == OPT_PLAY) {
            game.setScreen(new GameScreen(game));
            return;
        }
        if (choice == OPT_EXIT || Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }

        ScreenUtils.clear(Theme.VOID);
        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        batch.setProjectionMatrix(viewport.getCamera().combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        float hy = H * 0.5f + MathUtils.sin(time * 2f) * 0.25f;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        scenery.draw(shapes);
        scenery.drawRain(shapes);
        HeadRenderer.drawGlow(shapes, W / 2f, hy, 2.4f);
        shapes.end();

        batch.begin();
        HeadRenderer.drawSprite(batch, assets.neutral.getKeyFrame(time), W / 2f, hy, 3.0f,
            MathUtils.sin(time * 1.5f) * 8f);

        game.drawText(Theme.GAME_TITLE, 1.3f, Theme.CYAN, 1f, 0f, H * 0.82f, W, Align.center);
        game.drawText("sobreviva o maximo que puder", 0.4f, Theme.MAGENTA, 0.9f, 0f, H * 0.82f - 1.7f, W, Align.center);

        int best = game.getBestScore();
        if (best > 0) {
            game.drawText("RECORDE: " + best, 0.5f, Theme.YELLOW, 1f, 0f, 6.0f, W, Align.center);
        }

        menu.draw(game, time);

        game.drawText("SETAS / W S + ENTER, ou toque na opcao", 0.32f, Theme.WHITE, 0.6f, 0f, 2.2f, W, Align.center);
        game.drawText("No jogo: SETAS / A D / TOQUE NAS LATERAIS", 0.32f, Theme.WHITE, 0.5f, 0f, 1.6f, W, Align.center);
        game.drawText("P / ESC: pausar     R: reiniciar", 0.32f, Theme.WHITE, 0.5f, 0f, 1.1f, W, Align.center);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
}