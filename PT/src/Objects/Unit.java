package Objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Enums.BondCondition;
import Enums.Direction;
import Enums.ProtectionCondition;
import Enums.Stat;
import Enums.TempCondition;
import Enums.Type;
import Global.Constants;
import Global.GameData;

public class Unit {
	private Pokemon pokemon;
	private int[] modstages;
	private int movementrange;
	//list of perm and temp conditions and number of turns that status has been active
	private Map<String,Integer> conditions;
	//list of bondconditions and the recipient of the condition
	private Map<BondCondition,Unit> bondconditions;
	//list of protectionconditions and the number of turns they have been active
	private Map<ProtectionCondition,Integer> protectionconditions;
	private boolean controllable;
	private Direction directionfacing;
	private boolean hasmoved;
	private boolean hastakenaction;
	private ArrayList<Type> types;//default to pokemon's types. Can only be changed by conversion 1 and 2
	private boolean hasendedturn;
	private boolean canmove;
	private boolean canattack;
	private Move prevmove;
	private ArrayList<Unit> attackedby;
	private boolean isdigging;
	private boolean isflying;
	private boolean hasattacked;
	private int disabledmove;
	
	
	public Unit(Pokemon pokemon){
		this.pokemon=pokemon;
		modstages=new int[8];
		calculateMovementRange();
		conditions=new HashMap<String,Integer>();
		bondconditions=new HashMap<BondCondition,Unit>();
		protectionconditions=new HashMap<ProtectionCondition,Integer>();
		controllable=true;
		directionfacing=Direction.Down;
		hasmoved=false;
		hastakenaction=false;
		types=GameData.getTypes(pokemon.getNum());
		hasendedturn=false;
		canmove=true;
		canattack=true;
		prevmove=null;
		attackedby=new ArrayList<Unit>();
		isdigging=false;
		isflying=false;
		hasattacked=false;
		disabledmove=-1;
	}
	
	public boolean hasAttacked(){
		return hasattacked;
	}
	
	public void setHasAttacked(boolean hasattacked){
		this.hasattacked=hasattacked;
	}
	
	public int getDisabledMove(){
		return disabledmove;
	}
	
	public void enableDisabledMove(){
		disabledmove=-1;
	}
	
	public void setDisabledMove(int index){
		disabledmove=index;
	}
	
	public void calculateMovementRange(){
		movementrange=Constants.MOVEMENT_RANGE_MIN+(getStat(Stat.Speed)/Constants.SPEED_PER_UNIT_MOVEMENT_RANGE);
	}
	
	public void setFlying(boolean isflying){
		this.isflying=isflying;
	}
	
	public void setDigging(boolean isdigging){
		this.isdigging=isdigging;
	}
	
	public boolean isFlying(){
		return isflying;
	}
	
	public boolean isDigging(){
		return isdigging;
	}
	
	/**
	 * If the unit already has this protection condition, this method will fail
	 * @param condition
	 */
	public boolean addProtectionCondition(ProtectionCondition condition){
		if(protectionconditions.get(condition)!=null)
			return false;
		protectionconditions.put(condition,0);
		return true;
	}
	
	public void incNumTurnsProtected(ProtectionCondition condition){
		protectionconditions.put(condition,protectionconditions.get(condition)+1);
	}
	
	public void resetNumTurnsProtected(ProtectionCondition condition){
		protectionconditions.put(condition, 0);
	}
	
	public int getNumTurnsProtected(ProtectionCondition condition){
		return protectionconditions.get(condition);
	}
	
	/**
	 * Will fail if unit already has that bond with a recipient
	 * @param bond
	 * @param recipient
	 * @return
	 */
	public boolean addBondCondition(BondCondition bond, Unit recipient){
		if(bondconditions.get(bond)!=null)
			return false;
		bondconditions.put(bond,recipient);
		return true;
	}
	
	public Unit hasBond(BondCondition bond){
		if(bondconditions.containsKey(bond))
			return bondconditions.get(bond);
		return null;
	}
	
	public void removeBondCondition(BondCondition bond){
		bondconditions.remove(bond);
	}
	
	public void clearBondConditions(){
		bondconditions.clear();
	}
	
	public boolean attackedBy(Unit unit){
		return attackedby.contains(unit);
	}
	
	public void wasAttackedBy(Unit unit){
		if(!attackedBy(unit))
			attackedby.add(unit);
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
	
	public int getNumTurnsAfflicted(String condition){
		return conditions.get(condition);
	}
	
	public void incNumTurnsAfflicted(String condition){
		conditions.put(condition,conditions.get(condition)+1);
	}
	
	public void resetNumTurnsAfflicted(String condition){
		conditions.put(condition,0);
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
		else if(stat==Stat.SpecialAttack)
			index=2;
		else if(stat==Stat.SpecialDefense)
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
		else if(stat==Stat.SpecialAttack)
			index=2;
		else if(stat==Stat.SpecialDefense)
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
		else if(stat==Stat.SpecialAttack)
			index=3;
		else if(stat==Stat.SpecialDefense)
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
		if(conditions.containsKey(newcondition))
			return false;
		else if((newcondition==TempCondition.Trap&&hasTempCondition(TempCondition.DamageTrap))||(newcondition==TempCondition.DamageTrap&&hasTempCondition(TempCondition.Trap)))
			return false;
		conditions.put(newcondition.toString(),0);
		return true;
	}
	
	public boolean hasTempCondition(TempCondition condition){
		return conditions.containsKey(condition);
	}
	
	public void removeconditions(){
		conditions.clear();
	}
	
	/**
	 * Attempts to alleviate the given volatile condition. If the pokemon doesn't have that condition, method fails
	 * @param condition
	 * @return
	 */
	public boolean removeTcondition(TempCondition condition){
		return conditions.remove(condition)!=null;
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
