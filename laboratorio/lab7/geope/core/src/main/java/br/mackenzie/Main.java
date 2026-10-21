package br.mackenzie;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main extends ApplicationAdapter {

    private static final float W = 8f;
    private static final float H = 5f;
    private static final int STARS = 100;

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private FitViewport viewport;

    private final Vector2 mouse = new Vector2();
    private final float[] starX = new float[STARS];
    private final float[] starY = new float[STARS];
    private final float[] starSpeed = new float[STARS];

    private final String[] options = {"JOGAR", "OPÇÕES", "SAIR"};
    private final Rectangle[] buttons = {
        new Rectangle(2.5f, 2.35f, 3f, 0.55f),
        new Rectangle(2.5f, 1.55f, 3f, 0.55f),
        new Rectangle(2.5f, 0.75f, 3f, 0.55f)
    };

    private int selected = 0;
    private int screen = 0;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = new BitmapFont();
        font.setUseIntegerPositions(false);
        viewport = new FitViewport(W, H, new OrthographicCamera());

        for (int i = 0; i < STARS; i++) {
            starX[i] = MathUtils.random(W);
            starY[i] = MathUtils.random(H);
            starSpeed[i] = MathUtils.random(0.02f, 0.1f);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        for (int i = 0; i < STARS; i++) {
            starX[i] -= starSpeed[i] * delta;
            if (starX[i] < 0) {
                starX[i] = W;
                starY[i] = MathUtils.random(H);
            }
        }

        input();

        ScreenUtils.clear(0.02f, 0.03f, 0.08f, 1f);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapes.setProjectionMatrix(viewport.getCamera().combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.55f, 0.72f, 1f, 1f);
        for (int i = 0; i < STARS; i++) {
            shapes.circle(starX[i], starY[i], 0.015f, 6);
        }
        if (screen == 0) {
            for (int i = 0; i < buttons.length; i++) {
                shapes.setColor(i == selected ? 0.08f : 0.05f, i == selected ? 0.35f : 0.09f, i == selected ? 0.58f : 0.18f, 1f);
                shapes.rect(buttons[i].x, buttons[i].y, buttons[i].width, buttons[i].height);
            }
        }
        shapes.end();

        batch.begin();
        if (screen == 0) {
            font.setColor(0.35f, 0.8f, 1f, 1f);
            font.getData().setScale(0.42f);
            text("ODISSEIA CÓSMICA", 4.25f);

            font.setColor(1f, 1f, 1f, 1f);
            font.getData().setScale(0.19f);
            for (int i = 0; i < buttons.length; i++) {
                text(options[i], buttons[i].y + 0.4f);
            }
        } else {
            font.setColor(0.35f, 0.8f, 1f, 1f);
            font.getData().setScale(0.3f);
            text(screen == 1 ? "JOGO EM DESENVOLVIMENTO" : "OPÇÕES", 2.75f);

            font.setColor(1f, 1f, 1f, 1f);
            font.getData().setScale(0.17f);
            text("Pressione ESC para voltar", 2.15f);
        }
        batch.end();
    }

    private void input() {
        if (screen != 0) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) screen = 0;
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) selected = (selected + 2) % 3;
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) selected = (selected + 1) % 3;
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) activate(selected);

        if (Gdx.input.justTouched()) {
            mouse.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(mouse);
            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i].contains(mouse)) activate(i);
            }
        }
    }

    private void activate(int option) {
        if (option == 2) Gdx.app.exit();
        else screen = option + 1;
    }

    private void text(String text, float y) {
        font.draw(batch, text, 0, y, W, Align.center, false);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        font.dispose();
    }
}