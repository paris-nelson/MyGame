package Global;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

public class ControlsConfig {
	public static int LEFT;
	public static int RIGHT;
	public static int UP;
	public static int DOWN;
	public static int START;
	public static int BACK;
	
	public static void setLeftKeyCode(int code){
		LEFT=code;
	}
	public static void setRighttKeyCode(int code){
		RIGHT=code;
	}
	public static void setUpKeyCode(int code){
		UP=code;
	}
	public static void setDownKeyCode(int code){
		DOWN=code;
	}
	public static void setStartKeyCode(int code){
		START=code;
	}
	public static void setBackKeyCode(int code){
		BACK=code;
	}
	
	public static void load(){
		File f=new File(Constants.PATH+"InitializeData\\controlsavefile.txt");
		try{
			Scanner s=new Scanner(f);
			LEFT=s.nextInt();
			RIGHT=s.nextInt();
			UP=s.nextInt();
			DOWN=s.nextInt();
			START=s.nextInt();
			BACK=s.nextInt();
			s.close();
		}catch(Exception e){e.printStackTrace();}
	}
	
	public static void save(){
		File f=new File(Constants.PATH+"InitializeData\\controlsavefile.txt");
		try{
			PrintWriter pw=new PrintWriter(f);
			pw.println(LEFT);
			pw.println(RIGHT);
			pw.println(UP);
			pw.println(DOWN);
			pw.println(START);
			pw.println(BACK);
			pw.close();
		}catch(Exception e){e.printStackTrace();}
	}
}
