package org.hg.utils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hg.Main;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigUtils
{
    private File file;
    private FileConfiguration config;
    private static Map<File, ConfigUtils> map;

    static {
        ConfigUtils.map = new HashMap<File, ConfigUtils>();
    }

    public ConfigUtils(final File file) {
        this.file = file;
        if (!this.file.exists()) {
            ((Main) Main.getPlugin(Main.class)).saveResource(file.getName(), true);
        }
        try {
            this.config = (FileConfiguration)YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        catch (FileNotFoundException e2) {
            e2.printStackTrace();
        }
    }

    public void save() {
        try {
            this.config.save(this.file);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public static ConfigUtils getConfig(final File file) {
        ConfigUtils cu = ConfigUtils.map.get(file);
        if (cu == null) {
            cu = new ConfigUtils(file);
            ConfigUtils.map.put(file, cu);
        }
        return cu;
    }
}
