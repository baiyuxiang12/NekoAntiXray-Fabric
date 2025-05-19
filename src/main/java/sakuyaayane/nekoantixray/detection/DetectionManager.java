package sakuyaayane.nekoantixray.detection;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import sakuyaayane.nekoantixray.NekoAntiXray;
import sakuyaayane.nekoantixray.config.ConfigManager;
import sakuyaayane.nekoantixray.util.FakeOreManager;
import sakuyaayane.nekoantixray.util.PermissionUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages detection of xray behavior
 */
public class DetectionManager {
    private final Map<UUID, PlayerViolationData> playerViolations;
    private final Map<ChunkPos, Long> lastChunkGenerationTime;
    
    public DetectionManager() {
        this.playerViolations = new ConcurrentHashMap<>();
        this.lastChunkGenerationTime = new HashMap<>();
    }
    
    /**
     * Called when a player breaks a block
     * @param player The player who broke the block
     * @param world The world where the block was broken
     * @param pos The position of the broken block
     * @param state The state of the broken block
     */
    public void onBlockBreak(ServerPlayerEntity player, ServerWorld world, BlockPos pos, BlockState state) {
        if (player == null || world == null) return;
        
        // Skip if player has bypass permission
        if (PermissionUtil.hasPermission(player, "nekoantixray.bypass")) {
            return;
        }
        
        // Skip if world is in blacklist
        ConfigManager.ConfigData config = NekoAntiXray.getInstance().getConfigManager().getConfig();
        String worldName = world.getRegistryKey().getValue().toString();
        if (config.blacklistWorlds.contains(worldName)) {
            return;
        }
        
        // Check if the broken block is a fake ore
        FakeOreManager fakeOreManager = NekoAntiXray.getInstance().getFakeOreManager();
        if (fakeOreManager.isFakeOre(world, pos)) {
            // Player broke a fake ore, increase violation level
            UUID playerUUID = player.getUuid();
            PlayerViolationData violationData = playerViolations.computeIfAbsent(playerUUID, uuid -> new PlayerViolationData());
            
            // Check if the fake ore was generated for this player
            boolean isOwnFakeOre = fakeOreManager.isPlayerFakeOre(player.getUuid(), world, pos);
            double vlIncrease = isOwnFakeOre ? config.vlAddBreakSelf : config.vlAddBreakOther;
            
            // Increase violation level
            violationData.addViolation(vlIncrease);
            
            // Log the violation
            NekoAntiXray.LOGGER.info("Player {} broke a fake ore at {}, violation level: {}", 
                    player.getName().getString(), pos, violationData.getViolationLevel());
            
            // Check if player should be banned
            if (config.vlBanEnable && violationData.getViolationLevel() >= config.vlBanThreshold) {
                executeBanCommand(player, violationData.getViolationLevel());
            }
            
            // Remove the fake ore
            fakeOreManager.removeFakeOre(world, pos);
        }
    }
    
    /**
     * Executes the ban command for a player
     * @param player The player to ban
     * @param violationLevel The player's violation level
     */
    private void executeBanCommand(ServerPlayerEntity player, double violationLevel) {
        ConfigManager.ConfigData config = NekoAntiXray.getInstance().getConfigManager().getConfig();
        String command = config.vlBanCommand
                .replace("%player%", player.getName().getString())
                .replace("%duration%", String.valueOf(config.banDuration))
                .replace("%vl%", String.format("%.1f", violationLevel));
        
        // Execute the ban command on the server
        player.getServer().getCommandManager().executeWithPrefix(
                player.getServer().getCommandSource(), command);
        
        // Announce the ban if enabled
        if (config.enableBanAnnouncement) {
            String announcement = config.banAnnouncement
                    .replace("%player%", player.getName().getString());
            
            // Broadcast to all players
            for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                serverPlayer.sendMessage(net.minecraft.text.Text.of(announcement), false);
            }
        }
        
        // Reset violation level after ban
        playerViolations.remove(player.getUuid());
    }
    
    /**
     * Decreases violation levels for all players periodically
     */
    public void decreaseViolations() {
        ConfigManager.ConfigData config = NekoAntiXray.getInstance().getConfigManager().getConfig();
        double decreaseAmount = config.violationDecreaseAmount;
        
        for (PlayerViolationData data : playerViolations.values()) {
            data.decreaseViolation(decreaseAmount);
        }
    }
    
    /**
     * Gets a player's current violation level
     * @param playerUUID The UUID of the player
     * @return The player's violation level
     */
    public double getPlayerViolationLevel(UUID playerUUID) {
        PlayerViolationData data = playerViolations.get(playerUUID);
        return data != null ? data.getViolationLevel() : 0.0;
    }
    
    /**
     * Resets a player's violation level
     * @param playerUUID The UUID of the player
     */
    public void resetPlayerViolation(UUID playerUUID) {
        playerViolations.remove(playerUUID);
    }
    
    /**
     * Checks if a chunk is ready for fake ore generation
     * @param chunkPos The chunk position
     * @return true if the chunk is ready for fake ore generation
     */
    public boolean canGenerateFakeOresInChunk(ChunkPos chunkPos) {
        ConfigManager.ConfigData config = NekoAntiXray.getInstance().getConfigManager().getConfig();
        long currentTime = System.currentTimeMillis();
        long lastGenTime = lastChunkGenerationTime.getOrDefault(chunkPos, 0L);
        
        // Check if cooldown has passed
        return (currentTime - lastGenTime) >= (config.fakeOreGenerationCooldown * 1000L);
    }
    
    /**
     * Marks a chunk as having fake ores generated
     * @param chunkPos The chunk position
     */
    public void markChunkFakeOreGeneration(ChunkPos chunkPos) {
        lastChunkGenerationTime.put(chunkPos, System.currentTimeMillis());
    }
    
    /**
     * Class to track player violation data
     */
    private static class PlayerViolationData {
        private double violationLevel;
        private long lastViolationTime;
        private int consecutiveViolations;
        
        public PlayerViolationData() {
            this.violationLevel = 0.0;
            this.lastViolationTime = 0L;
            this.consecutiveViolations = 0;
        }
        
        public void addViolation(double amount) {
            long currentTime = System.currentTimeMillis();
            ConfigManager.ConfigData config = NekoAntiXray.getInstance().getConfigManager().getConfig();
            
            // Check for consecutive violations
            if ((currentTime - lastViolationTime) <= (config.continuousIncreaseInterval * 1000L)) {
                consecutiveViolations++;
                
                // Add extra violation if threshold reached
                if (consecutiveViolations >= config.continuousIncreaseCount) {
                    amount += 1.0; // Extra penalty for consecutive violations
                    consecutiveViolations = 0; // Reset counter
                }
            } else {
                consecutiveViolations = 1; // Reset to 1 (this is the first violation in a new sequence)
            }
            
            violationLevel += amount;
            lastViolationTime = currentTime;
        }
        
        public void decreaseViolation(double amount) {
            violationLevel = Math.max(0.0, violationLevel - amount);
        }
        
        public double getViolationLevel() {
            return violationLevel;
        }
    }
}
