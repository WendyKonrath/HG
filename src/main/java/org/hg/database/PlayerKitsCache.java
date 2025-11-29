package org.hg.database;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.hg.Main;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sistema de cache para kits dos jogadores
 * Evita queries excessivas ao banco de dados
 */
public class PlayerKitsCache {

    private static final Plugin plugin = Main.getInstance();

    // Cache: UUID -> Set de kits que o jogador possui
    private static final Map<UUID, Set<String>> kitsCache = new ConcurrentHashMap<>();

    // Rastreamento de mudanças pendentes para salvar
    private static final Map<UUID, Boolean> pendingUpdates = new ConcurrentHashMap<>();

    /**
     * Carrega os kits de um jogador do banco (assíncrono)
     * Chamado no PlayerJoinEvent
     */
    public static void loadPlayer(UUID uuid) {
        if (kitsCache.containsKey(uuid)) {
            return; // Já está carregado
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<String> kits = Main.getPlayerRepo().getKits(uuid);
                kitsCache.put(uuid, new HashSet<>(kits));

            } catch (Exception e) {
                plugin.getLogger().warning("§cErro ao carregar kits de " + uuid + ": " + e.getMessage());
                kitsCache.put(uuid, new HashSet<>()); // Cache vazio em caso de erro
            }
        });
    }

    /**
     * Verifica se o jogador tem um kit (instantâneo, lê do cache)
     */
    public static boolean hasKit(UUID uuid, String kitName) {
        Set<String> kits = kitsCache.get(uuid);
        return kits != null && kits.contains(kitName);
    }

    /**
     * Obtém todos os kits de um jogador (instantâneo, lê do cache)
     */
    public static Set<String> getKits(UUID uuid) {
        Set<String> kits = kitsCache.get(uuid);
        return kits != null ? new HashSet<>(kits) : new HashSet<>();
    }

    /**
     * Adiciona um kit ao jogador (atualiza cache e salva no banco)
     */
    public static void addKit(UUID uuid, String kitName) {
        // Atualiza cache
        Set<String> kits = kitsCache.computeIfAbsent(uuid, k -> new HashSet<>());

        if (kits.contains(kitName)) {
            return; // Já possui
        }

        kits.add(kitName);
        pendingUpdates.put(uuid, true);

        // Salva no banco (assíncrono)
        saveKitsAsync(uuid);
    }

    /**
     * Remove um kit do jogador (atualiza cache e salva no banco)
     */
    public static void removeKit(UUID uuid, String kitName) {
        Set<String> kits = kitsCache.get(uuid);

        if (kits == null || !kits.contains(kitName)) {
            return; // Não possui
        }

        kits.remove(kitName);
        pendingUpdates.put(uuid, true);

        // Salva no banco (assíncrono)
        saveKitsAsync(uuid);
    }

    /**
     * Salva os kits de um jogador no banco (assíncrono)
     */
    private static void saveKitsAsync(UUID uuid) {
        Set<String> kits = kitsCache.get(uuid);
        if (kits == null) return;

        // Clona para evitar ConcurrentModificationException
        final List<String> kitsToSave = new ArrayList<>(kits);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Main.getPlayerRepo().setKits(uuid, kitsToSave);
                pendingUpdates.remove(uuid);

            } catch (Exception e) {
                plugin.getLogger().warning("§cErro ao salvar kits de " + uuid + ": " + e.getMessage());
            }
        });
    }

    /**
     * Recarrega os kits de um jogador do banco (útil após compra de kit)
     */
    public static void reloadPlayer(UUID uuid) {
        kitsCache.remove(uuid);
        loadPlayer(uuid);
    }

    /**
     * Remove jogador do cache (chamado no PlayerQuitEvent)
     * Salva antes se houver mudanças pendentes
     */
    public static void unloadPlayer(UUID uuid) {
        if (pendingUpdates.containsKey(uuid)) {
            // Salva de forma síncrona para garantir
            Set<String> kits = kitsCache.get(uuid);
            if (kits != null) {
                try {
                    Main.getPlayerRepo().setKits(uuid, new ArrayList<>(kits));
                    pendingUpdates.remove(uuid);
                } catch (Exception e) {
                    plugin.getLogger().warning("§cErro ao salvar kits ao deslogar " + uuid);
                }
            }
        }

        kitsCache.remove(uuid);
    }

    /**
     * Salva todos os jogadores com mudanças pendentes e limpa cache
     */
    public static void shutdown() {
        plugin.getLogger().info("§7[Cache] Salvando kits pendentes...");

        int saved = 0;
        for (UUID uuid : pendingUpdates.keySet()) {
            Set<String> kits = kitsCache.get(uuid);
            if (kits != null) {
                try {
                    Main.getPlayerRepo().setKits(uuid, new ArrayList<>(kits));
                    saved++;
                } catch (Exception e) {
                    plugin.getLogger().warning("§cErro ao salvar kits de " + uuid);
                }
            }
        }

        plugin.getLogger().info("§a✓ " + saved + " jogador(es) com kits salvos!");
        kitsCache.clear();
        pendingUpdates.clear();
    }

    /**
     * Verifica se o jogador está carregado no cache
     */
    public static boolean isLoaded(UUID uuid) {
        return kitsCache.containsKey(uuid);
    }

    /**
     * Obtém o número de kits que o jogador possui
     */
    public static int getKitCount(UUID uuid) {
        Set<String> kits = kitsCache.get(uuid);
        return kits != null ? kits.size() : 0;
    }
}