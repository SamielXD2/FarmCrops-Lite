package dev.samiel.farmcrops;
import dev.samiel.farmcrops.commands.*;
import dev.samiel.farmcrops.gui.*;
import dev.samiel.farmcrops.managers.*;
import dev.samiel.farmcrops.listeners.*;
import dev.samiel.farmcrops.models.*;
import dev.samiel.farmcrops.utils.*;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
public class FarmCrops extends JavaPlugin implements Listener {
    private static FarmCrops instance;
    private Economy economy;
    private SellGUI sellGUI;
    private HoloManager holoManager;
    private StatsManager statsManager;
    private SettingsGUI settingsGUI;
    private MainMenuGUI mainMenuGUI;
    private StatsGUI statsGUI;
    private TopGUI topGUI;
    private PlayerSettings playerSettings;
    private PlayerSettingsGUI playerSettingsGUI;
    private CropPreviewManager cropPreviewManager;
    private ActionBarManager actionBarManager;
    private MessageHandler messageHandler;
    private boolean holoEnabled = false;
    private java.io.File welcomeSeenFile;
    private Set<String> persistentSeenWelcome = new HashSet<>();
    private AdminPanelGUI adminPanelGUI;
    private AchievementManager achievementManager;
    private AchievementGUI achievementGUI;
    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("========================================");
        getLogger().info("       FARMCROPS v" + getDescription().getVersion());
        getLogger().info("  Weight-Based Crop Economy System");
        getLogger().info("========================================");
        saveDefaultConfig();
        messageHandler = new MessageHandler(this);
        if (!setupEconomy()) {
            getLogger().severe("Vault not found! Plugin disabled.");
            getLogger().severe("Download: https://www.spigotmc.org/resources/vault.34315/");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        statsManager = new StatsManager(this);
        playerSettings = new PlayerSettings(this);
        actionBarManager = new ActionBarManager(this);
        achievementManager = new AchievementManager(this);
        adminPanelGUI = new AdminPanelGUI(this);
        getServer().getPluginManager().registerEvents(adminPanelGUI, this);
        achievementGUI = new AchievementGUI(this);
        getServer().getPluginManager().registerEvents(achievementGUI, this);
        getServer().getPluginManager().registerEvents(new CropListener(this), this);
        getServer().getPluginManager().registerEvents(this, this);
        sellGUI = new SellGUI(this);
        getServer().getPluginManager().registerEvents(sellGUI, this);
        settingsGUI = new SettingsGUI(this);
        getServer().getPluginManager().registerEvents(settingsGUI, this);
        mainMenuGUI = new MainMenuGUI(this);
        getServer().getPluginManager().registerEvents(mainMenuGUI, this);
        statsGUI = new StatsGUI(this);
        getServer().getPluginManager().registerEvents(statsGUI, this);
        topGUI = new TopGUI(this);
        getServer().getPluginManager().registerEvents(topGUI, this);
        playerSettingsGUI = new PlayerSettingsGUI(this);
        getServer().getPluginManager().registerEvents(playerSettingsGUI, this);
        getCommand("sellcrops").setExecutor(new SellCommand(this));
        getCommand("farmstats").setExecutor(new StatsCommand(this));
        getCommand("farmtop").setExecutor(new TopCommand(this));
        getCommand("farmsettings").setExecutor(new SettingsCommand(this));
        getCommand("farmreload").setExecutor(new ReloadCommand(this));
        getCommand("farm").setExecutor(new FarmCommand(this));
        getCommand("farmbackup").setExecutor(new BackupCommand(this));
        getCommand("farmachievements").setExecutor(new AchievementCommand(this));
        getCommand("farmadmin").setExecutor(new FarmAdminCommand(this));
        getCommand("farmgive").setExecutor(new FarmGiveCommand(this));
        int autoSaveInterval = getConfig().getInt("auto-save-interval", 6000);
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (statsManager != null) {
                statsManager.saveAll();
            }
            if (playerSettings != null) {
                playerSettings.saveSettings();
            }
        }, autoSaveInterval, autoSaveInterval);
        if (Bukkit.getPluginManager().getPlugin("DecentHolograms") != null) {
            cropPreviewManager = new CropPreviewManager(this);
            holoManager = new HoloManager(this);
            Bukkit.getPluginManager().registerEvents(cropPreviewManager, this);
            holoEnabled = true;
        }
        getLogger().info("FarmCrops v" + getDescription().getVersion() + " enabled!");
        loadWelcomeSeen();
    }
    private void loadWelcomeSeen() {
        welcomeSeenFile = new java.io.File(getDataFolder(), "welcome-seen.yml");
        if (!welcomeSeenFile.exists()) return;
        org.bukkit.configuration.file.FileConfiguration cfg =
            org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(welcomeSeenFile);
        java.util.List<String> list = cfg.getStringList("seen");
        persistentSeenWelcome = new HashSet<>(list);
    }
    private void saveWelcomeSeen() {
        try {
            org.bukkit.configuration.file.YamlConfiguration cfg = new org.bukkit.configuration.file.YamlConfiguration();
            cfg.set("seen", new java.util.ArrayList<>(persistentSeenWelcome));
            cfg.save(welcomeSeenFile);
        } catch (Exception e) {
            getLogger().warning("Could not save welcome-seen.yml: " + e.getMessage());
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // ONLY show to OPs/admins - NEVER to regular members
        if (!player.isOp() && !player.hasPermission("farmcrops.admin")) return;
        // Check if disabled in config
        if (!getConfig().getBoolean("show-welcome-message", true)) return;
        // Truly once - persists across server restarts
        String uuidStr = player.getUniqueId().toString();
        if (persistentSeenWelcome.contains(uuidStr)) return;
        persistentSeenWelcome.add(uuidStr);
        saveWelcomeSeen();
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!player.isOnline()) return;
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "Thanks for using FarmCrops!");
            player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.WHITE + "/farm" + ChatColor.GRAY + " to get started.");
            player.sendMessage("");
        }, 40L);
    }
    @Override
    public void onDisable() {

        if (statsManager != null) {
            statsManager.saveAll();
        }
        if (playerSettings != null) {
            playerSettings.saveSettings();
        }
        getLogger().info("FarmCrops disabled. All data saved.");
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        if (statsManager != null) {
            statsManager.clearCache(uuid);
        }
        if (playerSettings != null) {
            playerSettings.saveSettings();
            playerSettings.clearCache(uuid);
        }
        if (holoEnabled && cropPreviewManager != null) {
            cropPreviewManager.cleanup();
        }
    }
    public double getCropPrice(Material cropType) {
        String cropKey = null;
        switch (cropType) {
            case WHEAT:      cropKey = "wheat";    break;
            case CARROTS:    cropKey = "carrot";   break;
            case POTATOES:   cropKey = "potato";   break;
            case BEETROOTS:  cropKey = "beetroot"; break;
            case MELON:      cropKey = "melon";    break;
            default: break;
        }
        if (cropKey != null && getConfig().contains("prices." + cropKey)) {
            return getConfig().getDouble("prices." + cropKey);
        }
        return getConfig().getDouble("prices.default", 10.0);
    }
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp =
            getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }
    public static FarmCrops getInstance()      { return instance; }
    public Economy getEconomy()                { return economy; }
    public SellGUI getSellGUI()                { return sellGUI; }
    public boolean isHoloEnabled()             { return holoEnabled; }
    public StatsManager getStatsManager()      { return statsManager; }
    public SettingsGUI getSettingsGUI()        { return settingsGUI; }
    public MainMenuGUI getMainMenuGUI()        { return mainMenuGUI; }
    public StatsGUI getStatsGUI()              { return statsGUI; }
    public TopGUI getTopGUI()                  { return topGUI; }
    public PlayerSettings getPlayerSettings()  { return playerSettings; }
    public PlayerSettingsGUI getPlayerSettingsGUI() { return playerSettingsGUI; }
    public CropPreviewManager getCropPreviewManager() { return cropPreviewManager; }
    public HoloManager getHoloManager()        { return holoManager; }
    public ActionBarManager getActionBarManager() { return actionBarManager; }
    public MessageHandler getMessageHandler()  { return messageHandler; }
    public AdminPanelGUI getAdminPanelGUI()    { return adminPanelGUI; }
    public AchievementManager getAchievementManager() { return achievementManager; }
    public AchievementGUI getAchievementGUI()  { return achievementGUI; }
}
