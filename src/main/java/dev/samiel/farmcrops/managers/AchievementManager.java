package dev.samiel.farmcrops.managers;
import dev.samiel.farmcrops.FarmCrops;
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
    private File achievementsFile;
    private FileConfiguration achievementsConfig;
    public AchievementManager(FarmCrops plugin) {
        this.plugin = plugin;
        loadAchievements();
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
    public void checkAndGrant(Player player, String achievementId) {
        if (hasAchievement(player, achievementId)) return;
        grantAchievement(player, achievementId);
    }
    public void grantAchievement(Player player, String achievementId) {
        UUID uuid = player.getUniqueId();
        playerAchievements.computeIfAbsent(uuid, k -> new HashSet<>()).add(achievementId);
        String name = getAchievementName(achievementId);
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "★ ACHIEVEMENT UNLOCKED ★");
        player.sendMessage(ChatColor.YELLOW + name);
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        saveAchievements();
    }
    public boolean hasAchievement(Player player, String achievementId) {
        return playerAchievements.getOrDefault(player.getUniqueId(), new HashSet<>()).contains(achievementId);
    }
    public Set<String> getAchievements(Player player) {
        return new HashSet<>(playerAchievements.getOrDefault(player.getUniqueId(), new HashSet<>()));
    }
    public int getAchievementCount(Player player) {
        return playerAchievements.getOrDefault(player.getUniqueId(), new HashSet<>()).size();
    }
    private String getAchievementName(String id) {
        switch (id) {
            case "first_harvest": return "First Harvest";
            case "100_harvests": return "Farming Novice";
            case "1000_harvests": return "Farming Expert";
            case "first_rare": return "Rare Discovery";
            case "first_epic": return "Epic Find";
            case "first_legendary": return "Legendary Farmer";
            case "first_mythic": return "Mythic Legend";
            case "earn_1000": return "First Thousand";
            case "earn_10000": return "Money Maker";
            case "earn_100000": return "Wealthy Farmer";
            default: return id;
        }
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
}
