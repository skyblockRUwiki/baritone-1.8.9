package baritone.api.schematic;

import net.minecraft.block.state.IBlockState;

public class WallsSchematic extends MaskSchematic {

    public WallsSchematic(ISchematic schematic) {
        super(schematic);
    }

    @Override
    protected boolean partOfMask(int x, int y, int z, IBlockState currentState) {
        return x == 0 || z == 0 || x == widthX() - 1 || z == lengthZ() - 1;
    }
}
