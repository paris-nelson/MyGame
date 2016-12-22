package Menus;

import java.util.ArrayList;

import Engines.GlobalEngine;
import Engines.InventoryEngine;
import Engines.MapEngine;
import Engines.MenuEngine;
import Enums.PartyMenuMode;
import Global.PlayerData;

public class PlayerMenu implements Menu {
	
	private ArrayList<String> visibleoptions=new ArrayList<String>();
	
	public PlayerMenu(){
		visibleoptions.add("Party");
		visibleoptions.add("Inventory");
		visibleoptions.add("Save");
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
			MapEngine.takeControl();
			//MapEngine.initialize(PlayerData.getLocation());
		}
		else if(selected.equals("Party")){
			MenuEngine.close();
			MenuEngine.initialize(new PartyMenu(PartyMenuMode.VIEWMAP));
		}
		else if(selected.equals("Inventory")){
			MenuEngine.close();
			MapEngine.close();
			InventoryEngine.initialize(false);
		}
	}

}
