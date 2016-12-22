package Menus;

import java.util.ArrayList;

import Engines.DetailsEngine;
import Engines.MapEngine;
import Engines.MenuEngine;
import Enums.PartyMenuMode;
import Global.Constants;
import Global.PlayerData;
import Objects.Pokemon;

public class PartyDetailsMenu implements Menu {

	private Pokemon pokemon;
	private ArrayList<String> visibleoptions=new ArrayList<String>();
	private PartyMenuMode mode;

	public PartyDetailsMenu(Pokemon p,PartyMenuMode mode){
		pokemon=p;
		this.mode=mode;
		if(mode==PartyMenuMode.VIEWMAP){
			visibleoptions.add("Release");
			visibleoptions.add("Take Held Item");
		}
		visibleoptions.add("Exit");
	}

	@Override
	public void refreshVisibleOptions() {
		if(!visibleoptions.contains("Make Leading Pokemon")&&!PlayerData.getLeadingPokemon().equals(pokemon)&&mode==PartyMenuMode.VIEWMAP)
			visibleoptions.add(visibleoptions.size()-2,"Make Leading Pokemon");
	}

	@Override
	public int getNumOptions() {
		return visibleoptions.size();
	}

	@Override
	public ArrayList<String> getVisibleOptions() {
		return visibleoptions;
	}

	@Override
	public void select(short index) {
		String selected=visibleoptions.get(index);
		if(selected.equals("Exit")){
			MenuEngine.close();
			DetailsEngine.close();
			//MapEngine.initialize(PlayerData.getLocation());
			MenuEngine.initialize(new PartyMenu(mode));
		}
		else if(selected.equals("Make Leading Pokemon")){
			PlayerData.setLeadingPokemon(pokemon);
			visibleoptions.set(index,"Leading Pokemon Set!");
			MenuEngine.refreshMenu();
			MapEngine.changePlayerIcon(Constants.PATH+"Sprites\\"+pokemon.getNum()+".png");
		}
		else if(selected.equals("Release")){
			visibleoptions.clear();
			visibleoptions.add("Confirm Release");
			visibleoptions.add("Nevermind");
			MenuEngine.refreshMenu();
		}
		else if(selected.equals("Confirm Release")){
			MenuEngine.close();
			DetailsEngine.close();
			PlayerData.removePokemonFromParty(pokemon);
			MenuEngine.initialize(new PlayerMenu());
		}
		else if(selected.equals("Nevermind")){
			MenuEngine.close();
			MenuEngine.initialize(new PartyDetailsMenu(pokemon,mode));
		}
		else if(selected.equals("Take Held Item")){
			if(pokemon.getHeldID()>-1){
				PlayerData.addItem(pokemon.getHeldID(),1);
				pokemon.removeHeldItem();
				visibleoptions.remove(index);
				MenuEngine.refreshMenu();
			}
			else{
				visibleoptions.set(index,"No Item To Take!");
				MenuEngine.refreshMenu();
			}
		}
	}

}
