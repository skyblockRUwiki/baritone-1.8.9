package baritone.api.schematic;

import baritone.api.utils.BlockOptionalMeta;
import net.minecraft.block.state.IBlockState;

import java.util.List;

public class FillSchematic extends AbstractSchematic {

    private final BlockOptionalMeta bom;

    public FillSchematic(int x, int y, int z, BlockOptionalMeta bom) {
        super(x, y, z);
        this.bom = bom;
    }

    public FillSchematic(int x, int y, int z, IBlockState state) {
        this(x, y, z, new BlockOptionalMeta(state.getBlock(), state.getBlock().getMetaFromState(state)));
    }

    public BlockOptionalMeta getBom() {
        return bom;
    }

    @Override
    public IBlockState desiredState(int x, int y, int z, IBlockState current, List<IBlockState> approxPlaceable) {
        if (bom.matches(current)) {
            return current;
        }
        for (IBlockState placeable : approxPlaceable) {
            if (bom.matches(placeable)) {
                return placeable;
            }
        }
        return bom.getAnyBlockState();
    }
}
