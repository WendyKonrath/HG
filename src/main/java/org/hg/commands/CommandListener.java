package org.hg.commands;

import org.hg.Main;
import org.hg.commands.command.Coins;
import org.hg.commands.command.HG;

public class CommandListener {

    public static void setupCommands() {
        Main pl = Main.getInstance();

        pl.getCommand("hg").setExecutor(new HG());
        pl.getCommand("coins").setExecutor(new Coins());
    }
}
