package ebf.tim.entities.rollingstock;

import ebf.tim.TrainsInMotion;
import ebf.tim.api.RollingstockBase;
import ebf.tim.entities.trains.EntityBrigadelok080;
import ebf.tim.items.ItemTransport;
import ebf.tim.models.Bogie;
import ebf.tim.models.rollingstock.UP3Bay100TonHopper;
import fexcraft.tmt.slim.ModelBase;
import fexcraft.tmt.slim.Vec3d;
import ebf.tim.registry.URIRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static ebf.tim.registry.TransportRegistry.GenericCMDBogie;

/**
 * <h1>Union Pacific 3-Bay 100-Ton Hopper entity</h1>
 * For more information on the overrides and functions:
 * @see EntityBrigadelok080
 * @author Eternal Blue Flame
 */
public class EntityUP3Bay100TonHopper extends RollingstockBase {

    public static final String[] itemDescription = new String[]{
            "\u00A77" + StatCollector.translateToLocal("menu.item.weight") +": 2" + StatCollector.translateToLocal("menu.item.tons"),
            "\u00A77" + StatCollector.translateToLocal("menu.item.sizeof") +": 27" + StatCollector.translateToLocal("menu.item.slots")};
    public static final Item thisItem = new ItemTransport(new EntityUP3Bay100TonHopper(null)).setUnlocalizedName("hoppercar");

    public EntityUP3Bay100TonHopper(UUID owner, World world, double xPos, double yPos, double zPos) {
        super(owner, world, xPos, yPos, zPos);
    }
    public EntityUP3Bay100TonHopper(World world){
        super(world);
    }

    /**
     * <h1>Variable Overrides</h1>
     */

    /**
     * <h2>Bogie Offset</h2>
     */
    @Override
    public List<Double> getRenderBogieOffsets(){return  Arrays.asList(-1.1, 1.1);}
    @Override
    public int bogieLengthFromCenter(){return 1;}

    @Override
    public float getRenderScale() {
        return 0.0625f;
    }

    @Override
    public boolean isReinforced() {
        return false;
    }

    @Override
    public int getTankCapacity() {
        return 0;
    }

    @Override
    public int getRFCapacity() {
        return 0;
    }

    @Override
    public void manageFuel() {

    }

    @Override
    public float weightKg() {
        return 1814.3f;
    }

    @Override
    public float getMaxFuel() {
        return 0;
    }

    /**
     * <h2>Inventory Size</h2>
     */
    @Override
    public int getInventoryRows(){return 3;}
    /**
     * <h2>Type</h2>
     */
    @Override
    public TrainsInMotion.transportTypes getType(){return TrainsInMotion.transportTypes.HOPPER;}
    /**
     * <h2>Rider offsets</h2>
     */
    @Override
    public double[][] getRiderOffsets(){return null;}
    /**
     * <h2>Hitbox offsets</h2>
     */
    @Override
    public double[][] getHitboxPositions(){return new double[][]{{-1.7d,0.25d,0d},{-1.1d,0.25d,0d},{0d,0.25d,0d},{1,0.25d,0d},{1d,0.25d,0d},{1.7d,0.25d,0d}};}
    /**
     * <h2>Lamp offset</h2>
     */
    @Override
    public Vec3d getLampOffset(){return new Vec3d(0,0,0);}

    @Override
    public float getPistonOffset() {
        return 0;
    }

    @Override
    public float[][] getSmokeOffset() {
        return null;
    }


    @Override
    public Bogie[] getBogieModels(){return new Bogie[]{GenericCMDBogie(), GenericCMDBogie()};}
    @Override
    public ResourceLocation getTexture(){return URIRegistry.HD_MODEL_ROLLINGSTOCK_TEXTURE.getResource("up_3_bay_100_ton_open_hopper.png");} //URIRegistry.MODEL_ROLLINGSTOCK_TEXTURE.getResource("null.png");}

    @Override
    public List<? extends ModelBase> getModel(){return Collections.singletonList(new UP3Bay100TonHopper());}

    /**
     * <h2>pre-asigned values</h2>
     */
    @Override
    public Item getItem(){
        return thisItem;
    }
}
