package baritone.api.event.events;

import baritone.api.event.events.type.EventState;
import net.minecraft.client.multiplayer.WorldClient;

public final class WorldEvent {

    /**
     * The new world that is being loaded. {@code null} if being unloaded.
     */
    private final WorldClient world;

    /**
     * The state of the event
     */
    private final EventState state;

    public WorldEvent(WorldClient world, EventState state) {
        this.world = world;
        this.state = state;
    }

    /**
     * @return The new world that is being loaded. {@code null} if being unloaded.
     */
    public final WorldClient getWorld() {
        return this.world;
    }

    /**
     * @return The state of the event
     */
    public final EventState getState() {
        return this.state;
    }
}
