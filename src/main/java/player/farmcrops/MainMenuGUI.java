package player.farmcrops;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MainMenuGUI implements Listener {

    private final FarmCrops plugin;
    private final Map<Player, Inventory> playerGUIs = new HashMap<>();

    public MainMenuGUI(FarmCrops plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "FarmCrops Menu");

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta();
        if (gm != null) { gm.setDisplayName(" "); glass.setItemMeta(gm); }
        for (int i = 0; i < 27; i++) gui.setItem(i, glass);

        gui.setItem(10, createItem(Material.EMERALD,
            ChatColor.GREEN + "" + ChatColor.BOLD + "Sell Crops",
            "",
            ChatColor.GRAY + "Open the sell GUI to",
            ChatColor.GRAY + "sell your harvested crops",
            "",
            ChatColor.YELLOW + "Click to open"
        ));

        gui.setItem(11, createItem(Material.BOOK,
            ChatColor.AQUA + "" + ChatColor.BOLD + "Your Stats",
            "",
            ChatColor.GRAY + "View your farming",
            ChatColor.GRAY + "statistics and progress",
            "",
            ChatColor.YELLOW + "Click to view"
        ));

        gui.setItem(12, createItem(Material.GOLD_BLOCK,
            ChatColor.GOLD + "" + ChatColor.BOLD + "Leaderboard",
            "",
            ChatColor.GRAY + "View top farmers on",
            ChatColor.GRAY + "the server",
            "",
            ChatColor.YELLOW + "Click to view"
        ));

        gui.setItem(13, createItem(Material.COMPARATOR,
            ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Settings",
            "",
            ChatColor.GRAY + "Configure your personal",
            ChatColor.GRAY + "farming preferences",
            "",
            ChatColor.YELLOW + "Click to open"
        ));

        // Premium features - shown but disabled
        gui.setItem(15, createItem(Material.GRAY_DYE,
            ChatColor.GRAY + "" + ChatColor.BOLD + "Achievements",
            "",
            ChatColor.DARK_GRAY + "Premium Feature",
            "",
            ChatColor.GRAY + "Available in Premium version"
        ));

        gui.setItem(16, createItem(Material.GRAY_DYE,
            ChatColor.GRAY + "" + ChatColor.BOLD + "Daily Tasks",
            "",
            ChatColor.DARK_GRAY + "Premium Feature",
            "",
            ChatColor.GRAY + "Available in Premium version"
        ));

        gui.setItem(4, createItem(Material.SUNFLOWER,
            ChatColor.YELLOW + "" + ChatColor.BOLD + "FarmCrops",
            "",
            ChatColor.GRAY + "Weight-based farming economy",
            ChatColor.GRAY + "Harvest crops for money!",
            "",
            ChatColor.GREEN + "Version: " + plugin.getDescription().getVersion()
        ));

        gui.setItem(22, createItem(Material.BARRIER,
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
        if (!title.equals(ChatColor.DARK_GREEN + "FarmCrops Menu")) return;

        event.setCancelled(true);

        Inventory gui = playerGUIs.get(player);
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(gui)) return;

        int slot = event.getSlot();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);

        switch (slot) {
            case 10:
                player.closeInventory();
                playerGUIs.remove(player);
                plugin.getSellGUI().openGUI(player);
                break;
            case 11:
                player.closeInventory();
                playerGUIs.remove(player);
                plugin.getStatsGUI().openGUI(player);
                break;
            case 12:
                player.closeInventory();
                playerGUIs.remove(player);
                plugin.getTopGUI().openGUI(player, 1);
                break;
            case 13:
                player.closeInventory();
                playerGUIs.remove(player);
                plugin.getPlayerSettingsGUI().openGUI(player);
                break;
            case 15: // Achievements (Premium)
            case 16: // Daily Tasks (Premium)
                if (plugin.getConfig().getBoolean("show-premium-info", true)) {
                    player.sendMessage("");
                    player.sendMessage(ChatColor.GRAY + "This feature is only available in the Premium version.");
                    player.sendMessage("");
                }
                break;
            case 22:
                player.closeInventory();
                playerGUIs.remove(player);
                break;
        }
    }

    private ItemStack createItem(Material mat, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (lore.length > 0) {
                meta.setDisplayName(lore[0]);
                if (lore.length > 1) {
                    meta.setLore(Arrays.asList(Arrays.copyOfRange(lore, 1, lore.length)));
                }
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
