package baritone.api.event.events;

/**
 * @author Brady
 * @since 8/5/2018
 */
public final class RenderEvent {

    /**
     * The current render partial ticks
     */
    private final float partialTicks;

    public RenderEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    /**
     * @return The current render partial ticks
     */
    public final float getPartialTicks() {
        return this.partialTicks;
    }
}
