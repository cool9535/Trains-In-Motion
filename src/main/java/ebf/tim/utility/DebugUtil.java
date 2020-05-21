package ebf.tim.utility;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.launchwrapper.Launch;

/**
 * @author Ferdinand
 * Fex's debugging util;
 */
public class DebugUtil {
	
	private static Boolean dev;
	
	public static boolean dev(){
		if(dev == null){
			dev = (Boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment");
		}
		return dev;
	}

	/**
	 * Replicated method of System.out.println that supports an array of data and only prints in a dev environment.
	 * @param o
	 */
	public static void println(Object... o){
		if(dev()){
			System.out.println("------------------TiM Debug------------------");
			System.out.println(Thread.currentThread().getStackTrace()[2]);//print what function just called this
			for (Object obj : o){
				System.out.println(obj);
			}
			System.out.println("------------------TiM Debug------------------");
		}
	}

	@SideOnly(Side.CLIENT)
	public static void printGLError(int glerror){
		if(glerror!=0x0){
			System.out.println("------------------TiM Debug------------------");
			System.out.println(Thread.currentThread().getStackTrace()[2]);//print what function just called this
			System.out.println(org.lwjgl.util.glu.GLU.gluErrorString(glerror));

			printStackTrace();
			System.out.println("------------------TiM Debug------------------");
		}
	}
	public static void println(Object o){
		if(dev()){
			System.out.println("------------------TiM Debug------------------");
			System.out.println(Thread.currentThread().getStackTrace()[2]);//print what function just called this
			System.out.println(o);
			System.out.println("------------------TiM Debug------------------");
		}
	}


	public static void printStackTrace(){
		if(dev()) {
			System.out.println("------------------TiM Debug------------------");
			for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
				System.out.println(e);
			}
			System.out.println("------------------TiM Debug------------------");
		}
	}

	public static void throwStackTrace(){
		if(dev()) {
			System.out.println("------------------TiM Debug------------------");
			for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
				System.out.println(e);
			}
			System.out.println("------------------TiM Debug------------------");
			System.exit(-1);
		}
	}

	/**
	 * Generic logging method, meant for using an actual minecraft logger;
	 * @param obj the object to be logged or string
	 */
	@Deprecated
	public static void log(Object obj){
		if(dev()){
			//logger.info(String.valueOf(obj));
			System.out.println(obj);
		}
	}
	
	//CODE BELLOW COPY/EDITED FROM FCL
	
	/**
	 * Alternative to {@link #halt()} or {@link #stop()} which will only work in a developement workspace, nice for debugging.
	 * <br>
	 * Which also prints the caller classes into console.
	 * <br>
	 * See also {@link #dev()}
	 */
	public static void exception(int i, String string, boolean halt){
		Exception ex = new Exception();
		for(int j = i; j > 0; j--){
			StackTraceElement elm = ex.getStackTrace()[j];
			log("{" + elm.getClassName() + "#"  + elm.getMethodName() + " [LINE: " + elm.getLineNumber() + "]}");
		}
		if(string != null){
			log(string);
		}
		if(dev() && halt){
			halt();
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public static Side side(){
		return FMLCommonHandler.instance().getSide();
	}
	
	/**
	 * Simple method to halt the current Minecraft Instance, "force close".
	 */
	public static void halt(){
		halt(1);
	}
	
	public static void halt(int errc){
		FMLCommonHandler.instance().exitJava(errc, true);
	}
	
}