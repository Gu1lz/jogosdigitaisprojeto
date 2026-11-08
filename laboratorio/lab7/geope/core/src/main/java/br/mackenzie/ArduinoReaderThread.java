package br.mackenzie;

import com.fazecast.jSerialComm.SerialPort;
import java.io.InputStream;

public class ArduinoReaderThread extends Thread {

    private volatile String lastData = "TILT:0";
    private SerialPort arduinoPort;
    private boolean valido;

    public ArduinoReaderThread(String portName) {
        try {
            this.arduinoPort = SerialPort.getCommPort(portName);
            this.valido = true;
        } catch (Exception e) {
            System.err.println("Aviso: Porta serial invalida: " + portName
                + ". Jogo usara teclado/toque.");
            this.arduinoPort = null;
            this.valido = false;
        }
    }

    public boolean isValido() {
        return valido;
    }

    @Override
    public void run() {
        if (arduinoPort == null) {
            return;
        }
        if (!arduinoPort.openPort()) {
            System.err.println("Aviso: Nao foi possivel abrir a porta serial. Jogo usara teclado/toque.");
            return;
        }

        arduinoPort.setBaudRate(9600);
        InputStream in = arduinoPort.getInputStream();
        StringBuilder currentLine = new StringBuilder();

        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (in.available() > 0) {
                    char character = (char) in.read();
                    currentLine.append(character);

                    if (character == '\n') {
                        String texto = currentLine.toString().trim();
                        if (texto.startsWith("TILT:")) {
                            lastData = texto;
                        }
                        currentLine.setLength(0);
                    }
                }
                Thread.sleep(1);
            }
        } catch (Exception e) {
            System.err.println("Erro na leitura serial: " + e.getMessage());
        } finally {
            if (arduinoPort.isOpen()) {
                arduinoPort.closePort();
                System.out.println("Porta serial fechada.");
            }
        }
    }

    public String getLastData() {
        return lastData;
    }

    public void stopReading() {
        this.interrupt();
    }
}