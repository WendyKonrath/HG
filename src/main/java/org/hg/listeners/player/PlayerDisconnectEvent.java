package org.hg.listeners.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.hg.database.PlayerKitsCache;
import org.hg.database.PlayerStatsCache;
import org.hg.gamemanager.GameManager;

public class PlayerDisconnectEvent implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (GameManager.isInGame(e.getPlayer())) {
            GameManager.exitGame(p);
        }

        // ✅ Remove do cache (dados já foram salvos em tempo real)
        PlayerStatsCache.unloadPlayer(p.getUniqueId());

        // ✅ Remove kits do cache (salva antes se necessário)
        PlayerKitsCache.unloadPlayer(p.getUniqueId());
    }
}
