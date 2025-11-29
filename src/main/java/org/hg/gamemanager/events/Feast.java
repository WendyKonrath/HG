package org.hg.gamemanager.events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.hg.gamemanager.utils.FeastItem;
import org.hg.utils.GetSettings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Feast {

    private static final Random random = new Random();
    public static List<FeastItem> feastItems =
            FeastItem.fromConfig(GetSettings.config.getConfig().getStringList("feast_items"));

    // Salva o Feast por mundo
    private static final Map<World, Location> nextFeastLocations = new HashMap<>();

    /**
     * Obtém o centro do feast que foi agendado
     */
    public static Location getFeastCenter(World w) {
        return nextFeastLocations.get(w);
    }

    /**
     * Agenda um novo Feast em localização aleatória
     */
    public static void scheduleFeast(World w) {
        int borderLimit = GetSettings.config.getConfig().getInt("Configuration.borderlimit") / 2;

        int centerX = random.nextInt(borderLimit * 2 + 1) - borderLimit;
        int centerZ = random.nextInt(borderLimit * 2 + 1) - borderLimit;

        int y = w.getHighestBlockYAt(centerX, centerZ) + 1;

        Location feastLoc = new Location(w, centerX, y, centerZ);
        nextFeastLocations.put(w, feastLoc);
    }

    /**
     * Spawna o Feast no mundo
     */
    public static void spawnFeast(World w) {
        Location loc = nextFeastLocations.get(w);
        if (loc == null) return;

        int centerX = loc.getBlockX();
        int centerZ = loc.getBlockZ();
        int y = loc.getBlockY();

        // Plataforma circular de grama (raio 15)
        int radius = 15;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= radius * radius) {
                    Block b = w.getBlockAt(centerX + dx, y, centerZ + dz);
                    b.setType(Material.GRASS);
                }
            }
        }

        // Baús em padrão estratégico (12 posições)
        int[][] offsets = {
                {-1, -1}, {-1, 1}, {1, -1}, {1, 1},           // diagonais próximas
                {0, 2}, {-2, 0}, {0, -2}, {2, 0},             // cruz média
                {-2, 2}, {2, 2}, {2, -2}, {-2, -2}            // diagonais distantes
        };

        for (int[] off : offsets) {
            int X = centerX + off[0];
            int Z = centerZ + off[1];

            Block chestBlock = w.getBlockAt(X, y + 1, Z);
            chestBlock.setType(Material.CHEST);

            Chest chest = (Chest) chestBlock.getState();
            Inventory inv = chest.getBlockInventory();

            // Coloca itens com chance configurada
            for (FeastItem item : feastItems) {
                if (random.nextInt(100) < item.getChance()) {
                    int slot;
                    do {
                        slot = random.nextInt(inv.getSize());
                    } while (inv.getItem(slot) != null);

                    inv.setItem(slot, item.toItemStack());
                }
            }
        }

        // Mesa de encantamento no centro
        w.getBlockAt(centerX, y + 1, centerZ).setType(Material.ENCHANTMENT_TABLE);

        // Limpa agendamento
        nextFeastLocations.remove(w);
    }
}