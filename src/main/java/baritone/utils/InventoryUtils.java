package baritone.utils;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class InventoryUtils {

    public static boolean isHotbar(int index) {
        return index >= 0 && index < 9;
    }

    public static int getSlotFor(ItemStack stack, InventoryPlayer inventory) {
        for (int i = 0; i < inventory.mainInventory.length; ++i) {
            if (inventory.mainInventory[i] != null && stackEqualExact(stack, inventory.mainInventory[i])) {
                return i;
            }
        }

        return -1;
    }

    private static boolean stackEqualExact(ItemStack stack1, ItemStack stack2) {
        return stack1.getItem() == stack2.getItem() && (!stack1.getHasSubtypes() || stack1.getMetadata() == stack2.getMetadata()) && ItemStack.areItemStackTagsEqual(stack1, stack2);
    }
}
