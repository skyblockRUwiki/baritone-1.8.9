package baritone.api.process;

import baritone.api.schematic.ISchematic;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;

import java.io.File;
import java.util.List;

/**
 * @author Brady
 * @since 1/15/2019
 */
public interface IBuilderProcess extends IBaritoneProcess {

    /**
     * Requests a build for the specified schematic, labeled as specified, with the specified origin.
     *
     * @param name      A user-friendly name for the schematic
     * @param schematic The object representation of the schematic
     * @param origin    The origin position of the schematic being built
     */
    void build(String name, ISchematic schematic, Vec3i origin);

    /**
     * Requests a build for the specified schematic, labeled as specified, with the specified origin.
     *
     * @param name      A user-friendly name for the schematic
     * @param schematic The file path of the schematic
     * @param origin    The origin position of the schematic being built
     * @return Whether or not the schematic was able to load from file
     */
    boolean build(String name, File schematic, Vec3i origin);

    @Deprecated
    default boolean build(String schematicFile, BlockPos origin) {
        File file = new File(new File(Minecraft.getMinecraft().mcDataDir, "schematics"), schematicFile);
        return build(schematicFile, file, origin);
    }

    void buildOpenSchematic();

    void pause();

    boolean isPaused();

    void resume();

    void clearArea(BlockPos corner1, BlockPos corner2);

    /**
     * @return A list of block states that are estimated to be placeable by this builder process. You can use this in
     * schematics, for example, to pick a state that the builder process will be happy with, because any variation will
     * cause it to give up. This is updated every tick, but only while the builder process is active.
     */
    List<IBlockState> getApproxPlaceable();
}
