package Menus;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import Engines.DetailsEngine;
import Engines.MenuEngine;
import Engines.PCEngine;
import Enums.DetailsEngineMode;
import Objects.Pokemon;

public class PCMenu implements Menu {

	private ArrayList<String> options=new ArrayList<String>();
	private ArrayList<Pokemon> entries=new ArrayList<Pokemon>();
	private int pokenum;

	public PCMenu(int pokenum){
		this.pokenum=pokenum;
	}

	@Override
	public void refreshVisibleOptions() {
		entries=getPCEntries(pokenum);
		options.clear();
		if(entries==null)
			options.add("No Entries Found");
		else{
			for(Pokemon p:entries){
				options.add(p.getName());
			}
		}
		options.add("Exit");
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
		if(options.get(index).equals("Exit")){
			MenuEngine.close();
			PCEngine.takeControl();
		}
		else if(options.get(index).equals("No Entries Found")){
			//do nothing
		}
		else{
			MenuEngine.close();
			PCEngine.close();
			DetailsEngine.initialize(entries.get(index),DetailsEngineMode.PC);
		}
	}

	/**
	 * Pulls pc entries for the given pokemon number from PCData\pokenum.txt 
	 * If the file does not exist or is empty, will return null.
	 * @param pokenum
	 * @return
	 */
	public static ArrayList<Pokemon> getPCEntries(int pokenum){
		File f=new File("PCData\\"+pokenum+".txt");
		if(!f.exists())
			return null;
		ArrayList<Pokemon> entries=new ArrayList<Pokemon>();
		try{
			Scanner s=new Scanner(f);
			while(s.hasNextLine()){
				entries.add(Pokemon.readInPokemon(s.nextLine()));
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		if(entries.size()==0)
			return null;
		return entries;
	}

}
