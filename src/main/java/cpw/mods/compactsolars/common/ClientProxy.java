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
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(modid = "compactsolars", value = Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        //@formatter:off
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CompactSolars.compactSolarBlock), 0, new ModelResourceLocation(CompactSolars.compactSolarBlock.getRegistryName(), "type=low_voltage"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CompactSolars.compactSolarBlock), 1, new ModelResourceLocation(CompactSolars.compactSolarBlock.getRegistryName(), "type=medium_voltage"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CompactSolars.compactSolarBlock), 2, new ModelResourceLocation(CompactSolars.compactSolarBlock.getRegistryName(), "type=high_voltage"));
        //@formatter:on

        CompactSolarType.registerHatModels();
    }

    @Override
    public void registerSolarHatModels(Item item)
    {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName().toString()));
    }
}
