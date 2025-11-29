package org.hg.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public class CreateItem {

    public static ItemStack add(String name, Material material, int data, int amount) {
        ItemStack item = new ItemStack(material, amount);
        item.setDurability((short) data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack add(String name, Material material, int data, int amount, String[] lore) {
        ItemStack item = new ItemStack(material, amount);
        item.setDurability((short) data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack add(String name, Material material) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack add(String name, Material material, String[] lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack add(Material material) {
        ItemStack item = new ItemStack(material, 1);

        return item;
    }

    public static ItemStack add(String name, Material material, int data) {
        ItemStack item = new ItemStack(material, 1);
        item.setDurability((short) data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack add(String name, Material material, int data, String[] lore) {
        ItemStack item = new ItemStack(material, 1);
        item.setDurability((short) data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack add(String name, Material material, int data, List<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        item.setDurability((short) data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack add(String name, String Owner, int amount) {
        final ItemStack item = new ItemStack(Material.SKULL_ITEM, amount);
        item.setDurability((short) 3);
        final SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setDisplayName(name);
        skull.setOwner(Owner);
        item.setItemMeta(skull);
        return item;
    }

    public static ItemStack add(String name, String Owner, int amount, String[] lore) {
        final ItemStack item = new ItemStack(Material.SKULL_ITEM, amount);
        item.setDurability((short) 3);
        final SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setDisplayName(name);
        skull.setOwner(Owner);
        skull.setLore(Arrays.asList(lore));
        item.setItemMeta(skull);
        return item;
    }
}
