package dev.samiel.farmcrops.gui;
import dev.samiel.farmcrops.FarmCrops;
import dev.samiel.farmcrops.models.PlayerSettings;
import dev.samiel.farmcrops.utils.InventoryUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;
public class PlayerSettingsGUI implements Listener {
    private final FarmCrops plugin;
    private final Map<Player, Inventory> playerGUIs = new HashMap<>();
    public PlayerSettingsGUI(FarmCrops plugin) {
        this.plugin = plugin;
    }
    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.LIGHT_PURPLE + "Farm Settings");
        PlayerSettings.PlayerPreferences prefs = plugin.getPlayerSettings().getPreferences(player.getUniqueId());
        // Black glass border
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta(); if (gm != null) { gm.setDisplayName(" "); glass.setItemMeta(gm); }
        for (int i = 0; i < 54; i++) gui.setItem(i, glass);
        // ── Row 1: Core settings (slots 10-16) ──────────────────────────
        gui.setItem(10, toggle(Material.BOOK,
            ChatColor.AQUA + "" + ChatColor.BOLD + "Harvest Messages",
            prefs.showHarvestMessages,
            "Show chat messages when you harvest crops"));
        gui.setItem(11, toggle(Material.REDSTONE,
            ChatColor.RED + "" + ChatColor.BOLD + "Harvest Particles",
            prefs.showParticles,
            "Show particles when harvesting"));
        gui.setItem(12, toggle(Material.NAME_TAG,
            ChatColor.GREEN + "" + ChatColor.BOLD + "Holograms",
            prefs.showHolograms,
            "Show floating holograms above crops"));
        gui.setItem(13, toggle(Material.NOTE_BLOCK,
            ChatColor.YELLOW + "" + ChatColor.BOLD + "Sounds",
            prefs.playSounds,
            "Play sounds during actions"));
        gui.setItem(14, toggle(Material.EXPERIENCE_BOTTLE,
            ChatColor.AQUA + "" + ChatColor.BOLD + "Action Bar",
            prefs.showActionBar,
            "Show harvest info in the action bar"));
        gui.setItem(15, toggle(Material.EMERALD,
            ChatColor.GREEN + "" + ChatColor.BOLD + "Auto-Sell",
            prefs.autoSell,
            "Automatically sell harvested crops",
            ChatColor.GRAY + "Requires: farmcrops.autosell.use"));
        // ── Row 2: Achievement settings (slots 19-21) ───────────────────
        gui.setItem(19, toggle(Material.WRITABLE_BOOK,
            ChatColor.GOLD + "" + ChatColor.BOLD + "Achievement Notifications",
            prefs.achievementNotifications,
            "Show a message when you unlock achievements"));
        gui.setItem(20, toggle(Material.BELL,
            ChatColor.YELLOW + "" + ChatColor.BOLD + "Broadcast Achievements",
            prefs.broadcastAchievements,
            "Announce your achievements to the whole server",
            ChatColor.GRAY + "Other players will see your unlocks"));
        // ── Row 2: Hologram Detail (slots 23-25) ────────────────────────
        gui.setItem(22, createHoloDetailItem(prefs.hologramDetail, "SMALL",
            Material.PAPER,
            ChatColor.GRAY + "" + ChatColor.BOLD + "Hologram: SMALL",
            "Crop name + ready/growing status only",
            ChatColor.GRAY + "• Crop name",
            ChatColor.GRAY + "• Growing / Ready status"));
        gui.setItem(23, createHoloDetailItem(prefs.hologramDetail, "MEDIUM",
            Material.MAP,
            ChatColor.YELLOW + "" + ChatColor.BOLD + "Hologram: MEDIUM",
            "Small + progress bar & stage",
            ChatColor.GRAY + "• Crop name",
            ChatColor.GRAY + "• Status + progress bar",
            ChatColor.GRAY + "• Stage X/7"));
        gui.setItem(24, createHoloDetailItem(prefs.hologramDetail, "LARGE",
            Material.FILLED_MAP,
            ChatColor.GREEN + "" + ChatColor.BOLD + "Hologram: LARGE",
            "Medium + price estimate & possible tiers",
            ChatColor.GRAY + "• Crop name",
            ChatColor.GRAY + "• Status + progress bar",
            ChatColor.GRAY + "• Stage X/7",
            ChatColor.GRAY + "• Estimated value",
            ChatColor.GRAY + "• Possible tiers",
            ChatColor.GRAY + "• Harvest tip (when ready)"));
        // ── Close ────────────────────────────────────────────────────────
        gui.setItem(49, createItem(Material.BARRIER, ChatColor.RED + "" + ChatColor.BOLD + "Close"));
        playerGUIs.put(player, gui);
        player.openInventory(gui);
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory gui = playerGUIs.get(player);
        if (gui == null) return;
        if (!event.getView().getTitle().equals(ChatColor.LIGHT_PURPLE + "Farm Settings")) return;
        event.setCancelled(true);
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(gui)) return;
        PlayerSettings.PlayerPreferences prefs = plugin.getPlayerSettings().getPreferences(player.getUniqueId());
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        switch (event.getSlot()) {
            case 10: prefs.showHarvestMessages = !prefs.showHarvestMessages; break;
            case 11: prefs.showParticles       = !prefs.showParticles;       break;
            case 12: prefs.showHolograms       = !prefs.showHolograms;       break;
            case 13: prefs.playSounds          = !prefs.playSounds;          break;
            case 14: prefs.showActionBar       = !prefs.showActionBar;       break;
            case 15:
                if (player.hasPermission("farmcrops.autosell.use") || player.hasPermission("farmcrops.admin")) {
                    prefs.autoSell = !prefs.autoSell;
                } else {
                    player.sendMessage(ChatColor.RED + "You need farmcrops.autosell.use to use Auto-Sell!");
                    return;
                }
                break;
            case 19: prefs.achievementNotifications = !prefs.achievementNotifications; break;
            case 20: prefs.broadcastAchievements    = !prefs.broadcastAchievements;    break;
            // Hologram detail buttons - cycle through SMALL → MEDIUM → LARGE
            case 22: prefs.hologramDetail = "SMALL";  break;
            case 23: prefs.hologramDetail = "MEDIUM"; break;
            case 24: prefs.hologramDetail = "LARGE";  break;
            case 49:
                player.closeInventory();
                playerGUIs.remove(player);
                return;
            default: return;
        }
        plugin.getPlayerSettings().savePreferences(player.getUniqueId(), prefs);
        openGUI(player); // refresh GUI
    }
    // Creates a hologram detail selector item - highlighted if selected
    private ItemStack createHoloDetailItem(String current, String value, Material mat, String name, String desc, String... lines) {
        boolean selected = current.equalsIgnoreCase(value);
        ItemStack item = new ItemStack(selected ? mat : Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(selected ? name + ChatColor.GREEN + " ◀ SELECTED" : ChatColor.DARK_GRAY + name.replaceAll("§.", ""));
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + desc);
            lore.add("");
            for (String line : lines) lore.add(line);
            lore.add("");
            lore.add(selected ? ChatColor.GREEN + "✔ SELECTED" : ChatColor.YELLOW + "Click to select");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    private ItemStack toggle(Material material, String name, boolean enabled, String... description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add("");
            for (String line : description) {
                if (line.contains("§")) lore.add(line);
                else lore.add(ChatColor.GRAY + line);
            }
            lore.add("");
            lore.add(enabled ? ChatColor.GREEN + "✔ ENABLED  " + ChatColor.GRAY + "| Click to disable"
                             : ChatColor.RED   + "✘ DISABLED " + ChatColor.GRAY + "| Click to enable");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(name); item.setItemMeta(meta); }
        return item;
    }
}
