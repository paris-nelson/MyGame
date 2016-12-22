package Engines;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import Enums.Requirement;
import Global.Constants;
import Global.GameData;
import Global.PlayerData;
import KeyListeners.ShopKeyListener;
import acm.graphics.GCompound;
import acm.graphics.GLabel;
import acm.graphics.GLine;
import acm.graphics.GRect;

public class ShopEngine {

	public enum Mode {
		BUY, SELL, TRANSACTION, COMMIT, EXIT, CLEAR
	}; 

	private static int[] buyvals;
	private static int[] sellvals;
	private static Requirement[] requirements;
	private static ArrayList<Integer> shopIds;
	private static ArrayList<Integer> playerIds;
	private static ArrayList<Integer> sellIds;
	private static ArrayList<Integer> buyIds;
	private static ArrayList<Integer> sellQuants;
	private static ArrayList<Integer> buyQuants;
	private static ArrayList<GCompound> shopInventory;
	private static ArrayList<GCompound> playerInventory;
	private static ArrayList<GCompound> transactionInventory;
	private static GCompound screen;
	private static GCompound sellportion;
	private static GCompound transactionportion;
	private static Mode mode;
	private static int focusindex;
	private static int playerInventoryOffset;
	private static int transactionOffset;
	private static GRect clearbox;
	private static GRect commitbox;
	private static GRect exitbox;
	private static int total;
	private static GLabel buy1pt2;
	private static GLabel footer;

	public static void initialize(){

		for(int i=0;i<133;i++){
			PlayerData.addItem(i,2);
		}

		System.out.println("Initializing shop");
		if(buyvals==null||sellvals==null)
			loadItemVals();
		initScreen();
		takeStoreInventory();
		takePlayerInventory();
		GameData.getGUI().add(screen);
		initVariables();
		showPlayerInventory();
		showTransactionWindow();
		mode=Mode.BUY;
		focusindex=0;
		addFocus();
		takeControl();
	}

	private static void initScreen(){
		screen=new GCompound();		
		GRect bg=new GRect(0,0,1000,580);
		bg.setFilled(true);
		bg.setFillColor(Color.WHITE);
		screen.add(bg);
		GLabel label=new GLabel("SELL");
		label.setFont(new Font("Dialog",Font.BOLD,20));
		screen.add(label,630+(245-label.getWidth())/2,label.getHeight());
		GLabel label2=new GLabel("BUY");
		label2.setFont(new Font("Dialog",Font.BOLD,20));
		screen.add(label2,(180-label2.getWidth())/2,label2.getHeight());
		GLabel label3=new GLabel("TRANSACTION");
		label3.setFont(new Font("Dialog",Font.BOLD,20));
		screen.add(label3,275+(245-label3.getWidth())/2,label3.getHeight());
		GLabel header1=new GLabel("Item Name");
		GLabel header2=new GLabel("Cost");
		GLabel header3=new GLabel("Qty");
		header1.setFont(new Font("Dialog",Font.ITALIC,17));
		header2.setFont(new Font("Dialog",Font.ITALIC,17));
		header3.setFont(new Font("Dialog",Font.ITALIC,17));
		screen.add(header1,630,header1.getHeight()+31);
		screen.add(header2,755,header1.getY());
		screen.add(header3,820,header1.getY());
		GLabel header4=new GLabel("Item Name");
		GLabel header5=new GLabel("Cost");
		GLabel header6=new GLabel("Qty");
		header4.setFont(new Font("Dialog",Font.ITALIC,17));
		header5.setFont(new Font("Dialog",Font.ITALIC,17));
		header6.setFont(new Font("Dialog",Font.ITALIC,17));
		screen.add(header4,275,header1.getHeight()+31);
		screen.add(header5,400,header1.getY());
		screen.add(header6,465,header1.getY());
		GLabel clear=new GLabel("Clear");
		clear.setFont(new Font("Dialog",Font.ITALIC,18));
		clearbox=new GRect(200,508,75,30);
		clearbox.setFilled(true);
		clearbox.setFillColor(Color.WHITE);
		screen.add(clearbox);
		screen.add(clear,214,530);
		GLabel commit=new GLabel("Commit Transaction");
		commit.setFont(new Font("Dialog",Font.ITALIC,18));
		commitbox=new GRect(300,508,175,30);
		commitbox.setFilled(true);
		commitbox.setFillColor(Color.WHITE);
		screen.add(commitbox);
		screen.add(commit,306,530);
		GLabel exit=new GLabel("Exit Shop");
		exit.setFont(new Font("Dialog",Font.ITALIC,18));
		exitbox=new GRect(500,508,100,30);
		exitbox.setFilled(true);
		exitbox.setFillColor(Color.WHITE);
		screen.add(exitbox);
		screen.add(exit,512,530);
		footer=new GLabel("");
		footer.setFont(new Font("Dialog",Font.PLAIN,20));
		screen.add(footer,10,565);
	}

	private static void initVariables(){
		playerInventoryOffset=0;
		transactionOffset=0;
		buyIds=new ArrayList<Integer>();
		sellIds=new ArrayList<Integer>();
		buyQuants=new ArrayList<Integer>();
		sellQuants=new ArrayList<Integer>();
	}

	private static void loadItemVals(){
		buyvals=new int[Constants.NUM_ITEMS+1];
		sellvals=new int[Constants.NUM_ITEMS+1];
		requirements=new Requirement[Constants.NUM_ITEMS+1];
		File f=new File("InitializeData\\itemvals.txt");
		try{
			Scanner s=new Scanner(f);
			for(int i=1;i<buyvals.length;i++){
				String line=s.nextLine();
				String[] split=line.split(",");
				buyvals[i]=Integer.parseInt(split[0]);
				sellvals[i]=Integer.parseInt(split[1]);
				requirements[i]=Requirement.valueOf(split[2]);
			}
			s.close();
		}catch(Exception e){e.printStackTrace();}
	}

	private static void takeStoreInventory(){		
		shopIds=new ArrayList<Integer>();
		shopInventory=new ArrayList<GCompound>();
		int count=0;
		double bottom=0;
		for(int i=0;i<Constants.NUM_ITEMS+1;i++){
			int buyval=buyvals[i];
			if(i==0){
				count++;
				GLabel header1=new GLabel("Item Name");
				GLabel header2=new GLabel("Cost");
				header1.setFont(new Font("Dialog",Font.ITALIC,17));
				header2.setFont(new Font("Dialog",Font.ITALIC,17));
				screen.add(header1,5,header1.getHeight()*count+31);
				screen.add(header2,130,header1.getY());
			}
			else if(buyval>=0&&PlayerData.hasMetRequirement(requirements[i])){
				count++;
				GLabel label1=new GLabel(GameData.getItemName(i));
				GLabel label2=new GLabel("$"+buyval);
				label1.setFont(new Font("Dialog",Font.PLAIN,18));
				label2.setFont(new Font("Dialog",Font.PLAIN,18));
				GCompound item=new GCompound();
				item.add(label1,5,label1.getHeight()*count+31);
				item.add(label2,130,label1.getY());
				GLine horizontal=new GLine(0,label1.getY()+3,180,label1.getY()+3);
				screen.add(horizontal);
				shopIds.add(i);
				shopInventory.add(item);
				screen.add(item);
				bottom=horizontal.getY();
			}
		}
		GLine top=new GLine(0,60,180,60);
		screen.add(top);
		GLine vertical=new GLine(180,0,180,bottom);
		screen.add(vertical);
	}

	private static void takePlayerInventory(){	
		playerIds=new ArrayList<Integer>();
		for(int i=1;i<Constants.NUM_ITEMS+1;i++){
			int quantity=PlayerData.getItemQuantity(i);
			int sellval=sellvals[i];
			if(quantity>0&&sellval>=0){
				playerIds.add(i);
			}
		}
	}

	private static void showPlayerInventory(){
		if(sellportion!=null)
			GameData.getGUI().remove(sellportion);
		playerInventory=new ArrayList<GCompound>();
		sellportion=new GCompound();
		int count=1;
		double bottom=0;
		for(int i=playerInventoryOffset;i<playerIds.size()&&count<21;i++){
			int itemid=playerIds.get(i);
			int quantity=PlayerData.getItemQuantity(itemid);
			int sellval=sellvals[itemid];
			count++;
			GLabel label1=new GLabel(GameData.getItemName(itemid));
			GLabel label2=new GLabel("$"+sellval);
			GLabel label3=new GLabel(quantity+"");
			label1.setFont(new Font("Dialog",Font.PLAIN,18));
			label2.setFont(new Font("Dialog",Font.PLAIN,18));
			label3.setFont(new Font("Dialog",Font.PLAIN,18));
			GCompound item=new GCompound();
			item.add(label1,630,label1.getHeight()*count+31);
			item.add(label2,755,label1.getY());
			item.add(label3,820,label1.getY());
			GLine horizontal=new GLine(625,label1.getY()+3,875,label1.getY()+3);
			sellportion.add(horizontal);
			playerInventory.add(item);
			sellportion.add(item);
			bottom=horizontal.getY();
		}
		GLine top=new GLine(625,60,875,60);
		sellportion.add(top);
		GLine vertical=new GLine(875,0,875,bottom);
		sellportion.add(vertical);
		GLine vertical2=new GLine(625,0,625,bottom);
		sellportion.add(vertical2);
		GameData.getGUI().add(sellportion);
	}

	private static void showTransactionWindow(){
		if(transactionportion!=null)
			GameData.getGUI().remove(transactionportion);
		transactionInventory=new ArrayList<GCompound>();
		transactionportion=new GCompound();
		GLabel buy1pt1=new GLabel("TOTAL:  ");
		buy1pt1.setFont(new Font("Dialog",Font.PLAIN,20));
		total=0;
		for(int i=0;i<buyIds.size();i++){
			total-=(buyvals[buyIds.get(i)]*buyQuants.get(i));
		}
		for(int i=0;i<sellIds.size();i++){
			total-=(sellvals[sellIds.get(i)]*sellQuants.get(i));
		}
		if(total>=0)
			buy1pt2=new GLabel("+$"+total);
		else
			buy1pt2=new GLabel("-$"+Math.abs(total));
		buy1pt2.setFont(new Font("Dialog",Font.PLAIN,20));
		if(total<0&&Math.abs(total)>PlayerData.getMoney())
			buy1pt2.setColor(Color.RED);
		GCompound buy1=new GCompound();
		buy1.add(buy1pt1,0,0);
		buy1.add(buy1pt2,buy1pt1.getWidth(),buy1pt1.getY());
		transactionportion.add(buy1,275+(250-buy1.getWidth())/2,55+buy1.getHeight());
		GLine horizontal0=new GLine(270,buy1.getY()+3,520,buy1.getY()+3);
		transactionportion.add(horizontal0);
		GLabel buy2=new GLabel("BUYING \u2192");
		buy2.setFont(new Font("Dialog",Font.PLAIN,20));
		transactionportion.add(buy2,275+(250-buy2.getWidth())/2,buy1.getY()+buy2.getHeight());
		GLine horizontal=new GLine(270,buy2.getY()+3,520,buy2.getY()+3);
		transactionportion.add(horizontal);
		int count=3;
		double bottom=0;
		for(int i=transactionOffset;i<buyIds.size()&&count<18;i++){
			int itemid=buyIds.get(i);
			int quantity=buyQuants.get(i);
			int buyval=buyvals[itemid];
			count++;
			GLabel label1=new GLabel(GameData.getItemName(itemid));
			GLabel label2=new GLabel("$"+buyval);
			GLabel label3=new GLabel(quantity+"");
			label1.setFont(new Font("Dialog",Font.PLAIN,18));
			label2.setFont(new Font("Dialog",Font.PLAIN,18));
			label3.setFont(new Font("Dialog",Font.PLAIN,18));
			GCompound item=new GCompound();
			item.add(label1,275,label1.getHeight()*count+31);
			item.add(label2,400,label1.getY());
			item.add(label3,465,label1.getY());
			GLine horizontal2=new GLine(270,label1.getY()+3,520,label1.getY()+3);
			transactionportion.add(horizontal2);
			transactionInventory.add(item);
			transactionportion.add(item);
			bottom=horizontal2.getY();
		}
		GLabel buy4=new GLabel("SELLING \u2190");
		buy4.setFont(new Font("Dialog",Font.PLAIN,20));
		transactionportion.add(buy4,275+(250-buy4.getWidth())/2,24*count+31+buy2.getHeight());
		GLine horizontal3=new GLine(270,buy4.getY()+3,520,buy4.getY()+3);
		transactionportion.add(horizontal3);
		bottom=horizontal3.getY();
		count++;
		for(int i=Math.max(0,transactionOffset-buyIds.size());i<sellIds.size()&&count<19;i++){
			int itemid=sellIds.get(i);
			int quantity=sellQuants.get(i);
			int sellval=sellvals[itemid];
			count++;
			GLabel label1=new GLabel(GameData.getItemName(itemid));
			GLabel label2=new GLabel("$"+sellval);
			GLabel label3=new GLabel(quantity+"");
			label1.setFont(new Font("Dialog",Font.PLAIN,18));
			label2.setFont(new Font("Dialog",Font.PLAIN,18));
			label3.setFont(new Font("Dialog",Font.PLAIN,18));
			GCompound item=new GCompound();
			item.add(label1,275,label1.getHeight()*count+31);
			item.add(label2,400,label1.getY());
			item.add(label3,465,label1.getY());
			GLine horizontal2=new GLine(270,label1.getY()+3,520,label1.getY()+3);
			transactionportion.add(horizontal2);
			transactionInventory.add(item);
			transactionportion.add(item);
			bottom=horizontal2.getY();
		}
		GLine top=new GLine(270,60,520,60);
		transactionportion.add(top);
		GLine vertical=new GLine(520,0,520,bottom);
		transactionportion.add(vertical);
		GLine vertical2=new GLine(270,0,270,bottom);
		transactionportion.add(vertical2);
		GameData.getGUI().add(transactionportion);
	}

	public static void moveLeft(){
		if(mode==Mode.SELL){
			removeFocus();
			if(transactionInventory.size()>0)
				mode=Mode.TRANSACTION;
			else
				mode=Mode.BUY;
			focusindex=0;
			addFocus();
		}
		else if(mode==Mode.COMMIT){
			removeFocus();
			mode=Mode.CLEAR;
			addFocus();
		}
		else if(mode==Mode.EXIT){
			removeFocus();
			mode=Mode.COMMIT;
			addFocus();
		}
		else if(mode==Mode.TRANSACTION){
			removeFocus();
			mode=Mode.BUY;
			focusindex=0;
			addFocus();
		}
	}

	public static void moveRight(){
		if(mode==Mode.BUY){
			removeFocus();
			if(transactionInventory.size()>0){
				mode=Mode.TRANSACTION;
				focusindex=0;
			}
			else if(playerInventory.size()>0){
				mode=Mode.SELL;
				focusindex=0;
			}
			addFocus();
		}
		else if(mode==Mode.COMMIT){
			removeFocus();
			mode=Mode.EXIT;
			addFocus();
		}
		else if(mode==Mode.CLEAR){
			removeFocus();
			mode=Mode.COMMIT;
			addFocus();
		}
		else if(mode==Mode.TRANSACTION){
			removeFocus();
			mode=Mode.SELL;
			focusindex=0;
			addFocus();
		}
	}

	public static void moveDown(){
		//Not bottom of list, continue
		if(mode==Mode.SELL&&focusindex<Math.min(playerIds.size()-playerInventoryOffset-1,19)){
			removeFocus();
			focusindex++;
			addFocus();
		}
		//More list to go, cycle through
		else if(mode==Mode.SELL&&playerIds.size()-1>playerInventoryOffset+focusindex){
			playerInventoryOffset+=20;
			showPlayerInventory();
			focusindex=0;
			addFocus();
		}
		//Bottom of sell list. Go down to buttons below
		else if(mode==Mode.SELL&&focusindex==Math.min(playerIds.size()-playerInventoryOffset-1,19)){
			removeFocus();
			mode=Mode.EXIT;
			focusindex=0;
			addFocus();
		}
		//Not bottom of list, continue
		else if(mode==Mode.BUY&&focusindex<shopIds.size()-1){
			removeFocus();
			focusindex++;
			addFocus();
		}
		//Bottom of buy list. Go down to buttons below
		else if(mode==Mode.BUY&&focusindex==Math.min(shopIds.size()-1,19)){
			removeFocus();
			mode=Mode.CLEAR;
			focusindex=0;
			addFocus();
		}
		//Not the bottom of list, continue
		else if(mode==Mode.TRANSACTION&&focusindex<Math.min(buyIds.size()+sellIds.size()-transactionOffset-1,14)){
			removeFocus();
			focusindex++;
			addFocus();
		}
		//More list to go, cycle through
		else if(mode==Mode.TRANSACTION&&buyIds.size()+sellIds.size()-1>transactionOffset+focusindex){
			transactionOffset+=15;
			showTransactionWindow();
			focusindex=0;
			addFocus();
		}
		//Bottom of transaction list. Go down to buttons below
		else if(mode==Mode.TRANSACTION&&focusindex==Math.min(buyIds.size()+sellIds.size()-transactionOffset-1,14)){
			removeFocus();
			mode=Mode.CLEAR;
			focusindex=0;
			addFocus();
		}
	}

	public static void moveUp(){
		if(focusindex>0){
			removeFocus();
			focusindex--;
			addFocus();
		}
		else if(playerInventoryOffset>0&&mode==Mode.SELL){
			playerInventoryOffset-=20;
			showPlayerInventory();
			focusindex=19;
			addFocus();
		}
		else if(transactionOffset>0&&mode==Mode.TRANSACTION){
			transactionOffset-=15;
			showTransactionWindow();
			focusindex=14;
			addFocus();
		}
		else if(mode==Mode.CLEAR||mode==Mode.COMMIT||mode==Mode.EXIT){
			removeFocus();
			if(transactionInventory.size()>0){
				mode=Mode.TRANSACTION;
				focusindex=Math.min(buyIds.size()+sellIds.size()-transactionOffset-1,14);
			}
			else if(mode==Mode.CLEAR||mode==Mode.COMMIT){
				mode=Mode.BUY;
				focusindex=Math.min(shopIds.size()-1,19);
			}
			else{
				mode=Mode.SELL;
				focusindex=Math.min(playerIds.size()-playerInventoryOffset-1,19);
			}
			addFocus();
		}
	}

	public static void select(){
		if(mode==Mode.CLEAR){
			for(int i=0;i<sellIds.size();i++){
				PlayerData.addItem(sellIds.get(i),sellQuants.get(i));
			}
			initVariables();
			takePlayerInventory();
			showPlayerInventory();
			showTransactionWindow();
			removeFocus();
			mode=Mode.BUY;
			focusindex=0;
			addFocus();
		}
		else if(mode==Mode.COMMIT){
			if(transactionInventory.size()>0&&buy1pt2!=null&&buy1pt2.getColor()!=Color.RED){
				for(int i=0;i<buyIds.size();i++){
					PlayerData.addItem(buyIds.get(i),buyQuants.get(i));
				}
				if(total>0)
					PlayerData.gainMoney(total);
				if(total<0)
					PlayerData.loseMoney(Math.abs(total));
				initVariables();
				takePlayerInventory();
				showPlayerInventory();
				showTransactionWindow();
				removeFocus();
				mode=Mode.BUY;
				focusindex=0;
				addFocus();
				System.out.println(PlayerData.getMoney());
			}
			else{
				footer.setLabel("No transaction to commit or insufficient funds.");
			}
		}
		else if(mode==Mode.EXIT){
			close();
		}
		else if(mode==Mode.BUY){
			int index=buyIds.indexOf(shopIds.get(focusindex));
			if(index<0){
				buyIds.add(shopIds.get(focusindex));
				buyQuants.add(1);
			}
			else{
				buyQuants.set(index,buyQuants.get(index)+1);
			}
			showTransactionWindow();
		}
		else if(mode==Mode.SELL){
			int id=playerIds.get(focusindex+playerInventoryOffset);
			int index=sellIds.indexOf(id);
			if(index<0){
				sellIds.add(id);
				sellQuants.add(1);
			}
			else{
				sellQuants.set(index,sellQuants.get(index)+1);
			}
			PlayerData.removeItem(id,1);
			showTransactionWindow();
			takePlayerInventory();
			showPlayerInventory();
			while(focusindex>=playerIds.size()-playerInventoryOffset)
				focusindex--;
			if(focusindex<0){
				focusindex=0;
				mode=Mode.BUY;
			}
			addFocus();
		}
		else if(mode==Mode.TRANSACTION){
			int index=focusindex+transactionOffset;
			if(index<buyIds.size()){
				buyQuants.set(index,buyQuants.get(index)-1);
				if(buyQuants.get(index)==0){
					buyIds.remove(index);
					buyQuants.remove(index);
				}
			}
			else{
				index-=buyIds.size();
				int id=sellIds.get(index);
				sellQuants.set(index,sellQuants.get(index)-1);
				if(sellQuants.get(index)==0){
					sellIds.remove(index);
					sellQuants.remove(index);
				}
				PlayerData.addItem(id,1);
				takePlayerInventory();
				showPlayerInventory();
			}
			showTransactionWindow();
			while(focusindex>=buyIds.size()+sellIds.size()-transactionOffset)
				focusindex--;
			if(focusindex<0){
				focusindex=0;
				mode=Mode.BUY;
			}
			addFocus();
		}
	}

	private static void addFocus(){
		if(mode==Mode.BUY)
			shopInventory.get(focusindex).setColor(Color.BLUE);
		else if(mode==Mode.SELL)
			playerInventory.get(focusindex).setColor(Color.BLUE);
		else if(mode==Mode.COMMIT)
			commitbox.setFillColor(Color.CYAN);
		else if(mode==Mode.CLEAR)
			clearbox.setFillColor(Color.CYAN);
		else if(mode==Mode.EXIT)
			exitbox.setFillColor(Color.CYAN);
		else if(mode==Mode.TRANSACTION)
			transactionInventory.get(focusindex).setColor(Color.BLUE);
		changeFooter();
	}
	
	private static void changeFooter(){
		if(mode==Mode.BUY)
			footer.setLabel(GameData.getItemDescription(shopIds.get(focusindex)));
		else if(mode==Mode.SELL)
			footer.setLabel(GameData.getItemDescription(playerIds.get(focusindex+playerInventoryOffset)));
		else if(mode==Mode.COMMIT)
			footer.setLabel("Perform the transaction, charging (-) or giving (+) the total to the player's funds.");
		else if(mode==Mode.CLEAR)
			footer.setLabel("Clear the transaction, returning the items to the player's inventory");
		else if(mode==Mode.EXIT)
			footer.setLabel("Exit the shop and return to the map.");
		else if(mode==Mode.TRANSACTION){
			int index=focusindex+transactionOffset;
			if(index<buyIds.size()){
				footer.setLabel(GameData.getItemDescription(buyIds.get(index)));
			}
			else{
				index-=buyIds.size();
				footer.setLabel(GameData.getItemDescription(sellIds.get(index)));
			}
		}
			
	}

	private static void removeFocus(){
		if(mode==Mode.BUY)
			shopInventory.get(focusindex).setColor(Color.BLACK);
		else if(mode==Mode.SELL)
			playerInventory.get(focusindex).setColor(Color.BLACK);
		else if(mode==Mode.COMMIT)
			commitbox.setFillColor(Color.WHITE);
		else if(mode==Mode.CLEAR)
			clearbox.setFillColor(Color.WHITE);
		else if(mode==Mode.EXIT)
			exitbox.setFillColor(Color.WHITE);
		else if(mode==Mode.TRANSACTION)
			transactionInventory.get(focusindex).setColor(Color.BLACK);
	}

	public static void takeControl(){
		GameData.getGUI().giveControl(new ShopKeyListener());
	}

	public static void close(){
		System.out.println("Closing shop");
		GUI gui=GameData.getGUI();
		gui.remove(screen);
		gui.remove(sellportion);
		MapEngine.initialize(PlayerData.getLocation());
	}
}
