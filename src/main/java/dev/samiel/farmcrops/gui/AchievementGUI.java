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
import java.util.*;
public class AchievementGUI implements Listener {
    private final FarmCrops plugin;
    public AchievementGUI(FarmCrops plugin) {
        this.plugin = plugin;
    }
    public void openGUI(Player player) {
        int total    = plugin.getAchievementManager().getTotalAchievements();
        int unlocked = plugin.getAchievementManager().getAchievementCount(player);
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Achievements");
        // Fill with black glass
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta();
        if (gm != null) { gm.setDisplayName(" "); glass.setItemMeta(gm); }
        for (int i = 0; i < 54; i++) gui.setItem(i, glass);
        // Header
        gui.setItem(4, createItem(Material.NETHER_STAR,
            ChatColor.GOLD + "" + ChatColor.BOLD + "YOUR ACHIEVEMENTS",
            "",
            ChatColor.GRAY + "Unlocked: " + ChatColor.GREEN + unlocked + ChatColor.GRAY + "/" + total,
            ChatColor.GRAY + "Remaining: " + ChatColor.RED + (total - unlocked),
            "",
            ChatColor.AQUA + "Keep farming to unlock all 25!"
        ));
        // 25 slots across 4 rows (skip borders)
        int[] slots = {
            10, 11, 12, 13, 14, 15, 16,   // row 2 (7)
            19, 20, 21, 22, 23, 24, 25,   // row 3 (7)
            28, 29, 30, 31, 32, 33, 34,   // row 4 (7)
            37, 38                         // row 5 (2 = 25 total)
        };
        int idx = 0;
        for (AchievementManager.AchievementData ach : plugin.getAchievementManager().getAllAchievements()) {
            if (idx >= slots.length) break;
            gui.setItem(slots[idx], createAchievementItem(player, ach));
            idx++;
        }
        // Close button
        gui.setItem(49, createItem(Material.BARRIER, ChatColor.RED + "" + ChatColor.BOLD + "Close"));
        player.openInventory(gui);
    }
    private ItemStack createAchievementItem(Player player, AchievementManager.AchievementData ach) {
        boolean unlocked = plugin.getAchievementManager().hasAchievement(player, ach.id);
        ItemStack item = new ItemStack(unlocked ? getMaterial(ach.type) : Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(unlocked
                ? getColor(ach.type) + "" + ChatColor.BOLD + ach.name
                : ChatColor.DARK_GRAY + "??? Locked");
            List<String> lore = new ArrayList<>();
            lore.add("");
            if (unlocked) {
                lore.add(ChatColor.GRAY + ach.description);
                lore.add("");
                lore.add(ChatColor.YELLOW + "Requirement: " + ChatColor.WHITE + getReqText(ach));
                lore.add("");
                lore.add(ChatColor.GREEN + "✔ UNLOCKED");
            } else {
                lore.add(ChatColor.DARK_GRAY + "???");
                lore.add("");
                lore.add(ChatColor.GRAY + "Hint: " + getHint(ach.type));
                lore.add("");
                lore.add(ChatColor.RED + "✘ LOCKED");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    private Material getMaterial(AchievementManager.AchievementType type) {
        switch (type) {
            case HARVEST:             return Material.IRON_HOE;
            case EARNINGS:            return Material.GOLD_INGOT;
            case TIER_RARE:           return Material.LAPIS_LAZULI;
            case TIER_EPIC:           return Material.AMETHYST_SHARD;
            case TIER_LEGENDARY:      return Material.GOLD_BLOCK;
            case TIER_MYTHIC:         return Material.NETHERITE_INGOT;
            case COLLECTION_WHEAT:    return Material.WHEAT;
            case COLLECTION_CARROT:   return Material.CARROT;
            case COLLECTION_POTATO:   return Material.POTATO;
            case COLLECTION_BEETROOT: return Material.BEETROOT;
            case COLLECTION_MELON:    return Material.MELON_SLICE;
            case COLLECTION_ALL:      return Material.CHEST;
            default:                  return Material.PAPER;
        }
    }
    private ChatColor getColor(AchievementManager.AchievementType type) {
        switch (type) {
            case HARVEST:             return ChatColor.GREEN;
            case EARNINGS:            return ChatColor.GOLD;
            case TIER_RARE:           return ChatColor.BLUE;
            case TIER_EPIC:           return ChatColor.LIGHT_PURPLE;
            case TIER_LEGENDARY:      return ChatColor.GOLD;
            case TIER_MYTHIC:         return ChatColor.RED;
            case COLLECTION_WHEAT:
            case COLLECTION_CARROT:
            case COLLECTION_POTATO:
            case COLLECTION_BEETROOT:
            case COLLECTION_MELON:    return ChatColor.YELLOW;
            case COLLECTION_ALL:      return ChatColor.AQUA;
            default:                  return ChatColor.WHITE;
        }
    }
    private String getReqText(AchievementManager.AchievementData ach) {
        switch (ach.type) {
            case HARVEST:             return ach.requirement + " crops harvested";
            case EARNINGS:            return "$" + String.format("%,d", ach.requirement) + " earned";
            case TIER_RARE:           return ach.requirement + " Rare crop(s)";
            case TIER_EPIC:           return ach.requirement + " Epic crop(s)";
            case TIER_LEGENDARY:      return ach.requirement + " Legendary crop(s)";
            case TIER_MYTHIC:         return ach.requirement + " Mythic crop(s)";
            case COLLECTION_WHEAT:    return ach.requirement + " wheat";
            case COLLECTION_CARROT:   return ach.requirement + " carrots";
            case COLLECTION_POTATO:   return ach.requirement + " potatoes";
            case COLLECTION_BEETROOT: return ach.requirement + " beetroots";
            case COLLECTION_MELON:    return ach.requirement + " melons";
            case COLLECTION_ALL:      return ach.requirement + " of every crop type";
            default:                  return String.valueOf(ach.requirement);
        }
    }
    private String getHint(AchievementManager.AchievementType type) {
        switch (type) {
            case HARVEST:             return "Keep harvesting crops!";
            case EARNINGS:            return "Sell crops to earn money!";
            case TIER_RARE:           return "Find a Rare drop!";
            case TIER_EPIC:           return "Find an Epic drop!";
            case TIER_LEGENDARY:      return "Find a Legendary drop!";
            case TIER_MYTHIC:         return "Find an extremely rare Mythic drop!";
            case COLLECTION_WHEAT:    return "Harvest lots of wheat!";
            case COLLECTION_CARROT:   return "Harvest lots of carrots!";
            case COLLECTION_POTATO:   return "Harvest lots of potatoes!";
            case COLLECTION_BEETROOT: return "Harvest lots of beetroots!";
            case COLLECTION_MELON:    return "Harvest lots of melons!";
            case COLLECTION_ALL:      return "Harvest all crop types!";
            default:                  return "Keep farming!";
        }
    }
    private ItemStack createItem(Material material, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null && lore.length > 0) {
            meta.setDisplayName(lore[0]);
            if (lore.length > 1) {
                meta.setLore(new ArrayList<>(Arrays.asList(lore).subList(1, lore.length)));
            }
            item.setItemMeta(meta);
        }
        return item;
    }
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(ChatColor.GOLD + "Achievements")) return;
        e.setCancelled(true);
        if (e.getSlot() == 49 && e.getWhoClicked() instanceof Player) {
            Player p = (Player) e.getWhoClicked();
            p.closeInventory();
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
    }
}
