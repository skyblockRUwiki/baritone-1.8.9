package baritone.pathing.precompute;

import baritone.pathing.movement.MovementHelper;
import baritone.utils.BlockStateInterface;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import java.util.List;

import static baritone.pathing.precompute.Ternary.MAYBE;
import static baritone.pathing.precompute.Ternary.YES;

public class PrecomputedData {

    private final int[] data;

    private final List<IBlockState> states;

    {
        states = Lists.newArrayList(Block.BLOCK_STATE_IDS);
        data = new int[states.size()];
    }

    private static final int COMPLETED_MASK = 1 << 0;
    private static final int CAN_WALK_ON_MASK = 1 << 1;
    private static final int CAN_WALK_ON_SPECIAL_MASK = 1 << 2;
    private static final int CAN_WALK_THROUGH_MASK = 1 << 3;
    private static final int CAN_WALK_THROUGH_SPECIAL_MASK = 1 << 4;
    private static final int FULLY_PASSABLE_MASK = 1 << 5;
    private static final int FULLY_PASSABLE_SPECIAL_MASK = 1 << 6;

    private int fillData(int id, IBlockState state) {
        int blockData = 0;

        Ternary canWalkOnState = MovementHelper.canWalkOnBlockState(state);
        if (canWalkOnState == YES) {
            blockData |= CAN_WALK_ON_MASK;
        }
        if (canWalkOnState == MAYBE) {
            blockData |= CAN_WALK_ON_SPECIAL_MASK;
        }

        Ternary canWalkThroughState = MovementHelper.canWalkThroughBlockState(state);
        if (canWalkThroughState == YES) {
            blockData |= CAN_WALK_THROUGH_MASK;
        }
        if (canWalkThroughState == MAYBE) {
            blockData |= CAN_WALK_THROUGH_SPECIAL_MASK;
        }

        Ternary fullyPassableState = MovementHelper.fullyPassableBlockState(state);
        if (fullyPassableState == YES) {
            blockData |= FULLY_PASSABLE_MASK;
        }
        if (fullyPassableState == MAYBE) {
            blockData |= FULLY_PASSABLE_SPECIAL_MASK;
        }

        blockData |= COMPLETED_MASK;

        data[id] = blockData; // in theory, this is thread "safe" because every thread should compute the exact same int to write?
        return blockData;
    }

    public boolean canWalkOn(BlockStateInterface bsi, int x, int y, int z, IBlockState state) {
        int id = states.indexOf(state);
        int blockData = data[id];

        if ((blockData & COMPLETED_MASK) == 0) { // we need to fill in the data
            blockData = fillData(id, state);
        }

        if ((blockData & CAN_WALK_ON_SPECIAL_MASK) != 0) {
            return MovementHelper.canWalkOnPosition(bsi, x, y, z, state);
        } else {
            return (blockData & CAN_WALK_ON_MASK) != 0;
        }
    }

    public boolean canWalkThrough(BlockStateInterface bsi, int x, int y, int z, IBlockState state) {
        int id = states.indexOf(state);
        int blockData = data[id];

        if ((blockData & COMPLETED_MASK) == 0) { // we need to fill in the data
            blockData = fillData(id, state);
        }

        if ((blockData & CAN_WALK_THROUGH_SPECIAL_MASK) != 0) {
            return MovementHelper.canWalkThroughPosition(bsi, x, y, z, state);
        } else {
            return (blockData & CAN_WALK_THROUGH_MASK) != 0;
        }
    }

    public boolean fullyPassable(BlockStateInterface bsi, int x, int y, int z, IBlockState state) {
        int id = states.indexOf(state);
        int blockData = data[id];

        if ((blockData & COMPLETED_MASK) == 0) { // we need to fill in the data
            blockData = fillData(id, state);
        }

        if ((blockData & FULLY_PASSABLE_SPECIAL_MASK) != 0) {
            return MovementHelper.fullyPassablePosition(bsi, x, y, z, state);
        } else {
            return (blockData & FULLY_PASSABLE_MASK) != 0;
        }
    }
}