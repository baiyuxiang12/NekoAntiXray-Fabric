package sakuyaayane.nekoantixray.mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sakuyaayane.nekoantixray.NekoAntiXray;

/**
 * 区块加载Mixin - 监听区块加载和卸载事件
 */
@Mixin(ChunkHolder.class)
public class ChunkLoadMixin {
    
    /**
     * 注入到sendPacketToPlayersWatching方法，监听区块加载
     */
    @Inject(method = "sendPacketToPlayersWatching", at = @At("HEAD"))
    private void onChunkLoad(CallbackInfo ci) {
        ChunkHolder holder = (ChunkHolder)(Object)this;
        ChunkPos pos = holder.getPos();
        
        // 区块加载时，可以在这里处理
        // 由于这个方法会被频繁调用，我们不在这里生成假矿石
        // 假矿石生成将在玩家移动时触发
    }
    
    /**
     * 注入到scheduleUnload方法，监听区块卸载
     */
    @Inject(method = "scheduleUnload", at = @At("HEAD"))
    private void onChunkUnload(ChunkPos pos, ServerWorld world, CallbackInfo ci) {
        // 区块卸载时，清理假矿石
        NekoAntiXray.getInstance().getFakeOreManager().clearChunk(pos);
    }
}
