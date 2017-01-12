package Menus;

import java.util.ArrayList;

import Engines.BattleEngine;
import Engines.GlobalEngine;
import Engines.InventoryEngine;
import Engines.MenuEngine;
import Enums.MoveMenuMode;
import Global.GameData;
import Global.PlayerData;
import Objects.Move;
import Objects.Pokemon;
import Objects.Unit;

public class MoveMenu implements MenuWithExplanations {

	private ArrayList<String> options=new ArrayList<String>();
	private Pokemon pokemon;
	private MoveMenuMode mode;
	private int itemid;

	public MoveMenu(Pokemon p,MoveMenuMode m){
		mode=m;
		itemid=GlobalEngine.getItemToUse();
		pokemon=p;
		ArrayList<Move> moveset=p.getMoveSet();
		for(int i=0;i<moveset.size();i++){
			Move move=moveset.get(i);
			if(m==MoveMenuMode.ATTACK&&BattleEngine.getActiveUnit().getDisabledMove()==move.getNum())
				continue;
			options.add(GameData.getMoveName(move.getNum()));
		}
		options.add("Back");
	}

	@Override
	public void refreshVisibleOptions() {

	}

	@Override
	public int getNumOptions() {
		return options.size();
	}

	@Override
	public ArrayList<String> getVisibleOptions() {
		return options;
	}
	
	@Override
	public String explain(short index){
		if(index<options.size()-1)
			return GameData.getMoveDescription(pokemon.getMoveSet().get(index).getNum());
		return "";
	}

	@Override
	public void select(short index) {
		MenuEngine.close();
		if(!options.get(index).equals("Back")){
			Move m=pokemon.getMoveSet().get(index);
			if(mode==MoveMenuMode.ETHER){
				if(m.getCurrPP()<m.getCurrMax()){
					int pre=m.getCurrPP();
					if(GameData.getItemName(itemid).startsWith("Max"))
						m.restorePP();
					else
						m.incCurrPP(10);
					PlayerData.removeItem(itemid,1);
					System.out.println(GameData.getMoveName(m.getNum())+" curr pp increased from "+pre+" to "+m.getCurrPP());
				}
				else
					System.out.println(GameData.getItemName(itemid)+" has no effect on "+pokemon.getName());
				InventoryEngine.cleanUp();
			}
			else if(mode==MoveMenuMode.PPUP){
				if(m.getCurrMax()<GameData.getMoveMaxPP(m.getNum())){
					int pre=m.getCurrMax();
					if(GameData.getItemName(itemid).endsWith("Max"))
						m.maxPP();
					else
						m.incMaxPP();
					PlayerData.removeItem(itemid,1);
					System.out.println(GameData.getMoveName(m.getNum())+" max pp increased from "+pre+" to "+m.getCurrMax());
				}
				else
					System.out.println(GameData.getItemName(itemid)+" has no effect on "+pokemon.getName());
				InventoryEngine.cleanUp();
			}
			else if(mode==MoveMenuMode.TMHM){
				String descrip=GameData.getItemDescription(itemid);
				descrip=descrip.substring(5,descrip.indexOf(':'));
				Integer movenum=Integer.parseInt(descrip);
				pokemon.replaceMove(index,new Move(movenum));
				System.out.println(GameData.getMoveName(m.getNum())+" replaced with "+GameData.getMoveName(movenum));
				InventoryEngine.cleanUp();
			}
			else if(mode==MoveMenuMode.ATTACK&&BattleEngine.isLegalMove(m)){
				if(m.hasPP())
					BattleEngine.useMove(m);
				else{
					options.set(index,"Out of PP!");
					MenuEngine.refreshMenu();
				}
			}
		}
		else if(mode==MoveMenuMode.ATTACK)
			BattleEngine.openUnitMenu();
	}
}
