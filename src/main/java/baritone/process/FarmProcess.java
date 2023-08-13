package baritone.process;

import baritone.Baritone;
import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalGetToBlock;
import baritone.api.pathing.goals.GoalComposite;
import baritone.api.process.IFarmProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import baritone.api.utils.RayTraceUtils;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import baritone.api.utils.input.Input;
import baritone.pathing.movement.MovementHelper;
import baritone.utils.BaritoneProcessHelper;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class FarmProcess extends BaritoneProcessHelper implements IFarmProcess {

    private boolean active;

    private List<BlockPos> locations;
    private int tickCount;

    private int range;
    private BlockPos center;

    private static final List<Item> FARMLAND_PLANTABLE = Arrays.asList(
            Items.melon_seeds,
            Items.wheat_seeds,
            Items.pumpkin_seeds,
            Items.potato,
            Items.carrot
    );

    private static final List<Item> PICKUP_DROPPED = Arrays.asList(
            Items.melon_seeds,
            Items.melon,
            Item.getItemFromBlock(Blocks.melon_block),
            Items.wheat_seeds,
            Items.wheat,
            Items.pumpkin_seeds,
            Item.getItemFromBlock(Blocks.pumpkin),
            Items.potato,
            Items.carrot,
            Items.nether_wart,
            Items.reeds,
            Item.getItemFromBlock(Blocks.cactus)
    );

    public FarmProcess(Baritone baritone) {
        super(baritone);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void farm(int range, BlockPos pos) {
        if (pos == null) {
            center = baritone.getPlayerContext().playerFeet();
        } else {
            center = pos;
        }
        this.range = range;
        active = true;
        locations = null;
    }

    private enum Harvest {
        WHEAT((BlockCrops) Blocks.wheat),
        CARROTS((BlockCrops) Blocks.carrots),
        POTATOES((BlockCrops) Blocks.potatoes),
        PUMPKIN(Blocks.pumpkin, state -> true),
        MELON(Blocks.melon_block, state -> true),
        NETHERWART(Blocks.nether_wart, state -> state.getValue(BlockNetherWart.AGE) >= 3),
        COCOA(Blocks.cocoa, state -> state.getValue(BlockCocoa.AGE) >= 2),
        SUGARCANE(Blocks.reeds, null) {
            @Override
            public boolean readyToHarvest(World world, BlockPos pos, IBlockState state) {
                if (Baritone.settings().replantCrops.value) {
                    return world.getBlockState(pos.down()).getBlock() instanceof BlockReed;
                }
                return true;
            }
        },
        CACTUS(Blocks.cactus, null) {
            @Override
            public boolean readyToHarvest(World world, BlockPos pos, IBlockState state) {
                if (Baritone.settings().replantCrops.value) {
                    return world.getBlockState(pos.down()).getBlock() instanceof BlockCactus;
                }
                return true;
            }
        };
        public final Block block;
        public final Predicate<IBlockState> readyToHarvest;

        Harvest(BlockCrops blockCrops) {
            this(blockCrops, state -> state.getValue(BlockCrops.AGE) >= 7);
            // max age is 7 for wheat, carrots, and potatoes
        }

        Harvest(Block block, Predicate<IBlockState> readyToHarvest) {
            this.block = block;
            this.readyToHarvest = readyToHarvest;
        }

        public boolean readyToHarvest(World world, BlockPos pos, IBlockState state) {
            return readyToHarvest.test(state);
        }
    }

    private boolean readyForHarvest(World world, BlockPos pos, IBlockState state) {
        for (Harvest harvest : Harvest.values()) {
            if (harvest.block == state.getBlock()) {
                return harvest.readyToHarvest(world, pos, state);
            }
        }
        return false;
    }

    private boolean isPlantable(ItemStack stack) {
        return FARMLAND_PLANTABLE.contains(stack.getItem());
    }

    private boolean isBoneMeal(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemDye && EnumDyeColor.byDyeDamage(stack.getMetadata()) == EnumDyeColor.WHITE;
    }

    private boolean isNetherWart(ItemStack stack) {
        return stack != null && stack.getItem().equals(Items.nether_wart);
    }

    private boolean isCocoa(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemDye && EnumDyeColor.byDyeDamage(stack.getMetadata()) == EnumDyeColor.BROWN;
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        ArrayList<Block> scan = new ArrayList<>();
        for (Harvest harvest : Harvest.values()) {
            scan.add(harvest.block);
        }
        if (Baritone.settings().replantCrops.value) {
            scan.add(Blocks.farmland);
            scan.add(Blocks.log);
            if (Baritone.settings().replantNetherWart.value) {
                scan.add(Blocks.soul_sand);
            }
        }

        if (Baritone.settings().mineGoalUpdateInterval.value != 0 && tickCount++ % Baritone.settings().mineGoalUpdateInterval.value == 0) {
            Baritone.getExecutor().execute(() -> locations = BaritoneAPI.getProvider().getWorldScanner().scanChunkRadius(ctx, scan, 256, 10, 10));
        }
        if (locations == null) {
            return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
        }
        List<BlockPos> toBreak = new ArrayList<>();
        List<BlockPos> openFarmland = new ArrayList<>();
        List<BlockPos> bonemealable = new ArrayList<>();
        List<BlockPos> openSoulsand = new ArrayList<>();
        List<BlockPos> openLog = new ArrayList<>();
        for (BlockPos pos : locations) {
            //check if the target block is out of range.
            if (range != 0 && Math.sqrt(pos.distanceSq(center.getX(), center.getY(), center.getZ())) > range) {
                continue;
            }

            IBlockState state = ctx.world().getBlockState(pos);
            boolean airAbove = ctx.world().getBlockState(pos.up()).getBlock() instanceof BlockAir;
            if (state.getBlock() == Blocks.farmland) {
                if (airAbove) {
                    openFarmland.add(pos);
                }
                continue;
            }
            if (state.getBlock() == Blocks.soul_sand) {
                if (airAbove) {
                    openSoulsand.add(pos);
                }
                continue;
            }
            if (state.getBlock() == Blocks.log) {
                // yes, both log blocks and the planks block define separate properties but share the enum
                if (state.getValue(BlockOldLog.VARIANT) != BlockPlanks.EnumType.JUNGLE) {
                    continue;
                }
                for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
                    if (ctx.world().getBlockState(pos.offset(direction)).getBlock() instanceof BlockAir) {
                        openLog.add(pos);
                        break;
                    }
                }
                continue;
            }
            if (readyForHarvest(ctx.world(), pos, state)) {
                toBreak.add(pos);
                continue;
            }
            if (state.getBlock() instanceof IGrowable) {
                IGrowable ig = (IGrowable) state.getBlock();
                if (ig.canGrow(ctx.world(), pos, state, true) && ig.canUseBonemeal(ctx.world(), ctx.world().rand, pos, state)) {
                    bonemealable.add(pos);
                }
            }
        }

        baritone.getInputOverrideHandler().clearAllKeys();
        for (BlockPos pos : toBreak) {
            Optional<Rotation> rot = RotationUtils.reachable(ctx, pos);
            if (rot.isPresent() && isSafeToCancel) {
                baritone.getLookBehavior().updateTarget(rot.get(), true);
                MovementHelper.switchToBestToolFor(ctx, ctx.world().getBlockState(pos));
                if (ctx.isLookingAt(pos)) {
                    baritone.getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, true);
                }
                return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
            }
        }
        ArrayList<BlockPos> both = new ArrayList<>(openFarmland);
        both.addAll(openSoulsand);
        for (BlockPos pos : both) {
            boolean soulsand = openSoulsand.contains(pos);
            Optional<Rotation> rot = RotationUtils.reachableOffset(ctx, pos, new Vec3(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), ctx.playerController().getBlockReachDistance(), false);
            if (rot.isPresent() && isSafeToCancel && baritone.getInventoryBehavior().throwaway(true, soulsand ? this::isNetherWart : this::isPlantable)) {
                MovingObjectPosition result = RayTraceUtils.rayTraceTowards(ctx.player(), rot.get(), ctx.playerController().getBlockReachDistance());
                if (result.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && result.sideHit == EnumFacing.UP) {
                    baritone.getLookBehavior().updateTarget(rot.get(), true);
                    if (ctx.isLookingAt(pos)) {
                        baritone.getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, true);
                    }
                    return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
                }
            }
        }
        for (BlockPos pos : openLog) {
            for (EnumFacing dir : EnumFacing.Plane.HORIZONTAL) {
                if (!(ctx.world().getBlockState(pos.offset(dir)).getBlock() instanceof BlockAir)) {
                    continue;
                }
                Vec3i directionVec = dir.getDirectionVec();
                Vec3 faceCenter = new Vec3(pos).addVector(0.5, 0.5, 0.5).addVector(directionVec.getX() * 0.5, directionVec.getY() * 0.5, directionVec.getZ() * 0.5);
                Optional<Rotation> rot = RotationUtils.reachableOffset(ctx, pos, faceCenter, ctx.playerController().getBlockReachDistance(), false);
                if (rot.isPresent() && isSafeToCancel && baritone.getInventoryBehavior().throwaway(true, this::isCocoa)) {
                    MovingObjectPosition result = RayTraceUtils.rayTraceTowards(ctx.player(), rot.get(), ctx.playerController().getBlockReachDistance());
                    if (result.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && result.sideHit == dir) {
                        baritone.getLookBehavior().updateTarget(rot.get(), true);
                        if (ctx.isLookingAt(pos)) {
                            baritone.getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, true);
                        }
                        return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
                    }
                }
            }
        }
        for (BlockPos pos : bonemealable) {
            Optional<Rotation> rot = RotationUtils.reachable(ctx, pos);
            if (rot.isPresent() && isSafeToCancel && baritone.getInventoryBehavior().throwaway(true, this::isBoneMeal)) {
                baritone.getLookBehavior().updateTarget(rot.get(), true);
                if (ctx.isLookingAt(pos)) {
                    baritone.getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, true);
                }
                return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
            }
        }

        if (calcFailed) {
            logDirect("Farm failed");
            if (Baritone.settings().notificationOnFarmFail.value) {
                logNotification("Farm failed", true);
            }
            onLostControl();
            return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
        }

        List<Goal> goalz = new ArrayList<>();
        for (BlockPos pos : toBreak) {
            goalz.add(new BuilderProcess.GoalBreak(pos));
        }
        if (baritone.getInventoryBehavior().throwaway(false, this::isPlantable)) {
            for (BlockPos pos : openFarmland) {
                goalz.add(new GoalBlock(pos.up()));
            }
        }
        if (baritone.getInventoryBehavior().throwaway(false, this::isNetherWart)) {
            for (BlockPos pos : openSoulsand) {
                goalz.add(new GoalBlock(pos.up()));
            }
        }
        if (baritone.getInventoryBehavior().throwaway(false, this::isCocoa)) {
            for (BlockPos pos : openLog) {
                for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
                    if (ctx.world().getBlockState(pos.offset(direction)).getBlock() instanceof BlockAir) {
                        goalz.add(new GoalGetToBlock(pos.offset(direction)));
                    }
                }
            }
        }
        if (baritone.getInventoryBehavior().throwaway(false, this::isBoneMeal)) {
            for (BlockPos pos : bonemealable) {
                goalz.add(new GoalBlock(pos));
            }
        }
        for (Entity entity : ctx.world().loadedEntityList) {
            if (entity instanceof EntityItem && entity.onGround) {
                EntityItem ei = (EntityItem) entity;
                if (PICKUP_DROPPED.contains(ei.getEntityItem().getItem()) || isCocoa(ei.getEntityItem())) {
                    // +0.1 because of farmland's 0.9375 dummy height lol
                    goalz.add(new GoalBlock(new BlockPos(entity.posX, entity.posY + 0.1, entity.posZ)));
                }
            }
        }
        return new PathingCommand(new GoalComposite(goalz.toArray(new Goal[0])), PathingCommandType.SET_GOAL_AND_PATH);
    }

    @Override
    public void onLostControl() {
        active = false;
    }

    @Override
    public String displayName0() {
        return "Farming";
    }
}
