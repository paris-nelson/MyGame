package Engines;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import Enums.EventName;
import Enums.LocationName;
import Enums.MapType;
import Enums.MusicTheme;
import Enums.PermCondition;
import Global.Constants;
import Global.GameData;
import Global.PlayerData;
import KeyListeners.MapKeyListener;
import Menus.PlayerMenu;
import Objects.IntPair;
import Objects.Location;
import Objects.Pokemon;
import Objects.Radio;
import Objects.Trainer;
import Objects.WildTrainer;
import acm.graphics.GImage;
import acm.graphics.GObject;
import acm.graphics.GPoint;

public class MapEngine {
	//turns off wild pokemon encounters for quick testing. Remove when done testing
	private static final boolean DEBUG=false;

	private static short[][] logicalmap;
	private static short gridx=0;
	private static short gridy=0;
	private static short gridxoffset=0;
	private static short gridyoffset=0;
	private static String playericonpath;
	private static GImage playericon;
	private static int repelstepsleft=0;
	private static int movementspeed;
	private static int stepstaken=0;

	public static void initialize(Location l){
		System.out.println("Initializing "+l.getName());
		System.out.println("Repel steps remaining: "+repelstepsleft);
		logicalmap=new short[Constants.LOGICAL_MAP_WIDTH][Constants.LOGICAL_MAP_HEIGHT];
		load();
		showMap(l,true);
		l.enter();
	}

	private static void load(){
		System.out.println("Loading map data");
		File f=new File("InitializeData\\mapsavefile.txt");
		try{
			Scanner s=new Scanner(f);
			gridx=s.nextShort();
			gridy=s.nextShort();
			gridxoffset=s.nextShort();
			gridyoffset=s.nextShort();
			s.nextLine();
			playericonpath=s.nextLine();
			playericon=new GImage(playericonpath);
			repelstepsleft=s.nextInt();
			s.close();
		}catch(Exception e){e.printStackTrace();}
	}

	public static void save(){
		File f=new File("InitializeData\\mapsavefile.txt");
		try{
			PrintWriter pw=new PrintWriter(f);
			pw.println(gridx);
			pw.println(gridy);
			pw.println(gridxoffset);
			pw.println(gridyoffset);
			pw.println(playericonpath);
			pw.println(repelstepsleft);
			pw.close();
		}catch(Exception e){e.printStackTrace();}
	}

	/**
	 * Load map based on location, and whether this is the initial map load or a change of maps. 
	 * @param location
	 * @param initial
	 */
	public static void showMap(Location location,boolean initial){
		clearMap();
		GUI gui=GameData.getGUI();
		MapType type=location.getType();
		gui.add(new GImage(Constants.PATH+"Sprites\\"+location.getType()+".png"));
		if(type==MapType.Johto||type==MapType.Kanto)
			movementspeed=Constants.MAIN_MAP_MOVEMENT_SPEED;
		else
			movementspeed=Constants.SUB_MAP_MOVEMENT_SPEED;
		//initial map load, just use the gridx/gridy coming from save file
		if(initial){
			gui.add(playericon,gridx*10+gridxoffset-playericon.getWidth()/2,gridy*10+gridyoffset-playericon.getHeight()/2);
			Radio radio=GameData.getRadio();
			if(type==MapType.TeamRocket)
				radio.changeTheme(MusicTheme.TeamRocket);
			else if(type==MapType.Gym)
				radio.changeTheme(MusicTheme.Gym);
			else if(type==MapType.Kanto||type==MapType.Johto)
				radio.changeTheme(MusicTheme.Map);
			else
				radio.changeTheme(MusicTheme.Cave);
		}
		//map has changed from johto/kanto to a cave/forest/etc type map, set gridx/gridy to be at entrance
		else if(type==MapType.Cave||type==MapType.Forest||type==MapType.TeamRocket||type==MapType.Gym||type==MapType.OlivineTower){
			addIconToPosition(Short.valueOf("3"),Short.valueOf("29"));
			Radio radio=GameData.getRadio();
			if(type==MapType.TeamRocket)
				radio.changeTheme(MusicTheme.TeamRocket);
			else if(type==MapType.Gym)
				radio.changeTheme(MusicTheme.Gym);
			else
				radio.changeTheme(MusicTheme.Cave);
		}
		//map has changed from cave/forest/etc to a johto/kanto type map, set gridx/gridy based on output coordinates of prev location
		else{
			//determine which endpoint we have exited into
			Location prevlocation=PlayerData.getPrevLocation();
			IntPair coordinates=null;
			if(location.getName()==prevlocation.getEndpoints().get(0))
				coordinates=prevlocation.getCoordinates().get(0);
			else
				coordinates=prevlocation.getCoordinates().get(1);
			System.out.println(coordinates.toString());
			addIconToPosition(Short.valueOf(coordinates.getX()+""),Short.valueOf(coordinates.getY()+""));
			GameData.getRadio().changeTheme(MusicTheme.Map);
		}
		loadLogicalMap(location);
		//		System.out.println(logicalmap.length);
		//		for(int y=0;y<logicalmap[0].length;y++){
		//			for(int x=0;x<logicalmap.length;x++){
		//				System.out.print(logicalmap[x][y]);
		//			}
		//			System.out.println();
		//		}
	}

	public static void incRepelSteps(int num){
		repelstepsleft+=num;
	}

	public static void setGridx(short gridx) {
		MapEngine.gridx = gridx;
	}

	public static void setGridy(short gridy) {
		MapEngine.gridy = gridy;
	}

	public static void setGridxoffset(short gridxoffset) {
		MapEngine.gridxoffset = gridxoffset;
	}

	public static void setGridyoffset(short gridyoffset) {
		MapEngine.gridyoffset = gridyoffset;
	}

	public static void changePlayerIcon(String newpath){
		playericonpath=newpath;
		if(playericon!=null){
			GUI gui=GameData.getGUI();
			GPoint point=playericon.getLocation();
			System.out.println(point.getX()+","+point.getY());
			gui.remove(playericon);
			playericon=new GImage(playericonpath);
			gui.add(playericon,point);
		}
		else
			save();
	}

	private static void movePlayer(int xdelta,int ydelta){
		playericon.move(xdelta,ydelta);
		if(repelstepsleft>0)
			repelstepsleft--;
		if(stepstaken<Constants.POISON_HP_LOSS_RATE)
			stepstaken++;
		if(stepstaken==Constants.POISON_HP_LOSS_RATE){
			stepstaken=0;
			for(Pokemon p:PlayerData.getParty()){
				if(!p.isFainted()){
					PermCondition condition=p.getPcondition();
					if(condition!=null&&(condition==PermCondition.Poison||condition==PermCondition.BadlyPoison))
						p.decHP(Constants.POISON_HP_LOSS_AMOUNT);
				}
			}
			GlobalEngine.updateLeadingPokemon();
		}
		int modifier=1;
		if(PlayerData.getLeadingPokemon().isHolding("Cleanse Tag"))
			modifier=3;
		if(!DEBUG&&GameData.getRandom().nextInt(Constants.WILD_ENCOUNTER_RATE*modifier)==0){
			Location ln=PlayerData.getLocation();
			//encounter legendary
			if(encounterLegend()){
				ArrayList<Integer> validids=new ArrayList<Integer>();
				if(ln.getType()==MapType.Johto){
					validids.add(243);
					validids.add(244);
					validids.add(245);
				}
				else if(ln.getType()==MapType.Kanto){
					validids.add(144);
					validids.add(145);
					validids.add(146);
				}
				for(int i=2;i>=0;i--){
					if(PlayerData.hasCaught(validids.get(i)))
						validids.remove(i);
				}
				if(validids.size()>0){
					int index=GameData.getRandom().nextInt(validids.size());
					Pokemon[] legend={new Pokemon(validids.get(index),40)};
					GlobalEngine.enterBattle(new WildTrainer(legend));
				}
			}
			//normal wild encounter
			else if(repelstepsleft==0||PlayerData.getLocation().getMinWildLevel()>=PlayerData.getLeadingPokemon().getLevel()){
				Pokemon[] wildteam=PlayerData.getLocation().encounterWildPokemon(PlayerData.getPartySize(),GameData.getTime());
				GlobalEngine.enterBattle(new WildTrainer(wildteam));
			}
		}
	}

	private static boolean encounterLegend(){
		Location ln=PlayerData.getLocation();
		return 	PlayerData.hasClearedEvent(EventName.UnleashLegends)
				&&(ln.getType()==MapType.Johto||ln.getType()==MapType.Kanto)
				&&GameData.getRandom().nextInt(Constants.LEGEND_ENCOUNTER_RATE)==0;
	}

	public static void moveLeft(){
		if(!playericonpath.contains("Left"))
			changePlayerIcon(playericonpath.replace("\\Right\\","\\Left\\"));
		if(gridxoffset>=movementspeed){
			movePlayer(-movementspeed,0);
			gridxoffset-=movementspeed;
		}
		else if(gridx>0){
			short nextid=logicalmap[gridx-1][gridy];
			//attempting to move onto invalid spot. Do nothing
			if(nextid<0)
				return;

			//moving within same location, nothing special to do
			if(nextid==GameData.getIDByLocationName(PlayerData.getLocation().getName())){
				movePlayer(-movementspeed,0);
				gridx--;
				gridxoffset=Short.valueOf(""+(10-movementspeed-gridxoffset));
			}
			//moving to a new location
			else if(nextid>=200){
				LocationName newln=GameData.getLocationNameByID(nextid);
				if(newln!=null&&PlayerData.hasMetRequirement(GameData.getLocationRequirement(newln))){
					gridx--;
					gridxoffset=Short.valueOf(""+(10-movementspeed-gridxoffset));
					PlayerData.changeLocation(newln);
					movePlayer(-movementspeed,0);
					changeLocation();
				}
			}
			//moving onto trainer, which means we're within the same location. Enter battle
			else if(nextid<=Constants.NUM_TRAINERS){
				movePlayer(-movementspeed,0);
				gridx--;
				gridxoffset=Short.valueOf(""+(10-movementspeed-gridxoffset));
				GlobalEngine.enterBattle(PlayerData.getLocation().getTrainerByID(nextid));
			}
		}
	}

	public static void moveRight(){
		if(!playericonpath.contains("Right"))
			changePlayerIcon(playericonpath.replace("\\Left\\","\\Right\\"));
		if(gridxoffset<(10-movementspeed)){
			movePlayer(movementspeed,0);
			gridxoffset+=movementspeed;
		}
		else if(gridx+1<logicalmap.length){
			short nextid=logicalmap[gridx+1][gridy];
			//attempting to move onto invalid spot. Do nothing
			if(nextid<0)
				return;

			//moving within same location, nothing special to do
			if(nextid==GameData.getIDByLocationName(PlayerData.getLocation().getName())){
				movePlayer(movementspeed,0);
				gridx++;
				gridxoffset=Short.valueOf(""+(gridxoffset+movementspeed-10));
			}
			//moving to a new location
			else if(nextid>=200){
				LocationName newln=GameData.getLocationNameByID(nextid);
				if(newln!=null&&PlayerData.hasMetRequirement(GameData.getLocationRequirement(newln))){
					gridx++;
					gridxoffset=Short.valueOf(""+(gridxoffset+movementspeed-10));
					PlayerData.changeLocation(newln);
					movePlayer(movementspeed,0);
					changeLocation();
				}
			}
			//moving onto trainer, which means we're within the same location. Enter battle
			else if(nextid<=Constants.NUM_TRAINERS){
				movePlayer(movementspeed,0);
				gridx++;
				gridxoffset=Short.valueOf(""+(gridxoffset+movementspeed-10));
				GlobalEngine.enterBattle(PlayerData.getLocation().getTrainerByID(nextid));
			}
		}
	}

	public static void moveUp(){
		if(gridyoffset>=movementspeed){
			movePlayer(0,-movementspeed);
			gridyoffset-=movementspeed;
		}
		else if(gridy>0){
			short nextid=logicalmap[gridx][gridy-1];
			//attempting to move onto invalid spot. Do nothing
			if(nextid<0)
				return;

			//moving within same location, nothing special to do
			if(nextid==GameData.getIDByLocationName(PlayerData.getLocation().getName())){
				movePlayer(0,-movementspeed);
				gridy--;
				gridyoffset=Short.valueOf(""+(10-movementspeed-gridxoffset));
			}
			//moving to a new location
			else if(nextid>=200){
				LocationName newln=GameData.getLocationNameByID(nextid);
				if(newln!=null&&PlayerData.hasMetRequirement(GameData.getLocationRequirement(newln))){
					gridy--;
					gridyoffset=Short.valueOf(""+(10-movementspeed-gridxoffset));
					PlayerData.changeLocation(newln);
					movePlayer(0,-movementspeed);
					changeLocation();
				}
			}
			//moving onto trainer, which means we're within the same location. Enter battle
			else if(nextid<=Constants.NUM_TRAINERS){
				movePlayer(0,-movementspeed);
				gridy--;
				gridyoffset=Short.valueOf(""+(10-movementspeed-gridxoffset));
				GlobalEngine.enterBattle(PlayerData.getLocation().getTrainerByID(nextid));
			}
		}
	}

	public static void moveDown(){
		if(gridyoffset<(10-movementspeed)){
			movePlayer(0,movementspeed);
			gridyoffset+=movementspeed;
		}
		else if(gridy+1<logicalmap[0].length){
			short nextid=logicalmap[gridx][gridy+1];
			//attempting to move onto invalid spot. Do nothing
			if(nextid<0)
				return;
			//moving within same location, nothing special to do
			if(nextid==GameData.getIDByLocationName(PlayerData.getLocation().getName())){
				movePlayer(0,movementspeed);
				gridy++;
				gridyoffset=Short.valueOf(""+(gridxoffset+movementspeed-10));
			}
			//moving to a new location
			else if(nextid>=200){
				LocationName newln=GameData.getLocationNameByID(nextid);
				if(newln!=null&&PlayerData.hasMetRequirement(GameData.getLocationRequirement(newln))){
					gridy++;
					gridyoffset=Short.valueOf(""+(gridxoffset+movementspeed-10));;
					PlayerData.changeLocation(newln);
					movePlayer(0,movementspeed);
					changeLocation();
				}
			}
			//moving onto trainer, which means we're within the same location. Enter battle
			else if(nextid<=Constants.NUM_TRAINERS){
				movePlayer(0,movementspeed);
				gridy++;
				gridyoffset=Short.valueOf(""+(gridxoffset+movementspeed-10));
				GlobalEngine.enterBattle(PlayerData.getLocation().getTrainerByID(nextid));
			}
		}
	}

	public static void openMenu(){
		MenuEngine.initialize(new PlayerMenu());
	}

	public static void takeControl(){
		GameData.getGUI().giveControl(new MapKeyListener());
	}

	public static void close(){
		GlobalEngine.giveUpControl();
		System.out.println("Closing map");
		save();
		PlayerData.save();
		PlayerData.getLocation().leave();
		clearMap();
		playericon=null;
	}

	/**
	 * Manages map engine portion of location change. 
	 * Precondition: PlayerData.changeLocation() has already been called
	 * @param newlocation
	 */
	public static void changeLocation(){
		Location prevlocation=PlayerData.getPrevLocation();
		Location currlocation=PlayerData.getLocation();
		System.out.println("Leaving "+prevlocation.getName());
		System.out.println("Entering "+currlocation.getName());
		prevlocation.leave();
		if(prevlocation.getType()!=currlocation.getType())
			MapEngine.showMap(currlocation,false);
		currlocation.enter();
	}

	public static void addIconToPosition(short x, short y){
		GameData.getGUI().add(playericon);
		setIconToPosition(x,y);
	}

	public static void setIconToPosition(short x, short y){
		gridx=x;
		gridy=y;
		gridxoffset=0;
		gridyoffset=0;
		if(playericon!=null)
			playericon.setLocation(gridx*10+gridxoffset-playericon.getWidth()/2,gridy*10+gridyoffset-playericon.getHeight()/2);
	}


	private static void clearMap(){
		System.out.println("Clearing Map");
		GUI gui=GameData.getGUI();
		GObject object=gui.getElementAt(1,1);
		while(object!=null&&object instanceof GImage){
			gui.remove(object);
			object=gui.getElementAt(1,1);
		}
		gui.remove(playericon);
	}

	private static void loadLogicalMap(Location location){
		MapType type=location.getType();
		if(type==MapType.Kanto||type==MapType.Johto)
			loadLogicalMapTemplate(type);
		else{
			logicalmap=new short[Constants.LOGICAL_MAP_WIDTH][Constants.LOGICAL_MAP_HEIGHT];
			for(int x=0;x<logicalmap.length;x++){
				Arrays.fill(logicalmap[x],Short.valueOf("-1"));
			}
			addLogicalMiddle(location.getID());
		}
		if(type==MapType.Cave||type==MapType.Forest||type==MapType.TeamRocket||type==MapType.Gym||type==MapType.OlivineTower)
			addLogicalMapEndpoints(location);

	}

	private static void loadLogicalMapTemplate(MapType type){
		File f=new File(Constants.PATH+"InitializeData\\"+type+"LogicalMap.txt");
		try{
			Scanner s=new Scanner(f);
			for(int y=0;y<Constants.LOGICAL_MAP_HEIGHT;y++){
				short[] xvals=readShortArray(s.nextLine());
				for(int x=0;x<Constants.LOGICAL_MAP_WIDTH;x++){
					logicalmap[x][y]=xvals[x];
				}
			}
			s.close();
		}catch(Exception e){e.printStackTrace();}
	}

	private static void addLogicalMiddle(short id){
		for(int x=3;x<Constants.LOGICAL_MAP_WIDTH-3;x++){
			logicalmap[x][Constants.LOGICAL_MAP_HEIGHT/2]=id;
		}
	}

	private static void addLogicalMapEndpoints(Location location){
		Location prev=PlayerData.getPrevLocation();
		ArrayList<LocationName> endpoints=location.getEndpoints();
		if(prev==null){
			if(endpoints.size()>0){
				addLogicalEntrance(endpoints.get(0));
				if(endpoints.size()>1)
					addLogicalExit(endpoints.get(1));
			}
		}
		else{
			if(endpoints.size()>0){
				LocationName ln=prev.getName();
				addLogicalEntrance(ln);
				if(endpoints.size()>1){
					if(endpoints.get(0)==ln)
						addLogicalExit(endpoints.get(1));
					else
						addLogicalExit(endpoints.get(0));
				}
			}
		}
	}

	private static void addLogicalEntrance(LocationName ln){
		short id=GameData.getIDByLocationName(ln);
		logicalmap[0][Constants.LOGICAL_MAP_HEIGHT/2]=id;
		logicalmap[1][Constants.LOGICAL_MAP_HEIGHT/2]=id;
		logicalmap[2][Constants.LOGICAL_MAP_HEIGHT/2]=id;
	}

	private static void addLogicalExit(LocationName ln){
		short id=GameData.getIDByLocationName(ln);
		logicalmap[Constants.LOGICAL_MAP_WIDTH-1][Constants.LOGICAL_MAP_HEIGHT/2]=id;
		logicalmap[Constants.LOGICAL_MAP_WIDTH-2][Constants.LOGICAL_MAP_HEIGHT/2]=id;
		logicalmap[Constants.LOGICAL_MAP_WIDTH-3][Constants.LOGICAL_MAP_HEIGHT/2]=id;
	}

	public static void removeTrainerFromMap(Trainer t,short locationid){
		GImage image=t.getImage();
		int width=(int)image.getWidth();
		int height=(int)image.getHeight();
		int tx=t.getX();
		int ty=t.getY();
		GameData.getGUI().remove(image);
		for(int x=t.getX();x<tx+width;x++){
			for(int y=t.getY();y<ty+height;y++){
				logicalmap[x/10][y/10]=locationid;
			}
		}
	}

	public static void addTrainerToMap(Trainer t){
		GImage image=t.getImage();
		int width=(int)image.getWidth();
		int height=(int)image.getHeight();
		int tx=t.getX();
		int ty=t.getY();
		GameData.getGUI().add(image);
		for(int x=t.getX();x<tx+width;x++){
			for(int y=t.getY();y<ty+height;y++){
				logicalmap[x/10][y/10]=t.getID();
			}
		}
	}

	public static short[] readShortArray(String s){
		String[] sarray=s.substring(1,s.length()-1).split(",");
		short[] toReturn=new short[sarray.length];
		for(int i=0;i<sarray.length;i++){
			Scanner reader=new Scanner(sarray[i]);
			toReturn[i]=reader.nextShort();
			reader.close();
		}
		return toReturn;
	}

}
