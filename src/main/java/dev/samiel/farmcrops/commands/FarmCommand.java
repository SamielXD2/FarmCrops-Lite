package dev.samiel.farmcrops.commands;
import dev.samiel.farmcrops.FarmCrops;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
public class FarmCommand implements CommandExecutor {
    private final FarmCrops plugin;
    public FarmCommand(FarmCrops plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        Player player = (Player) sender;
        // /farm reset <player> - Admin only
        if (args.length >= 2 && args[0].equalsIgnoreCase("reset")) {
            if (!player.hasPermission("farmcrops.admin")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to reset player stats!");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Player '" + args[1] + "' is not online.");
                return true;
            }
            // Reset their stats
            plugin.getStatsManager().resetStats(target.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "✓ Successfully reset all farming stats for " + ChatColor.WHITE + target.getName());
            target.sendMessage("");
            target.sendMessage(ChatColor.YELLOW + "⚠ Your farming stats have been reset by an administrator.");
            target.sendMessage(ChatColor.GRAY + "All harvest counts, earnings, and records have been cleared.");
            target.sendMessage("");
            return true;
        }
        // /farm (no args) - Open main menu
        plugin.getMainMenuGUI().openGUI(player);
        return true;
    }
}
