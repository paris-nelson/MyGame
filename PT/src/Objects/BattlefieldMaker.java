package Objects;

import acm.graphics.GCompound;

public interface BattlefieldMaker {
	
	public void makeNew();
	public GCompound getImage();
	public Square[][] getResult();
	public String toString();
}
