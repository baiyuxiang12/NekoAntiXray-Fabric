package sakuyaayane.nekoantixray.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sakuyaayane.nekoantixray.NekoAntiXray;
import sakuyaayane.nekoantixray.config.ConfigManager;

/**
 * 世界种子Mixin - 处理假种子功能
 */
@Mixin(MinecraftServer.class)
public class WorldSeedMixin {
    
    /**
     * 注入到getSeed方法，实现假种子功能
     */
    @Inject(method = "getSeed", at = @At("HEAD"), cancellable = true)
    private void onGetSeed(CallbackInfoReturnable<Long> cir) {
        ConfigManager configManager = NekoAntiXray.getInstance().getConfigManager();
        
        // 检查是否启用假种子
        if (configManager.getConfig().fakeSeedEnabled) {
            // 返回假种子
            cir.setReturnValue(configManager.getConfig().fakeSeed);
        }
    }
}
