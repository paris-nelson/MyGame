package Global;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import Enums.EventName;
import Enums.LocationName;
import Enums.Requirement;
import Objects.Location;
import Objects.Move;
import Objects.Pokemon;

public class PlayerData {

	private static ArrayList<Pokemon> party=new ArrayList<Pokemon>();
	private static int[] inventory=new int[Constants.NUM_ITEMS+1];
	private static boolean[] hasCaught=new boolean[252];
	private static Location location=null;
	private static Location prevlocation=null;
	private static String name;
	private static boolean[] trainersbeaten=new boolean[Constants.NUM_TRAINERS+1];
	private static HashMap<EventName, Boolean> eventscleared=new HashMap<EventName, Boolean>();
	private static HashMap<Requirement, Boolean> requirementsmet=new HashMap<Requirement, Boolean>();
	private static int money=0;
	private static Pokemon leadingpokemon=null;

	public static boolean hasClearedEvent(EventName name){
		if(eventscleared.containsKey(name))
			return eventscleared.get(name);
		return false;
	}

	public static boolean hasMetRequirement(Requirement req){
		return requirementsmet.get(req)!=null;
	}

	public static void markRequirementMet(Requirement req){
		requirementsmet.put(req, true);
	}

	public static void markEventCleared(EventName name){
		eventscleared.put(name,true);
	}

	public static boolean hasBeatenTrainer(int id){
		return trainersbeaten[id];
	}

	public static void markTrainerBeaten(int id){
		trainersbeaten[id]=true;
	}


	public static Location getLocation() {
		return location;
	}

	public static Location getPrevLocation(){
		return prevlocation;
	}

	public static void setLocation(Location newlocation) {
		location=newlocation;
	}

	public static void setPrevLocation(Location newprevlocation){
		prevlocation=newprevlocation;
	}

	public static String getName() {
		return name;
	}
	
	public static int getMoney(){
		return money;
	}
	
	public static void gainMoney(int gain){
		money+=gain;
		if(money>Constants.MAX_MONEY)
			money=Constants.MAX_MONEY;
	}
	
	public static void loseMoney(int loss){
		money-=loss;
		if(money<0)
			money=0;
	}

	public static void changeLocation(LocationName ln){
		if(ln==prevlocation.getName()){
			Location temp=location;
			PlayerData.setLocation(prevlocation);
			PlayerData.setPrevLocation(temp);
		}
		else{
			PlayerData.setPrevLocation(location);
			PlayerData.setLocation(new Location(ln));
		}
	}

	public static int getPartySize(){
		return party.size();
	}

	/**
	 * Attempts to add the given pokemon to the player's party. Fails if party is full, putting pokemon into PC instead
	 * @param p
	 * @return: false if party is already full and pokemon is put in PC, true otherwise
	 */
	public static boolean addPokemonToParty(Pokemon p){
		if(party.size()==6){
			addToPC(p);
			return false;
		}
		party.add(p);
		return true;
	}

	/**
	 * Attempts to remove the pokemon. Will not remove if the party only has 1 pokemon (cannot have an empty party).
	 * @param p
	 * @return true if the pokemon was removed, false if party size is 1 or if pokemon is not in party
	 */
	public static boolean removePokemonFromParty(Pokemon p){
		int size=party.size();
		if(size==1)
			return false;
		party.remove(p);
		if(party.size()==size)
			return false;
		return true;
	}
	
	public static Pokemon getLeadingPokemon(){
		return leadingpokemon;
	}
	
	public static void setLeadingPokemon(Pokemon newleader){
		leadingpokemon=newleader;
	}

	public static void replacePokemonInParty(Pokemon leaver,Pokemon joiner){
		removePokemonFromParty(leaver);
		addPokemonToParty(joiner);
	}

	public static ArrayList<Pokemon> getParty(){
		return party;
	}

	public static boolean hasMaxOfItem(int itemnum){
		return inventory[itemnum]==Constants.MAX_ITEM_QUANTITY;
	}

	public static int getItemQuantity(int itemnum){
		return inventory[itemnum];
	}

	/**
	 * Attempts to remove the given number of a given item from inventory. IF the player
	 * doesn't have that many of that item, the method will fail
	 * @param itemnum: the id of the item to be removed
	 * @param count: the number to be removed
	 * @return: true if enough to remove, false otherwise
	 */
	public static boolean removeItem(int itemnum,int count){
		int currnum=inventory[itemnum];
		if(currnum<count)
			return false;
		currnum-=count;
		inventory[itemnum]=currnum;
		return true;
	}

	/**
	 * Attempts to add the given number of a given item to inventory. If the player
	 * has max quantity of that item, method will fail. If this method would increase
	 * the count beyond the max quantity, it sets it to max quantity instead
	 * @param itemnum: the id of the item to be added
	 * @param count: the number to be added
	 * @return: true if any items added, false otherwise
	 */
	public static boolean addItem(int itemnum,int count){
		int currnum=inventory[itemnum];
		if(currnum==Constants.MAX_ITEM_QUANTITY)
			return false;
		currnum+=count;
		if(currnum>Constants.MAX_ITEM_QUANTITY)
			currnum=Constants.MAX_ITEM_QUANTITY;
		inventory[itemnum]=currnum;
		return true;
	}

	public static void addToCaught(int pokenum){
		hasCaught[pokenum]=true;
	}

	public static boolean hasCaught(int pokenum){
		return hasCaught[pokenum];
	}

	public static void save(){
		File f=new File("InitializeData\\playersavefile.txt");
		try{
			PrintWriter out=new PrintWriter(f);
			out.println(name);
			for(Pokemon p:party){
				out.println(p);
			}
			out.println(Arrays.toString(inventory));
			out.println(Arrays.toString(hasCaught));
			out.println(prevlocation.getName().toString());
			out.println(location.getName().toString());
			out.println(Arrays.toString(trainersbeaten));
			out.println(eventscleared);
			out.println(requirementsmet);
			out.println(money);
			out.println(party.indexOf(leadingpokemon));
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void load(){
		File f=new File("InitializeData\\playersavefile.txt");
		try{
			Scanner reader=new Scanner(f);
			name=reader.nextLine();
			String line=reader.nextLine();
			do{
				party.add(Pokemon.readInPokemon(line));
				line=reader.nextLine();
			}while(line.startsWith("Pokemon:"));
			inventory=GameData.readIntArray(line);
			hasCaught=readBoolArray(reader.nextLine());
			prevlocation=new Location(LocationName.valueOf(reader.nextLine()));
			location=new Location(LocationName.valueOf(reader.nextLine()));
			trainersbeaten=readBoolArray(reader.nextLine());
			eventscleared=readEventsMap(reader.nextLine());
			requirementsmet=readRequirementsMap(reader.nextLine());
			money=Integer.parseInt(reader.nextLine());
			leadingpokemon=party.get(Integer.parseInt(reader.nextLine()));
			reader.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private static HashMap<EventName,Boolean> readEventsMap(String s){
		HashMap<EventName,Boolean> map=new HashMap<EventName,Boolean>();
		s=s.substring(1,s.length()-1);
		if(s.length()==0)
			return map;
		String[] pairs=s.split(", ");
		for(String pair:pairs){
			String[] split=pair.split("=");
			map.put(EventName.valueOf(split[0]),Boolean.parseBoolean(split[1]));
		}
		return map;
	}

	private static HashMap<Requirement,Boolean> readRequirementsMap(String s){
		HashMap<Requirement,Boolean> map=new HashMap<Requirement,Boolean>();
		s=s.substring(1,s.length()-1);
		if(s.length()==0)
			return map;
		String[] pairs=s.split(", ");
		for(String pair:pairs){
			String[] split=pair.split("=");
			map.put(Requirement.valueOf(split[0]),Boolean.parseBoolean(split[1]));
		}
		return map;
	}

	private static boolean[] readBoolArray(String s){
		String[] sarray=s.substring(1,s.length()-1).split(",");
		boolean[] toReturn=new boolean[sarray.length];
		for(int i=0;i<sarray.length;i++){
			Scanner reader=new Scanner(sarray[i]);
			toReturn[i]=reader.nextBoolean();
			reader.close();
		}
		return toReturn;
	}
	
	public static void healParty(){
		for(Pokemon p:party){
			p.revive();
			p.removePcondition();
			p.restoreHP();
			for(Move m:p.getMoveSet()){
				m.restorePP();
			}
		}
	}
	
	public static void addNewPokemon(Pokemon p){
		p.hasBeenCaught();
		addToCaught(p.getNum());
		addPokemonToParty(p);
	}

	public static void addToPC(Pokemon p){
		File f=new File("PCData\\"+p.getNum()+".txt");
		FileWriter fw;
		BufferedWriter bw;
		PrintWriter out;
		try{
			fw = new FileWriter(f, true);
			bw = new BufferedWriter(fw);
			out = new PrintWriter(bw);
			out.println(p);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Attempts to remove the pokemon from the pc file. If pokemon is not found, nothing happens, otherwise file is rewritten without given pokemon.
	 * This method will compare the given pokemon.toString to the strings found in file. It is technically possible that there will be more than one 
	 * pokemon with the same string (though incredibly rare), this method will only remove the first such match it finds.
	 * @param p
	 */
	public static void removeFromPC(Pokemon p){
		File f=new File(Constants.PATH+"PCData\\"+p.getNum()+".txt");
		try{
			Scanner s=new Scanner(f);
			ArrayList<String> entries=new ArrayList<String>();
			boolean found=false;
			while(s.hasNextLine()){
				String line=s.nextLine().trim();
				if(!found&&line.equals(p.toString())){
					found=true;
					continue;
				}
				entries.add(line);
			}
			s.close();
			if(found){
				System.out.println("Removing "+p.getName());
				PrintWriter pw=new PrintWriter(f);
				for(String entry:entries)
					pw.println(entry);
				pw.close();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
