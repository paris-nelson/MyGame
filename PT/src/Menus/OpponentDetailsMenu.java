package Menus;

import java.util.ArrayList;

import Engines.BattleEngine;
import Engines.DetailsEngine;
import Engines.MenuEngine;

public class OpponentDetailsMenu implements Menu {

	private ArrayList<String> visibleoptions=new ArrayList<String>();

	public OpponentDetailsMenu(){
		visibleoptions.add("Exit");
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
		if(selected.equals("Exit")){
			MenuEngine.close();
			DetailsEngine.close();
			MenuEngine.initialize(new OpponentPartyMenu(BattleEngine.currOpponent()));
		}
	}

}
