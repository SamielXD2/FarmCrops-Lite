package dev.samiel.farmcrops.gui;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.*;
import java.util.Arrays;
public class SellGUI implements Listener {
    private final FarmCrops plugin;
    private final Map<Player, Inventory> playerGUIs = new HashMap<>();
    private final Map<Player, List<CropEntry>> playerCropData = new HashMap<>();
    public SellGUI(FarmCrops plugin) {
        this.plugin = plugin;
    }
    static class CropEntry {
        final String tier;
        final Material cropType;   
        final Material dropType;   
        final List<Double> weights; 
        final double basePrice;
        final double tierMultiplier;
        CropEntry(String tier, Material cropType, Material dropType, double basePrice, double tierMultiplier) {
            this.tier = tier;
            this.cropType = cropType;
            this.dropType = dropType;
            this.weights = new ArrayList<>();
            this.basePrice = basePrice;
            this.tierMultiplier = tierMultiplier;
        }
        double totalValue() {
            double total = 0;
            for (double w : weights) {
                total += basePrice * tierMultiplier * w;
            }
            return total;
        }
        double totalWeight() {
            double sum = 0;
            for (double w : weights) sum += w;
            return Math.round(sum * 100.0) / 100.0;
        }
        int count() {
            return weights.size();
        }
        double sellOne() {
            if (weights.isEmpty()) return 0;
            double w = weights.remove(0);
            return basePrice * tierMultiplier * w;
        }
        double sellAll() {
            double total = totalValue();
            weights.clear();
            return total;
        }
        String groupKey() {
            return tier + ":" + cropType.name();
        }
    }
    public void openGUI(Player player) {
        List<CropEntry> entries = scanAndRemoveCrops(player);
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GREEN + "Sell Crops");
        ItemStack grayGlass = createNamedItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) {
            gui.setItem(i, grayGlass);
        }
        ItemStack closeBtn = createNamedItem(Material.BARRIER,
            ChatColor.RED + "" + ChatColor.BOLD + "CLOSE",
            ChatColor.GRAY + "Returns unsold crops to your inventory");
        gui.setItem(53, closeBtn);
        for (int i = 0; i < entries.size() && i < 45; i++) {
            gui.setItem(i, buildGroupedSlot(entries.get(i)));
        }
        refreshSellAllButton(gui, entries);
        playerGUIs.put(player, gui);
        playerCropData.put(player, entries);
        player.openInventory(gui);
    }
    private List<CropEntry> scanAndRemoveCrops(Player player) {
        Map<String, CropEntry> groups = new LinkedHashMap<>();
        ItemStack[] inv = player.getInventory().getContents();
        for (int i = 0; i < inv.length; i++) {
            ItemStack item = inv[i];
            if (item == null || item.getType().isAir() || !item.hasItemMeta()) continue;
            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            if (!pdc.has(CropListener.WEIGHT_KEY, PersistentDataType.DOUBLE)) continue;
            double weight = pdc.get(CropListener.WEIGHT_KEY, PersistentDataType.DOUBLE);
            String tier = pdc.getOrDefault(CropListener.TIER_KEY, PersistentDataType.STRING, "common");
            String cropName = pdc.getOrDefault(CropListener.CROP_KEY, PersistentDataType.STRING, null);
            Material cropType;
            if (cropName != null) {
                cropType = Material.valueOf(cropName);
            } else {
                cropType = item.getType(); 
            }
            Material dropType = item.getType();
            double basePrice = plugin.getCropPrice(cropType);
            double tierMult = plugin.getConfig().getDouble("tiers." + tier + ".multiplier", 1.0);
            String key = tier + ":" + cropType.name();
            CropEntry entry = groups.computeIfAbsent(key,
                k -> new CropEntry(tier, cropType, dropType, basePrice, tierMult));
            entry.weights.add(weight);
            player.getInventory().setItem(i, null);
        }
        return new ArrayList<>(groups.values());
    }
    private ItemStack buildGroupedSlot(CropEntry entry) {
        if (entry.count() == 0) return null;
        String color = plugin.getConfig().getString("tiers." + entry.tier + ".color", "&7");
        String tierName = entry.tier.substring(0, 1).toUpperCase() + entry.tier.substring(1);
        String cropName = CropListener.formatName(entry.cropType);
        ItemStack item = new ItemStack(entry.dropType, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
            color + tierName + " " + cropName));
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Count: " + ChatColor.WHITE + entry.count());
        lore.add(ChatColor.GRAY + "Total Weight: " + ChatColor.WHITE + entry.totalWeight() + " kg");
        lore.add(ChatColor.GRAY + "Total Value: " + ChatColor.GOLD + "$" + String.format("%.2f", entry.totalValue()));
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click " + ChatColor.WHITE + "— Sell one");
        lore.add(ChatColor.YELLOW + "Shift+Click " + ChatColor.WHITE + "— Sell all in group");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!playerGUIs.containsKey(player)) return;
        String title = InventoryUtil.getTitle(event.getView());
        if (!title.equals(ChatColor.GREEN + "Sell Crops")) return;
        event.setCancelled(true);
        Inventory gui = playerGUIs.get(player);
        List<CropEntry> entries = playerCropData.get(player);
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(gui)) return;
        int slot = event.getSlot();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;
        if (slot == 49 && clicked.getType() == Material.EMERALD_BLOCK) {
            sellAll(player, gui, entries);
            return;
        }
        if (slot == 53 && clicked.getType() == Material.BARRIER) {
            returnCropsToPlayer(player, entries);
            player.closeInventory();
            cleanup(player);
            return;
        }
        if (slot >= 45) return;
        if (slot < entries.size()) {
            CropEntry entry = entries.get(slot);
            if (entry.count() == 0) return;
            Economy economy = plugin.getEconomy();
            if (event.isShiftClick()) {
                double value = entry.sellAll();
                economy.depositPlayer(player, value);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                player.sendMessage(ChatColor.GREEN + "Sold group for " + ChatColor.GOLD + "$" + String.format("%.2f", value));
            } else {
                double value = entry.sellOne();
                economy.depositPlayer(player, value);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                player.sendMessage(ChatColor.GREEN + "Sold 1x " + clicked.getItemMeta().getDisplayName()
                    + ChatColor.GREEN + " for " + ChatColor.GOLD + "$" + String.format("%.2f", value));
            }
            if (entry.count() > 0) {
                gui.setItem(slot, buildGroupedSlot(entry));
            } else {
                gui.setItem(slot, null);
            }
            refreshSellAllButton(gui, entries);
        }
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        if (!playerGUIs.containsKey(player)) return;
        String title = InventoryUtil.getTitle(event.getView());
        if (!title.equals(ChatColor.GREEN + "Sell Crops")) return;
        List<CropEntry> entries = playerCropData.get(player);
        if (entries != null) {
            returnCropsToPlayer(player, entries);
        }
        cleanup(player);
    }
    private void sellAll(Player player, Inventory gui, List<CropEntry> entries) {
        double totalEarnings = 0;
        int totalItems = 0;
        for (int i = 0; i < entries.size(); i++) {
            CropEntry entry = entries.get(i);
            if (entry.count() == 0) continue;
            totalItems += entry.count();
            totalEarnings += entry.sellAll();
            gui.setItem(i, null);
        }
        if (totalItems == 0) {
            player.sendMessage(ChatColor.RED + "You have no crops to sell!");
            return;
        }
        plugin.getEconomy().depositPlayer(player, totalEarnings);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "Sold " + totalItems + " crop(s) for "
            + ChatColor.GOLD + "$" + String.format("%.2f", totalEarnings) + ChatColor.GREEN + "!");
        player.closeInventory();
        cleanup(player);
    }
    private void returnCropsToPlayer(Player player, List<CropEntry> entries) {
        for (CropEntry entry : entries) {
            for (double weight : entry.weights) {
                ItemStack item = new ItemStack(entry.dropType, 1);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    PersistentDataContainer pdc = meta.getPersistentDataContainer();
                    pdc.set(CropListener.WEIGHT_KEY, PersistentDataType.DOUBLE, weight);
                    pdc.set(CropListener.TIER_KEY, PersistentDataType.STRING, entry.tier);
                    pdc.set(CropListener.CROP_KEY, PersistentDataType.STRING, entry.cropType.name());
                    String color = plugin.getConfig().getString("tiers." + entry.tier + ".color", "&7");
                    String tierName = entry.tier.substring(0, 1).toUpperCase() + entry.tier.substring(1);
                    double price = entry.basePrice * entry.tierMultiplier * weight;
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                        color + tierName + " " + CropListener.formatName(entry.cropType)));
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.translateAlternateColorCodes('&', color + "Tier: " + tierName));
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&7Weight: &f" + weight + " kg"));
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&7Price: &a$" + String.format("%.2f", price)));
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                for (ItemStack drop : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
            }
        }
    }
    private void refreshSellAllButton(Inventory gui, List<CropEntry> entries) {
        double totalValue = 0;
        int totalItems = 0;
        for (CropEntry e : entries) {
            totalValue += e.totalValue();
            totalItems += e.count();
        }
        ItemStack btn = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = btn.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "SELL ALL");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click to sell all crops");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Total Items: " + ChatColor.WHITE + totalItems);
            lore.add(ChatColor.YELLOW + "Total Value: " + ChatColor.GOLD + "$" + String.format("%.2f", totalValue));
            meta.setLore(lore);
            btn.setItemMeta(meta);
        }
        gui.setItem(49, btn);
    }
    private void cleanup(Player player) {
        playerGUIs.remove(player);
        playerCropData.remove(player);
    }
    private ItemStack createNamedItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
