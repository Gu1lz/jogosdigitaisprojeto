# Guia de montagem do dispositivo IoT (ESP32 + MPU-6050)

*Passo a passo para montar, programar e testar o controle por inclinação da cabeça do nosso jogo de reabilitação. Baseado nas instruções que o grupo passou, reorganizadas, com explicações e checagens extras.*

## Como o dispositivo funciona

1. O **MPU-6050** (acelerômetro) mede a inclinação da cabeça.
2. O **ESP32** lê o sensor e envia pelo cabo USB uma mensagem simples: `TILT:-1` (inclinou para um lado), `TILT:0` (centro) ou `TILT:1` (inclinou para o outro lado). Qual lado é qual depende de como o módulo é fixado.
3. O **jogo em Java (libGDX)** lê essa mensagem pela porta serial e move o personagem.

## Regras de ouro (leia antes de começar)

- **Cabo USB desconectado** sempre que for soldar, ligar ou mexer nos fios.
- O MPU-6050 vai **sempre no pino 3V3** do ESP32. Nunca em VIN nem em 5V.
- **Confira fio por fio** (tabela da Etapa 3) antes de plugar o USB.
- **Não force** nenhum conector: se não entra, está invertido ou desalinhado.
- Feche o **Monitor Serial** da Arduino IDE antes de rodar o jogo. Só um programa usa a porta por vez.

## Lista de materiais

| Item | Para que serve | Observação |
| --- | --- | --- |
| ESP32 DevKit (DOIT, WROOM-32, chip CH9102X) | Lê o sensor e conversa com o PC | Já vem com os pinos soldados |
| Módulo MPU-6050 (GY-521) | Sensor de inclinação | Vem com a tira de pinos **solta**: precisa soldar |
| 4 jumpers | Ligam o sensor ao ESP32 | Veja a Etapa 2 |
| Cabo USB-A para micro-USB, com dados | Alimenta o ESP32 e leva os dados | Se não aparecer porta serial no PC, o cabo pode ser só de carga |
| Ferro de solda e solda | Soldar os pinos do MPU-6050 | Peça ajuda se nunca soldou |
| Notebook com Arduino IDE | Programar o ESP32 |  |
| Opcional: fita isolante, velcro ou tiara | Prender na cabeça (Etapa 10) |  |

## Etapa 1 — Soldar os pinos no MPU-6050

1. Com tudo desconectado, separe a tira de pinos que veio no saquinho do módulo.
2. Encaixe a tira por **baixo** do módulo (lado sem o chip): a parte **longa** dos pinos fica para baixo e a parte **curta** atravessa os furos até o lado do chip.
3. Mantenha o módulo reto e firme sobre a tira (por exemplo, espetando a tira numa protoboard ou apoiando numa superfície plana).
4. Solde **um pino da ponta primeiro**. Aqueça o furo e o pino juntos por 1 a 2 segundos, encoste a solda e retire. Confira se o módulo ficou reto e solde os demais.
5. Cada junta deve ficar **brilhante e em forma de cone**.
6. Confira que **não há ponte de solda** entre pinos vizinhos, principalmente entre SCL e SDA. Se houver, derreta e remova o excesso.
7. Espere esfriar 1 a 2 minutos antes de mexer. A ponta do ferro e as soldas recentes queimam.

Sem ferro de solda? Peça para alguém do laboratório ou do grupo soldar. Pinos mal soldados são a causa mais comum de sensor que "some" no meio do uso.

## Etapa 2 — Conferir os jumpers

Olhe as **duas pontas** dos jumpers. O ESP32 e o MPU-6050 (depois de soldado) têm pinos **macho**, então precisam de pontas **fêmea** (com buraquinho).

| Pontas do jumper | O que fazer |
| --- | --- |
| Fêmea nas duas pontas | Ligue direto, do MPU-6050 para o ESP32 |
| Macho de um lado, fêmea do outro | Use uma protoboard pequena: encaixe o MPU-6050 nela, ligue a ponta **fêmea** nos pinos do ESP32 e a ponta **macho** na protoboard, na mesma coluna do pino correspondente do MPU-6050 |
| Macho nas duas pontas | Não encaixa nos pinos do ESP32. Precisa de jumpers com pelo menos uma ponta fêmea |

## Etapa 3 — Ligar os fios

**Cabo USB desconectado.**

| # | Pino do MPU-6050 | Vai para o pino do ESP32 | Cor sugerida |
| --- | --- | --- | --- |
| 1 | VCC | 3V3 | Vermelho |
| 2 | GND | GND | Preto |
| 3 | SDA | D21 (GPIO 21) | Azul |
| 4 | SCL | D22 (GPIO 22) | Amarelo |

Os outros pinos do módulo (XDA, XCL, AD0 e INT) ficam **sem ligar**.

**Como achar os pinos no ESP32** (leia sempre o texto impresso na placa):

- Vire a placa com a **porta USB para você** e a antena (a parte retangular) para o lado oposto.
- No **lado direito**, contando a partir do USB, a ordem impressa é: 3V3, GND, D15, D2, D4, RX2, TX2, D5, D18, D19, **D21**, RX0, TX0, **D22**, D23.
- Ou seja: o 1º pino é **3V3**, o 2º é **GND**, o 11º é **D21** e o 14º é **D22**. Entre D21 e D22 ficam RX0 e TX0: não ligue nesses.
- No lado esquerdo há outro GND e o pino **VIN**. O VIN é entrada de 5V: **não use** para o sensor.

**Como achar os pinos no MPU-6050:** a ordem impressa no módulo é VCC, GND, SCL, SDA, XDA, XCL, AD0, INT. Atenção: o **SCL vem antes do SDA**, e é fácil trocar os dois.

**Conferência antes de ligar o USB:**

- VCC do módulo no **3V3** do ESP32 (e não no VIN).
- SDA no **D21** e SCL no **D22** (não trocados).
- Nenhum fio solto encostando em outro pino.

Só então conecte o cabo USB. O LED de energia do ESP32 deve acender, e o do módulo também. Se algo esquentar ou houver cheiro de queimado, **desconecte na hora** e revise os fios.

## Etapa 4 — Preparar o computador

1. Instale a **Arduino IDE 2.x** (arduino.cc/en/software).
2. Em `File → Preferences`, no campo **Additional boards manager URLs**, cole: `https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json`
3. Em `Tools → Board → Boards Manager`, procure **esp32** e instale **"esp32 by Espressif Systems"**. O download é grande e pode demorar.
4. Em `Tools → Board → ESP32 Arduino`, escolha **ESP32 Dev Module**.
5. **Não precisa instalar biblioteca do sensor.** O diagnóstico da Etapa 5 usa `Wire`, fornecida pelo suporte ao ESP32. O sketch B v3 implementa a comunicação I2C pelos GPIOs e utiliza `Arduino.h`.
6. **Driver da porta** (o chip da placa é o CH9102X):
   - **Windows:** com o ESP32 plugado, abra o Gerenciador de Dispositivos e veja em "Portas (COM e LPT)". Se aparecer um dispositivo desconhecido, instale o driver **CH343/CH9102** do fabricante (WCH). Em computador da faculdade, pode ser preciso pedir autorização.
   - **Linux:** rode `sudo usermod -aG dialout $USER` e faça logout e login. A porta costuma ser `/dev/ttyUSB0` ou `/dev/ttyACM0`.
7. Em `Tools → Port`, escolha a porta do ESP32. Se estiver em dúvida qual é, desconecte o cabo, veja a lista, reconecte e veja qual porta apareceu.

## Etapa 5 — Teste rápido: o sensor está funcionando?

Antes do código do jogo, confirme que o ESP32 enxerga o sensor. Este sketch **procura dispositivos na ligação I2C**, lê o código de identidade do sensor e mostra os valores brutos. Ele não usa nenhuma biblioteca de sensor, então não há nada extra para instalar. Grave este sketch de teste:

```cpp
#include <Wire.h>

const uint8_t MPU_ADDR = 0x68;   // se o AD0 estiver ligado no 3V3, o endereço vira 0x69

bool sensorAcordado = false;

void procurarDispositivos() {
    Serial.println("Procurando dispositivos I2C...");
    int achados = 0;
    for (uint8_t addr = 1; addr < 127; addr++) {
        Wire.beginTransmission(addr);
        if (Wire.endTransmission() == 0) {
            Serial.print("Achei dispositivo em 0x");
            Serial.println(addr, HEX);
            achados++;
        }
    }
    if (achados == 0) {
        Serial.println("Nenhum dispositivo encontrado. Revise fios e soldas.");
    }
}

int lerRegistrador(uint8_t reg) {
    Wire.beginTransmission(MPU_ADDR);
    Wire.write(reg);
    if (Wire.endTransmission(false) != 0) return -1;
    if (Wire.requestFrom((int)MPU_ADDR, 1) != 1) return -1;
    return Wire.read();
}

int16_t lerInt16() {
    uint8_t alto = Wire.read();
    uint8_t baixo = Wire.read();
    return (int16_t)((alto << 8) | baixo);
}

void setup() {
    Serial.begin(9600);
    Wire.begin(21, 22);   // SDA = 21, SCL = 22
    delay(1000);

    procurarDispositivos();

    int quem = lerRegistrador(0x75);   // WHO_AM_I: identidade do sensor
    if (quem < 0) {
        Serial.println("O sensor nao respondeu no endereco do MPU_ADDR.");
        return;
    }
    Serial.print("WHO_AM_I: 0x");
    Serial.println(quem, HEX);

    // tira o sensor do modo sleep
    Wire.beginTransmission(MPU_ADDR);
    Wire.write(0x6B);   // registrador PWR_MGMT_1
    Wire.write(0x00);
    sensorAcordado = (Wire.endTransmission(true) == 0);
}

void loop() {
    if (!sensorAcordado) return;

    Wire.beginTransmission(MPU_ADDR);
    Wire.write(0x3B);   // primeiro registrador da aceleração
    Wire.endTransmission(false);
    if (Wire.requestFrom((int)MPU_ADDR, 6) != 6) return;

    int16_t ax = lerInt16();
    int16_t ay = lerInt16();
    int16_t az = lerInt16();

    Serial.print("AX:"); Serial.print(ax);
    Serial.print("  AY:"); Serial.print(ay);
    Serial.print("  AZ:"); Serial.println(az);
    delay(200);
}
```

**Como gravar:**

1. Clique no botão de seta (**Upload**).
2. Se aparecer `Connecting........____` por muito tempo, **segure o botão BOOT** da placa até o envio começar, e depois solte.
3. Abra `Tools → Serial Monitor` e configure **9600 baud**.

**O que esperar:**

- Primeiro aparece `Achei dispositivo em 0x68` (ou `0x69`). Isso confirma uma resposta I2C naquele endereço durante a varredura.
- Depois aparece `WHO_AM_I: 0x68`. Alguns módulos trazem um chip "clone" que responde `0x70`, `0x72` ou `0x98`. A compatibilidade depende do chip presente no módulo e deve ser confirmada pelas leituras dos eixos.
- Em seguida, linhas com `AX`, `AY` e `AZ`. Com o módulo parado, **um dos três valores fica perto de 16000** (o eixo que aponta para cima ou para baixo) e os outros ficam perto de zero. Ao inclinar, os valores mudam de forma suave.
- Se aparecer `Nenhum dispositivo encontrado`, veja a lista abaixo.

**Importante:** o código do jogo usa o eixo **X (AX)**. Para a inclinação lateral da cabeça funcionar, o AX precisa ficar **perto de zero com a cabeça reta** e variar quando você inclina para os lados. Se o AX estiver perto de 16000 com a cabeça reta, gire o módulo ao fixá-lo (Etapa 10) ou troque `ax` por `ay` no código.

**Apareceu `Nenhum dispositivo encontrado` (ou `ERRO: MPU6050 nao respondeu`)?** Quase sempre é contato ruim, e não defeito do sensor. Confira nesta ordem:

1. Se o sensor ainda **não está ligado nos fios**, esse aviso é o esperado.
2. **Pinos só encaixados, sem solda, não fazem contato firme.** O sensor aparece e some. Solde os pinos (Etapa 1).
3. **SDA e SCL trocados.** O SDA vai no D21 e o SCL no D22.
4. **VCC no 3V3 e GND no GND.** Um único fio frouxo no GND já derruba tudo. Puxe cada jumper de leve e troque o suspeito.
5. **Pino AD0 encostando no VCC** muda o endereço para `0x69`. Se o scanner mostrar `0x69`, troque `MPU_ADDR` para `0x69` no código.
6. Desconecte o USB, confira tudo e plugue de novo: este teste só procura o sensor quando o ESP32 reinicia.

## Etapa 6 — Código que envia a inclinação para o jogo

O sketch B v3 lê os três eixos do MPU-6050 e usa AX para o controle lateral. A comunicação I2C usa SDA 21 e SCL 22, com as linhas em dreno aberto e pull-up habilitado. Na validação desta montagem, o acesso por Wire retornava erros enquanto esse acesso por GPIO conseguiu identificar `WHO_AM_I=0x68` e ler os eixos durante trechos do teste. Também houve falhas de confirmação com GPIO, portanto essa alternativa ainda não garante comunicação estável. O jogo recebe `TILT:-1`, `TILT:0` e `TILT:1` a 9600 baud.

```cpp
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
```

A temporização por sinal é de 100 microssegundos, com intervalo de leitura de 50 ms. O controle usa uma média de cinco amostras, limiar de 3000 e histerese de 500. AX acima de 3500 produz `TILT:1`, abaixo de -3500 produz `TILT:-1` e dentro da faixa de -3000 a 3000 retorna ao centro. O estado é enviado a cada mudança e repetido a cada 250 ms.

`READY` e o primeiro `TILT:0` são enviados depois de uma leitura inicial completa dos eixos. As leituras têm até três tentativas. Em caso de falha persistente, o sketch envia um erro, neutraliza a direção e tenta inicializar novamente após um segundo, recuperando o barramento com pulsos de clock e uma condição STOP. A busca contempla os endereços `0x68` e `0x69`.

### Diagnóstico do sketch B v3

- `INFO: HeadRush sketch B v3 ... I2C=GPIO` identifica a versão gravada.
- `INFO: I2C_GPIO endereco=0x68 ACK=1` confirma resposta naquele endereço. `ACK=0` indica ausência de confirmação naquela tentativa.
- `INFO: WHO_AM_I=0x68` mostra a identidade lida do módulo. A leitura dos eixos também precisa funcionar para emitir `READY`.
- `ERRO: I2C etapa=DETECTAR` indica que nenhum dos dois endereços respondeu.
- `ERRO: MPU6050 etapa=ACORDAR` identifica uma falha na escrita do registrador de energia.
- `ERRO: MPU6050 etapa=LER_ACCEL` indica que as tentativas de leitura dos eixos falharam.
- `INFO: AX=... AY=... AZ=... MEDIA_X=... TILT=...` mostra os valores usados para decidir o movimento. A opção `MOSTRAR_LEITURAS` controla essa exibição periódica.

Abra o arquivo atualizado `arduino.sketches/sketch_nov9b/sketch_nov9b.ino` na IDE e selecione ESP32 Dev Module e a porta do dispositivo. Se a conexão para upload precisar de ajuda, segure BOOT enquanto aparece `Connecting` e solte quando a gravação começar. Depois do upload, deixe BOOT solto e pressione somente EN para executar o programa, caso a reinicialização automática precise ser repetida. No Monitor Serial a 9600 baud, confira o identificador `HeadRush sketch B v3`. Os símbolos breves do boot podem anteceder esse identificador porque o boot do ESP32 transmite a 115200 baud.

Na validação com a placa física, foram recebidos os três comandos pelo jogo. Também houve períodos de falha I2C e recuperação. A comunicação ainda precisa ser conferida durante o manuseio do conjunto.

O sketch executa e transmite independentemente do jogo. Feche o Monitor Serial e o Plotter Serial antes de iniciar o jogo para liberar a porta. Ao executar pela JVM do jogo, as linhas `INFO` e `ERRO` também aparecem no console.

## Etapa 7 — Calibrar

O controle depende da inclinação do eixo X. Na tela ArduinoGame, o módulo respondeu para ambos os lados quando foi segurado na orientação indicada pelo teste físico. Preserve essa orientação ao fixá-lo no acessório. Com a cabeça centralizada, AX deve ficar dentro da zona central, próximo de zero. Inclinar para cada lado deve produzir sinais opostos em AX. A posição do módulo à esquerda ou à direita da mesa, por si só, não define o comando.

Manter `TILT:1` equivale a manter a seta direita pressionada, e manter `TILT:-1` equivale à seta esquerda. O personagem continua se deslocando até receber `TILT:0` ou alcançar a borda. No jogo completo, o movimento é aplicado durante a partida ativa. A tecla R reinicia a partida depois de uma colisão.

O teste `ArduinoGame` e o jogo completo usam `ArduinoReaderThread.getDirecao()` para interpretar o sensor. Durante a partida ativa, o controle por sensor aplica a velocidade lateral imediatamente, como no quadrado verde. Ao receber `TILT:0` ou um erro do sensor, a contribuição do sensor para o movimento para na atualização seguinte. O teclado e o toque continuam disponíveis. O teste não tem obstáculos, pausa ou morte, enquanto o jogo completo respeita esses estados.

Abra o Monitor Serial (9600 baud). Deve aparecer `READY` e, ao inclinar a cabeça, `TILT:-1` e `TILT:1`.

| Sintoma | O que fazer |
| --- | --- |
| Nunca aparece `TILT:1` ou `TILT:-1` | Baixe o `THRESHOLD` (ex.: 1500). Confira também se o eixo X é o certo (Etapa 5) |
| Aparece sempre `TILT:1` ou `TILT:-1` mesmo parado | Suba o `THRESHOLD` (ex.: 5000). Se o AX estiver perto de 16000 em repouso, o eixo X está na vertical: gire o módulo |
| Alterna rápido entre valores | Suba o `DEADBAND` (ex.: 1000) e/ou o `N_MEDIA` (ex.: 8) |
| Esquerda e direita invertidas | Gire o módulo 180 graus ao fixá-lo |

Para calibrar melhor, use o sketch da Etapa 5: veja o valor do AX com a cabeça reta e com a inclinação que você quer que conte como movimento. O `THRESHOLD` deve ficar **entre os dois**. Como o jogo é de reabilitação, essa sensibilidade pode ser ajustada para o conforto de cada jogador.

## Etapa 8 — Testar a leitura no PC (sem o jogo)

Antes de ligar ao jogo, confirme que o PC recebe os dados. Este programa Java lista as portas e mostra por 10 segundos o que o ESP32 envia. Ele usa a biblioteca **jSerialComm** (se o projeto ainda não tiver, adicione `com.fazecast:jSerialComm` no `build.gradle`).

```java
package br.mackenzie;

import com.fazecast.jSerialComm.SerialPort;
import java.io.InputStream;

public class TesteSerial {
    public static void main(String[] args) throws Exception {
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort p : ports) {
            System.out.println(p.getSystemPortName() + " - " + p.getDescriptivePortName());
        }
        if (ports.length == 0) {
            System.out.println("Nenhuma porta encontrada");
            return;
        }

        // Passe o nome da porta como argumento (ex.: COM3). Sem argumento, usa a primeira.
        SerialPort port = ports[0];
        if (args.length > 0) {
            for (SerialPort p : ports) {
                if (p.getSystemPortName().equals(args[0])) port = p;
            }
        }

        port.setBaudRate(9600);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
        if (!port.openPort()) {
            System.out.println("Nao foi possivel abrir a porta " + port.getSystemPortName());
            return;
        }
        System.out.println("Lendo " + port.getSystemPortName() + " por 10 segundos...");
        InputStream in = port.getInputStream();
        long t = System.currentTimeMillis();
        while (System.currentTimeMillis() - t < 10000) {
            int b = in.read();
            if (b >= 0) System.out.print((char) b);
        }
        port.closePort();
    }
}
```

Rode o programa e incline o módulo. Deve aparecer `READY` (o ESP32 reinicia quando a porta abre) e depois `TILT:-1`, `TILT:0` e `TILT:1` conforme você inclina. **Feche o Monitor Serial da Arduino IDE antes**, senão a porta fica ocupada.

## Etapa 9 — Ligar ao jogo

1. Feche o Monitor Serial e o Plotter Serial da Arduino IDE para liberar a porta.
2. Descubra a porta do ESP32, por exemplo `/dev/ttyUSB0`, `/dev/ttyACM0` ou `COM3`.
3. Abra um terminal em `laboratorio/lab7/geope` e execute o comando com a porta encontrada.

```bash
./gradlew lwjgl3:run -Darduino.port=/dev/ttyACM0
```

No Windows, use o wrapper correspondente e a porta do Gerenciador de Dispositivos.

```bat
gradlew.bat lwjgl3:run -Darduino.port=COM3
```

O `build.gradle` de `lwjgl3` repassa `arduino.port` à JVM que executa o jogo. Sem essa opção, o padrão é `/dev/ttyACM0`, como no teste do quadrado verde. Ao executar diretamente pela IDE, configure `-Darduino.port=COM3` nas opções da JVM. Para o JAR, coloque a propriedade antes de `-jar`.

Ao abrir o jogo, confira no console `Serial aberta` com a porta selecionada e `9600 baud`. Ao inclinar o sensor, confira `Serial recebida` com `TILT:-1`, `TILT:0` ou `TILT:1`. A direção recebida permanece ativa até a próxima mensagem. O leitor libera DTR e RTS antes de abrir a porta, para manter as linhas de controle de EN e GPIO0 liberadas. A aplicação mantém um único leitor desde o menu inicial até o fechamento da janela, incluindo reinícios e trocas de tela. Feche o jogo para liberar a porta antes de abrir o Monitor Serial.

Se o USB for desconectado ou a porta estiver indisponível ao iniciar a partida, o leitor neutraliza o comando e tenta abrir novamente a porta configurada a cada segundo. Quando o ESP32 reaparece nessa porta, a leitura é retomada automaticamente. Se o sistema atribuir outro nome à porta, execute novamente o jogo com o novo valor de `arduino.port`. A tecla R reinicia apenas a partida.

Depois que a conexão com o dispositivo for detectada, a pista aguarda um `TILT` válido. Um erro do sensor ou uma desconexão suspende o avanço até a leitura ser recuperada. A tela mostra `AGUARDANDO SENSOR` e informa a direção ou o estado da conexão no rodapé. Uma inclinação válida tem prioridade sobre teclas e toques opostos. O toque usado para abrir a partida só passa a controlar o personagem depois de ser solto e pressionado novamente. Sem dispositivo detectado, a partida pode ser utilizada com os controles manuais.

Se a porta estiver incorreta ou ocupada, o console informa o problema e lista as portas disponíveis. Se aparecer `ERRO` enviado pelo ESP32, verifique a leitura do sensor pelo sketch. Se a porta abrir e nenhuma mudança de `TILT` aparecer, confira o sketch gravado, o eixo X e os limiares da Etapa 7.

## Etapa 10 — Fixar na cabeça

- **Fixe o módulo firme** (fita dupla face, velcro ou costurado numa tiara). Se ele balançar, vira ruído e o personagem treme.
- **Orientação:** na posição neutra (cabeça reta), o eixo X precisa ficar na horizontal. Confira com o sketch da Etapa 5 **depois de fixar**, e recalibre se precisar (Etapa 7).
- **Cabo USB:** prenda na gola da camiseta ou no ombro, com folga. Assim o peso do cabo fica no corpo e não puxa o módulo da cabeça.
- **Opção mais leve:** deixe só o MPU-6050 na cabeça e prenda o ESP32 na gola ou no ombro. Use jumpers curtos: a comunicação I2C não gosta de fios longos.
- **Proteção:** cubra a parte de baixo do módulo e as soldas com fita isolante, para não encostarem na pele, em metal ou no suor.

## Problemas comuns

| Sintoma | Causa provável | O que fazer |
| --- | --- | --- |
| Nenhuma porta aparece no PC | Cabo só de carga, driver ou porta USB | Troque o cabo, instale o driver CH9102, tente outra porta USB |
| Upload falha em `Connecting........____` | A placa não entrou em modo de gravação | Segure o botão **BOOT** durante o envio |
| `ERRO: MPU6050 nao respondeu` | Pinos sem solda, SDA e SCL trocados, VCC errado ou fio frouxo | Siga a lista de causas da Etapa 5, começando pela solda dos pinos |
| Valores travados ou zerados | Conexão I2C instável | Confira solda e fios, troque o jumper suspeito |
| `Port already in use` ou não abre a porta | Monitor Serial da Arduino IDE aberto | Feche o Monitor Serial |
| Personagem não mexe | `THRESHOLD` alto ou eixo errado | Etapas 5 e 7 |
| Personagem treme | Ruído ou módulo solto | Suba `DEADBAND` e `N_MEDIA` e fixe melhor o módulo |
| Dados chegam, mas o jogo ignora | Formato ou baud diferentes | Confirme `TILT:X` sem espaços e 9600 baud nos dois lados |
| Placa ou módulo esquentando | Curto-circuito | Desconecte o USB na hora e revise os fios |

## Checklist final

- Pinos do MPU-6050 soldados, sem pontes de solda.
- Jumpers conferidos (Etapa 2).
- VCC no 3V3, GND no GND, SDA no D21, SCL no D22.
- Arduino IDE com o suporte ao ESP32 (sem biblioteca extra para o sensor).
- Sketch de teste (Etapa 5) mostrando `Achei dispositivo em 0x68` e valores que mudam ao inclinar.
- Sketch do jogo (Etapa 6) enviando `TILT:-1`, `TILT:0` e `TILT:1`.
- `TesteSerial` (Etapa 8) mostrando as mensagens no PC.
- Porta configurada no jogo (Etapa 9) e módulo fixado na cabeça (Etapa 10).
