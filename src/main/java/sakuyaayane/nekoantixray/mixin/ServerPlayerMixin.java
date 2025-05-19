package sakuyaayane.nekoantixray.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sakuyaayane.nekoantixray.NekoAntiXray;
import sakuyaayane.nekoantixray.util.PermissionUtil;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerMixin {

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void onPlayerDisconnect(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        
        // Clear permission cache for the player
        PermissionUtil.clearCache(player.getUuid());
        
        // Any other cleanup needed when a player disconnects
    }
}
