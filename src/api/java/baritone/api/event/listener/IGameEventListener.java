package baritone.api.event.listener;

import baritone.api.event.events.*;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;

/**
 * @author Brady
 * @since 7/31/2018
 */
public interface IGameEventListener {

    /**
     * Run once per game tick before screen input is handled.
     *
     * @param event The event
     * @see Minecraft#runTick()
     */
    void onTick(TickEvent event);

    /**
     * Run once per game tick from before and after the player rotation is sent to the server.
     *
     * @param event The event
     * @see EntityPlayerSP#onUpdate()
     */
    void onPlayerUpdate(PlayerUpdateEvent event);

    /**
     * Runs whenever the client player sends a message to the server.
     *
     * @param event The event
     * @see EntityPlayerSP#sendChatMessage(String)
     */
    void onSendChatMessage(ChatEvent event);

    /**
     * Runs whenever the client player tries to tab complete in chat.
     *
     * @param event The event
     */
    void onPreTabComplete(TabCompleteEvent event);

    /**
     * Runs before and after whenever a chunk is either loaded, unloaded, or populated.
     *
     * @param event The event
     * @see WorldClient#doPreChunk(int, int, boolean)
     */
    void onChunkEvent(ChunkEvent event);

    /**
     * Runs once per world render pass. Two passes are made when {@link GameSettings#anaglyph} is on.
     * <p>
     * <b>Note:</b> {@link GameSettings#anaglyph} has been removed in Minecraft 1.13
     *
     * @param event The event
     */
    void onRenderPass(RenderEvent event);

    /**
     * Runs before and after whenever a new world is loaded
     *
     * @param event The event
     * @see Minecraft#loadWorld(WorldClient, String)
     */
    void onWorldEvent(WorldEvent event);

    /**
     * Runs before a outbound packet is sent
     *
     * @param event The event
     * @see Packet
     * @see GenericFutureListener
     */
    void onSendPacket(PacketEvent event);

    /**
     * Runs before an inbound packet is processed
     *
     * @param event The event
     * @see Packet
     * @see GenericFutureListener
     */
    void onReceivePacket(PacketEvent event);

    /**
     * Run once per game tick from before and after the player's moveRelative method is called
     * and before and after the player jumps.
     *
     * @param event The event
     * @see Entity#moveFlying(float, float, float) 
     */
    void onPlayerRotationMove(RotationMoveEvent event);

    /**
     * Called whenever the sprint keybind state is checked in {@link EntityPlayerSP#onLivingUpdate}
     *
     * @param event The event
     * @see EntityPlayerSP#onLivingUpdate()
     */
    void onPlayerSprintState(SprintStateEvent event);

    /**
     * Called when the local player interacts with a block, whether it is breaking or opening/placing.
     *
     * @param event The event
     */
    void onBlockInteract(BlockInteractEvent event);

    /**
     * Called when the local player dies, as indicated by the creation of the {@link GuiGameOver} screen.
     *
     * @see GuiGameOver
     */
    void onPlayerDeath();

    /**
     * When the pathfinder's state changes
     *
     * @param event The event
     */
    void onPathEvent(PathEvent event);
}
