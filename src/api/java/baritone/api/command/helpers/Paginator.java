package baritone.api.command.helpers;

import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.exception.CommandException;
import baritone.api.command.exception.CommandInvalidTypeException;
import baritone.api.utils.Helper;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class Paginator<E> implements Helper {

    public final List<E> entries;
    public int pageSize = 8;
    public int page = 1;

    public Paginator(List<E> entries) {
        this.entries = entries;
    }

    public Paginator(E... entries) {
        this.entries = Arrays.asList(entries);
    }

    public Paginator<E> setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public int getMaxPage() {
        return (entries.size() - 1) / pageSize + 1;
    }

    public boolean validPage(int page) {
        return page > 0 && page <= getMaxPage();
    }

    public Paginator<E> skipPages(int pages) {
        page += pages;
        return this;
    }

    public void display(Function<E, IChatComponent> transform, String commandPrefix) {
        int offset = (page - 1) * pageSize;
        for (int i = offset; i < offset + pageSize; i++) {
            if (i < entries.size()) {
                logDirect(transform.apply(entries.get(i)));
            } else {
                logDirect("--", EnumChatFormatting.DARK_GRAY);
            }
        }
        boolean hasPrevPage = commandPrefix != null && validPage(page - 1);
        boolean hasNextPage = commandPrefix != null && validPage(page + 1);
        IChatComponent prevPageComponent = new ChatComponentText("<<");
        if (hasPrevPage) {
            prevPageComponent.getChatStyle()
                    .setChatClickEvent(new ClickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            String.format("%s %d", commandPrefix, page - 1)
                    ))
                    .setChatHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            new ChatComponentText("Click to view previous page")
                    ));
        } else {
            prevPageComponent.getChatStyle().setColor(EnumChatFormatting.DARK_GRAY);
        }
        IChatComponent nextPageComponent = new ChatComponentText(">>");
        if (hasNextPage) {
            nextPageComponent.getChatStyle()
                    .setChatClickEvent(new ClickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            String.format("%s %d", commandPrefix, page + 1)
                    ))
                    .setChatHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            new ChatComponentText("Click to view next page")
                    ));
        } else {
            nextPageComponent.getChatStyle().setColor(EnumChatFormatting.DARK_GRAY);
        }
        IChatComponent pagerComponent = new ChatComponentText("");
        pagerComponent.getChatStyle().setColor(EnumChatFormatting.GRAY);
        pagerComponent.appendSibling(prevPageComponent);
        pagerComponent.appendText(" | ");
        pagerComponent.appendSibling(nextPageComponent);
        pagerComponent.appendText(String.format(" %d/%d", page, getMaxPage()));
        logDirect(pagerComponent);
    }

    public void display(Function<E, IChatComponent> transform) {
        display(transform, null);
    }

    public static <T> void paginate(IArgConsumer consumer, Paginator<T> pagi, Runnable pre, Function<T, IChatComponent> transform, String commandPrefix) throws CommandException {
        int page = 1;
        consumer.requireMax(1);
        if (consumer.hasAny()) {
            page = consumer.getAs(Integer.class);
            if (!pagi.validPage(page)) {
                throw new CommandInvalidTypeException(
                        consumer.consumed(),
                        String.format(
                                "a valid page (1-%d)",
                                pagi.getMaxPage()
                        ),
                        consumer.consumed().getValue()
                );
            }
        }
        pagi.skipPages(page - pagi.page);
        if (pre != null) {
            pre.run();
        }
        pagi.display(transform, commandPrefix);
    }

    public static <T> void paginate(IArgConsumer consumer, List<T> elems, Runnable pre, Function<T, IChatComponent> transform, String commandPrefix) throws CommandException {
        paginate(consumer, new Paginator<>(elems), pre, transform, commandPrefix);
    }

    public static <T> void paginate(IArgConsumer consumer, T[] elems, Runnable pre, Function<T, IChatComponent> transform, String commandPrefix) throws CommandException {
        paginate(consumer, Arrays.asList(elems), pre, transform, commandPrefix);
    }

    public static <T> void paginate(IArgConsumer consumer, Paginator<T> pagi, Function<T, IChatComponent> transform, String commandPrefix) throws CommandException {
        paginate(consumer, pagi, null, transform, commandPrefix);
    }

    public static <T> void paginate(IArgConsumer consumer, List<T> elems, Function<T, IChatComponent> transform, String commandPrefix) throws CommandException {
        paginate(consumer, new Paginator<>(elems), null, transform, commandPrefix);
    }

    public static <T> void paginate(IArgConsumer consumer, T[] elems, Function<T, IChatComponent> transform, String commandPrefix) throws CommandException {
        paginate(consumer, Arrays.asList(elems), null, transform, commandPrefix);
    }

    public static <T> void paginate(IArgConsumer consumer, Paginator<T> pagi, Runnable pre, Function<T, IChatComponent> transform) throws CommandException {
        paginate(consumer, pagi, pre, transform, null);
    }

    public static <T> void paginate(IArgConsumer consumer, List<T> elems, Runnable pre, Function<T, IChatComponent> transform) throws CommandException {
        paginate(consumer, new Paginator<>(elems), pre, transform, null);
    }

    public static <T> void paginate(IArgConsumer consumer, T[] elems, Runnable pre, Function<T, IChatComponent> transform) throws CommandException {
        paginate(consumer, Arrays.asList(elems), pre, transform, null);
    }

    public static <T> void paginate(IArgConsumer consumer, Paginator<T> pagi, Function<T, IChatComponent> transform) throws CommandException {
        paginate(consumer, pagi, null, transform, null);
    }

    public static <T> void paginate(IArgConsumer consumer, List<T> elems, Function<T, IChatComponent> transform) throws CommandException {
        paginate(consumer, new Paginator<>(elems), null, transform, null);
    }

    public static <T> void paginate(IArgConsumer consumer, T[] elems, Function<T, IChatComponent> transform) throws CommandException {
        paginate(consumer, Arrays.asList(elems), null, transform, null);
    }
}
