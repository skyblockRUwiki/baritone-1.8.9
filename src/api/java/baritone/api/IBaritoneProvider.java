package baritone.api;

import baritone.api.cache.IWorldScanner;
import baritone.api.command.ICommandSystem;
import baritone.api.command.ICommand;
import baritone.api.schematic.ISchematicSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import java.util.List;
import java.util.Objects;

/**
 * Provides the present {@link IBaritone} instances, as well as non-baritone instance related APIs.
 *
 * @author leijurv
 */
public interface IBaritoneProvider {

    /**
     * Returns the primary {@link IBaritone} instance. This instance is persistent, and
     * is represented by the local player that is created by the game itself, not a "bot"
     * player through Baritone.
     *
     * @return The primary {@link IBaritone} instance.
     */
    IBaritone getPrimaryBaritone();

    /**
     * Returns all of the active {@link IBaritone} instances. This includes the local one
     * returned by {@link #getPrimaryBaritone()}.
     *
     * @return All active {@link IBaritone} instances.
     * @see #getBaritoneForPlayer(EntityPlayerSP)
     */
    List<IBaritone> getAllBaritones();

    /**
     * Provides the {@link IBaritone} instance for a given {@link EntityPlayerSP}.
     *
     * @param player The player
     * @return The {@link IBaritone} instance.
     */
    default IBaritone getBaritoneForPlayer(EntityPlayerSP player) {
        for (IBaritone baritone : this.getAllBaritones()) {
            if (Objects.equals(player, baritone.getPlayerContext().player())) {
                return baritone;
            }
        }
        return null;
    }

    /**
     * Provides the {@link IBaritone} instance for a given {@link Minecraft}.
     *
     * @param minecraft The minecraft
     * @return The {@link IBaritone} instance.
     */
    default IBaritone getBaritoneForMinecraft(Minecraft minecraft) {
        for (IBaritone baritone : this.getAllBaritones()) {
            if (Objects.equals(minecraft, baritone.getPlayerContext().minecraft())) {
                return baritone;
            }
        }
        return null;
    }

    /**
     * Creates and registers a new {@link IBaritone} instance using the specified {@link Minecraft}. The existing
     * instance is returned if already registered.
     *
     * @param minecraft The minecraft
     * @return The {@link IBaritone} instance
     */
    IBaritone createBaritone(Minecraft minecraft);

    /**
     * Destroys and removes the specified {@link IBaritone} instance. If the specified instance is the
     * {@link #getPrimaryBaritone() primary baritone}, this operation has no effect and will return {@code false}.
     *
     * @param baritone The baritone instance to remove
     * @return Whether the baritone instance was removed
     */
    boolean destroyBaritone(IBaritone baritone);

    /**
     * Returns the {@link IWorldScanner} instance. This is not a type returned by
     * {@link IBaritone} implementation, because it is not linked with {@link IBaritone}.
     *
     * @return The {@link IWorldScanner} instance.
     */
    IWorldScanner getWorldScanner();

    /**
     * Returns the {@link ICommandSystem} instance. This is not bound to a specific {@link IBaritone}
     * instance because {@link ICommandSystem} itself controls global behavior for {@link ICommand}s.
     *
     * @return The {@link ICommandSystem} instance.
     */
    ICommandSystem getCommandSystem();

    /**
     * @return The {@link ISchematicSystem} instance.
     */
    ISchematicSystem getSchematicSystem();
}
