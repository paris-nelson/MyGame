package Menus;

import java.util.ArrayList;

import Engines.BattleEngine;
import Engines.GlobalEngine;
import Engines.MenuEngine;
import Enums.PartyMenuMode;
import Objects.WildTrainer;

public class BattlePlayerMenu implements Menu {
	
	private ArrayList<String> visibleoptions=new ArrayList<String>();
	
	public BattlePlayerMenu(){
		visibleoptions.add("Party");
		visibleoptions.add("Opponent");
		visibleoptions.add("Save");
		if(BattleEngine.currOpponent() instanceof WildTrainer)
			visibleoptions.add("Flee");
		visibleoptions.add("Exit Menu");
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
		String selected=visibleoptions.get(index);
		if(selected.equals("Save")){
			GlobalEngine.save();
			visibleoptions.set(index,"Game Saved!");
			MenuEngine.refreshMenu();
		}
		else if(selected.equals("Exit Menu")){
			MenuEngine.close();
			BattleEngine.takeControl();
			//MapEngine.initialize(PlayerData.getLocation());
		}
		else if(selected.equals("Party")){
			MenuEngine.close();
			MenuEngine.initialize(new PartyMenu(PartyMenuMode.VIEWBATTLE));
		}
		else if(selected.equals("Opponent")){
			MenuEngine.close();
			MenuEngine.initialize(new OpponentPartyMenu(BattleEngine.currOpponent()));
		}
		else if(selected.equals("Flee")){
			MenuEngine.close();
			BattleEngine.flee();
		}
	}

}
