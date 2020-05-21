package ebf.tim.utility;

import ebf.tim.blocks.TileEntityStorage;
import ebf.tim.entities.GenericRailTransport;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Eternal Blue Flame
 * TODO: add support for tile entities post 0.2.7.
 */
public class ItemStackSlot extends Slot {

    private ItemStack stack = null, overlay = null;
    private int slotID, maxCraft;
    private boolean isCrafting, input;

    public ItemStackSlot(IInventory host, int slot){
        super(host, slot, 0,0);
        slotNumber=slot;
        slotID=slot;
    }

    public ItemStackSlot setCrafting(boolean io){
        input=io;isCrafting=true;
        return this;
    }

    public boolean isCrafting() {
        return isCrafting;
    }

    public ItemStackSlot(IInventory host, int slot, int x, int y){
        super(host, slot, 0,0);
        slotID=slot;
        slotNumber=slot;
        xDisplayPosition=x;
        yDisplayPosition=y;
    }

    public ItemStackSlot setCoords(int x, int y){
        xDisplayPosition=x;
        yDisplayPosition=y;
        return this;
    }
    public ItemStackSlot setSlotID(int slot){
        slotID=slot;
        return this;
    }
    public ItemStackSlot setSlot(int slot){
        this.slotNumber=slot;
        return this;
    }

    public ItemStack getOverlay(){
        return overlay;
    }

    public ItemStackSlot setOverlay(ItemStack s){
        overlay=s;
        return this;
    }

    public ItemStackSlot setOverlay(Item s){
        overlay=new ItemStack(s);
        return this;
    }

    public ItemStackSlot setOverlay(Block s){
        overlay=new ItemStack(s);
        return this;
    }

    public Item getItem(){
        return getStack()!=null?getStack().getItem():null;
    }


    public NBTTagCompound writeToNBT(){
        return getStack()!=null?getStack().writeToNBT(new NBTTagCompound()):null;
    }

    public int getStackSize(){
        return getStack()!=null?getStack().stackSize:0;
    }


    public ItemStack mergeStack(ItemStack itemStack, List<ItemStackSlot> hostInventory){
        if (isItemValid(itemStack)) {

            if (!getHasStack()) {
                return setSlotContents(itemStack,hostInventory) ? null : itemStack;
            } else {
                if (contentEquals(itemStack) && getStack().stackSize < getStack().getMaxStackSize()) {
                    if (itemStack.stackSize <= getStack().getMaxStackSize() - getStack().stackSize) {
                        setSlotStacksize(getStack().stackSize+itemStack.stackSize);
                        return null;
                    } else {
                        itemStack.stackSize -= getStack().getMaxStackSize() - getStack().stackSize;
                        setSlotStacksize(getStack().getMaxStackSize());
                        if(itemStack.stackSize==0){
                            itemStack=null;
                        }
                        return itemStack;
                    }
                }
            }
        }
        return itemStack;
    }

    public ItemStack mergeStack(ItemStackSlot itemStack, List<ItemStackSlot> hostInventory, boolean max){
        if (isItemValid(itemStack.getStack())) {
            if (!getHasStack()) {
                setSlotContents(itemStack.getStack().copy(),hostInventory);
                itemStack.setSlotContents(null,hostInventory);
            } else {
                if (contentEquals(itemStack) && getStack().stackSize < getStack().getMaxStackSize()) {
                    if (itemStack.getStackSize() <= getStack().getMaxStackSize() - getStack().stackSize) {
                        setSlotStacksize(getStack().stackSize+itemStack.getStackSize());
                        itemStack.setSlotContents(null,hostInventory);
                    } else {
                        itemStack.decrStackSize(getStack().getMaxStackSize() - getStack().stackSize);
                        setSlotStacksize(getStack().getMaxStackSize());
                        return itemStack.getStack();
                    }
                } else {
                    return itemStack.getStack();
                }
            }
        }
        return null;
    }

    public boolean contentEquals(ItemStackSlot other){
        if(getStack()==null || other.getStack()==null){
            return getStack()==null&& other.getStack()==null;
        }
        return other.getStack().getItem()== getStack().getItem();
    }
    public boolean contentEquals(ItemStack other){
        if(getStack()==null || other==null){
            return getStack()==null&& other==null;
        }
        return other.getItem()== getStack().getItem();
    }

    public int getMaxCraft(IInventory hostInventory, List<ItemStackSlot> hostSlots){
        int value =0;
        if(isCrafting && hostInventory instanceof TileEntityStorage) {
            if(((TileEntityStorage)hostInventory).storageType==0){
                int size=0;
                Recipe r = RecipeManager.getRecipe(stack);
                for(int i=0;i<9; i++){
                    for(ItemStack s :r.getRecipeItems().get(i)){
                        if(s==null && getStack()==null){
                            continue;
                        } else if (s!=null && ((TileEntityStorage) hostInventory).getSlotIndexByID(400+i).stack!=null &&
                                s.getItem()==((TileEntityStorage) hostInventory).getSlotIndexByID(400+i).stack.getItem()){
                            value=Math.max(size,
                                    ((TileEntityStorage) hostInventory).getSlotIndexByID(400+i).stack.stackSize/s.stackSize);
                        }
                    }
                }
            } else {
                value=((TileEntityStorage) hostInventory).getSlotIndexByID(400).stack.stackSize;
            }
        }
        return value;
    }

    private void onCraftMatrixChanged(IInventory hostInventory, List<ItemStackSlot> hostSlots) {
        if(isCrafting && hostInventory instanceof TileEntityStorage) {
            int page = ((TileEntityStorage)hostInventory).outputPage;
            switch (((TileEntityStorage)hostInventory).storageType) {
                case 0: {
                    List<ItemStack> slots = RecipeManager.getResult(RecipeManager.getTransportRecipe(hostInventory));
                    if(slots==null){
                        for (int i = 0; i < 9; i++) {
                            putStackInSlot(hostSlots,400 + i, null);
                        }
                    } else {
                        if(slots.size()<10) {
                            for (int i = 0; i < 9; i++) {
                                putStackInSlot(hostSlots,409 + i, i >= slots.size() ?null: slots.get(i));
                            }
                            ((TileEntityStorage)hostInventory).multiPage=false;
                        } else {//when theres 10 or more outputs skip 2 since buttons will be in their place.
                            putStackInSlot(hostSlots,409 + (7*page), slots.get((7*page)));
                            putStackInSlot(hostSlots,410 + (7*page), slots.get(1+ (7*page)));
                            putStackInSlot(hostSlots,411 + (7*page), slots.get(2+ (7*page)));
                            //intentionally skip 412 because an arrow is there
                            putStackInSlot(hostSlots,413 + (7*page), slots.get(3+ (7*page)));
                            //intentionally skip 414 because an arrow is there
                            putStackInSlot(hostSlots,415 + (7*page), slots.get(4+ (7*page)));
                            putStackInSlot(hostSlots,416 + (7*page), slots.get(5+ (7*page)));
                            putStackInSlot(hostSlots,417 + (7*page), slots.get(6+ (7*page)));

                            ((TileEntityStorage)hostInventory).multiPage=true;
                        }

                    }
                    break;
                }
                case 1: {
                    putStackInSlot(hostSlots,406, RecipeManager.railRecipe(hostInventory));
                    break;
                }
            }
        }
    }

    public void onCrafting(IInventory hostInventory, List<ItemStackSlot> hostSlots, int stacksize){
        if(!isCrafting){return;}
        Recipe r = RecipeManager.getRecipe(getStack());
        if(r==null || r.input==null){return;}

        switch (((TileEntityStorage)hostInventory).storageType) {
            case 0: {
                for(int i=0;i<9;i++){
                    if(r.input.get(i)!=null) {
                        for (ItemStack s : r.input.get(i)) {
                            //DebugUtil.println(s==null?"null":stack.getDisplayName()+stack.stackSize+":"+stacksize);
                            if (slotMatchesItem(hostSlots, 400 + i, s)) {
                                shrinkStackInSlot(hostSlots, 400 + i, s == null ? 0 : stacksize * s.stackSize);
                                break;
                            }
                        }
                    }
                }
                break;
            }
            case 1:{
                for(int i=0;i<4;i++){
                    shrinkStackInSlot(hostSlots,400+i,stacksize);
                }
                break;
            }
        }
    }


    public static boolean slotMatchesItem(List<ItemStackSlot> hostSlots, int slot, ItemStack stack){
        ItemStackSlot stackSlot=null;
        for(ItemStackSlot stak: hostSlots){
            if (stak.getSlotIndex() ==slot){
                stackSlot=stak;
            }
        }
        if (stackSlot!=null) {
            if(stackSlot.getStack()==null || stack==null) {
                return stackSlot.getStack() == null && stack == null;
            } else {
                return stack.getItem()==stackSlot.getStack().getItem();
            }
        } else {
            return stack==null;
        }
    }

    public static void shrinkStackInSlot(List<ItemStackSlot> hostSlots, int slot, int size) {
        if(size<1){return;}
        ItemStackSlot stackSlot=null;
        for(ItemStackSlot stak: hostSlots){
            if (stak.getSlotIndex() ==slot){
                stackSlot=stak;
            }
        }
        if (stackSlot!=null) {
            stackSlot.decrStackSize(size);
        }
    }

    public void putStackInSlot(List<ItemStackSlot> hostSlots, int slot, ItemStack stack) {
        ItemStackSlot stackSlot=null;
        for(ItemStackSlot stak: hostSlots){
            if (stak.getSlotIndex() ==slot){
                stackSlot=stak;
            }
        }
        if (stackSlot!=null) {
            if (!(stackSlot.inventory instanceof GenericRailTransport) && !(stackSlot.inventory instanceof TileEntityStorage)) {
                stackSlot.inventory.setInventorySlotContents(slot, stack);
            } else {
                stackSlot.setStack(stack);
            }
        }
    }

    public boolean setSlotContents(@Nullable ItemStack stack, List<ItemStackSlot> hostInventory){
        if (isItemValid(stack) || stack == null) {
            if (!(inventory instanceof GenericRailTransport) && !(inventory instanceof TileEntityStorage)) {
                    inventory.setInventorySlotContents(slotNumber, stack);
            } else {
                this.stack = stack;
            }
            this.onSlotChanged();
            if(hostInventory!=null) {
                onCraftMatrixChanged(inventory, hostInventory);
            }
            return true;
        }
        return false;
    }


    public boolean setSlotStacksize(int size){
            if (!(inventory instanceof GenericRailTransport) && !(inventory instanceof TileEntityStorage)) {
                ItemStack s = getStack().copy();
                s.stackSize = size;
                inventory.setInventorySlotContents(slotNumber,s);
            } else if(this.stack!=null){
                this.stack.stackSize=size;
            }
            this.onSlotChanged();
            return true;
    }

    /**
     * if par2 has more items than par1, onCrafting(item,countIncrease) is called
     */
    public void onSlotChange(ItemStack p_75220_1_, ItemStack p_75220_2_) {
        if (p_75220_1_ != null && p_75220_2_ != null && p_75220_1_.getItem() == p_75220_2_.getItem()) {
            int i = p_75220_2_.stackSize - p_75220_1_.stackSize;

            if (i > 0) {
                this.onCrafting(p_75220_1_, i);
            }
        }
        this.inventory.markDirty();
    }

    /*
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. Typically increases an
     * internal count then calls onCrafting(item).
     protected void onCrafting(ItemStack p_75210_1_, int p_75210_2_) {}
     */

    /*
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood.
     protected void onCrafting(ItemStack p_75208_1_) {}
     */

    /**
     * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
     */
    @Override
    public boolean isItemValid(ItemStack p_75214_1_) {
        if(stack==null || stack.getItem()==null){return true;}
        if(isCrafting && !input){return stack==null;}
        return inventory.isItemValidForSlot(getSlotID(), p_75214_1_);
    }

    /**
     * Helper fnct to get the stack in the slot.
     */
    @Override
    public ItemStack getStack() {
        return inventory instanceof GenericRailTransport || inventory instanceof TileEntityStorage ?stack:
                inventory!=null && inventory.getSizeInventory()>slotNumber?
                        inventory.getStackInSlot(slotNumber):null;
    }

    /**
     * Helper method to put a stack in the slot.
     * @deprecated use {@link #setSlotContents(ItemStack)} instead because it can return whether ot not it actually did it.
     */
    @Override
    @Deprecated
    public void putStack(ItemStack p_75215_1_) {
        setSlotContents(p_75215_1_,null);
    }

    public void setStack(ItemStack p_75215_1_) {
        stack=p_75215_1_;
    }

    /**
     * Called when the stack in a Slot changes, only for tile entities.
     */
    @Override
    public void onSlotChanged() {
        inventory.markDirty();
    }

    /**
     * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the case
     * of armor slots)
     */
    @Override
    public int getSlotStackLimit() {
        return getHasStack()?getStack().getMaxStackSize():64;
    }

    /**
     * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
     * stack.
     */
    @Override
    public ItemStack decrStackSize(int p_75209_1_) {
        if(!getHasStack() ||p_75209_1_ >=getStack().stackSize){
            setSlotContents(null,null);
        } else {
            getStack().stackSize-=p_75209_1_;
        }
        return getStack();
    }

    /**
     * returns if the target Iinventory is from the same class as this
     */
    @Override
    @Deprecated
    public boolean isSlotInInventory(IInventory p_75217_1_, int p_75217_2_) {
        return inventory.getClass() == p_75217_1_.getClass();
    }

    /**
     * Return whether this slot's stack can be taken from this slot.
     */
    @Override
    public boolean canTakeStack(EntityPlayer p_82869_1_) {
        return !(inventory instanceof GenericRailTransport) || ((GenericRailTransport) inventory).getPermissions(p_82869_1_, false, false);
    }

    /**
     * Retrieves the index in the inventory for this slot, this value should typically not
     * be used, but can be useful for some occasions.
     *
     * @return Index in associated inventory for this slot.
     */
    @Override
    public int getSlotIndex() { return slotNumber; }


    public int getSlotID(){return slotID;}
}
