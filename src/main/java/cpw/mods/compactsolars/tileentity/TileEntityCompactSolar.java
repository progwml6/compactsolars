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
package cpw.mods.compactsolars.tileentity;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import cpw.mods.compactsolars.CompactSolars;
import cpw.mods.compactsolars.common.CompactSolarType;
import ic2.api.energy.prefab.BasicEnergyTe.Source;
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
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class TileEntityCompactSolar extends Source implements ITickable, IInventory, IWrenchable
{
    private static Random random = new Random();

    private CompactSolarType type;

    private NonNullList<ItemStack> inventory;

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
        super(type.maxStorage, type.ordinal() + 1);
        this.type = type;
        this.inventory = NonNullList.<ItemStack> withSize(1, ItemStack.EMPTY);
        this.tick = random.nextInt(64);
    }

    @Override
    public void update()
    {
        if (!this.initialized && this.world != null)
        {
            this.canRain = this.world.getChunkFromBlockCoords(this.pos).getBiome(this.pos, this.world.getBiomeProvider()).getRainfall() > 0;
            this.noSunlight = this.world.provider.hasNoSky();
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

        this.getEnergyBuffer().addEnergy(energyProduction);

        if (!this.inventory.get(0).isEmpty() && (this.inventory.get(0).getItem() instanceof IElectricItem))
        {
            this.getEnergyBuffer().charge(this.inventory.get(0));
        }
    }

    private void updateSunState()
    {
        boolean isRaining = this.canRain && (this.world.isRaining() || this.world.isThundering());

        this.theSunIsVisible = this.world.isDaytime() && !isRaining && this.world.canSeeSky(this.pos.up());
    }

    private int generateEnergy()
    {
        return this.type.getOutput();
    }

    public NonNullList<ItemStack> getContents()
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
        return this.inventory.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        if (!this.inventory.get(index).isEmpty())
        {
            if (this.inventory.get(index).getCount() <= count)
            {
                ItemStack stack = this.inventory.get(index);
                this.inventory.set(index, ItemStack.EMPTY);
                this.markDirty();
                return stack;
            }

            ItemStack stack = this.inventory.get(index).splitStack(count);

            if (this.inventory.get(index).getCount() == 0)
            {
                this.inventory.set(index, ItemStack.EMPTY);
            }

            this.markDirty();

            return stack;
        }
        else
        {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void setInventorySlotContents(int index, @Nullable ItemStack stack)
    {
        this.inventory.set(index, stack);

        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit())
        {
            stack.setCount(this.getInventoryStackLimit());
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
    public boolean isUsableByPlayer(EntityPlayer player)
    {
        if (this.world == null)
        {
            return true;
        }

        if (this.world.getTileEntity(this.pos) != this)
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
        NBTTagList tagList = new NBTTagList();

        for (int slot = 0; slot < this.inventory.size(); slot++)
        {
            if (!this.inventory.get(slot).isEmpty())
            {
                NBTTagCompound itemCompound = new NBTTagCompound();

                itemCompound.setByte("Slot", (byte) slot);

                this.inventory.get(slot).writeToNBT(itemCompound);

                tagList.appendTag(itemCompound);
            }
        }

        compound.setTag("Items", tagList);

        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        NBTTagList tagList = compound.getTagList("Items", Constants.NBT.TAG_LIST);
        this.inventory = NonNullList.<ItemStack> withSize(this.getSizeInventory(), ItemStack.EMPTY);

        for (int itemCount = 0; itemCount < tagList.tagCount(); itemCount++)
        {
            NBTTagCompound itemCompound = tagList.getCompoundTagAt(itemCount);
            int slot = itemCompound.getByte("Slot") & 0xff;

            if (slot >= 0 && slot < this.inventory.size())
            {
                this.inventory.set(slot, new ItemStack(itemCompound));
            }
        }
    }

    public CompactSolarType getType()
    {
        return this.type;
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        if (!this.inventory.get(index).isEmpty())
        {
            ItemStack stack = this.inventory.get(index);

            this.inventory.set(index, ItemStack.EMPTY);

            return stack;
        }
        else
        {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void clear()
    {
        for (int i = 0; i < this.inventory.size(); ++i)
        {
            this.inventory.set(i, ItemStack.EMPTY);
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
        return !stack.isEmpty() && stack.getItem() instanceof IElectricItem;
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

    @Override
    public boolean isEmpty()
    {
        for (ItemStack itemstack : this.inventory)
        {
            if (!itemstack.isEmpty())
            {
                return false;
            }
        }

        return true;
    }
}
