package Menus;

import java.util.ArrayList;

import Engines.BattleEngine;
import Engines.InventoryEngine;
import Engines.MenuEngine;
import Enums.MoveMenuMode;
import Objects.Unit;

public class UnitMenu implements Menu {

	private ArrayList<String> visibleoptions=new ArrayList<String>();
	private Unit unit;

	public UnitMenu(Unit unit){
		this.unit=unit;
		if(!unit.hasMoved())
			visibleoptions.add("Move");
		if(!unit.hasTakenAction()){
			visibleoptions.add("Attack");
			visibleoptions.add("Use Item");
		}
		visibleoptions.add("End Turn");
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
		MenuEngine.close();
		if(visibleoptions.get(index).equals("Move")){
			BattleEngine.move();
		}
		else if(visibleoptions.get(index).equals("Attack")){
			MenuEngine.initialize(new MoveMenu(unit.getPokemon(),MoveMenuMode.ATTACK));
		}
		else if(visibleoptions.get(index).equals("Use Item")){
			InventoryEngine.initialize(true);
		}
		else if(visibleoptions.get(index).equals("End Turn")){
			BattleEngine.endTurn();
		}
	}

}
