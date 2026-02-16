package dev.samiel.farmcrops.commands;
import dev.samiel.farmcrops.FarmCrops;
import dev.samiel.farmcrops.listeners.CropListener;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.ArrayList;
import java.util.List;
public class FarmGiveCommand implements CommandExecutor {
    private final FarmCrops plugin;
    public FarmGiveCommand(FarmCrops plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("farmcrops.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /farmgive <player> <crop> <weight> <rarity>");
            sender.sendMessage(ChatColor.GRAY + "Example: /farmgive Samiel wheat 5.0 MYTHIC");
            return true;
        }
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }
        Material cropType = parseCropType(args[1]);
        if (cropType == null) {
            sender.sendMessage(ChatColor.RED + "Invalid crop! Use: wheat, carrot, potato, beetroot, melon");
            return true;
        }
        double weight;
        try {
            weight = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid weight!");
            return true;
        }
        String rarity = args[3].toLowerCase();
        ChatColor rarityColor = getRarityColor(rarity);
        ItemStack crop = new ItemStack(cropType);
        ItemMeta meta = crop.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(rarityColor + rarity.toUpperCase() + " " + getCropName(cropType));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Weight: " + ChatColor.WHITE + weight + "kg");
            lore.add(ChatColor.GRAY + "Rarity: " + rarityColor + rarity.toUpperCase());
            double value = plugin.getCropPrice(cropType) * weight * getRarityMultiplier(rarity);
            lore.add(ChatColor.GRAY + "Value: " + ChatColor.GOLD + "$" + String.format("%.2f", value));
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(CropListener.WEIGHT_KEY, PersistentDataType.DOUBLE, weight);
            meta.getPersistentDataContainer().set(CropListener.TIER_KEY, PersistentDataType.STRING, rarity);
            meta.getPersistentDataContainer().set(CropListener.CROP_KEY, PersistentDataType.STRING, cropType.name());
            crop.setItemMeta(meta);
        }
        target.getInventory().addItem(crop);
        sender.sendMessage(ChatColor.GREEN + "✓ Gave " + target.getName() + " " + rarityColor + rarity.toUpperCase() + ChatColor.GREEN + " " + getCropName(cropType));
        target.sendMessage(ChatColor.GREEN + "You received a " + rarityColor + rarity.toUpperCase() + ChatColor.GREEN + " " + getCropName(cropType) + ChatColor.GREEN + "!");
        return true;
    }
    private Material parseCropType(String input) {
        switch (input.toLowerCase()) {
            case "wheat": return Material.WHEAT;
            case "carrot": case "carrots": return Material.CARROTS;
            case "potato": case "potatoes": return Material.POTATOES;
            case "beetroot": case "beetroots": return Material.BEETROOTS;
            case "melon": return Material.MELON;
            default: return null;
        }
    }
    private String getCropName(Material crop) {
        switch (crop) {
            case WHEAT: return "Wheat";
            case CARROTS: return "Carrot";
            case POTATOES: return "Potato";
            case BEETROOTS: return "Beetroot";
            case MELON: return "Melon";
            default: return crop.name();
        }
    }
    private ChatColor getRarityColor(String rarity) {
        switch (rarity.toLowerCase()) {
            case "common": return ChatColor.GRAY;
            case "uncommon": return ChatColor.GREEN;
            case "rare": return ChatColor.BLUE;
            case "epic": return ChatColor.LIGHT_PURPLE;
            case "legendary": return ChatColor.GOLD;
            case "mythic": return ChatColor.RED;
            default: return ChatColor.WHITE;
        }
    }
    private double getRarityMultiplier(String rarity) {
        switch (rarity.toLowerCase()) {
            case "common": return 1.0;
            case "uncommon": return 1.5;
            case "rare": return 2.0;
            case "epic": return 3.0;
            case "legendary": return 5.0;
            case "mythic": return 10.0;
            default: return 1.0;
        }
    }
}
