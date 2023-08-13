package baritone.api.cache;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;

/**
 * @author Brady
 * @since 8/4/2018
 */
public interface IBlockTypeAccess {

    IBlockState getBlock(int x, int y, int z);

    default IBlockState getBlock(BlockPos pos) {
        return getBlock(pos.getX(), pos.getY(), pos.getZ());
    }
}
