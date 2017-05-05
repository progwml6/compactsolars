package cpw.mods.compactsolars.common.gui.slot;

import javax.annotation.Nullable;

import ic2.api.item.ElectricItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotCharging extends Slot
{
    /** The tier of solar panel */
    private final int tier;

    public SlotCharging(IInventory inventoryIn, int slotIndex, int xPosition, int yPosition, int tier)
    {
        super(inventoryIn, slotIndex, xPosition, yPosition);

        this.tier = tier;
    }

    /**
     * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
     */
    @Override
    public boolean isItemValid(@Nullable ItemStack stack)
    {
        return ElectricItem.manager.charge(stack, Double.POSITIVE_INFINITY, this.tier, true, true) > 0.0D;
    }

}
