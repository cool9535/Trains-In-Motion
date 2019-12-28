package ebf.tim.entities;


import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ebf.tim.blocks.rails.BlockRailCore;
import ebf.tim.utility.RailUtility;
import io.netty.buffer.ByteBuf;
import mods.railcraft.api.carts.IMinecart;
import mods.railcraft.api.carts.IRoutableCart;
import mods.railcraft.api.tracks.ITrackSwitch;
import mods.railcraft.api.tracks.ITrackTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockRailBase;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * <h1>Bogie Core</h1>
 * this controls the behavior of the bogies in trains and rollingstock.
 * @author Eternal Blue Flame
 */
public class EntityBogie extends EntityMinecart implements IMinecart, IRoutableCart, IEntityAdditionalSpawnData {

    /** used to keep a reference to the parent train/rollingstock.*/
    private int parentId = 0;
    /**client velocity used to smooth actual movement, this is a replacement for the vanilla velocity variables which have private access.*/
    private double cartVelocityX =0;
    /**client velocity used to smooth actual movement, this is a replacement for the vanilla velocity variables which have private access.*/
    private double cartVelocityY =0;
    /**client velocity used to smooth actual movement, this is a replacement for the vanilla velocity variables which have private access.*/
    private double cartVelocityZ =0;
    /**client velocity multiplier used to smooth actual movement, this is a replacement for the vanilla turnProgress which has private access.*/
    private double motionProgress=0;
    /**defines if this is the front bogie of the transport*/
    private boolean isFront=true;
    /**used to calculate the X/Y/Z velocity based on the direction the rail is facing, similar to how vanilla minecarts work.*/
    private static final int[][][] vanillaRailMatrix = new int[][][] {{{0, 0, -1}, {0, 0, 1}}, {{ -1, 0, 0}, {1, 0, 0}}, {{ -1, -1, 0}, {1, 0, 0}}, {{ -1, 0, 0}, {1, -1, 0}}, {{0, 0, -1}, {0, -1, 1}}, {{0, -1, -1}, {0, 0, 1}}, {{0, 0, 1}, {1, 0, 0}}, {{0, 0, 1}, { -1, 0, 0}}, {{0, 0, -1}, { -1, 0, 0}}, {{0, 0, -1}, {1, 0, 0}}};
    /*used by the transport class to apply a weight multiplication if the bogies are on a slope*/
    public boolean isOnSlope=false;

    /**instancing of this here so java GC doesn't have to keep cleaning and remaking it.*/
    private double dragMultiplier;

    /**cached value for the bogie path, prevents need to generate a new variable multiple times per tick*/
    private double[] motionPath;
    /**cached value for the rail path, prevents need to generate a new variable multiple times per tick*/
    private double railPathX;
    /**cached value for the rail path, prevents need to generate a new variable multiple times per tick*/
    private double railPathZ;
    /**cached value for the rail path, prevents need to generate a new variable multiple times per tick*/
    private double railPathSqrt;
    /**cached value for the rail path, prevents need to generate a new variable multiple times per tick*/
    private double motionSqrt;
    /**cached value for the rail path, prevents need to generate a new variable multiple times per tick*/
    private double railPathX2;
    /**cached value for the rail path, prevents need to generate a new variable multiple times per tick*/
    private double railPathZ2;
    /**cached value for the rail path, prevents need to generate a new variable multiple times per tick*/
    private double railPathDirection;
    /**cached value for the rail path, prevents need to generate a new variable multiple times per tick*/
    private int railMetadata;
    /**cached value for the rail path, prevents need to generate a new variable multiple times per tick*/
    private Block blockNext;

    public EntityBogie(World world) {
        super(world);
        yOffset=0.2f;
    }

    /**
     * used to initialize the entity
     * @param parent The EntityID of the parent entity (Must extend GenericRailTransport).
     * @param front whether or not this is the front bogie.
     */
    public EntityBogie(World world, double xPos, double yPos, double zPos, int parent, boolean front) {
        super(world);
        posX = xPos;
        posY = yPos;
        posZ = zPos;
        parentId = parent;
        isFront = front;
        yOffset=0.2f;
    }

    /**Small networking check to add the bogie to the host train/rollingstock. Or to remove the bogie from the world if the host doesn't exist.*/
    @Override
    public void readSpawnData(ByteBuf additionalData) {
        isFront = additionalData.readBoolean();
        parentId = additionalData.readInt();
    }
    /**sends the networking check on spawn/respawn so this can see if it should exist in the first place.*/
    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeBoolean(isFront);
        buffer.writeInt(parentId);
    }

    /**plays a sound during entity movement*/
    @Override
    protected void func_145780_a(int p_145780_1_, int p_145780_2_, int p_145780_3_, Block p_145780_4_) {}

    /**used by the game to tell different types of minecarts from eachother, this doesnt effect us, so just use something random*/
    @Override
    public int getMinecartType() {
        return 10001;
    }
    /**returns if the cart can make itself move*/
    @Override
    public boolean isPoweredCart() {
        return true;
    }
    /**returns if the cart can be ridden*/
    @Override
    public boolean canBeRidden() {
        return false;
    }
    /**if the cart can be pushed by an entity*/
    @Override
    public boolean canBePushed() {
        return false;
    }
    /**returns if the rider can interact with this, I don't think this makes any difference given the design of the bogie*/
    @Override
    public boolean canRiderInteract() {
        return false;
    }
    /**defines the max speed the minecart can move on a rail that is not an extension of BlockRailBase*/
    @Override
    public float getMaxCartSpeedOnRail() {
        return 1.2F;
    }
    /**defines the update tick of the entity, in this case we rely on the transport to provide that for us, keeps things synced on the chance entities ever get individualized threads*/
    @Override
    public void onUpdate() {
        //be sure to remove this if the parent is null, or in a different castle, I mean world.
        if (worldObj.getEntityByID(parentId) instanceof GenericRailTransport){
            if (worldObj.isRemote) {
                ((GenericRailTransport) worldObj.getEntityByID(parentId)).setBogie(this, isFront);
            }
        } else {
            worldObj.removeEntity(this);
        }
    }
    /**returns if this can be collided with, since we don't process collisions, we return false*/
    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
    /**disables reading from NBT*/
    @Override
    public void readFromNBT(NBTTagCompound tag){}
    /**disables writing to NBT, which kills the entity on game end.*/
    @Override
    public void writeToNBT(NBTTagCompound tag){}
    @Override
    public boolean writeToNBTOptional(NBTTagCompound tagCompound){return false;}
    @Override
    public boolean writeMountToNBT(NBTTagCompound tagCompound){return false;}

    /**
     * <h2> movement management</h2>
     * this is modified movement from the super class, should be more efficient, and reliable, but generally does the same thing, minus ability to collide.
     * @see EntityMinecart#onUpdate()
     * Some features are replaced using our own for compatibility with ZoraNoDensha
     * @see RailUtility
     * returns true or false depending on whether or not it derails from having no rail.
     */
    public boolean minecartMove(GenericRailTransport host, boolean hasDrag, boolean parking,  float weight)   {
        //define the yaw from the super
        this.setRotation(host.rotationYaw, host.rotationPitch);

        //client only, update position
        if (this.worldObj.isRemote) {
            if (motionProgress > 0) {
                this.posX += (prevPosX - this.posX) / motionProgress;
                this.posY += (((prevPosY - this.posY) + yOffset) / motionProgress);
                this.posZ += (prevPosZ - this.posZ) / motionProgress;
                --motionProgress;
            }
        }
        //server only
        else {
            //update old position, add the gravity, and get the block below this,
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            int floorX = MathHelper.floor_double(this.posX);
            int floorY = MathHelper.floor_double(this.posY);
            int floorZ = MathHelper.floor_double(this.posZ);
            //compensate for y offsets based on current position, just in case the movement is too steep.
            if (!BlockRailBase.func_150049_b_(this.worldObj, floorX, floorY, floorZ)) {
                if (BlockRailBase.func_150049_b_(this.worldObj, floorX, floorY - 1, floorZ)) {
                    --floorY;
                } else if (worldObj.getBlock(floorX, floorY, floorZ) instanceof BlockAir && worldObj.getBlock(floorX, floorY-1, floorZ) instanceof BlockAir && posY>-64){
                    posY-=0.3D;
                }
            }

            //apply parking brake
            if (parking){
                if (motionX <0.005 && motionX >-0.005){
                    this.cartVelocityX = motionX =0;
                } else {
                    motionX *= 0.9-(0.01* (weight * 0.0007457));
                    this.cartVelocityX *= 0.9-(0.01* (weight * 0.0007457));
                }
                if (motionZ <0.005 && motionZ >-0.005){
                    this.cartVelocityZ =motionZ =0;
                } else {
                    motionZ *= 0.9-(0.01* (weight * 0.0007457));
                    this.cartVelocityZ *= 0.9-(0.01* (weight * 0.0007457));
                }
            }

            dragMultiplier = Math.pow(weight,-0.07457);



            Block block = worldObj.getBlock(floorX, floorY, floorZ);
            host.onVanillaRails=false;
            //update on normal rails
            if (block instanceof BlockRailBase) {
                host.onVanillaRails=true;
                this.yOffset=(block instanceof BlockRailCore?0.2f:0.125f);
                segmentMovement(Math.abs(motionX)+Math.abs(motionZ),((BlockRailBase)block).getRailMaxSpeed(worldObj, this, floorY, floorX, floorZ),
                       hasDrag, floorX, floorY, floorZ, (BlockRailBase) block, host);
                //update on ZnD rails, and ones that don't extend block rail base.
                //todo ZnD support, either by jar reference or API update
            //} else if (block instanceof ITrackBase) {
                //update position for ZnD rails.
                //moveBogieZnD(motionX, motionZ, floorX, floorY, floorZ, (ITrackBase) block);
            } else {
                return true;
            }
        }
        return false;
    }



    private void segmentMovement(double velocity, double speedCap, boolean hasDrag, int floorX, int floorY, int floorZ, BlockRailBase block, GenericRailTransport host){

        //todo something with speedcap

        //add the uphill/downhill velocity
        switch (railMetadata){
            //todo replace with a similar calculation to the drag multiplier
            case 2:{motionX -= 0.0078125D*dragMultiplier; this.posY = (floorY + 1d); isOnSlope=true; break;}
            case 3:{motionX += 0.0078125D*dragMultiplier; this.posY = (floorY + 1d); isOnSlope=true; break;}
            case 4:{motionZ += 0.0078125D*dragMultiplier; this.posY = (floorY + 1d); isOnSlope=true; break;}
            case 5:{motionZ -= 0.0078125D*dragMultiplier; this.posY = (floorY + 1d); isOnSlope=true; break;}
            default:{
                isOnSlope=false;
                //apply drag
                if (hasDrag){
                    if (motionX <0.005 && motionX >-0.005){
                        this.cartVelocityX = motionX =0;
                    } else {
                        motionX *= dragMultiplier;
                        this.cartVelocityX *= dragMultiplier;
                    }
                    if (motionZ <0.005 && motionZ >-0.005){
                        this.cartVelocityZ =motionZ =0;
                    } else {
                        motionZ *= dragMultiplier;
                        this.cartVelocityZ *= dragMultiplier;
                    }
                }
            }
        }

        moveBogieVanillaDirectional(velocity, floorX,floorY,floorZ, block, host);

        if(velocity>0.3){
            velocity-=0.3;
        } else if(velocity<-0.3){
            velocity+=0.3;
        } else {
            return;
        }

        //update the last used block to the one we just used, if it's actually different.
        floorX = MathHelper.floor_double(this.posX);
        floorY = MathHelper.floor_double(this.posY);
        floorZ = MathHelper.floor_double(this.posZ);
        blockNext = this.worldObj.getBlock(floorX, floorY, floorZ);
        //now loop this again for the next increment of movement, if there is one
        if (blockNext instanceof BlockRailBase) {
            segmentMovement(velocity, speedCap, hasDrag, floorX, floorY, floorZ, (BlockRailBase) blockNext, host);
        }

    }


    private void moveBogieVanillaDirectional(double currentMotion, int floorX, int floorY, int floorZ, BlockRailBase block, GenericRailTransport host){
        //get the direction of the rail from it's metadata
        if (worldObj.getTileEntity(floorX, floorY, floorZ) instanceof ITrackTile && (((ITrackTile)worldObj.getTileEntity(floorX, floorY, floorZ)).getTrackInstance() instanceof ITrackSwitch)){
            railMetadata =((ITrackTile)worldObj.getTileEntity(floorX, floorY, floorZ)).getTrackInstance().getBasicRailMetadata(this);//railcraft support
        } else {
            railMetadata = block.getBasicRailMetadata(worldObj, this, floorX, floorY, floorZ);
        }


        //figure out the current rail's direction
        railPathX = (vanillaRailMatrix[railMetadata][1][0] - vanillaRailMatrix[railMetadata][0][0])*0.5;
        railPathZ = (vanillaRailMatrix[railMetadata][1][2] - vanillaRailMatrix[railMetadata][0][2])*0.5;
        railPathSqrt = Math.sqrt(railPathX * railPathX + railPathZ * railPathZ);

        if (motionX * railPathX + motionZ * railPathZ < 0.0D) {
            railPathX = -railPathX;
            railPathZ = -railPathZ;
        }

        motionPath=RailUtility.rotatePoint(currentMotion,
                host.rotationPitch, (Math.atan2((posZ+railPathZ)-posZ,(posX+railPathX)-posX)*(180d/Math.PI)));

        motionSqrt = Math.sqrt(motionX * motionX + motionZ * motionZ);
        motionX = motionSqrt * (railPathX / railPathSqrt);
        motionZ = motionSqrt * (railPathZ / railPathSqrt);

        //define the rail path again, to center the transport.
        railPathX2 = Math.floor(posX) + 0.5D + vanillaRailMatrix[railMetadata][0][0] * 0.5D;
        railPathZ2 = Math.floor(posZ) + 0.5D + vanillaRailMatrix[railMetadata][0][2] * 0.5D;
        railPathX = (Math.floor(posX) + 0.5D + vanillaRailMatrix[railMetadata][1][0] * 0.5D) - railPathX2;
        railPathZ = (Math.floor(posZ) + 0.5D + vanillaRailMatrix[railMetadata][1][2] * 0.5D) - railPathZ2;

        //pick the bigger one
        if (railPathX == 0.0D) {
            railPathDirection = this.posZ - floorZ;
        } else if (railPathZ == 0.0D) {
            railPathDirection = this.posX - floorX;
        } else {
            railPathDirection = ((this.posX - railPathX2) * railPathX + (this.posZ - railPathZ2) * railPathZ) * 2.0D;
        }
        //do the centering movement
        this.posX = (railPathX2 + railPathX * railPathDirection)+motionPath[0];
        this.posZ = (railPathZ2 + railPathZ * railPathDirection)+motionPath[2];

        this.prevPosY =motionY;
        //set the Y position
        if (vanillaRailMatrix[railMetadata][0][1] != 0 && MathHelper.floor_double(this.posX) - floorX == vanillaRailMatrix[railMetadata][0][0] && MathHelper.floor_double(this.posZ) - floorZ == vanillaRailMatrix[railMetadata][0][2]) {
            this.posY+=vanillaRailMatrix[railMetadata][0][1];
        }
        else if (vanillaRailMatrix[railMetadata][1][1] != 0 && MathHelper.floor_double(this.posX) - floorX == vanillaRailMatrix[railMetadata][1][0] && MathHelper.floor_double(this.posZ) - floorZ == vanillaRailMatrix[railMetadata][1][2]) {
            this.posY+=vanillaRailMatrix[railMetadata][1][1];
        }
        //endMagic();

        //do the rail functions.
        if(shouldDoRailFunctions()) {
            block.onMinecartPass(worldObj, this, floorX, floorY, floorZ);
        }
        if (block == Blocks.activator_rail) {
            this.onActivatorRailPass(floorX, floorY, floorZ, (worldObj.getBlockMetadata(floorX, floorY, floorZ) & 8) != 0);
        }
    }


    /*
    //todo ZnD support, either by jar reference or API update
    private void moveBogieZnD(double currentMotionX, double currentMotionZ, int floorX, int floorY, int floorZ, ITrackBase track){
        double[][] posVec6 = track.getPositionOnTrack(this);
        posX = posVec6[0][0];
        posY = posVec6[0][1];
        posZ = posVec6[0][2];
        //6[0] is xyz
        //6[1] is rotations
        //System.out.println(track.getDirectionOfSection().toString() + ":::" + track.getOrientation());
    }*/



    /*
     * <h1>Railcraft Functionality</h1>
     */


    /**checks if the parent transport matches the filter for railcraft items, if the filter is null or the parent is null, return false.*/
    @Override
    public boolean doesCartMatchFilter(ItemStack stack, EntityMinecart cart) {
        if (stack == null || worldObj.getEntityByID(parentId) == null) {
            return false;
        } else {
            Item cartItem = ((GenericRailTransport)worldObj.getEntityByID(parentId)).getItem();
            return cartItem != null && stack.getItem() == cartItem;
        }
    }
    /**returns the gameprofile of the owner for railcraft.*/
    @Override
    public GameProfile getOwner(){
        if (worldObj.getEntityByID(parentId) instanceof GenericRailTransport){
            GenericRailTransport parent = (GenericRailTransport) worldObj.getEntityByID(parentId);
            if (parent.getOwner() != null){
                return parent.getOwner();
            }
        }
        return null;
    }
    /**returns the destination of the parent for railcraft*/
    @Override
    public String getDestination() {
        if (worldObj.getEntityByID(parentId) instanceof GenericRailTransport){
            GenericRailTransport parent = (GenericRailTransport) worldObj.getEntityByID(parentId);
            if (parent.getOwner() != null){
                return parent.destination;
            }
        }
        return "";
    }
    /**this is supposed to set the ticket for the railcraft destination, but we don't use that, so we return false and do nothing.*/
    @Override
    public boolean setDestination(ItemStack ticket) {
        return false;
    }


    /*
     * <h2>Client Movement code</h2>
     */

    /**used to update positioning on client, and update the velocity multiplier*/
    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int turnProgress) {
        prevPosX = x;
        prevPosY = y;
        prevPosZ = z;
        motionX = cartVelocityX;
        motionY = cartVelocityY;
        motionZ = cartVelocityZ;
        motionProgress= turnProgress+2;
    }

    @Override
    public void setVelocity(double x, double y, double z) {
        cartVelocityX = motionX = x;
        cartVelocityY = motionY = y;
        cartVelocityZ = motionZ = z;
        isAirBorne = true;
    }
    /**used to add to the current velocity movement, also sets this as airborne*/
    @Override
    public void addVelocity(double velocityX, double velocityY, double velocityZ){
        setVelocity(motionX + velocityX, motionY + velocityY, motionZ + velocityZ);
    }
    /**override of the super method just so we can set the position without updating the hitbox, because we don't need to.*/
    @Override
    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }


}