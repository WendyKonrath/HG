package org.hg.utils;

import java.io.File;

public class GetSettings {
    public static ConfigUtils config = ConfigUtils.getConfig(new File("plugins/HG/config.yml"));
    public static ConfigUtils messages = ConfigUtils.getConfig(new File("plugins/HG/messages.yml"));
    public static ConfigUtils kits = ConfigUtils.getConfig(new File("plugins/HG/kits.yml"));


}
