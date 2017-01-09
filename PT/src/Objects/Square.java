package Objects;

import java.awt.Color;

import Enums.Tile;
import Global.Constants;
import acm.graphics.GRect;

public class Square {
	private Tile tile;
	private int unit;
	private boolean spikes;
	private GRect boundary;
	
	public Square(Tile tile){
		this.tile=tile;
		boundary=new GRect(Constants.TILE_SIZE,Constants.TILE_SIZE);
		boundary.setFilled(false);
		unit=-1;
		spikes=false;
	}
	
	public GRect getBoundary(){
		return boundary;
	}
	
	public boolean isValid(){
		return boundary.getColor()==Color.GREEN;
	}
	
	public void markNeutral(){
		boundary.setColor(Color.BLACK);
		boundary.sendToFront();
	}
	
	public void markValid(){
		boundary.setColor(Color.GREEN);
		boundary.sendToFront();
	}
	
	public void markInvalid(){
		boundary.setColor(Color.RED);
		boundary.sendToFront();
	}
	
	public void markGrayedOut(){
		boundary.setColor(Color.GRAY);
		boundary.sendToFront();
	}
	
	public Tile getTileType(){
		return tile;
	}
	
	public int getUnit(){
		return unit;
	}
	
	public void removeUnit(){
		unit=-1;
	}
	
	public void setUnit(int unit){
		this.unit=unit;
	}
	
	public boolean hasSpikes(){
		return spikes;
	}
	
	public void removeSpikes(){
		spikes=false;
	}
	
	public void setSpikes(){
		spikes=true;
	}
}
