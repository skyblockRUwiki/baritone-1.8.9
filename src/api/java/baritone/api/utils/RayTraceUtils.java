package baritone.api.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

/**
 * @author Brady
 * @since 8/25/2018
 */
public final class RayTraceUtils {

    private RayTraceUtils() {}

    /**
     * Performs a block raytrace with the specified rotations. This should only be used when
     * any entity collisions can be ignored, because this method will not recognize if an
     * entity is in the way or not. The local player's block reach distance will be used.
     *
     * @param entity             The entity representing the raytrace source
     * @param rotation           The rotation to raytrace towards
     * @param blockReachDistance The block reach distance of the entity
     * @return The calculated raytrace result
     */
    public static MovingObjectPosition rayTraceTowards(Entity entity, Rotation rotation, double blockReachDistance) {
        return rayTraceTowards(entity, rotation, blockReachDistance, false);
    }

    public static MovingObjectPosition rayTraceTowards(Entity entity, Rotation rotation, double blockReachDistance, boolean wouldSneak) {
        Vec3 start;
        if (wouldSneak) {
            start = inferSneakingEyePosition(entity);
        } else {
            start = entity.getPositionEyes(1.0F); // do whatever is correct
        }
        Vec3 direction = RotationUtils.calcVec3dFromRotation(rotation);
        Vec3 end = start.addVector(
                direction.xCoord * blockReachDistance,
                direction.yCoord * blockReachDistance,
                direction.zCoord * blockReachDistance
        );
        return entity.worldObj.rayTraceBlocks(start, end, false, false, true);
    }

    public static Vec3 inferSneakingEyePosition(Entity entity) {
        return new Vec3(entity.posX, entity.posY + IPlayerContext.eyeHeight(true), entity.posZ);
    }
}
