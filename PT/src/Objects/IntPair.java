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
	
	public String toString(){
		return "("+x+","+y+")";
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
