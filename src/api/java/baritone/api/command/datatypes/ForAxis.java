package baritone.api.command.datatypes;

import baritone.api.command.exception.CommandException;
import baritone.api.command.helpers.TabCompleteHelper;
import net.minecraft.util.EnumFacing;

import java.util.Locale;
import java.util.stream.Stream;

public enum ForAxis implements IDatatypeFor<EnumFacing.Axis> {
    INSTANCE;

    @Override
    public EnumFacing.Axis get(IDatatypeContext ctx) throws CommandException {
        return EnumFacing.Axis.valueOf(ctx.getConsumer().getString().toUpperCase(Locale.US));
    }

    @Override
    public Stream<String> tabComplete(IDatatypeContext ctx) throws CommandException {
        return new TabCompleteHelper()
                .append(Stream.of(EnumFacing.Axis.values())
                        .map(EnumFacing.Axis::getName).map(String::toLowerCase))
                .filterPrefix(ctx.getConsumer().getString())
                .stream();
    }
}
