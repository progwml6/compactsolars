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
package cpw.mods.compactsolars.items;

import java.util.List;

import cpw.mods.compactsolars.common.CompactSolarType;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemColored;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemCompactSolar extends ItemColored
{
    public ItemCompactSolar(Block b)
    {
        super(b, true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag)
    {
        tooltip.add(I18n.format("tile.compactsolars:powertier.tooltip", CompactSolarType.values()[stack.getItemDamage()].ordinal() + 1).trim());
    }

    @Override
    public int getMetadata(int damage)
    {
        if (damage < CompactSolarType.values().length)
        {
            return damage;
        }
        else
        {
            return 0;
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        return "tile.compactsolars:" + CompactSolarType.values()[stack.getItemDamage()].getName() + "_block";
    }
}
