/*******************************************************************************
 * Copyright (c) 2012 cpw.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     cpw - initial API and implementation
 ******************************************************************************/
package cpw.mods.compactsolars.common.gui.common;

import java.util.List;

import cpw.mods.compactsolars.common.CompactSolarType;
import cpw.mods.compactsolars.common.gui.slot.SlotCharging;
import cpw.mods.compactsolars.tileentity.TileEntityCompactSolar;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerCompactSolar extends Container
{
    public TileEntityCompactSolar tile;

    private boolean theSunIsVisible;

    private boolean initialized;

    private EntityPlayer myPlayer;

    public ContainerCompactSolar(IInventory playerInventory, TileEntityCompactSolar solarInventoryIn, CompactSolarType type)
    {
        this.tile = solarInventoryIn;
        this.myPlayer = ((InventoryPlayer) playerInventory).player;

        this.layoutContainer(playerInventory, solarInventoryIn, type);
    }

    private void layoutContainer(IInventory playerInventory, IInventory solarInventoryIn, CompactSolarType type)
    {
        this.addSlotToContainer(new SlotCharging(solarInventoryIn, 0, 80, 26, this.tile.getType().ordinal() + 1));

        for (int inventoryRow = 0; inventoryRow < 3; inventoryRow++)
        {
            for (int inventoryColumn = 0; inventoryColumn < 9; inventoryColumn++)
            {
                this.addSlotToContainer(new Slot(playerInventory, inventoryColumn + inventoryRow * 9 + 9, 8 + inventoryColumn * 18, 84 + inventoryRow * 18));
            }
        }

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++)
        {
            this.addSlotToContainer(new Slot(playerInventory, hotbarSlot, 8 + hotbarSlot * 18, 142));
        }

    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        List<IContainerListener> crafters = this.listeners;

        for (IContainerListener crafter : crafters)
        {
            if (this.theSunIsVisible != this.tile.theSunIsVisible || !this.initialized)
            {
                crafter.sendWindowProperty(this, 0, this.tile.theSunIsVisible ? 1 : 0);
            }
        }

        this.initialized = true;

        this.theSunIsVisible = this.tile.theSunIsVisible;
    }

    @Override
    public void updateProgressBar(int id, int data)
    {
        if (id == 0)
        {
            this.tile.theSunIsVisible = (data == 1);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return this.tile.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack stack = ItemStack.EMPTY;

        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack slotStack = slot.getStack();

            stack = slotStack.copy();

            if (index == 0)
            {
                if (!this.mergeItemStack(slotStack, 1, 37, true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (index >= 1 && index < 28)
            {
                if (!this.mergeItemStack(slotStack, 28, 37, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (index >= 28 && index < 37)
            {
                if (!this.mergeItemStack(slotStack, 1, 27, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.mergeItemStack(slotStack, 1, 37, false))
            {
                return ItemStack.EMPTY;
            }

            if (slotStack.getCount() == 0)
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (slotStack.getCount() != stack.getCount())
            {
                slot.onTake(playerIn, slotStack);
            }
            else
            {
                return ItemStack.EMPTY;
            }
        }

        return stack;
    }

    public EntityPlayer getPlayer()
    {
        return this.myPlayer;
    }
}
