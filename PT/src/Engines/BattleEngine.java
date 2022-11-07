package Engines;

import java.awt.event.KeyListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import Enums.Direction;
import Enums.EventName;
import Enums.PermCondition;
import Enums.Stat;
import Enums.Tile;
import Enums.Type;
import Enums.Weather;
import Global.Constants;
import Global.GameData;
import Global.PlayerData;
import Menus.BattlePlayerMenu;
import Menus.UnitMenu;
import Objects.BattleAttackLogic;
import Objects.BattleMovementLogic;
import Objects.BattlefieldMaker;
import Objects.CatchLogic;
import Objects.EliteTrainer;
import Objects.IntPair;
import Objects.LoadExistingBattlefieldMaker;
import Objects.Move;
import Objects.MoveLogic;
import Objects.Pokemon;
import Objects.ProceduralBattlefieldMaker;
import Objects.Square;
import Objects.Trainer;
import Objects.Unit;
import Objects.WildTrainer;
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
	private static boolean spikesplaced;
	private static int numpaydays;
	private static int[] xpgains;
	private static Weather weather;
	private static int turn;
	private static int weatherturn;

	public static void initialize(Trainer newopponent){
		System.out.println("Initializing battle with "+newopponent.getName());
		inbattle=true;
		opponent=newopponent;
		bfmaker=new ProceduralBattlefieldMaker();
		spikesplaced=false;
		numpaydays=0;
		weather=null;
		weatherturn=0;
		turn=0;
		initBattlefield();
		initUnits();
		xpgains=new int[punits.size()];
		placeNewPlayerUnits();
	}

	private static void initBattlefield(){
		bfmaker.makeNew();
		battlefieldimage=bfmaker.getImage();
		battlefield=bfmaker.getResult();
		GameData.getGUI().add(battlefieldimage,10,10);  
	}

	public static void initUnits(){
		punits=new ArrayList<Unit>();
		ounits=new ArrayList<Unit>();
		allunits=new ArrayList<Unit>();
		Pokemon[] party=opponent.getParty();
		int idcounter=1;
		for(int i=0;i<party.length;i++){ 
			ounits.add(new Unit(party[i],i,idcounter,false));
			idcounter++;
		}
		ArrayList<Pokemon> party2=PlayerData.getParty();
		for(int i=0;i<party2.size();i++){
			punits.add(new Unit(party2.get(i),i,idcounter,true));
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
		allunits.addAll(ounits);
		placeNewOppUnits();
	}
	
	public static void continueInit(ArrayList<Unit> newpunits){
		punits=newpunits;
		allunits.addAll(punits);
		setPriorities();
		activeindex=0;
		activeunit=getUnitByID(priorities.get(0));
		System.out.println("Player units: ");
		for(Unit u:punits){
			System.out.println(u.getName()+" Lvl "+u.getPokemon().getLevel()+" ID "+u.getID());
		}
		System.out.println("Opponent units: ");
		for(Unit u:ounits){
			System.out.println(u.getName()+" Lvl "+u.getPokemon().getLevel()+" ID "+u.getID());
		}
		System.out.println("Turn order: ");
		for(int id:priorities){
			System.out.println(getUnitByID(id).getName()+" ID "+getUnitByID(id).getID());
		}
		takeTurn();
	}

	private static void setPriorities(){
		priorities=new ArrayList<Integer>();
		for(int i=0;i<allunits.size();i++){
			priorities.add(allunits.get(i).getID());
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
			if(getUnitByID(priorities.get(i)).getPokemon().isHolding("Quick Claw")&&GameData.getRandom().nextInt(100)<Constants.QUICK_CLAW_CHANCE){
				clawholders.add(priorities.remove(i));
				i--;
			}
		}
		//inserts from temp queue into front of priorities queue, slowest pushed in first and fastest put in last
		for(int i=clawholders.size()-1;i>=0;i--){
			System.out.println(getUnitByID(clawholders.get(i)).getName()+" is currently first in queue thanks to quick claw");
			priorities.add(clawholders.get(i),0);
		}
	}

	public static void nextTurn(){
		System.out.println("Going to next turn");
		if(getNumLiveUnits(punits)==0||getNumLiveUnits(ounits)==0)
			battleOver();
		activeindex++;
		if(activeindex>=priorities.size()){
			setPriorities();
			activeindex=0;
		}
		activeunit=getUnitByID(priorities.get(activeindex));
		if(activeunit.getPokemon().isFainted())
			nextTurn();
		else
			takeTurn();
	}

	private static void takeTurn(){
		turn++;
		System.out.println("Beggining turn "+turn);
		checkWeather();
		activeunit.setHasMoved(false);
		activeunit.setHasTakenAction(false);
		activeunit.setHasEndedTurn(false);
		activeunit.setHasAttacked(false);
		System.out.println(activeunit.getName()+" ("+activeindex+" in order) taking turn");
		MoveLogic.startOfTurnActions(activeunit);
		if(activeunit.getPokemon().isFainted())
			endTurn();
		else if(activeunit.isDigging()||activeunit.isFlying()||activeunit.isCharging()){
			if(activeunit.isDigging()){
				System.out.println(activeunit.getName()+" surfaces to attack.");
				activeunit.setDigging(false);
				useMove(new Move(252),false);
			}
			else if(activeunit.isFlying()){
				System.out.println(activeunit.getName()+" swoops down to attack.");
				activeunit.setFlying(false);
				useMove(new Move(253),false);
			}
			else if(activeunit.isCharging()){
				System.out.println(activeunit.getName()+" has finished charging and unleashes their attack.");
				activeunit.setCharging(false);
				useMove(new Move(GameData.getChargeMoveNum(activeunit.getPrevMove())),false);
			}
		}
		else{
			if(ounits.contains(activeunit)){
				System.out.println(activeunit.getID());
				endTurn();
			}
			else
				openUnitMenu();
		}
	}
	
	private static void checkWeather() {
		if(weatherturn>0&&weatherturn<turn) {
			System.out.println("Weather condition "+weather+" subsides");
			setWeather(Weather.None);
		}
	}

	public static Unit getUnitByID(int id){
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

	private static void placeNewPlayerUnits(){
//		switch(punits.size()){
//		case 6:moveOnSquare(punits.get(5),Constants.BATTLEFIELD_WIDTH-1,Constants.BATTLEFIELD_HEIGHT/2-3);
//		case 5:moveOnSquare(punits.get(4),Constants.BATTLEFIELD_WIDTH-1,Constants.BATTLEFIELD_HEIGHT/2+2);
//		case 4:moveOnSquare(punits.get(3),Constants.BATTLEFIELD_WIDTH-1,Constants.BATTLEFIELD_HEIGHT/2-2);
//		case 3:moveOnSquare(punits.get(2),Constants.BATTLEFIELD_WIDTH-1,Constants.BATTLEFIELD_HEIGHT/2+1);
//		case 2:moveOnSquare(punits.get(1),Constants.BATTLEFIELD_WIDTH-1,Constants.BATTLEFIELD_HEIGHT/2-1);
//		case 1:moveOnSquare(punits.get(0),Constants.BATTLEFIELD_WIDTH-1,Constants.BATTLEFIELD_HEIGHT/2);
//		}
		

		System.out.println("Player placing their units");
		UnitPlacementEngine.initialize(punits);
		//takeTurn();
	}

	private static void placeNewOppUnits(){
		switch(ounits.size()){
		case 6:moveOnSquare(ounits.get(5),0,Constants.BATTLEFIELD_HEIGHT/2-3);
		case 5:moveOnSquare(ounits.get(4),0,Constants.BATTLEFIELD_HEIGHT/2+2);
		case 4:moveOnSquare(ounits.get(3),0,Constants.BATTLEFIELD_HEIGHT/2-2);
		case 3:moveOnSquare(ounits.get(2),0,Constants.BATTLEFIELD_HEIGHT/2+1);
		case 2:moveOnSquare(ounits.get(1),0,Constants.BATTLEFIELD_HEIGHT/2-1);
		case 1:moveOnSquare(ounits.get(0),0,Constants.BATTLEFIELD_HEIGHT/2);
		}
		for(Unit u:ounits){
			u.setDirectionFacing(Direction.Right);
		}
	}

	private static void placeExistingUnits(){
		for(Unit u:allunits){
			moveOnSquare(u,u.getCoordinates().getX(),u.getCoordinates().getY());
		}
	}

	public static void moveOnSquare(Unit unit,int x,int y){
		unit.setCoordinates(x, y);
		battlefield[x][y].setUnit(unit.getID());
		battlefieldimage.add(unit.getImage(),x*Constants.TILE_SIZE,y*Constants.TILE_SIZE);
	}

	public static void moveOffSquare(Unit unit,int x,int y){
		battlefield[x][y].removeUnit();
		battlefieldimage.remove(unit.getImage());
	}
	
	public static void markGreen(int x,int y){
		battlefield[x][y].markValid();
	}
	
	public static void markBlack(int x,int y){
		battlefield[x][y].markNeutral();
	}

	/**
	 * Resumes a battle that was saved previously. Called on initialization if last player save was mid-battle.
	 */
	public static void load(){
		inbattle=true;
		try{
			File f=new File(Constants.PATH+"\\InitializeData\\battlesavefile.txt");
			Scanner s=new Scanner(f);
			turn=s.nextInt();
			s.nextLine();
			String curr=s.next();
			if(curr.equals("WildTrainer:"))
				opponent=WildTrainer.readInTrainer(s);
			else if(curr.equals("EliteTrainer:"))
				opponent=EliteTrainer.readInTrainer(s);
			else if(curr.equals("Trainer:"))
				opponent=Trainer.readInTrainer(s);
			System.out.println("Initializing previous battle against "+opponent.getName());
			curr=s.next();
			allunits=new ArrayList<Unit>();
			punits=new ArrayList<Unit>();
			ounits=new ArrayList<Unit>();
			while(!curr.equals("End")){
				punits.add(Unit.readInUnit(null,s));
				curr=s.next();
			}
			s.nextLine();// Player Units
			curr=s.next();
			while(!curr.equals("End")){
				ounits.add(Unit.readInUnit(opponent,s));
				curr=s.next();
			}
			s.nextLine();// Opponent Units
			allunits.addAll(punits);
			allunits.addAll(ounits);
			priorities=new ArrayList<Integer>();
			int[] temp=GameData.readIntArray(s.nextLine());
			for(int i:temp){
				priorities.add(i);
			}
			xpgains=GameData.readIntArray(s.nextLine());
			spikesplaced=s.nextBoolean();
			numpaydays=s.nextInt();
			activeindex=s.nextInt();
			activeunit=getUnitByID(s.nextInt());
			weather=Weather.valueOf(s.next());
			weatherturn=s.nextInt();
			s.nextLine();
			bfmaker=new LoadExistingBattlefieldMaker(s);
			initBattlefield();
			placeExistingUnits();
			MenuEngine.initialize(new UnitMenu(activeunit));
		}catch(Exception e){e.printStackTrace();System.out.println(e.getCause().toString());}
	}

	/**
	 * Saves all battle data to file to be loaded on initialization of program. Used when player saves mid battle.
	 */
	public static void save(){
		try{
			File f=new File(Constants.PATH+"\\InitializeData\\battlesavefile.txt");
			PrintWriter pw=new PrintWriter(f);
			pw.println(turn);
			pw.println(opponent.toString());
			for(Unit u:punits){
				pw.println(u.toString());
			}
			pw.println("End Player Units");
			for(Unit u:ounits){
				pw.println(u.toString());
			}
			pw.println("End Opponent Units");
			pw.println(priorities);
			pw.println(Arrays.asList(xpgains));
			pw.println(spikesplaced+" "+numpaydays+" "+activeindex+" "+activeunit.getID()+" "+weather+" "+weatherturn);
			pw.println(bfmaker.toString());
			pw.close();
		}catch(Exception e){e.printStackTrace();}
	}
	
	public static boolean isWithinBounds(IntPair point) {
		int x=point.getX();
		int y=point.getY();
		return x>=0&&y>=0&&x<=Constants.BATTLEFIELD_WIDTH&&y<=Constants.BATTLEFIELD_HEIGHT;
	}
	
	public static Weather getWeather(){
		return weather;
	}
	
	public static void setWeather(Weather newconditions){
		weather=newconditions;
		if(Weather.getDuration(weather)>0) {
			weatherturn=turn+Weather.getDuration(weather);
		}
		else
			weatherturn=0;
	}

	public static int getPayDayVal(){
		return numpaydays;
	}

	public static void incPayDayVal(int lvl){
		numpaydays+=lvl;
	}

	public static Unit getActiveUnit(){
		return activeunit;
	}

	public static Pokemon getActivePokemon(){
		return activeunit.getPokemon();
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

	public static void openPlayerMenu(){
		System.out.println("Opening battle menu");
		MenuEngine.initialize(new BattlePlayerMenu());
	}

	public static void openUnitMenu(){
		System.out.println("Opening battle menu");
		MenuEngine.initialize(new UnitMenu(activeunit));
	}

	public static void move(){
		BattleMovementLogic.move(battlefield);
	}

	public static void useMove(Move move,boolean cancellable){
		BattleAttackLogic.useMove(battlefield,move,cancellable);
	}

	public static void catchLogic(int ballid){
		CatchLogic.catchLogic(battlefield,ballid);
	}

	
	public static void removeUnit(int unitid){
		Unit u=getUnitByID(unitid);
		IntPair coord=u.getCoordinates();
		moveOffSquare(u,coord.getX(),coord.getY());
		int index=ounits.indexOf(u);
		if(index>-1){
			ounits.remove(index);
		}
		else{
			index=punits.indexOf(u);
			punits.remove(index);
		}
		priorities.remove(new Integer(unitid));
		allunits.remove(u);
	}

	/**
	 * Returns the party that the target unit belongs to (including the unit itself)
	 * @param unit
	 * @return
	 */
	public static ArrayList<Unit> getFriendlyUnits(Unit unit){
		ArrayList<Unit> friends=new ArrayList<Unit>();
		if(unit.isPlayerOwned()){
			friends.addAll(punits);
		}
		else{
			friends.addAll(ounits);
		}
		return friends;
	}

	public static void experienceGainLogic(ArrayList<Unit> targetunits){
		for(Unit target:targetunits){
			Pokemon targetpokemon=target.getPokemon();
			//XP gain if target fainted (note xp is not given to opp units on defeating players units)
			if(ounits.contains(target)&&targetpokemon.isFainted()){
				ArrayList<Unit> xprecipients=new ArrayList<Unit>();
				for(Unit unit:punits){
					//TODO: attackedby not currently being set. need logic in unit to rule out
					//friendlies and then call into that when certain moves used on a unit
					if(unit.isHolding("Exp Share")||target.attackedBy(unit.getID()))
						xprecipients.add(unit);
				}
				awardExperience(xprecipients,targetpokemon);
			}
		}
	}


	private static void awardExperience(ArrayList<Unit> recipients,Pokemon giver){
		double xpshare=GameData.getBaseExp(giver.getNum())*giver.getLevel();
		if(!giver.isWild())
			xpshare*=1.5;
		xpshare/=recipients.size();
		for(Unit recipient:recipients){
			xpgains[punits.indexOf(recipient)]+=(round(xpshare));
			System.out.println(recipient.getName()+" will gain "+xpshare+" experience for taking down"+giver.getName()+"after battle");
		}
	}

	public static void endTurn(){
		System.out.println("Ending turn for unit "+activeunit.getID());
		activeunit.setHasEndedTurn(true);
		if(!activeunit.getPokemon().isFainted())
			MoveLogic.endOfTurnActions(activeunit);
		nextTurn();
	}
	
	public static boolean canMoveTo(Unit u,IntPair square,boolean stop){
		return canMoveTo(u,battlefield[square.getX()][square.getY()],stop);
	}

	public static boolean canMoveTo(Unit u,Square square,boolean stop){
		if(square.getUnit()>-1) {
			System.out.println("Already a unit on sqaure "+square);
			return false;
		}
		//Flyers can traverse any tile as long as they're not stopping on them.
		if(u.isFlying()&&!stop) {
			System.out.println(u+" flying over sqaure "+square);
			return true;
		}
		//Nothing can stop on these tiles
		if(square.getTileType()==Tile.Rock||square.getTileType()==Tile.Tree) {
			System.out.println(u+" cannot traverse "+square);
			return false;
		}
		if(square.getTileType()==Tile.Water&&!u.isType(Type.Water)) {
			System.out.println(u+" cannot traverse water because its not water type");
			return false;
		}
		if(square.getTileType()==Tile.Lava&&!u.isType(Type.Fire)) {
			System.out.println(u+" cannot traverse lava because its not fire type");
			return false;
		}
		return true;
	}

	public static int round(double num){
		int intnum=(int)num;
		if(num<intnum+0.5)
			return intnum;
		else 
			return intnum+1;
	}

	public static boolean areSpikesPlaced(){
		return spikesplaced;
	}

	public static void removeSpikes(){
		if(!spikesplaced)
			return;
		for(int x=0;x<battlefield.length;x++){
			for(int y=0;y<battlefield[0].length;y++){
				if(battlefield[x][y].hasSpikes())
					battlefield[x][y].removeSpikes();
			}
		}
	}



	public static boolean isLegalMove(Move m){
		Pokemon activepokemon=activeunit.getPokemon();
		if(activepokemon.getPcondition()==PermCondition.Sleep){
			return m.getNum()==GameData.getMoveNum("Snore")||m.getNum()==GameData.getMoveNum("Sleep Talk");
		}
		return true;
	}

	public static void win(){
		System.out.println("Player won the battle against "+opponent.getName());
		HashMap<Pokemon,Integer> pokemonToXP=new HashMap<Pokemon,Integer>();
		for(int i=0;i<punits.size();i++) {
			pokemonToXP.put(punits.get(i).getPokemon(),xpgains[i]);
		}
		GlobalEngine.defeatedTrainer(opponent,pokemonToXP);
	}

	public static void lose(){
		System.out.println("Player lost the battle against "+opponent.getName());
		GlobalEngine.loseMoney();
		close();
		GlobalEngine.triggerEvent(EventName.BlackOut);
	}

	public static void flee(){
		System.out.println("You have fled from the battle!");
		toMap();
	}
	
	public static void toMap(){
		close();
		MapEngine.initialize(PlayerData.getLocation());
	}

	public static void close(){
		GlobalEngine.giveUpControl();
		System.out.println("Ending battle");
		GameData.getGUI().remove(battlefieldimage);
		inbattle=false;
		File f=new File(Constants.PATH+"\\InitializeData\\battlesavefile.txt");
		if(f.exists())
			f.delete();
		GlobalEngine.updateLeadingPokemon();
		delete();
	}
	
	public static void delete(){
		for(Unit u:allunits){
			u.delete();
		}
		opponent=null;
		punits=null;
		ounits=null;
		allunits=null;
		priorities=null;
		activeunit=null;
		battlefieldimage=null;
		battlefield=null;
		bfmaker=null;
		weather=null;
		MoveLogic.delete();
		CatchLogic.delete();
		BattleAttackLogic.delete();
		BattleMovementLogic.delete();
	}
}
