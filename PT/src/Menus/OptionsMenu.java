package Menus;

import java.util.ArrayList;

import Engines.BattleEngine;
import Engines.ControlsEngine;
import Engines.MenuEngine;

public class OptionsMenu implements Menu {
	private ArrayList<String> options;
	
	public OptionsMenu(){
		options=new ArrayList<String>();
		options.add("Controls");
		options.add("Music");
		options.add("Exit Menu");
	}

	public void refreshVisibleOptions() {	
	}

	public int getNumOptions() {
		return options.size();
	}

	public ArrayList<String> getVisibleOptions() {
		return options;
	}

	@Override
	public void select(short index) {
		String selected=options.get(index);
		MenuEngine.close();
		if(selected.equals("Controls")){
			ControlsEngine.initialize();
		}
		else if(selected.equals("Music")){
			MenuEngine.initialize(new MusicMenu());
		}
		else if(selected.equals("Exit Menu")){
			if(BattleEngine.isInBattle())
				MenuEngine.initialize(new BattlePlayerMenu());
			else
				MenuEngine.initialize(new PlayerMenu());
		}
	}

}
