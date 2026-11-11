# Teste do quadrado verde

Este diretório preserva o jogo simples usado na sessão em que o quadrado respondeu às inclinações do MPU6050. A janela tem fundo azul e um quadrado verde que se move para os lados.

O movimento usa diretamente as mensagens `TILT:-1`, `TILT:0` e `TILT:1`, a 9600 baud. A lógica visual foi recuperada da revisão `be59ae1`, anterior à unificação de `getDirecao()`, e o launcher reproduz a janela de teste de 1000 por 600 pixels. O leitor serial e o listador de portas foram copiados dessa mesma revisão para manter os fontes do teste neste diretório.

## Executar

O teste usa JDK 17. O arquivo `gradle/gradle-daemon-jvm.properties`, copiado do projeto principal, orienta o Gradle a usar essa versão e fornece as URLs de provisionamento para os sistemas contemplados. Feche o jogo principal e o Monitor/Plotter Serial da Arduino IDE para liberar a porta do ESP32.

Na raiz do repositório, execute no Linux

```bash
bash laboratorio/lab7/geope/gradlew -p tests run -Darduino.port=/dev/ttyACM0
```

No Windows, ajuste a porta de acordo com o Gerenciador de Dispositivos

```bat
laboratorio\lab7\geope\gradlew.bat -p tests run -Darduino.port=COM3
```

Para compilar sem abrir a janela

```bash
bash laboratorio/lab7/geope/gradlew -p tests build
```

O projeto usa o Gradle Wrapper já existente no repositório e declara suas próprias dependências em `build.gradle`.

## Usar o controle

- `TILT:-1` move o quadrado para a esquerda.
- `TILT:1` move o quadrado para a direita.
- `TILT:0` para o quadrado.
- O quadrado permanece dentro das bordas da janela.

Incline o sensor como uma gangorra, usando a orientação em que AX varia nos dois sentidos. A posição do módulo em relação à mesa não determina o comando. O teste inicia diretamente e pode ser encerrado ao fechar a janela.

O firmware correspondente está em `arduino.sketches/sketch_nov9b/sketch_nov9b.ino`, na raiz do repositório. O console mostra as mensagens recebidas e os erros enviados pela placa. A sessão física apresentou resposta nos dois sentidos e períodos de falha I2C, que continuam registrados como limitação da montagem testada.

## Arquivos

- `src/main/java/br/mackenzie/ArduinoGame.java` contém o desenho e o movimento do quadrado.
- `src/main/java/br/mackenzie/ArduinoDemoLauncher.java` abre a janela de teste.
- `src/main/java/br/mackenzie/ArduinoReaderThread.java` lê a porta serial e disponibiliza o último comando válido.
- `src/main/java/br/mackenzie/SerialPortLister.java` lista as portas disponíveis.
- `build.gradle` configura as dependências, a compilação e a execução.
- `settings.gradle` identifica este projeto Gradle.
- `gradle/gradle-daemon-jvm.properties` seleciona o Java 17 para o Gradle.
- `.gitignore` exclui os arquivos gerados pelo Gradle.
