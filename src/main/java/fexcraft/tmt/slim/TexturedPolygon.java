package fexcraft.tmt.slim;

import ebf.tim.utility.DebugUtil;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.glGetError;


public class TexturedPolygon {

	public PositionTransformVertex[] vertices;
	
	public TexturedPolygon(PositionTransformVertex apositionTexturevertex[]){
		vertices = apositionTexturevertex;
    }

	/**
	 * function disabled, normals aren't needed anymore
	 */
	@Deprecated
	public void setInvertNormal(boolean isSet){ }

	/**
	 * function disabled, normals aren't needed anymore
	 */
	@Deprecated
	public void setNormals(float x, float y, float z){ }

	/**
	 * function disabled, normals aren't needed anymore
	 */
	@Deprecated
	public void setNormals(ArrayList<Vec3f> iNormal){ }

	public void draw(float f){

		DebugUtil.printGLError(glGetError());
		switch (vertices.length){
			case 3:{
				Tessellator.getInstance().startDrawing(GL11.GL_TRIANGLES);
				break;
			}
			case 4:{
				Tessellator.getInstance().startDrawing(GL11.GL_QUADS);
				break;
			}
			default:{
				Tessellator.getInstance().startDrawing(GL11.GL_POLYGON);
			}
		}

        for (PositionTransformVertex positionTexturevertex : vertices){
        	Tessellator.getInstance().addVertexWithUV(positionTexturevertex.vector3F.xCoord * f, positionTexturevertex.vector3F.yCoord * f, positionTexturevertex.vector3F.zCoord * f, positionTexturevertex.textureX, positionTexturevertex.textureY);
		}
		DebugUtil.printGLError(glGetError());
		Tessellator.getInstance().arrayEnabledDraw();
    }


	public void flipFace() {
		PositionTransformVertex[] apositiontexturevertex = new PositionTransformVertex[this.vertices.length];

		for (int i = 0; i < this.vertices.length; ++i) {
			apositiontexturevertex[i] = this.vertices[this.vertices.length - i - 1];
		}

		this.vertices = apositiontexturevertex;
	}

}
