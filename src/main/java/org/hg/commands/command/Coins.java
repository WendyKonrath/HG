package org.hg.commands.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hg.database.PlayerStatsCache;
import org.hg.utils.StringUtils;

public class Coins implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) return false;
        Player p = (Player) sender;

        if (args.length == 0) {
            p.sendMessage("§cUse: /coins <jogador> [add/remove/set] [quantia]");
            return false;
        }

        OfflinePlayer off = Bukkit.getOfflinePlayer(args[0]);

        boolean existe = off.isOnline() || off.hasPlayedBefore();

        if (!existe) {
            p.sendMessage("§cJogador não encontrado!");
            return false;
        }

        // Só mostrar coins
        if (args.length == 1) {
            int money = PlayerStatsCache.getMoney(off.getUniqueId());
            p.sendMessage("§aO jogador " + off.getName() + " possui " + money + " coins.");
            return false;
        }

        // Parte admin
        if (!sender.hasPermission("hg.admin")) {
            p.sendMessage("§cVocê não tem permissão.");
            return false;
        }

        if (args.length < 3 || !StringUtils.isInteger(args[2])) {
            p.sendMessage("§cUse /coins <player> <add|remove|set> <quantia>");
            return false;
        }

        int quantia = Integer.parseInt(args[2]);

        switch (args[1].toLowerCase()) {
            case "add":
                PlayerStatsCache.addMoney(off.getUniqueId(), quantia);
                p.sendMessage("§aVocê adicionou " + quantia + " coins para " + off.getName());
                break;

            case "remove":
                PlayerStatsCache.removeMoney(off.getUniqueId(), quantia);
                p.sendMessage("§aVocê removeu " + quantia + " coins de " + off.getName());
                break;

            case "set":
                PlayerStatsCache.setMoney(off.getUniqueId(), quantia);
                p.sendMessage("§aVocê definiu " + quantia + " coins para " + off.getName());
                break;

            default:
                p.sendMessage("§cUse /coins <player> <add|remove|set> <quantia>");
                break;
        }

        return false;
    }

}
