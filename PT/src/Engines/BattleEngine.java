package Engines;

import java.util.ArrayList;

import Enums.Stat;
import Global.GameData;
import Global.PlayerData;
import KeyListeners.BattleKeyListener;
import Menus.BattlePlayerMenu;
import Menus.UnitMenu;
import Objects.Move;
import Objects.Pokemon;
import Objects.Trainer;
import Objects.Unit;

public class BattleEngine {

	private static Trainer opponent;
	private static ArrayList<Unit> punits;
	private static ArrayList<Unit> ounits;
	private static ArrayList<Unit> allunits;
	private static ArrayList<Integer> priorities;
	private static int activeindex;
	private static Unit activeunit;

	public static void initialize(Trainer newopponent){
		//TODO: Add save/load feature. Either add resume feature for picking back up mid battle
		//or add logic to this method to determine if this is a new battle or if mid battle
		//e.g. wipe save file at end of battle, initialize method uses save file to determine if new battle or not
		System.out.println("Initializing battle with "+newopponent.getName());
		opponent=newopponent;
		initUnits();
		takeControl();
		setPriorities();
		activeindex=0;
		activeunit=allunits.get(priorities.get(0));
		takeTurn();
	}

	public static void setPriorities(){
		priorities=new ArrayList<Integer>();
		for(int i=0;i<allunits.size();i++){
			priorities.add(i);
		}
		ArrayList<Integer> speeds=new ArrayList<Integer>();
		for(Unit u:allunits){
			speeds.add(u.getStat(Stat.Speed));
		}
		for(int pointer=1;pointer<speeds.size();pointer++){
			int hole=pointer;
			while(hole>0&&speeds.get(hole)>speeds.get(hole-1)){
				int temp=speeds.get(hole-1);
				speeds.set(hole-1,speeds.get(hole));
				speeds.set(hole,temp);
				temp=priorities.get(hole-1);
				priorities.set(hole-1,priorities.get(hole));
				priorities.set(hole,temp);
				hole--;
			}
		}
	}

	public static void nextTurn(){
		System.out.println("Going to next turn");
		if(punits.size()==0||ounits.size()==0)
			battleOver();
		activeindex++;
		if(activeindex>=priorities.size()){
			setPriorities();
			activeindex=0;
		}
		activeunit=allunits.get(priorities.get(activeindex));
		if(activeunit.getPokemon().isFainted())
			nextTurn();
		else
			takeTurn();
	}

	private static void takeTurn(){
		activeunit.setHasMoved(false);
		activeunit.setHasTakenAction(false);
		activeunit.setHasEndedTurn(false);
		System.out.println(activeunit.getPokemon().getName()+" ("+activeindex+" in order) taking turn");
		openUnitMenu();
	}

	public static void takeControl(){
		GameData.getGUI().giveControl(new BattleKeyListener());
	}

	public static void initUnits(){
		punits=new ArrayList<Unit>();
		ounits=new ArrayList<Unit>();
		allunits=new ArrayList<Unit>();
		for(Pokemon p:opponent.getParty()){
			ounits.add(new Unit(p));
		}
		for(Pokemon p:PlayerData.getParty()){
			punits.add(new Unit(p));
		}
		allunits.addAll(punits);
		allunits.addAll(ounits);
	}

	public static void close(){
		System.out.println("Ending battle");
	}

	public static Trainer currOpponent(){
		return opponent;
	}

	private static void battleOver(){
		System.out.println("Battle Over.");
		boolean playerwins=false;
		if(ounits.size()==0){
			playerwins=true;
		}
		//If somehow both player and opponent simultaneously have no pokemon (say Self-Destruct was used), player is considered to have lost.
		if(punits.size()==0){
			playerwins=false;
		}
		if(playerwins)
			win();
		else
			lose();
	}

	private static void win(){
		//TODO:
	}

	private static void lose(){
		//TODO:
	}

	public static void flee(){
		System.out.println("You have fled from the battle!");
		close();
		MapEngine.initialize(PlayerData.getLocation());
	}

	public static void openPlayerMenu(){
		System.out.println("Opening battle menu");
		MenuEngine.initialize(new BattlePlayerMenu());
	}

	public static void openUnitMenu(){
		System.out.println("Opening battle menu");
		MenuEngine.initialize(new UnitMenu(activeunit));
	}
	
	public static void attack(Move move){
		System.out.println(activeunit.getPokemon().getName()+" using "+GameData.getMoveName(move.getNum()));
		//TODO:
		activeunit.setHasTakenAction(true);
	}
	
	public static void endTurn(){
		activeunit.setHasEndedTurn(true);
		nextTurn();
	}
	
	public static void move(){
		//TODO:display movement range, allow input to traverse and select desired location then move unit to that location.
	}
	
	public static Unit getActiveUnit(){
		return activeunit;
	}
	
	public static Pokemon getActivePokemon(){
		return activeunit.getPokemon();
	}

}
