package baritone.utils.accessor;

import net.minecraft.entity.Entity;

public interface IEntityList {

    String getString(Class<? extends Entity> entity);
}
