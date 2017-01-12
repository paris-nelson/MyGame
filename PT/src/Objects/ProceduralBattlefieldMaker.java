package Objects;

import Enums.Tile;
import Global.Constants;
import acm.graphics.GCompound;
import acm.graphics.GImage;

public class ProceduralBattlefieldMaker implements BattlefieldMaker {
	private Square[][] bf;
	private GCompound image;
	
	
	@Override
	public void makeNew() {
		//TODO:
		bf=new Square[Constants.BATTLEFIELD_HEIGHT][Constants.BATTLEFIELD_WIDTH];
		image=new GCompound();
		for(int x=0;x<bf.length;x++){
			for(int y=0;y<bf[0].length;y++){
				if(x>5&&x<10&&y>3&&y<8){
					image.add(new GImage(Constants.PATH+"\\WaterTile.png"),x*Constants.TILE_SIZE,y*Constants.TILE_SIZE);
					bf[x][y]=new Square(Tile.Water);
					image.add(bf[x][y].getBoundary(),x*Constants.TILE_SIZE,y*Constants.TILE_SIZE);
				}
				else{
					image.add(new GImage(Constants.PATH+"\\GrassTile.png"),x*Constants.TILE_SIZE,y*Constants.TILE_SIZE);
					bf[x][y]=new Square(Tile.Grass);
					image.add(bf[x][y].getBoundary(),x*Constants.TILE_SIZE,y*Constants.TILE_SIZE );
				}
			}
		}
	}

	@Override
	public GCompound getImage(){
		return image;
	}

	@Override
	public Square[][] getResult(){
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
