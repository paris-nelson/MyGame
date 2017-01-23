package Engines;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Enums.Control;
import Global.Constants;
import Global.ControlsConfig;
import Global.GameData;
import KeyListeners.ControlsKeyListener;
import Menus.PlayerMenu;
import acm.graphics.GCompound;
import acm.graphics.GLabel;

public class ControlsEngine {

	private static GCompound screen;
	private static int index;
	private static boolean selected;
	private static GLabel[] labels;
	private static Control[] controls;
	private static ArrayList<Integer> keys;

	public static void initialize(){
		convertControlsMap();
		initScreen();
		index=0;
		selected=false;
		takeControl();
	}

	private static void initScreen(){
		screen=new GCompound();
		labels=new GLabel[controls.length];
		//TODO:
		GUI gui=GameData.getGUI();
		gui.add(screen);
		for(GLabel gl:labels){
			gui.add(gl);
		}
	}

	private static void convertControlsMap(){
		HashMap<Control,Integer> map=ControlsConfig.getControls();
		controls=new Control[map.size()];
		keys=new ArrayList<Integer>();
		int counter=0;
		for(Control c:map.keySet()){
			controls[counter]=c;
			keys.add(map.get(c));
			counter++;
		}
	}

	private static Map<Control,Integer> convertControlsArray(){
		HashMap<Control,Integer> map=new HashMap<Control,Integer>();
		for(int i=0;i<controls.length;i++){
			map.put(controls[i],keys.get(i));
		}
		return map;
	}

	private static void takeControl(){
		GameData.getGUI().giveControl(new ControlsKeyListener());
	}

	/**
	 * Maps the given key to the currently selected control. Current implementation will wipe any control currently
	 * assigned this key first.
	 * @param newkey
	 */
	public static void map(int newkey){
		selected=false;
		int dupindex=keys.indexOf(newkey);
		if(dupindex>-1&&dupindex!=index)
			keys.set(dupindex,-1);
		keys.set(index,newkey);
		updateDisplay();
	}

	private static void updateDisplay(){
		for(int i=0;i<labels.length;i++){
			labels[i].setLabel(KeyEvent.getKeyText(keys.get(i)));
		}
	}

	public static boolean isSelected(){
		return selected;
	}
	
	private static void removeFocus(){
		labels[index].setColor(Color.BLACK);
		labels[index].setFont(new Font(Constants.FONT,Font.PLAIN,22));
	}
	
	private static void addFocus(){
		labels[index].setColor(Color.RED);
		labels[index].setFont(new Font(Constants.FONT,Font.BOLD,22));
	}

	public static void next(){
		if(index<controls.length-1){
			removeFocus();
			index++;
			addFocus();
		}
	}

	public static void previous(){
		if(index>0){
			removeFocus();
			index--;
			addFocus();
		}
	}

	public static void select(){
		selected=true;

	}

	private static void close(){
		if(screen!=null)
			GameData.getGUI().remove(screen);
		if(labels!=null){
			for(GLabel gl:labels){
				GameData.getGUI().remove(gl);
			}
		}
		//send new controls to map and send to controlsconfig
		ControlsConfig.setControls(convertControlsArray());
		screen=null;
		controls=null;
		keys=null;
		labels=null;
		ControlsConfig.save();
		//currently assuming controls will be called only from map player menu
		MenuEngine.initialize(new PlayerMenu());
	}
}
