package Engines;

import java.io.File;
import java.util.ArrayList;

import Enums.EventName;
import Enums.ItemType;
import Enums.LocationName;
import Enums.MoveMenuMode;
import Enums.PermCondition;
import Enums.Requirement;
import Enums.Stat;
import Global.Constants;
import Global.ControlsConfig;
import Global.GameData;
import Global.PlayerData;
import Menus.BreedMenu;
import Menus.MoveMenu;
import Objects.EliteTrainer;
import Objects.EventLogic;
import Objects.Location;
import Objects.Move;
import Objects.Pokemon;
import Objects.Trainer;
import Objects.WildTrainer;

public class GlobalEngine {

	private static int itemtouse;

	public static void initialize(){
		System.out.println("Initializing Game Data");
		GameData.initialize();
		System.out.println("Initializing Player Data");
		PlayerData.load();
		System.out.println("Initializing Controls");
		ControlsConfig.load();
		System.out.println("Initializing Complete");
		File f=new File(Constants.PATH+"\\InitializeData\\battlesavefile.txt");
		if(f.exists())
			BattleEngine.load();
		else
			MapEngine.initialize(PlayerData.getLocation());
	}

	public static void save(){
		System.out.println("Saving Player Data");
		PlayerData.save();
		if(BattleEngine.isInBattle()){
			System.out.println("Saving Battle Data");
			BattleEngine.save();
		}
		else{
			System.out.println("Saving Map Data");
			MapEngine.save();
		}
		System.out.println("Saving Controls");
		ControlsConfig.save();
		System.out.println("Saving Complete");
	}

	public static void enterBattle(Trainer opponent){
		MapEngine.close();
		BattleEngine.initialize(opponent);
	}

	public static boolean evolve(Pokemon base,String condition){
		int heldid=base.getHeldID();
		if(heldid>0&&GameData.getItemName(heldid).equals("Everstone"))
			return false;
		int evolutionnum=-1;
		if(base.getNum()==236){//tyrogue, evolves into one of three different based on stats
			int attack=base.getStat(Stat.Attack);
			int defense=base.getStat(Stat.Defense);
			if(attack>defense)
				evolutionnum=106;
			else if(defense>attack)
				evolutionnum=107;
			else
				evolutionnum=237;
		}
		else
			evolutionnum=GameData.getEvolutionNum(base.getNum(), condition);
		if(PlayerData.getLeadingPokemon().equals(base))
			MapEngine.changePlayerIcon("");
		base.evolve(evolutionnum);
		return true;
	}

	public static void triggerEvent(EventName event){
		save();
		//TODO
		EventLogic.triggerEvent(event);
	}

	public static void defeatedTrainer(Trainer defeated){
		if(!defeated.getName().equals("Challenger")){
			PlayerData.markTrainerBeaten(defeated.getID());
			MapEngine.removeTrainerFromMap(defeated,PlayerData.getLocation().getID());
		}
		rewardLoot(defeated);
		if(! (defeated instanceof WildTrainer))
			rewardMoney(defeated);
		if(defeated.getName().equals("Rival")){
			Location location=PlayerData.getLocation();
			//trigger a cutscene?
			if(location.getName()==LocationName.IlexForest){
				PlayerData.addNewPokemon(new Pokemon(175,5));
			}
		}
		else if(defeated.getName().equals("Red")){
			PlayerData.addNewPokemon(new Pokemon(1,5));
			PlayerData.addNewPokemon(new Pokemon(4,5));
			PlayerData.addNewPokemon(new Pokemon(7,5));
		}
	}

	private static void rewardMoney(Trainer defeated){
		int highestlevel=defeated.getHighestLevel();
		int base=Constants.PRIZE_MONEY_INCREMEMENT*defeated.getParty().length+Constants.PRIZE_MONEY_BASE;
		int mod=1;
		System.out.println("Player gets "+base+" prize money for their victory.");
		for(Pokemon p:PlayerData.getParty()){
			if(p.isHolding("Amulet Coin")){
				System.out.println("Player's prize money is doubled because "+p.getName()+" is holding an Amulet Coin.");
				mod=2;
				break;
			}
		}
		int paydaybonus=BattleEngine.getPayDayVal()*Constants.PAY_DAY_MULTIPLIER;
		System.out.println("Player receives a bonus of "+paydaybonus+" from uses of Pay Day.");
		System.out.println("Player gains "+(highestlevel*base*mod+paydaybonus)+" money for beating "+defeated.getName());
		PlayerData.gainMoney(highestlevel*base*mod+paydaybonus);
	}
	
	public static void loseMoney(){
		int loss=-1;
		if(PlayerData.hasMetRequirement(Requirement.PlainBadge))
			loss=PlayerData.getMoney()/2;
		else
			loss=PlayerData.getMoney()/3;
		System.out.println("Player loses "+loss+" money");
		PlayerData.loseMoney(loss);
	}
	
	public static void updateLeadingPokemon(){
		ArrayList<Pokemon> party=PlayerData.getParty();
		ArrayList<Pokemon> alive=new ArrayList<Pokemon>();
		for(Pokemon p: party){
			if(!p.isFainted())
				alive.add(p);
		}
		if(alive.size()==0)
			triggerEvent(EventName.BlackOut);
		else if(!alive.contains(PlayerData.getLeadingPokemon())){
			PlayerData.setLeadingPokemon(alive.get(0));
			MapEngine.changePlayerIcon(Constants.PATH+"\\Sprites\\Left\\"+alive.get(0).getNum()+".png");
		}
		
	}

	private static void rewardLoot(Trainer defeated){
		ArrayList<Integer> loot=generateLoot(defeated);
		System.out.println("Loot awarded: ");
		for(Integer itemid:loot){
			System.out.println(GameData.getItemName(itemid));
			if(GameData.getItemType(itemid)==ItemType.POKEMON){
				int pokenum=Integer.parseInt(GameData.getItemDescription(itemid));
				PlayerData.addNewPokemon(new Pokemon(pokenum,5));
			}
			else
				PlayerData.addItem(itemid,1);
		}
	}

	/**
	 * Generates the loot to be rewarded for beating an enemy team.
	 * @param defeated: the trainer beaten
	 * @return
	 */
	public static ArrayList<Integer> generateLoot(Trainer defeated){
		ArrayList<Integer> loot=new ArrayList<Integer>();
		int numitems=1;
		int teamsize=defeated.getParty().length;
		if(teamsize==3||teamsize==4)
			numitems=2;
		if(teamsize==5||teamsize==6)
			numitems=3;
		int highestlevel=defeated.getHighestLevel();
		for(int i=1;i<=numitems;i++){
			loot.add(GameData.getLoot(highestlevel*i/numitems,false));
		}
		if(defeated instanceof EliteTrainer)
			loot.add(GameData.getLoot(highestlevel+((EliteTrainer)defeated).getLootMod(),true));
		return loot;
	}
	/**
	 * Use an item that does not require a target
	 * @param itemid
	 */
	public static void useItem(int itemid){
		System.out.println("Using "+GameData.getItemName(itemid));
		ItemType type=GameData.getItemType(itemid);
		if(type==ItemType.BALL){
			BattleEngine.catchLogic(itemid);
		}
		else if(type==ItemType.REMATCHER){
			PlayerData.getLocation().reactivateTrainers();
		}
		else if(type==ItemType.REPEL){
			String descrip=GameData.getItemDescription(itemid);
			descrip=descrip.substring(52,descrip.indexOf(" steps"));
			MapEngine.incRepelSteps(Integer.parseInt(descrip));
			PlayerData.removeItem(itemid,1);
		}
	}

	/**
	 * Use an item that requires a party pokemon as a target
	 * @param itemid
	 * @return
	 */
	public static void useItemImpl(int itemid,Pokemon pokemon){
		if(!GameData.getItemName(itemid).startsWith("HM"))
			PlayerData.removeItem(itemid,1);
		if(BattleEngine.isInBattle()){
			InventoryEngine.close();
			BattleEngine.getActiveUnit().setHasTakenAction(true);
			BattleEngine.openUnitMenu();
		}
		else
			InventoryEngine.cleanUp();
	}

	public static void useItem(int itemid,Pokemon pokemon){
		ItemType type=GameData.getItemType(itemid);
		setItemToUse(itemid);
		if(!pokemon.isFainted()){
			if(type==ItemType.POTION){
				if(pokemon.getCurrHP()<pokemon.getStat(Stat.HP)){
					String name=GameData.getItemName(itemid);
					if(name.equals("Max Potion")){
						pokemon.restoreHP();
						System.out.println(pokemon.getName()+": HP restored to full");
						useItemImpl(itemid,pokemon);
					}
					else if(name.equals("Full Restore")){
						pokemon.restoreHP();
						pokemon.removePcondition();
						System.out.println(pokemon.getName()+": cured and HP restored to full");
						useItemImpl(itemid,pokemon);
					}
					String descrip=GameData.getItemDescription(itemid);
					String amount=descrip.substring(9,descrip.indexOf(" HP"));
					int intamount=Integer.parseInt(amount);
					pokemon.incHP(intamount);
					System.out.println(pokemon.getName()+": "+amount+" HP restored");
					useItemImpl(itemid,pokemon);
				}
				else{
					System.out.println(GameData.getItemName(itemid)+" has no effect on "+pokemon.getName());
					InventoryEngine.cleanUp();
				}
			}
			else if(type==ItemType.STATUSCURE){
				if(pokemon.getPcondition()!=null){
					String name=GameData.getItemName(itemid);
					if(name.equals("Full Heal")){
						PermCondition condition=pokemon.getPcondition();
						pokemon.removePcondition();
						if(BattleEngine.isInBattle())
							BattleEngine.getActiveUnit().removePermCondition(condition);
						System.out.println(pokemon.getName()+": cured of "+condition.toString());
						useItemImpl(itemid,pokemon);
					}
					else if(name.equals("Antidote")){
						if((pokemon.getPcondition()==PermCondition.BadlyPoison||pokemon.getPcondition()==PermCondition.Poison)){
							pokemon.removePcondition();
							System.out.println(pokemon.getName()+": status cured");
							useItemImpl(itemid,pokemon);
						}
						else{
							System.out.println(GameData.getItemName(itemid)+" has no effect on "+pokemon.getName());
							InventoryEngine.cleanUp();
						}
					}
					else{
						String condition=GameData.getItemDescription(itemid);
						condition=condition.substring(6);
						PermCondition pcondition=PermCondition.valueOf(condition);
						if(pokemon.removePcondition(pcondition)){
							System.out.println(pokemon.getName()+": status cured");
							useItemImpl(itemid, pokemon);
						}
						else{
							System.out.println(GameData.getItemName(itemid)+" has no effect on "+pokemon.getName());
							InventoryEngine.cleanUp();
						}
					}
				}
				else{
					System.out.println(GameData.getItemName(itemid)+" has no effect on "+pokemon.getName());
					InventoryEngine.cleanUp();
				}
			}
			else if(type==ItemType.STONE){
				String descrip=GameData.getItemDescription(itemid);
				if(descrip.contains(pokemon.getName())){
					String stonetype=GameData.getItemName(itemid);
					stonetype=stonetype.substring(stonetype.indexOf(" "));
					if(evolve(pokemon,stonetype))
						useItemImpl(itemid, pokemon);
					else{
						System.out.println(GameData.getItemName(itemid)+" has no effect on "+pokemon.getName());
						InventoryEngine.cleanUp();
					}
				}
				else{
					System.out.println(GameData.getItemName(itemid)+" has no effect on "+pokemon.getName());
					InventoryEngine.cleanUp();
				}
			}
			else if(type==ItemType.ETHER){
				MoveMenu mm=new MoveMenu(pokemon,MoveMenuMode.ETHER);
				MenuEngine.initialize(mm);
			}
			else if(type==ItemType.ELIXIR){
				String name=GameData.getItemName(itemid);
				ArrayList<Move> moveset=pokemon.getMoveSet();
				boolean inced=false;
				for(Move m:moveset){
					if(m.getCurrPP()<m.getCurrMax()){
						inced=true;
						if(name.startsWith("Max")){
							m.restorePP();
						}
						else{
							m.incCurrPP(10);
						}
					}
				}
				if(inced){
					System.out.println(pokemon.getName()+": curr pp of all moves restored");
					useItemImpl(itemid, pokemon);
				}
				else{
					System.out.println(GameData.getItemName(itemid)+" has no effect on "+pokemon.getName());
					InventoryEngine.cleanUp();
				}
			}
			else if(type==ItemType.VITAMIN){
				String descrip=GameData.getItemDescription(itemid);
				descrip=descrip.substring(18,descrip.indexOf(" stat"));
				if(pokemon.addEVsFromVitamin(Stat.valueOf(descrip))){
					System.out.println(pokemon.getName()+": "+descrip+" stat increased");
					useItemImpl(itemid, pokemon);
				}
				else{
					System.out.println(GameData.getItemName(itemid)+" has no effect on "+pokemon.getName());
					InventoryEngine.cleanUp();
				}
			}
			else if(type==ItemType.PPUP){
				MoveMenu mm=new MoveMenu(pokemon,MoveMenuMode.PPUP);
				MenuEngine.initialize(mm);
			}
			else if(type==ItemType.RARECANDY&&pokemon.getLevel()<100){
				System.out.print(pokemon.getName()+": leveled up from level "+pokemon.getLevel());
				pokemon.levelUp(false);
				System.out.println(" to level "+pokemon.getLevel());
			}
			else if(type==ItemType.TM||type==ItemType.HM){
				String descrip=GameData.getItemDescription(itemid);
				descrip=descrip.substring(5,descrip.indexOf(':'));
				Integer movenum=Integer.parseInt(descrip);
				if(pokemon.learnMove(new Move(movenum))){
					System.out.println(pokemon.getName()+": learned "+GameData.getMoveName(movenum));
					useItemImpl(itemid,pokemon);
				}
				else{
					System.out.println(pokemon.getName()+" wants to learn "+GameData.getMoveName(movenum)+". Needs to replace a move");
					MoveMenu mm=new MoveMenu(pokemon,MoveMenuMode.TMHM);
					MenuEngine.initialize(mm);
				}
			}
			else if(type==ItemType.BREED){
				if(GameData.getName(pokemon.getNum()).equals("Ditto"))
					System.out.println(GameData.getItemName(itemid)+" has no effect on "+pokemon.getName());
				else
					MenuEngine.initialize(new BreedMenu(pokemon));
			}
			else if(type==ItemType.REVIVE){
				System.out.println(GameData.getItemName(itemid)+" has no effect on "+pokemon.getName());
				InventoryEngine.cleanUp();
			}
		}
		else if(type==ItemType.REVIVE){
			pokemon.revive();
			if(GameData.getItemName(itemid).startsWith("Max")){
				pokemon.restoreHP();
			}
			else{
				pokemon.setHP(pokemon.getStat(Stat.HP)/2);
			}
			useItemImpl(itemid,pokemon);
		}
		else{
			System.out.println(GameData.getItemName(itemid)+" has no effect on "+pokemon.getName());
			InventoryEngine.cleanUp();
		}
	}

	public static int getItemToUse(){
		return itemtouse;
	}

	public static void setItemToUse(int itemid){
		itemtouse=itemid;
	}
	
	public static void giveUpControl(){
		GUI gui=GameData.getGUI();
		gui.giveControl(null);
	}

	public static void wait(int milliseconds){
		long starttime=System.nanoTime();
		int count=0;
		while(System.nanoTime()-starttime<milliseconds*1000000){
			count=count+count*count;
		}
	}
}
