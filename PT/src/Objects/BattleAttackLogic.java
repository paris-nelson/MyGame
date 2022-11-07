package Objects;

import java.util.ArrayList;
import java.util.HashMap;

import Engines.BattleEngine;
import Engines.GlobalEngine;
import Engines.MenuEngine;
import Enums.MoveMenuMode;
import Enums.PermCondition;
import Enums.SelectionType;
import Enums.Stat;
import Global.Constants;
import Global.GameData;
import KeyListeners.BattleAttackKeyListener;
import Menus.MoveMenu;
import Menus.UnitMenu;

public class BattleAttackLogic {
	private static SelectionType attackselection;
	private static int attackrange;
	private static IntPair epicenter;
	private static ArrayList<IntPair> validtargets;
	private static ArrayList<Unit> targetunits;
	private static Move currattack;
	private static Unit activeunit;
	private static Square[][] battlefield;
	
	private static final boolean DEBUG=true;

	
	public static void useMove(Square[][] thisbattlefield,Move move,boolean cancellable){
		battlefield=thisbattlefield;
		useMove(move,cancellable);
	}
	
	public static void useMove(Move move,boolean cancellable){
		activeunit=BattleEngine.getActiveUnit();
		debug(activeunit.getName()+" using "+GameData.getMoveName(move.getNum()));
		//hits self in confusion check
		if(cancellable&&activeunit.getPokemon().getPcondition()==PermCondition.Confusion&&GameData.getRandom().nextBoolean()){
			debug(activeunit.getName()+" hit itself in its confusion");
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
			if(activeunit.getPokemon().isHolding("Scope Lens")) {
				debug("Range extended by scope lens");
				attackrange++;
			}
			debug("Attack Selection Type: "+attackselection+". Range: "+attackrange);
			validtargets=getDefaultAttackSelection();
			displayAttackRange();
			BattleEngine.takeControl(new BattleAttackKeyListener(cancellable));
		}
	}

	private static ArrayList<IntPair> getDefaultAttackSelection(){
		ArrayList<IntPair> selection=new ArrayList<IntPair>();
		IntPair curr=activeunit.getCoordinates();
		if(attackselection==SelectionType.Single){
			if(curr.getX()>0)
				selection.add(new IntPair(curr.getX()-1,curr.getY()));
			else
				selection.add(new IntPair(curr.getX()+1,curr.getY()));
		}
		else if(attackselection==SelectionType.Self){
			selection.add(activeunit.getCoordinates());
		}
		else if(attackselection==SelectionType.Nova){
			selection.addAll(getSquareGrid(curr,attackrange));
		}
		else if(attackselection==SelectionType.Beam){
			if(curr.getX()>0){
				for(int i=Math.max(curr.getX()-attackrange,0);i<curr.getX();i++){
					selection.add(new IntPair(i,curr.getY()));
				}
			}
			else{
				for(int i=curr.getX()+1;i<=Math.min(Constants.BATTLEFIELD_WIDTH,curr.getX()+attackrange);i++){
					selection.add(new IntPair(i,curr.getY()));
				}
			}	
		}
		else if(attackselection==SelectionType.Area){
			if(curr.getX()>0){
				epicenter=new IntPair(curr.getX()-1,curr.getY());
				selection.addAll(getSquareGrid(epicenter,1));
			}
			else{
				epicenter=new IntPair(curr.getX()+1,curr.getY());
				selection.addAll(getSquareGrid(epicenter,1));
			}
		}
		else if(attackselection==SelectionType.Cone){
			if(curr.getX()>0){
				for(int i=1;i<=attackrange;i++){
					for(int j=1-i;j<=i-1;j++){
						IntPair newpair=new IntPair(curr.getX()-i,curr.getY()+j);
						if(BattleEngine.isWithinBounds(newpair))
							selection.add(newpair);
					}
				}
			}
			else{
				for(int i=1;i<=attackrange;i++){
					for(int j=1-i;j<=i-1;j++){
						IntPair newpair=new IntPair(curr.getX()+i,curr.getY()+j);
						if(BattleEngine.isWithinBounds(newpair))
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
			for(Unit u:BattleEngine.getFriendlyUnits(activeunit)){
				selection.add(u.getCoordinates());
			}
		}
		return selection;
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
	
	private static ArrayList<IntPair> getSquareGrid(IntPair epicenter,int range){
		ArrayList<IntPair> validpoints=new ArrayList<IntPair>();
		for(int x=Math.max(epicenter.getX()-range,0);x<=Math.min(epicenter.getX()+range,Constants.BATTLEFIELD_WIDTH-1);x++) {
			for(int y=Math.max(epicenter.getY()-range,0);y<=Math.min(epicenter.getY()+range,Constants.BATTLEFIELD_HEIGHT-1);y++) {
				validpoints.add(new IntPair(x,y));
			}
		}
		return validpoints;
	}

	public static void moveAttackRangeLeft(){
		removeAttackRange();
		IntPair active=activeunit.getCoordinates();
		int x=active.getX();
		int y=active.getY();
		if(attackselection==SelectionType.Single){
			IntPair curr=validtargets.get(0);
			if(curr.getX()>0){
				IntPair next=new IntPair(curr.getX()-1,curr.getY());
				if(active.distanceFrom(next)<=attackrange)
					validtargets.set(0,next);
			}
		}
		else if(attackselection==SelectionType.Beam){
			IntPair curr=validtargets.get(0);
			//Only need to redraw if the beam isn't already pointing left
			if(curr.getX()>=x){
				validtargets.clear();
				for(int i=Math.max(x-attackrange,0);i<x;i++){
					validtargets.add(new IntPair(i,y));
				}
			}
		}
		else if(attackselection==SelectionType.Cone){
			IntPair curr=validtargets.get(0);
			if(curr.getX()>=x){
				validtargets.clear();
				for(int i=1;i<=attackrange;i++){
					for(int j=1-i;j<=i-1;j++){
						IntPair newpair=new IntPair(x-i,y+j);
						if(BattleEngine.isWithinBounds(newpair))
							validtargets.add(newpair);
					}
				}
			}
		}
		else if(attackselection==SelectionType.Area){
			IntPair next=epicenter.shiftLeft();
			if(epicenter.getX()>0&&next.distanceFrom(active)<=attackrange) {
				epicenter=next;
				validtargets.clear();
				validtargets.addAll(getSquareGrid(epicenter,1));
			}
		}
		displayAttackRange();
	}

	public static void moveAttackRangeRight(){
		removeAttackRange();
		IntPair active=activeunit.getCoordinates();
		int x=activeunit.getCoordinates().getX();
		int y=activeunit.getCoordinates().getY();
		if(attackselection==SelectionType.Single){
			IntPair curr=validtargets.get(0);
			if(curr.getX()<battlefield.length-1){
				IntPair next=new IntPair(curr.getX()+1,curr.getY());
				if(active.distanceFrom(next)<=attackrange)
					validtargets.set(0,next);
			}
		}
		else if(attackselection==SelectionType.Beam){
			IntPair curr=validtargets.get(0);
			//Only need to redraw if the beam isn't already pointing right
			if(curr.getX()<=x){
				validtargets.clear();
				for(int i=x+1;i<=Math.min(x+attackrange,Constants.BATTLEFIELD_WIDTH-1);i++){
					validtargets.add(new IntPair(i,y));
				}
			}
		}
		else if(attackselection==SelectionType.Cone){
			IntPair curr=validtargets.get(0);
			if(curr.getX()<=x){
				validtargets.clear();
				for(int i=1;i<=attackrange;i++){
					for(int j=1-i;j<=i-1;j++){
						IntPair newpair=new IntPair(x+i,y+j);
						if(BattleEngine.isWithinBounds(newpair))
							validtargets.add(newpair);
					}
				}
			}
		}
		else if(attackselection==SelectionType.Area){
			IntPair next=epicenter.shiftRight();
			if(epicenter.getX()<Constants.BATTLEFIELD_WIDTH-1&&next.distanceFrom(active)<=attackrange) {
				epicenter=next;
				validtargets.clear();
				validtargets.addAll(getSquareGrid(epicenter,1));
			}
		}
		displayAttackRange();
	}

	public static void moveAttackRangeUp(){
		removeAttackRange();
		IntPair active=activeunit.getCoordinates();
		int x=activeunit.getCoordinates().getX();
		int y=activeunit.getCoordinates().getY();
		if(attackselection==SelectionType.Single){
			IntPair curr=validtargets.get(0);
			if(curr.getY()>0){
				IntPair next=new IntPair(curr.getX(),curr.getY()-1);
				if(active.distanceFrom(next)<=attackrange)
					validtargets.set(0,next);
			}
		}
		else if(attackselection==SelectionType.Beam){
			IntPair curr=validtargets.get(0);
			//Only need to redraw if the beam isn't already pointing up
			if(curr.getY()>=y){
				validtargets.clear();
				for(int i=Math.max(y-attackrange,0);i<y;i++){
					validtargets.add(new IntPair(x,i));
				}
			}
		}
		else if(attackselection==SelectionType.Cone){
			IntPair curr=validtargets.get(0);
			if(curr.getY()>=y){
				validtargets.clear();
				for(int i=1;i<=attackrange;i++){
					for(int j=1-i;j<=i-1;j++){
						IntPair newpair=new IntPair(x+j,y-i);
						if(BattleEngine.isWithinBounds(newpair))
							validtargets.add(newpair);
					}
				}
			}
		}
		else if(attackselection==SelectionType.Area){
			IntPair next=epicenter.shiftUp();
			if(epicenter.getY()>0&&next.distanceFrom(active)<=attackrange) {
				epicenter=next;
				validtargets.clear();
				validtargets.addAll(getSquareGrid(epicenter,1));
			}
		}
		displayAttackRange();
	}

	public static void moveAttackRangeDown(){
		removeAttackRange();
		IntPair active=activeunit.getCoordinates();
		int x=activeunit.getCoordinates().getX();
		int y=activeunit.getCoordinates().getY();
		if(attackselection==SelectionType.Single){
			IntPair curr=validtargets.get(0);
			if(curr.getY()<battlefield.length-1){
				IntPair next=new IntPair(curr.getX(),curr.getY()+1);
				if(active.distanceFrom(next)<=attackrange)
					validtargets.set(0,next);
			}
		}
		else if(attackselection==SelectionType.Beam){
			IntPair curr=validtargets.get(0);
			//Only need to redraw if the beam isn't already pointing down
			if(curr.getY()<=y){
				validtargets.clear();
				for(int i=y+1;i<=Math.min(y+attackrange,Constants.BATTLEFIELD_HEIGHT-1);i++){
					validtargets.add(new IntPair(x,i));
				}
			}
		}
		else if(attackselection==SelectionType.Cone){
			IntPair curr=validtargets.get(0);
			if(curr.getY()<=y){
				validtargets.clear();
				for(int i=1;i<=attackrange;i++){
					for(int j=1-i;j<=i-1;j++){
						IntPair newpair=new IntPair(x+j,y+i);
						if(BattleEngine.isWithinBounds(newpair))
							validtargets.add(newpair);
					}
				}
			}
		}
		else if(attackselection==SelectionType.Area){
			IntPair next=epicenter.shiftDown();
			if(epicenter.getY()<Constants.BATTLEFIELD_HEIGHT-1&&next.distanceFrom(active)<=attackrange) {
				epicenter=next;
				validtargets.clear();
				validtargets.addAll(getSquareGrid(epicenter,1));
			}
		}
		displayAttackRange();
	}

	public static void confirmAttackRange(boolean cancellable){
		GlobalEngine.giveUpControl();
		setTargetUnits();
		removeAttackRange();
		String name=GameData.getMoveName(currattack.getNum());
		if(name.equals("Sketch")){
			Pokemon userpokemon=activeunit.getPokemon();
			int lastmove=targetunits.get(0).getPrevMove();
			String lastname=GameData.getMoveName(lastmove);
			if(lastname.equals("Sketch")||lastname.equals("Struggle")||lastname.equals("Transform")||lastname.equals("Snore")||lastname.equals("Sleep Talk")
					||lastname.equals("Mimic")||lastname.equals("Mirror Move")||lastname.equals("Explosion")||lastname.equals("Self Destruct")
					||userpokemon.knowsMove(lastmove)){
				debug(lastname+" cannot be learned with Sketch.");
				afterAttackEffects(cancellable);
			}
			else{
				debug(userpokemon.getName()+" learning "+lastname);
				if(!userpokemon.learnMove(new Move(lastmove))){
					debug(userpokemon.getName()+" wants to learn "+lastname+". Needs to replace a move");
					MoveMenu mm=new MoveMenu(userpokemon,MoveMenuMode.SKETCH);
					MenuEngine.initialize(mm);
				}
			}
		}
		else if(name.equals("Teleport")){
			IntPair destination=validtargets.get(0);
			if(BattleEngine.canMoveTo(activeunit,destination,true)){
				debug(activeunit.getName()+" uses to teleport to move to the square at "+destination.toString());
				BattleEngine.moveOffSquare(activeunit,activeunit.getCoordinates().getX(),activeunit.getCoordinates().getY());
				BattleEngine.moveOnSquare(activeunit,destination.getX(),destination.getY());
			}
			else
				debug(activeunit.getName()+" cannot be placed on that square");
			afterAttackEffects(cancellable);
		}
		else if(name.equals("Spikes")){
			BattleEngine.removeSpikes();
			for(IntPair pair:validtargets){
				battlefield[pair.getX()][pair.getY()].setSpikes();
			}
			afterAttackEffects(cancellable);
		}
		else if(name.equals("Metronome")){
			int newmove;
			String newname;
			do{
				newmove=GameData.getRandom().nextInt(Constants.NUM_MOVES)+1;
				newname=GameData.getMoveName(newmove);
			}while(newname.equals("Sketch")||newname.equals("Struggle")||newname.equals("Metronome")||newname.equals("Transform")||newname.equals("Snore")
					||newname.equals("Sleep Talk")||newname.equals("Mimic")||newname.equals("Mirror Move")||activeunit.getPokemon().knowsMove(newmove));
			activeunit.setPrevMove(currattack.getNum());
			useMove(new Move(newmove),false);
		}
		else if(name.equals("Mirror Move")){
			Unit target=targetunits.get(0);
			int lastmove=target.getPrevMove();
			if(lastmove==-1){
				debug(target.getName()+" has no used a move yet. There's nothing to copy.");
				afterAttackEffects(cancellable);
			}
			else{
				String newname=GameData.getMoveName(lastmove);
				if(newname.equals("Sketch")||newname.equals("Struggle")||newname.equals("Metronome")||newname.equals("Transform")||newname.equals("Snore")
						||newname.equals("Sleep Talk")||newname.equals("Mimic")||newname.equals("Mirror Move")||activeunit.getPokemon().knowsMove(lastmove)){
					debug(newname+" cannot be copied with Mirror Move.");
					afterAttackEffects(cancellable);
				}
				else{
					activeunit.setPrevMove(currattack.getNum());
					useMove(new Move(lastmove),false);
				}
			}
		}
		else{
			MoveLogic.implementEffects(activeunit,targetunits,currattack);
			afterAttackEffects(cancellable);
		}
	}
	
	private static void setTargetUnits() {
		targetunits=new ArrayList<Unit>();
		for(IntPair pair:validtargets){
			Square s=battlefield[pair.getX()][pair.getY()];
			Unit u=BattleEngine.getUnitByID(s.getUnit());
			if(u!=null)
				targetunits.add(u);
		}
	}

	public static void cancelAttackRange(){
		removeAttackRange();
		debug(activeunit.getName()+" has cancelled the attack option");
		MenuEngine.initialize(new UnitMenu(activeunit));
	}

	public static void afterAttackEffects(boolean cancellable){
		activeunit.setHasAttacked(true);
		activeunit.setHasTakenAction(true);
		if(cancellable)
			activeunit.setPrevMove(currattack.getNum());
		//confusion turn count is only lowered by attacking turns. Pokemon can't avoid confusion by refusing to attack until it's over
		if(activeunit.getPokemon().getPcondition()==PermCondition.Confusion)
			activeunit.incNumTurnsAfflicted(PermCondition.Confusion.toString());
		BattleEngine.experienceGainLogic(targetunits);
		BattleEngine.openUnitMenu();
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
		debug(activeunit.getName()+" hits itself for "+damage+" damage");
		return damage;
	}
	
	public static void delete(){
		attackselection=null;
		epicenter=null;
		validtargets=null;
		currattack=null;
		activeunit=null;
		battlefield=null;
	}
	
	private static void debug(String s) {
		if(DEBUG) {
			System.out.println(s);
		}
	}
}
