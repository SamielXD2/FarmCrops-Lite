package dev.samiel.farmcrops.commands;
import dev.samiel.farmcrops.FarmCrops;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
public class FarmAdminCommand implements CommandExecutor {
    private final FarmCrops plugin;
    public FarmAdminCommand(FarmCrops plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("farmcrops.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        plugin.getAdminPanelGUI().openMainPanel(player);
        return true;
    }
}
