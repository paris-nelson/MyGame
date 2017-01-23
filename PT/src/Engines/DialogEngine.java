package Engines;

import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import Enums.EventName;
import Global.Constants;
import Global.GameData;
import KeyListeners.DialogKeyListener;
import acm.graphics.GLabel;

public class DialogEngine {
	private static ArrayList<String> lines;
	private static int linenum;
	private static GLabel label;

	public static void initialize(EventName event){
		loadLines(event);
		linenum=0;
		initLabel();
		updateLabel();
		takeControl();
	}
	
	private static void takeControl(){
		GameData.getGUI().giveControl(new DialogKeyListener());
	}
	
	private static void initLabel(){
		label=new GLabel(lines.get(linenum));
		label.setFont(new Font(Constants.FONT,Font.ITALIC,22));
		GameData.getGUI().add(label);
	}
	
	public static void next(){
		if(linenum<lines.size()-1){
			linenum++;
			updateLabel();
		}
		else{
			//Close? how to progress from here?
			//call a continue method in eventlogic
			//that continues the flow?
			close();
		}
	}
	
	public static void previous(){
		if(linenum>0){
			linenum--;
			updateLabel();
		}
	}
	
	private static void updateLabel(){
		label.setLabel(lines.get(linenum));
	}

	private static void loadLines(EventName event){
		lines=new ArrayList<String>();
		try{
			File f=new File(Constants.PATH+"\\EventData\\Dialogs\\"+event.toString()+".txt");
			Scanner s=new Scanner(f);
			while(s.hasNextLine()){
				lines.add(s.nextLine());
			}
			s.close();
		}catch(Exception e){e.printStackTrace();}
	}
	
	private static void close(){
		GlobalEngine.giveUpControl();
		GameData.getGUI().remove(label);
		label=null;
		lines=null;
	}
}
