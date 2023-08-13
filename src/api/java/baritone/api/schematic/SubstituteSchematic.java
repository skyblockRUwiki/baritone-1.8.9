package baritone.api.schematic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubstituteSchematic extends AbstractSchematic {

    private final ISchematic schematic;
    private final Map<Block, List<Block>> substitutions;
    private final Map<IBlockState, Map<Block, IBlockState>> blockStateCache = new HashMap<>();

    public SubstituteSchematic(ISchematic schematic, Map<Block, List<Block>> substitutions) {
        super(schematic.widthX(), schematic.heightY(), schematic.lengthZ());
        this.schematic = schematic;
        this.substitutions = substitutions;
    }

    @Override
    public boolean inSchematic(int x, int y, int z, IBlockState currentState) {
        return schematic.inSchematic(x, y, z, currentState);
    }

    @Override
    public IBlockState desiredState(int x, int y, int z, IBlockState current, List<IBlockState> approxPlaceable) {
        IBlockState desired = schematic.desiredState(x, y, z, current, approxPlaceable);
        Block desiredBlock = desired.getBlock();
        if (!substitutions.containsKey(desiredBlock)) {
            return desired;
        }
        List<Block> substitutes = substitutions.get(desiredBlock);
        if (substitutes.contains(current.getBlock()) && !(current.getBlock() instanceof BlockAir)) {// don't preserve air, it's almost always there and almost never wanted
            return withBlock(desired, current.getBlock());
        }
        for (Block substitute : substitutes) {
            if (substitute instanceof BlockAir) {
                return current.getBlock() instanceof BlockAir ? current : Blocks.air.getDefaultState(); // can always "place" air
            }
            for (IBlockState placeable : approxPlaceable) {
                if (substitute.equals(placeable.getBlock())) {
                    return withBlock(desired, placeable.getBlock());
                }
            }
        }
        return substitutes.get(0).getDefaultState();
    }

    private IBlockState withBlock(IBlockState state, Block block) {
        if (blockStateCache.containsKey(state) && blockStateCache.get(state).containsKey(block)) {
            return blockStateCache.get(state).get(block);
        }
        Collection<IProperty> properties = state.getPropertyNames();
        IBlockState newState = block.getDefaultState();
        for (IProperty<?> property : properties) {
            try {
                newState = copySingleProp(state, newState, property);
            } catch (IllegalArgumentException e) { //property does not exist for target block
            }
        }
        blockStateCache.computeIfAbsent(state, s -> new HashMap<Block, IBlockState>()).put(block, newState);
        return newState;
    }

    private <T extends Comparable<T>> IBlockState copySingleProp(IBlockState fromState, IBlockState toState, IProperty<T> prop) {
        return toState.withProperty(prop, fromState.getValue(prop));
    }
}
