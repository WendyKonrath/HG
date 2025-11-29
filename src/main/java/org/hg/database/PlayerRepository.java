package org.hg.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Repositório para gerenciar dados de jogadores no banco
 */
public class PlayerRepository {

    private final DatabaseManager database;

    public PlayerRepository(DatabaseManager database) {
        this.database = database;
    }

    /**
     * Cria ou atualiza um jogador no banco
     */
    public void savePlayer(UUID uuid, String name) {
        try {
            Connection conn = database.getConnection();

            // Verifica se jogador existe
            String checkSql = "SELECT uuid FROM hg_players WHERE uuid = ?";
            try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                check.setString(1, uuid.toString());
                ResultSet rs = check.executeQuery();

                if (rs.next()) {
                    // Atualiza nome e last_login
                    String updateSql = "UPDATE hg_players SET name = ? WHERE uuid = ?";
                    try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                        update.setString(1, name);
                        update.setString(2, uuid.toString());
                        update.executeUpdate();
                    }
                } else {
                    // Insere novo jogador
                    String insertSql = "INSERT INTO hg_players (uuid, name, kits) VALUES (?, ?, ?)";
                    try (PreparedStatement insert = conn.prepareStatement(insertSql)) {
                        insert.setString(1, uuid.toString());
                        insert.setString(2, name);
                        insert.setString(3, ""); // Kits vazios inicialmente
                        insert.executeUpdate();
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adiciona uma kill ao jogador
     */
    public void addKill(UUID uuid) {
        try {
            String sql = "UPDATE hg_players SET kills = kills + 1 WHERE uuid = ?";
            database.executeUpdate(sql, uuid.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adiciona uma death ao jogador
     */
    public void addDeath(UUID uuid) {
        try {
            String sql = "UPDATE hg_players SET deaths = deaths + 1 WHERE uuid = ?";
            database.executeUpdate(sql, uuid.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adiciona uma vitória ao jogador
     */
    public void addWin(UUID uuid) {
        try {
            String sql = "UPDATE hg_players SET wins = wins + 1, games_played = games_played + 1 WHERE uuid = ?";
            database.executeUpdate(sql, uuid.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adiciona um jogo jogado (derrota)
     */
    public void addGamePlayed(UUID uuid) {
        try {
            String sql = "UPDATE hg_players SET games_played = games_played + 1 WHERE uuid = ?";
            database.executeUpdate(sql, uuid.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== GERENCIAMENTO DE KITS ====================

    /**
     * Adiciona um kit ao jogador
     */
    public void addKit(UUID uuid, String kitName) {
        try {
            List<String> currentKits = getKits(uuid);

            // Evita duplicatas
            if (currentKits.contains(kitName)) {
                return;
            }

            currentKits.add(kitName);
            String kitsString = String.join(";", currentKits);

            String sql = "UPDATE hg_players SET kits = ? WHERE uuid = ?";
            database.executeUpdate(sql, kitsString, uuid.toString());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adiciona dinheiro ao jogador
     */
    public void addMoney(UUID uuid, int amount) {
        try {
            String sql = "UPDATE hg_players SET money = money + ? WHERE uuid = ?";
            database.executeUpdate(sql, amount, uuid.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove um kit do jogador
     */
    public void removeKit(UUID uuid, String kitName) {
        try {
            List<String> currentKits = getKits(uuid);
            currentKits.remove(kitName);

            String kitsString = String.join(";", currentKits);

            String sql = "UPDATE hg_players SET kits = ? WHERE uuid = ?";
            database.executeUpdate(sql, kitsString, uuid.toString());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove dinheiro do jogador
     */
    public void removeMoney(UUID uuid, int amount) {
        try {
            String sql = "UPDATE hg_players SET money = money - ? WHERE uuid = ?";
            database.executeUpdate(sql, amount, uuid.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifica se jogador tem um kit
     */
    public boolean hasKit(UUID uuid, String kitName) {
        return getKits(uuid).contains(kitName);
    }

    /**
     * Obtém todos os kits de um jogador
     */
    public List<String> getKits(UUID uuid) {
        try {
            Connection conn = database.getConnection();
            String sql = "SELECT kits FROM hg_players WHERE uuid = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String kitsString = rs.getString("kits");

                    if (kitsString == null || kitsString.isEmpty()) {
                        return new ArrayList<>();
                    }

                    // Converte "Kit1;Kit2;Kit3" em lista
                    return new ArrayList<>(Arrays.asList(kitsString.split(";")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Define TODOS os kits de um jogador (sobrescreve)
     */
    public void setKits(UUID uuid, List<String> kits) {
        try {
            String kitsString = String.join(";", kits);
            String sql = "UPDATE hg_players SET kits = ? WHERE uuid = ?";
            database.executeUpdate(sql, kitsString, uuid.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Define o dinheiro do jogador
     */
    public void setMoney(UUID uuid, int amount) {
        try {
            String sql = "UPDATE hg_players SET money = ? WHERE uuid = ?";
            database.executeUpdate(sql, amount, uuid.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== STATS ====================

    /**
     * Obtém as kills de um jogador
     */
    public int getKills(UUID uuid) {
        try {
            Connection conn = database.getConnection();
            String sql = "SELECT kills FROM hg_players WHERE uuid = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt("kills");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Obtém as mortes de um jogador
     */
    public int getDeaths(UUID uuid) {
        try {
            Connection conn = database.getConnection();
            String sql = "SELECT deaths FROM hg_players WHERE uuid = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt("deaths");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Obtém as vitórias de um jogador
     */
    public int getWins(UUID uuid) {
        try {
            Connection conn = database.getConnection();
            String sql = "SELECT wins FROM hg_players WHERE uuid = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt("wins");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Obtém o KDR (Kill/Death Ratio)
     */
    public double getKDR(UUID uuid) {
        int kills = getKills(uuid);
        int deaths = getDeaths(uuid);

        if (deaths == 0) {
            return kills;
        }

        return (double) kills / deaths;
    }

    /**
     * Obtém todas as stats de um jogador
     */
    public PlayerStats getStats(UUID uuid) {
        try {
            Connection conn = database.getConnection();
            String sql = "SELECT * FROM hg_players WHERE uuid = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String kitsString = rs.getString("kits");
                    List<String> kits = kitsString.isEmpty() ?
                            new ArrayList<>() :
                            Arrays.asList(kitsString.split(";"));

                    return new PlayerStats(
                            uuid,
                            rs.getString("name"),
                            rs.getInt("kills"),
                            rs.getInt("deaths"),
                            rs.getInt("wins"),
                            rs.getInt("games_played"),
                            rs.getInt("money"),
                            kits
                            // SEM player_visible!
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Obtém o dinheiro do jogador
     */
    public int getMoney(UUID uuid) {
        try {
            Connection conn = database.getConnection();
            String sql = "SELECT money FROM hg_players WHERE uuid = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt("money");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Verifica se jogador tem dinheiro suficiente
     */
    public boolean hasMoney(UUID uuid, int amount) {
        return getMoney(uuid) >= amount;
    }


    /**
     * Classe para armazenar stats de jogadores
     */
    public static class PlayerStats {
        private final UUID uuid;
        private final String name;
        private final int kills;
        private final int deaths;
        private final int wins;
        private final int gamesPlayed;
        private final int money;           // NOVO
        private final List<String> kits;

        public PlayerStats(UUID uuid, String name, int kills, int deaths,
                           int wins, int gamesPlayed, int money, List<String> kits) {
            this.uuid = uuid;
            this.name = name;
            this.kills = kills;
            this.deaths = deaths;
            this.wins = wins;
            this.gamesPlayed = gamesPlayed;
            this.money = money;
            this.kits = kits;
        }

        // Getters
        public UUID getUuid() {
            return uuid;
        }

        public String getName() {
            return name;
        }

        public int getKills() {
            return kills;
        }

        public int getDeaths() {
            return deaths;
        }

        public int getWins() {
            return wins;
        }

        public int getGamesPlayed() {
            return gamesPlayed;
        }

        public int getMoney() {
            return money;
        }            // NOVO

        public List<String> getKits() {
            return kits;
        }

        public double getKDR() {
            return deaths == 0 ? kills : (double) kills / deaths;
        }

        public double getWinRate() {
            return gamesPlayed == 0 ? 0 : (double) wins / gamesPlayed * 100;
        }
    }
}