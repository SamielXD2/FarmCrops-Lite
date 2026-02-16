package dev.samiel.farmcrops.gui;
import dev.samiel.farmcrops.FarmCrops;
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
    public SettingsGUI(FarmCrops plugin) {
        this.plugin = plugin;
    }
    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.RED + "Server Settings");
        gui.setItem(11, createToggleItem(Material.WHEAT,
            ChatColor.GREEN + "" + ChatColor.BOLD + "Custom Crops",
            plugin.getConfig().getBoolean("custom-crops.enabled", true),
            "Enable custom crop system"
        ));
        gui.setItem(13, createToggleItem(Material.ENDER_PEARL,
            ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Holograms",
            plugin.getConfig().getBoolean("holograms.enabled", true),
            "Enable hologram displays",
            "Requires DecentHolograms"
        ));
        gui.setItem(15, createToggleItem(Material.PAPER,
            ChatColor.AQUA + "" + ChatColor.BOLD + "Action Bar",
            plugin.getConfig().getBoolean("action-bar.enabled", true),
            "Show harvest info in action bar"
        ));
        if (player.hasPermission("farmcrops.admin")) {
            gui.setItem(22, createItem(Material.BOOK,
                ChatColor.YELLOW + "" + ChatColor.BOLD + "Plugin Info",
                "",
                ChatColor.GRAY + "Plugin: " + ChatColor.WHITE + "FarmCrops",
                ChatColor.GRAY + "Version: " + ChatColor.WHITE + "1.1.0",
                ChatColor.GRAY + "Author: " + ChatColor.WHITE + "SamielXD"
            ));
        }
        gui.setItem(18, createItem(Material.BARRIER,
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
        if (!title.equals(ChatColor.RED + "Server Settings")) return;
        event.setCancelled(true);
        Inventory gui = playerGUIs.get(player);
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(gui)) return;
        int slot = event.getSlot();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        switch (slot) {
            case 11:
                toggleConfig(player, "custom-crops.enabled");
                break;
            case 13:
                toggleConfig(player, "holograms.enabled");
                break;
            case 15:
                toggleConfig(player, "action-bar.enabled");
                break;
            case 18:
                player.closeInventory();
                playerGUIs.remove(player);
                break;
        }
    }
    private void toggleConfig(Player player, String path) {
        boolean current = plugin.getConfig().getBoolean(path, true);
        plugin.getConfig().set(path, !current);
        plugin.saveConfig();
        openGUI(player);
        player.sendMessage(ChatColor.GREEN + "✓ Setting updated!");
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
    private ItemStack createItem(Material material, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null && lore.length > 0) {
            meta.setDisplayName(lore[0]);
            if (lore.length > 1) {
                List<String> loreList = new ArrayList<>();
                for (int i = 1; i < lore.length; i++) {
                    loreList.add(lore[i]);
                }
                meta.setLore(loreList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
