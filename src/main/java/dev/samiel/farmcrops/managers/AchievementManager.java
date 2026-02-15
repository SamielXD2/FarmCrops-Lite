package dev.samiel.farmcrops.managers;
import dev.samiel.farmcrops.FarmCrops;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
public class AchievementManager {
    private final FarmCrops plugin;
    private final Map<UUID, Set<String>> playerAchievements = new HashMap<>();
    public AchievementManager(FarmCrops plugin) {
        this.plugin = plugin;
    }
    public void grantAchievement(Player player, String achievement) {
        UUID uuid = player.getUniqueId();
        playerAchievements.computeIfAbsent(uuid, k -> new HashSet<>()).add(achievement);
    }
    public boolean hasAchievement(Player player, String achievement) {
        UUID uuid = player.getUniqueId();
        return playerAchievements.getOrDefault(uuid, new HashSet<>()).contains(achievement);
    }
    public Set<String> getAchievements(Player player) {
        return playerAchievements.getOrDefault(player.getUniqueId(), new HashSet<>());
    }
}
