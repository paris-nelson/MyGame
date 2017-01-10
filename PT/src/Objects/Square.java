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
	
	/**
	 * Constuctor for reading in
	 * @param tile
	 * @param unit
	 * @param spikes
	 */
	public Square(Tile tile,int unit,boolean spikes){
		this.tile=tile;
		this.unit=unit;
		this.spikes=spikes;
		boundary=new GRect(Constants.TILE_SIZE,Constants.TILE_SIZE);
		boundary.setFilled(false);
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
	
	public void markRed(){
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
	
	public void setUnit(int unitid){
		this.unit=unitid;
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
	
	public String toString(){
		return tile+","+unit+","+spikes;
	}
	
	public static Square readIn(String s){
		String[] split=s.split(",");
		return new Square(Tile.valueOf(split[0]),Integer.parseInt(split[1]),Boolean.valueOf(split[2]));
	}
}
