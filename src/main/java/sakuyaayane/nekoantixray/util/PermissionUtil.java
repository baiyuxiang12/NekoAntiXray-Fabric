package sakuyaayane.nekoantixray.util;

import net.minecraft.server.network.ServerPlayerEntity;
import sakuyaayane.nekoantixray.NekoAntiXray;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for permission checking
 */
public class PermissionUtil {
    // Simple permission cache to avoid frequent checks
    private static final Map<UUID, Map<String, Boolean>> permissionCache = new HashMap<>();
    
    /**
     * Check if a player has a permission
     * @param player The player to check
     * @param permission The permission to check
     * @return true if the player has the permission
     */
    public static boolean hasPermission(ServerPlayerEntity player, String permission) {
        if (player == null) return false;
        
        // Server operators always have all permissions
        if (player.hasPermissionLevel(4)) {
            return true;
        }
        
        UUID playerUUID = player.getUuid();
        
        // Check cache first
        if (permissionCache.containsKey(playerUUID)) {
            Map<String, Boolean> playerPerms = permissionCache.get(playerUUID);
            if (playerPerms.containsKey(permission)) {
                return playerPerms.get(permission);
            }
        }
        
        // For now, implement a simple permission system
        // In a real implementation, this would integrate with a permission system like LuckPerms
        boolean hasPermission = checkPermission(player, permission);
        
        // Cache the result
        permissionCache.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(permission, hasPermission);
        
        return hasPermission;
    }
    
    /**
     * Check if a player has a permission (actual implementation)
     * @param player The player to check
     * @param permission The permission to check
     * @return true if the player has the permission
     */
    private static boolean checkPermission(ServerPlayerEntity player, String permission) {
        // This is a placeholder for actual permission checking
        // In a real implementation, this would check against a permission system
        
        // For now, only operators have permissions
        return player.hasPermissionLevel(4);
    }
    
    /**
     * Clear the permission cache for a player
     * @param playerUUID The UUID of the player
     */
    public static void clearCache(UUID playerUUID) {
        permissionCache.remove(playerUUID);
    }
    
    /**
     * Clear the entire permission cache
     */
    public static void clearAllCaches() {
        permissionCache.clear();
    }
}
