package Objects;

import java.io.File;
import java.util.Scanner;

import Engines.GlobalEngine;
import Engines.MapEngine;
import Enums.EventName;
import Enums.Requirement;
import Global.Constants;
import Global.GameData;
import Global.PlayerData;

public class EventLogic {


	public static void triggerEvent(EventName event){
		if(event==EventName.PokeCenter){
			System.out.println("Player's pokemon are healed");
			for(Pokemon p:PlayerData.getParty()){
				p.revive();
				p.restoreHP();
				p.removePcondition();
			}
		}
		else if(event==EventName.BlackOut){
			PlayerData.changeLocation(PlayerData.getLastTown());
			IntPair coordinates=PlayerData.getLocation().getCoordinates().get(0);
			MapEngine.setIconToPosition(Short.valueOf(coordinates.getX()+""),Short.valueOf(coordinates.getY()+""));
			MapEngine.save();
			MapEngine.initialize(PlayerData.getLocation());
		}
		else if(event.toString().endsWith("Gym")){
			
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
			//TODO:
		}
		else if(event.toString().startsWith("RivalEncounter")){
			File f=new File(Constants.PATH+"EventData\\"+event.toString()+".txt");
			EliteTrainer rival=null;
			try{
				rival=EliteTrainer.readInTrainer(new Scanner(f));
			}catch(Exception e){e.printStackTrace();}
			GlobalEngine.enterBattle(rival);
		}
	}
}
