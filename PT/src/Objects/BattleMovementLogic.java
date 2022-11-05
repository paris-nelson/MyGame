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

	private static Unit activeunit;
	private static ArrayList<IntPair> route;
	private static Square[][] battlefield;
	private static int range;

	public static void move(Square[][] thisbattlefield) {
		battlefield = thisbattlefield;
		activeunit = BattleEngine.getActiveUnit();
		route = new ArrayList<IntPair>();
		route.add(activeunit.getCoordinates());
		range=activeunit.getMovementRange();
		BattleEngine.takeControl(new BattleMovementKeyListener());
	}

	private static void addToRoute(IntPair coords) {
		if(route.size()>1&&coords.equals(route.get(route.size()-2))) {
			// all we did was undo our last move
			removeLastFromRoute();
		}
		else {
			route.add(coords);
			battlefield[coords.getX()][coords.getY()].markGrayedOut();
		}
	}

	private static void removeLastFromRoute() {
		IntPair lastmove=route.remove(route.size()-1);
		battlefield[lastmove.getX()][lastmove.getY()].markNeutral();
	}

	private static boolean canMoveTo(IntPair coords) {
		int x=coords.getX();
		int y=coords.getY();
		if(x<0||x>Constants.BATTLEFIELD_WIDTH-1)
			return false;
		if(y<0||y>Constants.BATTLEFIELD_HEIGHT-1)
			return false;
		if(!BattleEngine.canMoveTo(activeunit, coords,false))
			return false;
		if(route.size()-1>=range) {
			if(!coords.equals(route.get(route.size()-2)))
				return false;
		}
		return true;
	}

	public static void moveLeft() {
		int x = activeunit.getCoordinates().getX();
		int y = activeunit.getCoordinates().getY();
		IntPair newCoords=new IntPair(x-1,y);
		if (canMoveTo(newCoords)) {
			BattleEngine.moveOffSquare(activeunit, x, y);
			BattleEngine.moveOnSquare(activeunit, x - 1, y);
			activeunit.setDirectionFacing(Direction.Left);
			addToRoute(newCoords);
		}
	}

	public static void moveRight() {
		int x = activeunit.getCoordinates().getX();
		int y = activeunit.getCoordinates().getY();
		IntPair newCoords=new IntPair(x+1,y);
		if (canMoveTo(newCoords)) {
			BattleEngine.moveOffSquare(activeunit, x, y);
			BattleEngine.moveOnSquare(activeunit, x + 1, y);
			activeunit.setDirectionFacing(Direction.Right);
			addToRoute(newCoords);
		}
	}

	public static void moveUp() {
		int x = activeunit.getCoordinates().getX();
		int y = activeunit.getCoordinates().getY();
		IntPair newCoords=new IntPair(x,y-1);
		if (canMoveTo(newCoords)) {
			BattleEngine.moveOffSquare(activeunit, x, y);
			BattleEngine.moveOnSquare(activeunit, x, y - 1);
			addToRoute(newCoords);
		}
	}

	public static void moveDown() {
		int x = activeunit.getCoordinates().getX();
		int y = activeunit.getCoordinates().getY();
		IntPair newCoords=new IntPair(x,y+1);
		if (canMoveTo(newCoords)) {
			BattleEngine.moveOffSquare(activeunit, x, y);
			BattleEngine.moveOnSquare(activeunit, x, y + 1);
			addToRoute(newCoords);
		}
	}

	public static void confirmMovement() {
		GlobalEngine.giveUpControl();
		if(route.size()>1) {
			if(!BattleEngine.canMoveTo(activeunit,activeunit.getCoordinates(),true)) {
				System.out.println(activeunit.getName()+" cannot end its movement on tile "+activeunit.getCoordinates());
			}
			for (IntPair pair : route) {
				battlefield[pair.getX()][pair.getY()].markNeutral();
			}
			activeunit.setHasMoved(true);
			System.out.println(activeunit.getName() + " has moved " + (route.size()-1) + " squares.");
			if (BattleEngine.areSpikesPlaced()&&!activeunit.isFlying()) {
				int spikescount=0;
				for(IntPair stop : route) {
					if (battlefield[stop.getX()][stop.getY()].hasSpikes()) {
						spikescount++;
					}
				}
				int damage = BattleEngine
						.round(spikescount*activeunit.getPokemon().getStat(Stat.HP)*Constants.SPIKES_DAMAGE_RATE);
				System.out.println(activeunit.getName() + " stepped on "+spikescount+" tiles with spikes.");
				activeunit.getPokemon().decHP(damage,"spikes");
			}
		}
		if (!activeunit.getPokemon().isFainted())
			MenuEngine.initialize(new UnitMenu(activeunit));
		else
			BattleEngine.endTurn();
	}

	public static void cancelMovement() {
		for (IntPair pair : route) {
			battlefield[pair.getX()][pair.getY()].markNeutral();
		}
		System.out.println(activeunit.getName() + " has cancelled the move option");
		MenuEngine.initialize(new UnitMenu(activeunit));
	}

	public static void delete() {
		route = null;
		activeunit = null;
		battlefield = null;
	}
}
