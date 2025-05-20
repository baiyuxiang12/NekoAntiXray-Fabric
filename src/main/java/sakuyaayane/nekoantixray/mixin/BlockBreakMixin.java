package sakuyaayane.nekoantixray.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sakuyaayane.nekoantixray.NekoAntiXray;

/**
 * 方块破坏Mixin - 监听玩家挖掘方块事件
 * 增强版：添加了控制台通知功能
 */
@Mixin(ServerPlayerInteractionManager.class)
public class BlockBreakMixin {
    
    @Shadow
    public ServerPlayerEntity player;
    
    /**
     * 注入到tryBreakBlock方法，监听玩家挖掘方块
     */
    @Inject(method = "tryBreakBlock", at = @At("HEAD"))
    private void onBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (player != null && player.getWorld() != null) {
            World world = player.getWorld();
            
            // 检查是否为假矿石（使用带玩家参数的重载方法，自动输出控制台消息）
            if (NekoAntiXray.getInstance().getFakeOreManager().isFakeOre(pos, player)) {
                // 处理玩家挖掘假矿石
                double violationAdded = NekoAntiXray.getInstance().getDetectionManager().handleFakeOreMined(player, pos);
                
                // 获取玩家当前总违规值
                double totalViolation = 0.0;
                try {
                    totalViolation = NekoAntiXray.getInstance().getPlayerManager().getPlayerData(player.getUuid()).getViolationLevel();
                } catch (Exception e) {
                    // 忽略错误
                }
                
                // 记录违规值增加（输出控制台消息）
                NekoAntiXray.getInstance().getFakeOreManager().logViolation(
                    player, 
                    violationAdded, 
                    totalViolation, 
                    "挖掘假矿石"
                );
                
                // 检查是否达到封禁阈值
                double banThreshold = NekoAntiXray.getInstance().getConfigManager().getConfig().banThreshold;
                if (totalViolation >= banThreshold) {
                    // 记录达到阈值（输出控制台消息）
                    NekoAntiXray.getInstance().getFakeOreManager().logThresholdReached(
                        player,
                        totalViolation,
                        banThreshold,
                        "封禁"
                    );
                }
                
                // 移除假矿石记录
                NekoAntiXray.getInstance().getFakeOreManager().removeFakeOre(pos);
            }
        }
    }
}
