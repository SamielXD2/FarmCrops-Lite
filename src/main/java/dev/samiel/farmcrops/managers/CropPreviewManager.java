package dev.samiel.farmcrops.managers;
import dev.samiel.farmcrops.FarmCrops;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.RayTraceResult;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class CropPreviewManager implements Listener {
    private final FarmCrops plugin;
    private final Map<UUID, Hologram> activeHolograms = new HashMap<>();
    private final Map<UUID, Block> lastLookedBlock = new HashMap<>();
    public CropPreviewManager(FarmCrops plugin) {
        this.plugin = plugin;
    }
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getYaw() == event.getTo().getYaw() &&
            event.getFrom().getPitch() == event.getTo().getPitch() &&
            event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        Player player = event.getPlayer();
        // NO PERMISSION CHECK - everyone can see it!
        if (!plugin.getConfig().getBoolean("holograms.right-click-preview", true)) return;
        // Check player's personal hologram setting
        if (plugin.getPlayerSettings() != null) {
            if (!plugin.getPlayerSettings().getPreferences(player.getUniqueId()).showHolograms) return;
        }
        RayTraceResult result = player.rayTraceBlocks(5.0);
        if (result == null || result.getHitBlock() == null) {
            if (lastLookedBlock.containsKey(player.getUniqueId())) {
                removeHologram(player);
                lastLookedBlock.remove(player.getUniqueId());
            }
            return;
        }
        Block block = result.getHitBlock();
        Block lastBlock = lastLookedBlock.get(player.getUniqueId());
        if (lastBlock != null && lastBlock.equals(block)) return;
        if (!isTrackedCrop(block.getType())) {
            if (lastBlock != null) {
                removeHologram(player);
                lastLookedBlock.remove(player.getUniqueId());
            }
            return;
        }
        if (!(block.getBlockData() instanceof Ageable)) return;
        Ageable ageable = (Ageable) block.getBlockData();
        int currentAge = ageable.getAge();
        int maxAge = ageable.getMaximumAge();
        lastLookedBlock.put(player.getUniqueId(), block);
        showCropPreview(player, block.getLocation(), currentAge, maxAge, block.getType());
    }
    private void showCropPreview(Player player, Location location, int currentAge, int maxAge, Material crop) {
        removeHologram(player);
        String[] lines = formatCropInfo(crop, currentAge, maxAge);
        Location hologramLoc = location.clone().add(0.5, 1.8, 0.5);
        String holoName = "crop-preview-" + player.getUniqueId();
        try {
            Hologram hologram = DHAPI.createHologram(holoName, hologramLoc, Arrays.asList(lines));
            activeHolograms.put(player.getUniqueId(), hologram);
        } catch (Exception e) {
            // Hologram with same name exists, delete and retry
            try {
                DHAPI.removeHologram(holoName);
                Hologram hologram = DHAPI.createHologram(holoName, hologramLoc, Arrays.asList(lines));
                activeHolograms.put(player.getUniqueId(), hologram);
            } catch (Exception ignored) {}
        }
    }
    private void removeHologram(Player player) {
        Hologram hologram = activeHolograms.remove(player.getUniqueId());
        if (hologram != null) {
            hologram.delete();
        }
    }
    private String[] formatCropInfo(Material crop, int currentAge, int maxAge) {
        String cropName = getCropDisplayName(crop);
        boolean ready = currentAge == maxAge;
        int percentage = (int) ((currentAge / (double) maxAge) * 100);
        String progressBar = buildProgressBar(percentage);
        String statusColor = ready ? "§a" : (percentage >= 50 ? "§e" : "§c");
        String statusText = ready ? "§a§lREADY TO HARVEST ✔" : statusColor + "Growing... " + percentage + "%";
        String[] tiers = getPossibleTiers();
        double basePrice = getBasePrice(crop);
        if (ready) {
            return new String[] {
                "§6§l" + cropName,
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                statusText,
                "§7Progress: " + progressBar,
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Est. Value: §a$" + String.format("%.0f", basePrice * 0.5) + " §7- §6$" + String.format("%.0f", basePrice * 5.0),
                "§7Possible Tiers: " + tiers[0],
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§e⚡ Right-click to harvest!"
            };
        } else {
            return new String[] {
                "§e§l" + cropName,
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                statusText,
                "§7Progress: " + progressBar,
                "§7Stage: §f" + currentAge + "§7/§f" + maxAge,
                "§8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                "§7Est. Value: §a$" + String.format("%.0f", basePrice * 0.5) + " §7- §6$" + String.format("%.0f", basePrice * 5.0),
                "§7Possible Tiers: " + tiers[0]
            };
        }
    }
    private String buildProgressBar(int percentage) {
        int filled = percentage / 10;
        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < 10; i++) {
            if (i < filled) {
                bar.append("█");
            } else if (i == filled) {
                bar.append("§e█");
            } else {
                bar.append("§7░");
            }
        }
        bar.append(" §f").append(percentage).append("%");
        return bar.toString();
    }
    private String getCropDisplayName(Material crop) {
        switch (crop) {
            case WHEAT:             return "Wheat";
            case CARROTS:           return "Carrot";
            case POTATOES:          return "Potato";
            case BEETROOTS:         return "Beetroot";
            case NETHER_WART:       return "Nether Wart";
            case COCOA:             return "Cocoa";
            case SWEET_BERRY_BUSH:  return "Sweet Berries";
            default:                return crop.name().replace("_", " ");
        }
    }
    private double getBasePrice(Material crop) {
        switch (crop) {
            case WHEAT:             return plugin.getConfig().getDouble("prices.wheat", 10.0);
            case CARROTS:           return plugin.getConfig().getDouble("prices.carrot", 12.0);
            case POTATOES:          return plugin.getConfig().getDouble("prices.potato", 11.0);
            case BEETROOTS:         return plugin.getConfig().getDouble("prices.beetroot", 13.0);
            default:                return plugin.getConfig().getDouble("prices.default", 10.0);
        }
    }
    private String[] getPossibleTiers() {
        return new String[] {
            "§7Common §a§lU§9R§5E§6L§cM"
        };
    }
    private boolean isTrackedCrop(Material material) {
        return material == Material.WHEAT ||
               material == Material.CARROTS ||
               material == Material.POTATOES ||
               material == Material.BEETROOTS ||
               material == Material.NETHER_WART ||
               material == Material.COCOA ||
               material == Material.SWEET_BERRY_BUSH;
    }
    public void cleanup() {
        for (Hologram hologram : activeHolograms.values()) {
            hologram.delete();
        }
        activeHolograms.clear();
        lastLookedBlock.clear();
    }
}
