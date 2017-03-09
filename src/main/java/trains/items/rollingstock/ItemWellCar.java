package trains.items.rollingstock;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import trains.TrainsInMotion;
import trains.entities.rollingstock.EntityVATLogCar;
import trains.entities.rollingstock.EntityWellCar;
import trains.registry.URIRegistry;
import trains.utility.RailUtility;

import java.util.List;


/**
 * <h2>VAT Log Car Item</h2>
 * for more information:
 * @see ItemPullmansPalace
 * @author Eternal Blue Flame
 */

public class ItemWellCar extends Item {

    private static final String weight = "\u00A77" + StatCollector.translateToLocal("menu.item.weight") +": 2" + StatCollector.translateToLocal("menu.item.tons");
    private static final String inventorySize = "\u00A77" + StatCollector.translateToLocal("menu.item.sizeof") +": 27" + StatCollector.translateToLocal("menu.item.slots");

    /**
     * <h2>constructor</h2>
     * set the creative tab and call the super
     */
    public ItemWellCar() {
        super();
        setCreativeTab(TrainsInMotion.creativeTab);
    }

    /**
     * <h2>item subtext</h2>
     * add description text to the item. To add a new line, add another entry to the list.
     */
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
        par3List.add(weight);
        par3List.add(inventorySize);
    }


    /**
     * <h2>Spawn the rollingstock</h2>
     * spawns the rollingstock when the player/entity tries to use it on a tile.
     */
    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer playerEntity, World worldObj, int posX, int posY, int posZ, int blockSide, float pointToRayX, float pointToRayY, float pointToRayZ) {
        return RailUtility.placeOnRail(new EntityWellCar(playerEntity.getGameProfile().getId(), worldObj, posX + 0.5D, posY, posZ + 0.5D),playerEntity,worldObj,posX,posY,posZ);
    }

    /**
     * <h2>Item icon</h2>
     * Sets the icon for the item, this shouldn't need to be changed.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        this.itemIcon = iconRegister.registerIcon(URIRegistry.ITEM_ROLLINGSTOCK_TEXTURE.getResource(this.getUnlocalizedName()).toString());
    }

}