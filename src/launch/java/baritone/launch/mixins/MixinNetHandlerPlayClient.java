package baritone.launch.mixins;

import baritone.Baritone;
import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.event.events.ChunkEvent;
import baritone.api.event.events.type.EventState;
import baritone.api.utils.ChunkPos;
import baritone.cache.CachedChunk;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S42PacketCombatEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Brady
 * @since 8/3/2018
 */
@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    @Inject(
            method = "handleChunkData",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/Chunk;fillChunk([BIZ)V"
            )
    )
    private void preRead(S21PacketChunkData packetIn, CallbackInfo ci) {
        for (IBaritone ibaritone : BaritoneAPI.getProvider().getAllBaritones()) {
            EntityPlayerSP player = ibaritone.getPlayerContext().player();
            if (player != null && player.sendQueue == (NetHandlerPlayClient) (Object) this) {
                ibaritone.getGameEventHandler().onChunkEvent(
                        new ChunkEvent(
                                EventState.PRE,
                                packetIn.func_149274_i() ? ChunkEvent.Type.POPULATE_FULL : ChunkEvent.Type.POPULATE_PARTIAL,
                                packetIn.getChunkX(),
                                packetIn.getChunkZ()
                        )
                );
            }
        }
    }

    @Inject(
            method = "handleChunkData",
            at = @At("RETURN")
    )
    private void postHandleChunkData(S21PacketChunkData packetIn, CallbackInfo ci) {
        for (IBaritone ibaritone : BaritoneAPI.getProvider().getAllBaritones()) {
            EntityPlayerSP player = ibaritone.getPlayerContext().player();
            if (player != null && player.sendQueue == (NetHandlerPlayClient) (Object) this) {
                ibaritone.getGameEventHandler().onChunkEvent(
                        new ChunkEvent(
                                EventState.POST,
                                packetIn.func_149274_i() ? ChunkEvent.Type.POPULATE_FULL : ChunkEvent.Type.POPULATE_PARTIAL,
                                packetIn.getChunkX(),
                                packetIn.getChunkZ()
                        )
                );
            }
        }
    }

    @Inject(
            method = "handleBlockChange",
            at = @At("RETURN")
    )
    private void postHandleBlockChange(S23PacketBlockChange packetIn, CallbackInfo ci) {
        if (!Baritone.settings().repackOnAnyBlockChange.value) {
            return;
        }
        if (!CachedChunk.BLOCKS_TO_KEEP_TRACK_OF.contains(packetIn.getBlockState().getBlock())) {
            return;
        }
        for (IBaritone ibaritone : BaritoneAPI.getProvider().getAllBaritones()) {
            EntityPlayerSP player = ibaritone.getPlayerContext().player();
            if (player != null && player.sendQueue == (NetHandlerPlayClient) (Object) this) {
                ibaritone.getGameEventHandler().onChunkEvent(
                        new ChunkEvent(
                                EventState.POST,
                                ChunkEvent.Type.POPULATE_FULL,
                                packetIn.getBlockPosition().getX() >> 4,
                                packetIn.getBlockPosition().getZ() >> 4
                        )
                );
            }
        }
    }

    @Inject(
            method = "handleMultiBlockChange",
            at = @At("RETURN")
    )
    private void postHandleMultiBlockChange(S22PacketMultiBlockChange packetIn, CallbackInfo ci) {
        if (!Baritone.settings().repackOnAnyBlockChange.value) {
            return;
        }
        if (packetIn.getChangedBlocks().length == 0) {
            return;
        }
        https://docs.oracle.com/javase/specs/jls/se7/html/jls-14.html#jls-14.15
        {
            for (S22PacketMultiBlockChange.BlockUpdateData update : packetIn.getChangedBlocks()) {
                if (CachedChunk.BLOCKS_TO_KEEP_TRACK_OF.contains(update.getBlockState().getBlock())) {
                    break https;
                }
            }
            return;
        }
        ChunkPos pos = new ChunkPos(packetIn.getChangedBlocks()[0].getPos());
        for (IBaritone ibaritone : BaritoneAPI.getProvider().getAllBaritones()) {
            EntityPlayerSP player = ibaritone.getPlayerContext().player();
            if (player != null && player.sendQueue == (NetHandlerPlayClient) (Object) this) {
                ibaritone.getGameEventHandler().onChunkEvent(
                        new ChunkEvent(
                                EventState.POST,
                                ChunkEvent.Type.POPULATE_FULL,
                                pos.x,
                                pos.z
                        )
                );
            }
        }
    }

    @Inject(
            method = "handleCombatEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntityByID(I)Lnet/minecraft/entity/Entity;",
                    ordinal = 1
            )
    )
    private void onPlayerDeath(S42PacketCombatEvent packetIn, CallbackInfo ci) {
        for (IBaritone ibaritone : BaritoneAPI.getProvider().getAllBaritones()) {
            EntityPlayerSP player = ibaritone.getPlayerContext().player();
            if (player != null && player.sendQueue == (NetHandlerPlayClient) (Object) this) {
                ibaritone.getGameEventHandler().onPlayerDeath();
            }
        }
    }
}
