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
5. **Não precisa instalar biblioteca do sensor.** Os sketches deste guia conversam com o MPU-6050 direto pela biblioteca `Wire`, que já vem com o suporte ao ESP32.
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

- Primeiro aparece `Achei dispositivo em 0x68` (ou `0x69`). Isso prova que fios, solda e alimentação estão certos.
- Depois aparece `WHO_AM_I: 0x68`. Alguns módulos trazem um chip "clone" que responde `0x70`, `0x72` ou `0x98`. **Isso é normal e funciona igual**: o que importa é o sensor ter sido encontrado.
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

Este sketch lê o eixo X, suaviza o ruído com uma **média móvel**, aplica uma **zona morta (histerese)** para não ficar oscilando e envia `TILT:-1`, `TILT:0` ou `TILT:1` pela serial a **9600 baud**.

```cpp
#include <Wire.h>

const uint8_t MPU_ADDR = 0x68;   // se o AD0 estiver no 3V3, use 0x69

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
    Serial.println("TILT:0");
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

    if (dir != dirAnterior || millis() - ultimoTilt >= REENVIO_MS) {
        Serial.print("TILT:");
        Serial.println(dir);
        dirAnterior = dir;
        ultimoTilt = millis();
    }
}
```

**Como entender os números:**

- Na escala padrão do sensor, **1 g = 16384**. Então um `THRESHOLD` de 3000 equivale a uma inclinação de uns **10 graus**.
- O sistema só passa para `TILT:1` ou `TILT:-1` quando o valor ultrapassa 3500 (uns 12 graus) e só volta ao centro quando cai abaixo de 3000.
- O ESP32 envia `TILT:0` ao iniciar, transmite imediatamente cada mudança e repete o estado atual a cada 250 ms. Isso permite sincronizar o jogo mesmo quando ele abre a porta depois de uma mudança de inclinação.
- Além do `TILT`, o ESP32 envia `READY` ao ligar e `ERRO: ...` se o sensor falhar. O jogo mostra essas mensagens no console e retorna a direção para centro (0) ao recebê-las.

**Esta versão é mais robusta que a anterior:** ela conversa com o sensor direto pela biblioteca `Wire`, sem verificar o código de identidade (módulos com chip "clone" eram rejeitados por essa verificação, mesmo funcionando). Também **não trava** se o sensor falhar: ela avisa com `ERRO: MPU6050 nao respondeu` a cada segundo, solta o personagem (`TILT:0`) e volta a funcionar sozinha quando o contato voltar.

## Etapa 7 — Calibrar

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
./gradlew lwjgl3:run -Darduino.port=/dev/ttyUSB0
```

No Windows, use o wrapper correspondente e a porta do Gerenciador de Dispositivos.

```bat
gradlew.bat lwjgl3:run -Darduino.port=COM3
```

O `build.gradle` de `lwjgl3` repassa `arduino.port` à JVM que executa o jogo. Sem essa opção, o padrão é `/dev/ttyUSB0`. Ao executar diretamente pela IDE, configure `-Darduino.port=COM3` nas opções da JVM. Para o JAR, coloque a propriedade antes de `-jar`.

Ao iniciar uma partida, confira no console `Serial aberta` com a porta selecionada e `9600 baud`. Ao inclinar o sensor, confira `Serial recebida` com `TILT:-1`, `TILT:0` ou `TILT:1`. A direção recebida permanece ativa até a próxima mensagem. O leitor é encerrado ao voltar ao menu ou fechar o jogo, liberando a porta para a próxima partida.

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
