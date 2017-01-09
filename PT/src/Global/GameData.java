package Global;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import Engines.GUI;
import Enums.ItemType;
import Enums.LocationName;
import Enums.MoveEffect;
import Enums.Requirement;
import Enums.Stat;
import Enums.Time;
import Enums.Type;
import Objects.IntPair;
import Objects.Move;

public class GameData {
	private static Random random;
	private static String[][] movestrings=null;
	private static int[][] movenums=null;
	private static String[][] itemstrings=null;
	private static int[] itemlvls=null;
	private static int[] lootbylvl=null;
	private static IntPair[] lootlvlthresholds=null;
	private static String[] pokenames=null;
	private static int[][] stats=null;
	private static int[] expgroups=null;
	private static int[] catchrates=null;
	private static Type[][] types=null;
	private static int[] basexps=null;
	private static ArrayList<ArrayList<String>> evolutionconditions=null;
	private static ArrayList<ArrayList<Integer>> evolutionnums=null;
	private static int[][] expThresholdsByGroupAndLevel=null;
	private static ArrayList<ArrayList<Integer>> validtms=null;
	private static ArrayList<Integer>[][] moveslearned=new ArrayList[Constants.NUM_POKEMON+1][101];
	private static ArrayList<Move>[][] movesetsbylvl=new ArrayList[Constants.NUM_POKEMON+1][101];
	private static ArrayList<Short> locationids=new ArrayList<Short>();
	private static ArrayList<LocationName> locationnames=new ArrayList<LocationName>();
	private static ArrayList<Requirement> locationreqs=new ArrayList<Requirement>();
	private static double[] statstages=null;
	private static Map<Type,HashMap<Type,Double>> typechart=null;
	private static int[] critstages=null;
	private static double[] accevastages=null;
	private static String[] moveeffects=null;
	private static GUI gui;
	private static Time time;

	public static void initialize(){
		random=new Random();
		System.out.println("Initializing GUI");
		gui=new GUI();
		gui.start();
		File f;
		Scanner s;
		try{
			f=new File(Constants.PATH+"\\InitializeData\\gamesavefile.txt");
			s=new Scanner(f);
			time=Time.valueOf(s.nextLine());
			s.close();
			f=new File(Constants.PATH+"\\InitializeData\\locationreq.txt");
			s=new Scanner(f);
			short count=200;
			while(s.hasNextLine()){
				locationnames.add(LocationName.valueOf(s.next()));
				locationids.add(count);
				locationreqs.add(Requirement.valueOf(s.next()));
				count++;
			}
			s.close();
			f=new File(Constants.PATH+"\\InitializeData\\bylevel.txt");
			s=new Scanner(f);
			for(int i=1;i<Constants.NUM_POKEMON+1;i++){
				String line=s.nextLine();
				String[] levels=line.split(",,");
				ArrayList<Integer>[] level=new ArrayList[101];
				for(int j=0;j<level.length;j++){
					if(levels[j].equals("null"))
						level[j]=null;
					else{
						ArrayList<Integer> movenums1=new ArrayList<Integer>();
						String str=levels[j];
						str=str.substring(1,str.length()-1);
						String[] moves=str.split(",");
						for(String move:moves){
							movenums1.add(Integer.parseInt(move));
						}
						level[j]=movenums1;
					}
				}
				moveslearned[i]=level;
			}
			for(int i=1;i<Constants.NUM_POKEMON+1;i++){
				for(int j=1;j<101;j++){
					movesetsbylvl[i][j]=setMovesetByLevel(i,j);
				}
			}
		}
		catch(Exception e){e.printStackTrace();}
	}

	public static double getAccEvaStageMultiplier(Stat stat,int stage){
		if(accevastages==null){
			try{
				File f=new File(Constants.PATH+"\\InitializeData\\accevastages.txt");
				Scanner s=new Scanner(f);
				accevastages=new double[13];
				for(int i=0;i<accevastages.length;i++){
					accevastages[i]=Double.parseDouble(s.nextLine());
				}
				s.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		//maxed accuracy gets highest chance of hitting
		if(stat==Stat.Accuracy)
			return accevastages[stage+6];
		//maxed evasion gets lowest chance of hitting
		else if(stat==Stat.Evasion)
			return accevastages[6-stage];
		return 0;
	}

	public static int getCritRatio(int stage){
		if(critstages==null){
			try{
				File f=new File(Constants.PATH+"\\InitializeData\\critstages.txt");
				Scanner s=new Scanner(f);
				critstages=new int[7];
				for(int i=0;i<critstages.length;i++){
					critstages[i]=Integer.parseInt(s.nextLine());
				}
				s.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return critstages[stage];
	}

	public static double getStatStageMultiplier(int stage){
		if(statstages==null){
			try{
				File f=new File(Constants.PATH+"\\InitializeData\\statstages.txt");
				Scanner s=new Scanner(f);
				statstages=new double[13];
				for(int i=0;i<statstages.length;i++){
					statstages[i]=s.nextDouble();
				}
				s.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return statstages[stage+6];
	}

	public static Time getTime(){
		return time;
	}

	public static GUI getGUI(){
		return gui;
	}

	public static LocationName getLocationNameByID(short id){
		int index=locationids.indexOf(id);
		if(index<0)
			return null;
		return locationnames.get(index);
	}

	public static Requirement getLocationRequirement(LocationName ln){
		int index=locationnames.indexOf(ln);
		if(index<0)
			return null;
		return locationreqs.get(index);
	}

	public static short getIDByLocationName(LocationName ln){
		int index=locationnames.indexOf(ln);
		if(index<0)
			return -1;
		return locationids.get(index);
	}

	public static Random getRandom(){
		return random;
	}

	public static double getTypeEffectivenessDamageMod(Type attacktype,Type targettype){
		if(typechart==null){
			try{
				File f=new File(Constants.PATH+"\\InitializeData\\typechart.txt");
				Scanner s=new Scanner(f);
				typechart=new HashMap<Type,HashMap<Type,Double>>();
				Type[] typesordered={Type.Normal,Type.Fighting,Type.Flying,Type.Poison,Type.Ground,Type.Rock,Type.Bug,Type.Ghost,Type.Steel,Type.Fire,Type.Water,Type.Grass,Type.Electric,Type.Psychic,Type.Ice,Type.Dragon,Type.Dark};
				for(int i=0;i<typesordered.length;i++){
					HashMap<Type,Double> thistype=new HashMap<Type,Double>();
					for(int j=0;j<typesordered.length;j++){
						thistype.put(typesordered[j],s.nextDouble());
					}
					s.nextLine();
					typechart.put(typesordered[i],thistype);
				}
				s.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return typechart.get(attacktype).get(targettype);
	}

	public static int getBaseExp(int pokenum){
		if(basexps==null){
			try{
				File f=new File(Constants.PATH+"\\InitializeData\\basexp.txt");
				Scanner s=new Scanner(f);
				basexps=new int[Constants.NUM_POKEMON+1];
				for(int i=1;i<basexps.length;i++){
					basexps[i]=Integer.parseInt(s.nextLine());
				}
				s.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return basexps[pokenum];
	}

	public static int getMovePP(int movenum){
		if(movenums==null)
			loadMoveNums();
		return movenums[movenum][0];
	}

	public static int getMoveMaxPP(int movenum){
		if(movenums==null)
			loadMoveNums();
		return movenums[movenum][1];
	}

	public static int getMovePower(int movenum){
		if(movenums==null)
			loadMoveNums();
		return movenums[movenum][2];
	}

	public static double getMoveAccuracy(int movenum){
		if(movenums==null)
			loadMoveNums();
		return movenums[movenum][3]/100;
	}

	public static int getMoveNum(String movename){
		if(movestrings==null)
			loadMoveStrings();
		for(int i=1;i<movestrings.length;i++){
			if(movestrings[i][0].equals(movename))
				return i;
		}
		return -1;
	}

	public static String getMoveName(int movenum){
		if(movestrings==null)
			loadMoveStrings();
		return movestrings[movenum][0];
	}

	public static Type getMoveType(int movenum){
		if(movestrings==null)
			loadMoveStrings();
		return Type.valueOf(movestrings[movenum][1]);
	}

	public static String getMoveDescription(int movenum){
		if(movestrings==null)
			loadMoveStrings();
		return movestrings[movenum][2];
	}

	public static int[] getBaseStats(int pokenum){
		if(stats==null){
			try{
				stats=new int[Constants.NUM_POKEMON+1][6];
				File f=new File(Constants.PATH+"\\InitializeData\\stats.txt");
				Scanner s=new Scanner(f);
				for(int i=1;i<stats.length;i++){
					String[] split=s.nextLine().split(",");
					for(int j=0;j<split.length;j++){
						stats[i][j]=Integer.parseInt(split[j]);
					}
				}
				s.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return stats[pokenum];
	}

	public static ArrayList<Move> getMovesetByLevel(int pokenum,int level){
		return movesetsbylvl[pokenum][level];
	}

	private static ArrayList<Move> setMovesetByLevel(int pokenum,int level){
		ArrayList<Move> moveset=new ArrayList<Move>();
		int count=0;
		out:
			for(int i=level;i>0;i--){
				ArrayList<Integer> list=moveslearned[pokenum][i];
				if(list!=null){
					for(int j=list.size()-1;j>=0;j--){
						int num=list.get(j);
						if(moveset.contains(num))
							continue;
						moveset.add(new Move(num));
						count++;
						if(count==4)
							break out;
					}
				}
			}
		return moveset;
	}

	private static boolean containsMoveNum(Move[] moveset,int num){
		for(Move m:moveset){
			if(m!=null&&m.getNum()==num)
				return true;
		}
		return false;
	}

	public static ArrayList<Integer> getMovesLearnedByLevel(int pokenum,int level){
		return moveslearned[pokenum][level];
	}

	public static String getName(int pokenum){
		if(pokenames==null){
			try{
				pokenames=new String[Constants.NUM_POKEMON+1];
				File f=new File(Constants.PATH+"\\InitializeData\\pokenames.txt");
				Scanner s=new Scanner(f);
				for(int i=1;i<pokenames.length;i++){
					pokenames[i]=s.nextLine();
				}
				s.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return pokenames[pokenum];
	}

	public static ArrayList<Type> getTypes(int pokenum){
		if(types==null){
			try{
			types=new Type[Constants.NUM_POKEMON+1][2];
			File f=new File(Constants.PATH+"\\InitializeData\\types.txt");
			Scanner s=new Scanner(f);
			for(int i=1;i<types.length;i++){
				String line=s.nextLine();
				String[] split=line.split(",");
				Type[] curr=new Type[2];
				curr[0]=Type.valueOf(split[0]);
				if(split.length==2){
					curr[1]=Type.valueOf(split[1]);
				}
				types[i]=curr;
			}
			s.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		ArrayList<Type> toReturn=new ArrayList<Type>();
		Type[] temp=types[pokenum];
		toReturn.add(temp[0]);
		if(temp.length==2)
			toReturn.add(temp[1]);
		return toReturn;
	}

	public static int getCatchRate(int pokenum){
		if(catchrates==null){
			catchrates=new int[Constants.NUM_POKEMON+1];
			File f=new File(Constants.PATH+Constants.PATH+"\\InitializeData\\catchrates.txt");
			Scanner s=null;
			try {
				s = new Scanner(f);
				for(int i=1;i<catchrates.length;i++){
					catchrates[i]=Integer.parseInt(s.nextLine());
				}
				s.close();
			}catch(FileNotFoundException e){e.printStackTrace();}
		}
		return catchrates[pokenum];
	}

	public static ArrayList<String> getEvolutionConditions(int pokenum){
		if(evolutionconditions==null){
			try{
				evolutionconditions=new ArrayList<ArrayList<String>>();
				File f=new File(Constants.PATH+"\\InitializeData\\evolvecond.txt");
				Scanner s=new Scanner(f);
				evolutionconditions.add(new ArrayList<String>());
				while(s.hasNextLine()){
					ArrayList<String> conditions=new ArrayList<String>();
					String line=s.nextLine();
					if(line.equals("DNE")){
						evolutionconditions.add(conditions);
						continue;
					}
					String[] split=line.split("/");
					for(int i=0;i<split.length;i++){
						conditions.add(split[i]);
					}
					evolutionconditions.add(conditions);
				}
				s.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return evolutionconditions.get(pokenum);
	}


	public static int getEvolutionNum(int pokenum,String condition){
		if(evolutionnums==null){
			try{
				evolutionnums=new ArrayList<ArrayList<Integer>>();
				File f=new File(Constants.PATH+"\\InitializeData\\evolveresult.txt");
				Scanner s=new Scanner(f);
				evolutionnums.add(null);
				while(s.hasNextLine()){
					String line=s.nextLine();
					if(line.length()==0){
						evolutionnums.add(null);
						continue;
					}
					ArrayList<Integer> nums=new ArrayList<Integer>();
					String[] split=line.split("/");
					for(int i=0;i<split.length;i++){
						nums.add(Integer.parseInt(split[i]));
					}
					evolutionnums.add(nums);
				}
				s.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return evolutionnums.get(pokenum).get(getEvolutionConditions(pokenum).indexOf(condition));
	}

	public static int getExpThreshold(int pokenum,int level){
		if(expgroups==null||expThresholdsByGroupAndLevel==null){
			try{
				expgroups=new int[Constants.NUM_POKEMON+1];
				expThresholdsByGroupAndLevel=new int[4][101];
				File f=new File(Constants.PATH+"\\InitializeData\\expgroups.txt");
				Scanner s=new Scanner(f);
				for(int i=1;i<expgroups.length;i++){
					expgroups[i]=Integer.parseInt(s.nextLine());
				}
				s.close();
				f=new File(Constants.PATH+"\\InitializeData\\expthreshbygroup.txt");
				s=new Scanner(f);
				for(int i=0;i<expThresholdsByGroupAndLevel.length;i++){
					String line=s.nextLine();
					String[] split=line.split(",");
					for(int j=0;j<split.length;j++){
						expThresholdsByGroupAndLevel[i][j]=Integer.parseInt(split[j]);
					}
				}
				s.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		if(level<1)
			return expThresholdsByGroupAndLevel[expgroups[pokenum]][1];
		else if(level>100)
			return expThresholdsByGroupAndLevel[expgroups[pokenum]][100];
		else
			return expThresholdsByGroupAndLevel[expgroups[pokenum]][level];
	}


	public static String getItemName(int itemnum){
		if(itemstrings==null)
			loadItemStrings();
		return itemstrings[itemnum][0];
	}

	public static ItemType getItemType(int itemnum){
		if(itemstrings==null)
			loadItemStrings();
		return ItemType.valueOf(itemstrings[itemnum][1].toUpperCase().replace(" ", ""));
	}

	public static boolean isHeldItem(int itemnum){
		if(itemstrings==null)
			loadItemStrings();
		if(itemstrings[itemnum][2].equals("N"))
			return false;
		return true;
	}

	public static String getItemDescription(int itemnum){
		if(itemstrings==null)
			loadItemStrings();
		String s=itemstrings[itemnum][3];
		if(s.equals("N/A"))
			return null;
		return s;
	}

	public static boolean isUsableInBattle(int itemnum){
		if(itemstrings==null)
			loadItemStrings();
		return itemstrings[itemnum][4].equals("Y");
	}

	public static boolean isUsableOutOfBattle(int itemnum){
		if(itemstrings==null)
			loadItemStrings();
		return !(itemstrings[itemnum][1].equals("Ball")||itemstrings[itemnum][1].equals("Escape"));
	}

	public static boolean isValidTM(int pokenum,int itemnum){
		if(validtms==null){
			try{
				File f=new File(Constants.PATH+"\\InitializeData\\bytm.txt");
				Scanner s=new Scanner(f);
				validtms=new ArrayList<ArrayList<Integer>>();
				while(s.hasNextLine()){
					ArrayList<Integer> tms=new ArrayList<Integer>();
					String line=s.nextLine();
					if(line.length()==0){
						validtms.add(tms);
						continue;
					}
					String[] split=line.split(",");
					for(int j=0;j<split.length;j++){
						tms.add(Integer.parseInt(split[j]));
					}
					validtms.add(tms);
				}
				s.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return validtms.get(pokenum).contains(itemnum);
	}

	public static int getItemLevel(int itemnum){
		if(itemlvls==null){
			try{
				File f=new File(Constants.PATH+"\\InitializeData\\itemlvls.txt");
				Scanner s=new Scanner(f);
				itemlvls=new int[Constants.NUM_ITEMS+1];
				for(int i=1;i<itemlvls.length;i++){
					itemlvls[i]=Integer.parseInt(s.nextLine());
				}
				s.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return itemlvls[itemnum];
	}

	public static int getLoot(int level,boolean elitetrainer){
		if(lootbylvl==null||lootlvlthresholds==null){
			try{
				lootbylvl=new int[Constants.NUM_ITEMS+1];
				lootlvlthresholds=new IntPair[Constants.NUM_LOOT_LVL_THRESHOLDS];
				File f=new File(Constants.PATH+"\\InitializeData\\lootlevels.txt");
				Scanner s=new Scanner(f);
				String[] split=s.nextLine().split(",,");
				for(int i=1;i<split.length;i++){
					lootbylvl[i]=Integer.parseInt(split[i]);
				}
				split=s.nextLine().split(",,");
				for(int i=0;i<split.length;i++){
					String curr=split[i];
					curr=curr.substring(1,curr.length()-1);
					String[] nums=curr.split(",");
					lootlvlthresholds[i]=new IntPair(Integer.parseInt(nums[0]),Integer.parseInt(nums[1]));
				}
				s.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		int indexcap=2;
		for(int i=1;i<lootlvlthresholds.length;i++){
			if(level>=lootlvlthresholds[i].getX())
				continue;
			indexcap=lootlvlthresholds[i].getY();
			break;
		}
		int itemid;
		do{
			itemid=lootbylvl[random.nextInt(indexcap)];
		}while((elitetrainer&&getItemLevel(itemid)<(2*level/3)));
		return itemid;
	}
	
	public static HashMap<MoveEffect,HashMap<String,String>> getMoveEffects(int movenum){
		if(moveeffects==null){
		try{
			File f=new File(Constants.PATH+"\\InitializeData\\moveeffects.txt");
			Scanner s=new Scanner(f);
			moveeffects=new String[Constants.NUM_MOVES+1];
			for(int i=1;i<moveeffects.length;i++){
				moveeffects[i]=s.nextLine();
			}
			s.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		}
		return getMoveEffectsMap(moveeffects[movenum]);
	}
	
	private static HashMap<MoveEffect,HashMap<String,String>> getMoveEffectsMap(String effectstring){
		HashMap<MoveEffect,HashMap<String,String>> effectsmap=new HashMap<MoveEffect,HashMap<String,String>>();
		String[] spliteffects=effectstring.split(";");
		for(String spliteffect:spliteffects){
			String[] components=spliteffect.split(",");
			try{
				MoveEffect effectname=MoveEffect.valueOf(components[0].substring(7));
				if(components.length>1){
					HashMap<String,String> componentsmap=new HashMap<String,String>();
					for(int i=1;i<components.length;i++){
						String[] component=components[i].split("=");
						componentsmap.put(component[0],component[1]);
					}
					effectsmap.put(effectname,componentsmap);
				}
				else
					effectsmap.put(effectname,null);
			}catch(Exception e){System.out.println("No move effect enum for "+components[0]);};
		}
		return effectsmap;
	}

	public static int[] readIntArray(String s){
		String[] sarray=s.substring(1,s.length()-1).split(",");
		int[] toReturn=new int[sarray.length];
		for(int i=0;i<sarray.length;i++){
			Scanner reader=new Scanner(sarray[i]);
			toReturn[i]=reader.nextInt();
			reader.close();
		}
		return toReturn;
	}

	private static void loadMoveStrings(){
		try{
			movestrings=new String[Constants.NUM_MOVES+1][3];
			File f=new File(Constants.PATH+"\\InitializeData\\movestrings.txt");
			Scanner s=new Scanner(f);
			for(int i=1;i<movestrings.length;i++){
				String line=s.nextLine();
				movestrings[i]=line.split(",");
			}
			s.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private static void loadItemStrings(){
		try{
			itemstrings=new String[Constants.NUM_ITEMS+1][5];
			File f=new File(Constants.PATH+"\\InitializeData\\itemstrings.txt");
			Scanner s=new Scanner(f);
			for(int i=1;i<itemstrings.length;i++){
				String line=s.nextLine();
				itemstrings[i]=line.split(",");
			}
			s.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private static void loadMoveNums(){
		try{
			movenums=new int[Constants.NUM_MOVES+1][4];
			File f=new File(Constants.PATH+"\\InitializeData\\movenums.txt");
			Scanner s=new Scanner(f);
			for(int i=1;i<movenums.length;i++){
				String line=s.nextLine();
				String[] split=line.split(",");
				for(int j=0;j<split.length;j++){
					movenums[i][j]=Integer.parseInt(split[j]);
				}
			}
			s.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
