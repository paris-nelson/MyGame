package Engines;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import Enums.Direction;
import Global.Constants;
import Global.GameData;
import KeyListeners.UnitPlacementKeyListener;
import Objects.Unit;
import acm.graphics.GLabel;
import acm.graphics.GRect;

public class UnitPlacementEngine {

	private static ArrayList<Unit> punits;
	private static Unit[] tempunits;
	private static int yval;
	private static int ymin;
	private static int ymax;
	private static int unitindex;
	private static boolean selected;
	private static GRect button;
	private static GLabel buttonlabel;

	public static void initialize(ArrayList<Unit> units){
		//up/down move through various valid squares
		//enter while unselected selects the highlighted square
		//left/right cycles through various units in party or no unit
		//enter while selected commits the current unit (or no unit) to the selected square and unselects
		//once all units selected or player presses submit button off to the side
		//player will be prompted to confirm configuration. once this is done, battle begins and first turn is taken
		punits=units;
		tempunits=new Unit[units.size()];
		ymin=Constants.BATTLEFIELD_HEIGHT/2-units.size()/2;
		ymax=ymin+units.size()-1;
		yval=ymin;
		unitindex=-1;
		selected=false;
		initButton();
		addFocus();
		takeControl();
	}
	
	private static void initButton(){
		button=new GRect(780,Constants.TILE_SIZE*5+10,200,Constants.TILE_SIZE);
		button.setFilled(true);
		button.setFillColor(Color.WHITE);
		buttonlabel=new GLabel("Submit Placements");
		buttonlabel.setFont(new Font(Constants.FONT,Font.PLAIN,23));
		GUI gui=GameData.getGUI();
		gui.add(button);
		gui.add(buttonlabel,button.getX()+(button.getWidth()-buttonlabel.getWidth())/2,button.getY()+10+(button.getHeight()-buttonlabel.getHeight()));
	}

	public static void moveUp(){
		if(!selected&&yval>ymin){
			removeFocus();
			yval--;
			addFocus();
		}
	}

	public static void moveDown(){
		if(!selected&&yval<ymax){
			removeFocus();
			yval++;
			addFocus();
		}
	}

	public static void cycleLeft(){
		if(selected){
			int yindex=yval-ymin;
			if(tempunits[yindex]!=null){
				BattleEngine.moveOffSquare(tempunits[yindex],Constants.BATTLEFIELD_WIDTH-1,yval);
				punits.add(tempunits[yindex]);
				tempunits[yindex]=null;
			}
			if(unitindex>-1){
				BattleEngine.moveOffSquare(punits.get(unitindex),Constants.BATTLEFIELD_WIDTH-1,yval);
				unitindex--;
				if(unitindex>-1)
					BattleEngine.moveOnSquare(punits.get(unitindex),Constants.BATTLEFIELD_WIDTH-1,yval);
			}
			else{
				unitindex=punits.size()-1;
				BattleEngine.moveOnSquare(punits.get(unitindex),Constants.BATTLEFIELD_WIDTH-1,yval);
			}
		}
		else if(button.getFillColor()==Color.CYAN){
			button.setFillColor(Color.WHITE);
			addFocus();
		}
	}

	public static void cycleRight(){
		if(selected){
			int yindex=yval-ymin;
			if(tempunits[yindex]!=null){
				BattleEngine.moveOffSquare(tempunits[yindex],Constants.BATTLEFIELD_WIDTH-1,yval);
				punits.add(tempunits[yindex]);
				tempunits[yindex]=null;
			}
			if(unitindex<punits.size()-1){
				if(unitindex>-1)
					BattleEngine.moveOffSquare(punits.get(unitindex),Constants.BATTLEFIELD_WIDTH-1,yval);
				unitindex++;
				BattleEngine.moveOnSquare(punits.get(unitindex),Constants.BATTLEFIELD_WIDTH-1,yval);
			}
			else{
				BattleEngine.moveOffSquare(punits.get(unitindex),Constants.BATTLEFIELD_WIDTH-1,yval);
				unitindex=-1;
			}
		}
		else if(button.getFillColor()==Color.WHITE){
			button.setFillColor(Color.CYAN);
			removeFocus();
		}
	}

	public static void select(){
		if(!selected){
			if(button.getFillColor()==Color.CYAN)
				close();
			else
				selected=true;
		}
		else{
			int yindex=yval-ymin;
			if(unitindex>=0){
				tempunits[yindex]=punits.get(unitindex);
				punits.remove(unitindex);
			}
			else{
				if(tempunits[yindex]!=null)
					punits.add(tempunits[yindex]);
				tempunits[yindex]=null;
			}
			selected=false;
			moveDown();
			unitindex=-1;
			if(punits.size()==0)
				close();
		}
	}

	private static void addFocus(){
		BattleEngine.markGreen(Constants.BATTLEFIELD_WIDTH-1,yval);
	}

	private static void removeFocus(){
		BattleEngine.markBlack(Constants.BATTLEFIELD_WIDTH-1,yval);
	}

	public static boolean isSelected(){
		return selected;
	}

	public static void takeControl(){
		GameData.getGUI().giveControl(new UnitPlacementKeyListener());
	}

	public static void close(){
		ArrayList<Unit> newpunits=new ArrayList<Unit>();
		for(Unit u:tempunits){
			if(u!=null){
				u.setDirectionFacing(Direction.Left);
				newpunits.add(u);
			}
		}
		BattleEngine.continueInit(newpunits);
	}
}
