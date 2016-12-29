package Engines;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import Enums.PartyMenuMode;
import Global.GameData;
import Global.PlayerData;
import KeyListeners.PCKeyListener;
import Menus.PCMenu;
import Menus.PartyMenu;
import acm.graphics.GCompound;
import acm.graphics.GLabel;
import acm.graphics.GRect;

public class PCEngine {
	
	private static int num;
	private static ArrayList<GRect> goptions;
	private static GCompound gmenu;

	public static void initialize(){
		System.out.println("Entering PC");
		num=1;
		goptions=new ArrayList<GRect>();
		gmenu=new GCompound();
		showAllPokemonList();
		setFocus(0);
		takeControl();
		//TODO: handle logic for adding pokemon to pc
	}
	
	public static void takeControl(){
		GameData.getGUI().giveControl(new PCKeyListener());
	}
	
	public static void close(){
		System.out.println("Exiting PC");
		GameData.getGUI().remove(gmenu);
	}
	
	public static void showAllPokemonList(){
		GUI gui=GameData.getGUI();
		int count=1;
		for(int x=0;x<10;x++){
			for(int y=0;y<25;y++){
				GRect rect=new GRect(115*x,22*y,115,22);
				rect.setFilled(true);
				if(y%2==0)
					rect.setFillColor(Color.WHITE);
				else if(y%2==1)
					rect.setFillColor(Color.LIGHT_GRAY);
				goptions.add(rect);
				gmenu.add(rect);
				GLabel label=new GLabel("#"+count+": "+GameData.getName(count));
				label.setFont(new Font("Times New Roman",Font.PLAIN,16));
				gmenu.add(label,(rect.getWidth()-label.getWidth())/2+115*x,22+22*y-(rect.getHeight()-label.getHeight()));
				count++;
			}
		}
		GRect rect=new GRect(1150,0,115,22);
		rect.setFilled(true);
		rect.setFillColor(Color.WHITE);
		goptions.add(rect);
		GLabel label=new GLabel("#251: Celebi");
		label.setFont(new Font("Times New Roman",Font.PLAIN,16));
		GRect rect2=new GRect(1150,22,115,22);
		rect2.setFilled(true);
		rect2.setFillColor(Color.LIGHT_GRAY);
		goptions.add(rect2);
		GLabel label2=new GLabel("Deposit");
		label2.setFont(new Font("Times New Roman",Font.PLAIN,16));
		GRect rect3=new GRect(1150,44,115,22);
		rect3.setFilled(true);
		rect3.setFillColor(Color.WHITE);
		goptions.add(rect3);
		GLabel label3=new GLabel("Exit P.C.");
		label3.setFont(new Font("Times New Roman",Font.PLAIN,16));
		gmenu.add(rect);
		gmenu.add(rect2);
		gmenu.add(rect3);
		gmenu.add(label,1150+(rect.getWidth()-label.getWidth())/2,22-(rect.getHeight()-label.getHeight()));
		gmenu.add(label2,1150+(rect.getWidth()-label.getWidth())/2,44-(rect.getHeight()-label.getHeight()));
		gmenu.add(label3,1150+(rect.getWidth()-label.getWidth())/2,66-(rect.getHeight()-label.getHeight()));
		gui.add(gmenu);
	}
	
	public static void setFocus(int index){
		goptions.get(index).setFillColor(Color.CYAN);
	}
	
	public static void removeFocus(int index){
		Color color;
		if(index>25)
			color=goptions.get(index-25).getFillColor();
		else
			color=goptions.get(index+25).getFillColor();
		goptions.get(index).setFillColor(color);
	}
	
	public static void moveUp(){
		if(num>1){
			removeFocus(num-1);
			num--;
			setFocus(num-1);
		}
	}
	
	public static void moveDown(){
		if(num<253){
			removeFocus(num-1);
			num++;
			setFocus(num-1);
		}
	}
	
	public static void moveLeft(){
		if(num>25){
			removeFocus(num-1);
			num-=25;
			setFocus(num-1);
		}
	}
	
	public static void moveRight(){
		if(num<229){
			removeFocus(num-1);
			num+=25;
			setFocus(num-1);
		}
	}
	
	public static void select(){
		if(num==253){
			close();
			MapEngine.initialize(PlayerData.getLocation());
		}
		else if(num==252){
			MenuEngine.initialize(new PartyMenu(PartyMenuMode.DEPOSIT));
		}
		else{
			MenuEngine.initialize(new PCMenu(num));
		}
	}
	
}
