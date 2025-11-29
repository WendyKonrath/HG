package org.hg.kitmanager.inventorys;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hg.database.PlayerKitsCache;
import org.hg.kitmanager.KitAPI;
import org.hg.utils.CreateItem;
import org.hg.utils.GetSettings;

import java.util.ArrayList;
import java.util.List;

public class KitSelector extends KitAPI {

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
        Inventory inv = Bukkit.createInventory(null, 6 * 9, GetSettings.kits.getConfig().getString("Kitselector.InventoryName", "§8Selecionar Kit").replace("&", "§"));

        List<String> PlayerKits = new ArrayList<>();
        for (String kit : kitlist()) {
            if (hasKit(p, kit)) {
                PlayerKits.add(kit);
            }
        }

        if (PlayerKits.isEmpty()) {
            p.sendMessage("§cNenhum kit disponível no momento!");
            return;
        }
        int kitsPerPage = kitslots.length;
        int totalPages = (int) Math.ceil((double) PlayerKits.size() / kitsPerPage);

        // Valida página
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        int startIndex = (page - 1) * kitsPerPage;
        int endIndex = Math.min(startIndex + kitsPerPage, PlayerKits.size());

        // Preenche slots com os kits
        int slotIndex = 0;
        for (int i = startIndex; i < endIndex; i++) {
            boolean isSelected = selectedKit(p, PlayerKits.get(i));
            inv.setItem(kitslots[slotIndex], kitItemInventory(PlayerKits.get(i), isSelected));
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
        ItemStack info = CreateItem.add("§eKit selecionado - §a" + getKit(p), Material.BARRIER);
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

    public static ItemStack kitItemInventory(String kit, Boolean isSelected) {
        String name = GetSettings.kits.getConfig().getString("Kits." + kit + ".InventoryKitInfo.Name").replace("&", "§");
        String[] m = GetSettings.kits.getConfig().getString("Kits." + kit + ".InventoryKitInfo.Id", "1:0").split(":");
        // Lore configurada
        List<String> lore = new ArrayList<>();
        List<String> configLore = GetSettings.kits.getConfig().getStringList("Kits." + kit + ".InventoryKitInfo.Lore");

        for (String line : configLore) {
            lore.add(line.replace("&", "§"));
        }
        if (isSelected) {
            lore.add("");
            lore.add(GetSettings.kits.getConfig().getString("Kits." + kit + ".InventoryKitInfo.KitSelected").replace("&", "§").replace("{name}", kit));
        } else {
            lore.add("");
            lore.add(GetSettings.kits.getConfig().getString("Kits." + kit + ".InventoryKitInfo.KitNotSelected").replace("&", "§").replace("{name}", kit));
        }
        ItemStack item = CreateItem.add(name, Material.getMaterial(Integer.parseInt(m[0])), !m[1].isEmpty() ? Integer.parseInt(m[1]) : 0, lore);
        if (isSelected) {

            item.addUnsafeEnchantment(Enchantment.LURE, 1);
            final ItemMeta itemMeta = item.getItemMeta();
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(itemMeta);
        }

        return item;
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

        if (hasKit) {
            if (selectedKit(p, kitName)) {
                // Seleciona o kit
                p.sendMessage("§cVocê já selecionou este kit!");
            } else {
                KitAPI.setKit(p, kitName);
                p.sendMessage("§aKit §f" + kitName + " §aselecionado com sucesso!");
            }

            // Reabre o inventário para atualizar visual
            p.closeInventory();

        } else {
            // Não possui - pode abrir loja ou mostrar mensagem
            p.sendMessage("§cVocê não possui o kit §f" + kitName);
            p.closeInventory();

            // Aqui você pode abrir a loja de kits
            // KitShop.open(p, kitName);
        }
    }
}
