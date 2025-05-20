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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 方块破坏Mixin - 监听玩家挖掘方块事件
 * 增强版：添加了控制台通知功能
 */
@Mixin(ServerPlayerInteractionManager.class)
public class BlockBreakMixin {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("nekoantixray");
    
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
                NekoAntiXray.getInstance().getDetectionManager().handleFakeOreMined(player, pos);
                
                // 移除假矿石记录
                NekoAntiXray.getInstance().getFakeOreManager().removeFakeOre(pos);
            }
        }
    }
}
