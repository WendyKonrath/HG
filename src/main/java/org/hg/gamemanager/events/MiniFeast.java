package org.hg.gamemanager.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.hg.gamemanager.utils.FeastItem;
import org.hg.utils.GetSettings;

import java.util.List;
import java.util.Random;

public class MiniFeast {

    private static final Random random = new Random();
    public static List<FeastItem> minifeastItems = FeastItem.fromConfig(
            GetSettings.config.getConfig().getStringList("minifeast_items")
    );

    // Spawnar MiniFeast 3x3 com 4 baús, mesa de encantamento e plataforma de vidro
    public static void spawnMinifeast(World w) {

        int borderLimit = GetSettings.config.getConfig().getInt("Configuration.borderlimit") / 2;

        // coordenada base do centro do minifeast
        int baseX = random.nextInt(borderLimit * 2 + 1) - borderLimit;
        int baseZ = random.nextInt(borderLimit * 2 + 1) - borderLimit;
        int y = w.getHighestBlockYAt(baseX, baseZ) + 1; // 1 bloco acima do solo

        // criar plataforma de vidro 3x3
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block glassBlock = w.getBlockAt(baseX + dx, y, baseZ + dz);
                glassBlock.setType(Material.GLASS);
            }
        }

        // posições relativas dos 4 baús nos cantos do 3x3
        int[][] chestOffsets = {
                {-1, -1},
                {-1, 1},
                {1, -1},
                {1, 1}
        };

        // criar os baús 1 bloco acima da plataforma de vidro
        for (int[] offset : chestOffsets) {
            int x = baseX + offset[0];
            int z = baseZ + offset[1];
            Block block = w.getBlockAt(x, y + 1, z); // 1 bloco acima da plataforma
            block.setType(Material.CHEST);

            Chest chest = (Chest) block.getState();
            Inventory inv = chest.getBlockInventory();

            // adiciona os itens com chance e slot aleatório
            for (FeastItem item : minifeastItems) {
                if (random.nextInt(100) < item.getChance()) {
                    int slot;
                    do {
                        slot = random.nextInt(inv.getSize());
                    } while (inv.getItem(slot) != null);

                    inv.setItem(slot, item.toItemStack());
                }
            }
        }

        // criar a mesa de encantamento no centro 1 bloco acima da plataforma
        Block centerBlock = w.getBlockAt(baseX, y + 1, baseZ);
        centerBlock.setType(Material.ENCHANTMENT_TABLE);

        String x1 = getApproximateCoord(baseX, 10, 70);
        String z1 = getApproximateCoord(baseZ, 10, 70);
        String x2 = getApproximateCoord(baseX, -70, -10);
        String z2 = getApproximateCoord(baseZ, -70, -10);

        Bukkit.broadcastMessage(String.format(
                "§6Um Minifeast apareceu próximo a X:%s Z:%s e X:%s Z:%s",
                x1, z1, x2, z2
        ));
    }

    private static String getApproximateCoord(int real, int min, int max) {
        return String.valueOf(real + random.nextInt(max - min + 1) + min);
    }
}
