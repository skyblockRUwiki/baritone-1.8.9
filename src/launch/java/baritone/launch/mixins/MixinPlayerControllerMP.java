package baritone.launch.mixins;

import baritone.utils.accessor.IPlayerControllerMP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP implements IPlayerControllerMP {

    @Accessor
    @Override
    public abstract void setIsHittingBlock(boolean isHittingBlock);

    @Accessor
    @Override
    public abstract BlockPos getCurrentBlock();

    @Invoker
    @Override
    public abstract void callSyncCurrentPlayItem();
}
