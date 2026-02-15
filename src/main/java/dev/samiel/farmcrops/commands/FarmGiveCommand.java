package dev.samiel.farmcrops.commands;
import dev.samiel.farmcrops.FarmCrops;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
            return true;
        }
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }
        Material cropType = parseCropType(args[1]);
        if (cropType == null) {
            sender.sendMessage(ChatColor.RED + "Invalid crop!");
            return true;
        }
        double weight;
        try {
            weight = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid weight!");
            return true;
        }
        String rarity = args[3].toUpperCase();
        ChatColor rarityColor = getRarityColor(rarity);
        ItemStack crop = new ItemStack(cropType);
        ItemMeta meta = crop.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(rarityColor + rarity + " " + getCropName(cropType));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Weight: " + ChatColor.WHITE + weight + "kg");
            lore.add(ChatColor.GRAY + "Rarity: " + rarityColor + rarity);
            meta.setLore(lore);
            crop.setItemMeta(meta);
        }
        target.getInventory().addItem(crop);
        sender.sendMessage(ChatColor.GREEN + "Gave " + target.getName() + " " + rarityColor + rarity + ChatColor.GREEN + " " + getCropName(cropType));
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
        switch (rarity.toUpperCase()) {
            case "COMMON": return ChatColor.GRAY;
            case "UNCOMMON": return ChatColor.GREEN;
            case "RARE": return ChatColor.BLUE;
            case "EPIC": return ChatColor.LIGHT_PURPLE;
            case "LEGENDARY": return ChatColor.GOLD;
            case "MYTHIC": return ChatColor.RED;
            default: return ChatColor.WHITE;
        }
    }
}
