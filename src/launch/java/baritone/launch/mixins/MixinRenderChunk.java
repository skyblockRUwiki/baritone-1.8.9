package baritone.launch.mixins;

import baritone.Baritone;
import baritone.api.BaritoneAPI;
import baritone.api.utils.IPlayerContext;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Brady
 * @since 1/29/2019
 */
@Mixin(RenderChunk.class)
public class MixinRenderChunk {

    @Redirect(
            method = "rebuildChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/IBlockAccess;extendedLevelsInChunkCache()Z"
            )
    )
    private boolean isEmpty(IBlockAccess instance) {
        if (!instance.extendedLevelsInChunkCache()) {
            return false;
        }
        if (Baritone.settings().renderCachedChunks.value && !Minecraft.getMinecraft().isSingleplayer()) {
            Baritone baritone = (Baritone) BaritoneAPI.getProvider().getPrimaryBaritone();
            IPlayerContext ctx = baritone.getPlayerContext();
            if (ctx.player() != null && ctx.world() != null && baritone.bsi != null) {
                BlockPos position = ((RenderChunk) (Object) this).getPosition();
                // RenderChunk extends from -1,-1,-1 to +16,+16,+16
                // then the constructor of ChunkCache extends it one more (presumably to get things like the connected status of fences? idk)
                // so if ANY of the adjacent chunks are loaded, we are unempty
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (baritone.bsi.isLoaded(16 * dx + position.getX(), 16 * dz + position.getZ())) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    @Redirect(
            method = "rebuildChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/IBlockAccess;getBlockState(Lnet/minecraft/util/BlockPos;)Lnet/minecraft/block/state/IBlockState;"
            )
    )
    private IBlockState getBlockState(IBlockAccess instance, BlockPos pos) {
        if (Baritone.settings().renderCachedChunks.value && !Minecraft.getMinecraft().isSingleplayer()) {
            Baritone baritone = (Baritone) BaritoneAPI.getProvider().getPrimaryBaritone();
            IPlayerContext ctx = baritone.getPlayerContext();
            if (ctx.player() != null && ctx.world() != null && baritone.bsi != null) {
                return baritone.bsi.get0(pos);
            }
        }

        return instance.getBlockState(pos);
    }
}

