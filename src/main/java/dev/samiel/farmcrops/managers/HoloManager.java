package dev.samiel.farmcrops.managers;
import dev.samiel.farmcrops.FarmCrops;
import dev.samiel.farmcrops.models.PlayerSettings;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import java.util.Arrays;
import java.util.List;
public class HoloManager {
    private final FarmCrops plugin;
    private int holoCounter = 0;
    public HoloManager(FarmCrops plugin) {
        this.plugin = plugin;
    }
    public void flashHarvest(Location loc, String playerName, String tier, double weight, double price, String cropName) {
        try {
            String tierColor = getTierColor(tier);
            List<String> lines = Arrays.asList(
                tierColor + "✦ " + tier.toUpperCase() + " " + cropName + " " + tierColor + "✦",
                ChatColor.GRAY + "Weight: " + ChatColor.WHITE + String.format("%.2f", weight) + "kg",
                ChatColor.GOLD + "+$" + String.format("%.2f", price)
            );
            String holoName = "harvest_" + System.currentTimeMillis() + "_" + (holoCounter++);
            Location holoLoc = loc.clone().add(0.5, 1.5, 0.5);
            Hologram hologram = DHAPI.createHologram(holoName, holoLoc, lines);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (hologram != null) {
                    hologram.delete();
                }
            }, 40L);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create hologram: " + e.getMessage());
        }
    }
    private String getTierColor(String tier) {
        return switch (tier.toLowerCase()) {
            case "legendary" -> ChatColor.GOLD.toString() + ChatColor.BOLD;
            case "epic" -> ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD;
            case "rare" -> ChatColor.BLUE.toString() + ChatColor.BOLD;
            case "uncommon" -> ChatColor.GREEN.toString();
            default -> ChatColor.WHITE.toString(); 
        };
    }
}
