package org.hg.gamemanager.events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Coliseu {
    public static void setColiseu(World w) {
        Location spawn = w.getSpawnLocation();
        int y1 = spawn.getBlockY() + 50;
        int y2 = y1 + 6;

        int minX = -20;
        int maxX = 20;
        int minZ = -20;
        int maxZ = 20;

        // gera a arena
        for (int x = minX; x <= maxX; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = minZ; z <= maxZ; z++) {

                    Block b = w.getBlockAt(x + spawn.getBlockX(), y, z + spawn.getBlockZ());

                    // obsidian nas bordas para as camadas 1,2,4,6
                    if ((y - y1) == 2 || (y - y1) == 4) {
                        if (x == minX || x == maxX || z == minZ || z == maxZ) {
                            b.setType(Material.OBSIDIAN);
                        }
                    }

                    // glowstone na camada 7 nas bordas
                    if ((y - y1) == 6) {
                        if (x == minX || x == maxX || z == minZ || z == maxZ) {
                            b.setType(Material.GLOWSTONE);
                        }
                    }

                    // obsidian no chão (camada 0)
                    if ((y - y1) == 0) {
                        b.setType(Material.OBSIDIAN);
                    }

                    if ((y - y1) == 0 || (y - y1) == 1 || (y - y1) == 3) {
                        if (x == minX || x == maxX || z == minZ || z == maxZ) {
                            b.setType(Material.AIR);
                        }
                    }
                }
            }
        }

        // calcula centro do Coliseu
        double centerX = spawn.getBlockX() + (minX + maxX) / 2.0 + 0.5;
        double centerY = y1 + 1; // em cima do chão de obsidian
        double centerZ = spawn.getBlockZ() + (minZ + maxZ) / 2.0 + 0.5;

        Location center = new Location(w, centerX, centerY, centerZ, 0, 0);

        // teleporta todos os players do mundo para o centro
        for (Player player : w.getPlayers()) {
            player.teleport(center);
        }
    }
}
