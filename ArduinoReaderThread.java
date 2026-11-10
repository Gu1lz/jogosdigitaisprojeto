package br.mackenzie;

import com.fazecast.jSerialComm.SerialPort;
import java.io.InputStream;

public class ArduinoReaderThread extends Thread {
    private volatile String lastData = "TILT:0";
    private final SerialPort arduinoPort;
    private final boolean valido;
    private volatile boolean conectado = false;
    private volatile long instanteUltimoTilt = 0L;
    // O firmware envia TILT ao menos a cada 100 ms: 750 ms sem dados = controle neutro.
    private static final long LIMITE_SEM_DADOS_NS = 750_000_000L;
    private final boolean debug = Boolean.parseBoolean(System.getProperty("arduino.debug", "false"));

    public ArduinoReaderThread(String portName) {
        SerialPort porta;
        try {
            porta = SerialPort.getCommPort(portName);
        } catch (Exception e) {
            System.err.println("Porta serial invalida: " + portName + " (" + e.getMessage() + ")");
            porta = null;
        }
        this.arduinoPort = porta;
        this.valido = porta != null;
    }

    // isValido indica apenas que um objeto de porta foi criado.
    // A conexao real so e comprovada por openPort() em run().
    public boolean isValido() { return valido; }
    public boolean isConectado() { return conectado; }

    @Override
    public void run() {
        if (arduinoPort == null) return;
        arduinoPort.setBaudRate(9600);
        arduinoPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
        System.out.println("Tentando abrir porta serial: " + arduinoPort.getSystemPortName());
        if (!arduinoPort.openPort()) {
            System.err.println("ERRO: nao foi possivel abrir " + arduinoPort.getSystemPortName());
            System.err.println("Confira porta, permissao de acesso e Monitor Serial fechado.");
            SerialPort[] portas = SerialPort.getCommPorts();
            for (SerialPort p : portas) System.err.println("Porta encontrada: " + p.getSystemPortName());
            return;
        }
        conectado = true;
        System.out.println("Porta serial aberta: " + arduinoPort.getSystemPortName() + " a 9600 baud");
        StringBuilder linha = new StringBuilder();
        try (InputStream in = arduinoPort.getInputStream()) {
            while (!isInterrupted()) {
                if (in.available() > 0) {
                    int b = in.read();
                    if (b < 0) continue;
                    if (b == '\n') {
                        String texto = linha.toString().trim();
                        if (texto.equals("TILT:-1") || texto.equals("TILT:0") || texto.equals("TILT:1")) {
                            lastData = texto;
                            instanteUltimoTilt = System.nanoTime();
                            if (debug) System.out.println("ESP32 > " + texto);
                        } else if (texto.startsWith("ERRO:") || texto.equals("READY")) {
                            if (texto.startsWith("ERRO:")) {
                                lastData = "TILT:0";
                                instanteUltimoTilt = 0L;
                            }
                            System.out.println("ESP32 > " + texto);
                        } else if (debug && !texto.isEmpty()) {
                            System.out.println("ESP32 > " + texto);
                        }
                        linha.setLength(0);
                    } else if (linha.length() < 256) {
                        linha.append((char)b);
                    } else {
                        linha.setLength(0); // evita crescimento ilimitado de linha incompleta
                    }
                } else {
                    Thread.sleep(5);
                }
            }
        } catch (InterruptedException e) {
            interrupt();
        } catch (Exception e) {
            System.err.println("ERRO durante leitura serial: " + e.getMessage());
        } finally {
            conectado = false;
            lastData = "TILT:0";
            instanteUltimoTilt = 0L;
            if (arduinoPort.isOpen()) arduinoPort.closePort();
            System.out.println("Porta serial encerrada.");
        }
    }

    public String getLastData() {
        if (!conectado || instanteUltimoTilt == 0L
                || System.nanoTime() - instanteUltimoTilt > LIMITE_SEM_DADOS_NS) {
            return "TILT:0";
        }
        return lastData;
    }
    public void stopReading() { interrupt(); }
}
