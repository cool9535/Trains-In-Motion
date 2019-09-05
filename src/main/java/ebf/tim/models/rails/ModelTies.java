package ebf.tim.models.rails;

import ebf.tim.blocks.rails.RailShapeCore;
import ebf.tim.utility.Vec5f;
import fexcraft.tmt.slim.Tessellator;
import fexcraft.tmt.slim.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

import static ebf.tim.models.rails.Model1x1Rail.addVertexWithOffsetAndUV;

public class ModelTies {


    private static Vec5f getOffsetFromRotation(Vec5f p1){
        Vec5f p2 = new Vec5f(p1.xCoord,p1.yCoord,p1.zCoord,p1.u,p1.v);
        if(p2.v>=270){
            p2.v-=270;
            p2.v*=1.25f;
            p2.v+=270;
        } else if(p2.v>=180){
            p2.v-=180;
            p2.v*=1.25f;
            p2.v+=180;
        } else if(p2.v>=90){
            p2.v-=90;
            p2.v*=1.25f;
            p2.v+=90;
        } else {
            p2.v*=1.25f;
        }
        return p2;
    }


    public static IIcon iicon;
    public static Vec5f p2;
    public static float d0, d1;

    public static void modelPotatoTies(RailShapeCore shape, float maxWidth, float minWidth, ItemStack block){
        GL11.glPushMatrix();
        GL11.glTranslated(0, 0.125, 0);
        //top side
        iicon=  TextureManager.bindBlockTextureFromSide(ForgeDirection.WEST.ordinal(), block);
        boolean first = true;
        for (Vec5f p :shape.activePath) {
            if(first){
                first=false;
                continue;
            }

            Tessellator.getInstance().startDrawing(GL11.GL_QUADS);
            d0 = iicon.getMinU()+ ((iicon.getMaxU()-iicon.getMinU())*0.44f);
            d1 = d0+ ((iicon.getMaxU()-iicon.getMinU())*0.09f);

            p2 = getOffsetFromRotation(p);

            addVertexWithOffsetAndUV(p2, 0.125f + maxWidth, 0, 0.0625f,d0, iicon.getMinV());
            addVertexWithOffsetAndUV(p2, 0.125f + maxWidth, 0, 0.1875f,d1, iicon.getMinV());

            addVertexWithOffsetAndUV(p2, -0.125f + minWidth, 0, 0.1875f,d1,iicon.getMaxV());
            addVertexWithOffsetAndUV(p2, -0.125f + minWidth, 0, 0.0625f,d0, iicon.getMaxV());
            Tessellator.getInstance().arrayEnabledDraw();
        }
        GL11.glPopMatrix();
    }


    public static void model3DTies(RailShapeCore shape, float maxWidth, float minWidth, ItemStack block){

        modelPotatoTies(shape, maxWidth, minWidth, block);

        GL11.glPushMatrix();
        GL11.glTranslated(0, 0.125, 0);
        iicon=  TextureManager.bindBlockTextureFromSide(ForgeDirection.NORTH.ordinal(),block);
        boolean first = true;
        for (Vec5f p :shape.activePath) {
            if(first){
                first=false;
                continue;
            }



            Tessellator.getInstance().startDrawing(GL11.GL_QUADS);
            p2= getOffsetFromRotation(p);
            d1 = iicon.getMinU()+ ((iicon.getMaxU()-iicon.getMinU())*0.09f);

            addVertexWithOffsetAndUV(p2, 0.125f + maxWidth, 0, 0.1875f,iicon.getMinU(),iicon.getMinV());
            addVertexWithOffsetAndUV(p2, 0.125f + maxWidth, -0.125f, 0.1875f,d1,iicon.getMinV());

            addVertexWithOffsetAndUV(p2, -0.125f + minWidth, -0.125f, 0.1875f,iicon.getMinU(),iicon.getMaxV());
            addVertexWithOffsetAndUV(p2, -0.125f + minWidth, 0, 0.1875f,d1,iicon.getMaxV());

            Tessellator.getInstance().arrayEnabledDraw();
        }

        iicon=  TextureManager.bindBlockTextureFromSide(ForgeDirection.SOUTH.ordinal(), block);
        first = true;
        for (Vec5f p :shape.activePath) {
            if(first){
                first=false;
                continue;
            }

            Tessellator.getInstance().startDrawing(GL11.GL_QUADS);
            d1 = iicon.getMinU()+ ((iicon.getMaxU()-iicon.getMinU())*0.09f);


            p2= getOffsetFromRotation(p);

            addVertexWithOffsetAndUV(p2, 0.125f + maxWidth, -0.125f, 0.0625f,iicon.getMinU(),iicon.getMinV());
            addVertexWithOffsetAndUV(p2, 0.125f + maxWidth, 0, 0.0625f,d1,iicon.getMinV());

            addVertexWithOffsetAndUV(p2, -0.125f + minWidth, 0, 0.0625f,d1,iicon.getMaxV());
            addVertexWithOffsetAndUV(p2, -0.125f + minWidth, -0.125f, 0.0625f,iicon.getMinU(),iicon.getMaxV());
            Tessellator.getInstance().arrayEnabledDraw();
        }


        iicon=  TextureManager.bindBlockTextureFromSide(ForgeDirection.UP.ordinal(), block);
        first = true;
        for (Vec5f p :shape.activePath) {
            if(first){
                first=false;
                continue;
            }

            p2= getOffsetFromRotation(p);
            Tessellator.getInstance().startDrawing(GL11.GL_QUADS);

            addVertexWithOffsetAndUV(p2, 0.125f + maxWidth, -0.125f, 0.0625f,iicon.getMinU(),iicon.getMinV());
            addVertexWithOffsetAndUV(p2, 0.125f + maxWidth, -0.125f, 0.1875f,iicon.getMinU(),iicon.getMaxV());

            addVertexWithOffsetAndUV(p2, 0.125f + maxWidth, -0, 0.1875f,iicon.getMaxU(),iicon.getMaxV());
            addVertexWithOffsetAndUV(p2, 0.125f + maxWidth, -0, 0.0625f,iicon.getMaxU(),iicon.getMinV());
            Tessellator.getInstance().arrayEnabledDraw();
        }

        iicon=  TextureManager.bindBlockTextureFromSide(ForgeDirection.DOWN.ordinal(), block);
        first = true;
        for (Vec5f p :shape.activePath) {
            if(first){
                first=false;
                continue;
            }

            p2= getOffsetFromRotation(p);

            Tessellator.getInstance().startDrawing(GL11.GL_QUADS);

            addVertexWithOffsetAndUV(p2, -0.125f + minWidth, -0.125f, 0.1875f,iicon.getMinU(),iicon.getMinV());
            addVertexWithOffsetAndUV(p2, -0.125f + minWidth, -0.125f, 0.0625f,iicon.getMinU(),iicon.getMaxV());

            addVertexWithOffsetAndUV(p2, -0.125f + minWidth, -0, 0.0625f,iicon.getMaxU(),iicon.getMaxV());
            addVertexWithOffsetAndUV(p2, -0.125f + minWidth, -0, 0.1875f,iicon.getMaxU(),iicon.getMinV());
            Tessellator.getInstance().arrayEnabledDraw();
        }


        GL11.glPopMatrix();


    }

    public void modelHDTies(RailShapeCore shape, float maxWidth, float minWidth, ItemStack block){
        model3DTies(shape, maxWidth, minWidth, block);
        //todo HD ties should all the nails
    }
}
