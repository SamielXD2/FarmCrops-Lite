package dev.samiel.farmcrops.models;
import dev.samiel.farmcrops.FarmCrops;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class PlayerSettings {
    private final FarmCrops plugin;
    private final File settingsFile;
    private FileConfiguration settingsConfig;
    private final Map<UUID, PlayerPreferences> cache = new HashMap<>();
    public PlayerSettings(FarmCrops plugin) {
        this.plugin = plugin;
        this.settingsFile = new File(plugin.getDataFolder(), "player-settings.yml");
        loadSettings();
    }
    private void loadSettings() {
        if (!settingsFile.exists()) {
            try { settingsFile.createNewFile(); } catch (IOException e) {
                plugin.getLogger().warning("Failed to create player-settings.yml: " + e.getMessage());
            }
        }
        settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);
    }
    public void saveSettings() {
        try {
            settingsConfig.save(settingsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save player-settings.yml: " + e.getMessage());
        }
    }
    public PlayerPreferences getPreferences(UUID uuid) {
        if (cache.containsKey(uuid)) return cache.get(uuid);
        String path = uuid.toString() + ".";
        PlayerPreferences prefs = new PlayerPreferences();
        prefs.autoSell                 = settingsConfig.getBoolean(path + "auto-sell", false);
        prefs.showHolograms            = settingsConfig.getBoolean(path + "holograms", true);
        prefs.showParticles            = settingsConfig.getBoolean(path + "particles", true);
        prefs.playSounds               = settingsConfig.getBoolean(path + "sounds", true);
        prefs.showHarvestMessages      = settingsConfig.getBoolean(path + "harvest-messages", true);
        prefs.achievementNotifications = settingsConfig.getBoolean(path + "achievement-notifications", true);
        prefs.broadcastAchievements    = settingsConfig.getBoolean(path + "broadcast-achievements", true);
        prefs.showActionBar            = settingsConfig.getBoolean(path + "action-bar", false);
        prefs.showScoreboard           = settingsConfig.getBoolean(path + "scoreboard", false);
        prefs.showTitle                = settingsConfig.getBoolean(path + "title", true);
        // NEW - hologram detail level: SMALL, MEDIUM, LARGE (default MEDIUM)
        prefs.hologramDetail = settingsConfig.getString(path + "hologram-detail", "MEDIUM");
        cache.put(uuid, prefs);
        return prefs;
    }
    public void savePreferences(UUID uuid, PlayerPreferences prefs) {
        String path = uuid.toString() + ".";
        settingsConfig.set(path + "auto-sell",                  prefs.autoSell);
        settingsConfig.set(path + "holograms",                  prefs.showHolograms);
        settingsConfig.set(path + "particles",                  prefs.showParticles);
        settingsConfig.set(path + "sounds",                     prefs.playSounds);
        settingsConfig.set(path + "harvest-messages",           prefs.showHarvestMessages);
        settingsConfig.set(path + "achievement-notifications",  prefs.achievementNotifications);
        settingsConfig.set(path + "broadcast-achievements",     prefs.broadcastAchievements);
        settingsConfig.set(path + "action-bar",                 prefs.showActionBar);
        settingsConfig.set(path + "scoreboard",                 prefs.showScoreboard);
        settingsConfig.set(path + "title",                      prefs.showTitle);
        settingsConfig.set(path + "hologram-detail",            prefs.hologramDetail);
        cache.put(uuid, prefs);
        saveSettings();
    }
    public void clearCache(UUID uuid) { cache.remove(uuid); }
    public static class PlayerPreferences {
        public boolean autoSell                 = false;
        public boolean showHolograms            = true;
        public boolean showParticles            = true;
        public boolean playSounds               = true;
        public boolean showHarvestMessages      = true;
        public boolean achievementNotifications = true;
        public boolean broadcastAchievements    = true;
        public boolean showActionBar            = false;
        public boolean showScoreboard           = false;
        public boolean showTitle                = true;
        // SMALL = crop name + status only
        // MEDIUM = + progress bar + stage
        // LARGE = + price estimate + tiers + harvest tip
        public String hologramDetail            = "MEDIUM";
    }
}
