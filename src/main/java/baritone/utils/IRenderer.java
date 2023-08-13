package baritone.utils;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public interface IRenderer {

    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer buffer = tessellator.getWorldRenderer();
    RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
    TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
    Settings settings = BaritoneAPI.getSettings();

    float[] color = new float[] {1.0F, 1.0F, 1.0F, 255.0F};

    static void glColor(Color color, float alpha) {
        float[] colorComponents = color.getColorComponents(null);
        IRenderer.color[0] = colorComponents[0];
        IRenderer.color[1] = colorComponents[1];
        IRenderer.color[2] = colorComponents[2];
        IRenderer.color[3] = alpha;
    }

    static void startLines(Color color, float alpha, float lineWidth, boolean ignoreDepth) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor(color, alpha);
        glLineWidth(lineWidth);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.disableLighting();

        if (ignoreDepth) {
            GlStateManager.disableDepth();
        }
        buffer.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
    }

    static void startLines(Color color, float lineWidth, boolean ignoreDepth) {
        startLines(color, .4f, lineWidth, ignoreDepth);
    }

    static void endLines(boolean ignoredDepth) {
        tessellator.draw();
        if (ignoredDepth) {
            GlStateManager.enableDepth();
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
    }

    static void emitAABB(AxisAlignedBB aabb) {
        AxisAlignedBB toDraw = aabb.offset(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ);

        // bottom
        buffer.pos(toDraw.minX, toDraw.minY, toDraw.minZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(toDraw.maxX, toDraw.minY, toDraw.minZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(toDraw.maxX, toDraw.minY, toDraw.minZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(toDraw.maxX, toDraw.minY, toDraw.maxZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(toDraw.maxX, toDraw.minY, toDraw.maxZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(toDraw.minX, toDraw.minY, toDraw.maxZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(toDraw.minX, toDraw.minY, toDraw.maxZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(toDraw.minX, toDraw.minY, toDraw.minZ).color(color[0], color[1], color[2], color[3]).endVertex();
        // top
        buffer.pos(toDraw.minX, toDraw.maxY, toDraw.minZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(toDraw.maxX, toDraw.maxY, toDraw.minZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(toDraw.maxX, toDraw.maxY, toDraw.minZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(toDraw.maxX, toDraw.maxY, toDraw.maxZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(toDraw.maxX, toDraw.maxY, toDraw.maxZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(toDraw.minX, toDraw.maxY, toDraw.maxZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(toDraw.minX, toDraw.maxY, toDraw.maxZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(toDraw.minX, toDraw.maxY, toDraw.minZ).color(color[0], color[1], color[2], color[3]).endVertex();
        // corners
        buffer.pos(toDraw.minX, toDraw.minY, toDraw.minZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(toDraw.minX, toDraw.maxY, toDraw.minZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(toDraw.maxX, toDraw.minY, toDraw.minZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(toDraw.maxX, toDraw.maxY, toDraw.minZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(toDraw.maxX, toDraw.minY, toDraw.maxZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(toDraw.maxX, toDraw.maxY, toDraw.maxZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(toDraw.minX, toDraw.minY, toDraw.maxZ).color(color[0], color[1], color[2], color[3]).endVertex();
        buffer.pos(toDraw.minX, toDraw.maxY, toDraw.maxZ).color(color[0], color[1], color[2], color[3]).endVertex();
    }

    static void emitAABB(AxisAlignedBB aabb, double expand) {
        emitAABB(aabb.expand(expand, expand, expand));
    }

    static void drawAABB(AxisAlignedBB aabb) {
        buffer.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        emitAABB(aabb);
        tessellator.draw();
    }
}
