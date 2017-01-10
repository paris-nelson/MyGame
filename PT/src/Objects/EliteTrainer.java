package Objects;

import java.util.ArrayList;
import java.util.Scanner;

import Global.Constants;

public class EliteTrainer extends Trainer{
	private int lootmod;

	public EliteTrainer(short id, String name, Pokemon[] party,
			int x, int y, String imageFileLocation, int lootmod) {
		super(id, name, party, x, y, imageFileLocation);
		this.lootmod = lootmod;
	}

	public int getLootMod() {
		return lootmod;
	}
	
	public String toString(){
		String s="EliteTrainer: "+getID()+" "+getName()+" "+getX()+" "+getY()+" "
				+getFileLocation()+" "+lootmod;
		for(Pokemon p:getParty()){
			s+="\n   "+p.toString();
		}
		s+="\n End Trainer";
		return s;
	}
	
	public static EliteTrainer readInTrainer(Scanner s){
		short currid=s.nextShort();
		String currname=s.next();
		String temp=s.next();
		while(Character.isLetter(temp.charAt(0))){
			currname+=" "+temp;
			temp=s.next();
		}
		int currx=Integer.parseInt(temp);
		int curry=s.nextInt();
		String currfilelocation=s.next();
		if(!currfilelocation.startsWith("C:"))
			currfilelocation=Constants.PATH+currfilelocation;
		int currmod=s.nextInt();
		s.nextLine();//empty end of line
		temp=s.nextLine();
		ArrayList<Pokemon> currlist=new ArrayList<Pokemon>();
		while(!temp.equals(" End Trainer")){
			currlist.add(Pokemon.readInPokemon(temp));
			temp=s.nextLine();
		}
		Pokemon[] currparty=new Pokemon[currlist.size()];
		for(int i=0;i<currparty.length;i++){
			currparty[i]=currlist.get(i);
		}
		return new EliteTrainer(currid,currname,currparty,currx,curry,currfilelocation,currmod);
	}
}
