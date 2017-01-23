package Global;

import java.io.File;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Scanner;

import Enums.Control;

public class ControlsConfig {
	private static LinkedHashMap<Control,Integer> controlsmap;
	
	public static int getKey(Control control){
		return controlsmap.get(control);
	}
	
	public static LinkedHashMap<Control,Integer> getControls(){
		return controlsmap;
	}
	
	public static void setControls(LinkedHashMap<Control,Integer> newmap){
		controlsmap=newmap;
	}
	
	public static LinkedHashMap<Control,Integer> getDefaults(){
		load(Constants.PATH+"InitializeData\\defaultcontrols.txt");
		return controlsmap;
	}
	
	public static void load(){
		load(Constants.PATH+"InitializeData\\controlsavefile.txt");
	}
	
	public static void load(String filename){
		controlsmap=new LinkedHashMap<Control,Integer>();
		File f=new File(filename);
		try{
			Scanner s=new Scanner(f);
			while(s.hasNextLine()){
				String line=s.nextLine();
				String[] split=line.split(" : ");
				controlsmap.put(Control.valueOf(split[0].trim()),Integer.parseInt(split[1].trim()));
			}
			s.close();
		}catch(Exception e){e.printStackTrace();}
	}
	
	public static void save(){
		File f=new File(Constants.PATH+"InitializeData\\controlsavefile.txt");
		try{
			PrintWriter pw=new PrintWriter(f);
			for(Control c:controlsmap.keySet()){
				pw.println(c.toString()+" : "+controlsmap.get(c));
			}
			pw.close();
		}catch(Exception e){e.printStackTrace();}
	}
}
