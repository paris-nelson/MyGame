package Engines;

import java.awt.event.KeyListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import Enums.BondCondition;
import Enums.PermCondition;
import Enums.ProtectionCondition;
import Enums.SelectionType;
import Enums.Stat;
import Enums.TempCondition;
import Enums.Tile;
import Enums.Type;
import Global.Constants;
import Global.GameData;
import Global.PlayerData;
import KeyListeners.BattleAttackKeyListener;
import KeyListeners.BattleMovementKeyListener;
import Menus.BattlePlayerMenu;
import Menus.UnitMenu;
import Objects.BattlefieldMaker;
import Objects.EliteTrainer;
import Objects.IntPair;
import Objects.LoadExistingBattlefieldMaker;
import Objects.Move;
import Objects.MoveLogic;
import Objects.Pokemon;
import Objects.ProceduralBattlefieldMaker;
import Objects.Square;
import Objects.Trainer;
import Objects.Unit;
import Objects.WildTrainer;
import acm.graphics.GCompound;

public class BattleEngine {

	private static Trainer opponent;
	private static ArrayList<Unit> punits;
	private static ArrayList<Unit> ounits;
	private static ArrayList<Unit> allunits;
	private static ArrayList<Integer> priorities;
	private static int activeindex;
	private static Unit activeunit;
	private static boolean inbattle;
	private static GCompound battlefieldimage;
	private static Square[][] battlefield;
	private static BattlefieldMaker bfmaker;
	private static ArrayList<IntPair> validdestinations;
	private static SelectionType attackselection;
	private static int attackrange;
	private static IntPair epicenter;
	private static ArrayList<IntPair> validtargets;
	private static Move currattack;
	private static ArrayList<IntPair> previousmoves;
	private static boolean spikesplaced;
	private static int numpaydays;

	public static void initialize(Trainer newopponent){
		System.out.println("Initializing battle with "+newopponent.getName());
		inbattle=true;
		opponent=newopponent;
		bfmaker=new ProceduralBattlefieldMaker();
		spikesplaced=false;
		numpaydays=0;
		initBattlefield();
		initUnits();
		setPriorities();
		activeindex=0;
		activeunit=getUnitByID(priorities.get(0));
		takeTurn();
	}

	private static void initBattlefield(){
		bfmaker.makeNew();
		battlefieldimage=bfmaker.getImage();
		battlefield=bfmaker.getResult();
		GameData.getGUI().add(battlefieldimage,10,10);  
	}

	public static void initUnits(){
		punits=new ArrayList<Unit>();
		ounits=new ArrayList<Unit>();
		allunits=new ArrayList<Unit>();
		Pokemon[] party=opponent.getParty();
		int idcounter=1;
		for(int i=0;i<party.length;i++){ 
			ounits.add(new Unit(party[i],i,idcounter));
			idcounter++;
		}
		ArrayList<Pokemon> party2=PlayerData.getParty();
		for(int i=0;i<party2.size();i++){
			punits.add(new Unit(party2.get(i),i,idcounter));
			idcounter++;
		}
		//insertion sort opp units by desc level
		for(int pointer=1;pointer<ounits.size();pointer++){
			int hole=pointer;
			while(hole>0&&ounits.get(hole).getPokemon().getLevel()>ounits.get(hole-1).getPokemon().getLevel()){
				Unit temp=ounits.get(hole-1);
				ounits.set(hole-1,ounits.get(hole));
				ounits.set(hole,temp);
				hole--;
			}
		}		
		allunits.addAll(punits);
		allunits.addAll(ounits);
		placeNewOppUnits();
		placeNewPlayerUnits();
	}

	private static void setPriorities(){
		priorities=new ArrayList<Integer>();
		for(int i=0;i<allunits.size();i++){
			priorities.add(allunits.get(i).getID());
		}
		ArrayList<Integer> speeds=new ArrayList<Integer>();
		for(Unit u:allunits){
			u.calculateMovementRange();
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
		//if unit is holding quick claw, it has 20% chance to get first place in queue. This is evaluated from slowest to fastest. if multiple units are holding
		//claw and get the 20%, the faster will still be put first
		//removes priority links from queue and places in temp queue fastest to slowest
		ArrayList<Integer> clawholders=new ArrayList<Integer>();
		for(int i=0;i<priorities.size();i++){
			if(getUnitByID(priorities.get(i)).getPokemon().isHolding("Quick Claw")&&GameData.getRandom().nextInt(100)<Constants.QUICK_CLAW_CHANCE){
				clawholders.add(priorities.remove(i));
				i--;
			}
		}
		//inserts from temp queue into front of priorities queue, slowest pushed in first and fastest put in last
		for(int i=clawholders.size()-1;i>=0;i--){
			System.out.println(getUnitByID(clawholders.get(i)).getPokemon().getName()+" is currently first in queue thanks to quick claw");
			priorities.add(clawholders.get(i),0);
		}
	}

	private static void nextTurn(){
		System.out.println("Going to next turn");
		if(getNumLiveUnits(punits)==0||getNumLiveUnits(ounits)==0)
			battleOver();
		activeindex++;
		if(activeindex>=priorities.size()){
			setPriorities();
			activeindex=0;
		}
		activeunit=getUnitByID(priorities.get(activeindex));
		if(activeunit.getPokemon().isFainted())
			nextTurn();
		else
			takeTurn();
	}

	private static void takeTurn(){
		activeunit.setHasMoved(false);
		activeunit.setHasTakenAction(false);
		activeunit.setHasEndedTurn(false);
		activeunit.setHasAttacked(false);
		System.out.println(activeunit.getPokemon().getName()+" ("+activeindex+" in order) taking turn");
		MoveLogic.startOfTurnActions(activeunit);
		if(activeunit.getPokemon().isFainted())
			nextTurn();
		else
			openUnitMenu();
	}



	public static Unit getUnitByID(int id){
		for(Unit u:allunits){
			if(u.getID()==id)
				return u;
		}
		return null;
	}

	public static void takeControl(KeyListener kl){
		GameData.getGUI().giveControl(kl);
	}

	public static void giveControl(){
		GameData.getGUI().giveControl(null);
	}



	private static void placeNewPlayerUnits(){
		//For now will auto place units the same way it auto places opp.
		//TODO: Decide if I want to allow player to place as many as they want in their own order
		switch(punits.size()){
		case 6:moveOnSquare(punits.get(5),Constants.BATTLEFIELD_WIDTH-1,Constants.BATTLEFIELD_HEIGHT/2-3);
		case 5:moveOnSquare(punits.get(4),Constants.BATTLEFIELD_WIDTH-1,Constants.BATTLEFIELD_HEIGHT/2+2);
		case 4:moveOnSquare(punits.get(3),Constants.BATTLEFIELD_WIDTH-1,Constants.BATTLEFIELD_HEIGHT/2-2);
		case 3:moveOnSquare(punits.get(2),Constants.BATTLEFIELD_WIDTH-1,Constants.BATTLEFIELD_HEIGHT/2+1);
		case 2:moveOnSquare(punits.get(1),Constants.BATTLEFIELD_WIDTH-1,Constants.BATTLEFIELD_HEIGHT/2-1);
		case 1:moveOnSquare(punits.get(0),Constants.BATTLEFIELD_WIDTH-1,Constants.BATTLEFIELD_HEIGHT/2);
		}
	}

	private static void placeNewOppUnits(){
		switch(ounits.size()){
		case 6:moveOnSquare(ounits.get(5),0,Constants.BATTLEFIELD_HEIGHT/2-3);
		case 5:moveOnSquare(ounits.get(4),0,Constants.BATTLEFIELD_HEIGHT/2+2);
		case 4:moveOnSquare(ounits.get(3),0,Constants.BATTLEFIELD_HEIGHT/2-2);
		case 3:moveOnSquare(ounits.get(2),0,Constants.BATTLEFIELD_HEIGHT/2+1);
		case 2:moveOnSquare(ounits.get(1),0,Constants.BATTLEFIELD_HEIGHT/2-1);
		case 1:moveOnSquare(ounits.get(0),0,Constants.BATTLEFIELD_HEIGHT/2);
		}
	}

	private static void placeExistingUnits(){
		for(Unit u:allunits){
			moveOnSquare(u,u.getCoordinates().getX(),u.getCoordinates().getY());
		}
	}

	private static void moveOnSquare(Unit unit,int x,int y){
		unit.setCoordinates(x, y);
		battlefield[x][y].setUnit(unit.getID());
		battlefieldimage.add(unit.getImage(),x*Constants.TILE_SIZE,y*Constants.TILE_SIZE);
	}

	private static void moveOffSquare(Unit unit,int x,int y){
		battlefield[x][y].removeUnit();
		battlefieldimage.remove(unit.getImage());
	}

	/**
	 * Resumes a battle that was saved previously. Called on initialization if last player save was mid-battle.
	 */
	public static void load(){
		inbattle=true;
		try{
			File f=new File(Constants.PATH+"\\InitializeData\\battlesavefile.txt");
			Scanner s=new Scanner(f);
			String curr=s.next();
			if(curr.equals("WildTrainer"))
				opponent=WildTrainer.readInTrainer(s);
			else if(curr.equals("EliteTrainer"))
				opponent=EliteTrainer.readInTrainer(s);
			else
				opponent=Trainer.readInTrainer(s);
			System.out.println("Initializing previous battle against "+opponent.getName());
			curr=s.next();
			allunits=new ArrayList<Unit>();
			punits=new ArrayList<Unit>();
			ounits=new ArrayList<Unit>();
			while(!curr.equals("End")){
				punits.add(Unit.readInUnit(null,s));
				curr=s.next();
			}
			s.nextLine();// Player Units
			curr=s.next();
			while(!curr.equals("End")){
				ounits.add(Unit.readInUnit(opponent,s));
				curr=s.next();
			}
			s.nextLine();// Opponent Units
			allunits.addAll(punits);
			allunits.addAll(ounits);
			priorities=new ArrayList<Integer>();
			int[] temp=GameData.readIntArray(s.nextLine());
			for(int i:temp){
				priorities.add(i);
			}
			spikesplaced=s.nextBoolean();
			numpaydays=s.nextInt();
			activeindex=s.nextInt();
			activeunit=getUnitByID(s.nextInt());
			s.nextLine();
			bfmaker=new LoadExistingBattlefieldMaker(s);
			initBattlefield();
			placeExistingUnits();
			MenuEngine.initialize(new UnitMenu(activeunit));
		}catch(Exception e){e.printStackTrace();System.out.println(e.getCause().toString());}
	}

	/**
	 * Saves all battle data to file to be loaded on initialization of program. Used when player saves mid battle.
	 */
	public static void save(){
		System.out.println("Saving battle data");
		try{
			File f=new File(Constants.PATH+"\\InitializeData\\battlesavefile.txt");
			PrintWriter pw=new PrintWriter(f);
			pw.println(opponent.toString());
			for(Unit u:punits){
				pw.println(u.toString());
			}
			pw.println("End Player Units");
			for(Unit u:ounits){
				pw.println(u.toString());
			}
			pw.println("End Opponent Units");
			pw.println(priorities);
			pw.println(spikesplaced+" "+numpaydays+" "+activeindex+" "+activeunit.getID());
			pw.println(bfmaker.toString());
			pw.close();
		}catch(Exception e){e.printStackTrace();}
	}
	
	public static int getPayDayVal(){
		return numpaydays;
	}
	
	public static void incPayDayVal(int lvl){
		numpaydays+=lvl;
	}

	public static Unit getActiveUnit(){
		return activeunit;
	}

	public static Pokemon getActivePokemon(){
		return activeunit.getPokemon();
	}

	public static boolean isInBattle(){
		return inbattle;
	}

	public static Trainer currOpponent(){
		return opponent;
	}

	private static void battleOver(){
		System.out.println("Battle Over.");
		boolean playerwins=false;
		if(getNumLiveUnits(ounits)==0){
			playerwins=true;
		}
		//If somehow both player and opponent simultaneously have no pokemon (say Self-Destruct was used), player is considered to have lost.
		if(getNumLiveUnits(punits)==0){
			playerwins=false;
		}
		if(playerwins)
			win();
		else
			lose();
	}

	private static ArrayList<Unit> getLiveUnits(ArrayList<Unit> party){
		ArrayList<Unit> live=new ArrayList<Unit>();
		for(Unit u:party){
			if(!u.getPokemon().isFainted())
				live.add(u);
		}
		return live;
	}

	/**
	 * Returns the number of non-fainted units from the given party. 
	 * @param party
	 * @return
	 */
	private static int getNumLiveUnits(ArrayList<Unit> party){
		int count=0;
		for(Unit u:party){
			if(!u.getPokemon().isFainted())
				count++;
		}
		return count;
	}

	public static void openPlayerMenu(){
		System.out.println("Opening battle menu");
		MenuEngine.initialize(new BattlePlayerMenu());
	}

	public static void openUnitMenu(){
		System.out.println("Opening battle menu");
		MenuEngine.initialize(new UnitMenu(activeunit));
	}

	public static void useMove(Move move,boolean cancellable){
		System.out.println(activeunit.getPokemon().getName()+" using "+GameData.getMoveName(move.getNum()));
		//hits self in confusion check
		if(cancellable&&activeunit.getPokemon().getPcondition()==PermCondition.Confusion&&GameData.getRandom().nextBoolean()){
			System.out.println(activeunit.getPokemon().getName()+" hit itself in its confusion");
			activeunit.damage(calculateConfusionSelfHitDamage(activeunit));
		}
		else{
			currattack=move;
			HashMap<String,String> map=GameData.getMoveRange(move.getNum());
			attackselection=SelectionType.valueOf(map.get("Selection"));
			if(attackselection==SelectionType.Area||attackselection==SelectionType.Beam||attackselection==SelectionType.Cone
					||attackselection==SelectionType.Nova||attackselection==SelectionType.Single)
				attackrange=Integer.parseInt(map.get("Range"));
			else
				attackrange=-1;
			if(activeunit.getPokemon().isHolding("Scope Lens"))
				attackrange++;
			validtargets=getDefaultAttackSelection();
			System.out.println(validtargets);
			displayAttackRange();
			takeControl(new BattleAttackKeyListener(cancellable));
		}
	}

	private static ArrayList<IntPair> getDefaultAttackSelection(){
		ArrayList<IntPair> selection=new ArrayList<IntPair>();
		if(attackselection==SelectionType.Single){
			IntPair curr=activeunit.getCoordinates();
			if(curr.getX()>0)
				selection.add(new IntPair(curr.getX()-1,curr.getY()));
			else
				selection.add(new IntPair(curr.getX()+1,curr.getY()));
		}
		else if(attackselection==SelectionType.Self){
			selection.add(activeunit.getCoordinates());
		}
		else if(attackselection==SelectionType.Nova){
			IntPair curr=activeunit.getCoordinates();
			for(int x=curr.getX()-attackrange;x<=curr.getX()+attackrange;x++){
				for(int y=curr.getY()-attackrange;y<=curr.getY()+attackrange;y++){
					if(y>=Constants.BATTLEFIELD_HEIGHT)
						break;
					if(x==curr.getX()&&y==curr.getY())
						continue;
					if(x>=0&&y>=0&&x<Constants.BATTLEFIELD_WIDTH)
						selection.add(new IntPair(x,y));
				}
			}
		}
		else if(attackselection==SelectionType.Beam){
			IntPair curr=activeunit.getCoordinates();
			if(curr.getX()-1>=0){
				for(int i=1;i<=attackrange;i++){
					if(curr.getX()-i>=0)
						selection.add(new IntPair(curr.getX()-i,curr.getY()));
				}
			}
			else{
				for(int i=1;i<=attackrange;i++){
					selection.add(new IntPair(curr.getX()+i,curr.getY()));
				}
			}	
		}
		else if(attackselection==SelectionType.Area){
			IntPair curr=activeunit.getCoordinates();
			if(curr.getX()>3){
				epicenter=new IntPair(curr.getX()-2,curr.getY());
				for(int x=curr.getX()-3;x<curr.getX();x++){
					for(int y=curr.getY()-1;y<=curr.getY()+1;y++){
						if(y>=0)
							selection.add(new IntPair(x,y));
					}
				}
			}
			else{
				epicenter=new IntPair(curr.getX()+2,curr.getY());
				for(int x=curr.getX()+1;x<curr.getX()+4;x++){
					for(int y=curr.getY()-1;y<=curr.getY()+1;y++){
						if(y>=0)
							selection.add(new IntPair(x,y));
					}
				}
			}
		}
		else if(attackselection==SelectionType.Cone){
			IntPair curr=activeunit.getCoordinates();
			if(curr.getX()>=attackrange){
				for(int i=1;i<=attackrange;i++){
					for(int j=1-i;j<=i-1;j++){
						IntPair newpair=new IntPair(curr.getX()-i,curr.getY()+j);
						if(newpair.getY()>0&&newpair.getY()<battlefield[0].length)
							selection.add(newpair);
					}
				}
			}
			else{
				for(int i=1;i<=attackrange;i++){
					for(int j=1-i;j<=i-1;j++){
						IntPair newpair=new IntPair(curr.getX()+i,curr.getY()+j);
						if(newpair.getY()>0&&newpair.getY()<battlefield[0].length)
							selection.add(newpair);
					}
				}
			}
		}
		else if(attackselection==SelectionType.Global||attackselection==SelectionType.Weather){
			for(int x=0;x<battlefield.length;x++){
				for(int y=0;y<battlefield[0].length;y++){
					selection.add(new IntPair(x,y));
				}
			}
		}
		else if(attackselection==SelectionType.Friendly){
			for(Unit u:getFriendlyUnits(activeunit)){
				selection.add(u.getCoordinates());
			}
		}
		return selection;
	}
	
	public static ArrayList<Unit> getFriendlyUnits(Unit unit){
		ArrayList<Unit> friends=new ArrayList<Unit>();
		if(ounits.contains(unit)){
			friends.addAll(ounits);
		}
		else{
			friends.addAll(punits);
		}
		return friends;
	}

	private static void removeAttackRange(){
		for(IntPair pair:validtargets){
			battlefield[pair.getX()][pair.getY()].markNeutral();
		}
	}

	private static void displayAttackRange(){
		for(IntPair pair:validtargets){
			battlefield[pair.getX()][pair.getY()].markValid();
		}
	}

	public static void moveAttackRangeLeft(){
		removeAttackRange();
		if(attackselection==SelectionType.Single){
			IntPair curr=validtargets.get(0);
			if(curr.getX()>0){
				IntPair next=new IntPair(curr.getX()-1,curr.getY());
				if(curr.distanceFrom(next)<=attackrange)
				validtargets.set(0,next);
			}
		}
		else if(attackselection==SelectionType.Beam){
			IntPair curr=validtargets.get(0);
			int x=activeunit.getCoordinates().getX();
			if(curr.getX()>=x&&x>0){
				validtargets.clear();
				for(int i=1;i<=attackrange;i++){
					if(x-i>=0)
						validtargets.add(new IntPair(x-i,curr.getY()));
				}
			}
		}
		else if(attackselection==SelectionType.Cone){
			IntPair curr=validtargets.get(0);
			IntPair active=activeunit.getCoordinates();
			if(curr.getX()>=active.getX()){
				validtargets.clear();
				for(int i=1;i<=attackrange;i++){
					for(int j=1-i;j<=i-1;j++){
						IntPair newpair=new IntPair(active.getX()-i,active.getY()+j);
						if(newpair.getY()>=00&&newpair.getY()<battlefield[0].length)
							validtargets.add(newpair);
					}
				}
			}
		}
		else if(attackselection==SelectionType.Area){
			if(epicenter.getX()>0&&activeunit.getCoordinates().distanceFrom(new IntPair(epicenter.getX()-1,epicenter.getY()))<=attackrange){
				for(int i=0;i<validtargets.size();i++){
					IntPair pair=validtargets.get(i);
					if(pair.getX()-1>=0)
						validtargets.set(i,new IntPair(pair.getX()-1,pair.getY()));
					else{
						validtargets.remove(i);
						i--;
					}
				}
			}
		}
		displayAttackRange();
	}

	public static void moveAttackRangeRight(){
		removeAttackRange();
		if(attackselection==SelectionType.Single){
			IntPair curr=validtargets.get(0);
			if(curr.getX()<battlefield.length-1){
				IntPair next=new IntPair(curr.getX()+1,curr.getY());
				if(curr.distanceFrom(next)<=attackrange)
				validtargets.set(0,next);
			}
		}
		else if(attackselection==SelectionType.Beam){
			IntPair curr=validtargets.get(0);
			int x=activeunit.getCoordinates().getX();
			if(curr.getX()<=x&&x<battlefield.length-1){
				validtargets.clear();
				for(int i=1;i<=attackrange;i++){
					if(x+i<=battlefield.length-1)
						validtargets.add(new IntPair(x+i,curr.getY()));
				}
			}
		}
		else if(attackselection==SelectionType.Cone){
			IntPair curr=validtargets.get(0);
			IntPair active=activeunit.getCoordinates();
			if(curr.getX()<=active.getX()){
				validtargets.clear();
				for(int i=1;i<=attackrange;i++){
					for(int j=1-i;j<=i-1;j++){
						IntPair newpair=new IntPair(active.getX()+i,active.getY()+j);
						if(newpair.getY()>=0&&newpair.getY()<battlefield[0].length)
							validtargets.add(newpair);
					}
				}
			}
		}
		else if(attackselection==SelectionType.Area){
			if(epicenter.getX()<battlefield.length-1&&activeunit.getCoordinates().distanceFrom(new IntPair(epicenter.getX()+1,epicenter.getY()))<=attackrange){
				for(int i=0;i<validtargets.size();i++){
					IntPair pair=validtargets.get(i);
					if(pair.getX()+1<=battlefield.length-1)
						validtargets.set(i,new IntPair(pair.getX()+1,pair.getY()));
					else{
						validtargets.remove(i);
						i--;
					}
				}
			}
		}
		displayAttackRange();
	}

	public static void moveAttackRangeUp(){
		removeAttackRange();
		if(attackselection==SelectionType.Single){
			IntPair curr=validtargets.get(0);
			if(curr.getY()>0){
				IntPair next=new IntPair(curr.getX(),curr.getY()-1);
				if(curr.distanceFrom(next)<=attackrange)
				validtargets.set(0,next);
			}
		}
		else if(attackselection==SelectionType.Beam){
			IntPair curr=validtargets.get(0);
			int y=activeunit.getCoordinates().getY();
			if(curr.getY()>=y&&y>0){
				validtargets.clear();
				for(int i=1;i<=attackrange;i++){
					if(y-i>=0)
						validtargets.add(new IntPair(curr.getX(),y-i));
				}
			}
		}
		else if(attackselection==SelectionType.Cone){
			IntPair curr=validtargets.get(0);
			IntPair active=activeunit.getCoordinates();
			if(curr.getY()>=active.getY()){
				validtargets.clear();
				for(int i=1;i<=attackrange;i++){
					for(int j=1-i;j<=i-1;j++){
						IntPair newpair=new IntPair(active.getX()+j,active.getY()-i);
						if(newpair.getX()>=0&&newpair.getX()<battlefield[0].length)
							validtargets.add(newpair);
					}
				}
			}
		}
		else if(attackselection==SelectionType.Area){
			if(epicenter.getY()>0&&activeunit.getCoordinates().distanceFrom(new IntPair(epicenter.getX(),epicenter.getY()-1))<=attackrange){
				for(int i=0;i<validtargets.size();i++){
					IntPair pair=validtargets.get(i);
					if(pair.getY()-1>=0)
						validtargets.set(i,new IntPair(pair.getX(),pair.getY()-1));
					else{
						validtargets.remove(i);
						i--;
					}
				}
			}
		}
		displayAttackRange();
	}

	public static void moveAttackRangeDown(){
		removeAttackRange();
		if(attackselection==SelectionType.Single){
			IntPair curr=validtargets.get(0);
			if(curr.getY()<battlefield.length-1){
				IntPair next=new IntPair(curr.getX(),curr.getY()+1);
				if(curr.distanceFrom(next)<=attackrange)
				validtargets.set(0,next);
			}
		}
		else if(attackselection==SelectionType.Beam){
			IntPair curr=validtargets.get(0);
			int y=activeunit.getCoordinates().getY();
			if(curr.getY()<=y&&y<battlefield.length-1){
				validtargets.clear();
				for(int i=1;i<=attackrange;i++){
					if(y+i<=battlefield.length-1)
						validtargets.add(new IntPair(curr.getX(),y+i));
				}
			}
		}
		else if(attackselection==SelectionType.Cone){
			IntPair curr=validtargets.get(0);
			IntPair active=activeunit.getCoordinates();
			if(curr.getY()<=active.getY()){
				validtargets.clear();
				for(int i=1;i<=attackrange;i++){
					for(int j=1-i;j<=i-1;j++){
						IntPair newpair=new IntPair(active.getX()+j,active.getY()+i);
						if(newpair.getX()>=0&&newpair.getX()<battlefield[0].length)
							validtargets.add(newpair);
					}
				}
			}
		}
		else if(attackselection==SelectionType.Area){
			if(epicenter.getY()<battlefield.length-1&&activeunit.getCoordinates().distanceFrom(new IntPair(epicenter.getX(),epicenter.getY()+1))<=attackrange){
				for(int i=0;i<validtargets.size();i++){
					IntPair pair=validtargets.get(i);
					if(pair.getY()+1<=battlefield.length-1)
						validtargets.set(i,new IntPair(pair.getX(),pair.getY()+1));
					else{
						validtargets.remove(i);
						i--;
					}
				}
			}
		}
		displayAttackRange();
	}

	public static void confirmAttackRange(){
		//TODO: get passed whether or not this is a cancellable move. if not, don't set prevmove
		ArrayList<Unit> targets=new ArrayList<Unit>();
		for(IntPair pair:validtargets){
			targets.add(getUnitByID(battlefield[pair.getX()][pair.getY()].getUnit()));
		}
		MoveLogic.implementEffects(activeunit,targets,currattack);
		//call method to implement self targetting move effects
		//for all units in validtargets range, call method to implement remaining moveeffects
	}

	public static void cancelAttackRange(){
		removeAttackRange();
		System.out.println(activeunit.getPokemon().getName()+" has cancelled the attack option");
		MenuEngine.initialize(new UnitMenu(activeunit));
	}

	private static void afterAttackEffects(){


		//implementation logic
		//TODO:then parse moveeffects file for effects and apply effects below to each target( except self-targetting effects which apply once)
		//moveeffects implementation logic should likely be in it's own helper class for space reasons.
		//		for(Unit target:targets){
		//			System.out.println(activeunit.getPokemon().getName()+" attacks "+target.getPokemon().getName()+" with "+GameData.getMoveName(move.getNum()));
		//			//target.damage(calculateDamage(activeunit,target,move.getNum()));
		//			//TODO: endure/focus band logic
		//		}
		//		for(Unit target:targets){
		//			Pokemon targetpokemon=target.getPokemon();
		//			//XP gain if target fainted (note xp is not given to opp units on defeating players units)
		//			if(ounits.contains(target)&&targetpokemon.isFainted()){
		//				ArrayList<Pokemon> xprecipients=new ArrayList<Pokemon>();
		//				for(Unit unit:getLiveUnits(punits)){
		//					Pokemon pokemon=unit.getPokemon();
		//					if(pokemon.isHolding("Exp Share")||target.attackedBy(unit.getID()))
		//						xprecipients.add(pokemon);
		//				}
		//				awardExperience(xprecipients,targetpokemon);
		//			}
		//		}



		activeunit.setHasAttacked(true);
		activeunit.setHasTakenAction(true);
		//confusion turn count is only lowered by attacking turns. Pokemon can't avoid confusion by refusing to attack until it's over
		if(activeunit.getPokemon().getPcondition()==PermCondition.Confusion)
			activeunit.incNumTurnsAfflicted(PermCondition.Confusion.toString());
		MenuEngine.initialize(new UnitMenu(activeunit));
	}

	private static void awardExperience(ArrayList<Pokemon> recipients,Pokemon giver){
		double xpshare=GameData.getBaseExp(giver.getNum())*giver.getLevel();
		if(!giver.isWild())
			xpshare*=1.5;
		xpshare/=recipients.size();
		for(Pokemon p:recipients){
			p.gainExp(round(xpshare));
			System.out.println(p.getName()+" gains "+xpshare+" experience");
		}
	}

	public static void endTurn(){
		activeunit.setHasEndedTurn(true);
		MoveLogic.endOfTurnActions(activeunit);
		nextTurn();
	}

	public static void move(){
		previousmoves=new ArrayList<IntPair>();
		previousmoves.add(activeunit.getCoordinates());
		displayMovementRange();
		takeControl(new BattleMovementKeyListener());
	}

	private static void displayMovementRange(){
		validdestinations=new ArrayList<IntPair>();
		int range=activeunit.getMovementRange()-previousmoves.size()+1;
		int xcoordinate=activeunit.getCoordinates().getX();
		int ycoordinate=activeunit.getCoordinates().getY();
		validdestinations.add(activeunit.getCoordinates());
		System.out.println(activeunit.getPokemon().getName()+" moving up to "+range+" units");
		for(int x=xcoordinate-range;x<=xcoordinate+range;x++){
			for(int y=ycoordinate-range;y<=ycoordinate+range;y++){
				if(y>=Constants.BATTLEFIELD_HEIGHT)
					break;
				if(x>=0&&y>=0&&x<Constants.BATTLEFIELD_WIDTH
						&&Math.abs(x-xcoordinate)+Math.abs(y-ycoordinate)<=range&&canMoveTo(battlefield[x][y]))
					validdestinations.add(new IntPair(x,y));
			}
		}
		for(IntPair pair:previousmoves){
			if(!validdestinations.contains(pair))
				validdestinations.add(pair);
		}
		for(IntPair pair:validdestinations){
			battlefield[pair.getX()][pair.getY()].markValid();
		}
	}

	private static boolean canMoveTo(Square square){
		if(square.getUnit()>-1)
			return false;
		if(square.getTileType()==Tile.Rock||square.getTileType()==Tile.Tree)
			return false;
		if(square.getTileType()==Tile.Water&&!activeunit.isType(Type.Water))
			return false;
		if(square.getTileType()==Tile.Lava&&!activeunit.isType(Type.Fire))
			return false;
		//TODO: Implement logic so that flyers can move over water/lava tiles, but cannot stop on them
		//add check in confirmMovement that either disallows movement and/or gives prompt to user that 
		//they cannot end there.
		return true;
	}

	public static void moveLeft(){
		int x=activeunit.getCoordinates().getX();
		int y=activeunit.getCoordinates().getY();
		IntPair newcoordinates=new IntPair(x-1,y);
		if(previousmoves.size()==activeunit.getMovementRange()+1&&previousmoves.get(previousmoves.size()-2).equals(newcoordinates)
				||(x>0&&battlefield[x-1][y].isValid())){
			moveOffSquare(activeunit,x,y);
			moveOnSquare(activeunit,x-1,y);
			if(previousmoves.size()>1&&previousmoves.get(previousmoves.size()-2).equals(newcoordinates))
				previousmoves.remove(previousmoves.size()-1);
			else
				previousmoves.add(newcoordinates);
		}
		for(IntPair pair:validdestinations){
			battlefield[pair.getX()][pair.getY()].markNeutral();
		}
		displayMovementRange();
	}

	public static void moveRight(){
		int x=activeunit.getCoordinates().getX();
		int y=activeunit.getCoordinates().getY();
		IntPair newcoordinates=new IntPair(x+1,y);
		if(previousmoves.size()==activeunit.getMovementRange()+1&&previousmoves.get(previousmoves.size()-2).equals(newcoordinates)
				||(x<Constants.BATTLEFIELD_WIDTH-1&&battlefield[x+1][y].isValid())){
			moveOffSquare(activeunit,x,y);
			moveOnSquare(activeunit,x+1,y);
			if(previousmoves.size()>1&&previousmoves.get(previousmoves.size()-2).equals(newcoordinates))
				previousmoves.remove(previousmoves.size()-1);
			else
				previousmoves.add(newcoordinates);
		}
		for(IntPair pair:validdestinations){
			battlefield[pair.getX()][pair.getY()].markNeutral();
		}
		displayMovementRange();
	}

	public static void moveUp(){
		int x=activeunit.getCoordinates().getX();
		int y=activeunit.getCoordinates().getY();
		IntPair newcoordinates=new IntPair(x,y-1);
		if(previousmoves.size()==activeunit.getMovementRange()+1&&previousmoves.get(previousmoves.size()-2).equals(newcoordinates)
				||(y>0&&battlefield[x][y-1].isValid())){
			moveOffSquare(activeunit,x,y);
			moveOnSquare(activeunit,x,y-1);
			if(previousmoves.size()>1&&previousmoves.get(previousmoves.size()-2).equals(newcoordinates))
				previousmoves.remove(previousmoves.size()-1);
			else
				previousmoves.add(newcoordinates);
		}
		for(IntPair pair:validdestinations){
			battlefield[pair.getX()][pair.getY()].markNeutral();
		}
		displayMovementRange();
	}

	public static void moveDown(){
		int x=activeunit.getCoordinates().getX();
		int y=activeunit.getCoordinates().getY();
		IntPair newcoordinates=new IntPair(x,y+1);
		if(previousmoves.size()==activeunit.getMovementRange()+1&&previousmoves.get(previousmoves.size()-2).equals(newcoordinates)
				||(y<Constants.BATTLEFIELD_HEIGHT-1&&battlefield[x][y+1].isValid())){
			moveOffSquare(activeunit,x,y);
			moveOnSquare(activeunit,x,y+1);
			if(previousmoves.size()>1&&previousmoves.get(previousmoves.size()-2).equals(newcoordinates))
				previousmoves.remove(previousmoves.size()-1);
			else
				previousmoves.add(newcoordinates);
		}
		for(IntPair pair:validdestinations){
			battlefield[pair.getX()][pair.getY()].markNeutral();
		}
		displayMovementRange();
	}

	public static void confirmMovement(){
		for(IntPair pair:validdestinations){
			battlefield[pair.getX()][pair.getY()].markNeutral();
		}
		activeunit.setHasMoved(true);
		System.out.println(activeunit.getPokemon().getName()+" has moved "+(previousmoves.size()-1)+" squares.");
		if(spikesplaced){
			int spikescount=0;
			for(IntPair pair:previousmoves){
				if(battlefield[pair.getX()][pair.getY()].hasSpikes())
					spikescount++;
			}
			int damage=round(spikescount*activeunit.getPokemon().getStat(Stat.HP)*Constants.SPIKES_DAMAGE_RATE);
			System.out.println(activeunit.getPokemon().getName()+" takes "+damage+" damage from spikes");
			activeunit.getPokemon().decHP(damage);
		}
		if(!activeunit.getPokemon().isFainted())
			MenuEngine.initialize(new UnitMenu(activeunit));
		else
			nextTurn();
	}

	public static void cancelMovement(){
		for(IntPair pair:validdestinations){
			battlefield[pair.getX()][pair.getY()].markNeutral();
		}
		System.out.println(activeunit.getPokemon().getName()+" has cancelled the move option");
		MenuEngine.initialize(new UnitMenu(activeunit));
	}

	private static int round(double num){
		int intnum=(int)num;
		if(num<intnum+0.5)
			return intnum;
		else 
			return intnum+1;
	}
	
	//based on formula found here https://www.math.miami.edu/~jam/azure/attacks/comp/confuse.htm
	//if this is unsatisfactory, use modification of normal damage formula for base 40 power typeless physical attack.
	private static int calculateConfusionSelfHitDamage(Unit attacker){
		int damage=0;
		damage=2*activeunit.getPokemon().getLevel()/5+2;
		damage*=40;
		damage*=activeunit.getStat(Stat.Attack);
		damage/=activeunit.getStat(Stat.Defense);
		damage/=50;
		damage+=2;
		System.out.println(activeunit.getPokemon().getName()+" hits itself for "+damage+" damage");
		return damage;
	}

	public static boolean doesCatchSucceed(Pokemon target, double ballbonus){
		double a=calculateA(target,ballbonus);
		System.out.println("Catch Rate A: "+a);
		if(GameData.getRandom().nextInt(256)<a)
			return true;
		else{
			for(int i=0;i<3;i++){
				double b=calculateB(a);
				System.out.println("Catch Rate B: "+b);
				if(GameData.getRandom().nextInt(65536)>=b)
					return false;
				System.out.println("Catch passes shake #"+(i+1));
			}
			return true;
		}
	}

	private static double calculateB(double a){
		return 1048560/(Math.sqrt((int)Math.sqrt(16711680/a)));
	}

	private static double calculateA(Pokemon target, double ballbonus){
		double a=3*target.getStat(Stat.HP)-2*target.getCurrHP();
		a*=GameData.getCatchRate(target.getNum());
		a*=ballbonus;
		PermCondition condition=target.getPcondition();
		if(condition==PermCondition.Frozen||condition==PermCondition.Sleep)
			a*=2;
		else if(condition==PermCondition.Burn||condition==PermCondition.Paralysis||condition==PermCondition.Poison||condition==PermCondition.BadlyPoison)
			a*=1.5;
		a/=(3*target.getStat(Stat.HP));
		return a;
	}

	public static boolean isLegalMove(Move m){
		Pokemon activepokemon=activeunit.getPokemon();
		if(activepokemon.getPcondition()==PermCondition.Sleep){
			return m.getNum()==GameData.getMoveNum("Snore")||m.getNum()==GameData.getMoveNum("Sleep Talk");
		}
		return true;
	}



	public static void win(){
		System.out.println("Player won the battle against "+opponent.getName());
		//TODO:
		GlobalEngine.defeatedTrainer(opponent);
		close();
		MapEngine.initialize(PlayerData.getLocation());
	}

	private static void lose(){
		//TODO:
	}

	public static void flee(){
		System.out.println("You have fled from the battle!");
		close();
		MapEngine.initialize(PlayerData.getLocation());
	}

	public static void close(){
		System.out.println("Ending battle");
		inbattle=false;
		File f=new File(Constants.PATH+"\\InitializeData\\battlesavefile.txt");
		if(f.exists())
			f.delete();
	}
}
