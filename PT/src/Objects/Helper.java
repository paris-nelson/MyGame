 package Objects;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import Engines.GlobalEngine;
import Global.Constants;
import Global.GameData;


public class Helper {
	
	public static void main(String[] args) throws IOException{
		
		GlobalEngine.initialize();
		
		//TODO: make global player like GameData.getGUI() which plays/pauses tracks.
		//Want separate tracks for normal map, gym, elite four, battle, etc. Might have playlist of songs that play on
		//shuffle or just one track. swap logic can be handled in MapEngine.changeLocation and GlobalEngine.enterBattle
		//and either BattleEngine.close()/BattleEngine.toMap() or MapEngine.initialize()
		//TODO: Add move info to item descrips for tms/hms
		//TODO: Fix move descrips
//		BasicPlayer player = new BasicPlayer();
//		System.out.println(player.getStatus());
//		try {
//		    player.open(new URL("file:////"+Constants.PATH+"\\LR_371.mp3"));
//		    player.play();
//		    System.out.println(player.getStatus());
//		} catch (Exception e) {
//		    e.printStackTrace();
//		}
//		long start=System.nanoTime();
//		while(System.nanoTime()-start<Long.valueOf("5000000000")){
//			
//		}
//		System.out.println(player.getStatus());
//		try{
//		player.stop();
//		} catch (Exception e) {
//		    e.printStackTrace();
//		}
//		System.out.println(player.getStatus());
//		DialogEngine.initialize(EventName.RocketEncounter1);
		
//		Pokemon p=new Pokemon(120,5);
//		System.out.println(GlobalEngine.evolve(p,"Water"));
//		System.out.println(p.getNum());
//		MapEngine.initialize(PlayerData.getLocation());

		
//		File f=new File(Constants.PATH+"\\InitializeData\\moveranges.txt");
//		PrintWriter pw=new PrintWriter(f);
//		File f2=new File(Constants.PATH+"\\InitializeData\\moveeffects.txt");
//		Scanner s=new Scanner(f2);
//		for(int i=0;i<251;i++){
//			String line=s.nextLine();
//			if(line.contains("Effect=Buff")||line.contains("Effect=Nerf"))
//				pw.println("Selection=Single,Range=1");
//			else
//				pw.println("Selection=,Range=");
//		}
//		pw.close();
//		ArrayList<String> strings=new ArrayList<String>();
//		File f=new File("InitializeData\\prevolutions.txt");
//		Scanner s=new Scanner(f);
//		while(s.hasNextLine()){
//			String line=s.nextLine();
//			if(!strings.contains(line))
//				strings.add(line);
//		}
//		System.out.println(strings.size());
		
//		getPokemonThatLearnMove(GameData.getMoveNum("Pay Day"));
//		getPokemonThatLearnMove(GameData.getMoveNum("Whirlwind"));
//		getPokemonThatLearnMove(GameData.getMoveNum("Recover"));
		//getPokemonThatLearnMove(GameData.getMoveNum("Protect"));
		//getPokemonThatLearnMove(GameData.getMoveNum("Endure"));
		
		
		
//		File f=new File(Constants.PATH+"\\InitializeData\\moveeffects.txt");
//		Scanner s=new Scanner(f);
//		ArrayList<String> lines=new ArrayList<String>();
//		int i=0;
//		while(s.hasNextLine()){
//			String line=s.nextLine();
//			i++;
//			if(GameData.getMovePower(i)!=-1)
//				line="Effect=Damage,Power="+GameData.getMovePower(i)+";"+line;
//			lines.add(line);
//		}
//		
//		File f2=new File(Constants.PATH+"\\InitializeData\\moveeffects.txt");
//		PrintWriter writer=new PrintWriter(f2);
//		for(String line:lines){
//			writer.println(line);
//		}
//		writer.close();
		
//		System.out.println(System.getProperty("user.dir"));
		
//		for(int i=1;i<Constants.NUM_MOVES+1;i++)
//			GameData.getMoveEffects(i);
		
//		for(int i=0;i<10;i++){
//			GImage rock=new GImage(Constants.PATH+"\\RockTile.png");
//			rock.setSize(56,56);
//			GameData.getGUI().add(rock,0,56*i);
//		}
		
		
//		File f=new File(Constants.PATH+"InitializeData\\accevastages.txt");
//		PrintWriter writer=new PrintWriter(f);
//		writer.println(1.0/3.0);
//		writer.println(3.0/8.0);
//		writer.println(3.0/7.0);
//		writer.println(1.0/2.0);
//		writer.println(3.0/5.0);
//		writer.println(3.0/4.0);
//		writer.println(1.0);
//		writer.println(4.0/3.0);
//		writer.println(5.0/3.0);
//		writer.println(2.0/1.0);
//		writer.println(7.0/3.0);
//		writer.println(8.0/3.0);
//		writer.println(3.0);
//		writer.close();
		
		
//		System.out.println(GameData.getTypeEffectivenessDamageMod(Type.Fire, Type.Water));
//		System.out.println(GameData.getTypeEffectivenessDamageMod(Type.Normal, Type.Bug));
//		System.out.println(GameData.getTypeEffectivenessDamageMod(Type.Normal, Type.Ghost));
//		System.out.println(GameData.getTypeEffectivenessDamageMod(Type.Ice, Type.Dragon));
		//PlayerData.addNewPokemon(new Pokemon(34,8));

		

//		ArrayList<String> needupdated=new ArrayList<String>();
//		for(LocationName ln:LocationName.values()){
//			Location l=new Location(ln);
//			if(l.getEndpoints().size()>0)
//				needupdated.add(ln.toString());
//		}
//		System.out.println(needupdated);




		//Add trainer locations to forest/cave/gym etc
//		for(LocationName ln:LocationName.values()){
//			Location l=new Location(ln);
//			MapType type=l.getType();
//			int numtrainers=l.getTrainers().size();
//			if(numtrainers==0)
//				continue;
//			if(type==MapType.Cave||type==MapType.Forest||type==MapType.TeamRocket||type==MapType.Gym||type==MapType.OlivineTower){
//				File f=new File(Constants.PATH+"\\LocationData\\"+ln+".txt");
//				Scanner s=new Scanner(f);
//				String contents="";
//				while(s.hasNextLine()){
//					contents+=s.nextLine()+"\n";
//				}
//				int interval=0;
//				if(numtrainers>1)
//					interval=108/(numtrainers-1);
//				for(int i=numtrainers-1;i>=0;i--){
//					int x=120-interval*i;
//					contents=contents.replaceFirst("0 0 C:",(x*10)+" 270 C:");
//				}
//				PrintWriter writer=new PrintWriter(f);
//				writer.print(contents);
//				writer.close();
//			}
//		}




		//Use this to implement pause method for level up waiting if needed
		//		long start=System.nanoTime();
		//		System.out.println(start);
		//		while(System.nanoTime()-start<Long.valueOf("3000000000")){
		//			
		//		}
		//		System.out.println(System.nanoTime());
		//		BattleEngine.initialize(new WildTrainer(PlayerData.getLocation().encounterWildPokemon(PlayerData.getPartySize(),Time.Day)));
		//14x14 tiles, movement ranges 3-7? gives inc in range every 76 speed stat points
		//		GUI gui=GameData.getGUI();
		//		for(int x=0;x<14;x++){
		//			for(int y=0;y<14;y++){
		//				gui.add(new GImage(Constants.PATH+"\\DirtTile.png"),5+x*64,5+y*64);
		//			}
		//		}
		//		gui.setSize(Constants.SCREEN_WIDTH,Constants.SCREEN_HEIGHT+75);
		//		for(int x=4;x<8;x++){
		//			for(int y=6;y<10;y++){
		//				gui.add(new GImage(Constants.PATH+"\\LavaTile.png"),5+x*64,5+y*64);
		//			}
		//		}
		//DetailsEngine.initialize(p,"P.C.");
		//		for(int j=200;j<252;j++){
		//			for(int i=1;i<6;i++){
		//				System.out.print(GameData.getExpThreshold(j,i)+",");
		//			}
		//			System.out.println();
		//		}
		//		PCEngine.initialize();
		//		File f=new File("InitializeData\\itemstrings.txt");
		//		Scanner s=new Scanner(f);
		//		for(int i=0;s.hasNextLine();i++){
		//			String line=s.nextLine();
		//			lines.add(line);
		//		}
		//		s.close();
		//		PrintWriter pw=new PrintWriter(f);
		//		for(String line:lines){
		//			if(line.contains(",Move ")){
		//				int movenum=Integer.parseInt(line.substring(line.indexOf(",Move ")+6,line.indexOf(":,N")));
		//				line=line.substring(0,line.indexOf(":")+1)+" "+GameData.getMoveName(movenum)+",N";
		//				pw.println(line);
		//			}
		//			else
		//				pw.println(line);
		//		}
		//		pw.close();
		//		try{
		//			PrintWriter pw=new PrintWriter(new File("PCData\\1.txt"));
		//			pw.println(new Pokemon(1, 15));
		//			pw.close();
		//		}catch(Exception e){e.printStackTrace();}
		//		PCEngine.close();
		//		Location l=PlayerData.getLocation();
		//		MapEngine.initialize(l);
		//		MapEngine.close();
		//		for(LocationName ln:LocationName.values()){
		//			Location l=new Location(ln);
		//			if(l.getType()==MapType.Forest||l.getType()==MapType.Cave)
		//				System.out.println("-----------------------------"+l.getName()+" "+l.getEndpoints());
		//		}

		//		short count=200;
		//		for(LocationName ln:LocationName.values()){
		////		LocationName ln=LocationName.RocketHideout2Entrance;
		//			System.out.println(count+" "+ln);
		//			Location l=new Location(ln);
		//			File f=new File("C:\\Users\\paris.nelson\\Documents\\PT\\PT\\LocationData\\"+ln.toString()+".txt");
		//			PrintWriter pw=new PrintWriter(f);
		//			pw.println("ID: "+count);
		//			pw.print("Type: ");
		//			if(count==253||count==254||count==294)
		//				pw.println(MapType.TeamRocket.toString());
		//			else if(count==289)
		//				pw.println(MapType.EliteFour.toString());
		//			else if(count==292)
		//				pw.println(MapType.OlivineTower.toString());
		//			else if(count==291||count==247)
		//				pw.println(MapType.Forest.toString());
		//			else if(count==245||count==256||count==271||count==272||count==290||count==293||count==301||count==302
		//					||count==251||count==259)
		//				pw.println(MapType.Cave.toString());
		//			else if(count>=273&&count<=288)
		//				pw.println(MapType.Gym.toString());
		//			else if((count>=226&&count<=258)||(count>=295&&count<=299))
		//				pw.println(MapType.Johto.toString());
		//			else
		//				pw.println(MapType.Kanto.toString());
		//			pw.print("Event: ");
		//			if(l.getEvent()!=null)
		//				pw.println(l.getEvent().toString());
		//			else
		//				pw.println("N/A");
		//			pw.println("Trainers: ");
		//			for(Trainer t:l.getTrainers())
		//				pw.println(t.toString());
		//			pw.println("End Trainers");
		//			pw.print("Wild Pokemon: ");
		//			if(l.hasWilds()){
		//				pw.println(l.lvlmin+" "+l.lvlmax);
		//				pw.println(Arrays.toString(l.mornwild));
		//				pw.println(Arrays.toString(l.daywild));
		//				pw.println(Arrays.toString(l.nightwild));
		//			}
		//			else
		//				pw.println("N/A");
		//			pw.println("Menu: ");
		//			pw.println("End Menu");
		//			pw.close();
		//			count++;
		//		}

		//		MapEngine.switchRegions("Johto");
		//		MapEngine.doStuff();
		//		PlayerData.setLocation(LocationName.Route45);
		//		GameData.getGUI().pause();
		//		GlobalEngine.defeatedTrainer(PlayerData.getLocation().getTrainers().get(2));
		//location.getTrainers().get(0).deactivate();

		//		GHelper test=new GHelper();
		//		test.start();
		//		test.add(new GLabel("asdf",20,20));


		//		PlayerData.load();
		//		PlayerData.save();


		//		int[] pokenums={42,75,217,95,126,246,55,200,142};
		//		int[] mornchance={0,40,0,30,25,0,0,0,5};
		//		int[] daychance={0,30,30,20,10,5,0,0,5};
		//		int[] nightchance={35,25,0,20,0,0,5,10,5};
		//		LocationName lname=LocationName.MtSilver;
		//		int lvlmin=42;
		//		int lvlmax=45;
		//		
		//		
		//		ArrayList<Integer> mornwild=new ArrayList<Integer>();
		//		ArrayList<Integer> daywild=new ArrayList<Integer>();
		//		ArrayList<Integer> nightwild=new ArrayList<Integer>();
		//		
		//		if(mornchance.length!=daychance.length||mornchance.length!=nightchance.length||mornchance.length!=pokenums.length)
		//			System.out.println("Chance arrays don't line up");
		//		
		//		for(int i=0;i<pokenums.length;i++){
		//			for(int j=0;j<mornchance[i];j++)
		//				mornwild.add(pokenums[i]);
		//			for(int j=0;j<daychance[i];j++)
		//				daywild.add(pokenums[i]);
		//			for(int j=0;j<nightchance[i];j++)
		//				nightwild.add(pokenums[i]);
		//		}
		//		if(mornwild.size()<100)
		//			System.out.println(mornwild.size()+", doesn't add up");
		//		if(daywild.size()<100)
		//			System.out.println(daywild.size()+", doesn't add up");
		//		if(nightwild.size()<100)
		//			System.out.println(nightwild.size()+", doesn't add up");
		//		
		//		File f=new File("LocationData\\"+lname.toString()+".txt");
		//		FileWriter fw;
		//		BufferedWriter bw;
		//		PrintWriter out;
		//		try{
		//			fw = new FileWriter(f, true);
		//			bw = new BufferedWriter(fw);
		//			out = new PrintWriter(bw);
		//			out.println(lvlmin+" "+lvlmax);
		//			out.println(mornwild);
		//			out.println(daywild);
		//			out.println(nightwild);
		//			out.close();
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}

		//		Move[] moves=GameData.getMovesetByLevel(159,16);
		//		for(Move m:moves){
		//			if(m!=null)
		//				System.out.println(GameData.getMoveName(m.getNum()));
		//		}	
		//		System.out.println("---------");
		//		moves=GameData.getMovesetByLevel(159,22);
		//		for(Move m:moves){
		//			if(m!=null)
		//				System.out.println(GameData.getMoveName(m.getNum()));
		//		}	
		//		System.out.println("---------");
		//		moves=GameData.getMovesetByLevel(160,32);
		//		for(Move m:moves){
		//			if(m!=null)
		//				System.out.println(GameData.getMoveName(m.getNum()));
		//		}	
		//		System.out.println("---------");
		//		moves=GameData.getMovesetByLevel(160,38);
		//		for(Move m:moves){
		//			if(m!=null)
		//				System.out.println(GameData.getMoveName(m.getNum()));
		//		}	
		//		System.out.println("---------");
		//		moves=GameData.getMovesetByLevel(160,45);
		//		for(Move m:moves){
		//			if(m!=null)
		//				System.out.println(GameData.getMoveName(m.getNum()));
		//		}	
		//		System.out.println("---------");
		//		moves=GameData.getMovesetByLevel(160,50);
		//		for(Move m:moves){
		//			if(m!=null)
		//				System.out.println(GameData.getMoveName(m.getNum()));
		//		}	
		//		System.out.println("---------");





		//		for(int i=1;i<47;i++){
		//			File f=new File("LocationData\\Route"+i+".txt");
		//			if(!f.exists())
		//				f.createNewFile();
		//		}

		//		for(LocationName l:LocationName.values()){
		//			File f=new File("LocationData\\"+l.toString()+".txt");
		//			try{
		//				Scanner s=new Scanner(f);
		//				if(!s.hasNextLine()){
		//					PrintWriter pw=new PrintWriter(f);
		//					pw.println("Event: ");
		//					pw.close();
		//				}
		//			}catch(Exception e){}
		//		}
		//		int encnum=7;
		//		int lvl=50;
		//		int num1=65;
		////		int lvl1=32;
		//		int num2=169;
		////		int lvl2=14;
		//		int num3=18;
		////		int lvl3=18;
		//		int num4=181;
		////		int lvl4=12;
		//		int num5=94;
		////		int lvl5=35;
		//		int num6=143;
		////		int lvl6=45;
		//		int ivclass=3;
		//	
		//		Pokemon[] p1=new Pokemon[6];
		//		p1[0]=new Pokemon(num1,lvl,ivclass);
		//		p1[1]=new Pokemon(num2,lvl,ivclass);
		//		p1[2]=new Pokemon(num3,lvl,ivclass);
		//		p1[3]=new Pokemon(num4,lvl,ivclass);
		//		p1[4]=new Pokemon(num5,lvl,ivclass);
		//		p1[5]=new Pokemon(num6,lvl,ivclass);
		//		EliteTrainer t1=new EliteTrainer(encnum+125,"Rival",p1,0,0,
		//				"C:\\Users\\paris.nelson\\Documents\\PT\\PT\\Sprites\\Rival.png",encnum+1);
		//		
		//		File f=new File("LocationData\\IndigoPlateau.txt");
		//		FileWriter fw;
		//		BufferedWriter bw;
		//		PrintWriter out;
		//		try{
		//			fw = new FileWriter(f, true);
		//			bw = new BufferedWriter(fw);
		//			out = new PrintWriter(bw);
		//			out.println(t1.toString());
		//			out.close();
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}
		//		
		//		
		//		
		//		System.out.println(GameData.getPokeName(num1));
		//		Move[] moves=GameData.getMovesetByLevel(num1,lvl);
		//		for(Move m:moves){
		//			if(m!=null)
		//				System.out.println(GameData.getMoveName(m.getNum()));
		//		}
		//		System.out.println("-------------");
		//		System.out.println(GameData.getPokeName(num2));
		//		moves=GameData.getMovesetByLevel(num2,lvl);
		//		for(Move m:moves){
		//			if(m!=null)
		//				System.out.println(GameData.getMoveName(m.getNum()));
		//		}
		//		System.out.println("-------------");
		//		System.out.println(GameData.getPokeName(num3));
		//		moves=GameData.getMovesetByLevel(num3,lvl);
		//		for(Move m:moves){
		//			if(m!=null)
		//				System.out.println(GameData.getMoveName(m.getNum()));
		//		}
		//		System.out.println("-------------");
		//		System.out.println(GameData.getPokeName(num4));
		//		moves=GameData.getMovesetByLevel(num4,lvl);
		//		for(Move m:moves){
		//			if(m!=null)
		//				System.out.println(GameData.getMoveName(m.getNum()));
		//		}
		//		System.out.println("-------------");
		//		System.out.println(GameData.getPokeName(num5));
		//		moves=GameData.getMovesetByLevel(num5,lvl);
		//		for(Move m:moves){
		//			if(m!=null)
		//				System.out.println(GameData.getMoveName(m.getNum()));
		//		}
		//		System.out.println("-------------");
		//		System.out.println(GameData.getPokeName(num6));
		//		moves=GameData.getMovesetByLevel(num6,lvl);
		//		for(Move m:moves){
		//			if(m!=null)
		//				System.out.println(GameData.getMoveName(m.getNum()));
		//		}
		//		System.out.println("-------------");
		//		
		//		ArrayList<Trainer> trainers=new ArrayList<Trainer>();
		//		LocationName location=LocationName.MtSilver;

		//		Pokemon[] p1={new Pokemon(121,38,1),new Pokemon(121,38,1),new Pokemon(121,38,1),
		//				new Pokemon(87,38,1),new Pokemon(87,38,1),new Pokemon(87,38,1)};
		//		Trainer t1=new Trainer("Swimmer Lori",p1,location,0,0,
		//				"C:\\Users\\paris.nelson\\Documents\\PT\\PT\\Sprites\\SwimmerF.png");
		//		trainers.add(t1);
		//		Pokemon[] p2={new Pokemon(115,36,1),new Pokemon(115,36,1),new Pokemon(241,36,1),
		//				new Pokemon(128,36,1),new Pokemon(113,36,1),new Pokemon(242,36,1)};
		//		Trainer t2=new Trainer("PokeManiac Jim",p2,location,0,0,
		//				"C:\\Users\\paris.nelson\\Documents\\PT\\PT\\Sprites\\PokeManiac.png");
		//		trainers.add(t2);
		//		Pokemon[] p3={new Pokemon(8,34,1),new Pokemon(55,34,1),new Pokemon(73,34,1),
		//				new Pokemon(134,34,1),new Pokemon(171,34,1),new Pokemon(184,34,1)};
		//		Trainer t3=new Trainer("Swimmer Debra",p3,location,0,0,
		//				"C:\\Users\\paris.nelson\\Documents\\PT\\PT\\Sprites\\SwimmerF.png");
		//		trainers.add(t3);

		//		Pokemon[] p2={new Pokemon(220,34,3),new Pokemon(220,34,3),new Pokemon(87,34,3),
		//				new Pokemon(87,34,3),new Pokemon(91,34,3),new Pokemon(91,34,3)};
		//		Trainer t2=new Trainer("Boarder Brad",p2,location,0,0,
		//				"C:\\Users\\paris.nelson\\Documents\\PT\\PT\\Sprites\\Boarder.png");
		//		trainers.add(t2);
		//		Pokemon[] p5={new Pokemon(171,36,3),new Pokemon(222,36,3),new Pokemon(222,36,3),
		//				new Pokemon(224,36,3),new Pokemon(226,36,3),new Pokemon(171,36,3)};
		//		Trainer t5=new Trainer("Swimmer Aurora",p5,location,0,0,
		//				"C:\\Users\\paris.nelson\\Documents\\PT\\PT\\Sprites\\SwimmerF.png");
		//		trainers.add(t5);
		//		Pokemon[] p6={new Pokemon(5,38,3),new Pokemon(5,38,3),new Pokemon(218,38,3),
		//				new Pokemon(218,38,3),new Pokemon(58,38,3),new Pokemon(58,38,3)};
		//		Trainer t6=new Trainer("Firebreather Bane",p6,location,0,0,
		//				"C:\\Users\\paris.nelson\\Documents\\PT\\PT\\Sprites\\Firebreather.png");
		//		trainers.add(t6);
		//		Pokemon[] p4={new Pokemon(num1,lvl1,7),new Pokemon(num2,lvl2,7),new Pokemon(num3,lvl3,7),
		//				new Pokemon(num4,lvl4,7),new Pokemon(num5,lvl5,7),new Pokemon(num6,lvl6,7)};
		//		EliteTrainer t4=new EliteTrainer("Red",p4,0,0,
		//				"C:\\Users\\paris.nelson\\Documents\\PT\\PT\\Sprites\\Red.png",8);
		//		trainers.add(t4);
		//		
		//		File f=new File("TrainerData\\16\\"+location.toString()+".txt");
		//		PrintWriter pw=new PrintWriter(f);
		//		for(int i=0;i<trainers.size();i++){
		//			pw.println(trainers.get(i));
		//		}
		//		pw.close();



		//		File f=new File("withnames.txt");
		//		Scanner s=new Scanner(f);
		//		File f2=new File("bylevel.txt");
		//		PrintWriter pw=new PrintWriter(f2);
		//		for(int i=1;i<=251;i++){
		//			ArrayList<Integer>[] pokemon=new ArrayList[101];
		//			String[] split=s.nextLine().split(",,");
		//			for(String curr:split){
		//				curr=curr.substring(1,curr.length()-1);
		//				String[] halves=curr.split(",");
		//				int level=Integer.parseInt(halves[0]);
		//				if(pokemon[level]==null){
		//					ArrayList<Integer> moves=new ArrayList<Integer>();
		//					moves.add(Globals.getMoveNum(halves[1]));
		//					pokemon[level]=moves;
		//				}
		//				else
		//					pokemon[level].add(Globals.getMoveNum(halves[1]));
		//			}
		//			for(int j=0;j<pokemon.length;j++){
		//				if(pokemon[j]==null)
		//					pw.print("null");
		//				else{
		//					pw.print("[");
		//					for(int k=0;k<pokemon[j].size();k++){
		//						pw.print(pokemon[j].get(k));
		//						if(k<pokemon[j].size()-1)
		//							pw.print(",");
		//					}
		//					pw.print("]");
		//				}
		//				if(j<pokemon.length-1)
		//					pw.print(",,");
		//			}
		//			pw.println();
		//		}
		//		pw.close();
		//		File f=new File("bytm.txt");
		//		Scanner s=new Scanner(f);
		//		ArrayList<ArrayList<Integer>> bytm=new ArrayList<ArrayList<Integer>>();
		//		for(int i=0;i<251;i++){
		//			ArrayList<Integer> tms=new ArrayList<Integer>();
		//			String line=s.nextLine();
		//			String[] split=line.split(",");
		//			if(split.length==1){
		//				bytm.add(tms);
		//				continue;
		//			}
		//				//System.out.println(Arrays.toString(split));
		//			for(int j=1;j<split.length;j++){
		//				String str=split[j];
		//				if(str.startsWith("TM"))
		//					tms.add(Integer.parseInt(str.substring(2))+45);
		//				else{
		//					//System.out.println(str);
		//					tms.add(Integer.parseInt(str.substring(2))+95);
		//				}
		//			}
		//			bytm.add(tms);
		//		}
		//		for(ArrayList<Integer> x:bytm){
		//			System.out.println(x);
		//		}

		//		ArrayList<Integer> slow=new ArrayList<Integer>();
		//		ArrayList<Integer> medslow=new ArrayList<Integer>();
		//		ArrayList<Integer> medfast=new ArrayList<Integer>();
		//		ArrayList<Integer> fast=new ArrayList<Integer>();
		//		for(int n=0;n<101;n++){
		//			int d=(int)Math.pow(n,3);
		//			medfast.add(d);
		//			slow.add(5*d/4);
		//			fast.add(4*d/5);
		//			int e=(int)Math.pow(n,2);
		//			medslow.add(6*d/5-15*e+100*n-140);
		//		}
		//		System.out.println(slow);
		//		System.out.println(medslow);
		//		System.out.println(medfast);
		//		System.out.println(fast);


				
	}
	
	private static void writeLootLevelsFile() throws IOException{
		File f=new File(Constants.PATH+"\\InitializeData\\itemlvls.txt");
		Scanner s=new Scanner(f);
		int[] lootids=new int[Constants.NUM_ITEMS];
		int[] lootlvls=new int[lootids.length];
		for(int i=1;i<lootids.length;i++){
			String line=s.nextLine();
			lootids[i]=i;
			lootlvls[i]=Integer.parseInt(line);
		}
		int n = lootlvls.length;
		for (int j = 1; j < n; j++) {
			int key = lootlvls[j];
			int keyid=lootids[j];
			int i = j-1;
			while ( (i >0) && ( lootlvls[i] > key ) ) {
				lootlvls[i+1] = lootlvls[i];
				lootids[i+1] = lootids[i];
				i--;
			}
			lootlvls[i+1] = key;
			lootids[i+1]=keyid;
		}
		ArrayList<IntPair> changeindices=new ArrayList<IntPair>();
		int lastval=0;
		for(int i=1;i<lootlvls.length;i++){
			if(lootlvls[i]!=lastval){
				changeindices.add(new IntPair(lootlvls[i],i));
				lastval=lootlvls[i];
			}
		}
		System.out.println(Arrays.toString(lootids));
		
		//System.out.println(Arrays.toString(lootlvls));
		System.out.println(changeindices);
		System.out.println(changeindices.size());
		s.close();
	}
	
	private static void getPokemonThatLearnMove(int movenum) throws IOException{
		File f=new File(Constants.PATH+"\\InitializeData\\bylevel.txt");
		Scanner s=new Scanner(f);
		ArrayList<String> bylevels=new ArrayList<String>();
		bylevels.add("");
		while(s.hasNextLine()){
			bylevels.add(s.nextLine());
		}
		for(int i=0;i<bylevels.size();i++){
			String bylevel=bylevels.get(i);
			if(bylevel.contains(","+movenum+"]")||bylevel.contains(","+movenum+",")
					||bylevel.contains("["+movenum+"]")||bylevel.contains("["+movenum+",")){
				String[] levels=bylevel.split(",,");
				for(int level=1;level<levels.length;level++){
					if(levels[level].contains(","+movenum+"]")||levels[level].contains(","+movenum+",")
							|levels[level].contains("["+movenum+"]")||levels[level].contains("["+movenum+","))
						System.out.println(GameData.getName(i)+" learns "+GameData.getMoveName(movenum)+" at level "+level);
				}
			}
		}
		s.close();
	}
	
	public static void test(ArrayList<Unit> input){
		ArrayList<Unit> temp=(ArrayList<Unit>) input.clone();
		temp.add(null);
	}
}
