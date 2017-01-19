package Engines;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;

import Enums.DetailsEngineMode;
import Enums.PartyMenuMode;
import Enums.PermCondition;
import Enums.Stat;
import Global.Constants;
import Global.GameData;
import Menus.OpponentDetailsMenu;
import Menus.PCDetailsMenu;
import Menus.PartyDetailsMenu;
import Objects.Move;
import Objects.Pokemon;
import acm.graphics.GCompound;
import acm.graphics.GImage;
import acm.graphics.GLabel;
import acm.graphics.GLine;
import acm.graphics.GRect;

public class DetailsEngine {

	private static Pokemon pokemon;
	private static GCompound screen;

	/**
	 * Display the details for the given pokemon as well as a menu of options based on where this screen was entered from
	 * @param p "P.C." if entering from the PC, "Party" if from player's party 
	 * @param pokemonlocation
	 */
	public static void initialize(Pokemon p,DetailsEngineMode mode){
		System.out.println("Opening Details Screen");
		pokemon=p;
		showDetails();
		if(mode==DetailsEngineMode.PC)
			MenuEngine.initialize(new PCDetailsMenu(pokemon));
		else if(mode==DetailsEngineMode.PARTYMAP)
			MenuEngine.initialize(new PartyDetailsMenu(pokemon,PartyMenuMode.VIEWMAP));
		else if(mode==DetailsEngineMode.PARTYBATTLE)
			MenuEngine.initialize(new PartyDetailsMenu(pokemon,PartyMenuMode.VIEWBATTLE));
		else if(mode==DetailsEngineMode.OPPONENT)
			MenuEngine.initialize(new OpponentDetailsMenu());
	}

	private static void showDetails(){	
		screen=new GCompound();

		//create objects, no position or formatting
		GImage sprite=null;
		try{
			sprite=new GImage(Constants.PATH+"Sprites\\"+pokemon.getNum()+".png");
		}
		catch(Exception e){
			sprite=new GImage(Constants.PATH+"Sprites\\180.png");
		}
		sprite.setSize(75,75);
		GLabel hp=new GLabel("HP:");
		hp.setFont(new Font(Constants.FONT,Font.PLAIN,28));
		int currhpint=pokemon.getCurrHP();
		int maxhpint=pokemon.getStat(Stat.HP);
		GLabel name=new GLabel(pokemon.getName());
		name.setFont(new Font(Constants.FONT,Font.BOLD,32));
		GLabel gender=new GLabel(pokemon.getGender().toString());
		gender.setFont(new Font(Constants.FONT,Font.ITALIC,24));
		GLabel currhp=new GLabel(""+currhpint);
		currhp.setFont(new Font(Constants.FONT,Font.BOLD,28));
		GLabel maxhp=new GLabel("/"+maxhpint);
		maxhp.setFont(new Font(Constants.FONT,Font.BOLD,28));
		double quotient=((double)currhpint/(double)maxhpint);
		if(quotient<.2)
			currhp.setColor(Color.RED);
		else if(quotient<.4)
			currhp.setColor(Color.YELLOW);
		else
			currhp.setColor(Color.GREEN);
		PermCondition pcon=pokemon.getPcondition();
		GLabel condition=new GLabel("Condition:");
		GLabel pcondition;
		if(pcon==null){
			pcondition=new GLabel("    Healthy");
			pcondition.setColor(Color.GREEN);
		}
		else{
			pcondition=new GLabel("    "+pcon.toString());
			if(pcon==PermCondition.Sleep)
				pcondition.setColor(Color.BLUE);
			else if(pcon==PermCondition.BadlyPoison)
				pcondition.setColor(Color.BLACK);
			else if(pcon==PermCondition.Burn)
				pcondition.setColor(Color.RED);
			else if(pcon==PermCondition.Frozen)
				pcondition.setColor(Color.CYAN);
			else if(pcon==PermCondition.Paralysis)
				pcondition.setColor(Color.GRAY);
			else if(pcon==PermCondition.Poison)
				pcondition.setColor(Color.MAGENTA);
		}
		condition.setFont(new Font(Constants.FONT,Font.PLAIN,24));
		pcondition.setFont(new Font(Constants.FONT,Font.PLAIN,24));
		int id=pokemon.getHeldID();
		GLabel helditemname;
		if(id>=0)
			helditemname=new GLabel("Held Item: "+GameData.getItemName(id));
		else
			helditemname=new GLabel("Held Item: No Held Item");
		helditemname.setFont(new Font(Constants.FONT,Font.BOLD,24));
		GLabel happiness=new GLabel("Happiness:    "+pokemon.getHappiness());
		happiness.setFont(new Font(Constants.FONT,Font.PLAIN,24));
		int lvl=pokemon.getLevel();
		int currxpint=pokemon.getExp();
		int prevxpint=GameData.getExpThreshold(pokemon.getNum(),lvl);
		int nextxpint=GameData.getExpThreshold(pokemon.getNum(),lvl+1);
		int totalxpdiff=nextxpint-prevxpint;
		int currxpdiff=currxpint-prevxpint;
		GLabel level=new GLabel("Level "+lvl);
		level.setFont(new Font(Constants.FONT,Font.BOLD,24));
		GLabel currxp=new GLabel(""+currxpint);
		currxp.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		GLabel nextxp=new GLabel("/"+nextxpint);
		nextxp.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		GLabel nextlvl=new GLabel("Level "+Math.min(100,(lvl+1)));
		nextlvl.setFont(new Font(Constants.FONT,Font.BOLD,24));
		double xpratio=((double)currxpdiff/(double)totalxpdiff);
		double barratio=xpratio*250.0;
		GRect xpbar=new GRect(barratio,nextlvl.getHeight());
		xpbar.setFilled(true);
		xpbar.setColor(Color.BLACK);
		GRect xpstart=new GRect(5,nextlvl.getHeight()+5);
		xpstart.setFilled(true);
		xpstart.setColor(Color.RED);
		GRect xpend=new GRect(5,xpstart.getHeight());
		xpend.setFilled(true);
		xpend.setColor(Color.RED);
		int[] stats=pokemon.getStats();
		GLabel currstat1=new GLabel(""+stats[0]);
		currstat1.setFont(new Font(Constants.FONT,Font.BOLD,22));
		GLabel currstat2=new GLabel(""+stats[1]);
		currstat2.setFont(new Font(Constants.FONT,Font.BOLD,22));
		GLabel currstat3=new GLabel(""+stats[2]);
		currstat3.setFont(new Font(Constants.FONT,Font.BOLD,22));
		GLabel currstat4=new GLabel(""+stats[3]);
		currstat4.setFont(new Font(Constants.FONT,Font.BOLD,22));
		GLabel currstat5=new GLabel(""+stats[4]);
		currstat5.setFont(new Font(Constants.FONT,Font.BOLD,22));
		GLabel currstat6=new GLabel(""+stats[5]);
		currstat6.setFont(new Font(Constants.FONT,Font.BOLD,22));
		stats=pokemon.getIVs();
		GLabel iv1=new GLabel(""+stats[0]);
		iv1.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		GLabel iv2=new GLabel(""+stats[1]);
		iv2.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		GLabel iv3=new GLabel(""+stats[2]);
		iv3.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		GLabel iv4=new GLabel(""+stats[3]);
		iv4.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		GLabel iv5=new GLabel(""+stats[4]);
		iv5.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		GLabel iv6=new GLabel(""+stats[5]);
		iv6.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		stats=pokemon.getEVs();
		GLabel ev1=new GLabel(""+stats[0]);
		ev1.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		GLabel ev2=new GLabel(""+stats[1]);
		ev2.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		GLabel ev3=new GLabel(""+stats[2]);
		ev3.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		GLabel ev4=new GLabel(""+stats[3]);
		ev4.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		GLabel ev5=new GLabel(""+stats[4]);
		ev5.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		GLabel ev6=new GLabel(""+stats[5]);
		ev6.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		ArrayList<Move> moves=pokemon.getMoveSet();
		GLabel movename1=null,movecurrpp1=null,movemaxpp1=null,movename2=null,movecurrpp2=null,movemaxpp2=null,
				movename3=null,movecurrpp3=null,movemaxpp3=null,movename4=null,movecurrpp4=null,movemaxpp4=null;
		if(moves.size()>0){
			Move move1=moves.get(0);
			int movenum1=move1.getNum();
			int currpp=move1.getCurrPP();
			int maxpp=move1.getCurrMax();
			movename1=new GLabel(GameData.getMoveName(movenum1));
			movename1.setFont(new Font(Constants.FONT,Font.BOLD,24));
			movecurrpp1=new GLabel(""+currpp);
			movecurrpp1.setFont(new Font(Constants.FONT,Font.PLAIN,22));
			movemaxpp1=new GLabel("/"+maxpp);
			movemaxpp1.setFont(new Font(Constants.FONT,Font.PLAIN,22));
			quotient=((double)currpp)/((double)maxpp);
			if(quotient<.2)
				movecurrpp1.setColor(Color.YELLOW);
			else if(quotient<.4)
				movecurrpp1.setColor(Color.RED);
			else
				movecurrpp1.setColor(Color.GREEN);
			if(moves.size()>1){
				move1=moves.get(1);
				movenum1=move1.getNum();
				currpp=move1.getCurrPP();
				maxpp=move1.getCurrMax();
				movename2=new GLabel(GameData.getMoveName(movenum1));
				movename2.setFont(new Font(Constants.FONT,Font.BOLD,24));
				movecurrpp2=new GLabel(""+currpp);
				movecurrpp2.setFont(new Font(Constants.FONT,Font.PLAIN,22));
				movemaxpp2=new GLabel("/"+maxpp);
				movemaxpp2.setFont(new Font(Constants.FONT,Font.PLAIN,22));
				quotient=((double)currpp)/((double)maxpp);
				if(quotient<.2)
					movecurrpp2.setColor(Color.YELLOW);
				else if(quotient<.4)
					movecurrpp2.setColor(Color.RED);
				else
					movecurrpp2.setColor(Color.GREEN);
				if(moves.size()>2){
					move1=moves.get(2);
					movenum1=move1.getNum();
					currpp=move1.getCurrPP();
					maxpp=move1.getCurrMax();
					movename3=new GLabel(GameData.getMoveName(movenum1));
					movename3.setFont(new Font(Constants.FONT,Font.BOLD,24));
					movecurrpp3=new GLabel(""+currpp);
					movecurrpp3.setFont(new Font(Constants.FONT,Font.PLAIN,22));
					movemaxpp3=new GLabel("/"+maxpp);
					movemaxpp3.setFont(new Font(Constants.FONT,Font.PLAIN,22));
					quotient=((double)currpp)/((double)maxpp);
					if(quotient<.2)
						movecurrpp3.setColor(Color.YELLOW);
					else if(quotient<.4)
						movecurrpp3.setColor(Color.RED);
					else
						movecurrpp3.setColor(Color.GREEN);
					if(moves.size()>3){
						System.out.println(moves);
						move1=moves.get(3);
						movenum1=move1.getNum();
						currpp=move1.getCurrPP();
						maxpp=move1.getCurrMax();
						movename4=new GLabel(GameData.getMoveName(movenum1));
						movename4.setFont(new Font(Constants.FONT,Font.BOLD,24));
						movecurrpp4=new GLabel(""+currpp);
						movecurrpp4.setFont(new Font(Constants.FONT,Font.PLAIN,22));
						movemaxpp4=new GLabel("/"+maxpp);
						movemaxpp4.setFont(new Font(Constants.FONT,Font.PLAIN,22));
						quotient=((double)currpp)/((double)maxpp);
						if(quotient<.2)
							movecurrpp4.setColor(Color.YELLOW);
						else if(quotient<.4)
							movecurrpp4.setColor(Color.RED);
						else
							movecurrpp4.setColor(Color.GREEN);
					}
				}
			}
		}
		double labelheight=name.getHeight()+10;
		GRect bg=new GRect(0,0,Constants.SCREEN_WIDTH,Constants.SCREEN_HEIGHT);
		bg.setFilled(true);
		bg.setFillColor(Color.WHITE);
		screen.add(bg);
		screen.add(sprite,0,0);
		screen.add(name,sprite.getWidth()+20,labelheight);
		screen.add(gender,name.getX()+15+name.getWidth(),labelheight);
		screen.add(hp,gender.getX()+40+gender.getWidth(),labelheight);
		screen.add(currhp,hp.getX()+20+hp.getWidth(),labelheight);
		screen.add(maxhp,currhp.getX()+currhp.getWidth(),labelheight);
		screen.add(level,20,sprite.getHeight()+labelheight);
		screen.add(currxp,level.getX()+level.getWidth()+(270-currxp.getWidth()-nextxp.getWidth())/2,level.getY()+25);
		screen.add(nextxp,currxp.getX()+currxp.getWidth(),currxp.getY());
		screen.add(nextlvl,level.getX()+level.getWidth()+270,level.getY());
		screen.add(condition,nextlvl.getX()+nextlvl.getWidth()+30,level.getY());
		screen.add(pcondition,condition.getX()+condition.getWidth(),condition.getY());
		screen.add(happiness,pcondition.getX()+30+pcondition.getWidth(),pcondition.getY());
		screen.add(xpstart,level.getX()+level.getWidth()+5,level.getY()-xpbar.getHeight()-5);
		screen.add(xpend,nextlvl.getX()-10,xpstart.getY());
		screen.add(xpbar,xpstart.getX()+5,xpstart.getY()+5);
		screen.add(helditemname,25,nextlvl.getY()+labelheight+20);

		GLabel ivslabel=new GLabel("IVs");
		ivslabel.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		GLabel evslabel=new GLabel("EVs");
		evslabel.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		GLabel statslabel=new GLabel("Stats");
		statslabel.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		GLabel hplabel=new GLabel("HP");
		hplabel.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		GLabel att=new GLabel("Attack");
		att.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		GLabel def=new GLabel("Defense");
		def.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		GLabel satt=new GLabel("Sp. Attack");
		satt.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		GLabel sdef=new GLabel("Sp. Defense");
		sdef.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		GLabel speed=new GLabel("Speed");
		speed.setFont(new Font(Constants.FONT,Font.PLAIN,22));
		double ytop=helditemname.getY()+10;
		int xinc=150;
		screen.add(hplabel,75+(xinc-hplabel.getWidth())/2,ytop+hplabel.getHeight());
		screen.add(att,75+xinc+(xinc-att.getWidth())/2,hplabel.getY());
		screen.add(def,75+2*xinc+(xinc-def.getWidth())/2,hplabel.getY());
		screen.add(satt,75+3*xinc+(xinc-satt.getWidth())/2,hplabel.getY());
		screen.add(sdef,75+4*xinc+(xinc-sdef.getWidth())/2,hplabel.getY());
		screen.add(speed,75+5*xinc+(xinc-speed.getWidth())/2,hplabel.getY());
		ytop+=50;
		for(int x=75;x<=75+6*xinc;x+=xinc){
			GLine line=new GLine(x,ytop,x,ytop+150);
			screen.add(line);
		}
		for(int y=0;y<=150;y+=50){
			GLine line=new GLine(75,ytop+y,75+6*xinc,ytop+y);
			screen.add(line);
		}
		screen.add(ivslabel,10,ivslabel.getHeight()/2+ytop+20);
		screen.add(evslabel,10,evslabel.getHeight()/2+ytop+70);
		screen.add(statslabel,10,hplabel.getHeight()/2+ytop+120);
		screen.add(iv1,75+(xinc-iv1.getWidth())/2,ivslabel.getY());
		screen.add(iv2,75+xinc+(xinc-iv2.getWidth())/2,ivslabel.getY());
		screen.add(iv3,75+2*xinc+(xinc-iv3.getWidth())/2,ivslabel.getY());
		screen.add(iv4,75+3*xinc+(xinc-iv4.getWidth())/2,ivslabel.getY());
		screen.add(iv5,75+4*xinc+(xinc-iv5.getWidth())/2,ivslabel.getY());
		screen.add(iv6,75+5*xinc+(xinc-iv6.getWidth())/2,ivslabel.getY());
		screen.add(ev1,75+(xinc-ev1.getWidth())/2,evslabel.getY());
		screen.add(ev2,75+xinc+(xinc-ev2.getWidth())/2,evslabel.getY());
		screen.add(ev3,75+2*xinc+(xinc-ev3.getWidth())/2,evslabel.getY());
		screen.add(ev4,75+3*xinc+(xinc-ev4.getWidth())/2,evslabel.getY());
		screen.add(ev5,75+4*xinc+(xinc-ev5.getWidth())/2,evslabel.getY());
		screen.add(ev6,75+5*xinc+(xinc-ev6.getWidth())/2,evslabel.getY());
		screen.add(currstat1,75+(xinc-currstat1.getWidth())/2,statslabel.getY());
		screen.add(currstat2,75+xinc+(xinc-currstat2.getWidth())/2,statslabel.getY());
		screen.add(currstat3,75+2*xinc+(xinc-currstat3.getWidth())/2,statslabel.getY());
		screen.add(currstat4,75+3*xinc+(xinc-currstat4.getWidth())/2,statslabel.getY());
		screen.add(currstat5,75+4*xinc+(xinc-currstat5.getWidth())/2,statslabel.getY());
		screen.add(currstat6,75+5*xinc+(xinc-currstat6.getWidth())/2,statslabel.getY());
		if(movename1!=null){
			screen.add(movename1,50,ytop+150+labelheight);
			screen.add(movecurrpp1,movename1.getX()+movename1.getWidth()+30,movename1.getY());
			screen.add(movemaxpp1,movecurrpp1.getX()+movecurrpp1.getWidth(),movename1.getY());
		}
		if(movename2!=null){
			screen.add(movename2,movemaxpp1.getX()+movemaxpp1.getWidth()+100,movename1.getY());
			screen.add(movecurrpp2,movename2.getX()+movename2.getWidth()+30,movename1.getY());
			screen.add(movemaxpp2,movecurrpp2.getX()+movecurrpp2.getWidth(),movename1.getY());
		}
		if(movename3!=null){
			screen.add(movename3,50,movename1.getY()+labelheight);
			screen.add(movecurrpp3,movename3.getX()+movename3.getWidth()+30,movename3.getY());
			screen.add(movemaxpp3,movecurrpp3.getX()+movecurrpp3.getWidth(),movename3.getY());
		}
		if(movename4!=null){
			screen.add(movename4,movename2.getX(),movename3.getY());
			screen.add(movecurrpp4,movename4.getX()+movename4.getWidth()+30,movename3.getY());
			screen.add(movemaxpp4,movecurrpp4.getX()+movecurrpp4.getWidth(),movename3.getY());
		}
		GameData.getGUI().add(screen,0,0);
	}

	public static void close(){
		System.out.println("Closing Details Screen");
		GameData.getGUI().remove(screen);
	}

}
