package baritone.api.utils;

import baritone.api.BaritoneAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.Arrays;
import java.util.Calendar;
import java.util.stream.Stream;

/**
 * An ease-of-access interface to provide the {@link Minecraft} game instance,
 * chat and console logging mechanisms, and the Baritone chat prefix.
 *
 * @author Brady
 * @since 8/1/2018
 */
public interface Helper {

    /**
     * Instance of {@link Helper}. Used for static-context reference.
     */
    Helper HELPER = new Helper() {};

    /**
     * The main game instance returned by {@link Minecraft#getMinecraft()}.
     * Deprecated since {@link IPlayerContext#minecraft()} should be used instead (In the majority of cases).
     */
    @Deprecated
    Minecraft mc = Minecraft.getMinecraft();

    static IChatComponent getPrefix() {
        // Inner text component
        final Calendar now = Calendar.getInstance();
        final boolean xd = now.get(Calendar.MONTH) == Calendar.APRIL && now.get(Calendar.DAY_OF_MONTH) <= 3;
        IChatComponent baritone = new ChatComponentText(xd ? "Baritoe" : BaritoneAPI.getSettings().shortBaritonePrefix.value ? "B" : "Baritone");
        baritone.getChatStyle().setColor(EnumChatFormatting.LIGHT_PURPLE);

        // Outer brackets
        IChatComponent prefix = new ChatComponentText("");
        prefix.getChatStyle().setColor(EnumChatFormatting.DARK_PURPLE);
        prefix.appendText("[");
        prefix.appendSibling(baritone);
        prefix.appendText("]");

        return prefix;
    }

    /**
     * Send a message to display as a toast popup
     *
     * @param title   The title to display in the popup
     * @param message The message to display in the popup
     */
    default void logToast(IChatComponent title, IChatComponent message) {
        Minecraft.getMinecraft().addScheduledTask(() -> BaritoneAPI.getSettings().toaster.value.accept(title, message));
    }

    /**
     * Send a message to display as a toast popup
     *
     * @param title   The title to display in the popup
     * @param message The message to display in the popup
     */
    default void logToast(String title, String message) {
        logToast(new ChatComponentText(title), new ChatComponentText(message));
    }

    /**
     * Send a message to display as a toast popup
     *
     * @param message The message to display in the popup
     */
    default void logToast(String message) {
        logToast(Helper.getPrefix(), new ChatComponentText(message));
    }

    /**
     * Send a message as a desktop notification
     *
     * @param message The message to display in the notification
     */
    default void logNotification(String message) {
        logNotification(message, false);
    }

    /**
     * Send a message as a desktop notification
     *
     * @param message The message to display in the notification
     * @param error   Whether to log as an error
     */
    default void logNotification(String message, boolean error) {
        if (BaritoneAPI.getSettings().desktopNotifications.value) {
            logNotificationDirect(message, error);
        }
    }

    /**
     * Send a message as a desktop notification regardless of desktopNotifications
     * (should only be used for critically important messages)
     *
     * @param message The message to display in the notification
     */
    default void logNotificationDirect(String message) {
        logNotificationDirect(message, false);
    }

    /**
     * Send a message as a desktop notification regardless of desktopNotifications
     * (should only be used for critically important messages)
     *
     * @param message The message to display in the notification
     * @param error   Whether to log as an error
     */
    default void logNotificationDirect(String message, boolean error) {
        Minecraft.getMinecraft().addScheduledTask(() -> BaritoneAPI.getSettings().notifier.value.accept(message, error));
    }

    /**
     * Send a message to chat only if chatDebug is on
     *
     * @param message The message to display in chat
     */
    default void logDebug(String message) {
        if (!BaritoneAPI.getSettings().chatDebug.value) {
            //System.out.println("Suppressed debug message:");
            //System.out.println(message);
            return;
        }
        // We won't log debug chat into toasts
        // Because only a madman would want that extreme spam -_-
        logDirect(message, false);
    }

    /**
     * Send components to chat with the [Baritone] prefix
     *
     * @param logAsToast Whether to log as a toast notification
     * @param components The components to send
     */
    default void logDirect(boolean logAsToast, IChatComponent... components) {
        IChatComponent component = new ChatComponentText("");
        if (!logAsToast) {
            // If we are not logging as a Toast
            // Append the prefix to the base component line
            component.appendSibling(getPrefix());
            component.appendSibling(new ChatComponentText(" "));
        }
        Arrays.asList(components).forEach(component::appendSibling);
        if (logAsToast) {
            logToast(getPrefix(), component);
        } else {
            Minecraft.getMinecraft().addScheduledTask(() -> BaritoneAPI.getSettings().logger.value.accept(component));
        }
    }

    /**
     * Send components to chat with the [Baritone] prefix
     *
     * @param components The components to send
     */
    default void logDirect(IChatComponent... components) {
        logDirect(BaritoneAPI.getSettings().logAsToast.value, components);
    }

    /**
     * Send a message to chat regardless of chatDebug (should only be used for critically important messages, or as a
     * direct response to a chat command)
     *
     * @param message    The message to display in chat
     * @param color      The color to print that message in
     * @param logAsToast Whether to log as a toast notification
     */
    default void logDirect(String message, EnumChatFormatting color, boolean logAsToast) {
        Stream.of(message.split("\n")).forEach(line -> {
            IChatComponent component = new ChatComponentText(line.replace("\t", "    "));
            component.getChatStyle().setColor(color);
            logDirect(logAsToast, component);
        });
    }

    /**
     * Send a message to chat regardless of chatDebug (should only be used for critically important messages, or as a
     * direct response to a chat command)
     *
     * @param message The message to display in chat
     * @param color   The color to print that message in
     */
    default void logDirect(String message, EnumChatFormatting color) {
        logDirect(message, color, BaritoneAPI.getSettings().logAsToast.value);
    }

    /**
     * Send a message to chat regardless of chatDebug (should only be used for critically important messages, or as a
     * direct response to a chat command)
     *
     * @param message    The message to display in chat
     * @param logAsToast Whether to log as a toast notification
     */
    default void logDirect(String message, boolean logAsToast) {
        logDirect(message, EnumChatFormatting.GRAY, logAsToast);
    }

    /**
     * Send a message to chat regardless of chatDebug (should only be used for critically important messages, or as a
     * direct response to a chat command)
     *
     * @param message The message to display in chat
     */
    default void logDirect(String message) {
        logDirect(message, BaritoneAPI.getSettings().logAsToast.value);
    }
}
