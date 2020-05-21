package ebf.tim.registry;

import ebf.tim.TrainsInMotion;
import ebf.tim.blocks.BlockDynamic;
import ebf.tim.blocks.rails.BlockRailCore;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static cpw.mods.fml.common.registry.GameRegistry.addRecipe;
import static ebf.tim.registry.TiMGenericRegistry.registerBlock;

public class TiMBlocks {

    /**the crafting table for trains*/
    public static BlockDynamic trainTable = new BlockDynamic("blocktraintable", new Material(MapColor.mapColorArray[13]), 0);

    public static BlockDynamic railTable = new BlockDynamic("blockrailtable", new Material(MapColor.mapColorArray[6]), 1);

    public static BlockRailCore railBlock = new BlockRailCore(750,1f);


    public static void registerBlocks(){

        registerBlock(railBlock, null, "block.timrail", null, TrainsInMotion.proxy.getTESR());

        //register the train crafting table
        addRecipe(new ItemStack(registerBlock(trainTable, TrainsInMotion.creativeTab,"block.traintable", null, null),1),
                "WWW", "WIW", "WWW", 'W', Blocks.planks, 'I', Items.iron_ingot);

        addRecipe(new ItemStack(registerBlock(railTable, TrainsInMotion.creativeTab,"block.railtable", null, null),1),
                "III", "I I", "I I", 'I', Items.iron_ingot);

    }
}
