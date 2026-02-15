package dev.samiel.farmcrops.gui;
import dev.samiel.farmcrops.FarmCrops;
import dev.samiel.farmcrops.listeners.CropListener;
import dev.samiel.farmcrops.models.PlayerSettings;
import dev.samiel.farmcrops.managers.StatsManager;
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
public class SettingsGUI implements Listener {
    private final FarmCrops plugin;
    private final Map<Player, Inventory> playerGUIs = new HashMap<>();
    private static final String[][] TOGGLES = {
        { "holograms.harvest-flash",  "Harvest Hologram",  "Show hologram on harvest" },
        { "holograms.growing-cursor", "Growing Cursor",    "Hologram when looking at crops" },
        { "holograms.particles",      "Particles",         "Particle effects on harvest" },
        { "seeds.enabled",            "Seed Drops",        "Drop seeds when harvesting" },
        { "auto-save.enabled",        "Auto-Save",         "Automatic data saving" },
        { "action-bar.enabled",       "Action Bar",        "Show action bar messages" },
        { "chat-messages.enabled",    "Chat Messages",     "Harvest messages in chat" },
    };
    public SettingsGUI(FarmCrops plugin) {
        this.plugin = plugin;
    }
    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, ChatColor.DARK_GREEN + "FarmCrops Settings");
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta();
        if (gm != null) { gm.setDisplayName(" "); glass.setItemMeta(gm); }
        for (int i = 0; i < 45; i++) gui.setItem(i, glass);
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        for (int i = 0; i < TOGGLES.length && i < slots.length; i++) {
            gui.setItem(slots[i], buildToggleItem(TOGGLES[i][0], TOGGLES[i][1], TOGGLES[i][2]));
        }
        ItemStack infoBtn = new ItemStack(Material.BOOK);
        ItemMeta im = infoBtn.getItemMeta();
        if (im != null) {
            im.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "INFO");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "These settings are GLOBAL");
            lore.add(ChatColor.GRAY + "Changes affect ALL players");
            lore.add("");
            lore.add(ChatColor.GREEN + "✓ Toggle features on/off");
            lore.add(ChatColor.GRAY + "without editing config.yml");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Use /farmreload to");
            lore.add(ChatColor.YELLOW + "apply other config changes");
            im.setLore(lore);
            infoBtn.setItemMeta(im);
        }
        gui.setItem(4, infoBtn);
        ItemStack reloadBtn = new ItemStack(Material.COMPASS);
        ItemMeta rm = reloadBtn.getItemMeta();
        if (rm != null) {
            rm.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "RELOAD");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Reload plugin configuration");
            lore.add(ChatColor.GRAY + "and refresh all systems");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to reload");
            rm.setLore(lore);
            reloadBtn.setItemMeta(rm);
        }
        gui.setItem(40, reloadBtn);
        ItemStack closeBtn = new ItemStack(Material.BARRIER);
        ItemMeta cm = closeBtn.getItemMeta();
        if (cm != null) {
            cm.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "CLOSE");
            closeBtn.setItemMeta(cm);
        }
        gui.setItem(44, closeBtn);
        playerGUIs.put(player, gui);
        player.openInventory(gui);
    }
    private ItemStack buildToggleItem(String configPath, String label, String description) {
        boolean enabled = plugin.getConfig().getBoolean(configPath, true);
        Material mat = enabled ? Material.LIME_DYE : Material.RED_DYE;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName((enabled ? ChatColor.GREEN : ChatColor.RED) + label
                + " — " + (enabled ? "ON" : "OFF"));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + description);
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to toggle");
            lore.add(ChatColor.DARK_GRAY + "Affects all players globally");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!playerGUIs.containsKey(player)) return;
        String title = InventoryUtil.getTitle(event.getView());
        if (!title.equals(ChatColor.DARK_GREEN + "FarmCrops Settings")) return;
        event.setCancelled(true);
        Inventory gui = playerGUIs.get(player);
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(gui)) return;
        int slot = event.getSlot();
        if (slot == 44) {
            player.closeInventory();
            playerGUIs.remove(player);
            return;
        }
        if (slot == 4) {
            return;
        }
        if (slot == 40) {
            player.closeInventory();
            playerGUIs.remove(player);
            player.performCommand("farmreload");
            return;
        }
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        for (int i = 0; i < slots.length && i < TOGGLES.length; i++) {
            if (slot == slots[i]) {
                String configPath = TOGGLES[i][0];
                boolean current = plugin.getConfig().getBoolean(configPath, true);
                boolean newValue = !current;
                plugin.getConfig().set(configPath, newValue);
                plugin.saveConfig();
                gui.setItem(slot, buildToggleItem(TOGGLES[i][0], TOGGLES[i][1], TOGGLES[i][2]));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                player.sendMessage(ChatColor.GREEN + TOGGLES[i][1] + " → " + (newValue ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
                player.sendMessage(ChatColor.GRAY + "(Changed globally for all players)");
                break;
            }
        }
    }
}
