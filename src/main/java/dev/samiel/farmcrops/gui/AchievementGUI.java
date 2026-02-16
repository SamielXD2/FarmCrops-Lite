package dev.samiel.farmcrops.gui;
import dev.samiel.farmcrops.FarmCrops;
import dev.samiel.farmcrops.managers.AchievementManager;
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
            ChatColor.GRAY + "Progress: " + ChatColor.YELLOW + 
                plugin.getAchievementManager().getAchievementCount(player) + "/" + 
                plugin.getAchievementManager().getTotalAchievements(),
            "",
            ChatColor.AQUA + "Keep farming to unlock more!"
        ));
        int slot = 19;
        for (AchievementManager.AchievementData ach : plugin.getAchievementManager().getAllAchievements()) {
            gui.setItem(slot, createAchievementItem(player, ach));
            slot++;
            if (slot == 26) slot = 28;
            if (slot >= 35) break;
        }
        gui.setItem(49, createItem(Material.BARRIER,
            ChatColor.RED + "" + ChatColor.BOLD + "Close"
        ));
        player.openInventory(gui);
    }
    private ItemStack createAchievementItem(Player player, AchievementManager.AchievementData ach) {
        boolean unlocked = plugin.getAchievementManager().hasAchievement(player, ach.id);
        Material material = getMaterialForType(ach.type, unlocked);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(unlocked ? getColorForType(ach.type) + ach.name : ChatColor.DARK_GRAY + "???");
            List<String> lore = new ArrayList<>();
            lore.add("");
            if (unlocked) {
                lore.add(ChatColor.GRAY + ach.description);
                lore.add("");
                lore.add(ChatColor.YELLOW + "Requirement: " + ChatColor.WHITE + getRequirementText(ach));
                lore.add("");
                lore.add(ChatColor.GREEN + "✓ UNLOCKED");
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
    private Material getMaterialForType(AchievementManager.AchievementType type, boolean unlocked) {
        if (!unlocked) return Material.GRAY_DYE;
        switch (type) {
            case HARVEST: return Material.IRON_HOE;
            case EARNINGS: return Material.GOLD_INGOT;
            case TIER_EPIC: return Material.AMETHYST_SHARD;
            case TIER_LEGENDARY: return Material.GOLD_BLOCK;
            case TIER_MYTHIC: return Material.NETHERITE_INGOT;
            case COLLECTION_WHEAT: return Material.WHEAT;
            default: return Material.PAPER;
        }
    }
    private ChatColor getColorForType(AchievementManager.AchievementType type) {
        switch (type) {
            case HARVEST: return ChatColor.GREEN;
            case EARNINGS: return ChatColor.GOLD;
            case TIER_EPIC: return ChatColor.LIGHT_PURPLE;
            case TIER_LEGENDARY: return ChatColor.GOLD;
            case TIER_MYTHIC: return ChatColor.RED;
            case COLLECTION_WHEAT: return ChatColor.YELLOW;
            default: return ChatColor.WHITE;
        }
    }
    private String getRequirementText(AchievementManager.AchievementData ach) {
        switch (ach.type) {
            case HARVEST: return ach.requirement + " harvests";
            case EARNINGS: return "$" + ach.requirement + " earned";
            case TIER_EPIC: return ach.requirement + " epic crop";
            case TIER_LEGENDARY: return ach.requirement + " legendary crop";
            case TIER_MYTHIC: return ach.requirement + " mythic crop";
            case COLLECTION_WHEAT: return ach.requirement + " wheat harvested";
            default: return String.valueOf(ach.requirement);
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
