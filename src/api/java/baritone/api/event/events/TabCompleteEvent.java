package baritone.api.event.events;

import baritone.api.event.events.type.Cancellable;

/**
 * @author LoganDark
 */
public class TabCompleteEvent extends Cancellable {

    public final String prefix;
    public String[] completions;

    public TabCompleteEvent(String prefix) {
        this.prefix = prefix;
        this.completions = null;
    }
}
