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
package cpw.mods.compactsolars.common.gui;

import cpw.mods.compactsolars.common.gui.client.GUISolar.GUI;
import cpw.mods.compactsolars.common.gui.common.ContainerCompactSolar;
import cpw.mods.compactsolars.tileentity.TileEntityCompactSolar;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));

        if (te != null && te instanceof TileEntityCompactSolar)
        {
            TileEntityCompactSolar tecs = (TileEntityCompactSolar) te;

            return new ContainerCompactSolar(player.inventory, tecs, tecs.getType());
        }
        else
        {
            return null;
        }
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));

        if (te != null && te instanceof TileEntityCompactSolar)
        {
            TileEntityCompactSolar tecs = (TileEntityCompactSolar) te;

            return GUI.buildGUI(tecs.getType(), player.inventory, tecs);
        }
        else
        {
            return null;
        }
    }
}
