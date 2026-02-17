package dev.samiel.farmcrops.listeners;
import dev.samiel.farmcrops.FarmCrops;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.List;
public class JoinListener implements Listener {
    private final FarmCrops plugin;
    public JoinListener(FarmCrops plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // ONLY show to OPs/admins - NEVER to regular members
        if (!player.hasPermission("farmcrops.admin")) {
            return;
        }
        // Check if disabled in config
        if (!plugin.getConfig().getBoolean("show-welcome-message", true)) {
            return;
        }
        // Only show ONCE (first time OP joins, uses hasPlayedBefore)
        // Removed - show every time to OP so they see it on each login
        List<String> messages = plugin.getConfig().getStringList("messages.welcome");
        if (messages.isEmpty()) {
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "━━━━━━━━ " + ChatColor.YELLOW + "FARMCROPS" + ChatColor.GOLD + " ━━━━━━━━");
            player.sendMessage(ChatColor.GREEN + "Welcome back! FarmCrops v" + plugin.getDescription().getVersion() + " is running.");
            player.sendMessage(ChatColor.GRAY + "Type /farm to manage the plugin.");
            player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            return;
        }
        for (String message : messages) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}
