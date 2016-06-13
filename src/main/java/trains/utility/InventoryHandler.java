package trains.utility;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import trains.entities.EntityTrainCore;

public class InventoryHandler extends Container{
    private EntityTrainCore trainEntity;
    private int inventorySize;

    public InventoryHandler(InventoryPlayer iinventory, EntityTrainCore entityminecart) {
        trainEntity = entityminecart;
    }
    /*/
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int i) {
        return
    }
    /*/
    @Override
    public boolean canInteractWith(EntityPlayer var1) {
        if (trainEntity.isDead) {
            return false;
        }
        return true;
    }


}