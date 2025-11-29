package org.hg.database;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.hg.Main;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sistema de cache de estatísticas com salvamento assíncrono imediato
 * Garante dados sempre atualizados sem causar lag no servidor
 */
public class PlayerStatsCache {

    private static final Plugin plugin = Main.getInstance();

    // Cache para leitura rápida (ConcurrentHashMap = thread-safe)
    private static final Map<UUID, CachedStats> statsCache = new ConcurrentHashMap<>();

    /**
     * Carrega as estatísticas de um jogador do banco (assíncrono)
     * Chamado no PlayerJoinEvent
     */
    public static void loadPlayer(UUID uuid) {
        if (statsCache.containsKey(uuid)) {
            return; // Já está carregado
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int kills = Main.getPlayerRepo().getKills(uuid);
                int deaths = Main.getPlayerRepo().getDeaths(uuid);
                int wins = Main.getPlayerRepo().getWins(uuid);
                int money = Main.getPlayerRepo().getMoney(uuid);

                statsCache.put(uuid, new CachedStats(kills, deaths, wins, money));

            } catch (Exception e) {
                plugin.getLogger().warning("§cErro ao carregar stats de " + uuid + ": " + e.getMessage());
            }
        });
    }

    // ==================== KILLS ====================

    /**
     * Incrementa kill e salva IMEDIATAMENTE no banco (assíncrono)
     */
    public static void addKill(UUID uuid) {
        CachedStats stats = statsCache.computeIfAbsent(uuid, k -> new CachedStats(0, 0, 0, 0));
        stats.kills++;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Main.getPlayerRepo().addKill(uuid);
            } catch (Exception e) {
                plugin.getLogger().warning("§cErro ao salvar kill de " + uuid + ": " + e.getMessage());
            }
        });
    }

    /**
     * Obtém kills do cache (instantâneo, 0ms)
     */
    public static int getKills(UUID uuid) {
        CachedStats stats = statsCache.get(uuid);
        return stats != null ? stats.kills : 0;
    }

    // ==================== DEATHS ====================

    /**
     * Incrementa death e salva IMEDIATAMENTE no banco (assíncrono)
     */
    public static void addDeath(UUID uuid) {
        CachedStats stats = statsCache.computeIfAbsent(uuid, k -> new CachedStats(0, 0, 0, 0));
        stats.deaths++;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Main.getPlayerRepo().addDeath(uuid);
            } catch (Exception e) {
                plugin.getLogger().warning("§cErro ao salvar death de " + uuid + ": " + e.getMessage());
            }
        });
    }

    /**
     * Obtém deaths do cache (instantâneo, 0ms)
     */
    public static int getDeaths(UUID uuid) {
        CachedStats stats = statsCache.get(uuid);
        return stats != null ? stats.deaths : 0;
    }

    // ==================== WINS ====================

    /**
     * Incrementa win e salva IMEDIATAMENTE no banco (assíncrono)
     */
    public static void addWin(UUID uuid) {
        CachedStats stats = statsCache.computeIfAbsent(uuid, k -> new CachedStats(0, 0, 0, 0));
        stats.wins++;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Main.getPlayerRepo().addWin(uuid);
            } catch (Exception e) {
                plugin.getLogger().warning("§cErro ao salvar win de " + uuid + ": " + e.getMessage());
            }
        });
    }

    /**
     * Obtém wins do cache (instantâneo, 0ms)
     */
    public static int getWins(UUID uuid) {
        CachedStats stats = statsCache.get(uuid);
        return stats != null ? stats.wins : 0;
    }

    /**
     * Adiciona jogo jogado (derrota) e salva IMEDIATAMENTE
     */
    public static void addGamePlayed(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Main.getPlayerRepo().addGamePlayed(uuid);
            } catch (Exception e) {
                plugin.getLogger().warning("§cErro ao salvar game played de " + uuid + ": " + e.getMessage());
            }
        });
    }

    // ==================== MONEY (MOEDAS) ====================

    /**
     * Adiciona moedas ao jogador (instantâneo no cache, salva assíncrono)
     */
    public static void addMoney(UUID uuid, int amount) {
        CachedStats stats = statsCache.computeIfAbsent(uuid, k -> new CachedStats(0, 0, 0, 0));
        stats.money += amount;

        // Salva no banco de forma assíncrona
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Main.getPlayerRepo().addMoney(uuid, amount);
            } catch (Exception e) {
                plugin.getLogger().warning("§cErro ao adicionar moedas de " + uuid + ": " + e.getMessage());
            }
        });
    }

    /**
     * Remove moedas do jogador (instantâneo no cache, salva assíncrono)
     * Retorna true se tinha dinheiro suficiente, false caso contrário
     */
    public static boolean removeMoney(UUID uuid, int amount) {
        CachedStats stats = statsCache.get(uuid);

        if (stats == null || stats.money < amount) {
            return false; // Não tem dinheiro suficiente
        }

        stats.money -= amount;

        // Salva no banco de forma assíncrona
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Main.getPlayerRepo().removeMoney(uuid, amount);
            } catch (Exception e) {
                plugin.getLogger().warning("§cErro ao remover moedas de " + uuid + ": " + e.getMessage());
            }
        });

        return true;
    }

    /**
     * Define o dinheiro do jogador (sobrescreve o valor atual)
     */
    public static void setMoney(UUID uuid, int amount) {
        CachedStats stats = statsCache.computeIfAbsent(uuid, k -> new CachedStats(0, 0, 0, 0));
        stats.money = amount;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Main.getPlayerRepo().setMoney(uuid, amount);
            } catch (Exception e) {
                plugin.getLogger().warning("§cErro ao definir moedas de " + uuid + ": " + e.getMessage());
            }
        });
    }

    /**
     * Obtém moedas do cache (instantâneo, 0ms)
     */
    public static int getMoney(UUID uuid) {
        CachedStats stats = statsCache.get(uuid);
        return stats != null ? stats.money : 0;
    }

    /**
     * Verifica se o jogador tem dinheiro suficiente (instantâneo, 0ms)
     */
    public static boolean hasMoney(UUID uuid, int amount) {
        return getMoney(uuid) >= amount;
    }

    /**
     * Obtém moedas formatadas com separador de milhar
     */
    public static String getFormattedMoney(UUID uuid) {
        return String.format("%,d", getMoney(uuid));
    }

    // ==================== KDR ====================

    /**
     * Calcula KDR do cache (instantâneo, 0ms)
     */
    public static double getKDR(UUID uuid) {
        CachedStats stats = statsCache.get(uuid);
        if (stats == null) return 0.0;

        return stats.deaths == 0 ? stats.kills : (double) stats.kills / stats.deaths;
    }

    /**
     * Formata KDR com 2 casas decimais
     */
    public static String getFormattedKDR(UUID uuid) {
        return String.format("%.2f", getKDR(uuid));
    }

    // ==================== GERENCIAMENTO DO CACHE ====================

    /**
     * Remove jogador do cache (chamado no PlayerQuitEvent)
     * Não precisa salvar pois já salvou em tempo real
     */
    public static void unloadPlayer(UUID uuid) {
        statsCache.remove(uuid);
    }

    /**
     * Limpa todo o cache (chamado no onDisable)
     */
    public static void shutdown() {
        plugin.getLogger().info("§7[Cache] Limpando cache de estatísticas...");
        statsCache.clear();
        plugin.getLogger().info("§a✓ Cache limpo com sucesso!");
    }

    /**
     * Verifica se um jogador está no cache
     */
    public static boolean isLoaded(UUID uuid) {
        return statsCache.containsKey(uuid);
    }

    /**
     * Obtém o tamanho do cache (para debug)
     */
    public static int getCacheSize() {
        return statsCache.size();
    }

    // ==================== CLASSE INTERNA ====================

    /**
     * Classe para armazenar estatísticas em memória
     */
    private static class CachedStats {
        int kills;
        int deaths;
        int wins;
        int money;

        CachedStats(int kills, int deaths, int wins, int money) {
            this.kills = kills;
            this.deaths = deaths;
            this.wins = wins;
            this.money = money;
        }
    }
}