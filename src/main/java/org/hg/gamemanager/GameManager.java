package org.hg.gamemanager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.hg.gamemanager.utils.HidePlayers;
import org.hg.gamemanager.utils.SetGameItems;
import org.hg.kitmanager.KitAPI;
import org.hg.utils.ConfigUtils;
import org.hg.utils.GetSettings;
import org.lobby.commands.lobby.lobbymanager.LobbyManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {

    private static final Map<World, GameStats> stats = new HashMap<>();
    private static final Map<World, List<Player>> PlayerGame = new HashMap<>();
    private static final Map<Player, World> playerWorldCache = new HashMap<>();
    private static ConfigUtils cu = GetSettings.config;
    private static int minToInit;
    private static int maxplayers;

    static {
        minToInit = cu.getConfig().isInt("Configuration.min_players") ? cu.getConfig().getInt("Configuration.min_players") : 0;
        maxplayers = cu.getConfig().isInt("Configuration.max_players") ? cu.getConfig().getInt("Configuration.max_players") : 0;
    }

    public static void joinGame(Player p) {
        World w = null;
        int maiorQuantidade = -1;

        // Procura o melhor mapa disponível
        for (String mapName : cu.getConfig().getStringList("Worlds")) {
            World w2 = Bukkit.getWorld(mapName);
            if (w2 == null) continue;

            GameStats status = getGameStats(w2);

            // Só considera mapas em WAITING ou STARTING
            if (status == GameStats.WAITING || status == GameStats.STARTING) {
                int qtd = PlayersCountInGame(w2);

                // Verifica se o mapa ainda tem vaga
                if (qtd < maxplayers) {
                    // Prioriza mapas com mais jogadores (para preencher mapas)
                    if (qtd > maiorQuantidade) {
                        maiorQuantidade = qtd;
                        w = w2;
                    }
                }
            }
        }

        // Nenhum mapa disponível
        if (w == null) {
            p.sendMessage("§cNenhum mapa disponível no momento!");
            return;
        }

        // Jogador já está em jogo
        if (isInGame(p)) {
            p.sendMessage("§cVocê já está em um jogo!");
            return;
        }

        // Adiciona o jogador ao jogo
        p.teleport(w.getSpawnLocation());
        addPlayer(p, w);
        SetGameItems.WaitingGame(p);

        // Verifica se deve iniciar a contagem
        if (PlayersCountInGame(w) >= minToInit && getGameStats(w) == GameStats.WAITING) {
            setGameStats(w, GameStats.STARTING);
            GameTimerManager.startStarting(w);
        }

        HidePlayers.syncVisibility(p, w);
    }

    public static void exitGame(Player p) {
        if (isInGame(p)) {
            World world = GetPlayerGame(p);

            // Se estava em STARTING e caiu abaixo do mínimo, volta para WAITING

            removePlayer(p, world);
            KitAPI.removeKit(p);

            if (p.isOnline()) {
                LobbyManager.Lobby(p);
            }
        }
    }

    public static void addPlayer(Player p, World w) {
        PlayerGame.putIfAbsent(w, new ArrayList<>()); // Cria se não existir
        List<Player> players = PlayerGame.get(w);
        if (!players.contains(p)) {
            players.add(p);
            playerWorldCache.put(p, w); // Cache
        }
    }

    public static void removePlayer(Player p, World w) {
        List<Player> players = PlayerGame.get(w);
        if (players != null) {
            players.remove(p);
        }
        playerWorldCache.remove(p);
    }

    public static int PlayersCountInGame(World w) {
        if (!PlayerGame.containsKey(w)) return 0;
        return PlayerGame.get(w).size();
    }

    public static World GetPlayerGame(Player p) {
        if (!playerWorldCache.containsKey(p)) return null;
        return playerWorldCache.get(p);
    }

    public static List<Player> GetPlayersInGame(World w) {
        return PlayerGame.get(w);
    }

    public static boolean isInGame(Player p) {
        return playerWorldCache.containsKey(p);
    }

    public static void setGameStats(World w, GameStats s) {
        stats.put(w, s);
    }

    public static GameStats getGameStats(World w) {
        if (!stats.containsKey(w)) return null;
        return stats.get(w);
    }

    public static boolean isGame(World w) {
        return stats.containsKey(w);
    }

    /**
     * Retorna o limite máximo de jogadores por partida
     */
    public static int getMaxPlayers() {
        return maxplayers;
    }

    /**
     * Retorna o mínimo de jogadores para iniciar
     */
    public static int getMinPlayers() {
        return minToInit;
    }

    /**
     * Verifica se um mundo está cheio
     */
    public static boolean isWorldFull(World w) {
        return PlayersCountInGame(w) >= maxplayers;
    }
}