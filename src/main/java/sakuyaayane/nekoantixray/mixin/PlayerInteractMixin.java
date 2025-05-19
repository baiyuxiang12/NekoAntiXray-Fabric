package sakuyaayane.nekoantixray.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sakuyaayane.nekoantixray.NekoAntiXray;

/**
 * 玩家交互Mixin - 监听玩家移动和交互事件
 */
@Mixin(ServerPlayerEntity.class)
public class PlayerInteractMixin {
    
    private int tickCounter = 0;
    
    /**
     * 注入到tick方法，监听玩家tick事件
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void onPlayerTick(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        World world = player.getWorld();
        
        // 每20tick（约1秒）检查一次
        if (tickCounter++ % 20 == 0) {
            // 为玩家生成假矿石
            NekoAntiXray.getInstance().getFakeOreManager().generateFakeOresForPlayer(player, world);
        }
    }
}
