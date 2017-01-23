package Objects;

import java.util.ArrayList;

import Engines.BattleEngine;
import Engines.GlobalEngine;
import Engines.MenuEngine;
import Enums.Direction;
import Enums.Stat;
import Global.Constants;
import KeyListeners.BattleMovementKeyListener;
import Menus.UnitMenu;

public class BattleMovementLogic {

	private static ArrayList<IntPair> validdestinations;
	private static ArrayList<IntPair> previousmoves;
	private static Unit activeunit;
	private static Square[][] battlefield;
	
	
	
	
	public static void move(Square[][] thisbattlefield){
		battlefield=thisbattlefield;
		activeunit=BattleEngine.getActiveUnit();
		previousmoves=new ArrayList<IntPair>();
		previousmoves.add(activeunit.getCoordinates());
		displayMovementRange();
		BattleEngine.takeControl(new BattleMovementKeyListener());
	}

	private static void displayMovementRange(){
		validdestinations=new ArrayList<IntPair>();
		int range=activeunit.getMovementRange()-previousmoves.size()+1;
		int xcoordinate=activeunit.getCoordinates().getX();
		int ycoordinate=activeunit.getCoordinates().getY();
		validdestinations.add(activeunit.getCoordinates());
		for(int x=xcoordinate-range;x<=xcoordinate+range;x++){
			for(int y=ycoordinate-range;y<=ycoordinate+range;y++){
				if(y>=Constants.BATTLEFIELD_HEIGHT)
					break;
				if(x>=0&&y>=0&&x<Constants.BATTLEFIELD_WIDTH
						&&Math.abs(x-xcoordinate)+Math.abs(y-ycoordinate)<=range&&BattleEngine.canMoveTo(activeunit,battlefield[x][y]))
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


	public static void moveLeft(){
		int x=activeunit.getCoordinates().getX();
		int y=activeunit.getCoordinates().getY();
		IntPair newcoordinates=new IntPair(x-1,y);
		if(previousmoves.size()==activeunit.getMovementRange()+1&&previousmoves.get(previousmoves.size()-2).equals(newcoordinates)
				||(x>0&&battlefield[x-1][y].isValid())){
			BattleEngine.moveOffSquare(activeunit,x,y);
			BattleEngine.moveOnSquare(activeunit,x-1,y);
			activeunit.setDirectionFacing(Direction.Left);
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
			BattleEngine.moveOffSquare(activeunit,x,y);
			BattleEngine.moveOnSquare(activeunit,x+1,y);
			activeunit.setDirectionFacing(Direction.Right);
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
			BattleEngine.moveOffSquare(activeunit,x,y);
			BattleEngine.moveOnSquare(activeunit,x,y-1);
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
			BattleEngine.moveOffSquare(activeunit,x,y);
			BattleEngine.moveOnSquare(activeunit,x,y+1);
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
		GlobalEngine.giveUpControl();
		for(IntPair pair:validdestinations){
			battlefield[pair.getX()][pair.getY()].markNeutral();
		}
		activeunit.setHasMoved(true);
		System.out.println(activeunit.getPokemon().getName()+" has moved "+(previousmoves.size()-1)+" squares.");
		if(BattleEngine.areSpikesPlaced()){
			int spikescount=0;
			for(IntPair pair:previousmoves){
				if(battlefield[pair.getX()][pair.getY()].hasSpikes())
					spikescount++;
			}
			int damage=BattleEngine.round(spikescount*activeunit.getPokemon().getStat(Stat.HP)*Constants.SPIKES_DAMAGE_RATE);
			System.out.println(activeunit.getPokemon().getName()+" takes "+damage+" damage from spikes");
			activeunit.getPokemon().decHP(damage);
		}
		if(!activeunit.getPokemon().isFainted())
			MenuEngine.initialize(new UnitMenu(activeunit));
		else
			BattleEngine.endTurn();
	}

	public static void cancelMovement(){
		for(IntPair pair:validdestinations){
			battlefield[pair.getX()][pair.getY()].markNeutral();
		}
		System.out.println(activeunit.getPokemon().getName()+" has cancelled the move option");
		MenuEngine.initialize(new UnitMenu(activeunit));
	}
	
	public static void delete(){
		validdestinations=null;
		previousmoves=null;
		activeunit=null;
		battlefield=null;
	}
}
