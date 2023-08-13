package baritone.utils;

import baritone.api.BaritoneAPI;
import baritone.api.event.events.RenderEvent;
import baritone.api.pathing.calc.IPath;
import baritone.api.pathing.goals.*;
import baritone.api.utils.BetterBlockPos;
import baritone.api.utils.IPlayerContext;
import baritone.api.utils.interfaces.IGoalRenderPos;
import baritone.behavior.PathingBehavior;
import baritone.pathing.path.PathExecutor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Brady
 * @since 8/9/2018
 */
public final class PathRenderer implements IRenderer {

    private PathRenderer() {}

    public static void render(RenderEvent event, PathingBehavior behavior) {
        final IPlayerContext ctx = behavior.ctx;
        if (ctx.world() == null) {
            return;
        }
        if (ctx.minecraft().currentScreen instanceof GuiClick) {
            ((GuiClick) ctx.minecraft().currentScreen).onRender();
        }

        final float partialTicks = event.getPartialTicks();
        final Goal goal = behavior.getGoal();

        final int thisPlayerDimension = ctx.world().provider.getDimensionId();
        final int currentRenderViewDimension = BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext().world().provider.getDimensionId();

        if (thisPlayerDimension != currentRenderViewDimension) {
            // this is a path for a bot in a different dimension, don't render it
            return;
        }

        if (goal != null && settings.renderGoal.value) {
            drawGoal(ctx.player(), goal, partialTicks, settings.colorGoalBox.value);
        }

        if (!settings.renderPath.value) {
            return;
        }

        PathExecutor current = behavior.getCurrent(); // this should prevent most race conditions?
        PathExecutor next = behavior.getNext(); // like, now it's not possible for current!=null to be true, then suddenly false because of another thread
        if (current != null && settings.renderSelectionBoxes.value) {
            drawManySelectionBoxes(ctx.player(), current.toBreak(), settings.colorBlocksToBreak.value);
            drawManySelectionBoxes(ctx.player(), current.toPlace(), settings.colorBlocksToPlace.value);
            drawManySelectionBoxes(ctx.player(), current.toWalkInto(), settings.colorBlocksToWalkInto.value);
        }

        //drawManySelectionBoxes(player, Collections.singletonList(behavior.pathStart()), partialTicks, Color.WHITE);

        // Render the current path, if there is one
        if (current != null && current.getPath() != null) {
            int renderBegin = Math.max(current.getPosition() - 3, 0);
            drawPath(current.getPath(), renderBegin, settings.colorCurrentPath.value, settings.fadePath.value, 10, 20);
        }

        if (next != null && next.getPath() != null) {
            drawPath(next.getPath(), 0, settings.colorNextPath.value, settings.fadePath.value, 10, 20);
        }

        // If there is a path calculation currently running, render the path calculation process
        behavior.getInProgress().ifPresent(currentlyRunning -> {
            currentlyRunning.bestPathSoFar().ifPresent(p -> {
                drawPath(p, 0, settings.colorBestPathSoFar.value, settings.fadePath.value, 10, 20);
            });

            currentlyRunning.pathToMostRecentNodeConsidered().ifPresent(mr -> {
                drawPath(mr, 0, settings.colorMostRecentConsidered.value, settings.fadePath.value, 10, 20);
                drawManySelectionBoxes(ctx.player(), Collections.singletonList(mr.getDest()), settings.colorMostRecentConsidered.value);
            });
        });
    }

    private static void drawPath(IPath path, int startIndex, Color color, boolean fadeOut, int fadeStart0, int fadeEnd0) {
        IRenderer.startLines(color, settings.pathRenderLineWidthPixels.value, settings.renderPathIgnoreDepth.value);

        int fadeStart = fadeStart0 + startIndex;
        int fadeEnd = fadeEnd0 + startIndex;

        List<BetterBlockPos> positions = path.positions();
        for (int i = startIndex, next; i < positions.size() - 1; i = next) {
            BetterBlockPos start = positions.get(i);
            BetterBlockPos end = positions.get(next = i + 1);

            int dirX = end.x - start.x;
            int dirY = end.y - start.y;
            int dirZ = end.z - start.z;

            while (next + 1 < positions.size() && (!fadeOut || next + 1 < fadeStart) &&
                    (dirX == positions.get(next + 1).x - end.x &&
                            dirY == positions.get(next + 1).y - end.y &&
                            dirZ == positions.get(next + 1).z - end.z)) {
                end = positions.get(++next);
            }

            if (fadeOut) {
                float alpha;

                if (i <= fadeStart) {
                    alpha = 0.4F;
                } else {
                    if (i > fadeEnd) {
                        break;
                    }
                    alpha = 0.4F * (1.0F - (float) (i - fadeStart) / (float) (fadeEnd - fadeStart));
                }

                IRenderer.glColor(color, alpha);
            }

            emitLine(start.x, start.y, start.z, end.x, end.y, end.z);
        }

        IRenderer.endLines(settings.renderPathIgnoreDepth.value);
    }

    private static void emitLine(double x1, double y1, double z1, double x2, double y2, double z2) {
        double vpX = renderManager.viewerPosX;
        double vpY = renderManager.viewerPosY;
        double vpZ = renderManager.viewerPosZ;
        boolean renderPathAsFrickinThingy = !settings.renderPathAsLine.value;

        buffer.pos(x1 + 0.5D - vpX, y1 + 0.5D - vpY, z1 + 0.5D - vpZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(x2 + 0.5D - vpX, y2 + 0.5D - vpY, z2 + 0.5D - vpZ).color(color[0], color[1], color[2], color[3]).endVertex();

        if (renderPathAsFrickinThingy) {
            buffer.pos(x2 + 0.5D - vpX, y2 + 0.5D - vpY, z2 + 0.5D - vpZ).color(color[0], color[1], color[2], color[3]).endVertex();
            buffer.pos(x2 + 0.5D - vpX, y2 + 0.53D - vpY, z2 + 0.5D - vpZ).color(color[0], color[1], color[2], color[3]).endVertex();

            buffer.pos(x2 + 0.5D - vpX, y2 + 0.53D - vpY, z2 + 0.5D - vpZ).color(color[0], color[1], color[2], color[3]).endVertex();
            buffer.pos(x1 + 0.5D - vpX, y1 + 0.53D - vpY, z1 + 0.5D - vpZ).color(color[0], color[1], color[2], color[3]).endVertex();

            buffer.pos(x1 + 0.5D - vpX, y1 + 0.53D - vpY, z1 + 0.5D - vpZ).color(color[0], color[1], color[2], color[3]).endVertex();
            buffer.pos(x1 + 0.5D - vpX, y1 + 0.5D - vpY, z1 + 0.5D - vpZ).color(color[0], color[1], color[2], color[3]).endVertex();
        }
    }

    public static void drawManySelectionBoxes(Entity player, Collection<BlockPos> positions, Color color) {
        IRenderer.startLines(color, settings.pathRenderLineWidthPixels.value, settings.renderSelectionBoxesIgnoreDepth.value);

        //BlockPos blockpos = movingObjectPositionIn.getBlockPos();
        BlockStateInterface bsi = new BlockStateInterface(BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext()); // TODO this assumes same dimension between primary baritone and render view? is this safe?

        positions.forEach(pos -> {
            IBlockState state = bsi.get0(pos);
            AxisAlignedBB toDraw;

            if (state.getBlock().equals(Blocks.air)) {
                toDraw = Blocks.dirt.getDefaultState().getBlock().getSelectedBoundingBox(player.worldObj, pos);
            } else {
                toDraw = state.getBlock().getSelectedBoundingBox(player.worldObj, pos);
            }

            IRenderer.emitAABB(toDraw, .002D);
        });

        IRenderer.endLines(settings.renderSelectionBoxesIgnoreDepth.value);
    }

    private static void drawGoal(Entity player, Goal goal, float partialTicks, Color color) {
        drawGoal(player, goal, partialTicks, color, true);
    }

    private static void drawGoal(Entity player, Goal goal, float partialTicks, Color color, boolean setupRender) {
        double renderPosX = renderManager.viewerPosX;
        double renderPosY = renderManager.viewerPosY;
        double renderPosZ = renderManager.viewerPosZ;
        double minX, maxX;
        double minZ, maxZ;
        double minY, maxY;
        double y, y1, y2;
        if (!settings.renderGoalAnimated.value) {
            // y = 1 causes rendering issues when the player is at the same y as the top of a block for some reason
            y = 0.999F;
        } else {
            y = MathHelper.cos((float) (((float) ((System.nanoTime() / 100000L) % 20000L)) / 20000F * Math.PI * 2));
        }
        if (goal instanceof IGoalRenderPos) {
            BlockPos goalPos = ((IGoalRenderPos) goal).getGoalPos();
            minX = goalPos.getX() + 0.002 - renderPosX;
            maxX = goalPos.getX() + 1 - 0.002 - renderPosX;
            minZ = goalPos.getZ() + 0.002 - renderPosZ;
            maxZ = goalPos.getZ() + 1 - 0.002 - renderPosZ;
            if (goal instanceof GoalGetToBlock || goal instanceof GoalTwoBlocks) {
                y /= 2;
            }
            y1 = 1 + y + goalPos.getY() - renderPosY;
            y2 = 1 - y + goalPos.getY() - renderPosY;
            minY = goalPos.getY() - renderPosY;
            maxY = minY + 2;
            if (goal instanceof GoalGetToBlock || goal instanceof GoalTwoBlocks) {
                y1 -= 0.5;
                y2 -= 0.5;
                maxY--;
            }
            drawDankLitGoalBox(color, minX, maxX, minZ, maxZ, minY, maxY, y1, y2, setupRender);
        } else if (goal instanceof GoalXZ) {
            GoalXZ goalPos = (GoalXZ) goal;

            if (settings.renderGoalXZBeacon.value) {
                textureManager.bindTexture(new ResourceLocation("textures/entity/beacon_beam.png"));

                if (settings.renderGoalIgnoreDepth.value) {
                    GlStateManager.disableDepth();
                }

                renderBeamSegment(
                        goalPos.getX() - renderPosX,
                        -renderPosY,
                        goalPos.getZ() - renderPosZ,
                        settings.renderGoalAnimated.value ? partialTicks : 0,
                        1.0,
                        settings.renderGoalAnimated.value ? player.worldObj.getTotalWorldTime() : 0,
                        0,
                        256,
                        color.getColorComponents(null)
                );

                if (settings.renderGoalIgnoreDepth.value) {
                    GlStateManager.enableDepth();
                }
                return;
            }

            minX = goalPos.getX() + 0.002 - renderPosX;
            maxX = goalPos.getX() + 1 - 0.002 - renderPosX;
            minZ = goalPos.getZ() + 0.002 - renderPosZ;
            maxZ = goalPos.getZ() + 1 - 0.002 - renderPosZ;

            y1 = 0;
            y2 = 0;
            minY = 0 - renderPosY;
            maxY = 256 - renderPosY;
            drawDankLitGoalBox(color, minX, maxX, minZ, maxZ, minY, maxY, y1, y2, setupRender);
        } else if (goal instanceof GoalComposite) {
            // Simple way to determine if goals can be batched, without having some sort of GoalRenderer
            boolean batch = Arrays.stream(((GoalComposite) goal).goals()).allMatch(IGoalRenderPos.class::isInstance);

            if (batch) {
                IRenderer.startLines(color, settings.goalRenderLineWidthPixels.value, settings.renderGoalIgnoreDepth.value);
            }
            for (Goal g : ((GoalComposite) goal).goals()) {
                drawGoal(player, g, partialTicks, color, !batch);
            }
            if (batch) {
                IRenderer.endLines(settings.renderGoalIgnoreDepth.value);
            }
        } else if (goal instanceof GoalInverted) {
            drawGoal(player, ((GoalInverted) goal).origin, partialTicks, settings.colorInvertedGoalBox.value);
        } else if (goal instanceof GoalYLevel) {
            GoalYLevel goalpos = (GoalYLevel) goal;
            minX = player.posX - settings.yLevelBoxSize.value - renderPosX;
            minZ = player.posZ - settings.yLevelBoxSize.value - renderPosZ;
            maxX = player.posX + settings.yLevelBoxSize.value - renderPosX;
            maxZ = player.posZ + settings.yLevelBoxSize.value - renderPosZ;
            minY = ((GoalYLevel) goal).level - renderPosY;
            maxY = minY + 2;
            y1 = 1 + y + goalpos.level - renderPosY;
            y2 = 1 - y + goalpos.level - renderPosY;
            drawDankLitGoalBox(color, minX, maxX, minZ, maxZ, minY, maxY, y1, y2, setupRender);
        }
    }

    public static void renderBeamSegment(double x, double y, double z, double partialTicks, double textureScale, double totalWorldTime, int yOffset, int height, float[] colors) {
        renderBeamSegment(x, y, z, partialTicks, textureScale, totalWorldTime, yOffset, height, colors, 0.2D, 0.25D);
    }

    public static void renderBeamSegment(double x, double y, double z, double partialTicks, double textureScale, double totalWorldTime, int yOffset, int height, float[] colors, double beamRadius, double glowRadius) {
        int i = yOffset + height;
        GL11.glTexParameteri(3553, 10242, 10497);
        GL11.glTexParameteri(3553, 10243, 10497);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer bufferbuilder = tessellator.getWorldRenderer();
        double d0 = totalWorldTime + partialTicks;
        double d1 = height < 0 ? d0 : -d0;
        double num = d1 * 0.2D - (double)MathHelper.floor_double(d1 * 0.1D);
        double d2 = num - MathHelper.floor_double(num);
        float f = colors[0];
        float f1 = colors[1];
        float f2 = colors[2];
        double d3 = d0 * 0.025D * -1.5D;
        double d4 = 0.5D + Math.cos(d3 + 2.356194490192345D) * beamRadius;
        double d5 = 0.5D + Math.sin(d3 + 2.356194490192345D) * beamRadius;
        double d6 = 0.5D + Math.cos(d3 + (Math.PI / 4D)) * beamRadius;
        double d7 = 0.5D + Math.sin(d3 + (Math.PI / 4D)) * beamRadius;
        double d8 = 0.5D + Math.cos(d3 + 3.9269908169872414D) * beamRadius;
        double d9 = 0.5D + Math.sin(d3 + 3.9269908169872414D) * beamRadius;
        double d10 = 0.5D + Math.cos(d3 + 5.497787143782138D) * beamRadius;
        double d11 = 0.5D + Math.sin(d3 + 5.497787143782138D) * beamRadius;
        double d12 = 0.0D;
        double d13 = 1.0D;
        double d14 = -1.0D + d2;
        double d15 = (double)height * textureScale * (0.5D / beamRadius) + d14;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(x + d4, y + (double)i, z + d5).tex(1.0D, d15).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d4, y + (double)yOffset, z + d5).tex(1.0D, d14).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d6, y + (double)yOffset, z + d7).tex(0.0D, d14).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d6, y + (double)i, z + d7).tex(0.0D, d15).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d10, y + (double)i, z + d11).tex(1.0D, d15).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d10, y + (double)yOffset, z + d11).tex(1.0D, d14).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d8, y + (double)yOffset, z + d9).tex(0.0D, d14).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d8, y + (double)i, z + d9).tex(0.0D, d15).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d6, y + (double)i, z + d7).tex(1.0D, d15).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d6, y + (double)yOffset, z + d7).tex(1.0D, d14).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d10, y + (double)yOffset, z + d11).tex(0.0D, d14).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d10, y + (double)i, z + d11).tex(0.0D, d15).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d8, y + (double)i, z + d9).tex(1.0D, d15).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d8, y + (double)yOffset, z + d9).tex(1.0D, d14).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d4, y + (double)yOffset, z + d5).tex(0.0D, d14).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d4, y + (double)i, z + d5).tex(0.0D, d15).color(f, f1, f2, 1.0F).endVertex();
        tessellator.draw();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.depthMask(false);
        d3 = 0.5D - glowRadius;
        d4 = 0.5D - glowRadius;
        d5 = 0.5D + glowRadius;
        d6 = 0.5D - glowRadius;
        d7 = 0.5D - glowRadius;
        d8 = 0.5D + glowRadius;
        d9 = 0.5D + glowRadius;
        d10 = 0.5D + glowRadius;
        d11 = 0.0D;
        d12 = 1.0D;
        d13 = -1.0D + d2;
        d14 = (double)height * textureScale + d13;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(x + d3, y + (double)i, z + d4).tex(1.0D, d14).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d3, y + (double)yOffset, z + d4).tex(1.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d5, y + (double)yOffset, z + d6).tex(0.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d5, y + (double)i, z + d6).tex(0.0D, d14).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d9, y + (double)i, z + d10).tex(1.0D, d14).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d9, y + (double)yOffset, z + d10).tex(1.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d7, y + (double)yOffset, z + d8).tex(0.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d7, y + (double)i, z + d8).tex(0.0D, d14).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d5, y + (double)i, z + d6).tex(1.0D, d14).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d5, y + (double)yOffset, z + d6).tex(1.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d9, y + (double)yOffset, z + d10).tex(0.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d9, y + (double)i, z + d10).tex(0.0D, d14).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d7, y + (double)i, z + d8).tex(1.0D, d14).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d7, y + (double)yOffset, z + d8).tex(1.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d3, y + (double)yOffset, z + d4).tex(0.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d3, y + (double)i, z + d4).tex(0.0D, d14).color(f, f1, f2, 0.125F).endVertex();
        tessellator.draw();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
    }

    private static void drawDankLitGoalBox(Color colorIn, double minX, double maxX, double minZ, double maxZ, double minY, double maxY, double y1, double y2, boolean setupRender) {
        if (setupRender) {
            IRenderer.startLines(colorIn, settings.goalRenderLineWidthPixels.value, settings.renderGoalIgnoreDepth.value);
        }

        renderHorizontalQuad(minX, maxX, minZ, maxZ, y1);
        renderHorizontalQuad(minX, maxX, minZ, maxZ, y2);

        buffer.pos(minX, minY, minZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(minX, maxY, minZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(maxX, minY, minZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(maxX, maxY, minZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(maxX, minY, maxZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(maxX, maxY, maxZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(minX, minY, maxZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(minX, maxY, maxZ).color(color[0], color[1], color[2], color[3]).endVertex();

        if (setupRender) {
            IRenderer.endLines(settings.renderGoalIgnoreDepth.value);
        }
    }

    private static void renderHorizontalQuad(double minX, double maxX, double minZ, double maxZ, double y) {
        if (y != 0) {
            buffer.pos(minX, y, minZ).color(color[0], color[1], color[2], color[3]).endVertex();
            buffer.pos(maxX, y, minZ).color(color[0], color[1], color[2], color[3]).endVertex();

            buffer.pos(maxX, y, minZ).color(color[0], color[1], color[2], color[3]).endVertex();
            buffer.pos(maxX, y, maxZ).color(color[0], color[1], color[2], color[3]).endVertex();

            buffer.pos(maxX, y, maxZ).color(color[0], color[1], color[2], color[3]).endVertex();
            buffer.pos(minX, y, maxZ).color(color[0], color[1], color[2], color[3]).endVertex();

            buffer.pos(minX, y, maxZ).color(color[0], color[1], color[2], color[3]).endVertex();
            buffer.pos(minX, y, minZ).color(color[0], color[1], color[2], color[3]).endVertex();
        }
    }
}
