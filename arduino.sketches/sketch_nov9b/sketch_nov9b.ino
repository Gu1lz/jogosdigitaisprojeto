#include <Wire.h>

const uint8_t MPU_ADDR = 0x68;   // se o AD0 estiver no 3V3, use 0x69

// --- Parâmetros de calibração ---
const int   THRESHOLD   = 3000;   // sensibilidade (ajuste)
const int   N_MEDIA     = 5;      // tamanho da média móvel
const int   DEADBAND    = 500;    // histerese para não oscilar
const unsigned long INTERVALO_MS = 20;  // 50 leituras por segundo

int buf[N_MEDIA];
int idx = 0;
int dirAnterior = 0;
unsigned long ultimoEnvio = 0;
unsigned long ultimoAviso = 0;
bool sensorOk = false;

// tira o sensor do modo sleep; devolve false se ele não responder
bool acordarSensor() {
    Wire.beginTransmission(MPU_ADDR);
    Wire.write(0x6B);   // registrador PWR_MGMT_1
    Wire.write(0x00);
    return Wire.endTransmission(true) == 0;
}

// lê a aceleração do eixo X; devolve false se o sensor não responder
bool lerAceleracaoX(int16_t &ax) {
    Wire.beginTransmission(MPU_ADDR);
    Wire.write(0x3B);   // registrador ACCEL_XOUT_H
    if (Wire.endTransmission(false) != 0) return false;
    if (Wire.requestFrom((int)MPU_ADDR, 2) != 2) return false;
    uint8_t alto = Wire.read();
    uint8_t baixo = Wire.read();
    ax = (int16_t)((alto << 8) | baixo);
    return true;
}

// avisa no máximo uma vez por segundo, para não encher a serial
void avisarErro() {
    if (millis() - ultimoAviso > 1000) {
        Serial.println("ERRO: MPU6050 nao respondeu");
        ultimoAviso = millis();
    }
}

// se o sensor sumir, solta o personagem (TILT:0) e avisa
void sensorFalhou() {
    sensorOk = false;
    if (dirAnterior != 0) {
        dirAnterior = 0;
        Serial.println("TILT:0");
    }
    avisarErro();
}

void setup() {
    Serial.begin(9600);
    Wire.begin(21, 22);   // SDA = 21, SCL = 22 (ESP32)
    for (int i = 0; i < N_MEDIA; i++) buf[i] = 0;
    sensorOk = acordarSensor();
    Serial.println("READY");
}

void loop() {
    if (millis() - ultimoEnvio < INTERVALO_MS) return;
    ultimoEnvio = millis();

    // se o sensor falhou, tenta acordar de novo (sem travar o programa)
    if (!sensorOk) {
        sensorOk = acordarSensor();
        if (!sensorOk) { avisarErro(); return; }
    }

    int16_t ax;
    if (!lerAceleracaoX(ax)) {
        sensorFalhou();
        return;
    }

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

    if (dir != dirAnterior) {
        Serial.print("TILT:");
        Serial.println(dir);
        dirAnterior = dir;
    }
}