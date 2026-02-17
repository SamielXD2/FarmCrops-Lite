package dev.samiel.farmcrops.commands;
import dev.samiel.farmcrops.FarmCrops;
import dev.samiel.farmcrops.managers.StatsManager;
import dev.samiel.farmcrops.listeners.CropListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Map;
public class StatsCommand implements CommandExecutor {
    private final FarmCrops plugin;
    public StatsCommand(FarmCrops plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /farmstats reset <player>  — OP only
        if (args.length >= 2 && args[0].equalsIgnoreCase("reset")) {
            if (!(sender instanceof Player) || !((Player) sender).hasPermission("farmcrops.admin")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to reset stats!");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + args[1] + "' is not online.");
                return true;
            }
            plugin.getStatsManager().resetStats(target.getUniqueId());
            sender.sendMessage(ChatColor.GREEN + "✓ Reset all stats for " + ChatColor.WHITE + target.getName());
            target.sendMessage(ChatColor.YELLOW + "Your FarmCrops stats have been reset by an admin.");
            return true;
        }
        // Must be a player for everything below
        if (!(sender instanceof Player)) {
            sender.sendMessage("Use: /farmstats [player] | /farmstats reset <player>");
            return true;
        }
        Player player = (Player) sender;
        // /farmstats <player>
        if (args.length > 0) {
            if (!player.hasPermission("farmcrops.stats.others") && !player.hasPermission("farmcrops.admin")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to view other players' stats.");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Player '" + args[0] + "' is not online.");
                return true;
            }
            displayStats(player, target.getName(), plugin.getStatsManager().getStats(target.getUniqueId()));
            return true;
        }
        // /farmstats (self)
        displayStats(player, player.getName(), plugin.getStatsManager().getStats(player.getUniqueId()));
        return true;
    }
    private void displayStats(Player viewer, String targetName, StatsManager.PlayerStats stats) {
        String bar = ChatColor.DARK_GREEN + "══════════════════════════════";
        viewer.sendMessage("");
        viewer.sendMessage(bar);
        viewer.sendMessage(ChatColor.GREEN + "  🌾 FarmCrops Stats — " + ChatColor.WHITE + targetName);
        viewer.sendMessage(bar);
        viewer.sendMessage("");
        viewer.sendMessage(ChatColor.YELLOW + "  Overview");
        viewer.sendMessage(ChatColor.GRAY + "  Total Harvests: " + ChatColor.WHITE + stats.totalHarvests);
        viewer.sendMessage(ChatColor.GRAY + "  Total Earnings: " + ChatColor.GOLD + "$" + String.format("%.2f", stats.totalEarnings));
        viewer.sendMessage("");
        viewer.sendMessage(ChatColor.YELLOW + "  Harvests by Tier");
        viewer.sendMessage(ChatColor.GRAY        + "  Common:    " + ChatColor.WHITE + stats.commonHarvests);
        viewer.sendMessage(ChatColor.AQUA        + "  Rare:      " + stats.rareHarvests);
        viewer.sendMessage(ChatColor.LIGHT_PURPLE + "  Epic:      " + stats.epicHarvests);
        viewer.sendMessage(ChatColor.GOLD        + "  Legendary: " + stats.legendaryHarvests);
        viewer.sendMessage(ChatColor.RED         + "  Mythic:    " + stats.mythicHarvests);
        viewer.sendMessage("");
        if (!stats.cropHarvests.isEmpty()) {
            viewer.sendMessage(ChatColor.YELLOW + "  Harvests by Crop");
            for (Map.Entry<String, Integer> entry : stats.cropHarvests.entrySet()) {
                try {
                    String cropName = CropListener.formatName(org.bukkit.Material.valueOf(entry.getKey()));
                    double earnings = stats.cropEarnings.getOrDefault(entry.getKey(), 0.0);
                    viewer.sendMessage(ChatColor.GRAY + "  " + cropName + ": "
                        + ChatColor.WHITE + entry.getValue() + " harvests"
                        + ChatColor.GRAY + " | " + ChatColor.GOLD + "$" + String.format("%.2f", earnings));
                } catch (Exception ignored) {}
            }
            viewer.sendMessage("");
        }
        viewer.sendMessage(ChatColor.YELLOW + "  Personal Records");
        if (!stats.bestDropTier.equals("none")) {
            try {
                String bestCrop = CropListener.formatName(org.bukkit.Material.valueOf(stats.bestDropCrop));
                viewer.sendMessage(ChatColor.GRAY + "  Best Drop:     " + ChatColor.GOLD + "$" + String.format("%.2f", stats.bestDropValue)
                    + ChatColor.GRAY + " (" + stats.bestDropTier + " " + bestCrop + ", " + stats.bestDropWeight + "kg)");
            } catch (Exception ignored) {}
        }
        if (!stats.heaviestTier.equals("none")) {
            try {
                String heavyCrop = CropListener.formatName(org.bukkit.Material.valueOf(stats.heaviestCrop));
                viewer.sendMessage(ChatColor.GRAY + "  Heaviest Crop: " + ChatColor.WHITE + stats.heaviestWeight + "kg"
                    + ChatColor.GRAY + " (" + stats.heaviestTier + " " + heavyCrop + ")");
            } catch (Exception ignored) {}
        }
        viewer.sendMessage("");
        viewer.sendMessage(bar);
    }
}
