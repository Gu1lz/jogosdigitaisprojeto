package br.mackenzie;

import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;
import java.io.InputStream;

public class ArduinoReaderThread extends Thread {

    private volatile String lastData = "TILT:0";
    private final String portName;
    private SerialPort arduinoPort;
    private volatile boolean valido;
    private volatile boolean dispositivoDetectado;
    private volatile boolean sensorPronto;
    private volatile String status = "ESP32 aguardando conexao";

    public static String getPortaConfigurada() {
        return System.getProperty("arduino.port", "/dev/ttyACM0");
    }

    public boolean isAguardandoSensor() {
        return dispositivoDetectado && !sensorPronto;
    }

    public String getStatus() {
        return status;
    }

    public ArduinoReaderThread(String portName) {
        this.portName = portName;
        try {
            this.arduinoPort = SerialPort.getCommPort(portName);
            this.valido = false;
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
        while (!isInterrupted()) {
            readConnection();
            if (isInterrupted()) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void readConnection() {
        StringBuilder currentLine = new StringBuilder();

        try {
            arduinoPort = SerialPort.getCommPort(portName);
            arduinoPort.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
            arduinoPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
            // Libera as linhas ligadas a EN e GPIO0 antes de abrir o ESP32.
            arduinoPort.clearDTR();
            arduinoPort.clearRTS();
            if (isInterrupted()) {
                return;
            }
            if (!arduinoPort.openPort()) {
                System.err.println("Nao foi possivel abrir " + arduinoPort.getSystemPortName()
                    + ". Feche o Monitor/Plotter Serial da Arduino IDE e confira as permissoes da porta.");
                SerialPortLister.listSerialPorts();
                return;
            }
            valido = true;
            dispositivoDetectado = true;
            status = "ESP32 conectado - aguardando sensor";
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
                                sensorPronto = true;
                                if (texto.equals("TILT:-1")) {
                                    status = "SENSOR ESQUERDA";
                                } else if (texto.equals("TILT:1")) {
                                    status = "SENSOR DIREITA";
                                } else {
                                    status = "SENSOR CENTRO";
                                }
                            } else if (texto.equals("READY") || texto.startsWith("ERRO:")) {
                                lastData = "TILT:0";
                                sensorPronto = false;
                                status = texto.equals("READY") ? "Sensor iniciando" : "Sensor sem leitura";
                                System.out.println("ESP32: " + texto);
                            } else if (texto.startsWith("INFO:")) {
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
            sensorPronto = false;
            valido = false;
            status = "ESP32 aguardando conexao";
            if (arduinoPort != null && arduinoPort.isOpen()) {
                arduinoPort.closePort();
                System.out.println("Porta serial fechada.");
            }
        }
    }

    public String getLastData() {
        return lastData;
    }

    public int getDirecao() {
        String dado = lastData;
        if (dado.equals("TILT:1")) return 1;
        if (dado.equals("TILT:-1")) return -1;
        return 0;
    }

    public void stopReading() {
        this.interrupt();
    }
}
