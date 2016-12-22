package Menus;

import java.util.ArrayList;

import Engines.DetailsEngine;
import Engines.MenuEngine;
import Engines.PCEngine;
import Global.PlayerData;
import Objects.Pokemon;

public class PCDetailsMenu implements Menu {
	
	private Pokemon pokemon=null;
	private ArrayList<String> options=new ArrayList<String>();
	
	public PCDetailsMenu(Pokemon p){
		pokemon=p;
		options.add("Release");
		options.add("Add to Party");
		options.add("Take Held Item");
		options.add("Exit");
	}

	@Override
	public void refreshVisibleOptions() {
		//do nothing
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
		String selected=options.get(index);
		if(selected.equals("Exit")){
			MenuEngine.close();
			DetailsEngine.close();
			PCEngine.initialize();
		}
		else if(selected.equals("Release")){
			options.clear();
			options.add("Confirm Release");
			options.add("Nevermind");
			MenuEngine.refreshMenu();
		}
		else if(selected.equals("Confirm Release")){
			MenuEngine.close();
			DetailsEngine.close();
			PlayerData.removeFromPC(pokemon);
			PCEngine.initialize();
		}
		else if(selected.equals("Nevermind")){
			MenuEngine.close();
			MenuEngine.initialize(new PCDetailsMenu(pokemon));
		}
		else if(selected.equals("Add to Party")){
			if(PlayerData.getPartySize()<6){
				System.out.println(pokemon.getName()+" added to party");
				PlayerData.addPokemonToParty(pokemon);
				PlayerData.removeFromPC(pokemon);
				MenuEngine.close();
				DetailsEngine.close();
				PCEngine.initialize();
			}
			else{
				System.out.println("Party Full");
				options.set(index,"Party Full!");
				MenuEngine.refreshMenu();
			}
		}
		else if(selected.equals("Take Held Item")){
			if(pokemon.getHeldID()>-1){
			PlayerData.addItem(pokemon.getHeldID(),1);
			pokemon.removeHeldItem();
			options.remove(index);
			MenuEngine.refreshMenu();
			}
			else{
				options.set(index,"No Item To Take!");
				MenuEngine.refreshMenu();
			}
		}
			
	}

}
