package ebf.tim.utility;


import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import ebf.tim.blocks.BlockRailOverride;
import ebf.tim.blocks.LampBlock;
import ebf.tim.blocks.TileEntityStorage;
import ebf.tim.entities.EntityBogie;
import ebf.tim.entities.EntitySeat;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.gui.GUITrainTable;
import ebf.tim.gui.GUITransport;
import ebf.tim.gui.HUDTrain;
import ebf.tim.models.RenderEntity;
import ebf.tim.models.RenderScaledPlayer;
import ebf.tim.models.rails.ModelRailCurveVerySmall;
import ebf.tim.models.rails.ModelRailStraight;
import ebf.tim.registry.TransportRegistry;
import ebf.tim.registry.URIRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>client proxy</h1>
 * defines some of the more important client-only functionality that runs on the main thread of the mod.
 * @author Eternal Blue Flame
 */
public class ClientProxy extends CommonProxy {
    public static List<GenericRailTransport> carts = new ArrayList<GenericRailTransport>();

    /*
     * <h3>keybinds</h3>
     * Initialize the default values for keybinds.
     * Default values courtesy of Ferdinand
     */
    /**whether or not lights should be enabled*/
    public static boolean EnableLights = true;
    /**whether or not smoke and steam should be enabled*/
    public static boolean EnableSmokeAndSteam = true;
    /**whether or not animations should be enabled*/
    public static boolean EnableAnimations = true;
    /**whether or not to use the 3D rails*/
    public static boolean Enable3DRails = true;
    /**the keybind for the lamp toggle*/
    public static KeyBinding KeyLamp = new KeyBinding("Lamp Toggle", 38, "Trains in Motion");
    /**the keybind for the horn/whistle*/
    public static KeyBinding KeyHorn = new KeyBinding("Use Horn/Whistle", 35, "Trains in Motion");
    /**the keybind for opening the inventory*/
    public static KeyBinding KeyInventory = new KeyBinding("Open Train/rollingstock GUI", 23, "Trains in Motion");
    /**the keybind for acceleration*/
    public static KeyBinding KeyAccelerate = new KeyBinding("Train Acceleration", 19, "Trains in Motion");//R
    /**the keybind for deceleration/reverse*/
    public static KeyBinding KeyReverse = new KeyBinding("Train Deceleration/Reverse", 33, "Trains in Motion");//F

    /**
     * <h2> Client GUI Redirect </h2>
     *
     * Mostly a redirect between the event handler and the actual GUI
     *
     * defines the GUI element to display based on the ID
     * returns null if the player, cart or ID is invalid
     *
     * the inventory manager that server uses for this menu is defined in
     * @see CommonProxy#getServerGuiElement(int, EntityPlayer, World, int, int, int)
     */
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (player != null) {
            //Trains
            if (player.worldObj.getEntityByID(ID) instanceof GenericRailTransport) {
                return new GUITransport(player.inventory, (GenericRailTransport) player.worldObj.getEntityByID(ID));
                //tile entities
            } else if (player.worldObj.getTileEntity(x,y,z) instanceof TileEntityStorage) {
                return new GUITrainTable(player.inventory, player.worldObj, x, y, z);
            }
        }
        return null;
    }

    /**
     * <h2>Load config</h2>
     * this loads the config values that will only effect client.
     */
    @Override
    public void loadConfig(Configuration config){
        super.loadConfig(config);
        config.addCustomCategoryComment("Quality (Client only)", "Lamps take up a lot of extra processing on client side due to forced chunk reloading");
        EnableLights = config.get(Configuration.CATEGORY_GENERAL, "EnableLamp", true).getBoolean(true);
        config.addCustomCategoryComment("Quality (Client only)", "Smoke and steam effects are more lightweight than those of normal minecraft. These shouldn't cause much lag if any, but its client only so if you wanna disable it you can.");
        EnableSmokeAndSteam = config.get(Configuration.CATEGORY_GENERAL, "EnableSmokeAndSteam", true).getBoolean(true);
        config.addCustomCategoryComment("Quality (Client only)", "Animations are calculated by vector positioning and rotation every frame. These shouldn't cause much lag if any, but its client only so if you wanna disable it you can.");
        EnableAnimations = config.get(Configuration.CATEGORY_GENERAL, "EnableAnimations", true).getBoolean(true);
        config.addCustomCategoryComment("Quality (Client only)", "Overrides the render of vanilla rails to make them use a more detailed 3D render which supports more detailed switches and diagonals.");
        Enable3DRails = config.get(Configuration.CATEGORY_GENERAL, "Enable3DRails", false).getBoolean(false);

        config.addCustomCategoryComment("Keybinds (Client only)", "accepted values can be set from in-game, or defined using the key code values from: http://minecraft.gamepedia.com/Key_codes");

        KeyLamp.setKeyCode(config.getInt("LampKeybind", "Keybinds", 38, 0, 0, ""));
        KeyLamp.setKeyCode(config.getInt("HornKeybind", "Keybinds", 35, 0, 0, ""));
        KeyInventory.setKeyCode(config.getInt("InventoryKeybind", "Keybinds", 23, 0, 0, ""));
        KeyAccelerate.setKeyCode(config.getInt("AccelerateKeybind", "Keybinds", 19, 0, 0, ""));
        KeyReverse.setKeyCode(config.getInt("ReverseKeybind", "Keybinds", 33, 0, 0, ""));
    }

    /**the client only lamp block*/
    public static Block lampBlock= new LampBlock();

    /**
     * <h2>Client Register</h2>
     * Used for registering client only functions and redirecting registering the items in the train registry with their own textures and models.
     */
    @Override
    public void register() {
        super.register();
        GameRegistry.registerBlock(lampBlock, "lampblock");
        lampBlock.setLightLevel(1f);

        //register the fluid icons
        fluidOil.setIcons(BlockLiquid.getLiquidIcon("water_still"), BlockLiquid.getLiquidIcon("water_flow"));

        //trains and rollingstock
        int index=0;
        while (TransportRegistry.listTrains(index)!=null) {
            TransportRegistry reg = TransportRegistry.listTrains(index);
            RenderingRegistry.registerEntityRenderingHandler(reg.trainClass, new RenderEntity(
                    reg.model, reg.texture, reg.bogieModels));
            index++;
        }
        //hitboxes
        RenderingRegistry.registerEntityRenderingHandler(HitboxHandler.MultipartHitbox.class, nullRender);
        //bogies
        RenderingRegistry.registerEntityRenderingHandler(EntityBogie.class, nullRender);
        //seats
        RenderingRegistry.registerEntityRenderingHandler(EntitySeat.class, nullRender);
        //player scaler
        RenderingRegistry.registerEntityRenderingHandler(EntityPlayer.class, new RenderScaledPlayer());



        //GameRegistry.registerBlock(new BlockRailOverride(), Item);
        ClientRegistry.bindTileEntitySpecialRenderer(BlockRailOverride.renderTileEntity.class, new TileEntitySpecialRenderer() {
            @Override
            public void renderTileEntityAt(TileEntity p_147500_1_, double p_147500_2_, double p_147500_4_, double p_147500_6_, float p_147500_8_) {
                GL11.glPushMatrix();
                GL11.glTranslated(p_147500_2_+0.5,p_147500_4_+0.325, p_147500_6_+0.5);
                GL11.glScaled(1,0.5,1);
                switch (p_147500_1_.getBlockMetadata()){
                    //straight
                    case 1:{
                        GL11.glRotatef(180, 1, 0, 0);
                        Minecraft.getMinecraft().getTextureManager().bindTexture(URIRegistry.MODEL_RAIL_TEXTURE.getResource("RailStraight.png"));
                        railStraightModel.render(null,0,0,0,0,0,0);
                        break;
                    }
                    case 0:{
                        GL11.glRotatef(180, 1, 0, 1f);
                        Minecraft.getMinecraft().getTextureManager().bindTexture(URIRegistry.MODEL_RAIL_TEXTURE.getResource("RailStraight.png"));
                        railStraightModel.render(null,0,0,0,0,0,0);
                        break;
                    }
                    //curves
                        //TODO: model is upsidedown i think...
                    case 6:{
                        GL11.glRotatef(180, 1, 0, 0);
                        GL11.glRotatef(270, 0, 1, 0);
                        Minecraft.getMinecraft().getTextureManager().bindTexture(URIRegistry.MODEL_RAIL_TEXTURE.getResource("RailCurveVerySmall.png"));
                        railCurveModel.render(null,0,0,0,0,0,0);
                        break;
                    }
                    case 7:{
                        GL11.glRotatef(180, 1, 0, 0);
                        Minecraft.getMinecraft().getTextureManager().bindTexture(URIRegistry.MODEL_RAIL_TEXTURE.getResource("RailCurveVerySmall.png"));
                        railCurveModel.render(null,0,0,0,0,0,0);
                        break;
                    }
                    case 8:{
                        GL11.glRotatef(180, 1, 0, 0);
                        GL11.glRotatef(90, 0, 1, 0);
                        Minecraft.getMinecraft().getTextureManager().bindTexture(URIRegistry.MODEL_RAIL_TEXTURE.getResource("RailCurveVerySmall.png"));
                        railCurveModel.render(null,0,0,0,0,0,0);
                        break;
                    }
                    case 9:{
                        GL11.glRotatef(180, 1, 0, 0);
                        GL11.glRotatef(180, 0, 1, 0);
                        Minecraft.getMinecraft().getTextureManager().bindTexture(URIRegistry.MODEL_RAIL_TEXTURE.getResource("RailCurveVerySmall.png"));
                        railCurveModel.render(null,0,0,0,0,0, 0);
                        break;
                    }

                }

                GL11.glPopMatrix();
            }
        });




        //keybinds
        ClientRegistry.registerKeyBinding(KeyLamp);
        ClientRegistry.registerKeyBinding(KeyInventory);
        ClientRegistry.registerKeyBinding(KeyAccelerate);
        ClientRegistry.registerKeyBinding(KeyReverse);

        //register the transport HUD.
        HUDTrain hud = new HUDTrain();
        FMLCommonHandler.instance().bus().register(hud);
        MinecraftForge.EVENT_BUS.register(hud);

    }


    private static final ModelRailStraight railStraightModel = new ModelRailStraight();
    private static final ModelRailCurveVerySmall railCurveModel = new ModelRailCurveVerySmall();

    /**
     * <h3>null render</h3>
     * this is just a simple render that never draws anything, since its static it only ever needs to exist once, which makes it lighter on the render.
     */
    private static final Render nullRender = new Render() {
        @Override
        public void doRender(Entity p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_) {}

        @Override
        protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
            return null;
        }
    };

    /**
     * <h2> Forced Dynamic Lighting </h2>
     *
     * this is used to force events from the main thread of the mod, it can create a lot of lag sometimes.
     *
     * Used to force lighting updates (if enabled in config).
     * It also only updates if it's actually needed, to help preserve what performance we can because of how much lag this can create.
     * Because this is a client only method, it creates no overhead on the server.
     *
     * @param tick the client tick event from the main thread
     */
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tick) {
        if (EnableLights && tick.phase == TickEvent.Phase.END && carts.size() > 0) {
            if (Minecraft.getMinecraft().theWorld != null) {
                for (GenericRailTransport cart : carts) {
                    if (cart != null) {
                        Minecraft.getMinecraft().theWorld.updateLightByType(EnumSkyBlock.Block, cart.lamp.X, cart.lamp.Y, cart.lamp.Z);
                    }
                }
            }
        }
    }
}