package org.hg.gamemanager;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.hg.gamemanager.utils.CompassTarget;
import org.hg.gamemanager.utils.SetGameItems;
import org.hg.kitmanager.KitAPI;
import org.hg.kitmanager.inventorys.KitSelector;
import org.hg.kitmanager.inventorys.Shop;
import org.hg.utils.GetSettings;
import org.hg.utils.StringUtils;
import org.lobby.commands.lobby.lobbymanager.LobbyManager;
import org.lobby.utils.PlaceholderAPI;
import org.lobby.utils.ScoreboardAPI;

public class GameEventsManager implements Listener {

    @EventHandler
    public static void onPlayerDeathEvent(PlayerDeathEvent e) {
        Player p = e.getEntity();
        Player k = e.getEntity().getKiller();
        if (GameManager.isInGame(p)) {

            GameManager.exitGame(p);
            e.setDeathMessage(null);
            p.spigot().respawn();
        } else {
            e.setDeathMessage(null);
        }
    }

    @EventHandler
    public void onPlayerDamage(org.bukkit.event.entity.EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (GameManager.isInGame(p)) {
                if (GameManager.getGameStats(GameManager.GetPlayerGame(p)) != GameStats.GAME) {
                    e.setCancelled(true);
                }
            } else {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerHunger(org.bukkit.event.entity.FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (GameManager.isInGame(p)) {
                if (GameManager.getGameStats(GameManager.GetPlayerGame(p)) != GameStats.GAME) {
                    e.setCancelled(true);
                }
            } else {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerClickEvent(PlayerInteractEvent e) {
        if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK ||
                e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getItem() == null) return;
            Player p = e.getPlayer();
            if (GameManager.isInGame(p)) {
                World playerWorld = GameManager.GetPlayerGame(p);
                GameStats status = GameManager.getGameStats(playerWorld);

                if (e.getItem().isSimilar(SetGameItems.leavegame) &&
                        (status == GameStats.WAITING || status == GameStats.STARTING)) {
                    GameManager.exitGame(p);
                }

                if (e.getItem().isSimilar(SetGameItems.kitselector) &&
                        (status == GameStats.WAITING || status == GameStats.STARTING)) {
                    KitSelector.open(p);
                }
                if (e.getItem().isSimilar(SetGameItems.kitshop) &&
                        (status == GameStats.WAITING || status == GameStats.STARTING)) {
                    Shop.open(p);
                }

                if (e.getItem().isSimilar(SetGameItems.compass) &&
                        (status == GameStats.GAME || status == GameStats.INVINCIBLE)) {
                    Player t = CompassTarget.getNearestPlayer(p);
                    if (t != null) {
                        double distance = e.getPlayer().getLocation().distance(t.getLocation());
                        double speed = 5.0; // blocos por segundo
                        double time = distance / speed;

                        e.getPlayer().sendMessage("§aBússola apontando para §7" + t.getName() + "§a,");
                        e.getPlayer().sendMessage("§aVocê está a §7" +
                                (int) distance
                                + " blocos §ade distância, com §7" + (int) time + "s §ade expectativa de tempo para chegada.");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryInteract(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (GameManager.isInGame(p))
            if (GameManager.getGameStats(p.getWorld()) == GameStats.WAITING || GameManager.getGameStats(p.getWorld()) == GameStats.STARTING) {
                e.setCancelled(true);
            }
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (GameManager.isInGame(p))
            if (GameManager.getGameStats(p.getWorld()) == GameStats.WAITING || GameManager.getGameStats(p.getWorld()) == GameStats.STARTING) {
                e.setCancelled(true);
            }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (GameManager.isGame(e.getTo().getWorld())) {
            if (GetSettings.config.getConfig().getBoolean("Scoreboard.activated", false)) {
                PlaceholderAPI.registerGlobal("hg_map", player -> e.getTo().getWorld().getName());
                PlaceholderAPI.registerGlobal("hg_kit", KitAPI::getKit);
                PlaceholderAPI.registerGlobal("hg_players", player -> String.valueOf(GameManager.PlayersCountInGame(e.getTo().getWorld())));
                PlaceholderAPI.registerGlobal("hg_maxplayers", player -> String.valueOf(GameManager.getMaxPlayers()));
                PlaceholderAPI.registerGlobal("hg_maptime", player -> getTime(e.getTo().getWorld()));
                PlaceholderAPI.registerGlobal("hg_stats", player -> GameManager.getGameStats(e.getTo().getWorld()).getDescription());
                ScoreboardAPI.setScoreboard(p, GetSettings.config.getConfig().getString("Scoreboard.title"), GetSettings.config.getConfig().getStringList("Scoreboard.lines"));
            }
        }
        if (GameManager.isInGame(p) && LobbyManager.isLobby(p, e.getTo().getWorld())) {
            GameManager.exitGame(p);
        }
    }

    public static String getTime(World w) {
        if (GameStats.WAITING == GameManager.getGameStats(w)) {
            return "";
        }
        if (GameStats.GAME == GameManager.getGameStats(w)) {
            return StringUtils.formatTime(GetSettings.config.getConfig().getInt("Configuration.invincible_seconds") + GameTimerManager.getTime(w));
        }

        return StringUtils.formatTime(GameTimerManager.getTime(w));
    }
}
