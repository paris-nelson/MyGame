package Menus;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import Engines.BattleEngine;
import Engines.GlobalEngine;
import Engines.MapEngine;
import Engines.MenuEngine;
import Engines.PCEngine;
import Engines.ShopEngine;
import Enums.LocationName;
import Enums.Requirement;
import Global.Constants;
import Global.GameData;
import Global.PlayerData;
import Objects.Pokemon;
import Objects.Trainer;

public class TownMenu implements Menu{
	private ArrayList<String> totaloptions=new ArrayList<String>();
	private ArrayList<Requirement> requirements=new ArrayList<Requirement>();
	private ArrayList<String> visibleoptions=new ArrayList<String>();
	private ArrayList<String> initialpositions=new ArrayList<String>();

	public TownMenu(ArrayList<String> optionreqpairs){
		for(String s:optionreqpairs){
			String[] split=s.split(",");
			totaloptions.add(split[0]);
			requirements.add(Requirement.valueOf(split[1]));
			if(split.length>2)
				initialpositions.add(split[2]+" "+split[3]);
			else
				initialpositions.add(null);
		}
	}

	public void refreshVisibleOptions(){
		visibleoptions.clear();
		for(int i=0;i<totaloptions.size();i++){
			if(PlayerData.hasMetRequirement(requirements.get(i)))
				visibleoptions.add(totaloptions.get(i));
		}
	}

	public int getNumOptions(){
		return visibleoptions.size();
	}

	public ArrayList<String> getVisibleOptions(){
		return visibleoptions;
	}

	public void select(short index){
		String selected=visibleoptions.get(index);
		if(selected.startsWith("Battle ")){
			MenuEngine.close();
			if(selected.endsWith("Rival")){
				try{
					GlobalEngine.enterBattle(Trainer.readInTrainer(new Scanner(new File("EventData\\RivalEncounter7.txt"))));
				}catch(Exception e){e.printStackTrace();}
			}
			else if(selected.endsWith("Challenger")){
				Pokemon[] party=new Pokemon[6];
				Random r=GameData.getRandom();
				for(int i=0;i<6;i++){
					party[i]=new Pokemon(r.nextInt(251)+1,50,10);
				}
				GlobalEngine.enterBattle(new Trainer(Short.valueOf("0"),"Challenger",party,0,0,Constants.PATH+"Sprites\\CooltrainerF.png"));
			}
			else if(selected.endsWith("Elite Four")){
				LocationName ln=LocationName.EliteFour;
				if(PlayerData.hasMetRequirement(GameData.getLocationRequirement(ln))){
					PlayerData.changeLocation(ln);
					MapEngine.changeLocation();
					MapEngine.takeControl();
				}
			}
		}
		else if(selected.startsWith("Exit to ")){
			MenuEngine.close();
			int indexof=totaloptions.indexOf(selected);
			selected=selected.substring(8).replace(" ","");
			LocationName ln=LocationName.valueOf(selected);
			if(PlayerData.hasMetRequirement(GameData.getLocationRequirement(ln))){
				PlayerData.changeLocation(ln);
				MapEngine.changeLocation();
				String initial=initialpositions.get(indexof);
				if(initial!=null){
					String[] split=initial.split(" ");
					MapEngine.setIconToPosition(Short.parseShort(split[0]),Short.parseShort(split[1]));
				}
				MapEngine.takeControl();
			}
		}
		else if(selected.startsWith("Enter ")){
			MenuEngine.close();
			selected=selected.substring(6).replace(" ","");
			LocationName ln=LocationName.valueOf(selected);
			if(PlayerData.hasMetRequirement(GameData.getLocationRequirement(ln))){
				PlayerData.changeLocation(ln);
				MapEngine.changeLocation();
				//MapEngine.addIconToPosition(Short.valueOf("3"),Short.valueOf("29"));
				//MapEngine.takeControl();
			}
		}
		else if(selected.equals("Use P.C.")){
			MenuEngine.close();
			MapEngine.close();
			PCEngine.initialize();
		}
		else if(selected.equals("PokeCenter")){
//			MapEngine.close();
			//TODO: trigger event? Either way should give a prompt that something occurred.
			PlayerData.healParty();
		}
		else if(selected.equals("PokeShop")){
			MenuEngine.close();
			MapEngine.close();
			ShopEngine.initialize();
		}
	}

	public static Menu readInMenu(Scanner s){
		String curr=s.nextLine();
		ArrayList<String> menudata=new ArrayList<String>();
		while(!curr.equals("End Menu")){
			menudata.add(curr);
			curr=s.nextLine();
		}
		if(menudata.isEmpty())
			return null;
		return new TownMenu(menudata);
	}
}
