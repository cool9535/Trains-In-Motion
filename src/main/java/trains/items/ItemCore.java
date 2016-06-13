package trains.items;

import trains.TrainsInMotion;
import trains.entities.EntityTrainCore;
import com.mojang.authlib.GameProfile;
import mods.railcraft.api.carts.IMinecart;
import mods.railcraft.api.core.items.IMinecartItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import trains.gui.GUITest;

public class ItemCore extends ItemMinecart implements IMinecart, IMinecartItem {
    //constructor
    public ItemCore() {
        super(0);
        setCreativeTab(TrainsInMotion.creativeTab);
    }
    //if the item can be placed by a block or non-player entity
    @Override
    public boolean canBePlacedByNonPlayer(ItemStack cart) {
        return true;
    }
    //placing the cart
    @Override
    public EntityTrainCore placeCart(GameProfile owner, ItemStack cart, World world, int posX, int posY, int posZ) {
        return new EntityTrainCore(owner.getId(), world, posX + 0.5F, posY + 0.5F,posZ + 0.5F, 100f, new float[]{1, 5, 2}, 20, 1, GUITest.GUI_ID);
    }
    //trains shouldn't match a cart filter.
    @Override
    public boolean doesCartMatchFilter(ItemStack stack, EntityMinecart cart) {
        return false;
    }
    //create train on use
    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer playerEntity, World worldObj, int posX, int posY, int posZ, int blockSide, float pointToRayX, float raypointToRayY, float raypointToRayZ) {
        if (worldObj.isRemote) {
            return false;//returns whether or not to do animation placement.
        } else{
            // public EntityTrainCore(World world, double xPos, double yPos, double zPos, float maxSpeed, float[] acceleration, int inventorySlots, int type /*1-steam, 2-diesel, 3-electric*/)
            worldObj.spawnEntityInWorld(new EntityTrainCore(playerEntity.getGameProfile().getId(), worldObj, posX,posY,posZ, 120, new float[]{1,3,1},2,1,GUITest.GUI_ID));
          return true;
        }
    }
    //set icon for item
    /*/
    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        this.itemIcon = iconRegister.registerIcon(Info.modID.toLowerCase() + ":trains/" + this.iconName);
    }
    /*/

}
