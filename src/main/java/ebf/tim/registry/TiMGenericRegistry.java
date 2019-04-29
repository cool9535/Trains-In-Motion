package ebf.tim.registry;


import cpw.mods.fml.common.registry.GameRegistry;
import ebf.tim.TrainsInMotion;
import ebf.tim.blocks.BlockTrainFluid;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.entities.rollingstock.EntityGTAX13000GallonTanker;
import ebf.tim.entities.rollingstock.EntityPullmansPalace;
import ebf.tim.entities.rollingstock.EntityUP3Bay100TonHopper;
import ebf.tim.entities.rollingstock.EntityVATLogCar;
import ebf.tim.entities.trains.EntityBrigadelok080;
import ebf.tim.entities.trains.EntityBrigadelok080Electric;
import ebf.tim.gui.GUICraftBook;
import ebf.tim.items.ItemCraftGuide;
import ebf.tim.utility.DebugUtil;
import ebf.tim.utility.Recipe;
import ebf.tim.utility.RecipeManager;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * <h1>Train registry</h1>
 * this class lists the data necessary to register trains and rollingstock.
 * this is not intended to be a way to get specific trains from the mod. (use the unlocalized entity name, or a class instanceof check)
 * @author Eternal Blue Flame
 */
public class TiMGenericRegistry {

    public GenericRailTransport transport;
    public Item[] recipe;

    public TiMGenericRegistry(GenericRailTransport transport, Item[] recipe){
        this.transport = transport;
        this.recipe = recipe;
    }

    public static Block registerBlock(boolean isClient, Block block, CreativeTabs tab, String unlocalizedName, @Nullable String oreDictionaryName, @Nullable Object TESR){
        if (tab!=null){
            block.setCreativeTab(tab);
        }
        if (!unlocalizedName.equals("")){
            block.setBlockName(unlocalizedName);
            GameRegistry.registerBlock(block, unlocalizedName);
            usedNames.add(unlocalizedName);
        }
        if(oreDictionaryName!=null){
            OreDictionary.registerOre(oreDictionaryName, block);
        }
        if (TrainsInMotion.proxy.isClient() && block.getUnlocalizedName().equals(StatCollector.translateToLocal(block.getUnlocalizedName()))){
            DebugUtil.println("Block missing lang entry: " + block.getUnlocalizedName());
        }
        if(block instanceof ITileEntityProvider){
            GameRegistry.registerTileEntity(
                    ((ITileEntityProvider)block).createNewTileEntity(null,0).getClass(),
                    unlocalizedName+"tile");
            usedNames.add(unlocalizedName+"tile");

            if(isClient && TESR!=null){
                cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(((ITileEntityProvider) block).createNewTileEntity(null, 0).getClass(), (TileEntitySpecialRenderer) TESR);
            }
        }
        return block;
    }

    public static Item RegisterItem(boolean isClient, Item itm, String MODID, String unlocalizedName, CreativeTabs tab){
        return RegisterItem(isClient, itm, MODID, unlocalizedName,null,tab, null,null);
    }

    public static Item RegisterItem(boolean isClient, Item itm, String MODID, String unlocalizedName, @Nullable String oreDictionaryName, @Nullable CreativeTabs tab, @Nullable Item container, @Nullable Object itemRender){
        if (tab!=null) {
            itm.setCreativeTab(tab);
        }
        if (container!=null){
            itm.setContainerItem(container);
        }
        itm.setUnlocalizedName(unlocalizedName);
        if(isClient){
            itm.setTextureName(MODID+":"+unlocalizedName);
        }
        GameRegistry.registerItem(itm,unlocalizedName);
        if(oreDictionaryName!=null){
            OreDictionary.registerOre(oreDictionaryName, itm);
        }
        if (TrainsInMotion.proxy!=null && isClient && itm.getUnlocalizedName().equals(StatCollector.translateToLocal(itm.getUnlocalizedName()))){
            DebugUtil.println("Item missing lang entry: " + itm.getUnlocalizedName());
        }
        if(isClient && itemRender!=null){
            MinecraftForgeClient.registerItemRenderer(itm, (IItemRenderer)itemRender);
        }
        return itm;
    }


    public static void RegisterFluid(boolean isClient, Fluid fluid, String MODID, String unlocalizedName, boolean isGaseous, int density, MapColor color, CreativeTabs tab){
        fluid.setUnlocalizedName(unlocalizedName).setGaseous(isGaseous).setDensity(density);
        FluidRegistry.registerFluid(fluid);

        Block block = new BlockTrainFluid(fluid, new MaterialLiquid(color)).setBlockName("block."+unlocalizedName.replace(".item","")).setBlockTextureName(MODID+":block_"+unlocalizedName);
        GameRegistry.registerBlock(block, "block."+unlocalizedName);
        fluid.setBlock(block);

        Item bucket = new ItemBucket(block).setCreativeTab(tab).setUnlocalizedName(unlocalizedName + ".bucket").setContainerItem(Items.bucket);
                if(isClient){
                    bucket.setTextureName(MODID+":bucket_"+unlocalizedName);
                }
        GameRegistry.registerItem(bucket, "fluid." + unlocalizedName + ".bucket");
        FluidContainerRegistry.registerFluidContainer(fluid, new ItemStack(bucket), new ItemStack(Items.bucket));


        if (TrainsInMotion.proxy.isClient()){
            if(fluid.getUnlocalizedName().equals(StatCollector.translateToLocal(fluid.getUnlocalizedName()))) {
                DebugUtil.println("Fluid missing lang entry: " + fluid.getUnlocalizedName());
            }
            if (bucket.getUnlocalizedName().equals(StatCollector.translateToLocal(block.getUnlocalizedName()))) {
                DebugUtil.println("Item missing lang entry: " + bucket.getUnlocalizedName());
            }
            if(block.getUnlocalizedName().equals(StatCollector.translateToLocal(block.getUnlocalizedName()))){
                DebugUtil.println("Block missing lang entry: " + block.getUnlocalizedName());
            }

        }
    }

    private static List<String>usedNames = new ArrayList<>();
    private static int registryPosition =17;

    public static void registerTransports(boolean isClient, String MODID, GenericRailTransport[] entities, Object entityRender){
        if(registryPosition==-1){
            DebugUtil.println("ERROR", "ADDING TRANSPORT REGISTRY ITEMS OUTSIDE MOD INIT", "PLEASE REGISTER YOUR ENTITIES IN THE FOLLOWING EVENT:",
                    "@Mod.EventHandler public void init(FMLInitializationEvent event)");
        }
        for (GenericRailTransport registry : entities) {
            if(usedNames.contains(registry.transportName())){
                DebugUtil.println(registry.getClass().getName(),"is trying to register under the name", usedNames.contains(registry.transportName()), "which is already used");
            }
            cpw.mods.fml.common.registry.EntityRegistry.registerModEntity(
                    registry.getClass(),
                    registry.transportName().replace(" ","") + ".entity",
                    registryPosition, TrainsInMotion.instance, 3000, 1, true);
            GameRegistry.registerItem(registry.getCartItem().getItem(), registry.getCartItem().getItem().getUnlocalizedName());
            if(GUICraftBook.recipesInMods.containsKey(MODID)){
                GUICraftBook.recipesInMods.get(MODID).add(new Recipe(registry.getCartItem(),
                        registry.getRecipie()[0], registry.getRecipie()[1], registry.getRecipie()[2]
                        , registry.getRecipie()[3], registry.getRecipie()[4], registry.getRecipie()[5]
                        , registry.getRecipie()[6], registry.getRecipie()[7], registry.getRecipie()[8]));
            } else {
                GUICraftBook.recipesInMods.put(MODID, new ArrayList<Recipe>());
                GUICraftBook.recipesInMods.get(MODID).add(new Recipe(registry.getCartItem(),
                        registry.getRecipie()[0], registry.getRecipie()[1], registry.getRecipie()[2]
                        , registry.getRecipie()[3], registry.getRecipie()[4], registry.getRecipie()[5]
                        , registry.getRecipie()[6], registry.getRecipie()[7], registry.getRecipie()[8]));
            }
            registry.registerSkins();
            if(registry.getRecipie()!=null){
                RecipeManager.registerRecipe(registry.getCartItem(), registry.getRecipie());
            }
            if(isClient){
                ItemCraftGuide.itemEntries.add(registry.getClass());
                if(entityRender==null){
                    cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(registry.getClass(), (net.minecraft.client.renderer.entity.Render)TrainsInMotion.proxy.getEntityRender());
                } else {
                    cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(registry.getClass(), (net.minecraft.client.renderer.entity.Render)entityRender);
                }
            }
            usedNames.add(registry.transportName());
            registryPosition++;
        }
    }
    public static void endRegistration(){
        usedNames =null; registryPosition=-1;
    }


    public static GenericRailTransport[] listSteamTrains() {
        return new GenericRailTransport[]{new EntityBrigadelok080(null), new EntityBrigadelok080Electric(null)};
    }

    public static GenericRailTransport[] listPassenger() {
        return new GenericRailTransport[]{new EntityPullmansPalace(null)};
    }

    public static GenericRailTransport[] listFreight() {
        return new GenericRailTransport[]{new EntityVATLogCar(null), new EntityUP3Bay100TonHopper(null)};
    }

    public static GenericRailTransport[] listTanker() {
        return new GenericRailTransport[]{new EntityGTAX13000GallonTanker(null)};
    }


}
