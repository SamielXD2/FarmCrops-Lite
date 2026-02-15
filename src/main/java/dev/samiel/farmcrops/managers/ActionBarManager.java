package dev.samiel.farmcrops.managers;
import dev.samiel.farmcrops.FarmCrops;
import dev.samiel.farmcrops.models.PlayerSettings;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.text.DecimalFormat;
public class ActionBarManager {
    private final FarmCrops plugin;
    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");
    private final DecimalFormat weightFormat = new DecimalFormat("0.00");
    public ActionBarManager(FarmCrops plugin) {
        this.plugin = plugin;
    }
    public void sendHarvestNotification(Player player, String cropName, String tier, double weight, double earnings) {
        PlayerSettings.PlayerPreferences prefs = plugin.getPlayerSettings().getPreferences(player.getUniqueId());
        if (!prefs.showActionBar) {
            return;
        }
        try {
            String tierColor = getTierColor(tier);
            String message = String.format(
                "%s%s %s %s• %s%.2fkg • %s$%s",
                tierColor,
                getTierIcon(tier),
                tier,
                cropName,
                ChatColor.YELLOW,
                weight,
                ChatColor.GOLD,
                moneyFormat.format(earnings)
            );
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send action bar to " + player.getName() + ": " + e.getMessage());
        }
    }
    public void sendAutoSellNotification(Player player, int cropCount, double totalEarnings) {
        try {
            PlayerSettings.PlayerPreferences prefs = plugin.getPlayerSettings().getPreferences(player.getUniqueId());
            if (!prefs.showActionBar) {
                return;
            }
            String message = String.format(
                "%s⚡ Auto-Sold %s%d crops %s» %s$%s",
                ChatColor.GREEN,
                ChatColor.WHITE,
                cropCount,
                ChatColor.GRAY,
                ChatColor.GOLD,
                moneyFormat.format(totalEarnings)
            );
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send auto-sell action bar to " + player.getName() + ": " + e.getMessage());
        }
    }
    public void sendInfo(Player player, String message) {
        try {
            PlayerSettings.PlayerPreferences prefs = plugin.getPlayerSettings().getPreferences(player.getUniqueId());
            if (!prefs.showActionBar) {
                return;
            }
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send action bar info to " + player.getName() + ": " + e.getMessage());
        }
    }
    public void clearActionBar(Player player) {
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(""));
        } catch (Exception e) {
        }
    }
    private String getTierColor(String tier) {
        switch (tier.toLowerCase()) {
            case "common":    return ChatColor.GRAY.toString();
            case "uncommon":  return ChatColor.GREEN.toString();
            case "rare":      return ChatColor.BLUE.toString();
            case "epic":      return ChatColor.DARK_PURPLE.toString();
            case "legendary": return ChatColor.GOLD.toString();
            default:          return ChatColor.WHITE.toString();
        }
    }
    private String getTierIcon(String tier) {
        switch (tier.toLowerCase()) {
            case "common":    return "▪";
            case "uncommon":  return "▸";
            case "rare":      return "◆";
            case "epic":      return "★";
            case "legendary": return "✦";
            default:          return "•";
        }
    }
}
