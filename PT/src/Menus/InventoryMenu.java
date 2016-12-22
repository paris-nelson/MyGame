package Menus;

import java.util.ArrayList;

import Engines.GlobalEngine;
import Engines.MenuEngine;
import Global.PlayerData;
import Objects.Pokemon;

public class InventoryMenu implements Menu {
	
	private ArrayList<String> visibleoptions=new ArrayList<String>();
	private int itemid;
	
	public InventoryMenu(int itemid){
		this.itemid=itemid;
		for(Pokemon p:PlayerData.getParty()){
			visibleoptions.add(p.getName());
		}
	}

	@Override
	public void refreshVisibleOptions() {
		visibleoptions.clear();
		for(Pokemon p:PlayerData.getParty()){
			visibleoptions.add(p.getName());
		}
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
		MenuEngine.close();
		GlobalEngine.useItem(itemid,PlayerData.getParty().get(index));
	}

}