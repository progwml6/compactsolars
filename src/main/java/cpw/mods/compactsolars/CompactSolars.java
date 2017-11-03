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

import cpw.mods.compactsolars.blocks.BlockCompactSolar;
import cpw.mods.compactsolars.common.CommonProxy;
import cpw.mods.compactsolars.common.CompactSolarType;
import cpw.mods.compactsolars.common.gui.GuiHandler;
import cpw.mods.compactsolars.common.version.Version;
import cpw.mods.compactsolars.items.ItemCompactSolar;
import cpw.mods.compactsolars.items.ItemSolarHat;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

@EventBusSubscriber
@Mod(modid = "compactsolars", name = "Compact Solar Arrays", dependencies = "required-after:forge@[14.23.0.2491,);required-after:ic2@[2.8,)")
public class CompactSolars
{
    @SidedProxy(clientSide = "cpw.mods.compactsolars.common.ClientProxy", serverSide = "cpw.mods.compactsolars.common.CommonProxy")
    public static CommonProxy proxy;

    public static BlockCompactSolar compactSolarBlock;

    public static ItemCompactSolar compactSolarItemBlock;

    public static int productionRate = 1;

    @Instance("compactsolars")
    public static CompactSolars instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent preinit)
    {
        Version.init(preinit.getVersionProperties());

        preinit.getModMetadata().version = Version.version();

        Configuration cfg = new Configuration(preinit.getSuggestedConfigurationFile());

        try
        {
            cfg.load();

            Property scale = cfg.get(Configuration.CATEGORY_GENERAL, "scaleFactor", 1);
            scale.setComment("The EU generation scaling factor. " + "The average number of ticks needed to generate one EU packet." + "1 is every tick, 2 is every other tick etc. " + "Each Solar will still generate a whole packet (8, 64, 512 EU).");

            productionRate = scale.getInt(1);
        }
        catch (Exception e)
        {
            preinit.getModLog().error("CompactSolars was unable to load it's configuration successfully", e);

            throw new RuntimeException(e);
        }
        finally
        {
            cfg.save();
        }
    }

    @SubscribeEvent
    public static void registerBlocks(Register<Block> event)
    {
        compactSolarBlock = new BlockCompactSolar();

        compactSolarBlock.setUnlocalizedName("compactsolars.compact_solar_block");
        compactSolarBlock.setRegistryName(new ResourceLocation("compactsolars", "compact_solar_block"));

        event.getRegistry().register(compactSolarBlock);

        for (CompactSolarType typ : CompactSolarType.values())
        {
            GameRegistry.registerTileEntity(typ.clazz, typ.tileEntityName());
        }
    }

    @SubscribeEvent
    public static void registerItems(Register<Item> event)
    {
        compactSolarItemBlock = new ItemCompactSolar(compactSolarBlock);

        compactSolarItemBlock.setUnlocalizedName(compactSolarBlock.getUnlocalizedName());
        compactSolarItemBlock.setRegistryName(compactSolarBlock.getRegistryName());

        event.getRegistry().register(compactSolarItemBlock);

        for (CompactSolarType typ : CompactSolarType.values())
        {
            event.getRegistry().register(typ.buildHat());
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent init)
    {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent postinit)
    {
        CompactSolarType.generateRecipes(compactSolarBlock);
        CompactSolarType.generateHatRecipes(compactSolarBlock);
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent evt)
    {
        ItemSolarHat.clearRaining();
    }
}
