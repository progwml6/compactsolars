package cpw.mods.compactsolars;

import java.util.Map;
import java.util.Random;

import com.google.common.collect.MapMaker;
import com.google.common.math.IntMath;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.util.EnumHelper;

public class ItemSolarHat extends ItemArmor implements ISpecialArmor
{
    private class PlayerState
    {
        boolean canRain;

        public long buildUp;

        public long lastTick;
    }

    private static Random random = new Random();

    private static Map<EntityPlayer, PlayerState> playerState = new MapMaker().weakKeys().makeMap();

    private CompactSolarType type;

    public ItemSolarHat(CompactSolarType type)
    {
        super(EnumHelper.addArmorMaterial("COMPACTSOLARHAT", type.hatTexture.toString(), 1, new int[] { 1, 1, 1, 1 }, 1, null, 0.0F), 0,
                EntityEquipmentSlot.HEAD);
        this.type = type;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type)
    {
        return this.type.hatTexture.toString();
    }

    @Override
    public void onArmorTick(World worldObj, EntityPlayer player, ItemStack itemStack)
    {
        // client side or no sky: no charge
        if (worldObj.isRemote || worldObj.provider.hasNoSky())
        {
            return;
        }

        // productionrate is set, and the tick is not zero : no charge
        if (CompactSolars.productionRate != 1 && random.nextInt(CompactSolars.productionRate) != 0)
        {
            return;
        }

        boolean isRaining = false;

        if (!ItemSolarHat.playerState.containsKey(player))
        {
            ItemSolarHat.playerState.put(player, new PlayerState());
        }

        PlayerState state = playerState.get(player);

        if (worldObj.getTotalWorldTime() % 20 == 0)
        {
            //@formatter:off
            boolean canRain = worldObj.getChunkFromBlockCoords(player.getPosition()).getBiome(player.getPosition(), worldObj.getBiomeProvider()).getRainfall() > 0;
            //@formatter:on
            state.canRain = canRain;
        }

        isRaining = state.canRain && (worldObj.isRaining() || worldObj.isThundering());

        boolean theSunIsVisible = worldObj.isDaytime() && !isRaining && worldObj.canSeeSky(player.getPosition().up());

        if (!theSunIsVisible)
        {
            return;
        }

        int available = this.type.getOutput();

        for (ItemStack stack : player.inventory.armorInventory)
        {
            if (stack == itemStack)
            {
                continue;
            }

            if (stack != null)
            {
                if (stack.getItem() instanceof IElectricItem)
                {
                    available -= ElectricItem.manager.charge(stack, available, this.type.ordinal() + 1, false, false);
                }
            }
        }

        if (available <= 0)
        {
            state.buildUp += IntMath.pow(2, this.type.ordinal());
        }
        else
        {
            state.buildUp = Math.max(state.buildUp - (worldObj.getTotalWorldTime() - state.lastTick), 0);
        }

        state.lastTick = worldObj.getTotalWorldTime();

        int dose = IntMath.pow(10, this.type.ordinal()) * 5;

        if (state.buildUp > dose)
        {
            player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, dose >> 2, 0));
            state.buildUp -= dose;
        }
    }

    public static void clearRaining()
    {
        ItemSolarHat.playerState.clear();
    }

    @Override
    public ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot)
    {
        return new ArmorProperties(0, 0, 0);
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot)
    {
        return 0;
    }

    @Override
    public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot)
    {
        return;
    }
}
