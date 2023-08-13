package baritone.utils;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;

import javax.annotation.Nullable;

/**
 * @author Brady
 * @since 11/5/2019
 */
@SuppressWarnings("NullableProblems")
public final class BlockStateInterfaceAccessWrapper implements IBlockAccess {

    private final BlockStateInterface bsi;
    private final IBlockAccess world;

    BlockStateInterfaceAccessWrapper(BlockStateInterface bsi, IBlockAccess world) {
        this.bsi = bsi;
        this.world = world;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return 0;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        // BlockStateInterface#get0(BlockPos) btfo!
        return this.bsi.get0(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return this.bsi.get0(pos.getX(), pos.getY(), pos.getZ()).getBlock().getMaterial() == Material.air;
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(BlockPos pos) {
        return BiomeGenBase.forest;
    }

    @Override
    public boolean extendedLevelsInChunkCache() {
        return false;
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return 0;
    }

    @Override
    public WorldType getWorldType() {
        return this.world.getWorldType();
    }
}
