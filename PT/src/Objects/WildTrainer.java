package Objects;

import java.util.ArrayList;
import java.util.Scanner;

import Global.Constants;

public class WildTrainer extends Trainer{
	
	public WildTrainer(Pokemon[] party){
		super(Short.valueOf("-2"),"Wild Pokemon",party,-1,-1,null);
	}
	
	public String toString(){
		String s="WildTrainer: ";
		for(Pokemon p:getParty()){
			s+="\n   "+p.toString();
		}
		s+="\n End Trainer";
		return s;
	}
	
	public static Trainer readInTrainer(Scanner s){
		s.nextLine();//empty end of line
		String temp=s.nextLine();
		ArrayList<Pokemon> currlist=new ArrayList<Pokemon>();
		while(!temp.equals(" End Trainer")){
			currlist.add(Pokemon.readInPokemon(temp));
			temp=s.nextLine();
		}
		Pokemon[] currparty=new Pokemon[currlist.size()];
		for(int i=0;i<currparty.length;i++){
			currparty[i]=currlist.get(i);
		}
		return new WildTrainer(currparty);
	}
}
