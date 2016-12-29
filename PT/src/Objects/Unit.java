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
	//tracks number of turns the unit has been afflicted by confuse, sleep other status conditions that care how many turns
	private int numturnsafflicted;
	private boolean canmove;
	private boolean canattack;
	private Move prevmove;
	
	
	public Unit(Pokemon pokemon){
		this.pokemon=pokemon;
		modstages=new int[8];
		movementrange=Constants.MOVEMENT_RANGE_MIN+(pokemon.getStat(Stat.Speed)/Constants.SPEED_PER_UNIT_MOVEMENT_RANGE);
		tconditions=new ArrayList<TempCondition>();
		controllable=true;
		directionfacing=Direction.Down;
		hasmoved=false;
		hastakenaction=false;
		types=GameData.getTypes(pokemon.getNum());
		hasendedturn=false;
		numturnsafflicted=0;
		canmove=true;
		canattack=true;
		prevmove=null;
	}
	
	public Move getPrevMove(){
		return prevmove;
	}
	
	public void setPrevMove(Move m){
		prevmove=m;
	}
	
	public boolean canAttack(){
		return canattack;
	}
	
	public void setCanAttack(boolean newval){
		canattack=newval;
	}
	
	public boolean canMove(){
		return canmove;
	}
	
	public void setCanMove(boolean newval){
		canmove=newval;
	}
	
	public Pokemon getPokemon(){
		return pokemon;
	}
	
	public int getNumTurnsAfflicted(){
		return numturnsafflicted;
	}
	
	public void incNumTurnsAfflicted(){
		numturnsafflicted++;
	}
	
	public void resetNumTurnsAfflicted(){
		numturnsafflicted=0;
	}
	
	public boolean incCritRatio(int stages){
		int stage=modstages[7];
		if(stage==6)
			return false;
		stage+=stages;
		if(stage>6)
			stage=6;
		modstages[7]=stage;
		return true;
	}
	
	public boolean decCritRatio(int stages){
		int stage=modstages[7];
		if(stage==0)
			return false;
		stage-=stages;
		if(stage<0)
			stage=0;
		modstages[7]=stage;
		return true;
	}
	
	public int getCritRatio(){
		int stage=modstages[7];
		if(pokemon.isHolding("Scope Lens")&&modstages[7]<5)
			modstages[7]=stage+2;
		return GameData.getCritRatio(modstages[7]);
	}
	
	/**
	 *  Increases the given battle stat by the given number of stages. If stat would be increased beyond stage 6, it is set to stage 6 instead. If stat
	 *  cannot be further increased, the method fails
	 * @param stages
	 * @param stat
	 * @return: false if stat cannot be increased further, true otherwise
	 */
	public boolean incStat(int stages, Stat stat){
		int index=-1;
		if(stat==Stat.Attack)
			index=0;
		else if(stat==Stat.Defense)
			index=1;
		else if(stat==Stat.SpAttack)
			index=2;
		else if(stat==Stat.SpDefense)
			index=3;
		else if(stat==Stat.Speed)
			index=4;
		else if(stat==Stat.Accuracy)
			index=5;
		else if(stat==Stat.Evasion)
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
	public boolean decStat(int stages, Stat stat){
		int index=-1;
		if(stat==Stat.Attack)
			index=0;
		else if(stat==Stat.Defense)
			index=1;
		else if(stat==Stat.SpAttack)
			index=2;
		else if(stat==Stat.SpDefense)
			index=3;
		else if(stat==Stat.Speed)
			index=4;
		else if(stat==Stat.Accuracy)
			index=5;
		else if(stat==Stat.Evasion)
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
	
	public double getAccuracy(){
		return GameData.getAccEvaStageMultiplier(Stat.Accuracy,modstages[5]);
	}
	
	public double getEvasion(){
		return GameData.getAccEvaStageMultiplier(Stat.Evasion,modstages[6]);
	}
	/**
	 * Returns the functional stat of the unit. Note that these are the stats in this battle and not representative of
	 * the innate stats. e.g. pokemon.getStat(Stat.HP) gives the max hp, but unit.getStat(Stat.HP) gives current
	 * or if a (de)buff move has been used on a unit, it will impact the functional stat in that battle
	 * 
	 * This method should only be used for 6 primary stats, otherwise use getAccuracy, getEvasion, or getCritRatio
	 * @param stat
	 * @return
	 */
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
			return (int)(GameData.getStatStageMultiplier(modstages[index-1])*pokemon.getStats()[index]);
		else return -1;
	}
	
	public int getMovementRange(){
		return movementrange;
	}
	
	/**
	 * Attempts to inflict the given volatile status condition on the unit. If it already has that condition, the method fails
	 * If unit already has a trap condition, it cannot be caught in a damage trap and vice-versa
	 * @param newcondition
	 * @return: false if unit already has the condition, true otherwise
	 */
	public boolean addTempCondition(TempCondition newcondition){
		if(tconditions.contains(newcondition))
			return false;
		else if(newcondition==TempCondition.Confusion||newcondition==TempCondition.Encore||
				newcondition==TempCondition.Trap||newcondition==TempCondition.DamageTrap||newcondition==TempCondition.PerishSong){
			if(pokemon.getPcondition()!=null||tconditions.contains(TempCondition.Confusion)||
					tconditions.contains(TempCondition.Encore)||tconditions.contains(TempCondition.Trap)||
					tconditions.contains(TempCondition.DamageTrap)||tconditions.contains(TempCondition.PerishSong))
				return false;
			else{
				tconditions.add(newcondition);
				return true;
			}
		}
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
	
	public ArrayList<Type> getTypes(){
		return types;
	}
	
	public void damage(int damage){
		pokemon.decHP(damage);
	}
	
	
}
