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

import com.google.common.base.Throwables;

import ic2.api.item.IC2Items;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public enum CompactSolarType implements IStringSerializable
{
    //@formatter:off
    LOW_VOLTAGE(8, 32, "Low Voltage Solar Array", "lv_transformer", TileEntityCompactSolar.class, "lv_hat"),
    MEDIUM_VOLTAGE(64, 128, "Medium Voltage Solar Array", "mv_transformer", TileEntityCompactSolarMV.class, "mv_hat"),
    HIGH_VOLTAGE(512, 512, "High Voltage Solar Array", "hv_transformer", TileEntityCompactSolarHV.class, "hv_hat");
    //@formatter:on

    private int output;
    public Class<? extends TileEntityCompactSolar> clazz;
    public String friendlyName;
    public String transformerName;
    public final ResourceLocation hatTexture;
    public final String hatName;
    private ItemSolarHat item;
    public final ResourceLocation hatItemTexture;
    public final int outputPacketSize;
    public final int maxStorage;

    //@formatter:off
    private CompactSolarType(int output, int outputPacketSize, String friendlyName, String transformerName, Class<? extends TileEntityCompactSolar> clazz, String hatTexture)
    //@formatter:on
    {
        this.output = output;
        this.outputPacketSize = outputPacketSize;
        this.friendlyName = friendlyName;
        this.transformerName = transformerName;
        this.clazz = clazz;
        this.hatName = "solar_hat_" + this.name().toLowerCase();
        this.hatTexture = new ResourceLocation("compactsolars", "textures/armor/" + hatTexture + ".png");
        this.hatItemTexture = new ResourceLocation("compactsolars", hatTexture);
        this.maxStorage = outputPacketSize << 1;
    }

    public static void generateRecipes(BlockCompactSolar block)
    {
        ItemStack solar = IC2Items.getItem("te", "solar_generator");

        ItemStack parent = solar;

        for (CompactSolarType typ : values())
        {
            ItemStack targ = new ItemStack(block, 1, typ.ordinal());

            ItemStack transformer = IC2Items.getItem("te", typ.transformerName);

            addRecipe(targ, "SSS", "SXS", "SSS", 'S', parent, 'X', transformer);

            parent = targ;
        }
    }

    private static void addRecipe(ItemStack target, Object... args)
    {
        GameRegistry.addRecipe(target, args);
    }

    public int getOutput()
    {
        return this.output;
    }

    public static TileEntityCompactSolar makeEntity(int metadata)
    {
        try
        {
            TileEntityCompactSolar te = values()[metadata].clazz.newInstance();
            return te;
        }
        catch (Exception e)
        {
            throw Throwables.propagate(e);
        }
    }

    public int getTextureRow()
    {
        return this.ordinal();
    }

    public String tileEntityName()
    {
        return "CompactSolarType." + this.name();
    }

    public ItemSolarHat buildHat()
    {
        this.item = new ItemSolarHat(this);

        this.item.setUnlocalizedName("compactsolars:" + this.hatName);
        this.item.setRegistryName(new ResourceLocation("compactsolars", this.hatName));

        GameRegistry.register(this.item);

        return this.item;
    }

    public static void buildHats()
    {
        for (CompactSolarType typ : values())
        {
            typ.buildHat();
            CompactSolars.proxy.registerSolarHatModels(typ.item);
        }
    }

    public static void generateHatRecipes(BlockCompactSolar block)
    {
        Item ironHat = Items.IRON_HELMET;

        for (CompactSolarType typ : values())
        {
            ItemStack solarBlock = new ItemStack(block, 0, typ.ordinal());

            GameRegistry.addShapelessRecipe(new ItemStack(typ.item), solarBlock, ironHat);
        }
    }

    public static int validateMeta(int metadata)
    {
        if (metadata < values().length)
        {
            return metadata;
        }
        else
        {
            return 0;
        }
    }

    @Override
    public String getName()
    {
        return this.name().toLowerCase();
    }
}
