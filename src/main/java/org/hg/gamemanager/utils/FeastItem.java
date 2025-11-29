package org.hg.gamemanager.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeastItem {

    private final Material material;
    private final int amount;
    private final int chance; // 0-100
    private final Map<Enchantment, Integer> enchantments; // map de encantamentos

    public FeastItem(Material material, int amount, int chance, Map<Enchantment, Integer> enchantments) {
        this.material = material;
        this.amount = amount;
        this.chance = chance;
        this.enchantments = enchantments;
    }

    public Material getMaterial() { return material; }
    public int getAmount() { return amount; }
    public int getChance() { return chance; }
    public Map<Enchantment, Integer> getEnchantments() { return enchantments; }

    public ItemStack toItemStack() {
        ItemStack item = new ItemStack(material, amount);
        if (!enchantments.isEmpty()) {
            ItemMeta meta = item.getItemMeta();
            enchantments.forEach((ench, lvl) -> meta.addEnchant(ench, lvl, true));
            item.setItemMeta(meta);
        }
        return item;
    }

    public static List<FeastItem> fromConfig(List<String> list) {
        List<FeastItem> items = new ArrayList<>();
        for (String line : list) {
            String[] split = line.split(",");
            if (split.length < 3) continue;
            try {
                Material mat = Material.valueOf(split[0]);
                int amount = Integer.parseInt(split[1]);
                int chance = Integer.parseInt(split[2]);

                Map<Enchantment, Integer> enchants = new HashMap<>();
                if (split.length >= 4 && !split[3].isEmpty()) {
                    String[] enchPairs = split[3].split(";");
                    for (String pair : enchPairs) {
                        String[] e = pair.split(":");
                        Enchantment ench = Enchantment.getByName(e[0].toUpperCase());
                        int lvl = Integer.parseInt(e[1]);
                        if (ench != null) enchants.put(ench, lvl);
                    }
                }

                items.add(new FeastItem(mat, amount, chance, enchants));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return items;
    }

}
