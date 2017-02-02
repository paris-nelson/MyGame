package Engines;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import Enums.Control;
import Global.Constants;
import Global.ControlsConfig;
import Global.GameData;
import KeyListeners.ControlsKeyListener;
import Menus.OptionsMenu;
import acm.graphics.GCompound;
import acm.graphics.GLabel;
import acm.graphics.GRect;

public class ControlsEngine {

	private static GCompound screen;
	//index for up/down list of controls
	private static int udindex;
	//index for left/right list of buttons/controls
	private static int lrindex;
	private static boolean selected;
	private static GLabel[] labels;
	private static GRect[] boxes;
	private static GRect submitbutton;
	private static GRect undobutton;
	private static GRect defaultsbutton;
	private static Control[] controls;
	private static ArrayList<Integer> keys;

	public static void initialize(){
		convertControlsMap(ControlsConfig.getControls());
		initScreen();
		udindex=0;
		lrindex=0;
		selected=false;
		addFocus();
		takeControl();
	}

	private static void initScreen(){
		screen=new GCompound();
		labels=new GLabel[controls.length];
		boxes=new GRect[controls.length];
		GRect bg=new GRect(Constants.SCREEN_WIDTH,Constants.SCREEN_HEIGHT);
		bg.setFilled(true);
		bg.setFillColor(Color.WHITE);
		screen.add(bg);
		for(int i=0;i<controls.length;i++){
			GLabel controllabel=new GLabel(controls[i].toString());
			controllabel.setFont(new Font(Constants.FONT,Font.PLAIN,24));
			GLabel keylabel=new GLabel(KeyEvent.getKeyText(keys.get(i)));
			keylabel.setFont(new Font(Constants.FONT,Font.PLAIN,22));
			GRect box=new GRect(keylabel.getWidth()+10,keylabel.getHeight()+10);
			screen.add(controllabel,100,100*i+controllabel.getHeight());
			screen.add(keylabel,controllabel.getX()+controllabel.getWidth()+100,100*i+controllabel.getHeight());
			screen.add(box,keylabel.getX()-5,keylabel.getY()-2-keylabel.getHeight());
			labels[i]=keylabel;
			boxes[i]=box;
		}
		undobutton=new GRect(100,Constants.SCREEN_HEIGHT-200,160,50);
		defaultsbutton=new GRect(undobutton.getX()+undobutton.getWidth()+50,undobutton.getY(),160,50);
		submitbutton=new GRect(defaultsbutton.getX()+defaultsbutton.getWidth()+50,defaultsbutton.getY(),160,50);
		undobutton.setFilled(true);
		undobutton.setFillColor(Color.WHITE);
		defaultsbutton.setFilled(true);
		defaultsbutton.setFillColor(Color.WHITE);
		submitbutton.setFilled(true);
		submitbutton.setFillColor(Color.WHITE);
		screen.add(undobutton);
		screen.add(defaultsbutton);
		screen.add(submitbutton);
		GLabel undolabel=new GLabel("Undo Changes");
		undolabel.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		screen.add(undolabel,undobutton.getX()+(undobutton.getWidth()-undolabel.getWidth())/2,undobutton.getY()+undobutton.getHeight()-5-(undobutton.getHeight()-undolabel.getHeight())/2);
		GLabel defaultslabel=new GLabel("Reset Defaults");
		defaultslabel.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		screen.add(defaultslabel,defaultsbutton.getX()+(defaultsbutton.getWidth()-defaultslabel.getWidth())/2,defaultsbutton.getY()+defaultsbutton.getHeight()-5-(defaultsbutton.getHeight()-defaultslabel.getHeight())/2);
		GLabel submitlabel=new GLabel("Submit Changes");
		submitlabel.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		screen.add(submitlabel,submitbutton.getX()+(submitbutton.getWidth()-submitlabel.getWidth())/2,submitbutton.getY()+submitbutton.getHeight()-5-(submitbutton.getHeight()-submitlabel.getHeight())/2);
		GUI gui=GameData.getGUI();
		gui.add(screen);
		for(GLabel gl:labels){
			gui.add(gl);
		}
	}

	private static void convertControlsMap(HashMap<Control,Integer> currcontrols){
		HashMap<Control,Integer> map=currcontrols;
		controls=new Control[map.size()];
		keys=new ArrayList<Integer>();
		int counter=0;
		for(Control c:map.keySet()){
			controls[counter]=c;
			keys.add(map.get(c));
			counter++;
		}
	}

	private static LinkedHashMap<Control,Integer> convertControlsArray(){
		LinkedHashMap<Control,Integer> map=new LinkedHashMap<Control,Integer>();
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
		if(dupindex>-1&&dupindex!=udindex)
			keys.set(dupindex,-1);
		keys.set(udindex,newkey);
		updateDisplay();
	}

	private static void updateDisplay(){
		//GUI gui=GameData.getGUI();
		for(int i=0;i<labels.length;i++){
			if(keys.get(i)==-1){
				labels[i].setLabel(" ");
				continue;
			}
			screen.remove(labels[i]);
			labels[i].setLabel(KeyEvent.getKeyText(keys.get(i)));
			screen.add(labels[i]);
			GLabel label=labels[i];
			screen.remove(boxes[i]);
			boxes[i]=new GRect(label.getX()-5,label.getY()-2-label.getHeight(),label.getWidth()+10,label.getHeight()+10);
			screen.add(boxes[i]);
		}
	}

	public static boolean isSelected(){
		return selected;
	}

	private static void removeFocus(){
		if(isOnControlsList()){
			labels[udindex].setColor(Color.BLACK);
			labels[udindex].setFont(new Font(Constants.FONT,Font.PLAIN,22));
		}
		else if(lrindex==1){
			undobutton.setFillColor(Color.WHITE);
		}
		else if(lrindex==2){
			defaultsbutton.setFillColor(Color.WHITE);
		}
		else if(lrindex==3){
			submitbutton.setFillColor(Color.WHITE);
		}
	}

	private static void addFocus(){
		if(isOnControlsList()){
			labels[udindex].setColor(Color.BLUE);
			labels[udindex].setFont(new Font(Constants.FONT,Font.BOLD,22));
		}
		else if(lrindex==1){
			undobutton.setFillColor(Color.CYAN);
		}
		else if(lrindex==2){
			defaultsbutton.setFillColor(Color.CYAN);
		}
		else if(lrindex==3){
			submitbutton.setFillColor(Color.CYAN);
		}
	}

	public static void next(){
		if(isOnControlsList()){
			if(udindex<controls.length-1){
				removeFocus();
				udindex++;
				addFocus();
			}
			else{
				removeFocus();
				lrindex=1;
				addFocus();
			}
		}
	}

	public static void previous(){
		if(isOnControlsList()){
			if(udindex>0){
				removeFocus();
				udindex--;
				addFocus();
			}
		}
		else{
			removeFocus();
			lrindex=0;
			addFocus();
		}
	}

	public static boolean isOnControlsList(){
		return lrindex==0;
	}

	public static void moveLeft(){
		if(!isOnControlsList()&&lrindex>1){
			removeFocus();
			lrindex--;
			addFocus();
		}
	}

	public static void moveRight(){
		if(!isOnControlsList()&&lrindex<3){
			removeFocus();
			lrindex++;
			addFocus();
		}
	}

	public static void select(){
		if(isOnControlsList())
			selected=true;
		else if(lrindex==1)
			undo();
		else if(lrindex==2)
			resetDefaults();
		else if(lrindex==3)
			submit();
	}

	private static void undo(){
		convertControlsMap(ControlsConfig.getControls());
		updateDisplay();
	}

	private static void resetDefaults(){
		convertControlsMap(ControlsConfig.getDefaults());
		updateDisplay();
	}

	private static void submit(){
		if(!keys.contains(new Integer(-1)))
			close();
		else{
			for(int i=0;i<keys.size();i++){
				if(keys.get(i)==-1)
					boxes[i].setColor(Color.RED);
			}
			updateDisplay();
		}
	}

	private static void close(){
		GlobalEngine.giveUpControl();
		GUI gui=GameData.getGUI();
		if(screen!=null)
			gui.remove(screen);
		if(labels!=null){
			for(GLabel gl:labels)
				gui.remove(gl);
		}
		//send new controls to map and send to controlsconfig
		ControlsConfig.setControls(convertControlsArray());
		screen=null;
		controls=null;
		keys=null;
		labels=null;
		boxes=null;
		submitbutton=null;
		undobutton=null;
		defaultsbutton=null;
		ControlsConfig.save();
		MenuEngine.initialize(new OptionsMenu());
	}
}
