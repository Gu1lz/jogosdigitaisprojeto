package br.mackenzie;

import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;
import java.io.InputStream;

public class ArduinoReaderThread extends Thread {

    private volatile String lastData = "TILT:0";
    private SerialPort arduinoPort;
    private volatile boolean valido;

    public ArduinoReaderThread(String portName) {
        try {
            this.arduinoPort = SerialPort.getCommPort(portName);
            this.valido = true;
        } catch (Exception e) {
            System.err.println("Aviso: Porta serial invalida: " + portName
                + ". Configure -Darduino.port com a porta do ESP32.");
            SerialPortLister.listSerialPorts();
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
        StringBuilder currentLine = new StringBuilder();

        try {
            arduinoPort.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
            arduinoPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
            if (isInterrupted()) {
                return;
            }
            if (!arduinoPort.openPort()) {
                System.err.println("Nao foi possivel abrir " + arduinoPort.getSystemPortName()
                    + ". Feche o Monitor/Plotter Serial da Arduino IDE e confira as permissoes da porta.");
                SerialPortLister.listSerialPorts();
                return;
            }
            System.out.println("Serial aberta: " + arduinoPort.getSystemPortName()
                + " a 9600 baud. Aguardando TILT:-1 / TILT:0 / TILT:1.");

            try (InputStream in = arduinoPort.getInputStream()) {
                while (!isInterrupted() && arduinoPort.isOpen()) {
                    int disponiveis = in.available();
                    if (disponiveis < 0) {
                        throw new IOException("Porta serial desconectada");
                    }
                    if (disponiveis > 0) {
                        int recebido = in.read();
                        if (recebido < 0) {
                            throw new IOException("Conexao serial encerrada");
                        }
                        char character = (char) recebido;
                        currentLine.append(character);

                        if (character == '\n') {
                            String texto = currentLine.toString().trim();
                            if (texto.equals("TILT:-1") || texto.equals("TILT:0") || texto.equals("TILT:1")) {
                                if (!texto.equals(lastData)) {
                                    System.out.println("Serial recebida: " + texto);
                                }
                                lastData = texto;
                            } else if (texto.equals("READY") || texto.startsWith("ERRO:")) {
                                lastData = "TILT:0";
                                System.out.println("ESP32: " + texto);
                            } else if (texto.startsWith("TILT")) {
                                System.err.println("Mensagem serial invalida: " + texto
                                    + ". Use TILT:-1, TILT:0 ou TILT:1.");
                            }
                            currentLine.setLength(0);
                        }
                    }
                    Thread.sleep(1);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Erro na leitura serial: " + e.getMessage());
        } finally {
            lastData = "TILT:0";
            valido = false;
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
