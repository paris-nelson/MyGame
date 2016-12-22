package Objects;

import java.util.ArrayList;

import Enums.Direction;
import Enums.Stat;
import Enums.TempCondition;
import Enums.Type;
import Global.Constants;
import Global.GameData;

public class Unit {
	private Pokemon pokemon;
	private int[] modstages;
	private int movementrange;
	private ArrayList<TempCondition> tconditions;
	private boolean controllable;
	private Direction directionfacing;
	private boolean hasmoved;
	private boolean hastakenaction;
	private ArrayList<Type> types;//default to pokemon's types. Can only be changed by conversion 1 and 2
	private boolean hasendedturn;
	
	public Unit(Pokemon pokemon){
		this.pokemon=pokemon;
		modstages=new int[7];
		movementrange=Constants.MOVEMENT_RANGE_MIN+(pokemon.getStat(Stat.Speed)/Constants.SPEED_PER_UNIT_MOVEMENT_RANGE);
		tconditions=new ArrayList<TempCondition>();
		controllable=true;
		directionfacing=Direction.Down;
		hasmoved=false;
		hastakenaction=false;
		types=GameData.getPokeTypes(pokemon.getNum());
		hasendedturn=false;
	}
	
	public Pokemon getPokemon(){
		return pokemon;
	}
	
	/**
	 *  Increases the given battle stat by the given number of stages. If stat would be increased beyond stage 6, it is set to stage 6 instead. If stat
	 *  cannot be further increased, the method fails
	 * @param stages
	 * @param stat
	 * @return: false if stat cannot be increased further, true otherwise
	 */
	public boolean incStat(int stages, String statstr){
		int index=-1;
		if(statstr.equals("Attack"))
			index=0;
		else if(statstr.equals("Defense"))
			index=1;
		else if(statstr.equals("Special Attack"))
			index=2;
		else if(statstr.equals("Special Defense"))
			index=3;
		else if(statstr.equals("Speed"))
			index=4;
		else if(statstr.equals("Accuracy"))
			index=5;
		else if(statstr.equals("Evasion"))
			index=6;
		int stage=modstages[index];
		if(stage==6)
			return false;
		stage+=stages;
		if(stage>6)
			stage=6;
		modstages[index]=stage;
		return true;
	}
	
	/**
	 *  Decreases the given battle stat by the given number of stages. If stat would be decreased beyond stage -6, it is set to stage -6 instead. If stat
	 *  cannot be further decreased, the method fails
	 * @param stages
	 * @param stat
	 * @return: false if stat cannot be decreased further, true otherwise
	 */
	public boolean decStat(int stages, String statstr){
		int index=-1;
		if(statstr.equals("Attack"))
			index=0;
		else if(statstr.equals("Defense"))
			index=1;
		else if(statstr.equals("Special Attack"))
			index=2;
		else if(statstr.equals("Special Defense"))
			index=3;
		else if(statstr.equals("Speed"))
			index=4;
		else if(statstr.equals("Accuracy"))
			index=5;
		else if(statstr.equals("Evasion"))
			index=6;
		int stage=modstages[index];
		if(stage==-6)
			return false;
		stage-=stages;
		if(stage<-6)
			stage=-6;
		modstages[index]=stage;
		return true;
	}
	
	public int getStat(Stat stat){
		if(stat==Stat.HP)
			return pokemon.getStat(stat);
		int index=-1;
		if(stat==Stat.Attack)
			index=1;
		else if(stat==Stat.Defense)
			index=2;
		else if(stat==Stat.SpAttack)
			index=3;
		else if(stat==Stat.SpDefense)
			index=4;
		else if(stat==Stat.Speed)
			index=5;
		else if(stat==Stat.Accuracy)
			index=6;
		else if(stat==Stat.Evasion)
			index=7;
		if(index<6)
			return (int)(GameData.getStatStageMultiplier(modstages[index])*pokemon.getStats()[index]);
		else return -1; //TODO
	}
	
	public int getMovementRange(){
		return movementrange;
	}
	
	/**
	 * Attempts to inflict the given volatile status condition on the unit. If it already has that condition, the method fails
	 * @param newcondition
	 * @return: false if unit already has the condition, true otherwise
	 */
	public boolean addTempCondition(TempCondition newcondition){
		if(tconditions.contains(newcondition))
			return false;
		else
			tconditions.add(newcondition);
		return true;
	}
	
	public boolean hasTempCondition(TempCondition condition){
		return tconditions.contains(condition);
	}
	
	public void removeTconditions(){
		tconditions.clear();
	}
	
	/**
	 * Attempts to alleviate the given volatile condition. If the pokemon doesn't have that condition, method fails
	 * @param condition
	 * @return
	 */
	public boolean removeTcondition(TempCondition condition){
		return tconditions.remove(condition);
	}
	
	public boolean isControllable(){
		return controllable;
	}
	
	public void setControllable(boolean newval){
		controllable=newval;
	}
	
	public Direction getDirectionFacing(){
		return directionfacing;
	}
	
	public void setDirectionFacing(Direction newdirection){
		directionfacing=newdirection;
	}
	
	public boolean hasMoved(){
		return hasmoved;
	}
	
	public boolean hasTakenAction(){
		return hastakenaction;
	}
	
	public void setHasMoved(boolean newval){
		hasmoved=newval;
	}
	
	public void setHasTakenAction(boolean newval){
		hastakenaction=newval;
	}
	
	public boolean hasEndedTurn(){
		return hasendedturn;
	}
	
	public void setHasEndedTurn(boolean newval){
		hasendedturn=newval;
	}
	
	public boolean isType(Type type){
		return types.contains(type);
	}
	
	/**
	 * Overrides units types to become the current type. Currently only used for Conversion 1 and 2
	 * @param type
	 */
	public void setType(Type type){
		types.clear();
		types.add(type);
	}
	
	
}
