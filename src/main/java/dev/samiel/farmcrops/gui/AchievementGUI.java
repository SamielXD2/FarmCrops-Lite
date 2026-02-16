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
public class AchievementGUI implements Listener {
    private final FarmCrops plugin;
    public AchievementGUI(FarmCrops plugin) {
        this.plugin = plugin;
    }
    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Achievements");
        gui.setItem(10, createAchievement(player, "first_harvest", Material.WHEAT,
            ChatColor.GREEN + "First Harvest",
            "Harvest your first crop"
        ));
        gui.setItem(11, createAchievement(player, "100_harvests", Material.IRON_HOE,
            ChatColor.GREEN + "Farming Novice",
            "Harvest 100 crops"
        ));
        gui.setItem(12, createAchievement(player, "1000_harvests", Material.DIAMOND_HOE,
            ChatColor.GREEN + "Farming Expert",
            "Harvest 1,000 crops"
        ));
        gui.setItem(19, createAchievement(player, "first_rare", Material.BLUE_DYE,
            ChatColor.BLUE + "Rare Discovery",
            "Harvest your first RARE crop"
        ));
        gui.setItem(20, createAchievement(player, "first_epic", Material.PURPLE_DYE,
            ChatColor.LIGHT_PURPLE + "Epic Find",
            "Harvest your first EPIC crop"
        ));
        gui.setItem(21, createAchievement(player, "first_legendary", Material.ORANGE_DYE,
            ChatColor.GOLD + "Legendary Farmer",
            "Harvest your first LEGENDARY crop"
        ));
        gui.setItem(22, createAchievement(player, "first_mythic", Material.RED_DYE,
            ChatColor.RED + "Mythic Legend",
            "Harvest your first MYTHIC crop"
        ));
        gui.setItem(28, createAchievement(player, "earn_1000", Material.GOLD_NUGGET,
            ChatColor.YELLOW + "First Thousand",
            "Earn $1,000 from farming"
        ));
        gui.setItem(29, createAchievement(player, "earn_10000", Material.GOLD_INGOT,
            ChatColor.YELLOW + "Money Maker",
            "Earn $10,000 from farming"
        ));
        gui.setItem(30, createAchievement(player, "earn_100000", Material.GOLD_BLOCK,
            ChatColor.YELLOW + "Wealthy Farmer",
            "Earn $100,000 from farming"
        ));
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Your Progress");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Achievements: " + ChatColor.YELLOW + plugin.getAchievementManager().getAchievementCount(player) + "/10");
            lore.add("");
            infoMeta.setLore(lore);
            info.setItemMeta(infoMeta);
        }
        gui.setItem(4, info);
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Close");
            close.setItemMeta(closeMeta);
        }
        gui.setItem(49, close);
        player.openInventory(gui);
    }
    private ItemStack createAchievement(Player player, String id, Material material, String name, String description) {
        boolean unlocked = plugin.getAchievementManager().hasAchievement(player, id);
        ItemStack item = new ItemStack(unlocked ? material : Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(unlocked ? name : ChatColor.GRAY + "???");
            List<String> lore = new ArrayList<>();
            lore.add("");
            if (unlocked) {
                lore.add(ChatColor.GRAY + description);
                lore.add("");
                lore.add(ChatColor.GREEN + "✓ UNLOCKED");
            } else {
                lore.add(ChatColor.GRAY + "???");
                lore.add("");
                lore.add(ChatColor.RED + "✗ LOCKED");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(ChatColor.GOLD + "Achievements")) {
            e.setCancelled(true);
            if (e.getSlot() == 49 && e.getWhoClicked() instanceof Player) {
                e.getWhoClicked().closeInventory();
            }
        }
    }
}
