# HG — Hunger Games Plugin (Spigot/Minecraft)

Plugin para servidores de Minecraft (Spigot/Bukkit, API 1.8.8) que implementa o minigame Hunger Games: partidas simultâneas em múltiplos mapas, sistema de kits, economia/estatísticas de jogador e integração com PlaceholderAPI.

Projeto em desenvolvimento — algumas funcionalidades ainda estão incompletas ou em ajuste.

---

## 🎮 Funcionalidades

- **Matchmaking multi-mapa**: distribui jogadores entre vários mundos disponíveis simultaneamente, priorizando partidas com mais jogadores para preenchê-las primeiro.
- **Estados de partida**: controle de ciclo de vida da partida por mundo (aguardando, iniciando, em andamento), com timers dedicados (`GameTimerManager`).
- **Sistema de kits**: seleção e aplicação de kits por jogador (`KitAPI`, com inventários e kits configuráveis).
- **Estatísticas e economia de jogador**: kills, mortes, vitórias, partidas jogadas e moedas, com cache em memória (`ConcurrentHashMap`) para leitura instantânea e gravação assíncrona no banco, evitando travar a thread principal do servidor.
- **Persistência configurável**: suporte a MySQL ou SQLite, selecionável via `config.yml`, com fallback automático para SQLite em caso de configuração inválida.
- **Integração com PlaceholderAPI**: expõe placeholders (`hg_coins`, `hg_wins`, `hg_kills`) para uso em scoreboards, hologramas e outros plugins.
- **Utilitários de jogo**: geração/renovação de mapas (`WorldGenerator`), controle de visibilidade de jogadores, itens de espera, action bar e mensagens customizadas.

---

## 🛠️ Tecnologias

- Java 8
- Spigot API 1.8.8 (Bukkit)
- Maven (com `maven-shade-plugin` para empacotamento do `.jar`)
- MySQL Connector/J e SQLite (JDBC)
- PlaceholderAPI

---

## 🏗️ Arquitetura

Código organizado em pacotes por responsabilidade:

- `commands` — comandos e subcomandos do plugin.
- `database` — conexão com banco (`DatabaseManager`), repositório de jogador (`PlayerRepository`) e caches de estatísticas/kits com escrita assíncrona.
- `gamemanager` — matchmaking, estado das partidas, timers e eventos do jogo.
- `kitmanager` — kits, inventários e API de aplicação de kits.
- `listeners` — eventos de jogador (Bukkit).
- `utils` — utilitários (configuração, geração de mundo, itens, action bar, strings).

O plugin também depende de uma biblioteca própria (`lobbyutils`), reaproveitada de outro projeto para gerenciamento do lobby.

---

## ⚙️ Como Compilar

```bash
mvn clean package
```

O `.jar` final (com dependências empacotadas via shade plugin) é gerado em `target/`.

---

## 🚧 Status

Projeto em desenvolvimento. Funcionalidades centrais (matchmaking, kits, estatísticas, persistência) já estão implementadas; itens como configuração completa de kits, balanceamento de economia e testes ainda estão em andamento.

---

## 📄 Licença

Projeto pessoal, distribuído para fins de estudo e uso interno.
