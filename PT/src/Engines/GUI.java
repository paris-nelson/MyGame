package Engines;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

import Enums.LocationName;
import Global.Constants;
import Objects.Location;
import Objects.Trainer;
import acm.graphics.GCanvas;
import acm.graphics.GLabel;
import acm.graphics.GLine;
import acm.program.GraphicsProgram;

public class GUI extends GraphicsProgram{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void init(){
		setSize(Constants.SCREEN_WIDTH,Constants.SCREEN_HEIGHT);
		setBackground(Color.BLACK);
		addMouseListeners();
	}

	public void run(){
		GLabel label=new GLabel("#251: Feraligatr");
		label.setFont(new Font(Constants.FONT,Font.PLAIN,17));
		System.out.println(label.getWidth());
		System.out.println(label.getHeight());
	}

	public void doStuff(){
//		GRect test=new GRect(1100,0,200,400);
//		test.setFilled(true);
//		test.setColor(Color.BLUE);
//		add(test);
//		GCompound test2=new GCompound();
//		for(int i=0;i<8;i++){
//			GLabel label=new GLabel("Enter Rocket Hideout 2 Entrance");
//			label.setColor(Color.WHITE);
//			//label.setFont(new Font(Constants.FONT,Font.PLAIN,24));
//			test2.add(label,1100,25+50*i);
//			GLine line=new GLine(1100,50*i,1300,50*i);
//			line.setColor(Color.WHITE);
//			test2.add(line);
//		}
//		add(test2);
//		pause(1000);
//		test2.move(-100,100);
	}

	public void giveControl(KeyListener kl){
		GCanvas canvas=getGCanvas();
		for(KeyListener oldlistener:canvas.getKeyListeners())
			canvas.removeKeyListener(oldlistener);
		if(kl!=null)
			getGCanvas().addKeyListener(kl);
	}

	public void mouseClicked(MouseEvent e){
		System.out.println(e.getX()+","+e.getY());
	}

	public void showTrainers(){
		int counter=50;
		for(LocationName ln:LocationName.values()){
			Location l=new Location(ln);
			for(Trainer t:l.getTrainers()){
				if(t.getX()==0){
					add(t.getImage(),counter,0);
					counter+=50;
				}
				else
					add(t.getImage());
			}
		}
	}

	public void showPoints(int interval){
		for(int x=0;x<getWidth();x+=interval){
			GLine line=new GLine(x,0,x,getHeight());
			line.setColor(Color.RED);
			add(line);
			GLabel label=new GLabel(" "+x,x,10);
			label.setColor(Color.RED);
			add(label);
		}
		for(int y=0;y<getHeight();y+=interval){
			GLine line=new GLine(0,y,getWidth(),y);
			line.setColor(Color.RED);
			add(line);
			GLabel label=new GLabel(" "+y,10,y);
			label.setColor(Color.RED);
			add(label);
		}
	}

	public void pause(){
		pause(1000);
	}

}
