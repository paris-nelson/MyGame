package Objects;

import java.util.ArrayList;
import java.util.HashMap;

import Engines.BattleEngine;
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
	private static Move currattack;
	private static Unit activeunit;
	private static Square[][] battlefield;

	
	public static void useMove(Square[][] thisbattlefield,Move move,boolean cancellable){
		battlefield=thisbattlefield;
		useMove(move,cancellable);
	}
	
	public static void useMove(Move move,boolean cancellable){
		activeunit=BattleEngine.getActiveUnit();
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
			displayAttackRange();
			BattleEngine.takeControl(new BattleAttackKeyListener(cancellable));
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

	public static void confirmAttackRange(boolean cancellable){
		ArrayList<Unit> targets=new ArrayList<Unit>();
		for(IntPair pair:validtargets){
			Square s=battlefield[pair.getX()][pair.getY()];
			Unit u=BattleEngine.getUnitByID(s.getUnit());
			if(u!=null)
				targets.add(u);
		}
		removeAttackRange();
		String name=GameData.getMoveName(currattack.getNum());
		if(name.equals("Sketch")){
			Pokemon userpokemon=activeunit.getPokemon();
			int lastmove=targets.get(0).getPrevMove();
			String lastname=GameData.getMoveName(lastmove);
			if(lastname.equals("Sketch")||lastname.equals("Struggle")||lastname.equals("Transform")||lastname.equals("Snore")||lastname.equals("Sleep Talk")
					||lastname.equals("Mimic")||lastname.equals("Mirror Move")||lastname.equals("Explosion")||lastname.equals("Self Destruct")
					||userpokemon.knowsMove(lastmove)){
				System.out.println(lastname+" cannot be learned with Sketch.");
				afterAttackEffects(cancellable);
			}
			else{
				System.out.println(userpokemon.getName()+" learning "+lastname);
				if(!userpokemon.learnMove(new Move(lastmove))){
					System.out.println(userpokemon.getName()+" wants to learn "+lastname+". Needs to replace a move");
					MoveMenu mm=new MoveMenu(userpokemon,MoveMenuMode.SKETCH);
					MenuEngine.initialize(mm);
				}
			}
		}
		else if(name.equals("Teleport")){
			IntPair destination=validtargets.get(0);
			if(BattleEngine.canMoveTo(battlefield[destination.getX()][destination.getY()])){
				System.out.println(activeunit.getPokemon().getName()+" uses to teleport to move to the square at "+destination.toString());
				BattleEngine.moveOffSquare(activeunit,activeunit.getCoordinates().getX(),activeunit.getCoordinates().getY());
				BattleEngine.moveOnSquare(activeunit,destination.getX(),destination.getY());
			}
			else
				System.out.println(activeunit.getPokemon().getName()+" cannot be placed on that square");
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
			Unit target=targets.get(0);
			int lastmove=target.getPrevMove();
			if(lastmove==-1){
				System.out.println(target.getPokemon().getName()+" has no used a move yet. There's nothing to copy.");
				afterAttackEffects(cancellable);
			}
			else{
				String newname=GameData.getMoveName(lastmove);
				if(newname.equals("Sketch")||newname.equals("Struggle")||newname.equals("Metronome")||newname.equals("Transform")||newname.equals("Snore")
						||newname.equals("Sleep Talk")||newname.equals("Mimic")||newname.equals("Mirror Move")||activeunit.getPokemon().knowsMove(lastmove)){
					System.out.println(newname+" cannot be copied with Mirror Move.");
					afterAttackEffects(cancellable);
				}
				else{
					activeunit.setPrevMove(currattack.getNum());
					useMove(new Move(lastmove),false);
				}
			}
		}
		else{
			MoveLogic.implementEffects(activeunit,targets,currattack);
			afterAttackEffects(cancellable);
		}
	}

	public static void cancelAttackRange(){
		removeAttackRange();
		System.out.println(activeunit.getPokemon().getName()+" has cancelled the attack option");
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
		BattleEngine.experienceGainLogic(validtargets);
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
		System.out.println(activeunit.getPokemon().getName()+" hits itself for "+damage+" damage");
		return damage;
	}
}
