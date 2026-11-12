# Retomar o desenvolvimento do HeadRush

Estado registrado em 12 de novembro de 2026.

## Repositório e branch

- Repositório `danielzba-cmd/jogosdigitaisprojeto-gpt.patch`.
- Branch de trabalho `poc2`.
- A integração do controle foi salva na revisão `839105c`.
- As regras do projeto estão em `AGENTS.md`.

## Continuar pelo celular ou pelo navegador

A documentação do Codex Cloud orienta iniciar uma conversa e selecionar `Work in > Cloud` na web ou no aplicativo desktop. No aplicativo móvel, a entrada é `Codex`. Um ambiente padrão pode receber um pedido para abrir um repositório acessível pela conta GitHub conectada. Cada tarefa de nuvem tem seu próprio espaço de trabalho e pode executar enquanto o computador pessoal está desligado.

Fonte oficial consultada em 12 de novembro de 2026

https://learn.chatgpt.com/docs/cloud

Na consulta realizada nesta sessão, o ambiente padrão estava disponível para execução. O ambiente salvo com o nome do repositório aparecia com execução desabilitada e referência `main`. Escolha o ambiente padrão e informe explicitamente o repositório e a branch `poc2` no pedido. A disponibilidade e os nomes das opções dependem da conta e da versão do aplicativo.

Copie este pedido para retomar em uma nova conversa de nuvem

```text
Continue o projeto HeadRush no repositório
https://github.com/danielzba-cmd/jogosdigitaisprojeto-gpt.patch
Use exclusivamente a branch poc2. Leia AGENTS.md e
documentacao/RETOMAR_PROJETO.md antes de alterar arquivos.

É um jogo Java com libGDX e Gradle, controlado por ESP32 e MPU6050.
A integração de software passou nos testes com porta serial virtual.
O dispositivo está guardado, e a validação física continua pendente.

Confira o estado atual da poc2 e me apresente um resumo para retomarmos
o desenvolvimento. Execute ./gradlew build dentro de
laboratorio/lab7/geope ao verificar a compilação. Para os testes gráficos,
prepare uma sessão gráfica ou um display virtual, conforme o ambiente.
```

Uma nova conversa pode recuperar o contexto técnico por este arquivo e pelo código. Para usar a mesma cópia local do projeto pelo celular, a alternativa é o Remote do aplicativo, que depende de parear o aparelho e manter o computador ligado, conectado e sem suspensão.

O procedimento oficial de pareamento fica em `Settings > Connections > Control this Mac or PC > Set up ou Add`. O usuário deve escanear o QR code com o celular e utilizar a mesma conta e workspace.

https://developers.openai.com/training/walkthroughs/working-on-your-phone

## Projeto e arquivos principais

- `laboratorio/lab7/geope` contém o projeto Gradle do jogo principal.
- `core` contém as telas, o estado da partida e o leitor serial.
- `lwjgl3` contém a inicialização desktop e a tarefa de verificação da integração.
- `tests` contém o jogo preservado do quadrado verde e o programa de verificação com porta virtual.
- `arduino.sketches/sketch_nov9b/sketch_nov9b.ino` contém o sketch B revisado.
- `documentacao/Short_Paper_HeadRush.docx` contém o artigo editável com placeholders para dois autores.

## Comandos de desenvolvimento

Na raiz do repositório, compile o jogo principal

```bash
bash laboratorio/lab7/geope/gradlew -p laboratorio/lab7/geope build
```

Execute o jogo principal em uma sessão gráfica

```bash
bash laboratorio/lab7/geope/gradlew -p laboratorio/lab7/geope lwjgl3:run -Darduino.port=/dev/ttyACM0
```

Compile ou execute o quadrado verde

```bash
bash laboratorio/lab7/geope/gradlew -p tests build
bash laboratorio/lab7/geope/gradlew -p tests run -Darduino.port=/dev/ttyACM0
```

O Gradle está configurado para Java 17. Na primeira utilização, o ambiente pode precisar baixar o JDK, a distribuição do Gradle e as dependências declaradas nos projetos.

## Verificação sem o dispositivo físico

Em Linux com Python 3 e uma sessão gráfica disponível, execute

```bash
python3 tests/verificar_integracao.py
```

Em um ambiente Linux com Xvfb já instalado, a verificação também pode ser iniciada por um display virtual

```bash
xvfb-run -a python3 tests/verificar_integracao.py
```

O programa cria uma porta serial virtual e exercita o leitor, o menu e a renderização do jogo principal. A execução concluída com sucesso imprime `PASS ALL`.

A última verificação local cobriu os três comandos, entrada por toque, prioridade do sensor sobre uma tecla oposta, parada, limite lateral, pausa, colisão, reinício por tecla e toque, retorno ao menu, mensagens inválidas, falha de leitura e desconexão seguida de reconexão. O build principal e o build do quadrado verde também passaram.

## Estado atual da integração

A porta padrão do jogo principal é `/dev/ttyACM0`, com configuração por `-Darduino.port`. O protocolo usa 9600 baud e mensagens `TILT:-1`, `TILT:0` e `TILT:1` terminadas por quebra de linha.

`Main` mantém um único `ArduinoReaderThread` desde o menu inicial até o fechamento da aplicação. `TelaJogo` reutiliza esse leitor em novas partidas. Uma direção não neutra do sensor tem prioridade sobre teclas e toques opostos. O toque que abre ou reinicia a partida precisa ser solto antes de funcionar como controle lateral.

Após detectar a conexão com o dispositivo, o jogo aguarda uma leitura válida. Erros e desconexões suspendem o avanço da pista até a recuperação. O rodapé informa o estado da conexão e a direção recebida. Se nenhum dispositivo for detectado desde a abertura da aplicação, os controles manuais permitem jogar.

## Validação física pendente

Durante os testes anteriores, a placa transmitiu os três comandos e o quadrado verde respondeu nos dois sentidos em uma sessão. Também houve períodos sem confirmação do endereço I2C e sem leitura dos eixos. A causa exata dessa intermitência permaneceu indeterminada. O usuário confirmou que a barra de pinos foi soldada e informou que não tem multímetro nem outros jumpers para comparação.

O dispositivo está guardado. As correções mais recentes foram verificadas com uma porta serial virtual. A retomada deve preservar essa distinção ao relatar os resultados e reservar a confirmação do conjunto ESP32 e MPU6050 para quando ele estiver conectado ao computador que executa o teste.

Na nuvem, a porta `/dev/ttyACM0` do computador pessoal não fica disponível automaticamente. É possível continuar o trabalho de software e executar a simulação. O teste físico depende de um host com o dispositivo conectado.
