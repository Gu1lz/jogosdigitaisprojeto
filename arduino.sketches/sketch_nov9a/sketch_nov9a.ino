#include <Wire.h>
#include <MPU6050.h>

MPU6050 mpu;

// --- Parâmetros de calibração ---
const int   THRESHOLD   = 3000;   // sensibilidade (ajuste)
const int   N_MEDIA     = 5;      // tamanho da média móvel
const int   DEADBAND    = 500;    // histerese para não oscilar
const unsigned long INTERVALO_MS = 20;  // 50 leituras por segundo
const unsigned long REENVIO_MS = 250;  // repete o estado para sincronizar com o jogo

int buf[N_MEDIA];
int idx = 0;
int dirAnterior = 0;
unsigned long ultimoEnvio = 0;
unsigned long ultimoTilt = 0;

void setup() {
    Serial.begin(9600);
    Wire.begin(21, 22);   // SDA = 21, SCL = 22 (ESP32)

    mpu.initialize();
    if (!mpu.testConnection()) {
        Serial.println("ERRO: MPU6050 nao respondeu");
        while (1) { delay(1000); }
    }
    for (int i = 0; i < N_MEDIA; i++) buf[i] = 0;
    Serial.println("READY");
    Serial.println("TILT:0");
}

void loop() {
    if (millis() - ultimoEnvio < INTERVALO_MS) return;
    ultimoEnvio = millis();

    int16_t ax, ay, az;
    mpu.getAcceleration(&ax, &ay, &az);

    // média móvel do eixo X
    buf[idx] = ax;
    idx = (idx + 1) % N_MEDIA;
    long soma = 0;
    for (int i = 0; i < N_MEDIA; i++) soma += buf[i];
    int media = (int)(soma / N_MEDIA);

    // histerese
    int dir = 0;
    if (media >  THRESHOLD + DEADBAND) dir = 1;
    else if (media < -THRESHOLD - DEADBAND) dir = -1;
    else if (media > -THRESHOLD && media < THRESHOLD) dir = 0;
    else dir = dirAnterior;  // dentro da zona morta, mantém o estado

    if (dir != dirAnterior || millis() - ultimoTilt >= REENVIO_MS) {
        Serial.print("TILT:");
        Serial.println(dir);
        dirAnterior = dir;
        ultimoTilt = millis();
    }
}
