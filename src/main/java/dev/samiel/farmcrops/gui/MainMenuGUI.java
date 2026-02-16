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
public class MainMenuGUI implements Listener {
    private final FarmCrops plugin;
    private final Map<Player, Inventory> playerGUIs = new HashMap<>();
    public MainMenuGUI(FarmCrops plugin) {
        this.plugin = plugin;
    }
    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "FarmCrops Menu");
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta();
        if (gm != null) { gm.setDisplayName(" "); glass.setItemMeta(gm); }
        for (int i = 0; i < 54; i++) gui.setItem(i, glass);
        if (player.hasPermission("farmcrops.admin")) {
            gui.setItem(4, createItem(Material.SUNFLOWER,
                ChatColor.YELLOW + "" + ChatColor.BOLD + "FARMCROPS",
                "",
                ChatColor.GRAY + "Weight-based farming economy",
                ChatColor.GRAY + "Harvest crops to earn money!",
                "",
                ChatColor.GREEN + "Version: " + plugin.getDescription().getVersion()
            ));
        }
        gui.setItem(20, createItem(Material.EMERALD,
            ChatColor.GREEN + "" + ChatColor.BOLD + "Sell Crops",
            "",
            ChatColor.GRAY + "Open the sell GUI to",
            ChatColor.GRAY + "sell your harvested crops",
            "",
            ChatColor.YELLOW + "➜ Click to open"
        ));
        gui.setItem(21, createItem(Material.BOOK,
            ChatColor.AQUA + "" + ChatColor.BOLD + "Your Stats",
            "",
            ChatColor.GRAY + "View your farming",
            ChatColor.GRAY + "statistics and progress",
            "",
            ChatColor.YELLOW + "➜ Click to view"
        ));
        gui.setItem(22, createItem(Material.GOLD_BLOCK,
            ChatColor.GOLD + "" + ChatColor.BOLD + "Leaderboard",
            "",
            ChatColor.GRAY + "View top farmers on",
            ChatColor.GRAY + "the server",
            "",
            ChatColor.YELLOW + "➜ Click to view"
        ));
        gui.setItem(23, createItem(Material.COMPARATOR,
            ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Settings",
            "",
            ChatColor.GRAY + "Configure your personal",
            ChatColor.GRAY + "farming preferences",
            "",
            ChatColor.YELLOW + "➜ Click to open"
        ));
        gui.setItem(24, createItem(Material.WRITABLE_BOOK,
            ChatColor.GOLD + "" + ChatColor.BOLD + "Achievements",
            "",
            ChatColor.GRAY + "Track your farming",
            ChatColor.GRAY + "achievements and progress",
            "",
            ChatColor.YELLOW + "➜ Click to view"
        ));
        if (player.hasPermission("farmcrops.admin")) {
            gui.setItem(30, createItem(Material.COMMAND_BLOCK,
                ChatColor.RED + "" + ChatColor.BOLD + "Admin Panel",
                "",
                ChatColor.GRAY + "Manage server settings",
                ChatColor.GRAY + "and player data",
                "",
                ChatColor.YELLOW + "➜ Click to open"
            ));
        }
        gui.setItem(31, createItem(Material.WHEAT,
            ChatColor.GREEN + "" + ChatColor.BOLD + "How to Play",
            "",
            ChatColor.GRAY + "1. Harvest crops from farms",
            ChatColor.GRAY + "2. Each crop has weight + rarity",
            ChatColor.GRAY + "3. Sell crops for money",
            ChatColor.GRAY + "4. Unlock achievements!",
            "",
            ChatColor.AQUA + "Rarities: Common, Uncommon, Rare,",
            ChatColor.AQUA + "Epic, Legendary, Mythic"
        ));
        gui.setItem(32, createItem(Material.PAPER,
            ChatColor.AQUA + "" + ChatColor.BOLD + "Quick Commands",
            "",
            ChatColor.YELLOW + "/farm " + ChatColor.GRAY + "- Open this menu",
            ChatColor.YELLOW + "/sellcrops " + ChatColor.GRAY + "- Sell your crops",
            ChatColor.YELLOW + "/farmstats " + ChatColor.GRAY + "- View stats",
            ChatColor.YELLOW + "/farmtop " + ChatColor.GRAY + "- View leaderboard",
            ChatColor.YELLOW + "/farmachievements " + ChatColor.GRAY + "- View achievements"
        ));
        gui.setItem(49, createItem(Material.BARRIER,
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
            case 20:
                player.closeInventory();
                plugin.getSellGUI().openGUI(player);
                break;
            case 21:
                player.closeInventory();
                plugin.getStatsGUI().openGUI(player);
                break;
            case 22:
                player.closeInventory();
                plugin.getTopGUI().openGUI(player, 1);
                break;
            case 23:
                player.closeInventory();
                plugin.getPlayerSettingsGUI().openGUI(player);
                break;
            case 24:
                player.closeInventory();
                plugin.getAchievementGUI().openGUI(player);
                break;
            case 30:
                if (player.hasPermission("farmcrops.admin")) {
                    player.closeInventory();
                    plugin.getAdminPanelGUI().openMainPanel(player);
                }
                break;
            case 49:
                player.closeInventory();
                playerGUIs.remove(player);
                break;
        }
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
