package Objects;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import Engines.GlobalEngine;
import Engines.MapEngine;
import Engines.MenuEngine;
import Enums.EventName;
import Enums.LocationName;
import Enums.MapType;
import Enums.Time;
import Global.Constants;
import Global.GameData;
import Global.PlayerData;
import Menus.Menu;
import Menus.TownMenu;

public class Location {
	private short id;
	private LocationName name;
	private ArrayList<Trainer> trainers;
	private EventName event;
	private boolean haswilds;
	private MapType type;

	//when leaving a cave/forest/etc, what locations do you end up in
	private ArrayList<LocationName> endpoints;
	//when leaving a cave/forest/etc, where should player icon be moved to; in the case of a town/city, it's the coordinates the icon is
	//moved to when entering this place
	private ArrayList<IntPair> coordinates;

	private int lvlmin;
	private int lvlmax;
	private int[] mornwild;
	private int[] daywild;
	private int[] nightwild;

	private Menu menu;


	public Location(LocationName name){
		this.name=name;
		loadLocationData();
	}

	public void enter(){
		activateTrainers();
		if(event!=null&&!PlayerData.hasClearedEvent(event))
			GlobalEngine.triggerEvent(event);
		else if(menu!=null)
			MenuEngine.initialize(menu);
		else
			MapEngine.takeControl();
	}

	public void leave(){
		deactivateTrainers();
	}

	public void deactivateTrainers(){
		for(Trainer t:trainers)
			MapEngine.removeTrainerFromMap(t,id);
	}

	public void activateTrainers(){
		for(Trainer t:trainers){
			if(!PlayerData.hasBeatenTrainer(t.getID())||t.getName().startsWith("Elite Four ")||t.getName().equals("Red"))
				MapEngine.addTrainerToMap(t);
		}
	}

	public void reactivateTrainers(){
		for(Trainer t:trainers){
			if(PlayerData.hasBeatenTrainer(t.getID())&&!(t instanceof EliteTrainer))
				MapEngine.addTrainerToMap(t);
		}
	}

	public int getMinWildLevel(){
		return lvlmin;
	}

	public EventName getEvent(){
		return event;
	}

	public short getID(){
		return id;
	}

	public MapType getType(){
		return type;
	}

	public ArrayList<LocationName> getEndpoints(){
		return endpoints;
	}

	public ArrayList<IntPair> getCoordinates(){
		return coordinates;
	}

	private void loadLocationData(){
		System.out.println("Loading Location "+name.toString());
		trainers=new ArrayList<Trainer>();
		endpoints=new ArrayList<LocationName>();
		coordinates=new ArrayList<IntPair>();
		File f=new File(Constants.PATH+"LocationData\\"+name.toString()+".txt");
		try{
			Scanner s=new Scanner(f);
			s.next();//ID: 
			id=s.nextShort();
			s.next();//Type: 
			type=MapType.valueOf(s.next());
			String line=s.nextLine().trim();
			if(line.length()>0){
				if(type==MapType.Cave||type==MapType.Forest||type==MapType.TeamRocket||type==MapType.Gym||type==MapType.OlivineTower){
					String[] ends=line.split(" ");
					if(ends.length>0){
						endpoints.add(LocationName.valueOf(ends[0]));
						coordinates.add(IntPair.readIn(ends[1]));
						if(ends.length>2){
							endpoints.add(LocationName.valueOf(ends[2]));
							coordinates.add(IntPair.readIn(ends[3]));
						}
					}
				}
				//Town or city coordinates
				else{
					coordinates.add(IntPair.readIn(line));
				}	
			}
			s.next();//Event: 
			String curr=s.next();
			if(curr.equals("N/A"))
				event=null;
			else
				event=EventName.valueOf(curr);
			s.nextLine();//Trainers: 
			curr=s.next();
			while(!curr.equals("End")){
				if(curr.equals("Trainer:"))
					trainers.add(Trainer.readInTrainer(s));
				if(curr.equals("EliteTrainer:"))
					trainers.add(EliteTrainer.readInTrainer(s));
				curr=s.next();
			}
			s.nextLine();// Trainers
			curr=s.nextLine();
			if(curr.endsWith("N/A"))
				haswilds=false;
			else{
				haswilds=true;
				String[] split=curr.substring(14).split(" ");
				lvlmin=Integer.parseInt(split[0]);
				lvlmax=Integer.parseInt(split[1]);
				mornwild=GameData.readIntArray(s.nextLine());
				daywild=GameData.readIntArray(s.nextLine());
				nightwild=GameData.readIntArray(s.nextLine());
			}
			s.nextLine();//Menu: 
			menu=TownMenu.readInMenu(s);
		}
		catch(Exception e){
			System.out.println(name);
			e.printStackTrace();
			event=null;
		}
	}

	public LocationName getName(){
		return name;
	}

	public Menu getMenu(){
		return menu;
	}

	public ArrayList<Trainer> getTrainers(){
		return trainers;
	}

	public Trainer getTrainerByID(short id){
		for(Trainer t:trainers){
			if(t.getID()==id)
				return t;
		}
		return null;
	}

	public boolean hasWilds(){
		return haswilds;
	}

	public Pokemon[] encounterWildPokemon(int partysize,Time time){
		Pokemon[] wilds=new Pokemon[partysize];
		Random rand=GameData.getRandom();
		int lvlrange=lvlmax-lvlmin+1;
		for(int i=0;i<partysize;i++){
			int num=0;
			if(time==Time.Morning)
				num=mornwild[rand.nextInt(100)];
			else if(time==Time.Day)
				num=daywild[rand.nextInt(100)];
			else
				num=nightwild[rand.nextInt(100)];
			wilds[i]=new Pokemon(num,rand.nextInt(lvlrange)+lvlmin);
		}
		return wilds;
	}
}
