package br.mackenzie;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class ArduinoGame extends ApplicationAdapter {
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;

    private Rectangle player;
    private float playerSpeed = 200f;

    private ArduinoReaderThread arduinoReader;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        shapeRenderer = new ShapeRenderer();

        player = new Rectangle();
        player.x = 800 / 2 - 30 / 2;
        player.y = 20;
        player.width = 30;
        player.height = 30;

        String portName = ArduinoReaderThread.getPortaConfigurada();
        arduinoReader = new ArduinoReaderThread(portName);
        arduinoReader.start();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);

        player.x += arduinoReader.getDirecao() * playerSpeed * Gdx.graphics.getDeltaTime();

        if (player.x < 0) player.x = 0;
        if (player.x > 800 - player.width) player.x = 800 - player.width;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 1, 0, 1);
        shapeRenderer.rect(player.x, player.y, player.width, player.height);
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        arduinoReader.stopReading();
        try {
            arduinoReader.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        shapeRenderer.dispose();
    }
}
