package baritone.launch.mixins;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.event.events.RotationMoveEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static org.spongepowered.asm.lib.Opcodes.GETFIELD;

/**
 * @author Brady
 * @since 9/10/2018
 */
@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {

    /**
     * Event called to override the movement direction when jumping
     */
    @Unique
    private RotationMoveEvent jumpRotationEvent;

    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    @Inject(
            method = "jump",
            at = @At("HEAD")
    )
    private void preMoveRelative(CallbackInfo ci) {
        this.getBaritone().ifPresent(baritone -> {
            this.jumpRotationEvent = new RotationMoveEvent(RotationMoveEvent.Type.JUMP, this.rotationYaw, this.rotationPitch);
            baritone.getGameEventHandler().onPlayerRotationMove(this.jumpRotationEvent);
        });
    }

    @Redirect(
            method = "jump",
            at = @At(
                    value = "FIELD",
                    opcode = GETFIELD,
                    target = "net/minecraft/entity/EntityLivingBase.rotationYaw:F"
            )
    )
    private float overrideYaw(EntityLivingBase self) {
        if (self instanceof EntityPlayerSP && BaritoneAPI.getProvider().getBaritoneForPlayer((EntityPlayerSP) (Object) this) != null) {
            return this.jumpRotationEvent.getYaw();
        }
        return self.rotationYaw;
    }

    @Redirect(
            method = "moveEntityWithHeading",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityLivingBase;moveFlying(FFF)V"
            )
    )
    private void onMoveRelative(EntityLivingBase instance, float strafe, float forward, float friction) {
        Optional<IBaritone> baritone = this.getBaritone();
        if (!baritone.isPresent()) {
            moveFlying(strafe, forward, friction);
            return;
        }

        RotationMoveEvent event = new RotationMoveEvent(RotationMoveEvent.Type.MOTION_UPDATE, this.rotationYaw, this.rotationPitch);
        baritone.get().getGameEventHandler().onPlayerRotationMove(event);

        this.rotationYaw = event.getYaw();
        this.rotationPitch = event.getPitch();

        this.moveFlying(strafe, forward, friction);

        this.rotationYaw = event.getOriginal().getYaw();
        this.rotationPitch = event.getOriginal().getPitch();
    }

    @Unique
    private Optional<IBaritone> getBaritone() {
        // noinspection ConstantConditions
        if (EntityPlayerSP.class.isInstance(this)) {
            return Optional.ofNullable(BaritoneAPI.getProvider().getBaritoneForPlayer((EntityPlayerSP) (Object) this));
        } else {
            return Optional.empty();
        }
    }
}
