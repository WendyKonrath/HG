package org.hg.utils.WorldManager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.hg.gamemanager.GameManager;
import org.hg.gamemanager.GameStats;
import org.hg.utils.ConfigUtils;
import org.hg.utils.GetSettings;

import java.io.File;

public class WorldGenerator {
    public static void RenewMap(boolean deleteOld) {

        ConfigUtils cu = GetSettings.config;

        for (String mapName : cu.getConfig().getStringList("Worlds")) {

            World worldObj = Bukkit.getWorld(mapName);

            if (GameManager.getGameStats(worldObj) == GameStats.ENDED || GameManager.getGameStats(worldObj) == null) {

                // remove mundo velho
                if (deleteOld) {

                    if (worldObj != null) Bukkit.unloadWorld(worldObj, false);

                    File folder = new File(Bukkit.getWorldContainer(), mapName);
                    deleteFolder(folder);
                }

                // recria mundo
                WorldCreator wc = new WorldCreator(mapName);
                wc.generator("TerrainControl");

                World w = Bukkit.createWorld(wc);

                Block highest = w.getHighestBlockAt(0, 0);
                w.setSpawnLocation(highest.getX(), highest.getY() + 1, highest.getZ());

                w.getWorldBorder().setCenter(w.getSpawnLocation());
                w.getWorldBorder().setSize(cu.getConfig().getInt("Configuration.borderlimit"));

                // garante estado WAITING
                GameManager.setGameStats(w, GameStats.WAITING);
            }
        }
    }


    private static void deleteFolder(File file) {
        if (!file.exists()) return;

        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) deleteFolder(f);
                else f.delete();
            }
        }
        file.delete();
    }
}
