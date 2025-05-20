package sakuyaayane.nekoantixray.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sakuyaayane.nekoantixray.NekoAntiXray;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 假矿石管理器 - Fabric版本
 * 增强版：添加了控制台通知功能
 */
public class FakeOreManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("nekoantixray");
    private final NekoAntiXray mod;
    private final Map<ChunkPos, Set<BlockPos>> fakeOres = new ConcurrentHashMap<>();
    private final Map<UUID, Long> playerCooldowns = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public FakeOreManager(NekoAntiXray mod) {
        this.mod = mod;
    }

    /**
     * 为玩家生成假矿石
     * @param player 玩家
     * @param world 世界
     */
    public void generateFakeOresForPlayer(ServerPlayerEntity player, World world) {
        if (!mod.getConfigManager().getConfig().fakeOreEnabled) {
            return;
        }

        UUID uuid = player.getUuid();
        long currentTime = System.currentTimeMillis();
        
        // 检查冷却时间
        if (playerCooldowns.containsKey(uuid)) {
            long cooldownTime = playerCooldowns.get(uuid);
            int cooldownSeconds = mod.getConfigManager().getConfig().fakeOreCooldown;
            
            if (currentTime - cooldownTime < cooldownSeconds * 1000) {
                return;
            }
        }
        
        // 更新冷却时间
        playerCooldowns.put(uuid, currentTime);
        
        // 获取玩家所在区块
        ChunkPos chunkPos = player.getChunkPos();
        
        // 获取假矿石生成距离和数量
        int distance = mod.getConfigManager().getConfig().fakeOreDistance;
        int amount = mod.getConfigManager().getConfig().fakeOreAmount;
        
        // 生成假矿石
        for (int i = 0; i < amount; i++) {
            // 随机选择一个区块
            int offsetX = random.nextInt(distance * 2) - distance;
            int offsetZ = random.nextInt(distance * 2) - distance;
            ChunkPos targetChunkPos = new ChunkPos(chunkPos.x + offsetX, chunkPos.z + offsetZ);
            
            // 随机选择一个位置
            int x = targetChunkPos.getStartX() + random.nextInt(16);
            int y = 5 + random.nextInt(60); // 主要在地下生成
            int z = targetChunkPos.getStartZ() + random.nextInt(16);
            BlockPos pos = new BlockPos(x, y, z);
            
            // 随机选择一种矿石类型
            String oreType = getRandomOreType();
            Block oreBlock = Registries.BLOCK.get(new Identifier(oreType));
            
            // 记录假矿石位置
            fakeOres.computeIfAbsent(targetChunkPos, k -> new HashSet<>()).add(pos);
            
            if (mod.getConfigManager().getConfig().debug) {
                LOGGER.info("为玩家 {} 在 ({}, {}, {}) 生成了假 {}", 
                    player.getName().getString(), x, y, z, oreType);
            }
        }
        
        // 添加控制台通知 - 生成假矿石时通知
        LOGGER.info("[NekoAntiXray] 为玩家 {} 生成了 {} 个假矿石", player.getName().getString(), amount);
    }

    /**
     * 随机选择一种矿石类型
     * @return 矿石类型
     */
    private String getRandomOreType() {
        List<String> oreTypes = mod.getConfigManager().getConfig().fakeOreTypes;
        return oreTypes.get(random.nextInt(oreTypes.size()));
    }

    /**
     * 检查方块是否为假矿石
     * @param pos 方块位置
     * @return 是否为假矿石
     */
    public boolean isFakeOre(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<BlockPos> ores = fakeOres.get(chunkPos);
        return ores != null && ores.contains(pos);
    }
    
    /**
     * 检查方块是否为假矿石（带控制台通知功能）
     * @param pos 方块位置
     * @param player 挖掘的玩家
     * @return 是否为假矿石
     */
    public boolean isFakeOre(BlockPos pos, ServerPlayerEntity player) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<BlockPos> ores = fakeOres.get(chunkPos);
        boolean isFake = ores != null && ores.contains(pos);
        
        // 如果是假矿石，输出控制台消息
        if (isFake && player != null) {
            // 获取玩家当前违规值（如果可用）
            double vl = 0.0;
            try {
                vl = mod.getPlayerManager().getPlayerData(player.getUuid()).getViolationLevel();
            } catch (Exception e) {
                // 忽略错误，使用默认值0.0
            }
            
            // 输出详细的控制台通知
            String playerName = player.getName().getString();
            String playerUUID = player.getUuidAsString();
            String position = String.format("(%.1f, %.1f, %.1f)", player.getX(), player.getY(), player.getZ());
            String worldName = player.getWorld().getRegistryKey().getValue().toString();
            
            LOGGER.warn("[NekoAntiXray] 检测到玩家 {} ({}) 挖掘假矿石! 位置: {} | 世界: {} | 违规值: {}", 
                playerName, playerUUID, position, worldName, String.format("%.2f", vl));
        }
        
        return isFake;
    }

    /**
     * 移除假矿石
     * @param pos 方块位置
     */
    public void removeFakeOre(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<BlockPos> ores = fakeOres.get(chunkPos);
        if (ores != null) {
            ores.remove(pos);
            if (ores.isEmpty()) {
                fakeOres.remove(chunkPos);
            }
        }
    }

    /**
     * 清理区块中的假矿石
     * @param chunkPos 区块位置
     */
    public void clearChunk(ChunkPos chunkPos) {
        fakeOres.remove(chunkPos);
    }
    
    /**
     * 记录玩家违规值增加（带控制台通知）
     * @param player 玩家
     * @param violationAdded 增加的违规值
     * @param totalViolation 总违规值
     * @param reason 原因
     */
    public void logViolation(ServerPlayerEntity player, double violationAdded, double totalViolation, String reason) {
        if (player == null) return;
        
        String playerName = player.getName().getString();
        String playerUUID = player.getUuidAsString();
        String position = String.format("(%.1f, %.1f, %.1f)", player.getX(), player.getY(), player.getZ());
        String worldName = player.getWorld().getRegistryKey().getValue().toString();
        
        LOGGER.warn("[NekoAntiXray] 玩家 {} 违规值增加 +{} (总计: {}) | 原因: {} | 位置: {} | 世界: {}", 
            playerName, String.format("%.2f", violationAdded), String.format("%.2f", totalViolation), 
            reason, position, worldName);
    }
    
    /**
     * 记录玩家达到违规阈值（带控制台通知）
     * @param player 玩家
     * @param violationLevel 违规值
     * @param threshold 阈值
     * @param action 执行的操作
     */
    public void logThresholdReached(ServerPlayerEntity player, double violationLevel, double threshold, String action) {
        if (player == null) return;
        
        String playerName = player.getName().getString();
        String playerUUID = player.getUuidAsString();
        String position = String.format("(%.1f, %.1f, %.1f)", player.getX(), player.getY(), player.getZ());
        String worldName = player.getWorld().getRegistryKey().getValue().toString();
        
        LOGGER.error("[NekoAntiXray] 玩家 {} 违规值 {} 达到阈值 {} | 执行操作: {} | 位置: {} | 世界: {}", 
            playerName, String.format("%.2f", violationLevel), String.format("%.2f", threshold), 
            action, position, worldName);
    }
}
