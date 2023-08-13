package baritone.utils.player;

import baritone.api.utils.IPlayerController;
import baritone.api.utils.input.ClickType;
import baritone.utils.accessor.IPlayerControllerMP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

/**
 * Implementation of {@link IPlayerController} that chains to the primary player controller's methods
 *
 * @author Brady
 * @since 12/14/2018
 */
public final class BaritonePlayerController implements IPlayerController {

    private final Minecraft mc;

    public BaritonePlayerController(Minecraft mc) {
        this.mc = mc;
    }

    @Override
    public void syncHeldItem() {
        ((IPlayerControllerMP) mc.playerController).callSyncCurrentPlayItem();
    }

    @Override
    public boolean hasBrokenBlock() {
        return ((IPlayerControllerMP) mc.playerController).getCurrentBlock().getY() == -1;
    }

    @Override
    public boolean onPlayerDamageBlock(BlockPos pos, EnumFacing side) {
        return mc.playerController.onPlayerDamageBlock(pos, side);
    }

    @Override
    public void resetBlockRemoving() {
        mc.playerController.resetBlockRemoving();
    }

    @Override
    public ItemStack windowClick(int windowId, int slotId, int mouseButton, ClickType type, EntityPlayer player) {
        return mc.playerController.windowClick(windowId, slotId, mouseButton, type.ordinal(), player);
    }

    @Override
    public WorldSettings.GameType getGameType() {
        return mc.playerController.getCurrentGameType();
    }

    @Override
    public boolean processRightClickBlock(EntityPlayerSP player, World world, BlockPos pos, EnumFacing direction, Vec3 vec) {
        return mc.playerController.onPlayerRightClick(player, (WorldClient) world, player.getHeldItem(), pos, direction, vec);
    }

    @Override
    public boolean processRightClick(EntityPlayerSP player, World world) {
        return mc.playerController.sendUseItem(player, world, player.getHeldItem());
    }

    @Override
    public boolean clickBlock(BlockPos loc, EnumFacing face) {
        return mc.playerController.clickBlock(loc, face);
    }

    @Override
    public void setHittingBlock(boolean hittingBlock) {
        ((IPlayerControllerMP) mc.playerController).setIsHittingBlock(hittingBlock);
    }
}
