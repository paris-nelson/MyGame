package Menus;

import java.util.ArrayList;

import Engines.MenuEngine;
import Global.GameData;
import Objects.Radio;

public class MusicMenu implements Menu {
	
	private ArrayList<String> options;
	
	public MusicMenu(){
		Radio radio=GameData.getRadio();
		options=new ArrayList<String>();
		if(radio.isEnabled()){
			options.add("Previous Song");
			options.add("Next Song");
			options.add("Turn Off Music");
		}
		else{
			options.add("Turn On Music");
		}
		options.add("Exit Menu");
	}

	public void refreshVisibleOptions() {
		options=new ArrayList<String>();
		Radio radio=GameData.getRadio();
		if(radio.isEnabled()){
			options.add("Previous Song");
			options.add("Next Song");
			options.add("Turn Off Music");
		}
		else{
			options.add("Turn On Music");
		}
		options.add("Exit Menu");
	}

	public int getNumOptions() {
		return options.size();
	}

	public ArrayList<String> getVisibleOptions() {
		return options;
	}

	public void select(short index) {
		String selected=options.get(index);
		Radio radio=GameData.getRadio();
		if(selected.equals("Previous Song")){
			radio.prevSong();
		}
		else if(selected.equals("Next Song")){
			radio.nextSong();
		}
		else if(selected.equals("Turn Off Music")){
			radio.disable();
			MenuEngine.refreshMenu();
		}
		else if(selected.equals("Turn On Music")){
			radio.enable();
			MenuEngine.refreshMenu();
		}
		else if(selected.equals("Exit Menu")){
			MenuEngine.close();
			MenuEngine.initialize(new OptionsMenu());
		}
	}
}
