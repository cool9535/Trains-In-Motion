package ebf.tim.utility;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ebf.tim.blocks.BlockDynamic;
import ebf.tim.blocks.TileEntityStorage;
import ebf.tim.entities.EntityTrainCore;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.items.ItemRail;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author EternalBlueFlame
 */
public class TransportSlotManager extends net.minecraft.inventory.Container {

    public IInventory hostInventory;

    public List<ItemStackSlot> inventory = new ArrayList<>();

    //todo: add support for some way to define slot filters
    public void addSlots(ItemStackSlot slot){
        this.inventory.add(slot);
        this.inventorySlots.add(slot);
        this.inventoryItemStacks.add(slot.getStack());
    }

    /**
     * <h2>Server-side inventory GUI for trains and rollingstock</h2>
     * works as the middleman between the client GUI and the entity on client and server.
     *
     * this mostly just runs loops to add and place all the inventory slots that will appear and can be used on client.
     */
    public TransportSlotManager(InventoryPlayer iinventory, GenericRailTransport entityTrain) {
        //transport reference
        hostInventory = entityTrain;

        for(ItemStackSlot slot : entityTrain.inventory){
            addSlots(slot);
        }
        //player toolbar
        for (int iT = 0; iT < 9; iT++) {
            addSlots(new ItemStackSlot(iinventory, iT).setCoords( 113 + (iT * 18), 142));
        }
        //player inventory
        for (int ic = 0; ic < 9; ic++) {
            for (int ir = 0; ir < 3; ir++) {
                addSlots(new ItemStackSlot(iinventory, ((ir * 9) + ic) + 9, 113 + (ic * 18), 84 + (ir * 18)));
            }
        }
    }

    public TransportSlotManager(InventoryPlayer iinventory, TileEntityStorage block) {
        //tile entity reference
        hostInventory = block;

        for(ItemStackSlot slot : block.inventory){
            addSlots(slot);
        }

        //player toolbar
        for (int iT = 0; iT < 9; iT++) {
            addSlots(new ItemStackSlot(iinventory, iT, 8 + iT * 18, 142));
        }

        //player inventory
        for (int ic = 0; ic < 9; ic++) {
            for (int ir = 0; ir < 3; ir++) {
                addSlots(new ItemStackSlot(iinventory, ((ir * 9) + ic) + 9, 8 + (ic * 18), 84 + (ir * 18)));
            }
        }
        onCraftMatrixChanged(hostInventory);
    }

    @Override
    public Slot getSlot(int p_75139_1_) {
        return this.inventory.get(p_75139_1_);
    }


    /**
     * <h2>Inventory sorting and shift-clicking</h2>
     * sorts items from the players inventory to the entity's inventory, and the reverse.
     * This happens with shift click and during some other circumstances.
     * We manage player inventory first because we bound it first, plus it's more reliable to be the size we expected.
     */
    @Override
    @Deprecated
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        DebugUtil.println("something is using transfer stack, this is bad");
        return null;
    }

    /**modified from 1.7.10 version to check if the item is valid for the slot*/
    @Override
    protected boolean mergeItemStack(ItemStack itemStack, int startIndex, int endIndex, boolean reverseDirection) {
        DebugUtil.println("something is using merge stack, this is bad");
        return false;
    }


    @SideOnly(Side.CLIENT)
    public void putStacksInSlots(ItemStack[] p_75131_1_) {
        for (int i = 0; i < p_75131_1_.length; ++i) {
            this.getSlot(i).putStack(p_75131_1_[i]);
        }
    }





    private int dragEvent;
    private final List<ItemStackSlot> dragSlots = new ArrayList<ItemStackSlot>();
    private int dragMode =-1;


    private ItemStackSlot getSlotByID(int id){
        for(ItemStackSlot slot: inventory){
            if (slot.getSlotIndex() ==id){
                return slot;
            }
        }
        return null;
    }

    /*a heavily modified replica of the 1.12 version*/
    @Override
    public ItemStack slotClick(int slotId, int dragType, int clickTypeIn, EntityPlayer player) {

        if (clickTypeIn == 4){
            clickTypeIn = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) ? 1 ://cover shift click
                    player.inventory.getItemStack() != null ? 4 : //cover if the cursor is carrying an item
                            (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))?3://cover CTRL clicking
                                    0;//cover everything else
        }
        ItemStack itemstack = null;;
        ItemStackSlot slot = getSlotByID(slotId);

        switch (clickTypeIn) {
            case 0: {/*ClickType.PICKUP    aka normal pickup and put down*/
                if (slotId == -999) { //if the slot was the cursor
                    if (player.inventory.getItemStack() != null) {
                        if (dragType == 0) {
                            if (!player.worldObj.isRemote) {
                                player.entityDropItem(player.inventory.getItemStack(), player.inventory.getItemStack().stackSize);
                            }
                            player.inventory.setItemStack(null);
                        } else if (dragType == 1 && !player.worldObj.isRemote) {
                            player.entityDropItem(player.inventory.getItemStack(), 1);
                        }
                    }
                } else {
                    if (slot == null) {
                        return null;
                    } else if (slot.getHasStack() && player.inventory.getItemStack() == null) {
                        player.inventory.setItemStack(slot.getStack());
                        if (player.inventory.getItemStack() != null) {
                            slot.setSlotContents(null);
                            slot.onCraftMatrixChanged(hostInventory,inventory);
                        }
                    } else {
                        player.inventory.setItemStack(slot.mergeStack(hostInventory,inventory,player.inventory.getItemStack()));
                    }
                }
                break;
            }
            case 1:{/*ClickType.QUICK_MOVE    aka shift click*/
                if (slot == null) {
                    return null;
                }

                if(slotId<36 || slotId==-999){//if the selected slot was in player inventory or on the cursor

                    //try the crafting slots
                    for(ItemStackSlot s : inventory){
                        if(s.getSlotID()>399){
                            slot.setSlotContents(s.mergeStack(hostInventory,inventory,slot.getStack()));
                            if (slot.getStack() == null){return null;}
                        }
                    }
                    //try the storage
                    for(ItemStackSlot s : inventory){
                        if(s.getSlotID()>35){
                            slot.setSlotContents(s.mergeStack(hostInventory,inventory,slot.getStack()));
                            if (slot.getStack() == null){return null;}
                        }
                    }
                    //all else fails, go back to the players...
                    for(ItemStackSlot s : inventory){
                        if(s.getSlotID()<36){
                            slot.setSlotContents(s.mergeStack(hostInventory,inventory,slot.getStack()));
                            if (slot.getStack() == null){return null;}
                        }
                    }

                } else if(slotId<400){//if the selected slot was in transport inventory
                    //try the crafting slots
                    for(ItemStackSlot s : inventory){
                        if(s.getSlotID()>399){
                            slot.setSlotContents(s.mergeStack(hostInventory,inventory,slot.getStack()));
                            if (slot.getStack() == null){return null;}
                        }
                    }

                    //try the players
                    for(ItemStackSlot s : inventory){
                        if(s.getSlotID()<36){
                            slot.setSlotContents(s.mergeStack(hostInventory,inventory,slot.getStack()));
                            if (slot.getStack() == null){return null;}
                        }
                    }
                    //all else fails, go back to the storage
                    for(ItemStackSlot s : inventory){
                        if(s.getSlotID()>35){
                            slot.setSlotContents(s.mergeStack(hostInventory,inventory,slot.getStack()));
                            if (slot.getStack() == null){return null;}
                        }
                    }

                } else {//if the selected slot was in transport fuel/crafting slots

                    //try the players
                    for(ItemStackSlot s : inventory){
                        if(s.getSlotID()<36){
                            slot.setSlotContents(s.mergeStack(hostInventory,inventory,slot.getStack()));
                            if (slot.getStack() == null){return null;}
                        }
                    }
                    //try the storage
                    for(ItemStackSlot s : inventory){
                        if(s.getSlotID()>35){
                            slot.setSlotContents(s.mergeStack(hostInventory,inventory,slot.getStack()));
                            if (slot.getStack() == null){return null;}
                        }
                    }

                }
                break;
            }
            case 2: {/*ClickType.SWAP*/
                if (dragType >= 0 && dragType < 9){
                    itemstack = player.inventory.getStackInSlot(dragType);
                    if (itemstack != null || (slot!= null && slot.getStack() != null)) {
                        if (itemstack == null) {
                            if (slot.canTakeStack(player)) {
                                player.inventory.setInventorySlotContents(dragType, slot.getStack());
                                slot.setSlotContents(null);
                            }
                        } else if (slot != null) {
                            player.inventory.setInventorySlotContents(dragType, slot.mergeStack(hostInventory,inventory,itemstack.splitStack(slot.getSlotStackLimit())));
                        }
                    }
                }
                break;
            }
            case 3: { /*ClickType.CLONE*/
                if (player.capabilities.isCreativeMode && player.inventory.getItemStack() == null && slotId >= 0 && slot != null && slot.getHasStack()) {
                    itemstack = slot.getStack().copy();
                    itemstack.stackSize =itemstack.getMaxStackSize();
                    player.inventory.setItemStack(itemstack);
                    return itemstack;
                }
                break;
            }
            case 4: { /*ClickType.THROW*/
                if (player.inventory.getItemStack() == null && slotId >= 0 && slot != null && slot.getHasStack() && slot.canTakeStack(player)) {
                    itemstack = slot.decrStackSize(dragType == 0 ? 1 : slot.getStack().stackSize);
                    if (!player.worldObj.isRemote) {
                        player.entityDropItem(itemstack, itemstack.stackSize);
                    }
                }
                break;
            }
            case 5 : {/*ClickType.QUICK_CRAFT*/

                //TODO: if crafting slot, loop until slot output is null.
                    int j1 = this.dragEvent;
                    this.dragEvent = dragType & 3;

                    if (((j1 != 1 || this.dragEvent != 2) && j1 != this.dragEvent) || player.inventory.getItemStack() == null) {
                        this.dragEvent = 0;
                        this.dragSlots.clear();
                    } else if (this.dragEvent == 0) {
                        this.dragMode = dragType >> 2 & 3;

                        if ((this.dragMode <= 1 || (this.dragMode == 2 && player.capabilities.isCreativeMode))) {
                            this.dragEvent = 1;
                            this.dragSlots.clear();
                        } else {
                            this.dragEvent = 0;
                            this.dragSlots.clear();
                        }
                    } else if (this.dragEvent == 1) {
                        if (slot != null &&
                                slot.isItemValid(player.inventory.getItemStack()) && (this.dragMode == 2 || player.inventory.getItemStack().stackSize > this.dragSlots.size())) {
                            this.dragSlots.add(slot);
                        }
                    } else if (this.dragEvent == 2) {
                        if (!this.dragSlots.isEmpty()) {
                            int k1 = player.inventory.getItemStack().stackSize;

                            for (ItemStackSlot slot2 : this.dragSlots) {
                                if (slot2 != null && (this.dragMode == 2 || player.inventory.getItemStack().stackSize >= this.dragSlots.size())) {
                                    ItemStack itemstack14 = player.inventory.getItemStack().copy();
                                    int j3 = slot2.getHasStack() ? slot2.getStack().stackSize : 0;
                                    computeStackSize(this.dragSlots, this.dragMode, itemstack14, j3);
                                    int k3 = Math.min(itemstack14.getMaxStackSize(), slot2.getSlotStackLimit());

                                    if (itemstack14.stackSize > k3) {
                                        itemstack14.stackSize = k3;
                                    }

                                    k1 -= itemstack14.stackSize - j3;
                                    slot2.setSlotContents(itemstack14);
                                }
                            }
                            if (k1 !=0) {
                                player.inventory.getItemStack().stackSize = k1;
                            } else {
                                player.inventory.setItemStack(null);
                            }
                        }
                        this.dragEvent = 0;
                        this.dragSlots.clear();
                    } else {
                        this.dragEvent = 0;
                        this.dragSlots.clear();
                    }
                    break;
                }
                default:{
                    if (this.dragEvent != 0) {
                        this.dragEvent = 0;

                        this.dragSlots.clear();
                    }
                }
            case 6: {
                if (slotId >= 0) {
                    ItemStack itemstack1 = player.inventory.getItemStack();

                    if (itemstack1 != null && (slot == null || !slot.getHasStack() || !slot.canTakeStack(player))) {
                        int i = dragType == 0 ? 0 : this.inventory.size() - 1;
                        int j = dragType == 0 ? 1 : -1;

                        for (int k = 0; k < 2; ++k) {
                            for (int l = i; l >= 0 && l < this.inventory.size() && itemstack1.stackSize < itemstack1.getMaxStackSize(); l += j) {
                                Slot slot1 = getSlotByID(l);

                                if (slot1 != null && slot1.getHasStack() && canAddItemToSlot(slot1, itemstack1) && slot1.canTakeStack(player)) {
                                    ItemStack itemstack2 = slot1.getStack();

                                    if (k != 0 || itemstack2.stackSize != itemstack2.getMaxStackSize()) {
                                        int i1 = Math.min(itemstack1.getMaxStackSize() - itemstack1.stackSize, itemstack2.stackSize);
                                        ItemStack itemstack3 = slot1.decrStackSize(i1);
                                        itemstack1.stackSize += i1;

                                        if (itemstack3 == null) {
                                            slot1.putStack(null);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    this.detectAndSendChanges();
                }
            }


        }


        if (player.inventory.getItemStack() != null && player.inventory.getItemStack().stackSize ==0){
            player.inventory.setItemStack(null);
        }
        return itemstack;

    }



    /*a modified replica of the 1.12 version*/
    public static boolean canAddItemToSlot(@Nullable Slot slotIn, ItemStack stack) {
        boolean flag = slotIn == null || !slotIn.getHasStack();

        if (!flag && stack.isItemEqual(slotIn.getStack()) && ItemStack.areItemStackTagsEqual(slotIn.getStack(), stack)) {
            return slotIn.getStack().stackSize <= stack.getMaxStackSize();
        } else {
            return flag;
        }
    }

    /*a modified replica of the 1.12 version*/
    public static void computeStackSize(List<ItemStackSlot> dragSlotsIn, int dragModeIn, ItemStack stack, int slotStackSize) {
        switch (dragModeIn) {
            case 0: {
                stack.stackSize = (MathHelper.floor_float(stack.stackSize / dragSlotsIn.size()));
                break;
            }
            case 1: {
                stack.stackSize = 1;
                break;
            }
            case 2: {
                stack.stackSize = (stack.getMaxStackSize());
            }
        }
        stack.stackSize +=(slotStackSize);
    }


    @Override
    public void onContainerClosed(EntityPlayer p_75134_1_) {
        super.onContainerClosed(p_75134_1_);
        this.hostInventory.closeInventory();
    }

    /**
     * <h2>can interact with inventory</h2>
     * just a simple return true/false if the train is dead, or if the owner locked the transport and the player trying to access isn't the owner.
     * it's also a null check to be sure that no one tries to interact with an errored entity.
     */
    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return (hostInventory instanceof GenericRailTransport?((GenericRailTransport)hostInventory).getPermissions(player, hostInventory instanceof EntityTrainCore, false): hostInventory!=null);
    }
}
