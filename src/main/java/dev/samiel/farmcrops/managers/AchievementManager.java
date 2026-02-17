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
    private final Map<String, AchievementData> achievementData = new LinkedHashMap<>();
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
            try { achievementsFile.createNewFile(); } catch (Exception e) {
                plugin.getLogger().warning("Could not create achievements.yml: " + e.getMessage());
            }
        }
        achievementsConfig = YamlConfiguration.loadConfiguration(achievementsFile);
        for (String key : achievementsConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                List<String> list = achievementsConfig.getStringList(key);
                playerAchievements.put(uuid, new HashSet<>(list));
            } catch (Exception ignored) {}
        }
    }
    private void initializeAchievements() {
        // ── Harvest Milestones ──────────────────────────────────────────
        registerAchievement("first_harvest",            "First Harvest",        "Harvest your very first crop!",         1,       AchievementType.HARVEST);
        registerAchievement("fifty_harvest",            "Getting Started",      "Harvest 50 crops!",                     50,      AchievementType.HARVEST);
        registerAchievement("hundred_harvest",          "Century Farmer",       "Harvest 100 crops!",                    100,     AchievementType.HARVEST);
        registerAchievement("five_hundred_harvest",     "Dedicated Farmer",     "Harvest 500 crops!",                    500,     AchievementType.HARVEST);
        registerAchievement("thousand_harvest",         "Expert Farmer",        "Harvest 1,000 crops!",                  1000,    AchievementType.HARVEST);
        registerAchievement("ten_thousand_harvest",     "Elite Farmer",         "Harvest 10,000 crops!",                 10000,   AchievementType.HARVEST);
        registerAchievement("fifty_thousand_harvest",   "Farming Champion",     "Harvest 50,000 crops!",                 50000,   AchievementType.HARVEST);
        // ── Earnings Milestones ─────────────────────────────────────────
        registerAchievement("first_thousand",           "Money Maker",          "Earn $1,000 from farming!",             1000,    AchievementType.EARNINGS);
        registerAchievement("ten_thousand",             "Rich Farmer",          "Earn $10,000 from farming!",            10000,   AchievementType.EARNINGS);
        registerAchievement("fifty_thousand_earn",      "On The Rise",          "Earn $50,000 from farming!",            50000,   AchievementType.EARNINGS);
        registerAchievement("hundred_thousand",         "Crop Tycoon",          "Earn $100,000 from farming!",           100000,  AchievementType.EARNINGS);
        registerAchievement("million_earned",           "Millionaire",          "Earn $1,000,000 from farming!",         1000000, AchievementType.EARNINGS);
        // ── Rarity Milestones ───────────────────────────────────────────
        registerAchievement("first_rare",               "Rare Discovery",       "Harvest your first Rare crop!",         1,       AchievementType.TIER_RARE);
        registerAchievement("ten_rare",                 "Rare Hunter",       "Harvest 10 Rare crops!",                10,      AchievementType.TIER_RARE);
        registerAchievement("first_epic",               "Epic Harvester",       "Harvest your first Epic crop!",         1,       AchievementType.TIER_EPIC);
        registerAchievement("first_legendary",          "Legendary Find",       "Harvest your first Legendary crop!",    1,       AchievementType.TIER_LEGENDARY);
        registerAchievement("five_legendary",           "Legendary Collector",  "Harvest 5 Legendary crops!",            5,       AchievementType.TIER_LEGENDARY);
        registerAchievement("first_mythic",             "Mythic Legend",        "Harvest your first Mythic crop!",       1,       AchievementType.TIER_MYTHIC);
        registerAchievement("ten_mythic",               "Mythic Collector",        "Harvest 10 Mythic crops!",              10,      AchievementType.TIER_MYTHIC);
        // ── Crop Collections ────────────────────────────────────────────
        registerAchievement("wheat_master",             "Wheat Expert",         "Harvest 1,000 wheat!",                  1000,    AchievementType.COLLECTION_WHEAT);
        registerAchievement("carrot_king",              "Carrot Champion",          "Harvest 1,000 carrots!",                1000,    AchievementType.COLLECTION_CARROT);
        registerAchievement("potato_expert",            "Potato Pro",        "Harvest 1,000 potatoes!",               1000,    AchievementType.COLLECTION_POTATO);
        registerAchievement("beetroot_master",          "Beetroot Expert",      "Harvest 1,000 beetroots!",              1000,    AchievementType.COLLECTION_BEETROOT);
        registerAchievement("melon_master",             "Melon Expert",         "Harvest 1,000 melons!",                 1000,    AchievementType.COLLECTION_MELON);
        registerAchievement("crop_collector",           "Crop Collector",       "Harvest 500 of every crop type!",       500,     AchievementType.COLLECTION_ALL);
    }
    private void registerAchievement(String id, String name, String description, int requirement, AchievementType type) {
        achievementData.put(id, new AchievementData(id, name, description, requirement, type));
    }
    public void checkAchievements(Player player) {
        UUID uuid = player.getUniqueId();
        int totalHarvests    = plugin.getStatsManager().getTotalHarvests(uuid);
        double totalEarnings = plugin.getStatsManager().getTotalEarnings(uuid);
        int rareHarvests     = plugin.getStatsManager().getStats(uuid).rareHarvests;
        int epicHarvests     = plugin.getStatsManager().getEpicHarvests(uuid);
        int legendaryHarvests = plugin.getStatsManager().getLegendaryHarvests(uuid);
        int mythicHarvests   = plugin.getStatsManager().getMythicHarvests(uuid);
        int wheatHarvests    = plugin.getStatsManager().getCropHarvests(uuid, "WHEAT");
        int carrotHarvests   = plugin.getStatsManager().getCropHarvests(uuid, "CARROTS");
        int potatoHarvests   = plugin.getStatsManager().getCropHarvests(uuid, "POTATOES");
        int beetrootHarvests = plugin.getStatsManager().getCropHarvests(uuid, "BEETROOTS");
        int melonHarvests    = plugin.getStatsManager().getCropHarvests(uuid, "MELON");
        for (AchievementData ach : achievementData.values()) {
            if (hasAchievement(uuid, ach.id)) continue;
            boolean unlocked = false;
            switch (ach.type) {
                case HARVEST:            unlocked = totalHarvests >= ach.requirement; break;
                case EARNINGS:           unlocked = totalEarnings >= ach.requirement; break;
                case TIER_RARE:          unlocked = rareHarvests >= ach.requirement; break;
                case TIER_EPIC:          unlocked = epicHarvests >= ach.requirement; break;
                case TIER_LEGENDARY:     unlocked = legendaryHarvests >= ach.requirement; break;
                case TIER_MYTHIC:        unlocked = mythicHarvests >= ach.requirement; break;
                case COLLECTION_WHEAT:   unlocked = wheatHarvests >= ach.requirement; break;
                case COLLECTION_CARROT:  unlocked = carrotHarvests >= ach.requirement; break;
                case COLLECTION_POTATO:  unlocked = potatoHarvests >= ach.requirement; break;
                case COLLECTION_BEETROOT:unlocked = beetrootHarvests >= ach.requirement; break;
                case COLLECTION_MELON:   unlocked = melonHarvests >= ach.requirement; break;
                case COLLECTION_ALL:
                    unlocked = wheatHarvests   >= ach.requirement &&
                               carrotHarvests  >= ach.requirement &&
                               potatoHarvests  >= ach.requirement &&
                               beetrootHarvests >= ach.requirement &&
                               melonHarvests   >= ach.requirement;
                    break;
            }
            if (unlocked) unlockAchievement(player, ach.id);
        }
    }
    private void unlockAchievement(Player player, String id) {
        UUID uuid = player.getUniqueId();
        playerAchievements.computeIfAbsent(uuid, k -> new HashSet<>()).add(id);
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
            // Single clean note - not Haram!
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
        }
        if (prefs.broadcastAchievements && plugin.getConfig().getBoolean("achievements.broadcast", true)) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "★ " + ChatColor.YELLOW + player.getName() +
                ChatColor.GOLD + " unlocked: " + ChatColor.GREEN + ach.name);
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
        public final String id, name, description;
        public final int requirement;
        public final AchievementType type;
        public AchievementData(String id, String name, String description, int requirement, AchievementType type) {
            this.id = id; this.name = name; this.description = description;
            this.requirement = requirement; this.type = type;
        }
    }
    public enum AchievementType {
        HARVEST, EARNINGS,
        TIER_RARE, TIER_EPIC, TIER_LEGENDARY, TIER_MYTHIC,
        COLLECTION_WHEAT, COLLECTION_CARROT, COLLECTION_POTATO,
        COLLECTION_BEETROOT, COLLECTION_MELON, COLLECTION_ALL
    }
}
