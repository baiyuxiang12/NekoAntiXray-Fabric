package sakuyaayane.nekoantixray.detection;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sakuyaayane.nekoantixray.NekoAntiXray;
import sakuyaayane.nekoantixray.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 检测管理器 - Fabric版本
 */
public class DetectionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("nekoantixray");
    private final NekoAntiXray mod;
    private final Map<UUID, Integer> violations = new HashMap<>();
    private final Map<UUID, Long> lastViolationTime = new HashMap<>();

    public DetectionManager(NekoAntiXray mod) {
        this.mod = mod;
    }

    /**
     * 处理玩家挖掘假矿石
     * @param player 玩家
     * @param pos 方块位置
     */
    public void handleFakeOreMined(ServerPlayerEntity player, BlockPos pos) {
        if (!mod.getConfigManager().getConfig().enabled) {
            return;
        }

        UUID uuid = player.getUuid();
        int currentViolation = violations.getOrDefault(uuid, 0) + 1;
        violations.put(uuid, currentViolation);
        lastViolationTime.put(uuid, System.currentTimeMillis());

        LOGGER.info("玩家 {} 挖掘了假矿石，当前违规值: {}", player.getName().getString(), currentViolation);

        // 检查是否达到最大违规值
        if (currentViolation >= mod.getConfigManager().getConfig().maxViolation) {
            // 执行封禁
            String reason = mod.getConfigManager().getConfig().banReason;
            player.networkHandler.disconnect(net.minecraft.text.Text.of(reason));
            LOGGER.info("玩家 {} 因违规值达到上限被封禁", player.getName().getString());
        }
    }

    /**
     * 减少违规值
     */
    public void decayViolations() {
        if (!mod.getConfigManager().getConfig().enabled) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        int decayInterval = mod.getConfigManager().getConfig().violationDecayInterval * 1000;
        int decayAmount = mod.getConfigManager().getConfig().violationDecay;

        for (UUID uuid : new HashMap<>(violations).keySet()) {
            Long lastTime = lastViolationTime.get(uuid);
            if (lastTime == null) {
                continue;
            }

            if (currentTime - lastTime > decayInterval) {
                int currentViolation = violations.get(uuid);
                int newViolation = Math.max(0, currentViolation - decayAmount);
                
                if (newViolation <= 0) {
                    violations.remove(uuid);
                    lastViolationTime.remove(uuid);
                } else {
                    violations.put(uuid, newViolation);
                    lastViolationTime.put(uuid, currentTime);
                }
            }
        }
    }

    /**
     * 重置玩家违规记录
     * @param uuid 玩家UUID
     */
    public void resetViolation(UUID uuid) {
        violations.remove(uuid);
        lastViolationTime.remove(uuid);
        LOGGER.info("已重置玩家 {} 的违规记录", uuid);
    }

    /**
     * 获取玩家违规值
     * @param uuid 玩家UUID
     * @return 违规值
     */
    public int getViolation(UUID uuid) {
        return violations.getOrDefault(uuid, 0);
    }
}
