package sakuyaayane.nekoantixray.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import sakuyaayane.nekoantixray.NekoAntiXray;
import sakuyaayane.nekoantixray.config.ConfigManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages fake ore generation and tracking
 */
public class FakeOreManager {
    // Map to track fake ores: world -> position -> expiration time
    private final Map<ServerWorld, Map<BlockPos, Long>> fakeOres;
    
    // Map to track which player a fake ore was generated for
    private final Map<BlockPos, UUID> fakeOreOwners;
    
    // List of ore blocks that can be used as fake ores
    private final List<Block> oreBlocks;
    
    public FakeOreManager() {
        this.fakeOres = new HashMap<>();
        this.fakeOreOwners = new ConcurrentHashMap<>();
        this.oreBlocks = new ArrayList<>();
        
        // Initialize ore blocks
        initializeOreBlocks();
    }
    
    /**
     * Initialize the list of ore blocks that can be used as fake ores
     */
    private void initializeOreBlocks() {
        // Add vanilla ore blocks
        oreBlocks.add(Blocks.COAL_ORE);
        oreBlocks.add(Blocks.IRON_ORE);
        oreBlocks.add(Blocks.GOLD_ORE);
        oreBlocks.add(Blocks.DIAMOND_ORE);
        oreBlocks.add(Blocks.EMERALD_ORE);
        oreBlocks.add(Blocks.LAPIS_ORE);
        oreBlocks.add(Blocks.REDSTONE_ORE);
        oreBlocks.add(Blocks.COPPER_ORE);
        
        // Add deepslate variants
        oreBlocks.add(Blocks.DEEPSLATE_COAL_ORE);
        oreBlocks.add(Blocks.DEEPSLATE_IRON_ORE);
        oreBlocks.add(Blocks.DEEPSLATE_GOLD_ORE);
        oreBlocks.add(Blocks.DEEPSLATE_DIAMOND_ORE);
        oreBlocks.add(Blocks.DEEPSLATE_EMERALD_ORE);
        oreBlocks.add(Blocks.DEEPSLATE_LAPIS_ORE);
        oreBlocks.add(Blocks.DEEPSLATE_REDSTONE_ORE);
        oreBlocks.add(Blocks.DEEPSLATE_COPPER_ORE);
        
        // Add nether ores
        oreBlocks.add(Blocks.NETHER_GOLD_ORE);
        oreBlocks.add(Blocks.NETHER_QUARTZ_ORE);
        oreBlocks.add(Blocks.ANCIENT_DEBRIS);
    }
    
    /**
     * Generate fake ores around a player
     * @param player The UUID of the player
     * @param world The world to generate fake ores in
     * @param centerPos The center position to generate fake ores around
     * @return The number of fake ores generated
     */
    public int generateFakeOres(UUID player, ServerWorld world, BlockPos centerPos) {
        ConfigManager.ConfigData config = NekoAntiXray.getInstance().getConfigManager().getConfig();
        Random random = world.getRandom();
        int generatedCount = 0;
        
        // Check if this chunk is ready for fake ore generation
        ChunkPos chunkPos = new ChunkPos(centerPos);
        if (!NekoAntiXray.getInstance().getDetectionManager().canGenerateFakeOresInChunk(chunkPos)) {
            return 0;
        }
        
        // Mark this chunk as having fake ores generated
        NekoAntiXray.getInstance().getDetectionManager().markChunkFakeOreGeneration(chunkPos);
        
        // Initialize the world's fake ore map if it doesn't exist
        fakeOres.computeIfAbsent(world, w -> new ConcurrentHashMap<>());
        
        // Calculate expiration time
        long expirationTime = System.currentTimeMillis() + (config.fakeOreLifetime * 1000L);
        
        // Generate fake ores
        int oreCount = config.fakeOreCount;
        for (int i = 0; i < oreCount; i++) {
            // Calculate random position offset
            int xOffset = random.nextBetween(config.xChangeMin, config.xChangeMax);
            int yOffset = random.nextBetween(config.yChangeMin, config.yChangeMax);
            int zOffset = random.nextBetween(config.zChangeMin, config.zChangeMax);
            
            BlockPos orePos = centerPos.add(xOffset, yOffset, zOffset);
            
            // Check if position is valid (not too close to player or other fake ores)
            if (!isValidFakeOrePosition(world, centerPos, orePos, config.fakeOrePlayerDistanceLimit, config.fakeOreDistanceLimit)) {
                continue;
            }
            
            // Get the block at this position
            BlockState currentState = world.getBlockState(orePos);
            
            // Only replace stone or deepslate
            if (currentState.isOf(Blocks.STONE) || currentState.isOf(Blocks.DEEPSLATE)) {
                // Choose a random ore type
                Block oreType = getRandomOreBlock(random, orePos.getY() < 0);
                
                // Place the fake ore
                world.setBlockState(orePos, oreType.getDefaultState());
                
                // Track the fake ore
                fakeOres.get(world).put(orePos, expirationTime);
                fakeOreOwners.put(orePos, player);
                
                generatedCount++;
                
                // Generate a small vein of the same ore type
                int veinSize = random.nextBetween(config.amountMin, config.amountMax);
                generateOreVein(world, orePos, oreType, veinSize, expirationTime, player);
            }
        }
        
        NekoAntiXray.LOGGER.info("Generated {} fake ores for player {}", generatedCount, player);
        return generatedCount;
    }
    
    /**
     * Generate a small vein of ore blocks around a center position
     */
    private void generateOreVein(ServerWorld world, BlockPos centerPos, Block oreType, int veinSize, long expirationTime, UUID player) {
        Random random = world.getRandom();
        List<BlockPos> positions = new ArrayList<>();
        positions.add(centerPos);
        
        for (int i = 0; i < veinSize - 1; i++) {
            if (positions.isEmpty()) break;
            
            // Pick a random position from the list
            BlockPos pos = positions.remove(random.nextInt(positions.size()));
            
            // Try to add adjacent blocks
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        // Skip the center block and diagonal blocks
                        if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > 1) continue;
                        
                        BlockPos newPos = pos.add(dx, dy, dz);
                        BlockState state = world.getBlockState(newPos);
                        
                        // Only replace stone or deepslate
                        if ((state.isOf(Blocks.STONE) || state.isOf(Blocks.DEEPSLATE)) && 
                                !fakeOres.getOrDefault(world, Collections.emptyMap()).containsKey(newPos)) {
                            // Place the fake ore
                            world.setBlockState(newPos, oreType.getDefaultState());
                            
                            // Track the fake ore
                            fakeOres.get(world).put(newPos, expirationTime);
                            fakeOreOwners.put(newPos, player);
                            
                            // Add to positions list for next iteration
                            positions.add(newPos);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Check if a position is valid for fake ore generation
     */
    private boolean isValidFakeOrePosition(ServerWorld world, BlockPos playerPos, BlockPos orePos, int playerDistanceLimit, int oreDistanceLimit) {
        // Check distance from player
        if (playerPos.getSquaredDistance(orePos) < (playerDistanceLimit * playerDistanceLimit)) {
            return false;
        }
        
        // Check distance from other fake ores
        for (BlockPos existingOrePos : fakeOres.getOrDefault(world, Collections.emptyMap()).keySet()) {
            if (existingOrePos.getSquaredDistance(orePos) < (oreDistanceLimit * oreDistanceLimit)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Get a random ore block
     * @param random The random instance to use
     * @param isDeepslate Whether to use deepslate variants
     * @return A random ore block
     */
    private Block getRandomOreBlock(Random random, boolean isDeepslate) {
        // Filter ore blocks based on deepslate preference
        List<Block> validOres = new ArrayList<>();
        for (Block ore : oreBlocks) {
            String oreName = Registries.BLOCK.getId(ore).toString();
            if (isDeepslate && oreName.contains("deepslate")) {
                validOres.add(ore);
            } else if (!isDeepslate && !oreName.contains("deepslate") && !oreName.contains("nether")) {
                validOres.add(ore);
            }
        }
        
        // If no valid ores found, use all ores
        if (validOres.isEmpty()) {
            validOres.addAll(oreBlocks);
        }
        
        // Return a random ore
        return validOres.get(random.nextInt(validOres.size()));
    }
    
    /**
     * Check if a block is a fake ore
     * @param world The world to check in
     * @param pos The position to check
     * @return true if the block is a fake ore
     */
    public boolean isFakeOre(ServerWorld world, BlockPos pos) {
        return fakeOres.getOrDefault(world, Collections.emptyMap()).containsKey(pos);
    }
    
    /**
     * Check if a fake ore was generated for a specific player
     * @param playerUUID The UUID of the player
     * @param world The world to check in
     * @param pos The position to check
     * @return true if the fake ore was generated for the player
     */
    public boolean isPlayerFakeOre(UUID playerUUID, ServerWorld world, BlockPos pos) {
        if (!isFakeOre(world, pos)) {
            return false;
        }
        
        UUID owner = fakeOreOwners.get(pos);
        return owner != null && owner.equals(playerUUID);
    }
    
    /**
     * Remove a fake ore
     * @param world The world to remove from
     * @param pos The position to remove
     */
    public void removeFakeOre(ServerWorld world, BlockPos pos) {
        if (isFakeOre(world, pos)) {
            fakeOres.get(world).remove(pos);
            fakeOreOwners.remove(pos);
        }
    }
    
    /**
     * Clean up expired fake ores
     */
    public void cleanupExpiredOres() {
        long currentTime = System.currentTimeMillis();
        
        for (Map.Entry<ServerWorld, Map<BlockPos, Long>> worldEntry : fakeOres.entrySet()) {
            ServerWorld world = worldEntry.getKey();
            Map<BlockPos, Long> worldOres = worldEntry.getValue();
            
            // Find expired ores
            List<BlockPos> expiredOres = new ArrayList<>();
            for (Map.Entry<BlockPos, Long> entry : worldOres.entrySet()) {
                if (entry.getValue() < currentTime) {
                    expiredOres.add(entry.getKey());
                }
            }
            
            // Remove expired ores
            for (BlockPos pos : expiredOres) {
                // Restore the original block (stone or deepslate based on Y level)
                Block originalBlock = pos.getY() < 0 ? Blocks.DEEPSLATE : Blocks.STONE;
                world.setBlockState(pos, originalBlock.getDefaultState());
                
                // Remove from tracking
                worldOres.remove(pos);
                fakeOreOwners.remove(pos);
            }
            
            if (!expiredOres.isEmpty()) {
                NekoAntiXray.LOGGER.info("Cleaned up {} expired fake ores in world {}", 
                        expiredOres.size(), world.getRegistryKey().getValue());
            }
        }
    }
}
