package Objects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import Engines.GlobalEngine;
import Engines.MenuEngine;
import Enums.Gender;
import Enums.MoveMenuMode;
import Enums.PermCondition;
import Enums.Stat;
import Enums.Type;
import Global.Constants;
import Global.GameData;
import Global.PlayerData;
import Menus.MoveMenu;

public class Pokemon {
	private int num;
	private String name;
	private int[] ivs;
	private int[] evs;
	private int[] stats;
	private int currhp;
	private PermCondition pcondition;
	private int level;
	private int exp;
	private boolean wild;
	private int heldid;
	private Type helditemboosttype;
	private ArrayList<Move> moveset;
	private int happiness;
	private boolean fainted;
	private Gender gender;

	/**
	 * Constructor for wild pokemon with no set moveset.
	 * @param num
	 * @param ivs
	 * @param level
	 * @param wild
	 */
	public Pokemon(int num,int level){
		this(num,level,GameData.getMovesetByLevel(num,level),-1,true);
	}

	/**
	 * Constructor for wild pokemon with set moveset.
	 * @param num
	 * @param level
	 * @param moveset
	 */
	public Pokemon(int num,int level, ArrayList<Move> moveset){
		this(num,level,moveset,-1,true);
	}
	
	/**
	 * Constructor for trainer pokemon with no set moveset
	 * @param num
	 * @param level
	 * @param moveset
	 * @param trainerclass-determines the ivs for the team
	 * -1=Wild
	 * 1=Most trainers
	 * 3=Gym trainers
	 * 7=Gym leaders
	 * 10=Elite 4
	 */
	public Pokemon(int num,int level,int trainerclass){
		this(num,level,GameData.getMovesetByLevel(num,level),trainerclass,false);
	}

	/**
	 * Constructor for trainer pokemon with set moveset
	 * @param num
	 * @param level
	 * @param moveset
	 * @param trainerclass-determines the ivs for the team
	 * -1=Wild
	 * 1=Most trainers
	 * 3=Gym trainers
	 * 7=Gym leaders
	 * 10=Elite 4
	 */
	public Pokemon(int num,int level, ArrayList<Move> moveset,int trainerclass){
		this(num,level,moveset,trainerclass,false);
	}

	/**
	 * Private constructor called by helper constructors above
	 * @param num
	 * @param level
	 * @param mvoeset
	 * @param trainerclass
	 * @param wild
	 * @param gender
	 */
	private Pokemon(int num,int level,ArrayList<Move> moveset,int trainerclass,boolean wild){
		this.num=num;
		name=GameData.getName(num);
		ivs=Pokemon.generateIVs(trainerclass);
		evs=new int[6];
		this.level=level;
		stats=calculateStats();
		currhp=stats[0];
		pcondition=null;
		exp=0;
		this.wild=wild;
		this.heldid=-1;
		helditemboosttype=null;
		this.moveset=moveset;
		happiness=70;
		fainted=false;
		gender=Pokemon.generateGender(num);
	}
	
	/**
	 * Constructor for evolving a pokemon, preserving its
	 * characteristics
	 * @param prevolution
	 */
	public Pokemon(Pokemon base,int newnum){
		name=base.name;
		num=newnum;
		ivs=base.ivs;
		evs=base.evs;
		level=base.level;
		stats=calculateStats();
		currhp=base.currhp;
		exp=base.exp;
		wild=false;
		heldid=base.heldid;
		helditemboosttype=base.helditemboosttype;
		pcondition=base.pcondition;
		moveset=base.moveset;
		fainted=base.fainted;
		happiness=base.happiness;
		gender=base.gender;
	}
	
	/**
	 * Constructor for loading a saved Pokemon
	 * @param num
	 * @param ivs
	 * @param evs
	 * @param stats
	 * @param currhp
	 * @param pcondition
	 * @param level
	 * @param exp
	 * @param wild
	 * @param heldid
	 * @param helditemboosttype
	 * @param moveset
	 * @param happiness
	 * @param fainted
	 * @param gender
	 */
	public Pokemon(int num, String name, int[] ivs, int[] evs, int[] stats, int currhp,
			PermCondition pcondition, int level, int exp, boolean wild,
			int heldid, Type helditemboosttype, ArrayList<Move> moveset, int happiness,
			boolean fainted, Gender gender) {
		this.num = num;
		this.name = name;
		this.ivs = ivs;
		this.evs = evs;
		this.stats = stats;
		this.currhp = currhp;
		this.pcondition = pcondition;
		this.level = level;
		this.exp = exp;
		this.wild = wild;
		this.heldid = heldid;
		this.helditemboosttype = helditemboosttype;
		this.moveset = moveset;
		this.happiness = happiness;
		this.fainted = fainted;
		this.gender=gender;
	}

	private static int[] generateIVs(int trainerclass){
		int[] newivs=new int[6];
		for(int i=0;i<newivs.length;i++){
			if(trainerclass==-1)
				newivs[i]=GameData.getRandom().nextInt(16);
			else
				newivs[i]=trainerclass;
		}
		return newivs;
	}
	
	private static Gender generateGender(int num){
		//nidoran(m),nidorino,nidoking,hitmonlee,hitmonchan,tauros,hitmontop
		if(num==32||num==33||num==34||num==106||num==107||num==128||num==237)
			return Gender.Male;
		//nidoran(f),nidorina,nidoqueen,chansey,kangaskhan,jynx,miltank,blissey
		if(num==29||num==30||num==31||num==113||num==115||num==124||num==241||num==242)
			return Gender.Female;
		//magnemite,magneton,voltorb,electrode,staryu,starmie,porygon,porygon2
		if(num==81||num==812||num==100||num==101||num==120||num==121||num==137||num==233)
			return Gender.Genderless;
		int rand=GameData.getRandom().nextInt(2);
		if(rand==0)
			return Gender.Male;
		return Gender.Female;
	}

	private int[] calculateStats(){
		int[] basestats=GameData.getBaseStats(num);
		int[] newstats=new int[6];
		double hp1=(basestats[0]+ivs[0])*2;
		double hp2=Math.floor(Math.ceil(Math.sqrt((double)evs[0]))/4);
		hp1=(hp1+hp2)*level/100;
		newstats[0]=(int)Math.floor(hp1)+level+10;
		for(int i=1;i<6;i++){
			double stat1=(basestats[i]+ivs[i])*2;
			double stat2=Math.floor(Math.ceil(Math.sqrt((double)evs[i]))/4);
			stat1=(stat1+stat2)*level/100;
			newstats[i]=(int)Math.floor(stat1)+5;
		}
		return newstats;
	}

	
	/**
	 * The specific name of this pokemon. For the player's pokemon this is either given by the player or defaults to the generic name of the pokemon. For all other pokemon,
	 * it defaults to the generic name.
	 * @return
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Returns the pokemon number
	 * @return
	 */
	public int getNum(){
		return num;
	}
	
	/**
	 * Attempts to increase the stat by 2560 evs. If evs are at or above 25600, it will fail. If increase would make ev >25600, it will set them
	 * to 25600 instead.
	 * @param statstr
	 */
	public boolean addEVsFromVitamin(Stat stat){
		int index=-1;
		if(stat==Stat.HP)
			index=0;
		else if(stat==Stat.Attack)
			index=1;
		else if(stat==Stat.Defense)
			index=2;
		else if(stat==Stat.SpecialAttack)
			index=3;
		else if(stat==Stat.SpecialDefense)
			index=4;
		else if(stat==Stat.Speed)
			index=5;
		int ev=evs[index];
		if(ev>=25600)
			return false;
		ev+=2560;
		if(ev>25600)
			ev=25600;
		evs[index]=ev;
		if(level==100)
			calculateStats();
		return true;
	}
	
	/**
	 * When a pokemon defeats another, it gets evs equal to the defeated pokemon's base stats
	 * @param pokenum
	 */
	public void addEVsFromFaint(int pokenum){
		int[] additions=GameData.getBaseStats(pokenum);
		for(int i=0;i<6;i++){
			int ev=evs[i];
			ev+=additions[i];
			if(ev>65535)
				ev=65535;
			evs[i]=ev;
		}
		if(level==100)
			calculateStats();
	}
	
	/**
	 * Returns the active value for the given stat
	 * @param stat

	 */
	public int getStat(Stat stat){
		int index=-1;
		if(stat==Stat.HP)
			index=0;
		else if(stat==Stat.Attack)
			index=1;
		else if(stat==Stat.Defense)
			index=2;
		else if(stat==Stat.SpecialAttack)
			index=3;
		else if(stat==Stat.SpecialDefense)
			index=4;
		else if(stat==Stat.Speed)
			index=5;
		return stats[index];
	}
	
	public int getCurrHP(){
		return currhp;
	}
	
	/**
	 * Adds the specified value to the current HP. If using a Potion, this number is 20. If healing at a pokecenter, this would be getStat(0).
	 * If the addition would make the current HP greater than full, it becomes full instead.
	 * @param delta
	 * @return: true if added, false if the current HP is already at it's full value.
	 */
	public boolean incHP(int delta){
		if(currhp==stats[0])
			return false;
		currhp+=delta;
		if(currhp>stats[0])
			currhp=stats[0];
		return true;
	}
	
	/**
	 * Removes the specified value from the current HP. If hit by a move, this is the damage done. If hit by a 1-hit KO move, this would be getCurrHP().
	 * If the reduction would make the current HP less than 0, it becomes 0 instead. If hp is reduced to 0 from this method, the pokemon will faint
	 * @param delta
	 * @return: true if reduced, false if the current HP is alread at 0.
	 */
	public boolean decHP(int delta){
		if(currhp==0)
			return false;
		currhp-=delta;
		if(currhp<0)
			currhp=0;
		if(currhp==0)
			faint();
		return true;
	}
	
	/**
	 * Sets the current hp to the max
	 */
	public void restoreHP(){
		currhp=stats[0];
	}

	public PermCondition getPcondition() {
		return pcondition;
	}
	
	public void removePcondition(){
		pcondition=null;
	}
	
	public int getExp(){
		return exp;
	}
	
	public int[] getStats(){
		return stats;
	}
	
	public int[] getIVs(){
		return ivs;
	}
	
	public int[] getEVs(){
		return evs;
	}
	
	/**
	 * Attempts to alleviate the given non-volatile condition. If the pokemon doesn't have that condition, method fails
	 * @param condition
	 * @return
	 */
	public boolean removePcondition(PermCondition condition){
		if(pcondition!=condition)
			return false;
		pcondition=null;
		return true;
	}

	/**
	 * Attempts to inflict the pokemon with the specified non-volatile condition. If it already has one, this will fail
	 * @param pcondition: a non-volatile condition
	 * @return: false if pokemon already has a non-volatile status condition, true otherwise
	 */
	public boolean setPcondition(PermCondition condition) {
		if(condition==null){
			pcondition = condition;
			return true;
		}
		return false;
	}
	
	public int getLevel(){
		return level;
	}
	
	/**
	 * Pass in true if the pokemon is leveling from an exp gain, false if from a rare candy. Levels the pokemon up, increasing the level, 
	 * recalculating stats. If fromexp=false, then exp will be set to the exact amount required to attin the current level.
	 * @param fromexp
	 */
	public void levelUp(boolean fromexp){
		level++;
		calculateStats();
		if(!fromexp){
			exp=GameData.getExpThreshold(num,level);
		}
		if(GameData.getEvolutionConditions(num).contains(""+level)){
			if(!GlobalEngine.evolve(this,""+level))
				checkForMoveLearn();
		}
		else
			checkForMoveLearn();
	}
	
	public void checkForMoveLearn(){
		ArrayList<Integer> movesToLearn=GameData.getMovesLearnedByLevel(num,level);
		if(movesToLearn!=null){
			for(int movenum:movesToLearn){
				if(learnMove(new Move(movenum)))
					System.out.println(name+": learned "+GameData.getMoveName(movenum));
				else{
					System.out.println(name+" wants to learn "+GameData.getMoveName(movenum)+". Needs to replace a move");
					MoveMenu mm=new MoveMenu(this,MoveMenuMode.TMHM);
					MenuEngine.initialize(mm);
				}
			}
		}
	}
	
	/**
	 * Given the amount of experience gained, adds that much experience to the pokemon, returning the number of levels gained as a result.
	 * If pokemon is level 100, it will no longer actually gain experience
	 * @param expgain
	 * @return: the number of levels gained as a result of this experience gain
	 */
	public int gainExp(int expgain){
		if(level==100)
			return 0;
		exp+=expgain;
		int levelsgained=0;
		while(exp>=GameData.getExpThreshold(num,level)){
			levelsgained++;
			levelUp(true);
		}
		return levelsgained;
	}
	
	/**
	 * Returns whether or not the pokemon is wild, for use in determining capturability
	 * @return
	 */
	public boolean isWild(){
		return wild;
	}
	
	/**
	 * Set the pokemon as caught, ie no longer considered wild
	 */
	public void hasBeenCaught(){
		wild=false;
	}

	public int getHeldID() {
		return heldid;
	}
	
	public boolean isHolding(String item){
		if(heldid==-1)
			return false;
		return GameData.getItemName(heldid).equals(item);
	}
	
	public void evolve(int evolutionnum){
		System.out.println(getName()+" evolved into a "+GameData.getName(evolutionnum));
		num=evolutionnum;
		checkForMoveLearn();
		//TODO: Might need to remove this otherwise could end up in awkward scenario where Zubat Evolves into Golbat, triggering enough happiness to immediately
		//evolve into Crobat. 
		incHappiness(Constants.HAPPINESS_GAINED_ON_EVOLUTION);
	}
	
	/**
	 * Removes the currently held item if one is present, and sets helditemboosttype to null
	 */
	public void removeHeldItem(){
		heldid=-1;
		helditemboosttype=null;
	}
	
	/**
	 * Attempts to have pokemon hold new item of the specified id. If pokemon is already holding an item, will fail.
	 * If it succeeds, it will set heldid to the new itemid and set helditemboosttype if applicable
	 * @param itemid
	 * @return:false if pokemon is already holding an item, true otherwise
	 */
	public boolean holdItem(int itemid){
		if(heldid>=0)
			return false;
		heldid=itemid;
		if(GameData.isHeldItem(heldid)){
			String descrip=GameData.getItemDescription(itemid);
			String stype=descrip.substring(descrip.indexOf("holder's ")+9,descrip.indexOf(" type"));
			helditemboosttype=Type.valueOf(stype);
		}
		return true;
	}
	
	public Type getHeldItemBoostType(){
		return helditemboosttype;
	}
	
	public ArrayList<Move> getMoveSet(){
		return moveset;
	}
	
	public Gender getGender(){
		return gender;
	}
	
	/**
	 * Adds the given move the pokemon's moveset. If the pokemon already knows 4 moves, this method will fail. In that, case, replace() should be called
	 * with the index of the move to be replaced with the new move
	 * @param newmove
	 * @return: false if the pokemon already knows 4 moves, true otherwise
	 */
	public boolean learnMove(Move newmove){
		if(moveset.size()==4)
			return false;
		else
			moveset.add(newmove);
		return true;
	}
	
	/**
	 * Replaces the pokemon's move in the given index with the specified move.
	 * @param indexToReplace
	 * @param newmove
	 */
	public void replaceMove(int indexToReplace,Move newmove){
		moveset.set(indexToReplace,newmove);
	}
	
	public Move getMove(int movenum){
		for(Move m:moveset){
			if(m.getNum()==movenum)
				return m;
		}
		return null;
	}
	
	public int getHappiness(){
		return happiness;
	}
	
	/**
	 * Increases happiness by the specified amount. If happiness is increased beyond cap of 255, it is set to 255 instead
	 * @param delta
	 */
	public void incHappiness(int delta){
		happiness+=delta;
		if(happiness>Constants.MAX_HAPPINESS)
			happiness=Constants.MAX_HAPPINESS;
		if(happiness>=Constants.HAPPINESS_EVOLUTION_THRESHOLD&&GameData.getEvolutionConditions(num).contains("Happiness"))
			GlobalEngine.evolve(this,"Happiness");
	}

	public boolean isFainted() {
		return fainted;
	}

	/**
	 * Pokemon is set to fainted status. Will lose happiness as a result (happiness cannot go below 0)
	 * @param fainted
	 */
	public void faint() {
		this.fainted=true;
		happiness-=Constants.HAPPINESS_LOST_ON_FAINT;
		if(happiness<0)
			happiness=0;
	}
	
	public void revive(){
		this.fainted=false;
	}
	
	public String toString(){
		String s="Pokemon: ";
		s+=name+" "+num+" "+currhp+" "+level+" "+exp+" "+heldid+" "+happiness+" ";
		s+=wild+" "+fainted+" ";
		s+=pcondition+" "+helditemboosttype+" "+gender+" ";
		s+=moveset+"__";
		s+=Arrays.toString(ivs)+"__";
		s+=Arrays.toString(evs)+"__";
		s+=Arrays.toString(stats);
		return s;
	}
	
	public boolean equals(Pokemon other){
		return this.toString().equals(other.toString());
			
	}
	
	public static Pokemon readInPokemon(String line){
		Scanner s=new Scanner(line);
		s.next();
		String name;
		int num;
		String temp=s.next();
		if(Character.isDigit(temp.charAt(0))){
			num=Integer.parseInt(temp);
			name=GameData.getName(num);
		}
		else{
			name=temp;
			num=s.nextInt();
		}
		int currhp=s.nextInt();
		int level=s.nextInt();
		int exp=s.nextInt();
		int heldid=s.nextInt();
		int happiness=s.nextInt();
		boolean wild=s.nextBoolean();
		boolean fainted=s.nextBoolean();
		String type=s.next();
		PermCondition pcondition=null;
		Type helditemboosttype=null;
		Gender gender=null;
		if(!type.equals("null"))
			pcondition=PermCondition.valueOf(type);
		type=s.next();
		if(!type.equals("null"))
			helditemboosttype=Type.valueOf(type);
		type=s.next();
		if(!type.equals("null"))
			gender=Gender.valueOf(type);
		String[] arrays=s.nextLine().split("__");
		ArrayList<Move> moves=new ArrayList<Move>();
		String[] movearray=arrays[0].substring(2,arrays[0].length()-1).split(",");
		for(int i=0;i<movearray.length;i++){
			if(!movearray[i].trim().equals("null")){
				Scanner s2=new Scanner(movearray[i]);
				moves.add(new Move(s2.nextInt(),s2.nextInt(),s2.nextInt()));
				s2.close();
			}
		}
		int[] ivs=GameData.readIntArray(arrays[1]);
		int[] evs=GameData.readIntArray(arrays[2]);
		int[] stats=GameData.readIntArray(arrays[3]);
		s.close();
		return new Pokemon(num,name,ivs,evs,stats,currhp,pcondition,level,exp,wild,heldid,helditemboosttype,moves,happiness,fainted,gender);
	}
	
}
