package baritone.launch.mixins;

import baritone.utils.accessor.IEntityList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(EntityList.class)
public abstract class MixinEntityList implements IEntityList {

    @Shadow
    @Final
    private static Map< Class <? extends Entity> , String > classToStringMapping;

    @Override
    public String getString(Class<? extends Entity> entity) {
        return classToStringMapping.get(entity);
    }
}
