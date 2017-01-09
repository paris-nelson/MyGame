package Engines;

import java.awt.event.KeyListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import Enums.BondCondition;
import Enums.PermCondition;
import Enums.Stat;
import Enums.TempCondition;
import Enums.Tile;
import Enums.Type;
import Global.Constants;
import Global.GameData;
import Global.PlayerData;
import KeyListeners.BattleMovementKeyListener;
import Menus.BattlePlayerMenu;
import Menus.UnitMenu;
import Objects.BattlefieldMaker;
import Objects.IntPair;
import Objects.Move;
import Objects.Pokemon;
import Objects.ProceduralBattlefieldMaker;
import Objects.Square;
import Objects.Trainer;
import Objects.Unit;
import acm.graphics.GCompound;

public class BattleEngine {

	private static Trainer opponent;
	private static ArrayList<Unit> punits;
	private static ArrayList<Unit> ounits;
	private static ArrayList<Unit> allunits;
	private static ArrayList<Integer> priorities;
	private static int activeindex;
	private static Unit activeunit;
	private static boolean inbattle;
	private static GCompound battlefieldimage;
	private static Square[][] battlefield;
	private static BattlefieldMaker bfmaker;
	private static ArrayList<IntPair> validdestinations;
	private static ArrayList<IntPair> previousmoves;
	private static boolean spikesplaced;

	public static void initialize(Trainer newopponent){
		//TODO: Add save/load feature. Either add resume feature for picking back up mid battle
		//or add logic to this method to determine if this is a new battle or if mid battle
		//e.g. wipe save file at end of battle, initialize method uses save file to determine if new battle or not
		System.out.println("Initializing battle with "+newopponent.getName());
		inbattle=true;
		opponent=newopponent;
		bfmaker=new ProceduralBattlefieldMaker();
		spikesplaced=false;
		initBattlefield();
		initUnits();
		System.out.println(allunits.get(0).toString());
		Unit newunit=Unit.readInUnit(null,(allunits.get(0).toString()));
		System.out.println(newunit.toString());
		setPriorities();
		activeindex=0;
		activeunit=allunits.get(priorities.get(0));
		takeTurn();
	}

	private static void initBattlefield(){
		bfmaker.makeNew();
		battlefieldimage=bfmaker.getImage();
		battlefield=bfmaker.getResult();
		GameData.getGUI().add(battlefieldimage,10,10);  
	}

	private static void setPriorities(){
		priorities=new ArrayList<Integer>();
		for(int i=0;i<allunits.size();i++){
			priorities.add(i);
		}
		ArrayList<Integer> speeds=new ArrayList<Integer>();
		for(Unit u:allunits){
			u.calculateMovementRange();
			speeds.add(u.getStat(Stat.Speed));
		}
		for(int pointer=1;pointer<speeds.size();pointer++){
			int hole=pointer;
			while(hole>0&&speeds.get(hole)>speeds.get(hole-1)){
				int temp=speeds.get(hole-1);
				speeds.set(hole-1,speeds.get(hole));
				speeds.set(hole,temp);
				temp=priorities.get(hole-1);
				priorities.set(hole-1,priorities.get(hole));
				priorities.set(hole,temp);
				hole--;
			}
		}
		//if unit is holding quick claw, it has 20% chance to get first place in queue. This is evaluated from slowest to fastest. if multiple units are holding
		//claw and get the 20%, the faster will still be put first
		//removes priority links from queue and places in temp queue fastest to slowest
		ArrayList<Integer> clawholders=new ArrayList<Integer>();
		for(int i=0;i<priorities.size();i++){
			if(allunits.get(priorities.get(i)).getPokemon().isHolding("Quick Claw")&&GameData.getRandom().nextInt(100)<Constants.QUICK_CLAW_CHANCE){
				clawholders.add(priorities.remove(i));
				i--;
			}
		}
		//inserts from temp queue into front of priorities queue, slowest pushed in first and fastest put in last
		for(int i=clawholders.size()-1;i>=0;i--){
			System.out.println(allunits.get(clawholders.get(i)).getPokemon().getName()+" is currently first in queue thanks to quick claw");
			priorities.add(clawholders.get(i),0);
		}
	}

	private static void nextTurn(){
		System.out.println("Going to next turn");
		if(getNumLiveUnits(punits)==0||getNumLiveUnits(ounits)==0)
			battleOver();
		activeindex++;
		if(activeindex>=priorities.size()){
			setPriorities();
			activeindex=0;
		}
		activeunit=allunits.get(priorities.get(activeindex));
		if(activeunit.getPokemon().isFainted())
			nextTurn();
		else
			takeTurn();
	}

	private static void takeTurn(){
		activeunit.setHasMoved(false);
		activeunit.setHasTakenAction(false);
		activeunit.setHasEndedTurn(false);
		activeunit.setHasAttacked(false);
		startOfTurnActions();
		if(activeunit.getPokemon().isFainted())
			nextTurn();
		else
			openUnitMenu();
	}

	private static void startOfTurnActions(){
		Pokemon activepokemon=activeunit.getPokemon();
		System.out.println(activepokemon.getName()+" ("+activeindex+" in order) taking turn");
		PermCondition pcondition=activepokemon.getPcondition();
		if(pcondition==PermCondition.Burn||pcondition==PermCondition.Poison){
			System.out.print(activepokemon.getName()+" loses hp from "+pcondition.toString()+": from "
					+activepokemon.getCurrHP()+" ");
			activepokemon.decHP(round(Constants.BURN_POISON_HP_LOSS_RATE*activepokemon.getStat(Stat.HP)));
			System.out.println("to "+activepokemon.getCurrHP());
		}
		else if(pcondition==PermCondition.BadlyPoison){
			activeunit.incNumTurnsAfflicted(PermCondition.BadlyPoison.toString());
			System.out.print(activepokemon.getName()+" loses hp from "+pcondition.toString()+": from "
					+activepokemon.getCurrHP()+" ");
			activepokemon.decHP(round(activeunit.getNumTurnsAfflicted(PermCondition.BadlyPoison.toString())*Constants.BURN_POISON_HP_LOSS_RATE
					*activepokemon.getStat(Stat.HP)));
			System.out.println("to "+activepokemon.getCurrHP());
		}
		else if(pcondition==PermCondition.Frozen){
			int turns=activeunit.getNumTurnsAfflicted(PermCondition.Frozen.toString());
			//freeze can last 1-4 turns. 1/4 chance of ending after 1 turn, 1/3 after 2 turns, 1/2 after 3, 1/1 after 4. 
			if(turns>0&&GameData.getRandom().nextInt(100)<100/(Constants.FREEZE_MAX_TURNS+1-activeunit.getNumTurnsAfflicted(PermCondition.Frozen.toString()))){
				//thawed
				System.out.println(activepokemon.getName()+" is no longer frozen after "+turns+" turns.");
				activeunit.resetNumTurnsAfflicted(PermCondition.Frozen.toString());
				activepokemon.removePcondition(PermCondition.Frozen);
				activeunit.setCanMove(true);
				activeunit.setCanAttack(true);
			}
			else{
				System.out.println(activepokemon.getName()+" is still frozen after "+turns+" turns.");
				activeunit.incNumTurnsAfflicted(PermCondition.Frozen.toString());
				activeunit.setCanMove(false);
				activeunit.setCanAttack(false);
			}
		}
		else if(pcondition==PermCondition.Sleep){
			int turns=activeunit.getNumTurnsAfflicted(PermCondition.Sleep.toString());
			//sleep can last 1-4 turns. 1/4 chance of ending after 1 turn, 1/3 after 2 turns, 1/2 after 3, 1/1 after 4. 
			if(turns>0&&GameData.getRandom().nextInt(100)<100/(Constants.SLEEP_MAX_TURNS+1-activeunit.getNumTurnsAfflicted(PermCondition.Sleep.toString()))){
				//awaken
				System.out.println(activepokemon.getName()+" is no longer asleep after "+turns+" turns.");
				activeunit.resetNumTurnsAfflicted(PermCondition.Sleep.toString());
				activepokemon.removePcondition(PermCondition.Sleep);
				activeunit.setCanMove(true);
			}
			else{
				System.out.println(activepokemon.getName()+" is still asleep after "+turns+" turns.");
				activeunit.incNumTurnsAfflicted(PermCondition.Sleep.toString());
				activeunit.setCanMove(false);
			}
		}
		else if(activepokemon.getPcondition()==PermCondition.Paralysis){
			if(GameData.getRandom().nextInt(100)<Constants.PARALYSIS_INACTION_CHANCE){
				System.out.println(activepokemon.getName()+" is too paralyzed to do anything");
				activeunit.setCanAttack(false);
				activeunit.setCanMove(false);
			}
		}
		else if(activepokemon.getPcondition()==PermCondition.Confusion){
			int turns=activeunit.getNumTurnsAfflicted(PermCondition.Confusion.toString());
			//confusion can last 1-4 turns. 1/4 chance of ending after 1 turn, 1/3 after 2 turns, 1/2 after 3, 1/1 after 4. 
			if(turns>0&&GameData.getRandom().nextInt(100)<100/(Constants.CONFUSE_MAX_TURNS+1-activeunit.getNumTurnsAfflicted(PermCondition.Confusion.toString()))){
				//awaken
				System.out.println(activepokemon.getName()+" is no longer confused after "+turns+" turns.");
				activeunit.resetNumTurnsAfflicted(PermCondition.Confusion.toString());
				activepokemon.removePcondition(PermCondition.Confusion);
			}
			else{
				System.out.println(activepokemon.getName()+" is still confused after "+turns+" turns.");
			}
		}
		if(activeunit.hasTempCondition(TempCondition.Attract)){
			if(GameData.getRandom().nextInt(100)<Constants.ATTRACT_INACTION_CHANCE){
				System.out.println(activepokemon.getName()+" is too infatuated to attack");
				activeunit.setCanAttack(false);
			}
		}
		if(activeunit.hasTempCondition(TempCondition.Flinch)){
			System.out.println(activepokemon.getName()+" flinches");
			activeunit.setCanAttack(false);
		}
		if(activeunit.hasTempCondition(TempCondition.Trap)||activeunit.hasTempCondition(TempCondition.DamageTrap)){
			TempCondition temp=TempCondition.Trap;
			if(activeunit.hasTempCondition(TempCondition.DamageTrap))
				temp=TempCondition.DamageTrap;
			int turns=activeunit.getNumTurnsAfflicted(temp.toString());
			//trap can last 2-5 turns. 1/4 chance of ending after 2 turns, 1/3 after 3 turns, 1/2 after 4, 1/1 after 5. 
			if(turns>=Constants.TRAP_MIN_TURNS&&GameData.getRandom().nextInt(100)<100/(Constants.TRAP_MAX_TURNS+1-activeunit.getNumTurnsAfflicted(temp.toString()))){
				//break free
				System.out.println(activepokemon.getName()+" is no longer trapped after "+turns+" turns.");
				activeunit.resetNumTurnsAfflicted(temp.toString());
				activeunit.setCanMove(true);
				if(activeunit.hasTempCondition(TempCondition.Trap))
					activeunit.removeTcondition(TempCondition.Trap);
				else
					activeunit.removeTcondition(TempCondition.DamageTrap);
			}
			else{
				System.out.println(activepokemon.getName()+" is still trapped after "+turns+" turns.");
				activeunit.incNumTurnsAfflicted(temp.toString());
				activeunit.setCanMove(false);
				if(activeunit.hasTempCondition(TempCondition.DamageTrap)){
					int damage=round(activepokemon.getStat(Stat.HP)*Constants.TRAP_HP_LOSS_RATE);
					System.out.println(activepokemon.getName()+" takes "+damage+" damage");
					activeunit.damage(damage);
				}
			}
		}
		if(activeunit.hasTempCondition(TempCondition.Encore)){
			int turns=activeunit.getNumTurnsAfflicted(TempCondition.Encore.toString());
			//encore can last 2-5 turns. 1/4 chance of ending after 2 turns, 1/3 after 3 turns, 1/2 after 4, 1/1 after 5. 
			if(turns>=Constants.ENCORE_MIN_TURNS&&GameData.getRandom().nextInt(100)<100/(Constants.ENCORE_MAX_TURNS+1-activeunit.getNumTurnsAfflicted(TempCondition.Encore.toString()))){
				//break free
				System.out.println(activepokemon.getName()+" is no longer encored after "+turns+" turns.");
				activeunit.resetNumTurnsAfflicted(TempCondition.Encore.toString());
				activeunit.removeTcondition(TempCondition.Encore);
			}
			else{
				System.out.println(activepokemon.getName()+" is still encored after "+turns+" turns.");
				activeunit.incNumTurnsAfflicted(TempCondition.Encore.toString());
			}
		}
		if(activepokemon.isHolding("Leftovers")){
			int hp=round(activepokemon.getStat(Stat.HP)*Constants.LEFTOVERS_HP_RECOV_RATE);
			System.out.println(activepokemon.getName()+" recovers "+hp+"HP from leftovers");
			activepokemon.incHP(hp);
		}
		if(activeunit.hasTempCondition(TempCondition.Disable)){
			int turns=activeunit.getNumTurnsAfflicted(TempCondition.Disable.toString());
			//disable can last 2-5 turns. 1/4 chance of ending after 2 turns, 1/3 after 3 turns, 1/2 after 4, 1/1 after 5. 
			if(turns>=Constants.DISABLE_MIN_TURNS&&GameData.getRandom().nextInt(100)<100/(Constants.DISABLE_MAX_TURNS+1-activeunit.getNumTurnsAfflicted(TempCondition.Disable.toString()))){
				//disable ends
				System.out.println(activepokemon.getName()+" is no longer disabled after "+turns+" turns.");
				activeunit.resetNumTurnsAfflicted(TempCondition.Disable.toString());
				activeunit.removeTcondition(TempCondition.Disable);
				activeunit.enableDisabledMove();
			}
			else
				System.out.println(activepokemon.getName()+" is still disabled after "+turns+" turns.");
		}
	}

	private static void endOfTurnActions(){
		Pokemon activepokemon=activeunit.getPokemon();
		if(activeunit.hasTempCondition(TempCondition.Curse)){
			int damage=(int)(activepokemon.getStat(Stat.HP)*Constants.CURSE_NIGHTMARE_HP_LOSS_RATE);
			activepokemon.decHP(damage);
			System.out.println(activepokemon.getName()+" takes "+damage+" damage from their curse");
		}
		if(activeunit.hasTempCondition(TempCondition.Nightmare)){
			if(activepokemon.getPcondition()==PermCondition.Sleep){
				int damage=(int)(activepokemon.getStat(Stat.HP)*Constants.CURSE_NIGHTMARE_HP_LOSS_RATE);
				activepokemon.decHP(damage);
				System.out.println(activepokemon.getName()+" takes "+damage+" damage from their nightmare");
			}
			else{
				activeunit.removeTcondition(TempCondition.Nightmare);
				System.out.println(activepokemon.getName()+" woke up from their nightmare");
			}
		}
		int recipientid=activeunit.hasBond(BondCondition.LeechSeed);
		if(recipientid>0){
			Unit recipient=getUnitByID(recipientid);
			int damage=(int)(recipient.getPokemon().getStat(Stat.HP)*Constants.LEECH_SEED_HP_LOSS_RATE);
			recipient.getPokemon().decHP(damage);
			System.out.println(recipient.getPokemon().getName()+" takes "+damage+" damage from leech seed");
			activepokemon.incHP(damage);
			System.out.println(activepokemon.getName()+" gains "+damage+" hp from leech seed");
		}
		if(activeunit.hasTempCondition(TempCondition.PerishSong)){
			activeunit.incNumTurnsAfflicted(TempCondition.PerishSong.toString());
			System.out.println(activepokemon.getName()+" has "+(Constants.PERISH_SONG_TURNS-activeunit.getNumTurnsAfflicted(TempCondition.PerishSong.toString()))+" turns left of Perish Song".toString());
			if(activeunit.getNumTurnsAfflicted(TempCondition.PerishSong.toString())==Constants.PERISH_SONG_TURNS){
				activepokemon.decHP(activepokemon.getCurrHP());
			}
		}
		if(activeunit.hasTempCondition(TempCondition.Disable)){
			if(activeunit.hasAttacked())
				activeunit.incNumTurnsAfflicted(TempCondition.Disable.toString());
		}
	}
	
	private static Unit getUnitByID(int id){
		for(Unit u:allunits){
			if(u.getID()==id)
				return u;
		}
		return null;
	}

	public static void takeControl(KeyListener kl){
		GameData.getGUI().giveControl(kl);
	}

	public static void giveControl(){
		GameData.getGUI().giveControl(null);
	}

	public static void initUnits(){
		punits=new ArrayList<Unit>();
		ounits=new ArrayList<Unit>();
		allunits=new ArrayList<Unit>();
		Pokemon[] party=opponent.getParty();
		int idcounter=1;
		for(int i=0;i<party.length;i++){ 
			ounits.add(new Unit(party[i],i,idcounter));
			idcounter++;
		}
		ArrayList<Pokemon> party2=PlayerData.getParty();
		for(int i=0;i<party2.size();i++){
			punits.add(new Unit(party2.get(i),i,idcounter));
			idcounter++;
		}
		//insertion sort opp units by desc level
		for(int pointer=1;pointer<ounits.size();pointer++){
			int hole=pointer;
			while(hole>0&&ounits.get(hole).getPokemon().getLevel()>ounits.get(hole-1).getPokemon().getLevel()){
				Unit temp=ounits.get(hole-1);
				ounits.set(hole-1,ounits.get(hole));
				ounits.set(hole,temp);
				hole--;
			}
		}		
		allunits.addAll(punits);
		allunits.addAll(ounits);
		placeOppUnits();
		placePlayerUnits();
	}

	private static void placePlayerUnits(){
		//For now will auto place units the same way it auto places opp.
		//TODO: Decide if I want to allow player to place as many as they want in their own order
		switch(punits.size()){
		case 6:moveOnSquare(punits.get(5),Constants.BATTLEFIELD_WIDTH-1,Constants.BATTLEFIELD_HEIGHT/2-3);
		case 5:moveOnSquare(punits.get(4),Constants.BATTLEFIELD_WIDTH-1,Constants.BATTLEFIELD_HEIGHT/2+2);
		case 4:moveOnSquare(punits.get(3),Constants.BATTLEFIELD_WIDTH-1,Constants.BATTLEFIELD_HEIGHT/2-2);
		case 3:moveOnSquare(punits.get(2),Constants.BATTLEFIELD_WIDTH-1,Constants.BATTLEFIELD_HEIGHT/2+1);
		case 2:moveOnSquare(punits.get(1),Constants.BATTLEFIELD_WIDTH-1,Constants.BATTLEFIELD_HEIGHT/2-1);
		case 1:moveOnSquare(punits.get(0),Constants.BATTLEFIELD_WIDTH-1,Constants.BATTLEFIELD_HEIGHT/2);
		}
	}

	private static void placeOppUnits(){
		switch(ounits.size()){
		case 6:moveOnSquare(ounits.get(5),0,Constants.BATTLEFIELD_HEIGHT/2-3);
		case 5:moveOnSquare(ounits.get(4),0,Constants.BATTLEFIELD_HEIGHT/2+2);
		case 4:moveOnSquare(ounits.get(3),0,Constants.BATTLEFIELD_HEIGHT/2-2);
		case 3:moveOnSquare(ounits.get(2),0,Constants.BATTLEFIELD_HEIGHT/2+1);
		case 2:moveOnSquare(ounits.get(1),0,Constants.BATTLEFIELD_HEIGHT/2-1);
		case 1:moveOnSquare(ounits.get(0),0,Constants.BATTLEFIELD_HEIGHT/2);
		}
	}

	private static void moveOnSquare(Unit unit,int x,int y){
		unit.setCoordinates(x, y);
		battlefield[x][y].setUnit(unit.getID());
		battlefieldimage.add(unit.getImage(),x*Constants.TILE_SIZE,y*Constants.TILE_SIZE);
	}

	private static void moveOffSquare(Unit unit,int x,int y){
		battlefield[x][y].removeUnit();
		battlefieldimage.remove(unit.getImage());
	}

	/**
	 * Resumes a battle that was saved previously. Called on initialization if last player save was mid-battle.
	 */
	public static void load(){
		System.out.println("Initializing previous battle");
		inbattle=true;
		//opponent=newopponent; load opponent from file
		//bfmaker=new ProceduralBattlefieldMaker(); create new bfmaker that creates battlefield based on stored data
		//spikesplaced=false; store whether spikes have been placed
		//initUnits(); create new method for recreating allunits,punits,ounits, and priorities based on save file and place unit where they were
		//setPriorities(); priorities should already be set, no need for this
		//activeindex=0;
		//activeunit=allunits.get(priorities.get(0)); restore previous activeindex and activeunit.
		//takeTurn(); determine where in activeunits turn we are. mark hasmoved,hasattacked,hastakenaction accordingly and then open the unit menu.
		//calling takeTurn() will reset the vals and implement startofturnactions, which must already have been done on the activeunit for it
		//to have reached a point that it could save, so clearly that is not necessary here.
	}

	/**
	 * Saves all battle data to file to be loaded on initialization of program. Used when player saves mid battle.
	 */
	public static void save(){
		try{
			File f=new File(Constants.PATH+"\\InitializeData\\battlesavefile.txt");
			PrintWriter pw=new PrintWriter(f);

			pw.close();
		}catch(Exception e){e.printStackTrace();}
	}

	public static void close(){
		System.out.println("Ending battle");
		inbattle=false;
		File f=new File(Constants.PATH+"\\InitializeData\\battlesavefile.txt");
		if(f.exists())
			f.delete();
	}

	public static boolean isInBattle(){
		return inbattle;
	}

	public static Trainer currOpponent(){
		return opponent;
	}

	private static void battleOver(){
		System.out.println("Battle Over.");
		boolean playerwins=false;
		if(getNumLiveUnits(ounits)==0){
			playerwins=true;
		}
		//If somehow both player and opponent simultaneously have no pokemon (say Self-Destruct was used), player is considered to have lost.
		if(getNumLiveUnits(punits)==0){
			playerwins=false;
		}
		if(playerwins)
			win();
		else
			lose();
	}

	private static ArrayList<Unit> getLiveUnits(ArrayList<Unit> party){
		ArrayList<Unit> live=new ArrayList<Unit>();
		for(Unit u:party){
			if(!u.getPokemon().isFainted())
				live.add(u);
		}
		return live;
	}

	/**
	 * Returns the number of non-fainted units from the given party. 
	 * @param party
	 * @return
	 */
	private static int getNumLiveUnits(ArrayList<Unit> party){
		int count=0;
		for(Unit u:party){
			if(!u.getPokemon().isFainted())
				count++;
		}
		return count;
	}

	public static void win(){
		System.out.println("Player won the battle against "+opponent.getName());
		//TODO:
		GlobalEngine.defeatedTrainer(opponent);
		close();
		MapEngine.initialize(PlayerData.getLocation());
	}

	private static void lose(){
		//TODO:
	}

	public static void flee(){
		System.out.println("You have fled from the battle!");
		close();
		MapEngine.initialize(PlayerData.getLocation());
	}

	public static void openPlayerMenu(){
		System.out.println("Opening battle menu");
		MenuEngine.initialize(new BattlePlayerMenu());
	}

	public static void openUnitMenu(){
		System.out.println("Opening battle menu");
		MenuEngine.initialize(new UnitMenu(activeunit));
	}

	public static void useMove(Move move){
		System.out.println(activeunit.getPokemon().getName()+" using "+GameData.getMoveName(move.getNum()));
		if(activeunit.getPokemon().getPcondition()==PermCondition.Confusion&&GameData.getRandom().nextBoolean()){
			System.out.println(activeunit.getPokemon().getName()+" hit itself in its confusion");
			activeunit.damage(calculateConfusionSelfHitDamage(activeunit));
		}
		else{
			ArrayList<Unit> targets=null;
			//TODO:
			for(Unit target:targets){
				System.out.println(activeunit.getPokemon().getName()+" attacks "+target.getPokemon().getName()+" with "+GameData.getMoveName(move.getNum()));
				target.damage(calculateDamage(activeunit,target,move.getNum()));
				//TODO: endure/focus band logic
			}
			//TODO: currently assuming enemy dies, therefore party gets xp, need to add logic to check if party member died, since enemies
			//ai will also use this method to attack
			for(Unit target:targets){
				Pokemon targetpokemon=target.getPokemon();
				if(targetpokemon.isFainted()){
					ArrayList<Pokemon> xprecipients=new ArrayList<Pokemon>();
					for(Unit unit:getLiveUnits(punits)){
						Pokemon pokemon=unit.getPokemon();
						if(pokemon.isHolding("Exp Share")||target.attackedBy(unit.getID()))
							xprecipients.add(pokemon);
					}
					awardExperience(xprecipients,targetpokemon);
				}
			}
			activeunit.setHasAttacked(true);
		}
		activeunit.setHasTakenAction(true);
		//confusion turn count is only lowered by attacking turns. Pokemon can't avoid confusion by refusing to attack until it's over
		if(activeunit.getPokemon().getPcondition()==PermCondition.Confusion)
			activeunit.incNumTurnsAfflicted(PermCondition.Confusion.toString());
		MenuEngine.initialize(new UnitMenu(activeunit));
	}

	private static void awardExperience(ArrayList<Pokemon> recipients,Pokemon giver){
		double xpshare=GameData.getBaseExp(giver.getNum())*giver.getLevel();
		if(!giver.isWild())
			xpshare*=1.5;
		xpshare/=recipients.size();
		for(Pokemon p:recipients){
			p.gainExp(round(xpshare));
			System.out.println(p.getName()+" gains "+xpshare+" experience");
		}
	}

	public static void endTurn(){
		activeunit.setHasEndedTurn(true);
		endOfTurnActions();
		nextTurn();
	}

	public static void move(){
		previousmoves=new ArrayList<IntPair>();
		previousmoves.add(activeunit.getCoordinates());
		displayMovementRange();
		takeControl(new BattleMovementKeyListener());
	}

	private static void displayMovementRange(){
		validdestinations=new ArrayList<IntPair>();
		int range=activeunit.getMovementRange()-previousmoves.size()+1;
		int xcoordinate=activeunit.getCoordinates().getX();
		int ycoordinate=activeunit.getCoordinates().getY();
		validdestinations.add(activeunit.getCoordinates());
		System.out.println(activeunit.getPokemon().getName()+" moving up to "+range+" units");
		for(int x=xcoordinate-range;x<=xcoordinate+range;x++){
			for(int y=ycoordinate-range;y<=ycoordinate+range;y++){
				if(y>=Constants.BATTLEFIELD_HEIGHT)
					break;
				if(x>=0&&y>=0&&x<Constants.BATTLEFIELD_WIDTH
						&&Math.abs(x-xcoordinate)+Math.abs(y-ycoordinate)<=range&&canMoveTo(battlefield[x][y]))
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

	private static boolean canMoveTo(Square square){
		if(square.getUnit()>-1)
			return false;
		if(square.getTileType()==Tile.Water&&!activeunit.isType(Type.Water))
			return false;
		if(square.getTileType()==Tile.Lava&&!activeunit.isType(Type.Fire))
			return false;
		//TODO: Implement logic so that flyers can move over water/lava tiles, but cannot stop on them
		//add check in confirmMovement that either disallows movement and/or gives prompt to user that 
		//they cannot end there.
		return true;
	}

	public static void moveLeft(){
		int x=activeunit.getCoordinates().getX();
		int y=activeunit.getCoordinates().getY();
		IntPair newcoordinates=new IntPair(x-1,y);
		if(previousmoves.size()==activeunit.getMovementRange()+1&&previousmoves.get(previousmoves.size()-2).equals(newcoordinates)
				||(x>0&&battlefield[x-1][y].isValid())){
			moveOffSquare(activeunit,x,y);
			moveOnSquare(activeunit,x-1,y);
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
			moveOffSquare(activeunit,x,y);
			moveOnSquare(activeunit,x+1,y);
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
			moveOffSquare(activeunit,x,y);
			moveOnSquare(activeunit,x,y-1);
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
			moveOffSquare(activeunit,x,y);
			moveOnSquare(activeunit,x,y+1);
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
		for(IntPair pair:validdestinations){
			battlefield[pair.getX()][pair.getY()].markNeutral();
		}
		activeunit.setHasMoved(true);
		System.out.println(activeunit.getPokemon().getName()+" has moved "+(previousmoves.size()-1)+" squares.");
		if(spikesplaced){
			int spikescount=0;
			for(IntPair pair:previousmoves){
				if(battlefield[pair.getX()][pair.getY()].hasSpikes())
					spikescount++;
			}
			int damage=round(spikescount*activeunit.getPokemon().getStat(Stat.HP)*Constants.SPIKES_DAMAGE_RATE);
			System.out.println(activeunit.getPokemon().getName()+" takes "+damage+" damage from spikes");
			activeunit.getPokemon().decHP(damage);
		}
		if(!activeunit.getPokemon().isFainted())
			MenuEngine.initialize(new UnitMenu(activeunit));
		else
			nextTurn();
	}

	public static Unit getActiveUnit(){
		return activeunit;
	}

	public static Pokemon getActivePokemon(){
		return activeunit.getPokemon();
	}

	/**
	 * Determine if the given move uses special attack/defense for damage calculation
	 * @param movenum
	 * @return
	 */
	public static boolean usesSpecial(Type type){
		return (type==Type.Water||type==Type.Grass||type==Type.Fire||type==Type.Ice||type==Type.Electric||type==Type.Psychic||type==Type.Dragon||type==Type.Dark);
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

	private static int calculateDamage(Unit attacker, Unit defender, int movenum){
		int damage=0;
		Type movetype=GameData.getMoveType(movenum);
		Pokemon attackerpokemon=attacker.getPokemon();
		//BASE DAMAGE CALCULATION
		double parta=(2*attackerpokemon.getLevel()+10)/250;
		double partb=0;
		if(usesSpecial(movetype))
			partb=attacker.getStat(Stat.SpecialAttack)/defender.getStat(Stat.SpecialDefense);
		else
			partb=attacker.getStat(Stat.Attack)/defender.getStat(Stat.Defense);
		double partc=GameData.getMovePower(movenum);
		double totalbase=(parta*partb*partc)+2;
		//MODIFIERS
		//same type attack mod
		double stabmod=1;
		if(attacker.isType(movetype))
			stabmod=1.5;
		//type effectiveness mod
		double typemod=1;
		for(Type t:defender.getTypes()){
			typemod*=GameData.getTypeEffectivenessDamageMod(movetype,t);
		}
		//critical hit mod
		double critmod=1;
		if(GameData.getRandom().nextInt(100)<attacker.getCritRatio())
			critmod=2;
		//held item type boost mod
		double heldmod=1;
		if(movetype==attackerpokemon.getHeldItemBoostType()){
			if(movetype==Type.Normal)
				heldmod=1.15;
			else
				heldmod=1.2;
		}
		//random mod
		double randommod=(GameData.getRandom().nextInt(16)+85)/100;
		//burn mod
		double burnmod=1;
		if(attackerpokemon.getPcondition()==PermCondition.Burn&&!usesSpecial(movetype))
			burnmod-=Constants.BURN_PHYSICAL_ATTACK_DAMAGE_REDUCTION;
		double totalmodifier=stabmod*typemod*critmod*heldmod*randommod*burnmod;
		damage=(int)(totalbase*totalmodifier);
		System.out.println("( "+parta+" * "+partb+" * "+partc+" + 2) * "+stabmod+" * "+typemod+" * "+critmod+" * "
				+heldmod+" * "+randommod+" * "+burnmod+" = "+damage);
		return damage;
	}

	public static boolean doesMoveHit(Unit defender, Unit attacker, int movenum){
		return GameData.getRandom().nextInt(100)<calculateChanceOfHitting(defender,attacker,movenum);
	}

	private static int calculateChanceOfHitting(Unit defender, Unit attacker, int movenum){
		System.out.println(GameData.getMoveAccuracy(movenum)+"*"+defender.getEvasion()+"*"+attacker.getAccuracy());
		return round(GameData.getMoveAccuracy(movenum)*defender.getEvasion()*attacker.getAccuracy());
	}

	private static int round(double num){
		int intnum=(int)num;
		if(num<intnum+0.5)
			return intnum;
		else 
			return intnum+1;
	}

	public static boolean doesCatchSucceed(Pokemon target, double ballbonus){
		double a=calculateA(target,ballbonus);
		System.out.println("Catch Rate A: "+a);
		if(GameData.getRandom().nextInt(256)<a)
			return true;
		else{
			for(int i=0;i<3;i++){
				double b=calculateB(a);
				System.out.println("Catch Rate B: "+b);
				if(GameData.getRandom().nextInt(65536)>=b)
					return false;
			}
			return true;
		}
	}

	private static double calculateB(double a){
		return 1048560/(Math.sqrt((int)Math.sqrt(16711680/a)));
	}

	private static double calculateA(Pokemon target, double ballbonus){
		double a=3*target.getStat(Stat.HP)-2*target.getCurrHP();
		a*=GameData.getCatchRate(target.getNum());
		a*=ballbonus;
		PermCondition condition=target.getPcondition();
		if(condition==PermCondition.Frozen||condition==PermCondition.Sleep)
			a*=2;
		else if(condition==PermCondition.Burn||condition==PermCondition.Paralysis||condition==PermCondition.Poison||condition==PermCondition.BadlyPoison)
			a*=1.5;
		a/=(3*target.getStat(Stat.HP));
		return a;
	}

	public static boolean isLegalMove(Move m){
		Pokemon activepokemon=activeunit.getPokemon();
		if(activepokemon.getPcondition()==PermCondition.Sleep){
			return m.getNum()==GameData.getMoveNum("Snore")||m.getNum()==GameData.getMoveNum("Sleep Talk");
		}
		return true;
	}
}
