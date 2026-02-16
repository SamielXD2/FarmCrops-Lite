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
        if (!plugin.getConfig().getBoolean("show-welcome-message", true)) {
            return;
        }
        if (!player.hasPermission("farmcrops.admin")) {
            return;
        }
        List<String> messages = plugin.getConfig().getStringList("messages.welcome");
        if (messages.isEmpty()) {
            return;
        }
        for (String message : messages) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}
