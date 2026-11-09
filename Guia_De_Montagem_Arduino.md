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
5. Em `Sketch → Include Library → Manage Libraries`, procure **MPU6050** e instale **"MPU6050 by Electronic Cats"**. Instale só essa: o código deste guia **não funciona** com a biblioteca da Adafruit.
6. **Driver da porta** (o chip da placa é o CH9102X):
   - **Windows:** com o ESP32 plugado, abra o Gerenciador de Dispositivos e veja em "Portas (COM e LPT)". Se aparecer um dispositivo desconhecido, instale o driver **CH343/CH9102** do fabricante (WCH). Em computador da faculdade, pode ser preciso pedir autorização.
   - **Linux:** rode `sudo usermod -aG dialout $USER` e faça logout e login. A porta costuma ser `/dev/ttyUSB0` ou `/dev/ttyACM0`.
7. Em `Tools → Port`, escolha a porta do ESP32. Se estiver em dúvida qual é, desconecte o cabo, veja a lista, reconecte e veja qual porta apareceu.

## Etapa 5 — Teste rápido: o sensor está funcionando?

Antes do código do jogo, confirme que o ESP32 enxerga o sensor e descubra qual eixo usar. Grave este sketch de teste:

```cpp
#include <Wire.h>
#include <MPU6050.h>

MPU6050 mpu;

void setup() {
    Serial.begin(9600);
    Wire.begin(21, 22);   // SDA = 21, SCL = 22
    mpu.initialize();
    if (!mpu.testConnection()) {
        Serial.println("ERRO: MPU6050 nao respondeu");
        while (1) { delay(1000); }
    }
    Serial.println("MPU6050 OK");
}

void loop() {
    int16_t ax, ay, az;
    mpu.getAcceleration(&ax, &ay, &az);
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

- Deve aparecer `MPU6050 OK` e, depois, linhas com `AX`, `AY` e `AZ`.
- Com o módulo parado, **um dos três valores fica perto de 16000** (o eixo que aponta para cima ou para baixo) e os outros ficam perto de zero.
- Ao inclinar o módulo, os valores mudam de forma suave.
- Se aparecer `ERRO: MPU6050 nao respondeu`, desconecte o USB e revise a Etapa 3 e as soldas.

**Importante:** o código do jogo usa o eixo **X (AX)**. Para a inclinação lateral da cabeça funcionar, o AX precisa ficar **perto de zero com a cabeça reta** e variar quando você inclina para os lados. Se o AX estiver perto de 16000 com a cabeça reta, gire o módulo ao fixá-lo (Etapa 10) ou troque `ax` por `ay` no código.

## Etapa 6 — Código que envia a inclinação para o jogo

Este sketch lê o eixo X, suaviza o ruído com uma **média móvel**, aplica uma **zona morta (histerese)** para não ficar oscilando e envia `TILT:-1`, `TILT:0` ou `TILT:1` pela serial a **9600 baud**.

```cpp
#include <Wire.h>
#include <MPU6050.h>

MPU6050 mpu;

// --- Parâmetros de calibração ---
const int   THRESHOLD   = 3000;   // sensibilidade (ajuste)
const int   N_MEDIA     = 5;      // tamanho da média móvel
const int   DEADBAND    = 500;    // histerese para não oscilar
const unsigned long INTERVALO_MS = 20;  // 50 leituras por segundo

int buf[N_MEDIA];
int idx = 0;
int dirAnterior = 0;
unsigned long ultimoEnvio = 0;

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

    if (dir != dirAnterior) {
        Serial.print("TILT:");
        Serial.println(dir);
        dirAnterior = dir;
    }
}
```

**Como entender os números:**

- Na escala padrão do sensor, **1 g = 16384**. Então um `THRESHOLD` de 3000 equivale a uma inclinação de uns **10 graus**.
- O sistema só passa para `TILT:1` ou `TILT:-1` quando o valor ultrapassa 3500 (uns 12 graus) e só volta ao centro quando cai abaixo de 3000.
- O ESP32 só envia uma mensagem quando o estado **muda**, para gerar menos tráfego. Por isso, o jogo deve **começar assumindo o estado centro (0)**.
- Além do `TILT`, o ESP32 envia `READY` ao ligar e `ERRO: ...` se o sensor falhar. O jogo precisa ignorar essas linhas.

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

O jogo lê a porta pelo `ArduinoReaderThread`. Para ele saber qual porta usar:

1. Descubra o nome da porta (ex.: `COM3` no Windows, `/dev/ttyUSB0` no Linux). Ele muda de um computador para outro.
2. Passe o nome como propriedade ao rodar o jogo, nas **VM options** da sua IDE:

```
-Darduino.port=COM3
```

3. Ou fixe no `build.gradle` do módulo `lwjgl3`:

```gradle
application {
    applicationDefaultJvmArgs = ["-Darduino.port=COM3"]
}
```

4. Rode o jogo e incline a cabeça: o personagem deve se mover.

**Atenção:** isso só funciona se o `ArduinoReaderThread` do projeto realmente ler a propriedade `arduino.port`, usar **9600 baud** e interpretar só as linhas que começam com `TILT:`. Confirme com quem escreveu essa classe.

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
| `ERRO: MPU6050 nao respondeu` | SDA e SCL trocados, VCC errado ou solda ruim | Refaça a conferência da Etapa 3 e revise as soldas |
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
- Arduino IDE com o suporte ESP32 e a biblioteca "MPU6050 by Electronic Cats".
- Sketch de teste (Etapa 5) mostrando `MPU6050 OK` e valores que mudam ao inclinar.
- Sketch do jogo (Etapa 6) enviando `TILT:-1`, `TILT:0` e `TILT:1`.
- `TesteSerial` (Etapa 8) mostrando as mensagens no PC.
- Porta configurada no jogo (Etapa 9) e módulo fixado na cabeça (Etapa 10).
