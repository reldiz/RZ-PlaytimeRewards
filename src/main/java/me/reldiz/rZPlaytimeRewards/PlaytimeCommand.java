package me.reldiz.rZPlaytimeRewards.commands;

import me.reldiz.rZPlaytimeRewards.RZPlaytimeRewards;
import me.reldiz.rZPlaytimeRewards.gui.RewardGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlaytimeCommand implements CommandExecutor {

    private final RZPlaytimeRewards plugin;

    public PlaytimeCommand(RZPlaytimeRewards plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Reload komanda — prieinama TIK OP žaidėjams arba konsolei
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.isOp()) {
                sendMessage(sender, plugin.getConfig().getString("messages.no-permission"));
                return true;
            }
            plugin.reloadPluginConfigs();
            sendMessage(sender, plugin.getConfig().getString("messages.config-reloaded"));
            return true;
        }

        // Tikrinama ar tolimesnius veiksmus (GUI atidarymą) vykdo žaidėjas
        if (!(sender instanceof Player)) {
            sender.sendMessage("Ši komanda gali būti naudojama tik žaidėjo. Konsolės naudojimas: /pta reload");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("rzplaytimerewards.use")) {
            sendMessage(player, plugin.getConfig().getString("messages.no-permission"));
            return true;
        }

        new RewardGUI(plugin, player, 1).open();
        return true;
    }

    private void sendMessage(CommandSender sender, String message) {
        if (message != null && !message.isEmpty()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}