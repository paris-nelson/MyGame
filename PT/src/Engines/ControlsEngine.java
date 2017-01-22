package Engines;

import Global.ControlsConfig;
import Global.GameData;
import Menus.PlayerMenu;
import acm.graphics.GCompound;

public class ControlsEngine {
	
	private static GCompound screen;
	private static int index;
	private static boolean selected;

	public static void initialize(){
		
	}
	
	private static void takeControl(){
		
	}
	
	public static void map(int newkey){
		
	}
	
	public static boolean isSelected(){
		return selected;
	}
	
	private static void close(){
		if(screen!=null)
			GameData.getGUI().remove(screen);
		ControlsConfig.save();
		//currently assuming controls will be called only from map player menu
		MenuEngine.initialize(new PlayerMenu());
	}
}
