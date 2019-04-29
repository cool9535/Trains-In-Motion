package fexcraft.tmt.slim;

import ebf.tim.TrainsInMotion;
import ebf.tim.utility.ClientProxy;
import ebf.tim.utility.DebugUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;

public class TextureManager {


    public static ByteBuffer renderPixels = ByteBuffer.allocateDirect((4096*4096)*4);
    private static int i, length, skyLight;
    private static int[] RGBint, pixels;
    private static final byte fullAlpha=(byte)0;
    private static Set MCResourcePacks;

    public static Map<ItemStack,int[]> ingotColors = new HashMap<>();


    private static Map<ResourceLocation, Integer> tmtMap = new HashMap<>();

    private static Map<ResourceLocation, int[]> tmtTextureMap = new HashMap<>();

    private static ITextureObject object;
    /**
     * custom texture binding method, generally same as vanilla, but possible to improve performance later.
     * @param textureURI
     */
    public static boolean bindTexture(ResourceLocation textureURI) {
        if (textureURI == null){
            textureURI= new ResourceLocation(TrainsInMotion.MODID,"nullTrain");
        }
        if(ClientProxy.ForceTextureBinding) {
            object = Minecraft.getMinecraft().getTextureManager().getTexture(textureURI);
            if (object == null) {
                object = new SimpleTexture(textureURI);
                Minecraft.getMinecraft().getTextureManager().loadTexture(textureURI, object);
            }
            GL11.glBindTexture(GL_TEXTURE_2D, object.getGlTextureId());
        } else {
            Integer id = tmtMap.get(textureURI);
            if (id ==null){
                object = Minecraft.getMinecraft().getTextureManager().getTexture(textureURI);
                if (object == null) {
                    object = new SimpleTexture(textureURI);
                    Minecraft.getMinecraft().getTextureManager().loadTexture(textureURI, object);
                }
                id=object.getGlTextureId();
                tmtMap.put(textureURI, id);
            }
            if(GL11.glGetInteger(GL_TEXTURE_2D) !=id) {
                GL11.glBindTexture(GL_TEXTURE_2D, id);
            }
        }
        return true;
    }

    public static @Nullable int[] loadTexture(ResourceLocation resource){
        //if the list of loaded resource packs has changed, invalidate our texture cache as well.
        if(Minecraft.getMinecraft().getResourceManager().getResourceDomains() != MCResourcePacks){
            MCResourcePacks = Minecraft.getMinecraft().getResourceManager().getResourceDomains();
            tmtTextureMap =new HashMap<>();
        }

        int[] texture = tmtTextureMap.get(resource);

        bindTexture(resource);
        if(texture==null){

            try {
                texture = TextureUtil.readImageData(Minecraft.getMinecraft().getResourceManager(), resource);
            } catch (IOException e){
                if(!transportTextureFails.contains(resource.getResourcePath())) {
                    System.out.println("TRAINS IN MOTION WARNING");
                    System.out.println("TEXTURE FAILED TO LOAD: " + resource.getResourceDomain() + ":" + resource.getResourcePath());
                    transportTextureFails.add(resource.getResourcePath());
                }
                texture=null;
            }
            if(texture!=null) {
                tmtTextureMap.put(resource, texture);
            }
        }

        return texture;
    }

    private static List<String> transportTextureFails = new ArrayList<>();



    public static void maskColors(ResourceLocation textureURI, List<Integer> colors){
        pixels = loadTexture(textureURI);
        if(pixels==null){
            return;
        }
        length = ((pixels[0]*pixels[1])*4)-4;

        for(i=0; i<length; i+=4) {
            renderPixels.put(i+3, b(pixels[i+3]));//alpha is always from host texture.
            if (pixels[i+3] == fullAlpha){
                continue;//skip pixels with no color
            }
            //for each set of recoloring
            if (colors!=null && colors.size()>0) {
                for (Integer col: colors) {
                    RGBint = hexTorgba(col);
                    //if it's within 10 RGB, add the actual color we want to the differences
                    if (colorInRange(pixels[i] & 0xFF, pixels[i + 1] & 0xFF, pixels[i + 2] & 0xFF,
                            RGBint[0], RGBint[1], RGBint[2])) {
                        renderPixels.put(i, b(RGBint[0]));
                        renderPixels.put(i + 1, b(RGBint[1]));
                        renderPixels.put(i + 2, b(RGBint[2]));
                    } else {
                        renderPixels.put(i, b(pixels[i]));
                        renderPixels.put(i + 1, b(pixels[i + 1]));
                        renderPixels.put(i + 2, b(pixels[i + 2]));
                    }
                }
            } else {
                renderPixels.put(i, b(pixels[i]));
                renderPixels.put(i + 1, b(pixels[i + 1]));
                renderPixels.put(i + 2, b(pixels[i + 2]));
            }
        }

        glTexSubImage2D (GL_TEXTURE_2D, 0, 0, 0, pixels[0], pixels[1], GL_RGBA, GL_UNSIGNED_BYTE, renderPixels);
        renderPixels.clear();//reset the buffer to all 0's.
    }

    //most compilers should process this type of function faster than a normal typecast.
    public static byte b(int i){return (byte) i;}

    public static boolean colorInRange(int r, int g, int b, int oldR, int oldG, int oldB){
        return oldR-r>-15 && oldR-r <15 &&
                oldG-g>-15 && oldG-g <15 &&
                oldB-b>-15 && oldB-b <15;
    }


    /**Lighting fix*/
    public static void adjustLightFixture(World world, int i, int j, int k) {
        skyLight = world.getLightBrightnessForSkyBlocks(i, j, k, 0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,  skyLight % 65536,  skyLight / 65536f);
        GL11.glColor4f(255, 255, 255, 255);//fixes alpha layering bugs with other mods that don't clear their GL cache
    }


    /**
     * Ingot color textures
     */
    public static void collectIngotColors(){
        String[] ores = OreDictionary.getOreNames();

        int red,green,blue,divisor;
        int[]rgb, colorBuff;
        ResourceLocation texture;
        for(String o: ores){
            if (o.contains("ingot")){
                for (ItemStack s : OreDictionary.getOres(o)){

                    texture=null;
                    red =0;green=0;blue=0;divisor=0;
                    Item item = s.getItem();
                    String textureName = item.getIcon(s,0).getIconName();
                    if(textureName != null){
                        if(textureName.split(":").length == 1){
                            textureName = "minecraft:" + textureName;
                        }
                        texture = new ResourceLocation(textureName.split(":")[0], "textures/items/" + textureName.split(":")[1] + ".png");
                    }

                    if(texture != null){
                        try {
                            colorBuff = TextureUtil.readImageData(Minecraft.getMinecraft().getResourceManager(), texture);
                            for(int c : colorBuff){
                                rgb=hexTorgba(c);
                                if(rgb[3]>128) {
                                    if(rgb[0]+rgb[1]+rgb[2]>20) {
                                        red+=rgb[2];
                                        blue+=rgb[1];
                                        green+=rgb[0];
                                        divisor++;
                                    }
                                }
                            }
                            ingotColors.put(s, new int[]{red/divisor,blue/divisor,green/divisor});
                        } catch (IOException e) {
                            DebugUtil.println("Caught exception while parsing texture to get color: ");
                            e.printStackTrace();
                        }

                    }


                }
            }
        }
    }

    public static IIcon bindBlockTextureFromSide(int side, ItemStack b){
        IIcon texture = RenderBlocks.getInstance().getBlockIconFromSideAndMetadata(Block.getBlockFromItem(b.getItem()), side,b.getItemDamage());
        if (RenderBlocks.getInstance().hasOverrideBlockTexture()) {
            texture = RenderBlocks.getInstance().overrideBlockTexture;
        }
        return texture;
    }

    public static int[] hexTorgba(int hex){
        return new int[]{hex&0xFF, (hex>>8)&0xFF, (hex>>16)&0xFF, (hex>>24)&0xFF};
    }
}
