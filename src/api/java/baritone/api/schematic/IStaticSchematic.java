package baritone.api.schematic;

import net.minecraft.block.state.IBlockState;

/**
 * A static schematic is capable of providing the desired state at a given position without
 * additional context. Schematics of this type are expected to have non-varying contents.
 *
 * @author Brady
 * @see #getDirect(int, int, int)
 * @since 12/24/2019
 */
public interface IStaticSchematic extends ISchematic {

    /**
     * Gets the {@link IBlockState} for a given position in this schematic. It should be guaranteed
     * that the return value of this method will not change given that the parameters are the same.
     *
     * @param x The X block position
     * @param y The Y block position
     * @param z The Z block position
     * @return The desired state at the specified position.
     */
    IBlockState getDirect(int x, int y, int z);

    /**
     * Returns an {@link IBlockState} array of size {@link #heightY()} which contains all
     * desired block states in the specified vertical column. The index of {@link IBlockState}s
     * in the array are equivalent to their Y position in the schematic.
     *
     * @param x The X column position
     * @param z The Z column position
     * @return An {@link IBlockState} array
     */
    default IBlockState[] getColumn(int x, int z) {
        IBlockState[] column = new IBlockState[this.heightY()];
        for (int i = 0; i < this.heightY(); i++) {
            column[i] = getDirect(x, i, z);
        }
        return column;
    }
}
