package org.hg.kitmanager.inventorys;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.hg.database.PlayerKitsCache;
import org.hg.database.PlayerStatsCache;
import org.hg.kitmanager.KitAPI;
import org.hg.utils.CreateItem;
import org.hg.utils.GetSettings;

import java.util.ArrayList;
import java.util.List;

public class Shop extends KitAPI {

    private static final int[] kitslots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private static final int PREV_PAGE_SLOT = 48;
    private static final int INFO_SLOT = 49;
    private static final int NEXT_PAGE_SLOT = 50;

    public static void open(Player p) {
        open(p, 1);
    }

    public static void open(Player p, int page) {
        // Verifica se os dados do jogador estão carregados
        if (!PlayerKitsCache.isLoaded(p.getUniqueId())) {
            p.sendMessage("§cSeus dados ainda estão carregando, aguarde...");
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 6 * 9, GetSettings.kits.getConfig().getString("KitShop.InventoryName", "§8Loja de kits").replace("&", "§"));

        List<String> ShopKits = new ArrayList<>();
        for (String kit : kitlist()) {
            if (!hasKit(p, kit)) {
                ShopKits.add(kit);
            }
        }

        if (ShopKits.isEmpty()) {
            p.sendMessage("§cTodos os kits ja foram comprados!");
            return;
        }
        int kitsPerPage = kitslots.length;
        int totalPages = (int) Math.ceil((double) ShopKits.size() / kitsPerPage);

        // Valida página
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        int startIndex = (page - 1) * kitsPerPage;
        int endIndex = Math.min(startIndex + kitsPerPage, ShopKits.size());

        // Preenche slots com os kits
        int slotIndex = 0;
        for (int i = startIndex; i < endIndex; i++) {
            boolean isSelected = selectedKit(p, ShopKits.get(i));
            inv.setItem(kitslots[slotIndex], kitItemInventory(ShopKits.get(i), PlayerStatsCache.getMoney(p.getUniqueId())));
            slotIndex++;
        }
        // Navegação: Página anterior
        if (page > 1) {
            ItemStack prevArrow = CreateItem.add("§ePágina Anterior", Material.ARROW, 0, new String[]{
                    "§7Clique para voltar",
                    "§7Página " + (page - 1) + "/" + totalPages
            });
            inv.setItem(PREV_PAGE_SLOT, prevArrow);
        }

        // Info central
        ItemStack info = CreateItem.add("§aSuas moedas: - §o" + PlayerStatsCache.getMoney(p.getUniqueId()), Material.EMERALD);
        inv.setItem(INFO_SLOT, info);

        // Navegação: Próxima página
        if (page < totalPages) {
            ItemStack nextArrow = CreateItem.add("§ePróxima Página", Material.ARROW, 0, new String[]{
                    "§7Clique para avançar",
                    "§7Página " + (page + 1) + "/" + totalPages
            });
            inv.setItem(NEXT_PAGE_SLOT, nextArrow);
        }

        p.openInventory(inv);
    }

    public static ItemStack kitItemInventory(String kit, int playermoney) {
        String name = GetSettings.kits.getConfig().getString("Kits." + kit + ".InventoryKitInfo.Name").replace("&", "§");
        String[] m = GetSettings.kits.getConfig().getString("Kits." + kit + ".InventoryKitInfo.Id", "1:0").split(":");
        // Lore configurada
        List<String> lore = new ArrayList<>();
        List<String> configLore = GetSettings.kits.getConfig().getStringList("Kits." + kit + ".InventoryKitInfo.Lore");
        List<String> lorenotkitvalue = GetSettings.kits.getConfig().getStringList("Kits." + kit + ".InventoryKitInfo.PlayerNotKitValue");
        List<String> lorekitnotpurchased = GetSettings.kits.getConfig().getStringList("Kits." + kit + ".InventoryKitInfo.KitNotPurchased");
        int value = GetSettings.kits.getConfig().getInt("Kits." + kit + ".InventoryKitInfo.Value");

        for (String line : configLore) {
            lore.add(line.replace("&", "§").replace("{kit}", name));
        }
        if (playermoney >= value) {
            for (String line : lorekitnotpurchased) {
                lore.add(line.replace("&", "§").replace("{kit}", name).replace("{value}", "" + value));
            }
        } else {
            for (String line : lorenotkitvalue) {
                lore.add(line.replace("&", "§").replace("{kit}", name).replace("{value}", "" + value));
            }
        }

        return CreateItem.add(name, Material.getMaterial(Integer.parseInt(m[0])), !m[1].isEmpty() ? Integer.parseInt(m[1]) : 0, lore);
    }

    public static void handleNavigationClick(Player p, int currentPage, boolean next) {
        int newPage = next ? currentPage + 1 : currentPage - 1;
        open(p, newPage);
    }

    /**
     * Processa o clique em um kit
     */
    public static void handleKitClick(Player p, String kitName, int page) {
        // Verifica se o kit existe
        if (!KitAPI.kitExists(kitName)) {
            p.sendMessage("§cEste kit não existe ou está desativado!");
            return;
        }

        // Verifica se tem o kit
        boolean hasKit = KitAPI.hasKit(p, kitName);

        if (!hasKit) {
            if (hasKitValue(p, kitName)) {
                // Seleciona o kit
                PlayerKitsCache.addKit(p.getUniqueId(), kitName);
                p.sendMessage("§aVocê comprou o kit: " + kitName);
            } else {
                p.sendMessage("§cVocê não possui dinheiro o suficiente!");
            }

            // Reabre o inventário para atualizar visual
            p.closeInventory();

        } else {
            // Não possui - pode abrir loja ou mostrar mensagem
            p.sendMessage("§cVocê já possui o kit §f" + kitName);
            p.closeInventory();

            // Aqui você pode abrir a loja de kits
            // KitShop.open(p, kitName);
        }
    }
}
