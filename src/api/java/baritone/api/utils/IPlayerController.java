package baritone.api.utils;

import baritone.api.BaritoneAPI;
import baritone.api.utils.input.ClickType;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

/**
 * @author Brady
 * @since 12/14/2018
 */
public interface IPlayerController {

    void syncHeldItem();

    boolean hasBrokenBlock();

    boolean onPlayerDamageBlock(BlockPos pos, EnumFacing side);

    void resetBlockRemoving();

    ItemStack windowClick(int windowId, int slotId, int mouseButton, ClickType type, EntityPlayer player);

    WorldSettings.GameType getGameType();

    boolean processRightClickBlock(EntityPlayerSP player, World world, BlockPos pos, EnumFacing direction, Vec3 vec);

    boolean processRightClick(EntityPlayerSP player, World world);

    boolean clickBlock(BlockPos loc, EnumFacing face);

    void setHittingBlock(boolean hittingBlock);

    default double getBlockReachDistance() {
        return this.getGameType().isCreative() ? 5.0F : BaritoneAPI.getSettings().blockReachDistance.value;
    }
}
