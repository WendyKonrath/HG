package org.hg.kitmanager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.hg.database.PlayerKitsCache;
import org.hg.database.PlayerStatsCache;
import org.hg.gamemanager.GameManager;
import org.hg.gamemanager.events.GameStartEvent;
import org.hg.gamemanager.utils.SetGameItems;
import org.hg.kitmanager.kits.hermit.Hermit;
import org.hg.utils.GetSettings;

import java.util.*;

public class KitAPI implements Listener {
    private static final Map<UUID, String> KitSelected = new HashMap<>();
    private static final ConfigurationSection kitco = GetSettings.kits.getConfig().getConfigurationSection("Kits");

    private static final Set<String> kits = GetSettings.kits
            .getConfig()
            .getConfigurationSection("Kits")
            .getKeys(false);

    public static List<String> kitlist() {
        List<String> list = new ArrayList<>();

        for (String kit : kits) {
            if (kitco.getBoolean(kit + ".Activated")) {
                list.add(kit);
            }
        }

        // Aqui você ordena alfabeticamente
        list.sort(String::compareToIgnoreCase);

        return list;
    }

    public static boolean hasKit(Player p, String kit) {
        return PlayerKitsCache.hasKit(p.getUniqueId(), kit) || kitco.getBoolean(kit + ".Free") || p.hasPermission(kitco.getString(kit + ".Perm", ""));
    }

    public static boolean hasKitValue(Player p, String kit) {
        int kitvalue = kitco.getInt(kit + ".InventoryKitInfo.Value");
        return PlayerStatsCache.getMoney(p.getUniqueId()) >= kitvalue;
    }

    public static boolean selectedKit(Player p, String kit) {
        if (!KitSelected.containsKey(p.getUniqueId())) return false;
        if (KitSelected.get(p.getUniqueId()).equals(kit)) {
            return true;
        }
        return false;
    }

    public static void setKit(Player p, String kit) {
        KitSelected.put(p.getUniqueId(), kit);
    }

    public static void removeKit(Player p) {
        KitSelected.remove(p.getUniqueId());
    }

    public static String getKit(Player p) {
        if (KitSelected.get(p.getUniqueId()) != null) {
            return KitSelected.get(p.getUniqueId());
        }
        return "Nenhum!";
    }

    public static void purchasedKit(Player p, String kit) {

    }

    public static boolean kitExists(String kitName) {
        if (kitco == null) return false;

        return kitco.contains(kitName) &&
                kitco.getBoolean(kitName + ".Activated", true);
    }

    @EventHandler
    public void onGameStart(GameStartEvent e) {
        // Este é um exemplo - você precisará criar este evento ou
        // chamar applyHermitEffect() no momento certo do seu código
        for (Player p : GameManager.GetPlayersInGame(e.getWorld())) {
            SetGameItems.StartedGame(p);
            String selectedKit = getKit(p);

            if ("Hermit".equalsIgnoreCase(selectedKit)) {
                Hermit.applyHermitEffect(p);
            }
        }
    }
}
