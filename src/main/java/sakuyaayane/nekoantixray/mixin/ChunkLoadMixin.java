package sakuyaayane.nekoantixray.mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sakuyaayane.nekoantixray.NekoAntiXray;
import sakuyaayane.nekoantixray.config.ConfigManager;

import java.util.Random;

@Mixin(WorldChunk.class)
public class ChunkLoadMixin {

    @Inject(method = "setLoadedToWorld", at = @At("RETURN"))
    private void onChunkLoad(CallbackInfo ci) {
        WorldChunk chunk = (WorldChunk)(Object)this;
        
        // Only process on server side
        if (!(chunk.getWorld() instanceof ServerWorld)) {
            return;
        }
        
        ServerWorld world = (ServerWorld)chunk.getWorld();
        ChunkPos chunkPos = chunk.getPos();
        
        // Check if xray detection is enabled
        ConfigManager.ConfigData config = NekoAntiXray.getInstance().getConfigManager().getConfig();
        if (!config.enableXrayDetection) {
            return;
        }
        
        // Check if this chunk is ready for fake ore generation
        if (!NekoAntiXray.getInstance().getDetectionManager().canGenerateFakeOresInChunk(chunkPos)) {
            return;
        }
        
        // Check if world is in blacklist
        String worldName = world.getRegistryKey().getValue().toString();
        if (config.blacklistWorlds.contains(worldName)) {
            return;
        }
        
        // Random chance to generate fake ores (to avoid generating in every chunk)
        Random random = new Random();
        if (random.nextFloat() < 0.3f) { // 30% chance
            // Get a random position in the chunk for fake ore generation
            int x = chunkPos.getStartX() + random.nextInt(16);
            int z = chunkPos.getStartZ() + random.nextInt(16);
            int y = 30 + random.nextInt(40); // Between y=30 and y=70
            
            BlockPos centerPos = new BlockPos(x, y, z);
            
            // Generate fake ores for a "virtual" player
            // In a real implementation, we would track nearby players and generate for them
            NekoAntiXray.getInstance().getFakeOreManager().generateFakeOres(
                    new java.util.UUID(0, 0), // Placeholder UUID
                    world,
                    centerPos
            );
        }
    }
}
