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
        Inventory gui = Bukkit.createInventory(null, 36, ChatColor.LIGHT_PURPLE + "Farm Settings");
        PlayerSettings.PlayerPreferences prefs = plugin.getPlayerSettings().getPreferences(player.getUniqueId());
        gui.setItem(10, createToggleItem(Material.BOOK, 
            ChatColor.AQUA + "" + ChatColor.BOLD + "Harvest Messages",
            prefs.showHarvestMessages,
            "Show messages when you harvest crops"
        ));
        gui.setItem(11, createToggleItem(Material.REDSTONE,
            ChatColor.GOLD + "" + ChatColor.BOLD + "Harvest Particles",
            prefs.showParticles,
            "Show particles when harvesting"
        ));
        gui.setItem(12, createToggleItem(Material.NAME_TAG,
            ChatColor.GREEN + "" + ChatColor.BOLD + "Holograms",
            prefs.showHolograms,
            "Show holograms above crops"
        ));
        gui.setItem(13, createToggleItem(Material.NOTE_BLOCK,
            ChatColor.YELLOW + "" + ChatColor.BOLD + "Sounds",
            prefs.playSounds,
            "Play sounds on actions"
        ));
        gui.setItem(14, createToggleItem(Material.EXPERIENCE_BOTTLE,
            ChatColor.AQUA + "" + ChatColor.BOLD + "Action Bar",
            prefs.showActionBar,
            "Show harvest info in action bar"
        ));
        gui.setItem(15, createToggleItem(Material.EMERALD,
            ChatColor.GREEN + "" + ChatColor.BOLD + "Auto-Sell",
            prefs.autoSell,
            "Automatically sell harvested crops",
            "Requires permission: farmcrops.autosell.use"
        ));
        gui.setItem(31, createItem(Material.BARRIER,
            ChatColor.RED + "" + ChatColor.BOLD + "Close"
        ));
        playerGUIs.put(player, gui);
        player.openInventory(gui);
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!playerGUIs.containsKey(player)) return;
        String title = InventoryUtil.getTitle(event.getView());
        if (!title.equals(ChatColor.LIGHT_PURPLE + "Farm Settings")) return;
        event.setCancelled(true);
        Inventory gui = playerGUIs.get(player);
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(gui)) return;
        int slot = event.getSlot();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        PlayerSettings.PlayerPreferences prefs = plugin.getPlayerSettings().getPreferences(player.getUniqueId());
        switch (slot) {
            case 10:
                prefs.showHarvestMessages = !prefs.showHarvestMessages;
                plugin.getPlayerSettings().savePreferences(player.getUniqueId(), prefs);
                openGUI(player);
                break;
            case 11:
                prefs.showParticles = !prefs.showParticles;
                plugin.getPlayerSettings().savePreferences(player.getUniqueId(), prefs);
                openGUI(player);
                break;
            case 12:
                prefs.showHolograms = !prefs.showHolograms;
                plugin.getPlayerSettings().savePreferences(player.getUniqueId(), prefs);
                openGUI(player);
                break;
            case 13:
                prefs.playSounds = !prefs.playSounds;
                plugin.getPlayerSettings().savePreferences(player.getUniqueId(), prefs);
                openGUI(player);
                break;
            case 14:
                prefs.showActionBar = !prefs.showActionBar;
                plugin.getPlayerSettings().savePreferences(player.getUniqueId(), prefs);
                openGUI(player);
                break;
            case 15:
                if (player.hasPermission("farmcrops.autosell.use")) {
                    prefs.autoSell = !prefs.autoSell;
                    plugin.getPlayerSettings().savePreferences(player.getUniqueId(), prefs);
                    openGUI(player);
                } else {
                    player.sendMessage(ChatColor.RED + "You need permission: farmcrops.autosell.use");
                }
                break;
            case 31:
                player.closeInventory();
                playerGUIs.remove(player);
                break;
        }
    }
    private ItemStack createToggleItem(Material material, String name, boolean enabled, String... description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add("");
            for (String line : description) {
                lore.add(ChatColor.GRAY + line);
            }
            lore.add("");
            if (enabled) {
                lore.add(ChatColor.GREEN + "✓ ENABLED");
                lore.add(ChatColor.GRAY + "Click to disable");
            } else {
                lore.add(ChatColor.RED + "✗ DISABLED");
                lore.add(ChatColor.GRAY + "Click to enable");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }
}
