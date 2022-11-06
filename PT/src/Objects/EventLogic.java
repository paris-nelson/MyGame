package Objects;

import java.io.File;
import java.util.Scanner;

import Engines.DialogEngine;
import Engines.GlobalEngine;
import Engines.MapEngine;
import Engines.MenuEngine;
import Enums.EventName;
import Enums.LocationName;
import Enums.Requirement;
import Global.Constants;
import Global.GameData;
import Global.PlayerData;
import Menus.TownMenu;

public class EventLogic {


	public static void triggerEvent(EventName event){
		System.out.println("Triggered event "+event.toString());
		DialogEngine.initialize(event);
	}

	public static void continueEvent(EventName event){
		PlayerData.markEventCleared(event);
		if(event==EventName.PokeCenter){
			PlayerData.healParty();
			MapEngine.takeControl();
		}
		else if(event==EventName.BlackOut){
			PlayerData.changeLocation(PlayerData.getLastTown());
			IntPair coordinates=PlayerData.getLocation().getCoordinates().get(0);
			MapEngine.setIconToPosition(Short.valueOf(coordinates.getX()+""),Short.valueOf(coordinates.getY()+""));
			MapEngine.save();
			MapEngine.initialize(PlayerData.getLocation());
		}
		else if(event.toString().endsWith("Badge")){
			Requirement badge=Requirement.valueOf(event.toString());
			PlayerData.markRequirementMet(badge);
			if(event==EventName.InsectBadge)
				PlayerData.addItem(GameData.getItemNum("HM01"),1);
			else if(event==EventName.PlainBadge)
				PlayerData.addItem(GameData.getItemNum("HMO4"),1);
			else if(event==EventName.FogBadge)
				PlayerData.addItem(GameData.getItemNum("HM03"),1);
			else if(event==EventName.StormBadge)
				PlayerData.addItem(GameData.getItemNum("HMO2"),1);
			else if(event==EventName.ZephyrBadge)
				PlayerData.addItem(GameData.getItemNum("HM05"),1);
			else if(event==EventName.GlacierBadge)
				PlayerData.addItem(GameData.getItemNum("HMO6"),1);
			else if(event==EventName.RisingBadge)
				PlayerData.addItem(GameData.getItemNum("HM07"),1);
			else if(event==EventName.MineralBadge)
				PlayerData.addItem(GameData.getItemNum("Rematcher"),1);
			MapEngine.initialize(PlayerData.getLocation());
		}
		else if(event.toString().startsWith("RivalEncounter")){
			if(event.toString().endsWith("Beaten")){
				if(event==EventName.RivalEncounter2Beaten)
					PlayerData.addNewPokemon(new Pokemon(175,5));
				MapEngine.initialize(PlayerData.getLocation());
			}
			else if(event.toString().endsWith("Final")){
				GlobalEngine.enterBattle(GlobalEngine.generateRival());
			}
			else{
				File f=new File(Constants.PATH+"EventData\\Trainers\\"+event.toString()+".txt");
				EliteTrainer rival=null;
				try{
					rival=EliteTrainer.readInTrainer(new Scanner(f));
				}catch(Exception e){e.printStackTrace();}
				GlobalEngine.enterBattle(rival);
			}
		}
		else if(event==EventName.EliteFourBeaten){
			if(!PlayerData.hasMetRequirement(Requirement.EliteFourChampion)){
				PlayerData.addNewPokemon(new Pokemon(1,5));
				PlayerData.addNewPokemon(new Pokemon(4,5));
				PlayerData.addNewPokemon(new Pokemon(7,5));
				PlayerData.markRequirementMet(Requirement.EliteFourChampion);
				PlayerData.setName("Champion "+PlayerData.getName());
			}
			PlayerData.changeLocation(LocationName.IndigoPlateau);
			IntPair coordinates=PlayerData.getLocation().getCoordinates().get(0);
			MapEngine.setIconToPosition(Short.valueOf(coordinates.getX()+""),Short.valueOf(coordinates.getY()+""));
			MapEngine.save();
			MapEngine.initialize(PlayerData.getLocation());
		}
		else if(event==EventName.RocketEncounter1Beaten){
			PlayerData.markRequirementMet(Requirement.RocketEncounterOneBeaten);
			MapEngine.takeControl();
		}
		else if(event==EventName.RocketEncounter2Beaten){
			PlayerData.markRequirementMet(Requirement.RocketEncounterTwoBeaten);
			MapEngine.takeControl();
		}
		else if(event==EventName.RocketEncounter3Beaten){
			PlayerData.markRequirementMet(Requirement.RocketEncounterThreeBeaten);
			MapEngine.takeControl();
		}
		else if(PlayerData.getLocation().getMenu()!=null){
			MenuEngine.initialize(PlayerData.getLocation().getMenu());
		}
		else
			MapEngine.takeControl();
	}
}
