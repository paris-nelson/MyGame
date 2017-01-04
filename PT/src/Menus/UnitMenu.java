package Menus;

import java.util.ArrayList;

import Engines.BattleEngine;
import Engines.InventoryEngine;
import Engines.MenuEngine;
import Enums.MoveMenuMode;
import Enums.TempCondition;
import Global.GameData;
import Objects.Move;
import Objects.Unit;

public class UnitMenu implements Menu {

	private ArrayList<String> visibleoptions=new ArrayList<String>();
	private Unit unit;

	public UnitMenu(Unit unit){
		this.unit=unit;
		if(!unit.hasMoved()&&unit.canMove())
			visibleoptions.add("Move");
		if(!unit.hasTakenAction()){
			if(unit.canAttack()){
			visibleoptions.add("Attack");
			}
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
			BattleEngine.win();
			//BattleEngine.move();
		}
		else if(visibleoptions.get(index).equals("Attack")){
			if(unit.hasTempCondition(TempCondition.Encore)){
				if(unit.getPrevMove().hasPP())
					BattleEngine.useMove(unit.getPrevMove());
				else
					BattleEngine.useMove(new Move(GameData.getMoveNum("Struggle")));
			}
			MenuEngine.initialize(new MoveMenu(unit.getPokemon(),MoveMenuMode.ATTACK));
		}
		else if(visibleoptions.get(index).equals("Use Item")){
			InventoryEngine.initialize();
		}
		else if(visibleoptions.get(index).equals("End Turn")){
			BattleEngine.endTurn();
		}
	}

}
