package Objects;

import Engines.BattleEngine;
import Engines.GUI;
import Engines.GlobalEngine;
import Engines.MenuEngine;
import Enums.PermCondition;
import Enums.Stat;
import Global.Constants;
import Global.GameData;
import Global.PlayerData;
import KeyListeners.CatchKeyListener;
import Menus.UnitMenu;

public class CatchLogic {
	private static Square[][] battlefield;
	private static Unit activeunit;
	private static int ballid;
	private static String ball;
	private static int range;
	private static IntPair target;

	public static void catchLogic(Square[][] thisbattlefield,int thisballid){
		battlefield=thisbattlefield;
		activeunit=BattleEngine.getActiveUnit();
		ballid=thisballid;
		ball=GameData.getItemName(ballid);
		range=getBallRange();
		useBall();
	}

	private static int getBallRange(){
		if(ball.equals("Pokeball"))
			return Constants.POKEBALL_RANGE;
		else if(ball.equals("Friend Ball"))
			return Constants.FRIENDBALL_RANGE;
		else if(ball.equals("Great Ball"))
			return Constants.GREATBALL_RANGE;
		else if(ball.equals("Ultra Ball"))
			return Constants.ULTRABALL_RANGE;
		else if(ball.equals("Master Ball"))
			return Constants.MASTERBALL_RANGE;
		return -1;
	}

	private static void useBall(){
		System.out.println(activeunit.getName()+" using "+ball);
		target=getDefaultCatchSelection();
		displayCatchRange();
		BattleEngine.takeControl(new CatchKeyListener());
	}

	private static IntPair getDefaultCatchSelection(){
		IntPair curr=activeunit.getCoordinates();
		if(curr.getX()>0)
			return new IntPair(curr.getX()-1,curr.getY());
		return new IntPair(curr.getX()+1,curr.getY());
	}

	private static void removeCatchRange(){
		battlefield[target.getX()][target.getY()].markNeutral();
	}

	private static void displayCatchRange(){
		battlefield[target.getX()][target.getY()].markValid();
	}

	public static void moveCatchRangeLeft(){
		removeCatchRange();
		if(target.getX()>0){
			IntPair next=new IntPair(target.getX()-1,target.getY());
			if(activeunit.getCoordinates().distanceFrom(next)<=range)
				target=next;
		}
		displayCatchRange();
	}

	public static void moveCatchRangeRight(){
		removeCatchRange();
		if(target.getX()<battlefield.length-1){
			IntPair next=new IntPair(target.getX()+1,target.getY());
			if(activeunit.getCoordinates().distanceFrom(next)<=range)
				target=next;
		}
		displayCatchRange();
	}

	public static void moveCatchRangeUp(){
		removeCatchRange();
		if(target.getY()>0){
			IntPair next=new IntPair(target.getX(),target.getY()-1);
			if(activeunit.getCoordinates().distanceFrom(next)<=range)
				target=next;
		}
		displayCatchRange();
	}

	public static void moveCatchRangeDown(){
		removeCatchRange();
		if(target.getY()<battlefield.length-1){
			IntPair next=new IntPair(target.getX(),target.getY()+1);
			if(activeunit.getCoordinates().distanceFrom(next)<=range)
				target=next;
		}
		displayCatchRange();
	}

	public static void confirmCatchTarget(){
		GlobalEngine.giveUpControl();
		removeCatchRange();
		Unit tocatch=BattleEngine.getUnitByID(battlefield[target.getX()][target.getY()].getUnit());
		Pokemon tocatchpoke=tocatch.getPokemon();
		if(!(BattleEngine.currOpponent() instanceof WildTrainer)){
			System.out.println("Cannot catch a trainer's pokemon!");
		}
		else if(doesCatchSucceed(tocatchpoke,getBallMultiplier())){
			System.out.println(tocatchpoke.getName()+" is caught!");
			BattleEngine.removeUnit(tocatch.getID());
			PlayerData.addNewPokemon(tocatchpoke);
		}
		else{
			System.out.println(tocatchpoke.getName()+" breaks free!");
		}
		GlobalEngine.useItemImpl(ballid,tocatchpoke);
	}
	
	private static double getBallMultiplier(){
		if(ball.equals("Pokeball"))
			return Constants.POKEBALL_MULTIPLIER;
		else if(ball.equals("Friend Ball"))
			return Constants.FRIENDBALL_MULTIPLIER;
		else if(ball.equals("Great Ball"))
			return Constants.GREATBALL_MULTIPLIER;
		else if(ball.equals("Ultra Ball"))
			return Constants.ULTRABALL_MULTIPLIER;
		else if(ball.equals("Master Ball"))
			return Constants.MASTERBALL_MULTIPLIER;
		return -1;
	}

	public static void cancelCatchRange(){
		removeCatchRange();
		System.out.println(activeunit.getName()+" has cancelled the catch attempt");
		MenuEngine.initialize(new UnitMenu(activeunit));
	}
	
	
	public static boolean doesCatchSucceed(Pokemon target, double ballbonus){
		if(ball.equals("Master Ball")){
			System.out.println("Master ball automatically succeeds");
			return true;
		}
		double a=calculateA(target,ballbonus);
		System.out.println("Catch Rate A: "+a);
		int rand=GameData.getRandom().nextInt(256);
		System.out.println("Random = "+rand);
		if(rand<a)
			return true;
		else{
			double b=calculateB(a);
			System.out.println("Catch Rate B: "+b);
			for(int i=0;i<3;i++){
				rand=GameData.getRandom().nextInt(65536);
				if(rand>=b)
					return false;
				System.out.println("Catch passes shake #"+(i+1));
			}
			return true;
		}
	}

	private static double calculateB(double a){
		return 1048560/(Math.sqrt((int)Math.sqrt(16711680/a)));
	}

	private static double calculateA(Pokemon targetp, double ballbonus){
		double a=3*targetp.getStat(Stat.HP)-2*targetp.getCurrHP();
		System.out.println("3 * "+targetp.getStat(Stat.HP)+" - 2 * "+targetp.getCurrHP()+" = "+a);
		a*=GameData.getCatchRate(targetp.getNum());
		System.out.println("a * "+GameData.getCatchRate(targetp.getNum())+" = "+a);
		a*=ballbonus;
		System.out.println("a * "+ballbonus+" = "+a);
		PermCondition condition=targetp.getPcondition();
		if(condition==PermCondition.Frozen||condition==PermCondition.Sleep){
			a*=2;
			System.out.println("a * 2 = "+a);
		}
		else if(condition==PermCondition.Burn||condition==PermCondition.Paralysis||condition==PermCondition.Poison||condition==PermCondition.BadlyPoison){
			a*=1.5;
			System.out.println("a * 1.5 = "+a);
		}
		a/=(3*targetp.getStat(Stat.HP));
		System.out.println("a / (3 * "+targetp.getStat(Stat.HP)+") = "+a);
		Unit tocatch=BattleEngine.getUnitByID(battlefield[target.getX()][target.getY()].getUnit());
		if(activeunit.isFlanking(tocatch))
			a*=1;
		else if(activeunit.isFacing(tocatch))
			a*=Constants.FRONT_FACING_ACCURACY_RATE;
		else
			a*=Constants.SIDE_FACING_ACCURACY_RATE;
		return a;
	}
	
	public static void delete(){
		battlefield=null;
		activeunit=null;
		ball=null;
		target=null;
	}
}
