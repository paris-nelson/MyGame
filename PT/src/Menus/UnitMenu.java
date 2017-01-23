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
		visibleoptions.add("Change Direction");
		visibleoptions.add("End Turn");
		visibleoptions.add("Player Menu");
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
		if(selected.equals("Move")){
			MenuEngine.close();
			BattleEngine.move();
		}
		else if(selected.equals("Attack")){
			MenuEngine.close();
			if(unit.hasTempCondition(TempCondition.Encore)){
				if(unit.getPokemon().getMove(unit.getPrevMove()).hasPP())
					BattleEngine.useMove(unit.getPokemon().getMove(unit.getPrevMove()),true);
				else
					BattleEngine.useMove(new Move(GameData.getMoveNum("Struggle")),true);
			}
			MenuEngine.initialize(new MoveMenu(unit.getPokemon(),MoveMenuMode.ATTACK));
		}
		else if(selected.equals("Use Item")){
			MenuEngine.close();
			InventoryEngine.initialize();
		}
		else if(selected.equals("End Turn")){
			MenuEngine.close();
			BattleEngine.endTurn();
		}
		else if(selected.equals("Player Menu")){
			MenuEngine.close();
			BattleEngine.openPlayerMenu();
		}
		else if(selected.equals("Change Direction")){
			BattleEngine.getActiveUnit().changeDirectionFacing();
			MenuEngine.refreshMenu();
		}
	}

}
