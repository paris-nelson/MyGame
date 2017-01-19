package Objects;

import java.util.ArrayList;
import java.util.Scanner;

import Global.Constants;
import acm.graphics.GImage;


public class Trainer{
	private short id;
	private String name;
	private Pokemon[] party;
	private int x;
	private int y;
	private String filelocation;
	private GImage image;

	public Trainer(short id, String name, Pokemon[] party,
			int x, int y,String imageFileLocation) {
		this.id=id;
		this.name = name;
		this.party = party;
		this.x = x;
		this.y = y;
		filelocation=imageFileLocation;
		if(filelocation==null)
			image=null;
		else{
			image=new GImage(imageFileLocation);
			image.setSize(Constants.TRAINER_ICON_SIZE,Constants.TRAINER_ICON_SIZE);
			image.setLocation(x, y);
		}
	}

	public short getID(){
		return id;
	}

	public GImage getImage(){
		return image;
	}

	public String getName() {
		return name;
	}

	public Pokemon[] getParty() {
		return party;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getHighestLevel(){
		int max=party[0].getLevel();
		for(int i=1;i<party.length;i++){
			max=Math.max(max,party[i].getLevel());
		}
		return max;
	}

	protected String getFileLocation(){
		return filelocation;
	}

	public String toString(){
		String s="Trainer: "+id+" "+name+" "+x+" "+y+" "+filelocation;
		for(Pokemon p:party){
			s+="\n   "+p.toString();
		}
		s+="\n End Trainer";
		return s;
	}

	public static Trainer readInTrainer(Scanner s){
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
		return new Trainer(currid,currname,currparty,currx,curry,currfilelocation);
	}
}
