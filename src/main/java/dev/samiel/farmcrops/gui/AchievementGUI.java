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
public class AchievementGUI implements Listener {
    private final FarmCrops plugin;
    public AchievementGUI(FarmCrops plugin) {
        this.plugin = plugin;
    }
    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Achievements");
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta();
        if (gm != null) { gm.setDisplayName(" "); glass.setItemMeta(gm); }
        for (int i = 0; i < 54; i++) gui.setItem(i, glass);
        gui.setItem(4, createItem(Material.NETHER_STAR,
            ChatColor.GOLD + "" + ChatColor.BOLD + "YOUR ACHIEVEMENTS",
            "",
            ChatColor.GRAY + "Progress: " + ChatColor.YELLOW + plugin.getAchievementManager().getAchievementCount(player) + "/10",
            "",
            ChatColor.AQUA + "Keep farming to unlock more!"
        ));
        gui.setItem(19, createAchievement(player, "first_harvest", Material.WHEAT,
            ChatColor.GREEN + "First Harvest",
            "Harvest your first crop"
        ));
        gui.setItem(20, createAchievement(player, "100_harvests", Material.IRON_HOE,
            ChatColor.GREEN + "Farming Novice",
            "Harvest 100 crops"
        ));
        gui.setItem(21, createAchievement(player, "1000_harvests", Material.DIAMOND_HOE,
            ChatColor.GREEN + "Farming Expert",
            "Harvest 1,000 crops"
        ));
        gui.setItem(28, createAchievement(player, "first_rare", Material.LAPIS_LAZULI,
            ChatColor.BLUE + "Rare Discovery",
            "Harvest your first RARE crop"
        ));
        gui.setItem(29, createAchievement(player, "first_epic", Material.AMETHYST_SHARD,
            ChatColor.LIGHT_PURPLE + "Epic Find",
            "Harvest your first EPIC crop"
        ));
        gui.setItem(30, createAchievement(player, "first_legendary", Material.GOLD_INGOT,
            ChatColor.GOLD + "Legendary Farmer",
            "Harvest your first LEGENDARY crop"
        ));
        gui.setItem(31, createAchievement(player, "first_mythic", Material.NETHERITE_INGOT,
            ChatColor.RED + "Mythic Legend",
            "Harvest your first MYTHIC crop"
        ));
        gui.setItem(37, createAchievement(player, "earn_1000", Material.GOLD_NUGGET,
            ChatColor.YELLOW + "First Thousand",
            "Earn $1,000 from farming"
        ));
        gui.setItem(38, createAchievement(player, "earn_10000", Material.GOLD_INGOT,
            ChatColor.YELLOW + "Money Maker",
            "Earn $10,000 from farming"
        ));
        gui.setItem(39, createAchievement(player, "earn_100000", Material.GOLD_BLOCK,
            ChatColor.YELLOW + "Wealthy Farmer",
            "Earn $100,000 from farming"
        ));
        gui.setItem(49, createItem(Material.BARRIER,
            ChatColor.RED + "" + ChatColor.BOLD + "Close"
        ));
        player.openInventory(gui);
    }
    private ItemStack createAchievement(Player player, String id, Material material, String name, String description) {
        boolean unlocked = plugin.getAchievementManager().hasAchievement(player, id);
        ItemStack item = new ItemStack(unlocked ? material : Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(unlocked ? name : ChatColor.DARK_GRAY + "???");
            List<String> lore = new ArrayList<>();
            lore.add("");
            if (unlocked) {
                lore.add(ChatColor.GRAY + description);
                lore.add("");
                lore.add(ChatColor.GREEN + "✓ UNLOCKED");
                lore.add(ChatColor.GRAY + "Great job!");
            } else {
                lore.add(ChatColor.DARK_GRAY + "???");
                lore.add("");
                lore.add(ChatColor.RED + "✗ LOCKED");
                lore.add(ChatColor.GRAY + "Keep farming to unlock!");
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
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(ChatColor.GOLD + "Achievements")) {
            e.setCancelled(true);
            if (e.getSlot() == 49 && e.getWhoClicked() instanceof Player) {
                e.getWhoClicked().closeInventory();
                ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
        }
    }
}
