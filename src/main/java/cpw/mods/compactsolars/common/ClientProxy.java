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
package cpw.mods.compactsolars.common;

import cpw.mods.compactsolars.CompactSolars;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;

public class ClientProxy extends CommonProxy
{
    @Override
    public void registerRenderInformation()
    {
        //@formatter:off
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CompactSolars.compactSolarBlock), 0, new ModelResourceLocation(CompactSolars.compactSolarBlock.getRegistryName(), "type=low_voltage"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CompactSolars.compactSolarBlock), 1, new ModelResourceLocation(CompactSolars.compactSolarBlock.getRegistryName(), "type=medium_voltage"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CompactSolars.compactSolarBlock), 2, new ModelResourceLocation(CompactSolars.compactSolarBlock.getRegistryName(), "type=high_voltage"));
        //@formatter:on
    }

    @Override
    public void registerSolarHatModels(Item item)
    {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName().toString()));
    }

}
