package Objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import Engines.BattleEngine;
import Enums.BondCondition;
import Enums.Direction;
import Enums.PermCondition;
import Enums.ProtectionCondition;
import Enums.Stat;
import Enums.TempCondition;
import Enums.Type;
import Global.Constants;
import Global.GameData;
import Global.PlayerData;
import acm.graphics.GImage;

public class Unit {
	private int id;
	private Pokemon pokemon;
	private int pokemonpartyindex;
	private int[] modstages;
	private int movementrange;
	//list of perm and temp conditions and number of turns that status has been active
	private Map<String,Integer> conditions;
	//list of bondconditions and the recipient of the condition
	private Map<BondCondition,Integer> bondconditions;
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
	private int prevmove;
	private ArrayList<Integer> attackedby;
	private boolean isdigging;
	private boolean isflying;
	private boolean hasattacked;
	private int disabledmove;
	private GImage image;
	private IntPair coordinates;
	private boolean isminimized;
	private boolean israging;
	private int consecuses;
	private boolean ischarging;


	public Unit(Pokemon pokemon,int partyindex,int id){
		this.pokemon=pokemon;
		this.id=id;
		pokemonpartyindex=partyindex;
		modstages=new int[8];
		calculateMovementRange();
		conditions=new HashMap<String,Integer>();
		if(pokemon.getPcondition()!=null)
			addPermCondition(pokemon.getPcondition());
		bondconditions=new HashMap<BondCondition,Integer>();
		protectionconditions=new HashMap<ProtectionCondition,Integer>();
		controllable=true;
		directionfacing=Direction.Left;
		hasmoved=false;
		hastakenaction=false;
		types=GameData.getTypes(pokemon.getNum());
		hasendedturn=false;
		canmove=true;
		canattack=true;
		prevmove=-1;
		attackedby=new ArrayList<Integer>();
		isdigging=false;
		isflying=false;
		hasattacked=false;
		disabledmove=-1;
		coordinates=new IntPair(-1,-1);
		isminimized=false;
		israging=false;
		consecuses=0;
		ischarging=false;
		image=new GImage(Constants.PATH+"\\Sprites\\Left\\"+pokemon.getNum()+".png");
	}

	public Unit(Pokemon p,int[] modstag,int moverange,HashMap<String,Integer> conds,HashMap<BondCondition,Integer> bondconds,int id,
			HashMap<ProtectionCondition,Integer> proconds,boolean control,Direction dir,boolean moved,boolean acted,ArrayList<Type> types,
			boolean ended,boolean move,boolean attack,int prev,ArrayList<Integer> attby,boolean dig,boolean fly,boolean attacked,int dis,
			IntPair coord,boolean min,boolean raging,int consec,boolean charging){
		pokemon=p;
		this.id=id;
		modstages=modstag;
		movementrange=moverange;
		conditions=conds;
		bondconditions=bondconds;
		protectionconditions=proconds;
		controllable=control;
		directionfacing=dir;
		hasmoved=moved;
		hastakenaction=acted;
		this.types=types;
		hasendedturn=ended;
		canmove=move;
		canattack=attack;
		prevmove=prev;
		attackedby=attby;
		isdigging=dig;
		isflying=fly;
		hasattacked=attacked;
		disabledmove=dis;
		coordinates=coord;
		isminimized=min;
		israging=raging;
		consecuses=consec;
		ischarging=charging;
		image=new GImage(Constants.PATH+"\\Sprites\\Left\\"+pokemon.getNum()+".png");
		if(min)
			image.setSize(image.getWidth()*Constants.MINIMIZE_RATIO, image.getHeight()*Constants.MINIMIZE_RATIO);
	}

	public void copyStatMods(Unit other){
		for(int i=0;i<modstages.length;i++){
			modstages[i]=other.modstages[i];
		}
	}

	public boolean isCharging(){
		return ischarging;
	}

	public void setCharging(boolean newval){
		ischarging=newval;
	}

	public int getNumConsecUses(){
		return consecuses;
	}

	public void incNumConsecUses(){
		consecuses++;
	}

	public void resetConsecUses(){
		consecuses=0;
	}

	public boolean isRaging(){
		return israging;
	}

	public void setRaging(boolean newval){
		israging=newval;
	}

	public boolean isMinimized(){
		return isminimized;
	}

	public void setMinimized(boolean newval){
		isminimized=newval;
	}

	public GImage getImage(){
		return image;
	}

	public IntPair getCoordinates(){
		return coordinates;
	}

	public void setCoordinates(int x,int y){
		coordinates=new IntPair(x,y); 
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

	public void setDisabledMove(int movenum){
		disabledmove=movenum;
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

	public boolean hasProtectionCondition(ProtectionCondition condition){
		return protectionconditions.containsKey(condition);
	}

	public boolean removeProtectionCondition(ProtectionCondition condition){
		return protectionconditions.remove(condition)!=null;
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
	public boolean addBondCondition(BondCondition bond, int recipientid){
		if(bondconditions.get(bond)!=null)
			return false;
		bondconditions.put(bond,recipientid);
		return true;
	}

	public int hasBond(BondCondition bond){
		if(bondconditions.containsKey(bond))
			return bondconditions.get(bond);
		return -1;
	}

	public boolean removeBondCondition(BondCondition bond){
		return bondconditions.remove(bond)!=null;
	}

	public void clearBondConditions(){
		bondconditions.clear();
	}

	public boolean attackedBy(int unitid){
		return attackedby.contains(unitid);
	}

	public void wasAttackedBy(int unitid){
		if(!attackedBy(unitid))
			attackedby.add(unitid);
	}

	public int getPrevMove(){
		return prevmove;
	}

	public void setPrevMove(int movenum){
		prevmove=movenum;
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
		if(stage>6){
			stage=6;
			System.out.println(pokemon.getName()+" has hit maximum value for critical ratio");
		}
		modstages[7]=stage;
		return true;
	}

	public boolean decCritRatio(int stages){
		int stage=modstages[7];
		if(stage==0)
			return false;
		stage-=stages;
		if(stage<0){
			stage=0;
			System.out.println(pokemon.getName()+" has hit minimum value for critical ratio");
		}
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
		if(stage>6){
			stage=6;
			System.out.println(pokemon.getName()+" has hit maximum value for "+stat.toString());
		}
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
		if(stage<-6){
			stage=-6;
			System.out.println(pokemon.getName()+" has hit minimum value for "+stat.toString());
		}
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
	 * the innate stats. e.g. if a (de)buff move has been used on a unit, it will impact the functional stat in that battle
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
			index=0;
		else if(stat==Stat.Defense)
			index=1;
		else if(stat==Stat.SpecialAttack)
			index=2;
		else if(stat==Stat.SpecialDefense)
			index=3;
		else if(stat==Stat.Speed)
			index=4;
		if(index<5&&index>=0)
			return (int)(GameData.getStatStageMultiplier(modstages[index])*pokemon.getStats()[index+1]);
		else return -1;
	}

	public void clearStatMods(){
		modstages=new int[8];
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
		return conditions.containsKey(condition.toString());
	}

	public void removeconditions(){
		conditions.clear();
	}

	/**
	 * Attempts to alleviate the given volatile condition. If the pokemon doesn't have that condition, method fails
	 * @param condition
	 * @return
	 */
	public boolean removeTempCondition(TempCondition condition){
		return conditions.remove(condition.toString())!=null;
	}

	/**
	 * Attempts to inflict the given nonvolatile status condition on the unit. If it already has a perm condition, the method fails
	 * @param newcondition
	 * @return: false if unit already has the condition, true otherwise
	 */
	public boolean addPermCondition(PermCondition newcondition){
		if((newcondition==PermCondition.Burn&&types.contains(Type.Fire))||(newcondition==PermCondition.Frozen&&types.contains(Type.Ice))
				||(newcondition==PermCondition.Paralysis&&types.contains(Type.Electric))||(newcondition==PermCondition.Poison&&types.contains(Type.Poison))
				||(newcondition==PermCondition.BadlyPoison&&types.contains(Type.Poison)))
			return false;
		if(!pokemon.setPcondition(newcondition))
			return false;
		conditions.put(newcondition.toString(),0);
		return true;
	}

	public boolean hasPermCondition(PermCondition condition){
		return conditions.containsKey(condition.toString());
	}

	/**
	 * Attempts to alleviate the given nonvolatile condition. If the pokemon doesn't have that condition, method fails
	 * @param condition
	 * @return
	 */
	public boolean removePermCondition(PermCondition condition){
		if(!hasPermCondition(condition))
			return false;
		conditions.remove(condition.toString());
		pokemon.removePcondition();
		return true;
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
		if(directionfacing!=newdirection){
			BattleEngine.moveOffSquare(this,coordinates.getX(),coordinates.getY());
			directionfacing=newdirection;
			image=new GImage(Constants.PATH+"\\Sprites\\"+newdirection.toString()+"\\"+pokemon.getNum()+".png");
			BattleEngine.moveOnSquare(this,coordinates.getX(),coordinates.getY());
		}
	}
	
	public void changeDirectionFacing(){
		Direction newdirection=Direction.Left;
		if(directionfacing==Direction.Left)
			newdirection=Direction.Right;
		setDirectionFacing(newdirection);
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
		if(type==null)
			return false;
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

	public void setTypes(ArrayList<Type> types){
		this.types.clear();
		this.types.add(types.get(0));
		this.types.add(types.get(1));
	}

	public ArrayList<Type> getTypes(){
		return types;
	}

	public void damage(int damage){
		pokemon.decHP(damage);
	}

	public int getID(){
		return id;
	}

	public boolean isFlanking(Unit other){
		if((other.directionfacing==Direction.Left&&coordinates.getX()>other.coordinates.getX())
				||(other.directionfacing==Direction.Right&&coordinates.getX()<other.coordinates.getX())){
			int ydiff=Math.abs(coordinates.getY()-other.coordinates.getY());
			int xdiff=Math.abs(coordinates.getX()-other.coordinates.getX());
			return ydiff<=xdiff;
		}
		return false;
	}

	public boolean isFacing(Unit other){
		if((other.directionfacing==Direction.Left&&coordinates.getX()<other.coordinates.getX())
				||(other.directionfacing==Direction.Right&&coordinates.getX()>other.coordinates.getX())){
		int ydiff=Math.abs(coordinates.getY()-other.coordinates.getY());
		int xdiff=Math.abs(coordinates.getX()-other.coordinates.getX());
		return ydiff<=xdiff-1;
	}
	return false;
	}

	public boolean equals(Unit other){
		return id==other.id;
	}
	
	public void delete(){
		this.pokemon=null;
		modstages=null;
		conditions=null;
		bondconditions=null;
		protectionconditions=null;
		directionfacing=null;
		types=null;
		image=null;
	}

	public String toString(){
		String s="Unit: ";
		s+=id+"\n";
		s+=pokemonpartyindex+" "+movementrange+" "+controllable+" "+directionfacing+" "+hasmoved+" "+hastakenaction+" "+hasendedturn+" "+canmove+" "
				+canattack+" "+isdigging+" "+isflying+" "+hasattacked+" "+isminimized+" "+israging+" "+consecuses+" "
				+prevmove+" "+disabledmove+" "+ischarging+"\n";
		s+=coordinates.toString()+"\n";
		s+=Arrays.toString(modstages)+"\n";
		s+=conditions.toString()+"\n";
		s+=bondconditions.toString()+"\n";
		s+=protectionconditions.toString()+"\n";
		s+=types.toString()+"\n";
		s+=attackedby.toString();
		return s;
	}

	public static Unit readInUnit(Trainer owner, Scanner reader){
		int id=reader.nextInt();
		reader.nextLine();
		int partyindex=reader.nextInt();
		Pokemon p;
		if(owner==null)
			p=PlayerData.getParty().get(partyindex);
		else
			p=owner.getParty()[partyindex];
		int moverange=reader.nextInt();
		boolean control=reader.nextBoolean();
		Direction dir=Direction.valueOf(reader.next());
		boolean moved=reader.nextBoolean();
		boolean acted=reader.nextBoolean();
		boolean ended=reader.nextBoolean();
		boolean move=reader.nextBoolean();
		boolean attack=reader.nextBoolean();
		boolean dig=reader.nextBoolean();
		boolean fly=reader.nextBoolean();
		boolean attacked=reader.nextBoolean();
		boolean ismin=reader.nextBoolean();
		boolean raging=reader.nextBoolean();
		int consec=reader.nextInt();
		int prev=reader.nextInt();
		int dis=reader.nextInt();
		boolean charging=reader.nextBoolean();
		reader.nextLine();
		IntPair coord=IntPair.readIn(reader.nextLine());
		int[] modstag=GameData.readIntArray(reader.nextLine());
		HashMap<String,Integer> conds=new HashMap<String,Integer>();
		String line=reader.nextLine();
		String[] entries;
		if(line.length()>2){
			entries=line.substring(1,line.length()-1).split(", ");
			for(String entry:entries){
				String[] split=entry.split("=");
				conds.put(split[0],Integer.parseInt(split[1]));
			}
		}
		HashMap<BondCondition,Integer> bondconds=new HashMap<BondCondition,Integer>();
		line=reader.nextLine();
		if(line.length()>2){
			entries=line.substring(1,line.length()-1).split(", ");
			for(String entry:entries){
				String[] split=entry.split("=");
				bondconds.put(BondCondition.valueOf(split[0]),Integer.parseInt(split[1]));
			}
		}
		HashMap<ProtectionCondition,Integer> proconds=new HashMap<ProtectionCondition,Integer>();
		line=reader.nextLine();
		if(line.length()>2){
			entries=line.substring(1,line.length()-1).split(", ");
			for(String entry:entries){
				String[] split=entry.split("=");
				proconds.put(ProtectionCondition.valueOf(split[0]),Integer.parseInt(split[1]));
			}
		}
		line=reader.nextLine();
		ArrayList<Type> types=new ArrayList<Type>();
		String[] split=line.substring(1,line.length()-1).split(",");
		for(int i=0;i<split.length;i++){
			if(!split[i].trim().equals("null"))
				types.add(Type.valueOf(split[i].trim()));
		}
		//line=reader.nextLine();
		ArrayList<Integer> attby=new ArrayList<Integer>();
		int[] attackbylist=GameData.readIntArray(reader.nextLine());
		if(attackbylist!=null){
			for(int x:attackbylist)
				attby.add(x);
		}
		return new Unit(p, modstag, moverange, conds, bondconds, id, proconds, control, dir, moved, acted, types, ended,
				move, attack, prev, attby, dig, fly, attacked, dis, coord,ismin,raging,consec,charging);
	}

}
