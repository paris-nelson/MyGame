package Objects;

import java.util.Scanner;

import Global.Constants;
import acm.graphics.GCompound;
import acm.graphics.GImage;

public class LoadExistingBattlefieldMaker implements BattlefieldMaker {
	private Square[][] bf;
	private GCompound image;
	private Scanner reader;
	
	public LoadExistingBattlefieldMaker(Scanner reader){
		this.reader=reader;
	}

	@Override
	public void makeNew() {
		bf=new Square[Constants.BATTLEFIELD_HEIGHT][Constants.BATTLEFIELD_WIDTH];
		image=new GCompound();
		for(int y=0;y<Constants.BATTLEFIELD_HEIGHT;y++){
			String line=reader.nextLine();
			String[] split=line.split(" ");
			for(int x=0;x<Constants.BATTLEFIELD_WIDTH;x++){
				Square square=Square.readIn(split[x]);
				bf[x][y]=square;
				image.add(new GImage(Constants.PATH+"Sprites\\"+square.getTileType().toString()+"Tile.png"),x*Constants.TILE_SIZE,y*Constants.TILE_SIZE);
				image.add(square.getBoundary(),x*Constants.TILE_SIZE,y*Constants.TILE_SIZE);
			}
		}
	}

	@Override
	public GCompound getImage() {
		return image;
	}

	@Override
	public Square[][] getResult() {
		return bf;
	}
	
	@Override
	public String toString(){
		String s="";
		for(int y=0;y<bf[0].length;y++){
			for(int x=0;x<bf.length;x++){
				s+=bf[x][y].toString()+" ";
			}
			s+="\n";
		}
		return s;
	}

}
