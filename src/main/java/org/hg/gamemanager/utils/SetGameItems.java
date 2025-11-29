package org.hg.gamemanager.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.hg.gamemanager.GameManager;
import org.hg.utils.CreateItem;

public class SetGameItems {
    public static ItemStack compass = CreateItem.add(Material.COMPASS);
    public static ItemStack kitselector = CreateItem.add("§aSelecionar Kit!", Material.CHEST);
    public static ItemStack kitshop = CreateItem.add("§eLoja de Kits!", Material.EMERALD);
    public static ItemStack leavegame = CreateItem.add("§cSair do jogo!", Material.BED);

    public static void WaitingGame(Player p) {
        if (!GameManager.isInGame(p)) return;
        p.getInventory().clear();
        p.getInventory().setItem(0, kitselector);
        p.getInventory().setItem(4, kitshop);
        p.getInventory().setItem(8, leavegame);
    }

    public static void StartedGame(Player p) {
        if (!GameManager.isInGame(p)) return;
        p.getInventory().clear();
        p.getInventory().setItem(0, compass);

    }
}
