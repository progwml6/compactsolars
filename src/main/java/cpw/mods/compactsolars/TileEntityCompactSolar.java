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
package cpw.mods.compactsolars;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import ic2.api.energy.prefab.BasicSource;
import ic2.api.item.IElectricItem;
import ic2.api.tile.IWrenchable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class TileEntityCompactSolar extends TileEntity implements ITickable, IInventory, IWrenchable
{
    private BasicSource energySource;
    private static Random random = new Random();
    private CompactSolarType type;
    private ItemStack[] inventory;
    private boolean initialized;
    public boolean theSunIsVisible;
    private int tick;
    private boolean canRain;
    private boolean noSunlight;

    public TileEntityCompactSolar()
    {
        this(CompactSolarType.LOW_VOLTAGE);
    }

    public TileEntityCompactSolar(CompactSolarType type)
    {
        super();
        this.type = type;
        this.inventory = new ItemStack[1];
        this.tick = random.nextInt(64);
        this.energySource = new BasicSource(this, type.maxStorage, type.ordinal() + 1);
    }

    @Override
    public void update()
    {
        this.energySource.update();

        if (!this.initialized && this.worldObj != null)
        {
            this.canRain = this.worldObj.getChunkFromBlockCoords(this.pos).getBiome(this.pos, this.worldObj.getBiomeProvider()).getRainfall() > 0;
            this.noSunlight = this.worldObj.provider.getHasNoSky();
            this.initialized = true;
        }

        if (this.noSunlight)
        {
            return;
        }

        if (this.tick-- == 0)
        {
            this.updateSunState();
            this.tick = 64;
        }

        int energyProduction = 0;

        if (this.theSunIsVisible && (CompactSolars.productionRate == 1 || random.nextInt(CompactSolars.productionRate) == 0))
        {
            energyProduction = this.generateEnergy();
        }

        this.energySource.addEnergy(energyProduction);

        if (this.inventory[0] != null && (this.inventory[0].getItem() instanceof IElectricItem))
        {
            this.energySource.charge(this.inventory[0]);
        }
    }

    private void updateSunState()
    {
        boolean isRaining = this.canRain && (this.worldObj.isRaining() || this.worldObj.isThundering());

        this.theSunIsVisible = this.worldObj.isDaytime() && !isRaining && this.worldObj.canSeeSky(this.pos.up());
    }

    private int generateEnergy()
    {
        return this.type.getOutput();
    }

    public ItemStack[] getContents()
    {
        return this.inventory;
    }

    @Override
    public int getSizeInventory()
    {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        return this.inventory[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        if (this.inventory[index] != null)
        {
            if (this.inventory[index].stackSize <= count)
            {
                ItemStack stack = this.inventory[index];
                this.inventory[index] = null;
                this.markDirty();
                return stack;
            }

            ItemStack stack = this.inventory[index].splitStack(count);

            if (this.inventory[index].stackSize == 0)
            {
                this.inventory[index] = null;
            }

            this.markDirty();

            return stack;
        }
        else
        {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int index, @Nullable ItemStack stack)
    {
        this.inventory[index] = stack;

        if (stack != null && stack.stackSize > this.getInventoryStackLimit())
        {
            stack.stackSize = this.getInventoryStackLimit();
        }

        this.markDirty();
    }

    @Override
    public String getName()
    {
        return this.type.name();
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        if (this.worldObj == null)
        {
            return true;
        }

        if (this.worldObj.getTileEntity(this.pos) != this)
        {
            return false;
        }

        return (!this.isInvalid()) && (player.getDistanceSq(this.pos) <= 64.0D);
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
        // NOOP
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
        // NOOP

    }

    @Override
    public EnumFacing getFacing(World world, BlockPos pos)
    {
        return EnumFacing.VALUES[0];
    }

    @Override
    public boolean setFacing(World world, BlockPos pos, EnumFacing newDirection, EntityPlayer player)
    {
        return false;
    }

    @Override
    public boolean wrenchCanRemove(World world, BlockPos pos, EntityPlayer player)
    {
        return true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        NBTTagList tagList = new NBTTagList();
        for (int slot = 0; slot < this.inventory.length; slot++)
        {
            if (this.inventory[slot] != null)
            {
                NBTTagCompound itemCompound = new NBTTagCompound();
                itemCompound.setByte("Slot", (byte) slot);
                this.inventory[slot].writeToNBT(itemCompound);
                tagList.appendTag(itemCompound);
            }
        }

        compound.setTag("Items", tagList);
        return this.energySource.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.energySource.readFromNBT(compound);
        NBTTagList tagList = compound.getTagList("Items", Constants.NBT.TAG_LIST);
        this.inventory = new ItemStack[this.getSizeInventory()];
        for (int itemCount = 0; itemCount < tagList.tagCount(); itemCount++)
        {
            NBTTagCompound itemCompound = tagList.getCompoundTagAt(itemCount);
            int slot = itemCompound.getByte("Slot") & 0xff;
            if (slot >= 0 && slot < this.inventory.length)
            {
                this.inventory[slot] = ItemStack.loadItemStackFromNBT(itemCompound);
            }
        }
    }

    public CompactSolarType getType()
    {
        return this.type;
    }

    @Override
    public void onChunkUnload()
    {
        this.energySource.onChunkUnload();
    }

    @Override
    public void invalidate()
    {
        this.energySource.invalidate();
        super.invalidate();
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        if (this.inventory[index] != null)
        {
            ItemStack stack = this.inventory[index];
            this.inventory[index] = null;
            return stack;
        }
        else
        {
            return null;
        }
    }

    @Override
    public void clear()
    {
        for (int i = 0; i < this.inventory.length; ++i)
        {
            this.inventory[i] = null;
        }
    }

    @Override
    public List<ItemStack> getWrenchDrops(World world, BlockPos pos, IBlockState state, TileEntity te, EntityPlayer player, int fortune)
    {
        return Arrays.asList(new ItemStack[] { new ItemStack(CompactSolars.compactSolarBlock, 1, this.getType().ordinal()) });
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return stack != null && stack.getItem() instanceof IElectricItem;
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {
    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName(), new Object[0]);
    }
}
