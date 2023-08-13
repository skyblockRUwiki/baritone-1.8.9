package baritone.utils;

import baritone.Baritone;
import baritone.api.utils.IPlayerContext;
import net.minecraft.util.MovingObjectPosition;

public class BlockPlaceHelper {

    private final IPlayerContext ctx;
    private int rightClickTimer;

    BlockPlaceHelper(IPlayerContext playerContext) {
        this.ctx = playerContext;
    }

    public void tick(boolean rightClickRequested) {
        if (rightClickTimer > 0) {
            rightClickTimer--;
            return;
        }
        MovingObjectPosition mouseOver = ctx.objectMouseOver();
        if (!rightClickRequested || ctx.player().isRiding() || mouseOver == null || mouseOver.getBlockPos() == null || mouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return;
        }
        rightClickTimer = Baritone.settings().rightClickSpeed.value;

        if (ctx.playerController().processRightClickBlock(ctx.player(), ctx.world(), mouseOver.getBlockPos(), mouseOver.sideHit, mouseOver.hitVec)) {
            ctx.player().swingItem();
            return;
        }

        if (ctx.player().getHeldItem() == null) {
            return;
        }

        ctx.playerController().processRightClick(ctx.player(), ctx.world());
    }
}
