package org.hg.kitmanager.inventorys;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.hg.kitmanager.KitAPI;
import org.hg.utils.GetSettings;

public class KitSelectorListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player p = (Player) e.getWhoClicked();

        // Verifica se é o inventário de seletor de kits
        String invName = GetSettings.kits.getConfig().getString("Kitselector.InventoryName", "§8Seletor de Kits")
                .replace("&", "§");

        if (e.getView().getTitle() == null || !e.getView().getTitle().equals(invName)) {
            return;
        }

        // Cancela o evento para não mover itens
        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) {
            return;
        }

        int slot = e.getSlot();

        // Clique na seta de página anterior (slot 48)
        if (slot == 48 && clicked.getType().name().contains("ARROW")) {
            int currentPage = extractPageFromInventory(e.getInventory());
            KitSelector.handleNavigationClick(p, currentPage, false);
            return;
        }

        // Clique na seta de próxima página (slot 50)
        if (slot == 50 && clicked.getType().name().contains("ARROW")) {
            int currentPage = extractPageFromInventory(e.getInventory());
            KitSelector.handleNavigationClick(p, currentPage, true);
            return;
        }

        // Clique em um kit (slots 10-16, 19-25, 28-34)
        if (isKitSlot(slot)) {
            String kitName = extractKitNameFromItem(clicked.getItemMeta().getDisplayName());
            int currentPage = extractPageFromInventory(e.getInventory());

            if (kitName != null) {
                KitSelector.handleKitClick(p, kitName, currentPage);
            }
        }
    }

    /**
     * Verifica se o slot é um slot de kit
     */
    private boolean isKitSlot(int slot) {
        int[] kitSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };

        for (int kitSlot : kitSlots) {
            if (slot == kitSlot) return true;
        }

        return false;
    }

    /**
     * Extrai o nome do kit a partir do display name
     * Remove cores e indicadores como "✔"
     */
    private String extractKitNameFromItem(String displayName) {
        if (displayName == null) return null;

        // Remove todos os códigos de cor
        String clean = displayName.replaceAll("§[0-9a-fk-or]", "");

        // Remove indicadores
        clean = clean.replace("✔", "").trim();

        // Busca o kit correspondente na config
        for (String kit : KitAPI.kitlist()) {
            String configName = GetSettings.kits.getConfig()
                    .getString("Kits." + kit + ".InventoryKitInfo.Name", kit)
                    .replace("&", "§")
                    .replaceAll("§[0-9a-fk-or]", "")
                    .trim();

            if (configName.equalsIgnoreCase(clean)) {
                return kit;
            }
        }

        return null;
    }

    /**
     * Extrai o número da página atual do inventário
     * Lê do item de informação no slot 49
     */
    private int extractPageFromInventory(org.bukkit.inventory.Inventory inv) {
        ItemStack infoItem = inv.getItem(49);

        if (infoItem == null || !infoItem.hasItemMeta() || !infoItem.getItemMeta().hasLore()) {
            return 1;
        }

        // Procura na lore pela linha "Página: X/Y"
        for (String line : infoItem.getItemMeta().getLore()) {
            if (line.contains("Página:")) {
                try {
                    // Extrai o número antes da barra
                    String pageStr = line.split(":")[1].trim().split("/")[0].replaceAll("§[0-9a-fk-or]", "").trim();
                    return Integer.parseInt(pageStr);
                } catch (Exception e) {
                    // Se falhar, retorna página 1
                }
            }
        }

        return 1;
    }
}