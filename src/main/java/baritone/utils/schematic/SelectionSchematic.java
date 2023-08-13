package baritone.utils.schematic;

import baritone.api.schematic.ISchematic;
import baritone.api.schematic.MaskSchematic;
import baritone.api.selection.ISelection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;

import java.util.stream.Stream;

public class SelectionSchematic extends MaskSchematic {

    private final ISelection[] selections;

    public SelectionSchematic(ISchematic schematic, Vec3i origin, ISelection[] selections) {
        super(schematic);
        this.selections = Stream.of(selections).map(
                        sel -> sel
                                .shift(EnumFacing.WEST, origin.getX())
                                .shift(EnumFacing.DOWN, origin.getY())
                                .shift(EnumFacing.NORTH, origin.getZ()))
                .toArray(ISelection[]::new);
    }

    @Override
    protected boolean partOfMask(int x, int y, int z, IBlockState currentState) {
        for (ISelection selection : selections) {
            if (x >= selection.min().x && y >= selection.min().y && z >= selection.min().z
                    && x <= selection.max().x && y <= selection.max().y && z <= selection.max().z) {
                return true;
            }
        }
        return false;
    }
}
