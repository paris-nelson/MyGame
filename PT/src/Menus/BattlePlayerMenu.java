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
		visibleoptions.add("Options");
		visibleoptions.add("Exit Menu");
	}

	public void refreshVisibleOptions() {
	}

	public int getNumOptions() {
		return visibleoptions.size();
	}

	public ArrayList<String> getVisibleOptions() {
		return visibleoptions;
	}

	public void select(short index) {
		String selected=visibleoptions.get(index);
		if(selected.equals("Save")){
			GlobalEngine.save();
			visibleoptions.set(index,"Game Saved!");
			MenuEngine.refreshMenu();
		}
		else if(selected.equals("Exit Menu")){
			MenuEngine.close();
			MenuEngine.initialize(new UnitMenu(BattleEngine.getActiveUnit()));
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
		else if(selected.equals("Options")){
			MenuEngine.close();
			MenuEngine.initialize(new OptionsMenu());
		}
		else if(selected.equals("Flee")){
			MenuEngine.close();
			BattleEngine.flee();
		}
	}
}
