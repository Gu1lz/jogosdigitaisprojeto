package br.mackenzie;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class ArduinoDemoLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Teste do controle ESP32 - MPU6050");
        config.setWindowedMode(1000, 600);
        config.useVsync(true);
        new Lwjgl3Application(new ArduinoGame(), config);
    }
}
