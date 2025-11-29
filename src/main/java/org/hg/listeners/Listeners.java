package org.hg.listeners;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.hg.Main;
import org.hg.gamemanager.GameEventsManager;
import org.hg.kitmanager.KitAPI;
import org.hg.kitmanager.inventorys.KitSelectorListener;
import org.hg.kitmanager.inventorys.ShopListener;
import org.hg.kitmanager.kits.Cultivator.Cultivator;
import org.hg.listeners.player.PlayerDisconnectEvent;
import org.hg.listeners.player.PlayerJoinEvent;

public class Listeners {

    public static void setupListeners() {
        Main plugin = Main.getInstance();
        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(new GameEventsManager(), plugin);
        pm.registerEvents(new PlayerJoinEvent(), plugin);
        pm.registerEvents(new PlayerDisconnectEvent(), plugin);
        pm.registerEvents(new KitSelectorListener(), plugin);
        pm.registerEvents(new ShopListener(), plugin);
        pm.registerEvents(new KitAPI(), plugin);


        //Kits
        pm.registerEvents(new Cultivator(), plugin);

    }
}
