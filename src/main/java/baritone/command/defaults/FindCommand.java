package baritone.command.defaults;

import baritone.api.IBaritone;
import baritone.api.command.Command;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.datatypes.BlockById;
import baritone.api.command.exception.CommandException;
import baritone.api.command.helpers.TabCompleteHelper;
import baritone.api.utils.BetterBlockPos;
import baritone.cache.CachedChunk;
import net.minecraft.block.Block;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static baritone.api.command.IBaritoneChatControl.FORCE_COMMAND_PREFIX;

public class FindCommand extends Command {

    public FindCommand(IBaritone baritone) {
        super(baritone, "find");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        args.requireMin(1);
        List<Block> toFind = new ArrayList<>();
        while (args.hasAny()) {
            toFind.add(args.getDatatypeFor(BlockById.INSTANCE));
        }
        BetterBlockPos origin = ctx.playerFeet();
        IChatComponent[] components = toFind.stream()
                .flatMap(block ->
                        ctx.worldData().getCachedWorld().getLocationsOf(
                                Block.blockRegistry.getNameForObject(block).getResourcePath(),
                                Integer.MAX_VALUE,
                                origin.x,
                                origin.y,
                                4
                        ).stream()
                )
                .map(BetterBlockPos::new)
                .map(this::positionToComponent)
                .toArray(IChatComponent[]::new);
        if (components.length > 0) {
            Arrays.asList(components).forEach(this::logDirect);
        } else {
            logDirect("No positions known, are you sure the blocks are cached?");
        }
    }

    private IChatComponent positionToComponent(BetterBlockPos pos) {
        String positionText = String.format("%s %s %s", pos.x, pos.y, pos.z);
        String command = String.format("%sgoal %s", FORCE_COMMAND_PREFIX, positionText);
        IChatComponent baseComponent = new ChatComponentText(pos.toString());
        IChatComponent hoverComponent = new ChatComponentText("Click to set goal to this position");
        baseComponent.getChatStyle()
                .setColor(EnumChatFormatting.GRAY)
                .setInsertion(positionText)
                .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponent));
        return baseComponent;
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        return new TabCompleteHelper()
                .append(
                        CachedChunk.BLOCKS_TO_KEEP_TRACK_OF.stream()
                                .map(Block.blockRegistry::getNameForObject)
                                .map(Object::toString)
                )
                .filterPrefixNamespaced(args.getString())
                .sortAlphabetically()
                .stream();
    }

    @Override
    public String getShortDesc() {
        return "Find positions of a certain block";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "The find command searches through Baritone's cache and attempts to find the location of the block.",
                "Tab completion will suggest only cached blocks and uncached blocks can not be found.",
                "",
                "Usage:",
                "> find <block> [...] - Try finding the listed blocks"
        );
    }
}