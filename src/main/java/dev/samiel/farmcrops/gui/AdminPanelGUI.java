package dev.samiel.farmcrops.gui;
import dev.samiel.farmcrops.FarmCrops;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;
public class AdminPanelGUI implements Listener {
    private final FarmCrops plugin;
    public AdminPanelGUI(FarmCrops plugin) {
        this.plugin = plugin;
    }
    public void openMainPanel(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.RED + "Admin Panel");
        ItemStack glass = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta();
        if (gm != null) { gm.setDisplayName(" "); glass.setItemMeta(gm); }
        for (int i = 0; i < 27; i++) gui.setItem(i, glass);
        gui.setItem(10, createItem(Material.WHEAT,
            ChatColor.GREEN + "" + ChatColor.BOLD + "Give Crops",
            "",
            ChatColor.GRAY + "Give custom crops to players",
            ChatColor.GRAY + "Use: /farmgive <player> <crop> <weight> <rarity>",
            "",
            ChatColor.YELLOW + "Example: /farmgive Samiel wheat 5.0 MYTHIC"
        ));
        gui.setItem(11, createItem(Material.BARRIER,
            ChatColor.RED + "" + ChatColor.BOLD + "Clear Player Data",
            "",
            ChatColor.GRAY + "Reset a player's stats",
            ChatColor.GRAY + "Use: /farmstats <player> reset",
            "",
            ChatColor.YELLOW + "Clears all farming data"
        ));
        gui.setItem(12, createItem(Material.PAPER,
            ChatColor.AQUA + "" + ChatColor.BOLD + "Reload Config",
            "",
            ChatColor.GRAY + "Reload configuration",
            ChatColor.GRAY + "Use: /farmreload",
            "",
            ChatColor.YELLOW + "Refreshes all settings"
        ));
        gui.setItem(13, createItem(Material.BOOK,
            ChatColor.GOLD + "" + ChatColor.BOLD + "View Stats",
            "",
            ChatColor.GRAY + "View any player's stats",
            ChatColor.GRAY + "Use: /farmstats <player>",
            "",
            ChatColor.YELLOW + "Check farming progress"
        ));
        gui.setItem(14, createItem(Material.COMPARATOR,
            ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Server Settings",
            "",
            ChatColor.GRAY + "Configure server settings",
            ChatColor.GRAY + "Use: /farmsettings",
            "",
            ChatColor.YELLOW + "Adjust global options"
        ));
        gui.setItem(15, createItem(Material.CHEST,
            ChatColor.YELLOW + "" + ChatColor.BOLD + "Backup Data",
            "",
            ChatColor.GRAY + "Backup all player data",
            ChatColor.GRAY + "Use: /farmbackup",
            "",
            ChatColor.YELLOW + "Creates data backup"
        ));
        gui.setItem(16, createItem(Material.PLAYER_HEAD,
            ChatColor.GREEN + "" + ChatColor.BOLD + "Online Players",
            "",
            ChatColor.GRAY + "Players online: " + ChatColor.WHITE + Bukkit.getOnlinePlayers().size(),
            ChatColor.GRAY + "View leaderboard: /farmtop"
        ));
        gui.setItem(22, createItem(Material.ARROW,
            ChatColor.RED + "" + ChatColor.BOLD + "Close"
        ));
        player.openInventory(gui);
    }
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        if (!e.getView().getTitle().equals(ChatColor.RED + "Admin Panel")) return;
        e.setCancelled(true);
        if (e.getClickedInventory() == null) return;
        int slot = e.getSlot();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        switch (slot) {
            case 10:
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Usage: " + ChatColor.WHITE + "/farmgive <player> <crop> <weight> <rarity>");
                player.sendMessage(ChatColor.GRAY + "Example: /farmgive Samiel wheat 5.0 MYTHIC");
                break;
            case 11:
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "To clear player data, use:");
                player.sendMessage(ChatColor.WHITE + "/farmstats <player> reset");
                break;
            case 12:
                player.closeInventory();
                plugin.reloadConfig();
                player.sendMessage(ChatColor.GREEN + "✓ Configuration reloaded!");
                break;
            case 13:
                player.closeInventory();
                plugin.getTopGUI().openGUI(player, 1);
                break;
            case 14:
                player.closeInventory();
                plugin.getSettingsGUI().openGUI(player);
                break;
            case 15:
                player.closeInventory();
                player.performCommand("farmbackup");
                break;
            case 16:
                player.closeInventory();
                plugin.getTopGUI().openGUI(player, 1);
                break;
            case 22:
                player.closeInventory();
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
