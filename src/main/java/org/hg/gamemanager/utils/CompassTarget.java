package org.hg.gamemanager.utils;

import org.bukkit.entity.Player;
import org.hg.gamemanager.GameManager;

public class CompassTarget {

    public static Player getNearestPlayer(Player p) {
        double menor = Double.MAX_VALUE;
        Player alvo = null;

        for (Player other : GameManager.GetPlayersInGame(GameManager.GetPlayerGame(p))) {
            if (other == p) continue;
            if (!other.isOnline()) continue;
            if (other.isDead()) continue;

            double dist = p.getLocation().distanceSquared(other.getLocation());

            if (dist < menor) {
                menor = dist;
                alvo = other;
            }
        }

        return alvo;
    }
}

