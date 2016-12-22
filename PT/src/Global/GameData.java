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
import Enums.Requirement;
import Enums.Stat;
import Enums.Time;
import Enums.Type;
import Objects.IntPair;
import Objects.Move;

public class GameData {
	private static Random random;
	private static String[][] movestrings=new String[252][3];
	private static int[][] movenums=new int[252][4];
	private static String[][] itemstrings=new String[Constants.NUM_ITEMS+1][5];
	private static int[] itemlvls=new int[Constants.NUM_ITEMS+1];
	private static int[] lootbylvl=new int[Constants.NUM_ITEMS+1];
	private static IntPair[] lootlvlthresholds=new IntPair[Constants.NUM_LOOT_LVL_THRESHOLDS];
	private static String[] pokenames=new String[252];
	private static int[][] stats=new int[252][6];
	private static int[] expgroups=new int[252];
	private static int[] catchrates=new int[252];
	private static Type[][] types=new Type[252][2];
	private static int[] basexps=new int[252];
	private static ArrayList<ArrayList<String>> evolutionconditions=new ArrayList<ArrayList<String>>();
	private static ArrayList<ArrayList<Integer>> evolutionnums=new ArrayList<ArrayList<Integer>>();
	private static int[][] expThresholdsByGroupAndLevel=new int[4][101];
	private static ArrayList<ArrayList<Integer>> validtms=new ArrayList<ArrayList<Integer>>();
	private static ArrayList<Integer>[][] moveslearned=new ArrayList[252][101];
	private static Move[][][] movesetsbylvl=new Move[252][101][4];
	private static ArrayList<Short> locationids=new ArrayList<Short>();
	private static ArrayList<LocationName> locationnames=new ArrayList<LocationName>();
	private static ArrayList<Requirement> locationreqs=new ArrayList<Requirement>();
	private static double[] statstages=new double[13];
	private static Map<Type,HashMap<Type,Double>> typechart=new HashMap<Type,HashMap<Type,Double>>();
	private static int[] critstages=new int[7];
	private static double[] accevastages=new double[13];
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
			f=new File("InitializeData\\accevastages.txt");
			s=new Scanner(f);
			for(int i=0;i<accevastages.length;i++){
				accevastages[i]=Double.parseDouble(s.nextLine());
			}
			s.close();
			f=new File("InitializeData\\critstages.txt");
			s=new Scanner(f);
			System.out.println(f.exists());
			for(int i=0;i<critstages.length;i++){
				critstages[i]=Integer.parseInt(s.nextLine());
			}
			s.close();
			f=new File("InitializeData\\typechart.txt");
			s=new Scanner(f);
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
			f=new File("InitializeData\\statstages.txt");
			s=new Scanner(f);
			for(int i=0;i<statstages.length;i++){
				statstages[i]=s.nextDouble();
			}
			s.close();
			f=new File("InitializeData\\gamesavefile.txt");
			s=new Scanner(f);
			time=Time.valueOf(s.nextLine());
			s.close();
			f=new File("InitializeData\\locationreq.txt");
			s=new Scanner(f);
			short count=200;
			while(s.hasNextLine()){
				locationnames.add(LocationName.valueOf(s.next()));
				locationids.add(count);
				locationreqs.add(Requirement.valueOf(s.next()));
				count++;
			}
			s.close();
			f=new File("InitializeData\\basexp.txt");
			s=new Scanner(f);
			for(int i=1;i<basexps.length;i++){
				basexps[i]=Integer.parseInt(s.nextLine());
			}
			s.close();
			f=new File("InitializeData\\itemlvls.txt");
			s=new Scanner(f);
			for(int i=1;i<itemlvls.length;i++){
				itemlvls[i]=Integer.parseInt(s.nextLine());
			}
			s.close();
			f=new File("InitializeData\\bytm.txt");
			s=new Scanner(f);
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
			f=new File("InitializeData\\movestrings.txt");
			s=new Scanner(f);
			for(int i=1;i<movestrings.length;i++){
				String line=s.nextLine();
				movestrings[i]=line.split(",");
			}
			s.close();
			f=new File("InitializeData\\movenums.txt");
			s=new Scanner(f);
			for(int i=1;i<movenums.length;i++){
				String line=s.nextLine();
				String[] split=line.split(",");
				for(int j=0;j<split.length;j++){
					movenums[i][j]=Integer.parseInt(split[j]);
				}
			}
			s.close();
			f=new File("InitializeData\\itemstrings.txt");
			s=new Scanner(f);
			for(int i=1;i<itemstrings.length;i++){
				String line=s.nextLine();
				itemstrings[i]=line.split(",");
			}
			s.close();
			f=new File("InitializeData\\lootlevels.txt");
			s=new Scanner(f);
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
			f=new File("InitializeData\\stats.txt");
			s=new Scanner(f);
			for(int i=1;i<stats.length;i++){
				split=s.nextLine().split(",");
				for(int j=0;j<split.length;j++){
					stats[i][j]=Integer.parseInt(split[j]);
				}
			}
			s.close();
			f=new File("InitializeData\\expgroups.txt");
			s=new Scanner(f);
			for(int i=1;i<expgroups.length;i++){
				expgroups[i]=Integer.parseInt(s.nextLine());
			}
			s.close();
			f=new File("InitializeData\\expthreshbygroup.txt");
			s=new Scanner(f);
			for(int i=0;i<expThresholdsByGroupAndLevel.length;i++){
				String line=s.nextLine();
				split=line.split(",");
				for(int j=0;j<split.length;j++){
					expThresholdsByGroupAndLevel[i][j]=Integer.parseInt(split[j]);
				}
			}
			s.close();
			f=new File("InitializeData\\evolvecond.txt");
			s=new Scanner(f);
			evolutionconditions.add(new ArrayList<String>());
			while(s.hasNextLine()){
				ArrayList<String> conditions=new ArrayList<String>();
				String line=s.nextLine();
				if(line.equals("DNE")){
					evolutionconditions.add(conditions);
					continue;
				}
				split=line.split("/");
				for(int i=0;i<split.length;i++){
					conditions.add(split[i]);
				}
				evolutionconditions.add(conditions);
			}
			s.close();
			f=new File("InitializeData\\evolveresult.txt");
			s=new Scanner(f);
			evolutionnums.add(null);
			while(s.hasNextLine()){
				String line=s.nextLine();
				if(line.length()==0){
					evolutionnums.add(null);
					continue;
				}
				ArrayList<Integer> nums=new ArrayList<Integer>();
				split=line.split("/");
				for(int i=0;i<split.length;i++){
					nums.add(Integer.parseInt(split[i]));
				}
				evolutionnums.add(nums);
			}
			s.close();
			f=new File("InitializeData\\types.txt");
			s=new Scanner(f);
			for(int i=1;i<types.length;i++){
				String line=s.nextLine();
				split=line.split(",");
				Type[] curr=new Type[2];
				curr[0]=Type.valueOf(split[0]);
				if(split.length==2){
					curr[1]=Type.valueOf(split[1]);
				}
				types[i]=curr;
			}
			s.close();
			f=new File("InitializeData\\bylevel.txt");
			s=new Scanner(f);
			for(int i=1;i<252;i++){
				String line=s.nextLine();
				String[] levels=line.split(",,");
				ArrayList<Integer>[] level=new ArrayList[101];
				for(int j=0;j<level.length;j++){
					if(levels[j].equals("null"))
						level[j]=null;
					else{
						ArrayList<Integer> movenums=new ArrayList<Integer>();
						String str=levels[j];
						str=str.substring(1,str.length()-1);
						String[] moves=str.split(",");
						for(String move:moves){
							movenums.add(Integer.parseInt(move));
						}
						level[j]=movenums;
					}
				}
				moveslearned[i]=level;
			}
			for(int i=1;i<252;i++){
				for(int j=1;j<101;j++){
					movesetsbylvl[i][j]=setMovesetByLevel(i,j);
				}
			}
		}
		catch(Exception e){e.printStackTrace();}
	}

	public static double getAccEvaStageMultiplier(Stat stat,int stage){
		//maxed accuracy gets highest chance of hitting
		if(stat==Stat.Accuracy)
			return accevastages[stage+6];
		//maxed evasion gets lowest chance of hitting
		else if(stat==Stat.Evasion)
			return accevastages[6-stage];
		return 0;
	}

	public static int getCritRatio(int stage){
		return critstages[stage];
	}

	public static double getStatStageMultiplier(int stage){
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
		return typechart.get(attacktype).get(targettype);
	}

	public static int getMovePP(int movenum){
		return movenums[movenum][0];
	}

	public static int getMoveMaxPP(int movenum){
		return movenums[movenum][1];
	}

	public static int getMovePower(int movenum){
		return movenums[movenum][2];
	}

	public static double getMoveAccuracy(int movenum){
		return movenums[movenum][3]/100;
	}

	public static int getMoveNum(String movename){
		for(int i=1;i<movestrings.length;i++){
			if(movestrings[i][0].equals(movename))
				return i;
		}
		return -1;
	}

	public static String getMoveName(int movenum){
		return movestrings[movenum][0];
	}

	public static Type getMoveType(int movenum){
		return Type.valueOf(movestrings[movenum][1]);
	}

	public static String getMoveDescription(int movenum){
		return movestrings[movenum][2];
	}

	public static int[] getBaseStats(int pokenum){
		return stats[pokenum];
	}

	public static Move[] getMovesetByLevel(int pokenum,int level){
		return movesetsbylvl[pokenum][level];
	}

	private static Move[] setMovesetByLevel(int pokenum,int level){
		Move[] moveset=new Move[4];
		int count=0;
		out:
			for(int i=level;i>0;i--){
				ArrayList<Integer> list=moveslearned[pokenum][i];
				if(list!=null){
					for(int j=list.size()-1;j>=0;j--){
						int num=list.get(j);
						if(containsMoveNum(moveset,num))
							continue;
						moveset[count]=new Move(num);
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

	public static String getPokeName(int pokenum){
		if(pokenames[pokenum]!=null)
			return pokenames[pokenum];
		try{
			File f=new File("InitializeData\\pokenames.txt");
			Scanner s=new Scanner(f);
			for(int i=1;i<pokenames.length;i++){
				pokenames[i]=s.nextLine();
			}
			s.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return pokenames[pokenum];
	}

	public static ArrayList<Type> getPokeTypes(int pokenum){
		ArrayList<Type> toReturn=new ArrayList<Type>();
		Type[] temp=types[pokenum];
		toReturn.add(temp[0]);
		if(temp.length==2)
			toReturn.add(temp[1]);
		return toReturn;
	}

	public static int getCatchRate(int pokenum){
		if(catchrates[pokenum]>0)
			return catchrates[pokenum];
		File f=new File(Constants.PATH+"InitializeData\\catchrates.txt");
		Scanner s=null;
		try {
			s = new Scanner(f);
			for(int i=1;i<pokenum;i++){
				s.nextLine();
			}
			s.close();
		}catch(FileNotFoundException e){e.printStackTrace();}
		catchrates[pokenum]=Integer.parseInt(s.nextLine());
		return catchrates[pokenum];
	}

	public static ArrayList<String> getEvolutionConditions(int pokenum){
		return evolutionconditions.get(pokenum);
	}


	public static int getEvolutionNum(int pokenum,String condition){
		return evolutionnums.get(pokenum).get(evolutionconditions.get(pokenum).indexOf(condition));
	}

	public static int getExpThreshold(int pokenum,int level){
		if(level<1)
			return expThresholdsByGroupAndLevel[expgroups[pokenum]][1];
		else if(level>100)
			return expThresholdsByGroupAndLevel[expgroups[pokenum]][100];
		else
			return expThresholdsByGroupAndLevel[expgroups[pokenum]][level];
	}


	public static String getItemName(int itemnum){
		return itemstrings[itemnum][0];
	}

	public static ItemType getItemType(int itemnum){
		return ItemType.valueOf(itemstrings[itemnum][1].toUpperCase().replace(" ", ""));
	}

	public static boolean isHeldItem(int itemnum){
		if(itemstrings[itemnum][2].equals("N"))
			return false;
		return true;
	}

	public static String getItemDescription(int itemnum){
		String s=itemstrings[itemnum][3];
		if(s.equals("N/A"))
			return null;
		return s;
	}

	public static boolean isUsableInBattle(int itemnum){
		return itemstrings[itemnum][4].equals("Y");
	}

	public static boolean isUsableOutOfBattle(int itemnum){
		return !(itemstrings[itemnum][1].equals("Ball")||itemstrings[itemnum][1].equals("Escape"));
	}

	public static boolean isValidTM(int pokenum,int itemnum){
		return validtms.get(pokenum).contains(itemnum);
	}

	public static int getItemLevel(int itemnum){
		return itemlvls[itemnum];
	}

	public static int getLoot(int level,boolean elitetrainer){
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
		}while((elitetrainer&&itemlvls[itemid]<(2*level/3)));
		return itemid;
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

}
