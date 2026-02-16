package dev.samiel.farmcrops.managers;
import dev.samiel.farmcrops.FarmCrops;
import dev.samiel.farmcrops.models.PlayerSettings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.util.*;
public class AchievementManager {
    private final FarmCrops plugin;
    private final Map<UUID, Set<String>> playerAchievements = new HashMap<>();
    private final Map<String, AchievementData> achievementData = new HashMap<>();
    private File achievementsFile;
    private FileConfiguration achievementsConfig;
    public AchievementManager(FarmCrops plugin) {
        this.plugin = plugin;
        loadAchievements();
        initializeAchievements();
    }
    private void loadAchievements() {
        achievementsFile = new File(plugin.getDataFolder(), "achievements.yml");
        if (!achievementsFile.exists()) {
            try {
                achievementsFile.createNewFile();
            } catch (Exception e) {
                plugin.getLogger().warning("Could not create achievements.yml: " + e.getMessage());
            }
        }
        achievementsConfig = YamlConfiguration.loadConfiguration(achievementsFile);
        for (String key : achievementsConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                List<String> achievements = achievementsConfig.getStringList(key);
                playerAchievements.put(uuid, new HashSet<>(achievements));
            } catch (Exception ignored) {}
        }
    }
    private void initializeAchievements() {
        registerAchievement("first_harvest", "First Harvest", "Harvest your first crop!", 1, AchievementType.HARVEST);
        registerAchievement("hundred_harvest", "Century Farmer", "Harvest 100 crops!", 100, AchievementType.HARVEST);
        registerAchievement("thousand_harvest", "Master Farmer", "Harvest 1,000 crops!", 1000, AchievementType.HARVEST);
        registerAchievement("first_thousand", "Money Maker", "Earn $1,000 from crops!", 1000, AchievementType.EARNINGS);
        registerAchievement("ten_thousand", "Rich Farmer", "Earn $10,000 from crops!", 10000, AchievementType.EARNINGS);
        registerAchievement("hundred_thousand", "Crop Tycoon", "Earn $100,000 from crops!", 100000, AchievementType.EARNINGS);
        registerAchievement("first_epic", "Epic Harvester", "Harvest an Epic tier crop!", 1, AchievementType.TIER_EPIC);
        registerAchievement("first_legendary", "Legendary Harvester", "Harvest a Legendary tier crop!", 1, AchievementType.TIER_LEGENDARY);
        registerAchievement("first_mythic", "Mythic Legend", "Harvest a Mythic tier crop!", 1, AchievementType.TIER_MYTHIC);
        registerAchievement("wheat_master", "Wheat Master", "Harvest 1,000 wheat!", 1000, AchievementType.COLLECTION_WHEAT);
    }
    private void registerAchievement(String id, String name, String description, int requirement, AchievementType type) {
        achievementData.put(id, new AchievementData(id, name, description, requirement, type));
    }
    public void checkAchievements(Player player) {
        UUID uuid = player.getUniqueId();
        int totalHarvests = plugin.getStatsManager().getTotalHarvests(uuid);
        double totalEarnings = plugin.getStatsManager().getTotalEarnings(uuid);
        for (AchievementData ach : achievementData.values()) {
            if (hasAchievement(uuid, ach.id)) continue;
            boolean unlocked = false;
            switch (ach.type) {
                case HARVEST:
                    unlocked = totalHarvests >= ach.requirement;
                    break;
                case EARNINGS:
                    unlocked = totalEarnings >= ach.requirement;
                    break;
                case TIER_EPIC:
                    unlocked = plugin.getStatsManager().getEpicHarvests(uuid) >= ach.requirement;
                    break;
                case TIER_LEGENDARY:
                    unlocked = plugin.getStatsManager().getLegendaryHarvests(uuid) >= ach.requirement;
                    break;
                case TIER_MYTHIC:
                    unlocked = plugin.getStatsManager().getMythicHarvests(uuid) >= ach.requirement;
                    break;
                case COLLECTION_WHEAT:
                    unlocked = plugin.getStatsManager().getCropHarvests(uuid, "WHEAT") >= ach.requirement;
                    break;
            }
            if (unlocked) {
                unlockAchievement(player, ach.id);
            }
        }
    }
    private void unlockAchievement(Player player, String id) {
        UUID uuid = player.getUniqueId();
        Set<String> achievements = playerAchievements.computeIfAbsent(uuid, k -> new HashSet<>());
        achievements.add(id);
        AchievementData ach = achievementData.get(id);
        PlayerSettings.PlayerPreferences prefs = plugin.getPlayerSettings().getPreferences(uuid);
        if (prefs.achievementNotifications) {
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage(ChatColor.GOLD + "★ " + ChatColor.YELLOW + "ACHIEVEMENT UNLOCKED!");
            player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + ach.name);
            player.sendMessage(ChatColor.GRAY + ach.description);
            player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("");
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
        if (prefs.broadcastAchievements && plugin.getConfig().getBoolean("achievements.broadcast", true)) {
            String message = ChatColor.GOLD + "★ " + ChatColor.YELLOW + player.getName() + 
                            ChatColor.GOLD + " unlocked: " + ChatColor.GREEN + ach.name;
            Bukkit.broadcastMessage(message);
        }
        saveAchievements();
    }
    public boolean hasAchievement(UUID uuid, String id) {
        return playerAchievements.containsKey(uuid) && playerAchievements.get(uuid).contains(id);
    }
    public boolean hasAchievement(Player player, String id) {
        return hasAchievement(player.getUniqueId(), id);
    }
    public Set<String> getAchievements(Player player) {
        return new HashSet<>(playerAchievements.getOrDefault(player.getUniqueId(), new HashSet<>()));
    }
    public int getAchievementCount(Player player) {
        return playerAchievements.getOrDefault(player.getUniqueId(), new HashSet<>()).size();
    }
    public int getTotalAchievements() {
        return achievementData.size();
    }
    public AchievementData getAchievementData(String id) {
        return achievementData.get(id);
    }
    public Collection<AchievementData> getAllAchievements() {
        return achievementData.values();
    }
    private void saveAchievements() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                achievementsConfig = new YamlConfiguration();
                for (Map.Entry<UUID, Set<String>> entry : playerAchievements.entrySet()) {
                    achievementsConfig.set(entry.getKey().toString(), new ArrayList<>(entry.getValue()));
                }
                achievementsConfig.save(achievementsFile);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save achievements: " + e.getMessage());
            }
        });
    }
    public static class AchievementData {
        public final String id;
        public final String name;
        public final String description;
        public final int requirement;
        public final AchievementType type;
        public AchievementData(String id, String name, String description, int requirement, AchievementType type) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.requirement = requirement;
            this.type = type;
        }
    }
    public enum AchievementType {
        HARVEST, EARNINGS, TIER_EPIC, TIER_LEGENDARY, TIER_MYTHIC,
        COLLECTION_WHEAT, COLLECTION_CARROT, COLLECTION_POTATO
    }
}
