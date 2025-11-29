package org.hg.kitmanager.kits.Cultivator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.hg.gamemanager.GameManager;
import org.hg.gamemanager.GameStats;
import org.hg.kitmanager.KitAPI;

import java.util.HashMap;

public class Cultivator implements Listener {

    TreeType[] trees = {
            TreeType.TREE,
            TreeType.TALL_REDWOOD,
            TreeType.TALL_BIRCH,
            TreeType.SMALL_JUNGLE,
            TreeType.ACACIA,
            TreeType.DARK_OAK
    };

    int[] treeheight = {
            5, 7, 6, 5, 6, 7
    };

    HashMap<Player, Byte> tree = new HashMap<>();

    @EventHandler
    public void onPlantEvent(BlockPlaceEvent e) {
        String selectedKit = KitAPI.getKit(e.getPlayer());
        if ("Cultivator".equalsIgnoreCase(selectedKit)) {
            if (GameManager.getGameStats(GameManager.GetPlayerGame(e.getPlayer())) == GameStats.GAME || GameManager.getGameStats(GameManager.GetPlayerGame(e.getPlayer())) == GameStats.INVINCIBLE) {
                Block b = e.getBlockPlaced();
                ItemStack mainHand = e.getPlayer().getInventory().getItemInHand();
                // Faz a planta crescer instantaneamente
                Material type = b.getType();

                // Crops padrão (wheat, carrot, potato, nether wart)
                if (type == Material.CROPS || type == Material.CARROT || type == Material.POTATO || type == Material.NETHER_WARTS) {
                    BlockState bs = b.getState();
                    byte age = (type == Material.NETHER_WARTS) ? (byte) 3 : (byte) 7;
                    bs.setData(new org.bukkit.material.MaterialData(type, age));
                    bs.update(true);
                    return;
                }

                // Cocoa: preserve facing and apenas altere o tamanho para maduro
                if (type == Material.COCOA) {
                    BlockState bs = b.getState();
                    if (bs.getData() instanceof org.bukkit.material.CocoaPlant) {
                        org.bukkit.material.CocoaPlant cocoa = (org.bukkit.material.CocoaPlant) bs.getData();
                        cocoa.setSize(org.bukkit.material.CocoaPlant.CocoaPlantSize.LARGE); // maduro
                        bs.setData(cocoa);
                        bs.update(true);
                    }
                    return;
                }
                if (b.getType() == Material.SAPLING) {
                    // Faz a árvore crescer instantaneamente
                    if (treeHasNoSpace(treeheight[b.getData()], e.getPlayer(), b)) {
                        e.setCancelled(true);
                        return;
                    }
                    if (TreeType.DARK_OAK == trees[b.getData()]) {
                        if (mainHand.getAmount() >= 4) {
                            tree.put(e.getPlayer(), b.getData());
                            b.setType(Material.AIR);
                            mainHand.setAmount(mainHand.getAmount() - 4);
                            b.getWorld().generateTree(b.getLocation(), trees[tree.get(e.getPlayer())]);
                            tree.clear();
                        } else {
                            e.setCancelled(true);
                            e.getPlayer().sendMessage("§cVocê precisa de 4 mudas de carvalho escuro para plantar essa árvore.");
                            return;
                        }
                    } else {
                        if (mainHand.getAmount() >= 1) {
                            tree.put(e.getPlayer(), b.getData());
                            b.setType(Material.AIR);
                            mainHand.setAmount(mainHand.getAmount() - 1);
                            b.getWorld().generateTree(b.getLocation(), trees[tree.get(e.getPlayer())]);
                            tree.clear();
                        } else {
                            e.setCancelled(true);
                            e.getPlayer().sendMessage("§cVocê precisa de uma muda para plantar essa árvore.");
                            return;

                        }
                    }

                }
            }
        }
    }

    private boolean treeHasNoSpace(int treeHeight, Player player, Block block) {
        // A localização inicial (bloco onde a semente será plantada)
        Location checkLoc = block.getLocation();

        // Altura máxima + 1 (para o topo da árvore)
        for (int y = 1; y < treeHeight; y++) {

            // Obtém o bloco acima (Y + 1, Y + 2, etc.)
            Block currentBlock = checkLoc.getWorld().getBlockAt(
                    checkLoc.getBlockX(),
                    checkLoc.getBlockY() + y,
                    checkLoc.getBlockZ()
            );

            Material type = currentBlock.getType();

            // Verifica se o bloco NÃO é um material que permite o crescimento da árvore.
            // O Bloco precisa ser AIR ou um material substituível (folhas, vinhas, etc.)
            // Adiciono WATER e LAVA como checagens de segurança.
            if (type != Material.AIR &&
                    type != Material.LEAVES && // Materiais de folha
                    type != Material.VINE &&  // Vinhas, etc.
                    type != Material.LONG_GRASS &&
                    type != Material.DOUBLE_PLANT &&
                    type != Material.WATER && // Impede crescimento em água
                    type != Material.STATIONARY_WATER &&
                    type != Material.LAVA && // Impede crescimento em lava
                    type != Material.STATIONARY_LAVA) {

                // Se encontrarmos um bloco sólido ou obstáculo, a árvore não tem espaço.
                sendNoSpaceMessage(player);
                return true; // Retorna TRUE, indicando que a árvore NÃO PODE CRESCER (No Space)
            }
        }

        // Se o loop terminar sem encontrar obstáculos, a árvore tem espaço.
        return false; // Retorna FALSE, indicando que a árvore PODE CRESCER
    }

    // Método auxiliar para enviar a mensagem
    private void sendNoSpaceMessage(Player player) {
        // Use ChatColor para garantir a compatibilidade e a cor vermelha.
        player.sendMessage(ChatColor.RED + "Desculpe, mas parece que você não pode crescer esta árvore aqui.");
    }

}
