package baritone.api.command.datatypes;


import baritone.api.command.exception.CommandException;
import baritone.api.command.helpers.TabCompleteHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;

import java.util.stream.Stream;

public enum EntityClassById implements IDatatypeFor<Class<? extends Entity>> {
    INSTANCE;

    @Override
    public Class<? extends Entity> get(IDatatypeContext ctx) throws CommandException {
        ResourceLocation id = new ResourceLocation(ctx.getConsumer().getString());
        Class<? extends Entity> entity;

        try {
            entity = (Class<? extends Entity>) EntityList.class.getMethod("getClass", ResourceLocation.class).invoke(null, id);
        } catch (Exception ex) {
            throw new RuntimeException("EntityList.REGISTRY does not exist and failed to call the Forge-replacement method", ex);
        }

        if (entity == null) {
            throw new IllegalArgumentException("no entity found by that id");
        }
        return entity;
    }

    @Override
    public Stream<String> tabComplete(IDatatypeContext ctx) throws CommandException {
        return new TabCompleteHelper()
                .append(EntityList.getEntityNameList().stream().map(Object::toString))
                .filterPrefixNamespaced(ctx.getConsumer().getString())
                .sortAlphabetically()
                .stream();
    }
}
