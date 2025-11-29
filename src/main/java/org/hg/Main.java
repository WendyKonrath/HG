package org.hg;

import org.bukkit.plugin.java.JavaPlugin;
import org.hg.commands.CommandListener;
import org.hg.database.DatabaseManager;
import org.hg.database.PlayerKitsCache;
import org.hg.database.PlayerRepository;
import org.hg.database.PlayerStatsCache;
import org.hg.gamemanager.GameTimerManager;
import org.hg.listeners.Listeners;
import org.hg.utils.WorldManager.WorldGenerator;
import org.lobby.utils.PlaceholderAPI;

public final class Main extends JavaPlugin {

    private static Main instance;
    private DatabaseManager databaseManager;
    private PlayerRepository playerRepository;

    public Main() {
        instance = this;
    }

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        getLogger().info("§e========================================");
        getLogger().info("§a  Inicializando HungerGames");
        getLogger().info("§e========================================");

        // 1. Configuração inicial
        getLogger().info("§7[1/6] Carregando configurações...");
        saveDefaultConfig();
        getLogger().info("§a✓ Configurações carregadas!");

        // 2. Banco de dados
        getLogger().info("§7[2/6] Conectando ao banco de dados...");
        databaseManager = new DatabaseManager(this);

        if (!databaseManager.connect()) {
            getLogger().severe("§c✗ Falha ao conectar ao banco de dados!");
            getLogger().severe("§cO plugin será desativado.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        databaseManager.createTables();
        playerRepository = new PlayerRepository(databaseManager);
        getLogger().info("§a✓ Banco de dados conectado!");

        // 3. Sistemas de cache
        getLogger().info("§7[3/6] Inicializando sistemas de cache...");
        // PlayerStatsCache não precisa de init() pois usa salvamento imediato
        // PlayerKitsCache não precisa de init() pois usa salvamento imediato
        getLogger().info("§a✓ Cache de estatísticas: Salvamento imediato assíncrono");
        getLogger().info("§a✓ Cache de kits: Leitura instantânea");

        // 4. Game Manager
        getLogger().info("§7[4/6] Inicializando game timers...");
        GameTimerManager.init();
        getLogger().info("§a✓ Timers inicializados!");

        // 5. Comandos e Listeners
        getLogger().info("§7[5/6] Registrando comandos e eventos...");
        CommandListener.setupCommands();
        Listeners.setupListeners();
        getLogger().info("§a✓ Comandos e eventos registrados!");

        // 6. Geração de mundos
        getLogger().info("§7[6/6] Preparando mundos...");
        WorldGenerator.RenewMap(true);
        getLogger().info("§a✓ Mundos preparados!");

        getLogger().info("§e========================================");
        getLogger().info("§a✓ Plugin iniciado com sucesso!");
        getLogger().info("§e========================================");

        PlaceholderAPI.registerGlobal("hg_coins", player -> String.valueOf(PlayerStatsCache.getMoney(player.getUniqueId())));
        PlaceholderAPI.registerGlobal("hg_wins", player -> String.valueOf(PlayerStatsCache.getWins(player.getUniqueId())));
        PlaceholderAPI.registerGlobal("hg_kills", player -> String.valueOf(PlayerStatsCache.getKills(player.getUniqueId())));
    }

    @Override
    public void onDisable() {
        getLogger().info("§e========================================");
        getLogger().info("§c  Desligando HungerGames");
        getLogger().info("§e========================================");

        if (playerRepository != null) {
            // Limpa cache de stats (dados já foram salvos em tempo real)
            getLogger().info("§7[1/3] Limpando cache de estatísticas...");
            PlayerStatsCache.shutdown();

            // Salva kits pendentes
            getLogger().info("§7[2/3] Salvando kits pendentes...");
            PlayerKitsCache.shutdown();
        }

        // Desconecta do banco
        if (databaseManager != null) {
            getLogger().info("§7[3/3] Desconectando do banco...");
            databaseManager.disconnect();
        }

        getLogger().info("§e========================================");
        getLogger().info("§c✗ Plugin desativado com sucesso!");
        getLogger().info("§e========================================");
    }

    // ==================== GETTERS ====================

    public static PlayerRepository getPlayerRepo() {
        return instance.playerRepository;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}