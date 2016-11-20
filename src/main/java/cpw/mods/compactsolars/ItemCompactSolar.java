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

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemColored;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("deprecation")
public class ItemCompactSolar extends ItemColored
{
    public ItemCompactSolar(Block b)
    {
        super(b, true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        tooltip.add(I18n.translateToLocal(
                I18n.translateToLocalFormatted("tile.compactsolars:powertier.tooltip", CompactSolarType.values()[stack.getItemDamage()].ordinal() + 1).trim())
                .trim());
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
