package baritone.utils;

import baritone.api.utils.IPlayerContext;
import net.minecraft.util.MovingObjectPosition;

/**
 * @author Brady
 * @since 8/25/2018
 */
public final class BlockBreakHelper {

    private final IPlayerContext ctx;
    private boolean didBreakLastTick;

    BlockBreakHelper(IPlayerContext ctx) {
        this.ctx = ctx;
    }

    public void stopBreakingBlock() {
        // The player controller will never be null, but the player can be
        if (ctx.player() != null && didBreakLastTick) {
            if (!ctx.playerController().hasBrokenBlock()) {
                // insane bypass to check breaking succeeded
                ctx.playerController().setHittingBlock(true);
            }
            ctx.playerController().resetBlockRemoving();
            didBreakLastTick = false;
        }
    }

    public void tick(boolean isLeftClick) {
        MovingObjectPosition trace = ctx.objectMouseOver();
        boolean isBlockTrace = trace != null && trace.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;

        if (isLeftClick && isBlockTrace) {
            if (!didBreakLastTick) {
                ctx.playerController().syncHeldItem();
                ctx.playerController().clickBlock(trace.getBlockPos(), trace.sideHit);
                ctx.player().swingItem();
            }

            // Attempt to break the block
            if (ctx.playerController().onPlayerDamageBlock(trace.getBlockPos(), trace.sideHit)) {
                ctx.player().swingItem();
            }

            ctx.playerController().setHittingBlock(false);

            didBreakLastTick = true;
        } else if (didBreakLastTick) {
            stopBreakingBlock();
            didBreakLastTick = false;
        }
    }
}
