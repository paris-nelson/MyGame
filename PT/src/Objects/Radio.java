package Objects;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import Enums.MusicTheme;
import Global.Constants;
import Global.GameData;

public class Radio {
	private BasicPlayer player;
	private MusicTheme theme;
	private int songindex;
	private boolean enabled;
	
	public Radio(boolean enabled){
		theme=null;
		songindex=-1;
		player=new BasicPlayer();
		this.enabled=enabled;
	}
	
	public Radio(MusicTheme theme){
		this.theme=theme;
		songindex=0;
		player=new BasicPlayer();
	}
	
	public void changeTheme(MusicTheme newtheme){
		if(newtheme!=theme){
			if(enabled)
				stop();
			theme=newtheme;
			songindex=0;
			open();
			if(enabled)
				play();
		}
	}
	
	public void prevSong(){
		try{
			player.stop();
			songindex--;
			open();
			player.play();
		}catch(Exception e){e.printStackTrace();}
	}
	
	public void nextSong(){
		try{
			player.stop();
			songindex++;
			open();
			player.play();
		}catch(Exception e){e.printStackTrace();}
	}
	
	private void open(){
		try {
			File song=new File(Constants.PATH+"\\Music\\"+theme+"\\"+songindex+".mp3");
			if(!song.exists()){
				if(songindex==0)
					throw new FileNotFoundException("No songs found for "+theme+" theme.");
				else if(songindex>0)
					songindex=0;
				else{
					do{
						songindex++;
					}while(new File(Constants.PATH+"\\Music\\"+theme+"\\"+(songindex+1)+".mp3").exists());
				}
				song=new File(Constants.PATH+"\\Music\\"+theme+"\\"+songindex+".mp3");
			}
			player.open(song);
		} catch (Exception e) {
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
}
