package Engines;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import Enums.ItemType;
import Enums.PartyMenuMode;
import Global.Constants;
import Global.GameData;
import Global.PlayerData;
import KeyListeners.InventoryKeyListener;
import Menus.PartyMenu;
import acm.graphics.GCompound;
import acm.graphics.GLabel;
import acm.graphics.GObject;
import acm.graphics.GRect;

public class InventoryEngine {

	private static ArrayList<GCompound> inventory;
	private static ArrayList<Integer> ids;
	private static GCompound screen;
	private static int offset;
	private static int focusindex;
	private static int chosenid;
	private static boolean battle;
	private static GRect button;

	public static void initialize(boolean inbattle){
		battle=inbattle;
		screen=new GCompound();
		offset=0;
		focusindex=0;
		chosenid=-1;
		init();
		takeInventory();
		showInventory();
		addFocus();
		takeControl();
	}

	private static void init(){
		GRect bg=new GRect(500,500);
		bg.setFilled(true);
		bg.setFillColor(Color.WHITE);
		screen.add(bg,0,0);
		button=new GRect(200,50);
		button.setFilled(true);
		button.setFillColor(Color.WHITE);
		GLabel label=new GLabel("Exit");
		label.setFont(new Font("Dialog",Font.BOLD,22));
		screen.add(button,300,225);
		screen.add(label,300+(200-label.getWidth())/2,225+label.getHeight()+(50-label.getHeight())/2);
	}

	private static void takeInventory(){
		inventory=new ArrayList<GCompound>();
		ids=new ArrayList<Integer>();
		for(int i=1;i<Constants.NUM_ITEMS+1;i++){
			int quantity=PlayerData.getItemQuantity(i);
			if(quantity>0){
				if((battle&&GameData.isUsableInBattle(i))||(!battle&&GameData.isUsableOutOfBattle(i))){
					GCompound item=new GCompound();
					GRect box=new GRect(200,50);
					box.setFilled(true);
					box.setFillColor(Color.WHITE);
					GLabel namelabel=new GLabel(GameData.getItemName(i));
					namelabel.setFont(new Font("Dialog",Font.PLAIN,22));
					GLabel quantlabel=new GLabel(quantity+"");
					quantlabel.setFont(new Font("Dialog",Font.PLAIN,22));
					item.add(box,0,0);
					item.add(namelabel,5,namelabel.getHeight()+(box.getHeight()-namelabel.getHeight())/2);
					item.add(quantlabel,box.getWidth()-quantlabel.getWidth()-5,namelabel.getY());
					ids.add(i);
					inventory.add(item);
				}
			}
		}
	}

	private static void showInventory(){
		if(screen!=null)
			GameData.getGUI().remove(screen);
		screen=new GCompound();
		init();
		for(int i=offset;i<offset+10&&i<ids.size();i++){
			screen.add(inventory.get(i),0,(i-offset)*50);
		}
		GameData.getGUI().add(screen);
	}

	public static void takeControl(){
		GameData.getGUI().giveControl(new InventoryKeyListener());
	}

	public static void moveUp() {
		if(focusindex>0){
			removeFocus();
			focusindex--;
			addFocus();
		}
		else if(focusindex==0&&offset>0){
			offset-=10;
			takeInventory();
			showInventory();
			focusindex=9;
			addFocus();
		}
	}

	public static void moveDown() {
		if(focusindex<Math.min(9,inventory.size()-offset-1)){
			removeFocus();
			focusindex++;
			addFocus();
		}
		else if(inventory.size()>offset+focusindex+1){
			offset+=10;
			takeInventory();
			showInventory();
			focusindex=0;
			addFocus();
		}
	}

	public static void moveLeft(){
		if(focusindex==-1){
			removeFocus();
			focusindex=0;
			addFocus();
		}
	}

	public static void moveRight(){
		if(focusindex>=0){
			removeFocus();
			focusindex=-1;
			addFocus();
		}
	}

	public static int getChosenId(){
		return chosenid;
	}

	public static void select() {
		if(focusindex==-1){
			InventoryEngine.close();
			//TODO: Change with logic to save/load battleengine
			if(battle)
				BattleEngine.initialize(BattleEngine.currOpponent());
			else
				MapEngine.initialize(PlayerData.getLocation());
		}
		else{
			chosenid=ids.get(focusindex+offset);
			ItemType type=GameData.getItemType(chosenid);
			if(GameData.isHeldItem(chosenid)){
				MenuEngine.initialize(new PartyMenu(PartyMenuMode.GIVE));
			}
			else if(type==ItemType.BALL){

			}
			else if(type==ItemType.REMATCHER){
				InventoryEngine.close();
				MapEngine.initialize(PlayerData.getLocation());
				GlobalEngine.useItem(chosenid);
			}
			else if(type==ItemType.REPEL){
				GlobalEngine.useItem(chosenid);
				cleanUp();
			}
			else{
				if(!battle)
					MenuEngine.initialize(new PartyMenu(PartyMenuMode.USE));
				else
					GlobalEngine.useItem(chosenid,BattleEngine.getActivePokemon());
			}
		}
	}

	public static void cleanUp(){
		takeInventory();
		showInventory();
		if(focusindex+offset==inventory.size())
			focusindex--;
		addFocus();
		takeControl();
	}

	private static void addFocus(){
		if(focusindex>=0){
			GObject item=inventory.get(offset+focusindex).getElement(0);
			GRect box=(GRect)item;
			box.setFillColor(Color.CYAN);
		}
		else if(focusindex==-1)
			button.setFillColor(Color.CYAN);
	}

	private static void removeFocus(){
		if(focusindex>=0){
			GObject item=inventory.get(offset+focusindex).getElement(0);
			GRect box=(GRect)item;
			box.setFillColor(Color.WHITE);
		}
		else if(focusindex==-1)
			button.setFillColor(Color.WHITE);
	}

	public static void close(){
		GameData.getGUI().remove(screen);
	}

}
