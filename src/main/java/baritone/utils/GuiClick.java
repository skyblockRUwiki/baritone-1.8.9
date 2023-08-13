package baritone.utils;

import baritone.Baritone;
import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.utils.BetterBlockPos;
import baritone.api.utils.Helper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collections;

import static baritone.api.command.IBaritoneChatControl.FORCE_COMMAND_PREFIX;
import static org.lwjgl.opengl.GL11.*;

public class GuiClick extends GuiScreen {

    // My name is Brady and I grant leijurv permission to use this pasted code
    private final FloatBuffer MODELVIEW = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer PROJECTION = BufferUtils.createFloatBuffer(16);
    private final IntBuffer VIEWPORT = BufferUtils.createIntBuffer(16);
    private final FloatBuffer TO_WORLD_BUFFER = BufferUtils.createFloatBuffer(3);

    private BlockPos clickStart;
    private BlockPos currentMouseOver;

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int mx = Mouse.getX();
        int my = Mouse.getY();
        Vec3 near = toWorld(mx, my, 0);
        Vec3 far = toWorld(mx, my, 1); // "Use 0.945 that's what stack overflow says" - leijurv
        if (near != null && far != null) {
            Vec3 viewerPos = new Vec3(mc.getRenderManager().viewerPosX, mc.getRenderManager().viewerPosY, mc.getRenderManager().viewerPosZ);
            MovingObjectPosition result = mc.theWorld.rayTraceBlocks(near.add(viewerPos), far.add(viewerPos), false, false, true);
            if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                currentMouseOver = result.getBlockPos();
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (currentMouseOver != null) { //Catch this, or else a click into void will result in a crash
            if (mouseButton == 0) {
                if (clickStart != null && !clickStart.equals(currentMouseOver)) {
                    BaritoneAPI.getProvider().getPrimaryBaritone().getSelectionManager().removeAllSelections();
                    BaritoneAPI.getProvider().getPrimaryBaritone().getSelectionManager().addSelection(BetterBlockPos.from(clickStart), BetterBlockPos.from(currentMouseOver));
                    IChatComponent component = new ChatComponentText("Selection made! For usage: " + Baritone.settings().prefix.value + "help sel");
                    component.getChatStyle()
                            .setColor(EnumChatFormatting.WHITE)
                            .setChatClickEvent(new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    FORCE_COMMAND_PREFIX + "help sel"
                            ));
                    Helper.HELPER.logDirect(component);
                    clickStart = null;
                } else {
                    BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(currentMouseOver));
                }
            } else if (mouseButton == 1) {
                BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(currentMouseOver.up()));
            }
        }
        clickStart = null;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        clickStart = currentMouseOver;
    }

    public void onRender() {
        GlStateManager.getFloat(GL_MODELVIEW_MATRIX, (FloatBuffer) MODELVIEW.clear());
        GlStateManager.getFloat(GL_PROJECTION_MATRIX, (FloatBuffer) PROJECTION.clear());
        glGetInteger(GL_VIEWPORT, (IntBuffer) VIEWPORT.clear());

        if (currentMouseOver != null) {
            Entity e = mc.getRenderViewEntity();
            // drawSingleSelectionBox WHEN?
            PathRenderer.drawManySelectionBoxes(e, Collections.singletonList(currentMouseOver), Color.CYAN);
            if (clickStart != null && !clickStart.equals(currentMouseOver)) {
                IRenderer.startLines(Color.RED, Baritone.settings().pathRenderLineWidthPixels.value, true);
                BetterBlockPos a = new BetterBlockPos(currentMouseOver);
                BetterBlockPos b = new BetterBlockPos(clickStart);
                IRenderer.emitAABB(new AxisAlignedBB(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z), Math.max(a.x, b.x) + 1, Math.max(a.y, b.y) + 1, Math.max(a.z, b.z) + 1));
                IRenderer.endLines(true);
            }
        }
    }

    private Vec3 toWorld(double x, double y, double z) {
        boolean result = GLU.gluUnProject((float) x, (float) y, (float) z, MODELVIEW, PROJECTION, VIEWPORT, (FloatBuffer) TO_WORLD_BUFFER.clear());
        if (result) {
            return new Vec3(TO_WORLD_BUFFER.get(0), TO_WORLD_BUFFER.get(1), TO_WORLD_BUFFER.get(2));
        }
        return null;
    }
}
