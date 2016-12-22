package Menus;

import java.util.ArrayList;

import Engines.BattleEngine;
import Engines.DetailsEngine;
import Engines.MenuEngine;
import Enums.DetailsEngineMode;
import Objects.Pokemon;
import Objects.Trainer;

public class OpponentPartyMenu implements Menu {

	private ArrayList<String> visibleoptions=new ArrayList<String>();
	private Trainer opp;

	public OpponentPartyMenu(Trainer opponent){
		opp=opponent;
		for(Pokemon p:opponent.getParty()){
			visibleoptions.add(p.getName());
		}
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
		if(visibleoptions.get(index).equals("Exit")){
			MenuEngine.close();
			//MenuEngine.initialize(new PlayerMenu());
			BattleEngine.takeControl();
		}
		else{
			MenuEngine.close();
			DetailsEngine.initialize(opp.getParty()[index],DetailsEngineMode.OPPONENT);
		}
	}

}
