package baritone.api.utils;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import net.minecraft.block.BlockFire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;

import java.util.Optional;

/**
 * @author Brady
 * @since 9/25/2018
 */
public final class RotationUtils {

    /**
     * Constant that a degree value is multiplied by to get the equivalent radian value
     */
    public static final double DEG_TO_RAD = Math.PI / 180.0;

    /**
     * Constant that a radian value is multiplied by to get the equivalent degree value
     */
    public static final double RAD_TO_DEG = 180.0 / Math.PI;

    /**
     * Offsets from the root block position to the center of each side.
     */
    private static final Vec3[] BLOCK_SIDE_MULTIPLIERS = new Vec3[]{
            new Vec3(0.5, 0, 0.5), // Down
            new Vec3(0.5, 1, 0.5), // Up
            new Vec3(0.5, 0.5, 0), // North
            new Vec3(0.5, 0.5, 1), // South
            new Vec3(0, 0.5, 0.5), // West
            new Vec3(1, 0.5, 0.5)  // East
    };

    private RotationUtils() {}

    /**
     * Calculates the rotation from BlockPos<sub>dest</sub> to BlockPos<sub>orig</sub>
     *
     * @param orig The origin position
     * @param dest The destination position
     * @return The rotation from the origin to the destination
     */
    public static Rotation calcRotationFromCoords(BlockPos orig, BlockPos dest) {
        return calcRotationFromVec3d(new Vec3(orig), new Vec3(dest));
    }

    /**
     * Wraps the target angles to a relative value from the current angles. This is done by
     * subtracting the current from the target, normalizing it, and then adding the current
     * angles back to it.
     *
     * @param current The current angles
     * @param target  The target angles
     * @return The wrapped angles
     */
    public static Rotation wrapAnglesToRelative(Rotation current, Rotation target) {
        if (current.yawIsReallyClose(target)) {
            return new Rotation(current.getYaw(), target.getPitch());
        }
        return target.subtract(current).normalize().add(current);
    }

    /**
     * Calculates the rotation from Vec<sub>dest</sub> to Vec<sub>orig</sub> and makes the
     * return value relative to the specified current rotations.
     *
     * @param orig    The origin position
     * @param dest    The destination position
     * @param current The current rotations
     * @return The rotation from the origin to the destination
     * @see #wrapAnglesToRelative(Rotation, Rotation)
     */
    public static Rotation calcRotationFromVec3d(Vec3 orig, Vec3 dest, Rotation current) {
        return wrapAnglesToRelative(current, calcRotationFromVec3d(orig, dest));
    }

    /**
     * Calculates the rotation from Vec<sub>dest</sub> to Vec<sub>orig</sub>
     *
     * @param orig The origin position
     * @param dest The destination position
     * @return The rotation from the origin to the destination
     */
    private static Rotation calcRotationFromVec3d(Vec3 orig, Vec3 dest) {
        double[] delta = {orig.xCoord - dest.xCoord, orig.yCoord - dest.yCoord, orig.zCoord - dest.zCoord};
        double yaw = MathHelper.atan2(delta[0], -delta[2]);
        double dist = Math.sqrt(delta[0] * delta[0] + delta[2] * delta[2]);
        double pitch = MathHelper.atan2(delta[1], dist);
        return new Rotation(
                (float) (yaw * RAD_TO_DEG),
                (float) (pitch * RAD_TO_DEG)
        );
    }

    /**
     * Calculates the look vector for the specified yaw/pitch rotations.
     *
     * @param rotation The input rotation
     * @return Look vector for the rotation
     */
    public static Vec3 calcVec3dFromRotation(Rotation rotation) {
        float f = MathHelper.cos(-rotation.getYaw() * (float) DEG_TO_RAD - (float) Math.PI);
        float f1 = MathHelper.sin(-rotation.getYaw() * (float) DEG_TO_RAD - (float) Math.PI);
        float f2 = -MathHelper.cos(-rotation.getPitch() * (float) DEG_TO_RAD);
        float f3 = MathHelper.sin(-rotation.getPitch() * (float) DEG_TO_RAD);
        return new Vec3((double) (f1 * f2), (double) f3, (double) (f * f2));
    }

    /**
     * @param ctx Context for the viewing entity
     * @param pos The target block position
     * @return The optional rotation
     * @see #reachable(IPlayerContext, BlockPos, double)
     */
    public static Optional<Rotation> reachable(IPlayerContext ctx, BlockPos pos) {
        return reachable(ctx, pos, false);
    }

    public static Optional<Rotation> reachable(IPlayerContext ctx, BlockPos pos, boolean wouldSneak) {
        return reachable(ctx, pos, ctx.playerController().getBlockReachDistance(), wouldSneak);
    }

    /**
     * Determines if the specified entity is able to reach the center of any of the sides
     * of the specified block. It first checks if the block center is reachable, and if so,
     * that rotation will be returned. If not, it will return the first center of a given
     * side that is reachable. The return type will be {@link Optional#empty()} if the entity is
     * unable to reach any of the sides of the block.
     *
     * @param ctx                Context for the viewing entity
     * @param pos                The target block position
     * @param blockReachDistance The block reach distance of the entity
     * @return The optional rotation
     */
    public static Optional<Rotation> reachable(IPlayerContext ctx, BlockPos pos, double blockReachDistance) {
        return reachable(ctx, pos, blockReachDistance, false);
    }

    public static Optional<Rotation> reachable(IPlayerContext ctx, BlockPos pos, double blockReachDistance, boolean wouldSneak) {
        if (BaritoneAPI.getSettings().remainWithExistingLookDirection.value && ctx.isLookingAt(pos)) {
            /*
             * why add 0.0001?
             * to indicate that we actually have a desired pitch
             * the way we indicate that the pitch can be whatever and we only care about the yaw
             * is by setting the desired pitch to the current pitch
             * setting the desired pitch to the current pitch + 0.0001 means that we do have a desired pitch, it's
             * just what it currently is
             *
             * or if you're a normal person literally all this does it ensure that we don't nudge the pitch to a normal level
             */
            Rotation hypothetical = ctx.playerRotations().add(new Rotation(0, 0.0001F));
            if (wouldSneak) {
                // the concern here is: what if we're looking at it now, but as soon as we start sneaking we no longer are
                MovingObjectPosition result = RayTraceUtils.rayTraceTowards(ctx.player(), hypothetical, blockReachDistance, true);
                if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && result.getBlockPos().equals(pos)) {
                    return Optional.of(hypothetical); // yes, if we sneaked we would still be looking at the block
                }
            } else {
                return Optional.of(hypothetical);
            }
        }
        Optional<Rotation> possibleRotation = reachableCenter(ctx, pos, blockReachDistance, wouldSneak);
        //System.out.println("center: " + possibleRotation);
        if (possibleRotation.isPresent()) {
            return possibleRotation;
        }

        IBlockState state = ctx.world().getBlockState(pos);
        AxisAlignedBB aabb = state.getBlock().getCollisionBoundingBox(ctx.world(), pos, state);
        if (aabb == null) {
            aabb = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        }
        for (Vec3 sideOffset : BLOCK_SIDE_MULTIPLIERS) {
            double xDiff = aabb.minX * sideOffset.xCoord + aabb.maxX * (1 - sideOffset.xCoord);
            double yDiff = aabb.minY * sideOffset.yCoord + aabb.maxY * (1 - sideOffset.yCoord);
            double zDiff = aabb.minZ * sideOffset.zCoord + aabb.maxZ * (1 - sideOffset.zCoord);
            possibleRotation = reachableOffset(ctx, pos, new Vec3(pos).addVector(xDiff, yDiff, zDiff), blockReachDistance, wouldSneak);
            if (possibleRotation.isPresent()) {
                return possibleRotation;
            }
        }
        return Optional.empty();
    }

    /**
     * Determines if the specified entity is able to reach the specified block with
     * the given offsetted position. The return type will be {@link Optional#empty()} if
     * the entity is unable to reach the block with the offset applied.
     *
     * @param ctx                Context for the viewing entity
     * @param pos                The target block position
     * @param offsetPos          The position of the block with the offset applied.
     * @param blockReachDistance The block reach distance of the entity
     * @return The optional rotation
     */
    public static Optional<Rotation> reachableOffset(IPlayerContext ctx, BlockPos pos, Vec3 offsetPos, double blockReachDistance, boolean wouldSneak) {
        Vec3 eyes = wouldSneak ? RayTraceUtils.inferSneakingEyePosition(ctx.player()) : ctx.player().getPositionEyes(1.0F);
        Rotation rotation = calcRotationFromVec3d(eyes, offsetPos, ctx.playerRotations());
        Rotation actualRotation = BaritoneAPI.getProvider().getBaritoneForPlayer(ctx.player()).getLookBehavior().getAimProcessor().peekRotation(rotation);
        MovingObjectPosition result = RayTraceUtils.rayTraceTowards(ctx.player(), actualRotation, blockReachDistance, wouldSneak);
        //System.out.println(result);
        if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if (result.getBlockPos().equals(pos)) {
                return Optional.of(rotation);
            }
            if (ctx.world().getBlockState(pos).getBlock() instanceof BlockFire && result.getBlockPos().equals(pos.down())) {
                return Optional.of(rotation);
            }
        }
        return Optional.empty();
    }

    /**
     * Determines if the specified entity is able to reach the specified block where it is
     * looking at the direct center of it's hitbox.
     *
     * @param ctx                Context for the viewing entity
     * @param pos                The target block position
     * @param blockReachDistance The block reach distance of the entity
     * @return The optional rotation
     */
    public static Optional<Rotation> reachableCenter(IPlayerContext ctx, BlockPos pos, double blockReachDistance, boolean wouldSneak) {
        return reachableOffset(ctx, pos, VecUtils.calculateBlockCenter(ctx.world(), pos), blockReachDistance, wouldSneak);
    }

    @Deprecated
    public static Optional<Rotation> reachable(EntityPlayerSP entity, BlockPos pos, double blockReachDistance) {
        return reachable(entity, pos, blockReachDistance, false);
    }

    @Deprecated
    public static Optional<Rotation> reachable(EntityPlayerSP entity, BlockPos pos, double blockReachDistance, boolean wouldSneak) {
        IBaritone baritone = BaritoneAPI.getProvider().getBaritoneForPlayer(entity);
        IPlayerContext ctx = baritone.getPlayerContext();
        return reachable(ctx, pos, blockReachDistance, wouldSneak);
    }

    @Deprecated
    public static Optional<Rotation> reachableOffset(Entity entity, BlockPos pos, Vec3 offsetPos, double blockReachDistance, boolean wouldSneak) {
        Vec3 eyes = wouldSneak ? RayTraceUtils.inferSneakingEyePosition(entity) : entity.getPositionEyes(1.0F);
        Rotation rotation = calcRotationFromVec3d(eyes, offsetPos, new Rotation(entity.rotationYaw, entity.rotationPitch));
        MovingObjectPosition result = RayTraceUtils.rayTraceTowards(entity, rotation, blockReachDistance, wouldSneak);
        //System.out.println(result);
        if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if (result.getBlockPos().equals(pos)) {
                return Optional.of(rotation);
            }
            if (entity.worldObj.getBlockState(pos).getBlock() instanceof BlockFire && result.getBlockPos().equals(pos.down())) {
                return Optional.of(rotation);
            }
        }
        return Optional.empty();
    }

    @Deprecated
    public static Optional<Rotation> reachableCenter(Entity entity, BlockPos pos, double blockReachDistance, boolean wouldSneak) {
        return reachableOffset(entity, pos, VecUtils.calculateBlockCenter(entity.worldObj, pos), blockReachDistance, wouldSneak);
    }
}
