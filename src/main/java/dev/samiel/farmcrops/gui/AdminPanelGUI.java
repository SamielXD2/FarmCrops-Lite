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
import java.util.*;
public class AdminPanelGUI implements Listener {
    private final FarmCrops plugin;
    public AdminPanelGUI(FarmCrops plugin) {
        this.plugin = plugin;
    }
    public void openMainPanel(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.RED + "Admin Panel");
        // Border
        ItemStack glass = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta();
        if (gm != null) {
            gm.setDisplayName(" ");
            glass.setItemMeta(gm);
        }
        for (int i : new int[]{0,1,2,3,5,6,7,8,9,17,18,19,20,21,22,23,24,25,26}) {
            gui.setItem(i, glass);
        }
        // Give Custom Crop
        gui.setItem(10, createItem(Material.WHEAT,
            ChatColor.GREEN + "" + ChatColor.BOLD + "Give Custom Crop",
            "",
            ChatColor.GRAY + "Command: " + ChatColor.WHITE + "/farmgive <player> <crop> <weight> <tier>",
            "",
            ChatColor.YELLOW + "Example:",
            ChatColor.WHITE + "/farmgive Samiel wheat 5.0 mythic"
        ));
        // Reset Player Stats
        gui.setItem(12, createItem(Material.BARRIER,
            ChatColor.RED + "" + ChatColor.BOLD + "Reset Player Stats",
            "",
            ChatColor.GRAY + "Command: " + ChatColor.WHITE + "/farm reset <player>",
            "",
            ChatColor.YELLOW + "Example:",
            ChatColor.WHITE + "/farm reset Samiel",
            "",
            ChatColor.DARK_RED + "⚠ This deletes ALL farming data!"
        ));
        // Reload Config
        gui.setItem(14, createItem(Material.WRITABLE_BOOK,
            ChatColor.AQUA + "" + ChatColor.BOLD + "Reload Config",
            "",
            ChatColor.GRAY + "Command: " + ChatColor.WHITE + "/farmreload",
            "",
            ChatColor.GRAY + "Reloads config.yml without restarting"
        ));
        // Server Settings
        gui.setItem(16, createItem(Material.COMPARATOR,
            ChatColor.GOLD + "" + ChatColor.BOLD + "Server Settings",
            "",
            ChatColor.GRAY + "Command: " + ChatColor.WHITE + "/farmsettings",
            "",
            ChatColor.GRAY + "Opens server-wide plugin settings"
        ));
        // Close
        gui.setItem(4, createItem(Material.OAK_DOOR,
            ChatColor.RED + "" + ChatColor.BOLD + "Close"
        ));
        player.openInventory(gui);
    }
    private ItemStack createItem(Material mat, String... lines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null && lines.length > 0) {
            meta.setDisplayName(lines[0]);
            if (lines.length > 1) {
                List<String> lore = new ArrayList<>();
                for (int i = 1; i < lines.length; i++) {
                    lore.add(lines[i]);
                }
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(ChatColor.RED + "Admin Panel")) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        if (e.getSlot() == 4) {
            player.closeInventory();
        }
    }
}
