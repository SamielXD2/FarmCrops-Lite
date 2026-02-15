package dev.samiel.farmcrops.commands;
import dev.samiel.farmcrops.FarmCrops;
import dev.samiel.farmcrops.gui.AchievementGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
public class AchievementCommand implements CommandExecutor {
    private final FarmCrops plugin;
    public AchievementCommand(FarmCrops plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;
        plugin.getAchievementGUI().openGUI(player);
        return true;
    }
}
