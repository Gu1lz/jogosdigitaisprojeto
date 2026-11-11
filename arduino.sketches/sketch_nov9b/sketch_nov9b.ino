#include <Arduino.h>

const int SDA_PIN = 21;
const int SCL_PIN = 22;
const unsigned int TEMPO_SINAL_US = 100;
const unsigned long TIMEOUT_CLOCK_US = 2000;
const int TENTATIVAS_LEITURA = 3;
const bool MOSTRAR_LEITURAS = true;   // mostra AX, AY e AZ uma vez por segundo
uint8_t MPU_ADDR = 0x68;              // detectado entre 0x68 e 0x69

// --- Parâmetros de calibração ---
const int   THRESHOLD   = 3000;   // sensibilidade (ajuste)
const int   N_MEDIA     = 5;      // tamanho da média móvel
const int   DEADBAND    = 500;    // histerese para não oscilar
const unsigned long INTERVALO_MS = 50;  // ate 20 leituras por segundo
const unsigned long REENVIO_MS = 250;  // repete o estado para sincronizar com o jogo
const unsigned long NOVA_TENTATIVA_MS = 1000;

int buf[N_MEDIA];
int idx = 0;
int dirAnterior = 0;
unsigned long ultimoEnvio = 0;
unsigned long ultimoTilt = 0;
unsigned long ultimaTentativa = 0;
unsigned long ultimaLeituraExibida = 0;
bool sensorOk = false;

// I2C por GPIO. As linhas ficam em dreno aberto com pull-up habilitado.
// Essa configuracao conseguiu ler o MPU nesta montagem, onde Wire retornou NACK.
void liberarLinha(int pino) {
    digitalWrite(pino, HIGH);
    delayMicroseconds(TEMPO_SINAL_US);
}

void baixarLinha(int pino) {
    digitalWrite(pino, LOW);
    delayMicroseconds(TEMPO_SINAL_US);
}

bool elevarClock() {
    liberarLinha(SCL_PIN);
    unsigned long inicio = micros();
    while (digitalRead(SCL_PIN) == LOW) {
        if (micros() - inicio >= TIMEOUT_CLOCK_US) return false;
    }
    return true;
}

bool iniciarTransacao() {
    liberarLinha(SDA_PIN);
    if (!elevarClock() || digitalRead(SDA_PIN) == LOW) return false;
    baixarLinha(SDA_PIN);
    baixarLinha(SCL_PIN);
    return true;
}

void encerrarTransacao() {
    baixarLinha(SDA_PIN);
    elevarClock();
    liberarLinha(SDA_PIN);
}

bool enviarByte(uint8_t valor) {
    for (int bit = 7; bit >= 0; bit--) {
        if (valor & (1 << bit)) liberarLinha(SDA_PIN);
        else baixarLinha(SDA_PIN);
        if (!elevarClock()) return false;
        baixarLinha(SCL_PIN);
    }
    liberarLinha(SDA_PIN);
    bool clockOk = elevarClock();
    bool confirmado = clockOk && digitalRead(SDA_PIN) == LOW;
    baixarLinha(SCL_PIN);
    return confirmado;
}

bool receberByte(uint8_t &valor, bool confirmar) {
    valor = 0;
    liberarLinha(SDA_PIN);
    for (int bit = 0; bit < 8; bit++) {
        if (!elevarClock()) return false;
        valor = (valor << 1) | digitalRead(SDA_PIN);
        baixarLinha(SCL_PIN);
    }
    if (confirmar) baixarLinha(SDA_PIN);
    else liberarLinha(SDA_PIN);
    bool clockOk = elevarClock();
    baixarLinha(SCL_PIN);
    liberarLinha(SDA_PIN);
    return clockOk;
}

void recuperarBarramento() {
    pinMode(SDA_PIN, OUTPUT_OPEN_DRAIN | PULLUP);
    pinMode(SCL_PIN, OUTPUT_OPEN_DRAIN | PULLUP);
    liberarLinha(SDA_PIN);
    elevarClock();
    for (int i = 0; i < 9; i++) {
        baixarLinha(SCL_PIN);
        elevarClock();
    }
    encerrarTransacao();
}

bool escreverRegistrador(uint8_t registrador, uint8_t valor) {
    bool ok = iniciarTransacao() && enviarByte(MPU_ADDR << 1)
        && enviarByte(registrador) && enviarByte(valor);
    encerrarTransacao();
    return ok;
}

bool lerRegistradores(uint8_t registrador, uint8_t *dados, int quantidade) {
    bool ok = iniciarTransacao() && enviarByte(MPU_ADDR << 1)
        && enviarByte(registrador) && iniciarTransacao()
        && enviarByte((MPU_ADDR << 1) | 1);
    for (int i = 0; ok && i < quantidade; i++) {
        ok = receberByte(dados[i], i < quantidade - 1);
    }
    encerrarTransacao();
    return ok;
}

void registrarErro(const char *etapa) {
    Serial.print("ERRO: MPU6050 etapa=");
    Serial.print(etapa);
    Serial.print(" endereco=0x");
    Serial.println(MPU_ADDR, HEX);
}

bool lerAceleracao(int16_t &ax, int16_t &ay, int16_t &az) {
    uint8_t dados[6];
    for (int tentativa = 0; tentativa < TENTATIVAS_LEITURA; tentativa++) {
        if (lerRegistradores(0x3B, dados, 6)) {
            ax = (int16_t)((dados[0] << 8) | dados[1]);
            ay = (int16_t)((dados[2] << 8) | dados[3]);
            az = (int16_t)((dados[4] << 8) | dados[5]);
            return true;
        }
        delay(1);
    }
    registrarErro("LER_ACCEL");
    return false;
}

bool iniciarSensor() {
    recuperarBarramento();
    bool encontrado = false;
    for (uint8_t endereco = 0x68; endereco <= 0x69; endereco++) {
        bool confirmou = iniciarTransacao() && enviarByte(endereco << 1);
        encerrarTransacao();
        Serial.print("INFO: I2C_GPIO endereco=0x");
        Serial.print(endereco, HEX);
        Serial.print(" ACK=");
        Serial.println(confirmou);
        if (confirmou) {
            MPU_ADDR = endereco;
            encontrado = true;
            break;
        }
    }
    if (!encontrado) {
        Serial.println("ERRO: I2C etapa=DETECTAR sem resposta em 0x68 e 0x69");
        return false;
    }
    if (!escreverRegistrador(0x6B, 0x00)) {
        registrarErro("ACORDAR");
        return false;
    }
    delay(100);

    uint8_t identidade;
    if (lerRegistradores(0x75, &identidade, 1)) {
        Serial.print("INFO: WHO_AM_I=0x");
        Serial.println(identidade, HEX);
    }
    int16_t ax, ay, az;
    if (!lerAceleracao(ax, ay, az)) return false;

    // Descarta a media anterior ao recuperar o sensor.
    for (int i = 0; i < N_MEDIA; i++) buf[i] = ax;
    idx = 0;
    dirAnterior = 0;
    Serial.println("READY");
    Serial.println("TILT:0");
    ultimoTilt = millis();
    return true;
}

// se o sensor sumir, solta o personagem (TILT:0) e avisa
void sensorFalhou() {
    sensorOk = false;
    if (dirAnterior != 0) {
        dirAnterior = 0;
        Serial.println("TILT:0");
    }
    ultimaTentativa = millis();
}

void setup() {
    Serial.begin(9600);
    delay(1000);   // mesma espera usada no diagnostico que leu os eixos
    Serial.println("INFO: HeadRush sketch B v3 SERIAL=9600 SDA=21 SCL=22 I2C=GPIO SINAL_US=100");
    sensorOk = iniciarSensor();
    ultimaTentativa = millis();
}

void loop() {
    delay(1);
    if (!sensorOk) {
        if (millis() - ultimaTentativa < NOVA_TENTATIVA_MS) return;
        sensorOk = iniciarSensor();
        ultimaTentativa = millis();
        return;
    }

    if (millis() - ultimoEnvio < INTERVALO_MS) return;
    ultimoEnvio = millis();

    int16_t ax, ay, az;
    if (!lerAceleracao(ax, ay, az)) {
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

    if (dir != dirAnterior || millis() - ultimoTilt >= REENVIO_MS) {
        Serial.print("TILT:");
        Serial.println(dir);
        dirAnterior = dir;
        ultimoTilt = millis();
    }

    if (MOSTRAR_LEITURAS && millis() - ultimaLeituraExibida >= 1000) {
        Serial.print("INFO: AX=");
        Serial.print(ax);
        Serial.print(" AY=");
        Serial.print(ay);
        Serial.print(" AZ=");
        Serial.print(az);
        Serial.print(" MEDIA_X=");
        Serial.print(media);
        Serial.print(" TILT=");
        Serial.println(dir);
        ultimaLeituraExibida = millis();
    }
}
