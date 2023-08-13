package baritone.launch.mixins;

import baritone.api.utils.ChunkPos;
import baritone.utils.accessor.IChunkProviderClient;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChunkProviderClient.class)
public class MixinChunkProviderClient implements IChunkProviderClient {

    private final Long2ObjectMap<Chunk> map = new Long2ObjectOpenHashMap<>(8192);

    @Inject(
            method = "loadChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/Chunk;setChunkLoaded(Z)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void loadChunk(int chunkX, int chunkZ, CallbackInfoReturnable<Chunk> cir, Chunk chunk) {
        map.put(ChunkPos.asLong(chunkX, chunkZ), chunk);
    }

    @Inject(
            method = "unloadChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/Chunk;isEmpty()Z"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void remove(int chunkX, int chunkZ, CallbackInfo ci, Chunk chunk) {
        map.remove(ChunkPos.asLong(chunkX, chunkZ));
    }

    @Override
    public Long2ObjectMap<Chunk> loadedChunks() {
        return map;
    }
}
