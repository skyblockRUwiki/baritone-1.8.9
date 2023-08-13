package baritone.launch.mixins;

import baritone.utils.accessor.IAnvilChunkLoader;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;

@Mixin(AnvilChunkLoader.class)
public abstract class MixinAnvilChunkLoader implements IAnvilChunkLoader {

    @Accessor
    @Override
    public abstract File getChunkSaveLocation();
}
