package org.hg.gamemanager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.hg.Main;
import org.hg.database.PlayerStatsCache;
import org.hg.gamemanager.events.*;
import org.hg.gamemanager.utils.CompassTarget;
import org.hg.utils.ConfigUtils;
import org.hg.utils.GetSettings;
import org.hg.utils.StringUtils;
import org.hg.utils.WorldManager.WorldGenerator;

import java.util.*;

import static org.bukkit.Bukkit.getLogger;

public class GameTimerManager {
    private static final Map<World, Integer> gameTime = new HashMap<>();
    private static final Set<World> registeredWorlds = new HashSet<>();

    private static final Main plugin = Main.getInstance();
    private static final ConfigUtils config = GetSettings.config;
    private static final ConfigUtils messages = GetSettings.messages;

    private static final int waitingSeconds;
    private static final int invincibleSeconds;
    private static final int endedSeconds;
    private static final int coliseuMinutes;
    private static final int minifeastMinutes;
    private static final int feastSpawnMinutes;

    private static boolean initialized = false;

    static {
        waitingSeconds = config.getConfig().getInt("Configuration.waiting_seconds");
        invincibleSeconds = config.getConfig().getInt("Configuration.invincible_seconds");
        endedSeconds = config.getConfig().getInt("Configuration.finished_seconds");
        coliseuMinutes = config.getConfig().getInt("Configuration.minutes_to_coliseu") * 60;
        minifeastMinutes = config.getConfig().getInt("Configuration.minifeast_spawn_minutes") * 60;
        feastSpawnMinutes = config.getConfig().getInt("Configuration.feast_spawn_minutes") * 60;
    }

    public static void init() {
        if (initialized) return;
        initialized = true;

        new BukkitRunnable() {
            @Override
            public void run() {
                tickAll();
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private static void tickAll() {
        for (World w : new HashSet<>(registeredWorlds)) {
            GameStats status = GameManager.getGameStats(w);

            switch (status) {
                case STARTING:
                    applyStartingTick(w);
                    break;
                case INVINCIBLE:
                    applyInvincibilityTick(w);
                    break;
                case GAME:
                    applyGameTick(w);
                    break;
                case ENDED:
                    applyEndTick(w);
                    break;
                default:
                    break;
            }
        }
    }

    // ========================================
    //  STARTING - Contagem regressiva
    // ========================================
    private static void applyStartingTick(World w) {
        int time = gameTime.getOrDefault(w, 0);
        gameTime.put(w, time - 1);

        // Envia mensagens configuradas para este tempo
        sendTimeMessages(w, time, "InGameMessages.DuringWaiting");

        if (GameManager.PlayersCountInGame(w) < GameManager.getMinPlayers()) {
            removeTimer(w);
            GameManager.setGameStats(w, GameStats.WAITING);
        }

        // Teleporta e dá itens quando chegar a 0
        if (time <= 0) {
            for (Player p : w.getPlayers()) {
                p.teleport(w.getSpawnLocation());
            }

            removeTimer(w);
            GameManager.setGameStats(w, GameStats.INVINCIBLE);
            startInvincibility(w);
            Bukkit.getPluginManager().callEvent(new GameStartEvent(w));
        }
    }

    // ========================================
    //  INVINCIBLE - Período de invencibilidade
    // ========================================
    private static void applyInvincibilityTick(World w) {
        int time = gameTime.getOrDefault(w, 0);

        // Envia mensagens configuradas para este tempo
        sendTimeMessages(w, time, "InGameMessages.DuringInvincible");

        if (time <= 0) {
            GameManager.setGameStats(w, GameStats.GAME);
        }
        gameTime.put(w, time - 1);
    }

    // ========================================
    //  GAME - Partida em andamento
    // ========================================
    private static void applyGameTick(World w) {
        // Verifica vitória
        if (GameManager.PlayersCountInGame(w) <= 1) {
            startEnd(w);
            Bukkit.getPluginManager().callEvent(new GameEndEvent(w));
            GameManager.setGameStats(w, GameStats.ENDED);
            return;
        }

        int time = gameTime.getOrDefault(w, 0) + 1;
        gameTime.put(w, time);

        // Atualiza bússola de todos os jogadores
        updateAllCompasses(w);

        // Executa eventos do jogo
        processGameEvents(w, time);
    }

    // ========================================
    //  ENDED - Partida finalizada
    // ========================================
    private static void applyEndTick(World w) {
        int time = gameTime.getOrDefault(w, 0) - 1;
        gameTime.put(w, time);

        if (time == 3) {
            // Expulsa todos os jogadores
            for (Player p : GameManager.GetPlayersInGame(w)) {
                PlayerStatsCache.addWin(p.getUniqueId());
            }
            for (Player p : new ArrayList<>(w.getPlayers())) {
                GameManager.exitGame(p);
            }
        }

        if (time <= 0) {
            removeTimer(w);
            WorldGenerator.RenewMap(true); // Descomente se necessário
        }
    }

    // ========================================
    //  HELPERS - Métodos auxiliares
    // ========================================

    /**
     * Envia mensagens configuradas no YAML para um determinado tempo
     */
    private static void sendTimeMessages(World w, int time, String sectionPath) {
        ConfigurationSection timeMessages = messages.getConfig().getConfigurationSection(sectionPath);

        if (timeMessages == null) {
            getLogger().warning("Seção '" + sectionPath + "' não encontrada!");
            return;
        }

        String timeKey = String.valueOf(time);

        // Verifica se existe configuração para este tempo
        if (!timeMessages.contains(timeKey)) {
            return; // Não há mensagem configurada
        }

        int playerCount = GameManager.PlayersCountInGame(w);

        // Obtém as mensagens (pode ser String única ou List)
        List<String> lines;
        Object value = timeMessages.get(timeKey);

        if (value instanceof List) {
            lines = timeMessages.getStringList(timeKey);
        } else if (value instanceof String) {
            lines = Collections.singletonList((String) value);
        } else {
            return; // Formato inválido
        }

        // Envia as mensagens para todos os jogadores do mundo
        for (Player p : w.getPlayers()) {
            for (String line : lines) {
                String formatted = line
                        .replace("{time}", StringUtils.calculateTime(time))
                        .replace("{players}", String.valueOf(playerCount))
                        .replace("&", "§");

                p.sendMessage(formatted);
            }
        }
    }

    /**
     * Atualiza a bússola de todos os jogadores
     */
    private static void updateAllCompasses(World w) {
        for (Player p : GameManager.GetPlayersInGame(w)) {
            Player target = CompassTarget.getNearestPlayer(p);
            if (target != null) {
                p.setCompassTarget(target.getLocation());
            }
        }
    }

    /**
     * Processa eventos do jogo (Feast, MiniFeast, Coliseu)
     */
    private static void processGameEvents(World w, int time) {
        // Coliseu
        if (time == coliseuMinutes) {
            Coliseu.setColiseu(w);
        }

        // MiniFeast
        if (time == minifeastMinutes) {
            MiniFeast.spawnMinifeast(w);
        }

        // Feast - Sistema completo
        processFeastEvents(w, time);
    }

    /**
     * Processa todos os eventos relacionados ao Feast
     */
    private static void processFeastEvents(World w, int time) {
        int timeUntilSpawn = feastSpawnMinutes - time;

        // Se não há tempo restante ou já passou, não faz nada
        if (timeUntilSpawn < 0) {
            return;
        }

        // Verifica se existe mensagem configurada para este tempo específico
        ConfigurationSection feastMessages = messages.getConfig().getConfigurationSection("InGameMessages.FeastMessages");

        if (feastMessages == null) {
            getLogger().warning("Seção 'FeastMessages' não encontrada!");
            return;
        }

        String timeKey = String.valueOf(timeUntilSpawn);

        // Se não há mensagem para este tempo, pula
        if (!feastMessages.contains(timeKey)) {
            return;
        }

        // Agenda o Feast na primeira mensagem (normalmente 300 segundos/5 minutos)
        if (Feast.getFeastCenter(w) == null) {
            Feast.scheduleFeast(w);
        }

        // Obtém as mensagens (pode ser String única ou List)
        List<String> lines;
        Object value = feastMessages.get(timeKey);

        if (value instanceof List) {
            lines = feastMessages.getStringList(timeKey);
        } else if (value instanceof String) {
            lines = Collections.singletonList((String) value);
        } else {
            return; // Formato inválido
        }

        // Envia as mensagens para todos os jogadores
        Location feastLoc = Feast.getFeastCenter(w);

        for (Player p : w.getPlayers()) {
            for (String line : lines) {
                String formatted = line
                        .replace("{timeUntilSpawn}", StringUtils.calculateTime(timeUntilSpawn))
                        .replace("&", "§");

                // Se há coordenadas do feast, substitui
                if (feastLoc != null) {
                    int x = feastLoc.getBlockX();
                    int z = feastLoc.getBlockZ();

                    formatted = formatted
                            .replace("{x1}", String.valueOf(x + 60))
                            .replace("{z1}", String.valueOf(z + 30))
                            .replace("{x2}", String.valueOf(x - 50))
                            .replace("{z2}", String.valueOf(z - 40));
                }

                p.sendMessage(formatted);
            }
        }

        // Spawn do Feast quando tempo chegar a 0
        if (timeUntilSpawn == 0) {
            Feast.spawnFeast(w);
        }
    }

    /**
     * Envia mensagem para todos os jogadores de um mundo
     */
    private static void broadcastToWorld(World w, String message) {
        w.getPlayers().forEach(p -> p.sendMessage(message));
    }

    // ========================================
    //  CONTROLE PÚBLICO
    // ========================================

    public static void startStarting(World w) {
        registeredWorlds.add(w);
        gameTime.put(w, waitingSeconds);
    }

    public static void startInvincibility(World w) {
        registeredWorlds.add(w);
        gameTime.put(w, invincibleSeconds);
    }

    public static void startEnd(World w) {
        registeredWorlds.add(w);
        gameTime.put(w, endedSeconds);
    }

    public static void removeTimer(World w) {
        gameTime.remove(w);
        registeredWorlds.remove(w);
    }

    public static int getTime(World w) {
        return gameTime.getOrDefault(w, 0);
    }
}