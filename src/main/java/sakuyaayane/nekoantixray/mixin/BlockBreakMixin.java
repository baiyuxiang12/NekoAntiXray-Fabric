package sakuyaayane.nekoantixray.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sakuyaayane.nekoantixray.NekoAntiXray;

@Mixin(PlayerEntity.class)
public class BlockBreakMixin {

    @Inject(method = "breakBlock", at = @At("HEAD"))
    private void onBlockBreak(BlockPos pos, CallbackInfo ci) {
        // Cast this to PlayerEntity since we're in a mixin
        PlayerEntity player = (PlayerEntity)(Object)this;
        
        // Only process on server side and for server players
        if (player.getWorld().isClient || !(player instanceof ServerPlayerEntity)) {
            return;
        }
        
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
        ServerWorld world = (ServerWorld)player.getWorld();
        BlockState state = world.getBlockState(pos);
        
        // Notify the detection manager about the block break
        NekoAntiXray.getInstance().getDetectionManager().onBlockBreak(serverPlayer, world, pos, state);
    }
}
