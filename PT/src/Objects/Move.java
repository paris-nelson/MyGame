package Objects;

import Global.GameData;

public class Move {
	private int currpp;
	private int currmax;
	private int num;
	/**
	 * Takes in the move number and sets
	 * the pp to the base value for that move.
	 * @param movenum
	 */
	public Move(int movenum){
		num=movenum;
		currpp=GameData.getMovePP(num);
		currmax=currpp;
	}
	
	/**
	 * Constructor for loading a saved move.
	 * @param movenum
	 * @param currpp
	 * @param currmax
	 */
	public Move(int movenum,int currpp,int currmax){
		num=movenum;
		this.currpp=currpp;
		this.currmax=currmax;
	}
	
	public int getNum(){
		return num;
	}
	
	public boolean hasPP(){
		return currpp>0;
	}
	
	/**
	 * Adds the specified value to the current PP. If using an Ether, this number is 10. If healing at a pokecenter, this would be the current max.
	 * If the addition would make the current PP greater than full, it becomes full instead.
	 * @param delta
	 * @return: true if added, false if the current PP is already at it's full value.
	 */
	public boolean incCurrPP(int delta){
		if(currpp==currmax)
			return false;
		currpp+=delta;
		if(currpp>currmax)
			currpp=currmax;
		return true;
	}
	
	/**
	 * Removes the specified value from the current PP. If using a move, this number is 1. If the move Spite is used, this would be 1-5.
	 * If the reduction would make the current PP less than 0, it becomes 0 instead.
	 * @param delta
	 * @return: true if reduced, false if the current PP is alread at 0.
	 */
	public boolean decCurrPP(int delta){
		if(currpp==0)
			return false;
		currpp-=delta;
		if(currpp<0)
			currpp=0;
		return true;
	}
	
	/**
	 * Restores currpp to currmax
	 */
	public void restorePP(){
		currpp=currmax;
	}
	
	public int getCurrPP(){
		return currpp;
	}
	
	/**
	 * Gets the current max value for the move's PP. Used when restoring the move's PP to it's full value such as at a Pokecenter
	 * @return
	 */
	public int getCurrMax(){
		return currmax;
	}
	
	/**
	 * Set the max PP to the maximum allowed value (8/5)*basePP. If it is already at that value, it will fail
	 * @return: true if max PP set to max value, false if it's already maxed
	 */
	public boolean maxPP(){
		int absmax=GameData.getMoveMaxPP(num);
		if(currmax<absmax){
			currmax=absmax;
			return true;
		}
		return false;
	}
	
	/**
	 * Increase the max PP of the move by 20% of the base PP. If the PP is already at the maximum allowed value (8/5)*basePP, then it will fail.
	 * If the increase would make the maximum higher than the allowed value, it will set it to that value instead.
	 * @return: true if max PP icreased, false if it's already maxed
	 */
	public boolean incMaxPP(){
		int absmax=GameData.getMoveMaxPP(num);
		if(currmax==absmax)
			return false;
		currmax+=GameData.getMovePP(num)/5;
		if(currmax>absmax)
			currmax=absmax;
		return true;
	}
	
	public String toString(){
		return num+" "+currpp+" "+currmax;
	}
	
	public boolean isMaxPP(){
		return currmax==GameData.getMoveMaxPP(num);
	}
}
