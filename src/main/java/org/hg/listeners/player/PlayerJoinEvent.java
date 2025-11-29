package org.hg.listeners.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.hg.Main;
import org.hg.database.PlayerKitsCache;
import org.hg.database.PlayerStatsCache;

public class PlayerJoinEvent implements Listener {

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent e) {
        Player p = e.getPlayer();

        // Salva/atualiza jogador no banco (nome + last_login)
        Main.getPlayerRepo().savePlayer(p.getUniqueId(), p.getName());


        // ✅ Carrega estatísticas no cache (assíncrono)
        PlayerStatsCache.loadPlayer(p.getUniqueId());

        // ✅ Carrega kits no cache (assíncrono)
        PlayerKitsCache.loadPlayer(p.getUniqueId());

    }
}
