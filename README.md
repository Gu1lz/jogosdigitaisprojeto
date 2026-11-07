# jogosdigitaisprojeto — Projeto IoT

## Jogo HeadRush libGDX

### 1. Visão geral

Desenvolvimento de um jogo digital inspirado em **Geometry Dash**, com foco em acessibilidade e apoio à reabilitação física. O projeto utilizará movimentos da cabeça como forma de interação, por meio de um dispositivo IoT capaz de coletar dados de movimento.

O objetivo é criar uma experiência gamificada na qual o jogador controla um personagem e supera obstáculos enquanto o sistema registra métricas de desempenho.

### 2. Objetivo

Desenvolver um jogo acessível que permita controlar um personagem por meio dos movimentos da cabeça, oferecendo uma alternativa aos controles convencionais e explorando o potencial dos jogos digitais como ferramenta de apoio à reabilitação.

### 3. Público-alvo e acessibilidade

O projeto tem como público-alvo pessoas com paralisia cerebral infantil e dificuldades motoras que possam se beneficiar de uma interface controlada pelos movimentos da cabeça.

A sensibilidade dos controles e a dificuldade deverão considerar as necessidades individuais de cada jogador.

### 4. Tecnologias e dispositivos

- **Linguagem:** Java.
- **Framework:** libGDX.
- **IoT:** sensor de movimento com acelerômetro, a definir.
- **Controle:** captura e interpretação dos movimentos da cabeça.
- **Versionamento:** Git e GitHub.

### 5. Funcionamento do jogo

O dispositivo IoT captará os movimentos da cabeça e enviará os dados ao jogo. Esses dados serão utilizados para controlar as ações do personagem.

O jogo contará com obstáculos, progressão de dificuldade e registro de métricas, como velocidade e consistência dos movimentos, para acompanhar o desempenho do jogador ao longo das fases.

Inicialmente, a mecânica poderá ser desenvolvida com comandos simulados. Na versão final, o controle deverá ser integrado ao dispositivo IoT.

### 6. Fases e dificuldade progressiva

O jogo contará com **6 fases**, com dificuldade progressiva.

A dificuldade poderá variar de acordo com os movimentos registrados pelo sensor e o desempenho do jogador. A adaptação deverá considerar a consistência dos comandos e a taxa de acertos, evitando que movimentos involuntários aumentem automaticamente o desafio.

### 7. Funcionalidades previstas

- Menu principal com opções para iniciar, pausar e reiniciar o jogo.
- Personagem principal controlado por movimentos da cabeça.
- Seis fases com obstáculos e dificuldade progressiva.
- Integração com dispositivo IoT para captura dos movimentos.
- Registro de métricas de desempenho e pontuação.
- Ajustes de sensibilidade para tornar os controles mais acessíveis.

### 8. Validação

A validação será realizada por meio de testes de funcionamento do jogo, integração com o sensor, precisão dos comandos e progressão da dificuldade.

A adequação ao público-alvo e os possíveis benefícios para a reabilitação deverão ser avaliados posteriormente. O protótipo, por si só, não comprova eficácia clínica.

### 9. Entregáveis

- Código-fonte completo do projeto.
- Executável ou APK do jogo.
- Short paper de até seis páginas, contendo a descrição do problema, a justificativa, as funcionalidades implementadas e capturas de tela do jogo.
- Commits semanais no GitHub.
- Acompanhamento do desenvolvimento por meio de um quadro Kanban.

### 10. Prazo de entrega

**Data limite: 11 de novembro de 2026.**
