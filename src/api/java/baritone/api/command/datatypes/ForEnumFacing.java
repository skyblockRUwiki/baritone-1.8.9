package baritone.api.command.datatypes;

import baritone.api.command.exception.CommandException;
import baritone.api.command.helpers.TabCompleteHelper;
import net.minecraft.util.EnumFacing;

import java.util.Locale;
import java.util.stream.Stream;

public enum ForEnumFacing implements IDatatypeFor<EnumFacing> {
    INSTANCE;

    @Override
    public EnumFacing get(IDatatypeContext ctx) throws CommandException {
        return EnumFacing.valueOf(ctx.getConsumer().getString().toUpperCase(Locale.US));
    }

    @Override
    public Stream<String> tabComplete(IDatatypeContext ctx) throws CommandException {
        return new TabCompleteHelper()
                .append(Stream.of(EnumFacing.values())
                        .map(EnumFacing::getName).map(String::toLowerCase))
                .filterPrefix(ctx.getConsumer().getString())
                .stream();
    }
}
