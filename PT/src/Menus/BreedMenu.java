package Menus;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import Engines.MenuEngine;
import Global.Constants;
import Global.GameData;
import Global.PlayerData;
import Objects.Pokemon;

public class BreedMenu implements Menu {
	
	private static ArrayList<String> options=new ArrayList<String>();
	private static Pokemon pokemon;
	
	public BreedMenu(Pokemon p){
		pokemon=p;
		for(Pokemon poke: PlayerData.getParty()){
			if(viablePartner(poke))
				options.add(poke.getName());
		}
	}
	
	private boolean viablePartner(Pokemon p){
		if(p.equals(pokemon))
			return false;
		if(GameData.getPokeName(p.getNum()).equals("Ditto"))
			return true;
		if(GameData.getPokeName(p.getNum()).equals(GameData.getPokeName(pokemon.getNum()))
			&& p.getGender()!=pokemon.getGender())
			return true;
		return false;
	}

	@Override
	public void refreshVisibleOptions() {
		
	}

	@Override
	public int getNumOptions() {
		return options.size();
	}

	@Override
	public ArrayList<String> getVisibleOptions() {
		return options;
	}

	@Override
	public void select(short index) {
		MenuEngine.close();
		if(!options.get(index).equals("Back")){
			System.out.println("Bred "+pokemon.getName()+" and "+options.get(index));
			PlayerData.removeItem(112,1);
			Pokemon offspring=new Pokemon(getOffspringNum(pokemon.getNum()),5);
			offspring.incHappiness(130);
			PlayerData.addNewPokemon(offspring);
		}
	}
	
	private static int getOffspringNum(int parentnum){
		int num=-1;
		File f=new File(Constants.PATH+"\\InitializeData\\prevolutions.txt");
		try{
			Scanner s=new Scanner(f);
			String line="";
			for(int i=0;i<parentnum;i++)
				line=s.nextLine();
			num=Integer.parseInt(line);
		}catch(Exception e){
			e.printStackTrace();
		}
		return num;
	}

}
