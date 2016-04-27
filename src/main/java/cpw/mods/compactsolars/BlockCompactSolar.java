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

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCompactSolar extends BlockContainer
{
    public static final PropertyEnum<CompactSolarType> TYPE_PROP = PropertyEnum.create("type", CompactSolarType.class);

    private Random random;

    public BlockCompactSolar()
    {
        super(Material.iron);
        setUnlocalizedName("compact_solar_block");
        setHardness(3.0F);
        random = new Random();
        setCreativeTab(CreativeTabs.tabRedstone);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE_PROP, CompactSolarType.LOW_VOLTAGE));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata)
    {
        return CompactSolarType.makeEntity(metadata);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (playerIn.isSneaking())
        {
            return false;
        }

        if (worldIn.isRemote)
        {
            return true;
        }

        TileEntity te = worldIn.getTileEntity(pos);
        if (te != null && te instanceof TileEntityCompactSolar)
        {
            TileEntityCompactSolar tecs = (TileEntityCompactSolar) te;
            playerIn.openGui(CompactSolars.instance, tecs.getType().ordinal(), worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return CompactSolarType.validateMeta(state.getValue(TYPE_PROP).ordinal());
    }

    @Override
    public int getRenderType()
    {
        return 3;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        TileEntityCompactSolar tileSolar = (TileEntityCompactSolar) world.getTileEntity(pos);
        if (tileSolar != null)
        {
            dropContent(0, tileSolar, world);
        }
        super.breakBlock(world, pos, state);
    }

    public void dropContent(int newSize, TileEntityCompactSolar tileSolar, World world)
    {
        for (int l = newSize; l < tileSolar.getSizeInventory(); l++)
        {
            ItemStack itemstack = tileSolar.getStackInSlot(l);
            if (itemstack == null)
            {
                continue;
            }
            float f = random.nextFloat() * 0.8F + 0.1F;
            float f1 = random.nextFloat() * 0.8F + 0.1F;
            float f2 = random.nextFloat() * 0.8F + 0.1F;
            while (itemstack.stackSize > 0)
            {
                int i1 = random.nextInt(21) + 10;
                if (i1 > itemstack.stackSize)
                {
                    i1 = itemstack.stackSize;
                }
                itemstack.stackSize -= i1;
                EntityItem entityitem = new EntityItem(world, tileSolar.getPos().getX() + f, (float) tileSolar.getPos().getY() + (newSize > 0 ? 1 : 0) + f1,
                        tileSolar.getPos().getZ() + f2, new ItemStack(itemstack.getItem(), i1, itemstack.getItemDamage()));
                float f3 = 0.05F;
                entityitem.motionX = (float) random.nextGaussian() * f3;
                entityitem.motionY = (float) random.nextGaussian() * f3 + 0.2F;
                entityitem.motionZ = (float) random.nextGaussian() * f3;
                if (itemstack.hasTagCompound())
                {
                    entityitem.getEntityItem().setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());
                }
                world.spawnEntityInWorld(entityitem);
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List itemList)
    {
        for (CompactSolarType type : CompactSolarType.values())
        {
            itemList.add(new ItemStack(this, 1, type.ordinal()));
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(TYPE_PROP, CompactSolarType.values()[meta]);
    }

    @Override
    public int getMetaFromState(IBlockState blockState)
    {
        return blockState.getValue(TYPE_PROP).ordinal();
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty<?>[] { TYPE_PROP });
    }

}
