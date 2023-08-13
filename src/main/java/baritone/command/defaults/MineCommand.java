package baritone.command.defaults;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.command.Command;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.datatypes.ForBlockOptionalMeta;
import baritone.api.command.exception.CommandException;
import baritone.api.utils.BlockOptionalMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class MineCommand extends Command {

    public MineCommand(IBaritone baritone) {
        super(baritone, "mine");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        int quantity = args.getAsOrDefault(Integer.class, 0);
        args.requireMin(1);
        List<BlockOptionalMeta> boms = new ArrayList<>();
        while (args.hasAny()) {
            boms.add(args.getDatatypeFor(ForBlockOptionalMeta.INSTANCE));
        }
        BaritoneAPI.getProvider().getWorldScanner().repack(ctx);
        logDirect(String.format("Mining %s", boms.toString()));
        baritone.getMineProcess().mine(quantity, boms.toArray(new BlockOptionalMeta[0]));
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        args.getAsOrDefault(Integer.class, 0);
        while (args.has(2)) {
            args.getDatatypeFor(ForBlockOptionalMeta.INSTANCE);
        }
        return args.tabCompleteDatatype(ForBlockOptionalMeta.INSTANCE);
    }

    @Override
    public String getShortDesc() {
        return "Mine some blocks";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "The mine command allows you to tell Baritone to search for and mine individual blocks.",
                "",
                "The specified blocks can be ores (which are commonly cached), or any other block.",
                "",
                "Also see the legitMine settings (see #set l legitMine).",
                "",
                "Usage:",
                "> mine diamond_ore - Mines all diamonds it can find.",
                "> mine redstone_ore lit_redstone_ore - Mines redstone ore.",
                "> mine log:0 - Mines only oak logs."
        );
    }
}
