package sakuyaayane.nekoantixray.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sakuyaayane.nekoantixray.NekoAntiXray;
import sakuyaayane.nekoantixray.config.ConfigManager;

@Mixin(ServerWorld.class)
public class WorldSeedMixin {

    @Inject(method = "getSeed", at = @At("HEAD"), cancellable = true)
    private void onGetSeed(CallbackInfoReturnable<Long> cir) {
        // Check if fake seed is enabled
        ConfigManager.ConfigData config = NekoAntiXray.getInstance().getConfigManager().getConfig();
        if (!config.enableFakeSeed || !config.enableAntiSeedCracker) {
            return;
        }
        
        // Get the real seed
        ServerWorld world = (ServerWorld)(Object)this;
        long realSeed = world.getSeed();
        
        // Generate a fake seed
        long fakeSeed = realSeed ^ 0x5DEECE66DL + 11L;
        
        // Return the fake seed
        cir.setReturnValue(fakeSeed);
    }
}
