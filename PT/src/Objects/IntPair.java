package Objects;

public class IntPair {
	private int x;
	private int y;
	
	public IntPair(int x,int y){
		this.x=x;
		this.y=y;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public void setX(int newval){
		x=newval;
	}
	
	public void setY(int newval){
		y=newval;
	}
	
	public IntPair add(IntPair other){
		return new IntPair(x+other.x,y+other.y);
	}
	
	public String toString(){
		return x+","+y;
	}
	/**
	 * Not actual distance calculation, but the number of unit movements to get from one point to the other aka the difference in x values 
	 * plus the difference in y values
	 * @param other
	 * @return
	 */
	public int distanceFrom(IntPair other){
		return Math.abs(x-other.x)+Math.abs(y-other.y);
	}
	
	/**
	 * Read in int pair in the form "x,y" e.g. "37,290"
	 * @param intpair
	 * @return
	 */
	public static IntPair readIn(String intpair){
		String[] vals=intpair.split(",");
		return new IntPair(Integer.parseInt(vals[0]),Integer.parseInt(vals[1]));
	}
	
	public boolean equals(IntPair other){
		return (x==other.x&&y==other.y);
	}
}
