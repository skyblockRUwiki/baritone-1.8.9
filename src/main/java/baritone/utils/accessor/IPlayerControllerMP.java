package baritone.utils.accessor;

import net.minecraft.util.BlockPos;

public interface IPlayerControllerMP {

    void setIsHittingBlock(boolean isHittingBlock);

    BlockPos getCurrentBlock();

    void callSyncCurrentPlayItem();
}
