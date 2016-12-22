package Engines;

import java.awt.Color;
import java.util.ArrayList;

import Global.GameData;
import KeyListeners.MenuKeyListener;
import Menus.Menu;
import acm.graphics.GCompound;
import acm.graphics.GLabel;
import acm.graphics.GRect;

public class MenuEngine {
	private static Menu menu;
	private static GCompound gmenu;
	private static ArrayList<GRect> goptions;
	private static short menuindex;
	
	public static void initialize(Menu m){
		System.out.println("Initializing menu "+m.getClass().toString());
		menu=m;
		menuindex=0;
		showMenu();
		takeControl();
	}
	
	public static void showMenu(){
		menu.refreshVisibleOptions();
		buildGMenu();
		setFocus(Short.valueOf(menuindex));
	}
	
	public static void refreshMenu(){
		GameData.getGUI().remove(gmenu);
		showMenu();
	}
	
	public static void buildGMenu(){
		goptions=new ArrayList<GRect>();
		gmenu=new GCompound();
		ArrayList<String> options=menu.getVisibleOptions();
		for(int i=0;i<options.size();i++){
			GRect rect=new GRect(0,50*i,200,50);
			rect.setFilled(true);
			rect.setFillColor(Color.WHITE);
			goptions.add(rect);
			gmenu.add(rect);
			GLabel label=new GLabel(options.get(i));
			gmenu.add(label,(rect.getWidth()-label.getWidth())/2,25+50*i);
		}
		GameData.getGUI().add(gmenu,1100,0);
	}
	
	public static void setFocus(short index){
		goptions.get(index).setFillColor(Color.CYAN);
	}
	
	public static void removeFocus(short index){
		goptions.get(index).setFillColor(Color.WHITE);
	}
	
	public static void select(){
		menu.select(menuindex);
	}
	
	public static void moveDown(){
		if(menuindex<menu.getNumOptions()-1){
			removeFocus(menuindex);
			menuindex++;
			setFocus(menuindex);
		}
	}
	
	public static void moveUp(){
		if(menuindex>0){
			removeFocus(menuindex);
			menuindex--;
			setFocus(menuindex);
		}
	}
	
	public static Menu getMenu(){
		return menu;
	}
	
	public static void takeControl(){
		GameData.getGUI().giveControl(new MenuKeyListener());
	}

	public static void close(){
		System.out.println("Closing menu "+menu.getClass().toString());
		GameData.getGUI().remove(gmenu);
	}
}
