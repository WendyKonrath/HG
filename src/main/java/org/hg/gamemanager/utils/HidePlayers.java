package org.hg.gamemanager.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.hg.gamemanager.GameManager;

public class HidePlayers {

    public static void syncVisibility(Player p, World gameWorld) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(p)) continue;

            // Se estão no mesmo mundo do jogo, mostrar
            if (online.getWorld().equals(gameWorld) && GameManager.isInGame(online)) {
                p.showPlayer(online);
                online.showPlayer(p);
            } else {
                // Mundos diferentes ou não está no jogo, esconder
                p.hidePlayer(online);
                online.hidePlayer(p);
            }
        }
    }
}
