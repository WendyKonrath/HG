package org.hg.commands.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hg.gamemanager.GameManager;

public class HG implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender Sender, Command command, String s, String[] strings) {
        Player p = (Player) Sender;
        GameManager.joinGame(p);
        return false;
    }
}
