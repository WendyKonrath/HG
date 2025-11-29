package org.hg.kitmanager.kits.hermit;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class Hermit implements Listener {

    public static void applyHermitEffect(Player p) {
        p.sendMessage("§6[Hermit] §eVocê será teleportado para longe dos outros jogadores...");

        // Teleporta para um bioma aleatório dentro da borda
        BiomeTeleport.teleportToRandomBiome(p);
    }
}