package dev.samiel.farmcrops.gui;
import dev.samiel.farmcrops.FarmCrops;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
        ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Give Crops");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Give custom crops to players");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        gui.setItem(13, item);
        player.openInventory(gui);
    }
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().contains("Admin Panel")) {
            e.setCancelled(true);
        }
    }
}
