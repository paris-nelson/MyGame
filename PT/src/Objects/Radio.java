package Objects;

import java.io.File;
import java.util.ArrayList;

import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import Enums.MusicTheme;
import Global.Constants;
import Global.GameData;

public class Radio {
	private BasicPlayer player;
	private MusicTheme theme;
	private ArrayList<String> playlist;
	private int songindex;
	private boolean enabled;
	
	public Radio(boolean enabled){
		theme=null;
		playlist=null;
		songindex=-1;
		player=new BasicPlayer();
		this.enabled=enabled;
	}
	
	public Radio(MusicTheme theme){
		this.theme=theme;
		playlist=GameData.getPlaylist(theme);
		songindex=0;
		player=new BasicPlayer();
	}
	
	public void changeTheme(MusicTheme newtheme){
		if(newtheme!=theme){
			if(enabled)
				stop();
			theme=newtheme;
			playlist=GameData.getPlaylist(theme);
			songindex=0;
			open(playlist.get(songindex));
			if(enabled)
				play();
		}
	}
	
	public void prevSong(){
		try{
			player.stop();
			songindex--;
			if(songindex<0)
				songindex=playlist.size()-1;
			open(playlist.get(songindex));
			player.play();
		}catch(Exception e){e.printStackTrace();}
	}
	
	public void nextSong(){
		try{
			player.stop();
			songindex++;
			if(songindex>=playlist.size())
				songindex=0;
			open(playlist.get(songindex));
			player.play();
		}catch(Exception e){e.printStackTrace();}
	}
	
	private void open(String songtitle){
		try {
			player.open(new File(Constants.PATH+"\\Music\\"+songtitle+".mp3"));
		} catch (BasicPlayerException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isEnabled(){
		return enabled;
	}
	
	public void enable(){
		enabled=true;
	}
	
	public void disable(){
		enabled=false;
	}
	
	public void play(){
		try{
			if(player.getStatus()!=0)
				player.play();
		}catch(Exception e){e.printStackTrace();}
	}
	
	public void stop(){
		try{
			if(player.getStatus()!=2)
				player.stop();
		}catch(Exception e){e.printStackTrace();}
	}

	public BasicPlayer getPlayer() {
		return player;
	}

	public MusicTheme getTheme() {
		return theme;
	}

	public ArrayList<String> getPlaylist() {
		return playlist;
	}
}
