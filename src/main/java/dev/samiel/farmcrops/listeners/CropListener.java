package dev.samiel.farmcrops.listeners;
import dev.samiel.farmcrops.FarmCrops;
import dev.samiel.farmcrops.models.PlayerSettings;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
public class CropListener implements Listener {
    private final FarmCrops plugin;
    public static final NamespacedKey WEIGHT_KEY = new NamespacedKey("farmcrops", "weight");
    public static final NamespacedKey TIER_KEY   = new NamespacedKey("farmcrops", "tier");
    public static final NamespacedKey CROP_KEY   = new NamespacedKey("farmcrops", "crop");
    private static final Material[] TRACKED_CROPS = {
            Material.WHEAT, Material.CARROTS, Material.POTATOES,
            Material.BEETROOTS, Material.MELON
    };
    public CropListener(FarmCrops plugin) {
        this.plugin = plugin;
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!isTrackedCrop(block.getType())) return;
        Player player = event.getPlayer();
        if (!plugin.getConfig().getBoolean("custom-crops.enabled", true)) {
            return;
        }
        if (!player.hasPermission("farmcrops.harvest")) return;
        if (block.getType() == Material.MELON) {
            try {
                processCropHarvest(player, block, event);
            } catch (Exception e) {
                plugin.getLogger().severe("Error processing melon harvest for " + player.getName() + ": " + e.getMessage());
                player.sendMessage(ChatColor.RED + "✗ Error harvesting melon!");
                player.sendMessage(ChatColor.GRAY + "Make sure you have the 'farmcrops.harvest' permission.");
                e.printStackTrace();
            }
            return;
        }
        if (!(block.getBlockData() instanceof Ageable)) return;
        Ageable ageable = (Ageable) block.getBlockData();
        if (ageable.getAge() != ageable.getMaximumAge()) return;
        try {
            processCropHarvest(player, block, event);
        } catch (Exception e) {
            plugin.getLogger().severe("Error processing harvest for " + player.getName() + ": " + e.getMessage());
            player.sendMessage(ChatColor.RED + "✗ Error harvesting crop! Check console for details.");
            e.printStackTrace();
        }
    }
    private void processCropHarvest(Player player, Block block, BlockBreakEvent event) {
        event.setCancelled(true);
        block.setType(Material.AIR);
        double weight = generateWeight();
        String tier = determineTier();
        String color = getTierColor(tier);
        double price = calculatePrice(block.getType(), weight, tier);
        Location dropLoc = block.getLocation().add(0.5, 0.5, 0.5);
        PlayerSettings.Preferences prefs = plugin.getPlayerSettings().getPreferences(player);
        if (prefs.autoSellEnabled) {
            try {
                if (plugin.getEconomy() != null) {
                    plugin.getEconomy().depositPlayer(player, price);
                    if (prefs.showHarvestParticles && plugin.isHoloEnabled()) {
                        block.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, dropLoc, 10, 0.3, 0.3, 0.3, 0);
                    }
                    if (prefs.showHarvestMessages) {
                        player.sendMessage(colorize(color) + "+" + weight + "kg " + formatName(block.getType()) + 
                            ChatColor.GREEN + " → " + ChatColor.GOLD + "+$" + String.format("%.2f", price));
                    }
                    if (plugin.getActionBarManager() != null) {
                        plugin.getActionBarManager().sendHarvestNotification(
                            player, formatName(block.getType()), tier, weight, price
                        );
                    }
                    plugin.getStatsManager().recordHarvest(player, block.getType(), tier, weight, price);
                    checkAchievements(player, tier);
                } catch (Exception e) {
                    plugin.getLogger().warning("Error during auto-sell for " + player.getName() + ": " + e.getMessage());
                    player.sendMessage(ChatColor.RED + "✗ Auto-sell failed! Check console for details.");
                }
            } else {
                try {
                    Material dropMat = getDropMaterial(block.getType());
                    ItemStack item = new ItemStack(dropMat, 1);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        PersistentDataContainer pdc = meta.getPersistentDataContainer();
                        pdc.set(WEIGHT_KEY, PersistentDataType.DOUBLE, weight);
                        pdc.set(TIER_KEY, PersistentDataType.STRING, tier);
                        pdc.set(CROP_KEY, PersistentDataType.STRING, block.getType().name());
                        List<String> lore = new ArrayList<>();
                        lore.add(colorize(color) + "Tier: " + capitalize(tier));
                        lore.add(colorize("&7Weight: &f" + weight + " kg"));
                        lore.add(colorize("&7Price: &a$" + String.format("%.2f", price)));
                        meta.setLore(lore);
                        String fullName = capitalize(tier) + " " + formatName(block.getType());
                        meta.setDisplayName(colorize(color) + fullName);
                        item.setItemMeta(meta);
                    }
                    player.getWorld().dropItemNaturally(dropLoc, item);
                    if (plugin.getActionBarManager() != null) {
                        plugin.getActionBarManager().sendHarvestNotification(
                            player, formatName(block.getType()), tier, weight, price
                        );
                    }
                    plugin.getStatsManager().recordHarvest(player, block.getType(), tier, weight, price);
                    checkAchievements(player, tier);
                    if (prefs.showHarvestParticles && plugin.isHoloEnabled()) {
                        block.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, dropLoc, 5);
                    }
                    if (plugin.isHoloEnabled()) {
                        plugin.getHoloManager().spawnTemporaryHologram(
                            dropLoc, player.getName(), tier, weight, price, formatName(block.getType())
                        );
                    }
                } catch (Exception e) {
                    plugin.getLogger().severe("Error creating crop item: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            if (plugin.getConfig().getBoolean("seeds.enabled", true)) {
                int dropChance = plugin.getConfig().getInt("seeds.drop-chance", 100);
                if (ThreadLocalRandom.current().nextInt(100) < dropChance) {
                    Material seedType = getSeedType(block.getType());
                    if (seedType != null) {
                        block.getWorld().dropItemNaturally(dropLoc, new ItemStack(seedType, 1));
                    }
                }
            }
        }
        private double generateWeight() {
            double min = plugin.getConfig().getDouble("weight.min", 0.5);
            double max = plugin.getConfig().getDouble("weight.max", 5.0);
            double weight = min + (ThreadLocalRandom.current().nextDouble() * (max - min));
            return Math.round(weight * 10.0) / 10.0;
        }
        private String determineTier() {
            double roll = ThreadLocalRandom.current().nextDouble(100);
            double cumulative = 0;
            for (String tierName : new String[]{"common", "uncommon", "rare", "epic", "legendary", "mythic"}) {
                cumulative += plugin.getConfig().getDouble("tiers." + tierName + ".chance", 0);
                if (roll < cumulative) {
                    return tierName;
                }
            }
            return "common";
        }
        private double calculatePrice(Material cropType, double weight, String tier) {
            double basePrice = plugin.getCropPrice(cropType);
            double multiplier = plugin.getConfig().getDouble("tiers." + tier + ".multiplier", 1.0);
            return basePrice * weight * multiplier;
        }
        private String getTierColor(String tier) {
            return plugin.getConfig().getString("tiers." + tier + ".color", "&f");
        }
        private Material getSeedType(Material cropType) {
            switch (cropType) {
                case WHEAT:     return Material.WHEAT_SEEDS;
                case CARROTS:   return Material.CARROT;
                case POTATOES:  return Material.POTATO;
                case BEETROOTS: return Material.BEETROOT_SEEDS;
                case MELON:     return Material.MELON_SEEDS;
                default:        return null;
            }
        }
        private Material getDropMaterial(Material blockType) {
            switch (blockType) {
                case WHEAT:     return Material.WHEAT;
                case CARROTS:   return Material.CARROT;
                case POTATOES:  return Material.POTATO;
                case BEETROOTS: return Material.BEETROOT;
                case MELON:     return Material.MELON_SLICE;
                default:        return blockType;
            }
        }
        private boolean isTrackedCrop(Material material) {
            for (Material crop : TRACKED_CROPS) {
                if (crop == material) return true;
            }
            return false;
        }
        private String capitalize(String s) {
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
        public static String formatName(Material m) {
            String name = m.name();
            if (name.endsWith("S")) {
                name = name.substring(0, name.length() - 1);
            }
            return name.charAt(0) + name.substring(1).toLowerCase().replace("_", " ");
        }
        private String colorize(String s) {
            return ChatColor.translateAlternateColorCodes('&', s);
        }
        private void checkAchievements(Player player, String tier) {
            if (plugin.getAchievementManager() == null) return;
            int totalHarvests = plugin.getStatsManager().getStats(player.getUniqueId()).totalHarvests;
            double totalEarnings = plugin.getStatsManager().getStats(player.getUniqueId()).totalEarnings;
            if (totalHarvests == 1) {
                plugin.getAchievementManager().checkAndGrant(player, "first_harvest");
            }
            if (totalHarvests == 100) {
                plugin.getAchievementManager().checkAndGrant(player, "100_harvests");
            }
            if (totalHarvests == 1000) {
                plugin.getAchievementManager().checkAndGrant(player, "1000_harvests");
            }
            if (tier.equals("rare")) {
                plugin.getAchievementManager().checkAndGrant(player, "first_rare");
            }
            if (tier.equals("epic")) {
                plugin.getAchievementManager().checkAndGrant(player, "first_epic");
            }
            if (tier.equals("legendary")) {
                plugin.getAchievementManager().checkAndGrant(player, "first_legendary");
            }
            if (tier.equals("mythic")) {
                plugin.getAchievementManager().checkAndGrant(player, "first_mythic");
            }
            if (totalEarnings >= 1000 && totalEarnings < 10000) {
                plugin.getAchievementManager().checkAndGrant(player, "earn_1000");
            }
            if (totalEarnings >= 10000 && totalEarnings < 100000) {
                plugin.getAchievementManager().checkAndGrant(player, "earn_10000");
            }
            if (totalEarnings >= 100000) {
                plugin.getAchievementManager().checkAndGrant(player, "earn_100000");
            }
        }
    }
