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

import org.apache.logging.log4j.Level;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = "compactsolars", name = "Compact Solar Arrays", dependencies = "required-after:Forge@[12.17.0.1940,);required-after:IC2@[2.2,)")
public class CompactSolars
{
    @SidedProxy(clientSide = "cpw.mods.compactsolars.client.ClientProxy", serverSide = "cpw.mods.compactsolars.CommonProxy")
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
            compactSolarBlock = new BlockCompactSolar();
            compactSolarItemBlock = new ItemCompactSolar(compactSolarBlock);
            CompactSolarType.buildHats();
            Property scale = cfg.get(Configuration.CATEGORY_GENERAL, "scaleFactor", 1);
            scale.setComment("The EU generation scaling factor. " + "The average number of ticks needed to generate one EU packet."
                    + "1 is every tick, 2 is every other tick etc. " + "Each Solar will still generate a whole packet (8, 64, 512 EU).");
            productionRate = scale.getInt(1);
        }
        catch (Exception e)
        {
            FMLLog.log(Level.ERROR, e, "CompactSolars was unable to load it's configuration successfully");
            throw new RuntimeException(e);
        }
        finally
        {
            cfg.save();
        }

        compactSolarBlock.setUnlocalizedName("compactsolars.compact_solar_block");
        compactSolarBlock.setRegistryName(new ResourceLocation("compactsolars", "compact_solar_block"));

        compactSolarItemBlock.setUnlocalizedName("compactsolars.compact_solar_block");
        compactSolarItemBlock.setRegistryName(new ResourceLocation("compactsolars", "compact_solar_block"));

        GameRegistry.register(compactSolarBlock);
        GameRegistry.register(compactSolarItemBlock);

        for (CompactSolarType typ : CompactSolarType.values())
        {
            GameRegistry.registerTileEntity(typ.clazz, typ.tileEntityName());
        }

        proxy.registerRenderInformation();
    }

    @EventHandler
    public void init(FMLInitializationEvent init)
    {
        proxy.registerTileEntityRenderers();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
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
