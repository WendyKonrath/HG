package org.hg.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Gerenciador de banco de dados com suporte a MySQL e SQLite
 */
public class DatabaseManager {

    private final JavaPlugin plugin;
    private Connection connection;
    private DatabaseType type;

    // Configurações MySQL
    private String hostname;
    private String database;
    private String username;
    private String password;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * Carrega as configurações do config.yml
     */
    private void loadConfig() {
        String typeStr = plugin.getConfig().getString("database.type", "SQLite");

        try {
            this.type = DatabaseType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Tipo de banco inválido: " + typeStr + ". Usando SQLite.");
            this.type = DatabaseType.SQLITE;
        }

        // Carrega configs do MySQL
        this.hostname = plugin.getConfig().getString("database.remote.hostname", "localhost:3306");
        this.database = plugin.getConfig().getString("database.remote.database", "hg");
        this.username = plugin.getConfig().getString("database.remote.username", "root");
        this.password = plugin.getConfig().getString("database.remote.password", "");

        plugin.getLogger().info("§aTipo de banco de dados: §f" + type.name());
    }

    /**
     * Conecta ao banco de dados
     */
    public boolean connect() {
        try {
            if (connection != null && !connection.isClosed()) {
                return true;
            }

            switch (type) {
                case MYSQL:
                    return connectMySQL();
                case SQLITE:
                    return connectSQLite();
                default:
                    plugin.getLogger().severe("§cTipo de banco desconhecido!");
                    return false;
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("§cErro ao conectar ao banco de dados:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Conecta ao MySQL
     */
    private boolean connectMySQL() throws SQLException {
        plugin.getLogger().info("§eConectando ao MySQL...");

        String url = "jdbc:mysql://" + hostname + "/" + database +
                "?autoReconnect=true&useSSL=false&characterEncoding=utf8";

        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);

            plugin.getLogger().info("§a✓ Conectado ao MySQL com sucesso!");
            return true;

        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("§cDriver MySQL não encontrado!");
            plugin.getLogger().severe("§cAdicione o MySQL Connector ao seu servidor!");
            return false;
        }
    }

    /**
     * Conecta ao SQLite
     */
    private boolean connectSQLite() throws SQLException {
        plugin.getLogger().info("§eConectando ao SQLite...");

        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File dbFile = new File(dataFolder, "database.db");
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(url);

            plugin.getLogger().info("§a✓ Conectado ao SQLite com sucesso!");
            plugin.getLogger().info("§7Arquivo: " + dbFile.getAbsolutePath());
            return true;

        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("§cDriver SQLite não encontrado!");
            plugin.getLogger().severe("§cO SQLite já vem incluído no Spigot/Paper.");
            return false;
        }
    }

    /**
     * Desconecta do banco de dados
     */
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("§eDesconectado do banco de dados.");
            } catch (SQLException e) {
                plugin.getLogger().warning("§cErro ao desconectar do banco:");
                e.printStackTrace();
            }
        }
    }

    /**
     * Cria as tabelas necessárias
     */
    public void createTables() {
        if (!isConnected()) {
            plugin.getLogger().warning("§cNão conectado ao banco! Tentando reconectar...");
            if (!connect()) {
                plugin.getLogger().severe("§cFalha ao criar tabelas - sem conexão!");
                return;
            }
        }

        try {
            // Tabela de jogadores - SIMPLIFICADA
            String createPlayersTable = type == DatabaseType.MYSQL ?
                    // MySQL
                    "CREATE TABLE IF NOT EXISTS hg_players (" +
                            "uuid VARCHAR(36) PRIMARY KEY," +
                            "name VARCHAR(16) NOT NULL," +
                            "kills INT DEFAULT 0," +
                            "deaths INT DEFAULT 0," +
                            "wins INT DEFAULT 0," +
                            "games_played INT DEFAULT 0," +
                            "money INT DEFAULT 0," +
                            "kits TEXT DEFAULT ''" +  // MUDANÇA: Kits como string separada por ;
                            ")" :
                    // SQLite
                    "CREATE TABLE IF NOT EXISTS hg_players (" +
                            "uuid TEXT PRIMARY KEY," +
                            "name TEXT NOT NULL," +
                            "kills INTEGER DEFAULT 0," +
                            "deaths INTEGER DEFAULT 0," +
                            "wins INTEGER DEFAULT 0," +
                            "games_played INTEGER DEFAULT 0," +
                            "money INT DEFAULT 0," +
                            "kits TEXT DEFAULT ''" +  // MUDANÇA: Kits como string
                            ")";

            executeUpdate(createPlayersTable);

            plugin.getLogger().info("§a✓ Tabelas criadas/verificadas com sucesso!");

        } catch (SQLException e) {
            plugin.getLogger().severe("§cErro ao criar tabelas:");
            e.printStackTrace();
        }
    }

    /**
     * Executa um UPDATE/INSERT/DELETE
     */
    public void executeUpdate(String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
        }
    }

    /**
     * Obtém a conexão (para queries personalizadas)
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("§cErro ao verificar conexão:");
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Verifica se está conectado
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Obtém o tipo de banco
     */
    public DatabaseType getType() {
        return type;
    }

    /**
     * Enum dos tipos de banco
     */
    public enum DatabaseType {
        MYSQL,
        SQLITE
    }
}