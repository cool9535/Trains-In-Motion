package ebf.tim.utility;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ebf.tim.TrainsInMotion;
import ebf.tim.api.SkinRegistry;
import ebf.tim.entities.EntitySeat;
import ebf.tim.entities.EntityTrainCore;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.networking.PacketInteract;
import fexcraft.tmt.slim.Tessellator;
import fexcraft.tmt.slim.Vec3d;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ReportedException;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.event.RenderWorldEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <h1>event management</h1>
 * used to manage specific events that can't be predicted, like player key presses.
 * @author Eternal Blue Flame
 */
public class EventManager {

    private static List<GenericRailTransport> arraylist,stock;
    private static Vec3d vert, vec, cacheVec;

    /**
     * <h2>Keybind management</h2>
     * manages key pressed or released, since 1.7.10 has no direct support for key released we have to do it directly through LWJGL.
     * Most cases just send a packet to manage things
     * @see PacketInteract
     *
     * Credit to Ferdinand for help with this function.
     *
     * @param event the event of a key being pressed on client.
     */
    @SubscribeEvent
    public void onClientKeyPress(InputEvent.KeyInputEvent event) {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        if (player.ridingEntity instanceof GenericRailTransport || player.ridingEntity instanceof EntitySeat) {
            //for lamp
            if (ClientProxy.KeyLamp.isPressed()) {
                TrainsInMotion.keyChannel.sendToServer(new PacketInteract(0, player.ridingEntity.getEntityId()));
                ((GenericRailTransport) player.ridingEntity).setBoolean(GenericRailTransport.boolValues.LAMP, !((GenericRailTransport) player.ridingEntity).getBoolean(GenericRailTransport.boolValues.LAMP));
            }
            //for inventory
            if (ClientProxy.KeyInventory.isPressed()) {
                TrainsInMotion.keyChannel.sendToServer(new PacketInteract(1, player.ridingEntity.getEntityId()));
            }
            if (player.ridingEntity instanceof EntityTrainCore) {
                //for speed change
                if (FMLClientHandler.instance().getClient().gameSettings.keyBindForward.isPressed()) {
                    TrainsInMotion.keyChannel.sendToServer(new PacketInteract(2, player.ridingEntity.getEntityId()));
                } else if (FMLClientHandler.instance().getClient().gameSettings.keyBindBack.getIsKeyPressed()) {
                    TrainsInMotion.keyChannel.sendToServer(new PacketInteract(3, player.ridingEntity.getEntityId()));
                } else if (ClientProxy.KeyHorn.isPressed()){
                    TrainsInMotion.keyChannel.sendToServer(new PacketInteract(9, player.ridingEntity.getEntityId()));
                } else if (FMLClientHandler.instance().getClient().gameSettings.keyBindJump.isPressed()){
                    TrainsInMotion.keyChannel.sendToServer(new PacketInteract(16, player.ridingEntity.getEntityId()));
                }

                //manage key release events
                if (Keyboard.getEventKey() == FMLClientHandler.instance().getClient().gameSettings.keyBindJump.getKeyCode() && !Keyboard.getEventKeyState()){
                    TrainsInMotion.keyChannel.sendToServer(new PacketInteract(15, player.ridingEntity.getEntityId()));
                }
            }
        } else if(DebugUtil.dev()) {
            if (ClientProxy.raildevtoolUp.isPressed()){
                ClientProxy.devSplineModification[ClientProxy.devSplineCurrentPoint][0]+=0.0625;
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("current spline shape is " +
                        ClientProxy.devSplineModification[0][0] + "," + ClientProxy.devSplineModification[0][1] +"," + ClientProxy.devSplineModification[0][2] +" : " +
                        ClientProxy.devSplineModification[1][0] + "," + ClientProxy.devSplineModification[1][1] +"," + ClientProxy.devSplineModification[1][2] +" : " +
                        ClientProxy.devSplineModification[2][0] + "," + ClientProxy.devSplineModification[2][1] +"," + ClientProxy.devSplineModification[2][2] +" : " +
                        ClientProxy.devSplineModification[3][0] + "," + ClientProxy.devSplineModification[3][1] +"," + ClientProxy.devSplineModification[3][2]));
            } else if (ClientProxy.raildevtoolDown.isPressed()){
                ClientProxy.devSplineModification[ClientProxy.devSplineCurrentPoint][0]-=0.0625;
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("current spline shape is " +
                        ClientProxy.devSplineModification[0][0] + "," + ClientProxy.devSplineModification[0][1] +"," + ClientProxy.devSplineModification[0][2] +" : " +
                        ClientProxy.devSplineModification[1][0] + "," + ClientProxy.devSplineModification[1][1] +"," + ClientProxy.devSplineModification[1][2] +" : " +
                        ClientProxy.devSplineModification[2][0] + "," + ClientProxy.devSplineModification[2][1] +"," + ClientProxy.devSplineModification[2][2] +" : " +
                        ClientProxy.devSplineModification[3][0] + "," + ClientProxy.devSplineModification[3][1] +"," + ClientProxy.devSplineModification[3][2]));
            }
            if (ClientProxy.raildevtoolLeft.isPressed()){
                ClientProxy.devSplineModification[ClientProxy.devSplineCurrentPoint][2]+=0.0625;
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("current spline shape is " +
                        ClientProxy.devSplineModification[0][0] + "," + ClientProxy.devSplineModification[0][1] +"," + ClientProxy.devSplineModification[0][2] +" : " +
                        ClientProxy.devSplineModification[1][0] + "," + ClientProxy.devSplineModification[1][1] +"," + ClientProxy.devSplineModification[1][2] +" : " +
                        ClientProxy.devSplineModification[2][0] + "," + ClientProxy.devSplineModification[2][1] +"," + ClientProxy.devSplineModification[2][2] +" : " +
                        ClientProxy.devSplineModification[3][0] + "," + ClientProxy.devSplineModification[3][1] +"," + ClientProxy.devSplineModification[3][2]));
            } else if (ClientProxy.raildevtoolRight.isPressed()){
                ClientProxy.devSplineModification[ClientProxy.devSplineCurrentPoint][2]-=0.0625;
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("current spline shape is " +
                        ClientProxy.devSplineModification[0][0] + "," + ClientProxy.devSplineModification[0][1] +"," + ClientProxy.devSplineModification[0][2] +" : " +
                        ClientProxy.devSplineModification[1][0] + "," + ClientProxy.devSplineModification[1][1] +"," + ClientProxy.devSplineModification[1][2] +" : " +
                        ClientProxy.devSplineModification[2][0] + "," + ClientProxy.devSplineModification[2][1] +"," + ClientProxy.devSplineModification[2][2] +" : " +
                        ClientProxy.devSplineModification[3][0] + "," + ClientProxy.devSplineModification[3][1] +"," + ClientProxy.devSplineModification[3][2]));
            }
            if (ClientProxy.raildevtoolRaise.isPressed()){
                ClientProxy.devSplineModification[ClientProxy.devSplineCurrentPoint][1]+=0.0625;
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("current spline shape is " +
                        ClientProxy.devSplineModification[0][0] + "," + ClientProxy.devSplineModification[0][1] +"," + ClientProxy.devSplineModification[0][2] +" : " +
                        ClientProxy.devSplineModification[1][0] + "," + ClientProxy.devSplineModification[1][1] +"," + ClientProxy.devSplineModification[1][2] +" : " +
                        ClientProxy.devSplineModification[2][0] + "," + ClientProxy.devSplineModification[2][1] +"," + ClientProxy.devSplineModification[2][2] +" : " +
                        ClientProxy.devSplineModification[3][0] + "," + ClientProxy.devSplineModification[3][1] +"," + ClientProxy.devSplineModification[3][2]));
            } else if (ClientProxy.raildevtoolLower.isPressed()){
                ClientProxy.devSplineModification[ClientProxy.devSplineCurrentPoint][1]-=0.0625;
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("current spline shape is " +
                        ClientProxy.devSplineModification[0][0] + "," + ClientProxy.devSplineModification[0][1] +"," + ClientProxy.devSplineModification[0][2] +" : " +
                        ClientProxy.devSplineModification[1][0] + "," + ClientProxy.devSplineModification[1][1] +"," + ClientProxy.devSplineModification[1][2] +" : " +
                        ClientProxy.devSplineModification[2][0] + "," + ClientProxy.devSplineModification[2][1] +"," + ClientProxy.devSplineModification[2][2] +" : " +
                        ClientProxy.devSplineModification[3][0] + "," + ClientProxy.devSplineModification[3][1] +"," + ClientProxy.devSplineModification[3][2]));
            }

            if (ClientProxy.raildevtoolNextPoint.isPressed()){
                ClientProxy.devSplineCurrentPoint++;
                if (ClientProxy.devSplineCurrentPoint>3){
                    ClientProxy.devSplineCurrentPoint = 0;
                }
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("current point is now " + ClientProxy.devSplineCurrentPoint));
            } else if (ClientProxy.raildevtoolLastPoint.isPressed()){
                ClientProxy.devSplineCurrentPoint--;
                if (ClientProxy.devSplineCurrentPoint<0){
                    ClientProxy.devSplineCurrentPoint = 3;
                }
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("current point is now " + ClientProxy.devSplineCurrentPoint));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientKeyPress(InputEvent.MouseInputEvent event) {
        if (Mouse.isButtonDown(1) || Mouse.isButtonDown(0)) {
            cacheVec = RailUtility.rotateDistance(0.2f, Minecraft.getMinecraft().thePlayer.rotationPitch, 810+Minecraft.getMinecraft().thePlayer.rotationYawHead);

            arraylist = getTrainsInRange();

            for (GenericRailTransport t : arraylist) {
                if(Minecraft.getMinecraft().thePlayer.ridingEntity==t){continue;}
                for (int i = 0; i < (Minecraft.getMinecraft().playerController.extendedReach() ? 32 : 16); i++) {
                    if (t.collisionHandler.containsPoint(
                            Minecraft.getMinecraft().thePlayer.posX + (cacheVec.xCoord * i),
                            Minecraft.getMinecraft().thePlayer.posY + (cacheVec.yCoord * i)-1,
                            Minecraft.getMinecraft().thePlayer.posZ + (cacheVec.zCoord * i))) {

                        (t).interact(Minecraft.getMinecraft().thePlayer.getEntityId(), false, false, Mouse.isButtonDown(1)?-1:-999);
                        MinecraftForge.EVENT_BUS.post(new EntityInteractEvent(Minecraft.getMinecraft().thePlayer, t));
                        return;
                    }
                }
            }
        }
    }


    @SubscribeEvent
    public void EntityInteractEvent(EntityInteractEvent e){
        if(e.target instanceof GenericRailTransport){
            e.setCanceled(true);
        }
    }


    private static List<GenericRailTransport> getTrainsInRange(){
        ArrayList<GenericRailTransport> list =new ArrayList<>();
        for (int i1 = Minecraft.getMinecraft().thePlayer.chunkCoordX - 1; i1 <= Minecraft.getMinecraft().thePlayer.chunkCoordX + 1; ++i1) {
            for (int j1 = Minecraft.getMinecraft().thePlayer.chunkCoordZ - 1; j1 <= Minecraft.getMinecraft().thePlayer.chunkCoordX + 1; ++j1) {
                if (Minecraft.getMinecraft().thePlayer.worldObj.getChunkProvider().chunkExists(i1, j1)) {
                    List[] e = Minecraft.getMinecraft().thePlayer.worldObj.getChunkFromChunkCoords(i1, j1).entityLists;
                    for (List olist : e) {
                        for (Object obj : olist) {
                            if (obj instanceof GenericRailTransport) {
                                list.add((GenericRailTransport) obj);
                            }
                        }
                    }
                }
            }
        }
        return list;
    }


    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (ClientProxy.enableTransportTooltip&&
                Minecraft.getMinecraft().renderViewEntity != null && Minecraft.getMinecraft().theWorld != null) {

            if(Minecraft.getMinecraft().thePlayer.ridingEntity instanceof GenericRailTransport ||
                    Minecraft.getMinecraft().thePlayer.ridingEntity instanceof EntitySeat ||
            Minecraft.getMinecraft().currentScreen!=null){
                return;
            }

            //get all the trains and stock in reach
            if(stock==null || Minecraft.getMinecraft().thePlayer.ticksExisted %5==0) {
                stock = getTrainsInRange();
            }
            if(stock.size()<1){return;}

            //calculate if vector is in the hitbox at any point along the path. safest is to check by microblock for the 3 to 6 block length, but quarter would be WAY more efficient

            vec = RailUtility.rotateDistance(0.25f, Minecraft.getMinecraft().thePlayer.rotationPitch, 810+Minecraft.getMinecraft().thePlayer.rotationYawHead);
            for (int i=0; i<(Minecraft.getMinecraft().playerController.extendedReach()?16:8); i++){
                vert=vec.crossProduct(i).addVector(Minecraft.getMinecraft().thePlayer.posX,Minecraft.getMinecraft().thePlayer.posY,Minecraft.getMinecraft().thePlayer.posZ);


                for(GenericRailTransport t : stock){
                    if(t.collisionHandler.containsPoint(vert.xCoord, vert.yCoord-1, vert.zCoord)){
                        ClientProxy.toggleWaila(false);
                        ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft(),Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
                        left=res.getScaledWidth()/2;
                        disp=getStaticStrings(t);
                        if(disp==null){return;}
                        longest=0;
                        for(String s: disp){
                            if(s.length()>longest*2){
                                longest=s.length()*2;
                            }
                        }
                        longest+=10;

                        drawTooltipBox(left-(longest)-35, 2, 70+(longest*2), 8+(10*disp.length), ClientProxy.WAILA_BGCOLOR, ClientProxy.WAILA_GRADIENT1, ClientProxy.WAILA_GRADIENT2,ClientProxy.WAILA_ALPHA);

                        GL11.glTranslatef(0.0F, 0.0F, 32.0F);
                        itemRender.renderItemAndEffectIntoGUI(Minecraft.getMinecraft().fontRenderer, Minecraft.getMinecraft().getTextureManager(),
                                t.getCartItem(), left-(longest)-30, 12);
                        GL11.glDisable(GL11.GL_LIGHTING);
                        for(int ii=0; ii<disp.length;ii++) {
                            Minecraft.getMinecraft().fontRenderer.drawString(disp[ii],
                                    40+left-(longest*3)+ ((longest-disp[ii].length())*2), 8+(ii*10),ii==0?0xFFFFFFFF:ClientProxy.WAILA_FONTCOLOR);
                        }
                        GL11.glEnable(GL11.GL_LIGHTING);
                        //todo: draw an array of strings for the tooltip info, derrived from the transport's class.

                        ClientProxy.toggleWaila(true);
                        return;
                    }
                }
            }
        }
    }
    private static int left=0,longest;
    private static String[] disp;
    private static RenderItem itemRender = new RenderItem();

    private static String[] getStaticStrings(GenericRailTransport t){
        return new String[]{
                StatCollector.translateToLocal(t.getItem().getUnlocalizedName()+".name"),
                "owner: " + t.getOwnerName(),
                "skin: " + SkinRegistry.getSkin(t.getClass(), t.getDataWatcher().getWatchableObjectString(24)).name
        };
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void playerQuitEvent(PlayerEvent.PlayerLoggedOutEvent event){
        ClientProxy.toggleWaila(ClientProxy.WAILA_TOGGLE);
    }

    public static void drawGradientRect(int x, int y, int w, int h, int grad1, int grad2, int alpha) {
        Tessellator.getInstance().startDrawing(GL11.GL_QUADS);
        GL11.glColor4ub((byte)((grad1 >> 16) & 0xFF), (byte)((grad1 >> 8) & 0xFF), (byte)(grad1 & 0xFF), (byte)((255f/100f)*alpha));
        Tessellator.getInstance().addVertex((x + w), y, 0f);
        Tessellator.getInstance().addVertex(x, y, 0f);
        GL11.glColor4ub((byte)((grad2 >> 16) & 0xFF), (byte)((grad2 >> 8) & 0xFF), (byte)(grad2 & 0xFF), (byte)((255f/100f)*alpha));
        Tessellator.getInstance().addVertex(x, (y + h), 0f);
        Tessellator.getInstance().addVertex((x + w), (y + h), 0f);
        Tessellator.getInstance().draw();
    }

    public static void drawTooltipBox(int x, int y, int w, int h, int bg, int grad1, int grad2, int alpha) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        drawGradientRect(x + 1, y, w - 1, 1, bg, bg,alpha);
        drawGradientRect(x + 1, y + h, w - 1, 1, bg, bg,alpha);
        drawGradientRect(x + 1, y + 1, w - 1, h - 1, bg, bg,alpha);
        drawGradientRect(x, y + 1, 1, h - 1, bg, bg,alpha);
        drawGradientRect(x + w, y + 1, 1, h - 1, bg, bg,alpha);
        drawGradientRect(x + 1, y + 2, 1, h - 3, grad1, grad2,alpha);
        drawGradientRect(x + w - 1, y + 2, 1, h - 3, grad1, grad2,alpha);
        drawGradientRect(x + 1, y + 1, w - 1, 1, grad1, grad1,alpha);
        drawGradientRect(x + 1, y + h - 1, w - 1, 1, grad2, grad2,alpha);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    /**
     * <h2>join world</h2>
     * This event is called when a player joins the world, we use this to display the alpha notice, and check for new mod versions, this is only displayed on the client side, but can be used for server..
     */
    @SubscribeEvent
    @SuppressWarnings("unused")
    public void entityJoinWorldEvent(EntityJoinWorldEvent event) {
        if (event.world.isRemote && event.entity instanceof EntityPlayer) {

            List<String[]> ids = new ArrayList<>();
            try {
                //make an HTTP connection to the file, and set the type as get.
                HttpURLConnection conn = (HttpURLConnection) new URL("https://raw.githubusercontent.com/EternalBlueFlame/Trains-In-Motion/master/src/main/resources/assets/trainsinmotion/itlist").openConnection();
                conn.setRequestMethod("GET");
                //use the HTTP connection as an input stream to actually get the file, then put it into a buffered reader.
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String[] entries = rd.toString().split(",");
                if(entries!=null && entries.length>1) {
                    for (int i = 0; i < entries.length; i+=2){
                        ids.add(new String[]{entries[i], entries[i+1]});
                    }

                }
                rd.close();
                conn.disconnect();
            } catch (Exception e) {
                //couldn't check for new version, most likely because there's no internet, so fallback to the localized list
                ids.add(new String[]{"60760e4b-55bc-404d-9409-fa40d796b314","0"});
                ids.add(new String[]{"157eae46-e464-46c2-9913-433a40896831","1"});
                ids.add(new String[]{"2096b3ec-8ba7-437f-8e8a-0977fc769af1","1"});
            }


            for(String[] entry : ids){
                if(event.entity.getUniqueID().equals(UUID.fromString(entry[0]))){
                    if(entry[1].equals("0")){
                        throw new ReportedException(CrashReport.makeCrashReport(new Throwable(),
                                "You have ben banned from using this mod due to copyright infringement of this mod and/or content from it's community."));
                    } else {//1
                        throw new ReportedException(CrashReport.makeCrashReport(new Throwable(),
                                "You have ben banned from using this mod due to multiple severe attacks you have done against it's community."));
                    }
                }
            }





            //add alpha notice
            ((EntityPlayer) event.entity).addChatMessage(new ChatComponentText("You are currently playing a pre-alpha release of Trains In Motion."));
            ((EntityPlayer) event.entity).addChatMessage(new ChatComponentText("For official releases, check out https://github.com/EternalBlueFlame/Trains-In-Motion/"));
            ((EntityPlayer) event.entity).addChatMessage(new ChatComponentText("Keep in mind that everything in this mod currently is subject to change."));
            ((EntityPlayer) event.entity).addChatMessage(new ChatComponentText("Good luck and thanks for the assistance. - Eternal Blue Flame."));

            //use an HTTP request and parse to check for new versions of the mod from github.
            try {
                //make an HTTP connection to the version text file, and set the type as get.
                HttpURLConnection conn = (HttpURLConnection) new URL("https://raw.githubusercontent.com/EternalBlueFlame/Trains-In-Motion/master/version.txt").openConnection();
                conn.setRequestMethod("GET");
                //use the HTTP connection as an input stream to actually get the file, then put it into a buffered reader.
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                //read the first line of the text document, if it's not the same as the current running version, notify there is an update, then display the second line, which is intended for a download URL.
                if (!TrainsInMotion.MOD_VERSION.equals(rd.readLine())) {
                    ((EntityPlayer) event.entity).addChatMessage(new ChatComponentText("A new version of Trains In Motion is available, check it out at:"));
                    ((EntityPlayer) event.entity).addChatMessage(new ChatComponentText(rd.readLine()));
                }
                rd.close();
                conn.disconnect();
            } catch (Exception e) {
                //couldn't check for new version, most likely because there's no internet, so just do nothing.
            }

        }
    }
}
