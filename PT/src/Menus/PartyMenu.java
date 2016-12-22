package Menus;

import java.util.ArrayList;

import Engines.BattleEngine;
import Engines.DetailsEngine;
import Engines.GlobalEngine;
import Engines.InventoryEngine;
import Engines.MapEngine;
import Engines.MenuEngine;
import Engines.PCEngine;
import Enums.DetailsEngineMode;
import Enums.PartyMenuMode;
import Global.GameData;
import Global.PlayerData;
import Objects.Pokemon;

public class PartyMenu implements Menu {

	private ArrayList<String> visibleoptions=new ArrayList<String>();
	private PartyMenuMode mode;
	private Pokemon pokemon;

	public PartyMenu(PartyMenuMode mode){
		for(Pokemon p:PlayerData.getParty()){
			visibleoptions.add(p.getName());
		}
		visibleoptions.add("Exit");
		this.mode=mode;
	}

	@Override
	public void refreshVisibleOptions() {
	}

	@Override
	public int getNumOptions() {
		return visibleoptions.size();
	}

	@Override
	public ArrayList<String> getVisibleOptions() {
		return visibleoptions;
	}

	@Override
	public void select(short index) {
		if(visibleoptions.get(index).equals("Exit")){
			MenuEngine.close();
			//MenuEngine.initialize(new PlayerMenu());
			if(mode==PartyMenuMode.VIEWBATTLE)
				BattleEngine.takeControl();
			else if(mode==PartyMenuMode.DEPOSIT)
				PCEngine.takeControl();
			else if(mode==PartyMenuMode.USE||mode==PartyMenuMode.GIVE)
				InventoryEngine.cleanUp();
			else if(mode==PartyMenuMode.VIEWMAP)
				MapEngine.takeControl();
		}
		else if(visibleoptions.get(index).equals("Back")){
			visibleoptions.clear();
			for(Pokemon p:PlayerData.getParty()){
				visibleoptions.add(p.getName());
			}
			visibleoptions.add("Exit");
			MenuEngine.refreshMenu();
		}
		else if(visibleoptions.get(index).equals("Replace")){
			PlayerData.addItem(pokemon.getHeldID(),1);
			pokemon.removeHeldItem();
			PlayerData.removeItem(InventoryEngine.getChosenId(),1);
			pokemon.holdItem(InventoryEngine.getChosenId());
			MenuEngine.close();
			InventoryEngine.cleanUp();
		}
		else if(mode==PartyMenuMode.VIEWBATTLE){
			MenuEngine.close();
			//MapEngine.close();
			DetailsEngine.initialize(PlayerData.getParty().get(index),DetailsEngineMode.PARTYBATTLE);
		}
		else if(mode==PartyMenuMode.VIEWMAP){
			MenuEngine.close();
			DetailsEngine.initialize(PlayerData.getParty().get(index),DetailsEngineMode.PARTYMAP);
		}
		else if(mode==PartyMenuMode.USE){
			MenuEngine.close();
			GlobalEngine.useItem(InventoryEngine.getChosenId(),PlayerData.getParty().get(index));
			//InventoryEngine.cleanUp();
		}
		else if(mode==PartyMenuMode.GIVE){
			pokemon=PlayerData.getParty().get(index);
			if(!pokemon.holdItem(InventoryEngine.getChosenId())){
				visibleoptions.clear();
				visibleoptions.add("Holding "+GameData.getItemName(pokemon.getHeldID()));
				visibleoptions.add("Replace");
				visibleoptions.add("Back");
				MenuEngine.refreshMenu();
			}
			else{
				PlayerData.removeItem(InventoryEngine.getChosenId(),1);
				pokemon.holdItem(InventoryEngine.getChosenId());
				MenuEngine.close();
				InventoryEngine.cleanUp();
			}
		}
		else if(mode==PartyMenuMode.DEPOSIT){
			if(PlayerData.getPartySize()>1){
				pokemon=PlayerData.getParty().get(index);
				System.out.println("Depositing "+pokemon.getName());
				PlayerData.removePokemonFromParty(pokemon);
				PlayerData.addToPC(pokemon);
				MenuEngine.close();
				PCEngine.takeControl();
			}
			else{
				System.out.println("Cannot deposit only pokemon in party");
			}
		}
	}

}
