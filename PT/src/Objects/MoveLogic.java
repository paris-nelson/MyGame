package Objects;

import java.util.ArrayList;
import java.util.HashMap;

import Engines.BattleEngine;
import Engines.GlobalEngine;
import Engines.MenuEngine;
import Enums.BondCondition;
import Enums.MoveEffect;
import Enums.MoveMenuMode;
import Enums.PermCondition;
import Enums.ProtectionCondition;
import Enums.Stat;
import Enums.TempCondition;
import Enums.Type;
import Global.Constants;
import Global.GameData;
import Menus.MoveMenu;

public class MoveLogic {
	private static Unit user;
	private static Pokemon userpokemon;
	private static ArrayList<Unit> targets;
	private static Move move;
	private static HashMap<MoveEffect,HashMap<String,String>> effects;
	private static HashMap<String,String> curreffects;
	private static int damagedone;


	public static void implementEffects(Unit thisuser,ArrayList<Unit> thistargets,Move thismove){
		user=thisuser;
		userpokemon=user.getPokemon();
		
		targets=thistargets;
		move=thismove;
		effects=GameData.getMoveEffects(move.getNum());
//		HashMap<MoveEffect,HashMap<String,String>> selftargetting=getSelfTargettingEffects();
		int count=0;
		damagedone=0;
		for(Unit u:targets){
			if(doesMoveHit(u,user,move.getNum())){
				if(count==0){
					if(user.getPrevMove()==move.getNum())
						user.incNumConsecUses();
					else
						user.resetConsecUses();
				}
				count++;
				//TODO: should look into maybe sorting effects in movefile so they are executed in particular order. for example with brickbreaker, where 
				//shields need to be broken first, then damage calculated
				for(MoveEffect effect:effects.keySet()){
					implement(effect,effects.get(effect),u);
				}
				if(userpokemon.isHolding("King's Rock")&&effects.size()==1
						&&effects.containsKey(MoveEffect.Damage)&&GameData.getRandom().nextInt(100)<Constants.KINGS_ROCK_FLINCH_CHANCE){
					HashMap<String,String> map=new HashMap<String,String>();
					map.put("Condition","Flinch");
					implement(MoveEffect.GiveTCondition,map,u);
				}
			}
			else{
				System.out.println("The move does not hit");
				user.resetConsecUses();
			}
		}
//		//do self targetting effects last
//		for(MoveEffect effect:selftargetting.keySet()){
//			if(effect==MoveEffect.MissRecoil&&count>0)
//				continue;
//			implement(effect,selftargetting.get(effect),user);
//		}
	}

	/**
	 * Returns the map of effects that target the move user and pulls them out of the original effects map
	 * @return
	 */
	private static HashMap<MoveEffect,HashMap<String,String>> getSelfTargettingEffects(){
		HashMap<MoveEffect,HashMap<String,String>> selftargetting=new HashMap<MoveEffect,HashMap<String,String>>();
		for(MoveEffect effect:effects.keySet()){
			HashMap<String,String> params=effects.get(effect);
			String target=params.get("Target");
			if(target!=null&&target.equals("Self")){
				selftargetting.put(effect,params);
				effects.remove(effect);
			}
		}
		return selftargetting;
	}

	private static void implement(MoveEffect effect,HashMap<String,String> params,Unit target){
		curreffects=params;
		if(effect==MoveEffect.Damage)
			implementDamageEffect(target);
		else if(effect==MoveEffect.Buff||effect==MoveEffect.Nerf)
			implementBuffNerfEffect(effect,target);
		else if(effect==MoveEffect.PayDay)
			BattleEngine.incPayDayVal(userpokemon.getLevel());
		else if(effect.toString().contains("Condition"))
			implementConditionEffect(effect,target);
		else if(effect==MoveEffect.HealthSteal){
			int percentage=damagedone/2;
			userpokemon.incHP(percentage);
			System.out.println(userpokemon.getName()+" heals "+percentage+" HP.");
		}
		else if(effect==MoveEffect.Recoil){
			int percentage=damagedone*round((Double.parseDouble(curreffects.get("Percentage"))/100));
			user.damage(percentage);
			System.out.println(userpokemon.getName()+" takes "+percentage+" recoil damage.");
		}
		else if(effect==MoveEffect.HealthSac){
			System.out.println(userpokemon.getName()+" loses half of their max HP");
			userpokemon.decHP(userpokemon.getStat(Stat.HP)/2);
		}
		else if(effect==MoveEffect.Conversion){
			user.setTypes(target.getTypes());
			System.out.println(userpokemon.getName()+"'s types have been changed to "+user.getTypes());
		}
		else if(effect==MoveEffect.Conversion2){
			Type lastmovetype=GameData.getMoveType(target.getPrevMove());
			ArrayList<Type> newtype=new ArrayList<Type>();
			newtype.add(GameData.getResistantType(lastmovetype));
			user.setTypes(newtype);
			System.out.println(userpokemon.getName()+"'s types have been changed to "+user.getTypes().get(0));
		}
		else if(effect==MoveEffect.SelfDestruct){
			System.out.println(userpokemon.getName()+" self destructs");
			userpokemon.decHP(userpokemon.getCurrHP());
		}
		else if(effect==MoveEffect.StatStageReset){
			System.out.println(userpokemon.getName()+"'s stats are reset to unmodified values");
			user.clearStatMods();
		}
		else if(effect==MoveEffect.Recharge){
			System.out.println(userpokemon.getName()+" must recharge next turn");
			user.isCharging();
			user.setCanMove(false);
		}
		else if(effect==MoveEffect.Heal){
			int amount=target.getPokemon().getStat(Stat.HP)*round((Double.parseDouble(curreffects.get("Percentage"))/100));
			System.out.println(target.getPokemon().getName()+" heals "+amount+" HP.");
			target.getPokemon().incHP(amount);
		}
		else if(effect==MoveEffect.TimeHeal){
			double percent=Constants.HEAL_RIGHT_TIME_PERCENTAGE;
			if(Enums.Time.valueOf(curreffects.get("Time"))!=GameData.getTime())
				percent=Constants.HEAL_WRONG_TIME_PERCENTAGE;
			int amount=round(target.getPokemon().getStat(Stat.HP)*percent);
			System.out.println(target.getPokemon().getName()+" heals "+amount+" HP.");
			target.getPokemon().incHP(amount);
		}
		else if(effect==MoveEffect.PsychUp){
			System.out.println(userpokemon.getName()+" copies "+target.getPokemon().getName()+"'s stat modifications");
			user.copyStatMods(target);
		}
		else if(effect==MoveEffect.Spite){
			Move move=userpokemon.getMove(user.getPrevMove());
			move.decCurrPP(GameData.getRandom().nextInt(Constants.SPITE_MAX_PP-Constants.SPITE_MIN_PP+1)+Constants.SPITE_MIN_PP);
			System.out.println(userpokemon.getName()+"'s move "+GameData.getMoveName(move.getNum())+" PP reduced to "+move.getCurrPP());
		}
		else if(effect==MoveEffect.Splash)
			System.out.println(userpokemon.getName()+" splashes around, doing nothing.");
		else if(effect==MoveEffect.Thief){
			if(target.getPokemon().getHeldID()!=-1&&userpokemon.getHeldID()==-1&&GameData.getRandom().nextInt(100)<Constants.THIEF_STEAL_CHANCE){
				int id=target.getPokemon().removeHeldItem();
				System.out.println(userpokemon.getName()+" steals "+GameData.getItemName(id)+" from "+target.getPokemon().getName());
				userpokemon.holdItem(id);
			}
		}
		else if(effect==MoveEffect.RemoveSpikes){
			System.out.println("Spikes are removed from the battlefield");
			BattleEngine.removeSpikes();
		}
		else if(effect==MoveEffect.Rage){
			System.out.println(userpokemon.getName()+" has become enraged");
			user.setRaging(true);
		}
		else if(effect==MoveEffect.BatonPass){
			System.out.println(userpokemon.getName()+" swapping positions with "+target.getPokemon().getName());
			swapPositions(user,target);
		}
		else if(effect==MoveEffect.RandomSwap){
			ArrayList<Unit> options=BattleEngine.getFriendlyUnits(target);
			options.remove(target);
			Unit othertarget=options.get(GameData.getRandom().nextInt(options.size()));
			System.out.println(othertarget.getPokemon().getName()+" swapping positions with "+target.getPokemon().getName());
			swapPositions(othertarget,target);
		}
		else if(effect==MoveEffect.Dig&&!user.isDigging()){
			user.setDigging(true);
			System.out.println(userpokemon.getName()+" digs down to attack");
		}
		else if(effect==MoveEffect.Fly&&!user.isFlying()){
			user.setFlying(true);
			System.out.println(userpokemon.getName()+" flies up to attack");
		}
		else if(effect==MoveEffect.ChargeUp&&!user.isCharging()){
			user.setCharging(true);
			System.out.println(userpokemon.getName()+" charges up to attack");
		}
	}

	private static void swapPositions(Unit a,Unit b){
		IntPair apair=a.getCoordinates();
		IntPair bpair=b.getCoordinates();
		BattleEngine.moveOffSquare(a,apair.getX(),apair.getY());
		BattleEngine.moveOffSquare(b,bpair.getX(),bpair.getY());
		BattleEngine.moveOnSquare(b,apair.getX(),apair.getY());
		BattleEngine.moveOnSquare(a,bpair.getX(),bpair.getY());
	}

	private static void implementConditionEffect(MoveEffect effect,Unit target){
		if(curreffects.containsKey("Chance")&&GameData.getRandom().nextInt(100)<Integer.parseInt(curreffects.get("Chance"))){
			if(effect==MoveEffect.GiveTCondition){
				if(target.addTempCondition(TempCondition.valueOf(curreffects.get("Condition"))))
					System.out.println(target.getPokemon().getName()+" is now afflicted by "+curreffects.get("Condition"));
			}
			else if(effect==MoveEffect.GivePCondition){
				if(target.addPermCondition(PermCondition.valueOf(curreffects.get("Condition"))))
					System.out.println(target.getPokemon().getName()+" is now afflicted by "+curreffects.get("Condition"));
			}
			else if(effect==MoveEffect.GiveBondCondition){
				if(target.addBondCondition(BondCondition.valueOf(curreffects.get("Condition")),target.getID()))
					System.out.println(target.getPokemon().getName()+" is now bound to "+userpokemon.getName()+" by "+curreffects.get("Condition"));
			}
			else if(effect==MoveEffect.GiveProtectionCondition){
				if(target.addProtectionCondition(ProtectionCondition.valueOf(curreffects.get("Condition"))))
					System.out.println(target.getPokemon().getName()+" is now protected by "+curreffects.get("Condition"));
			}
			else if(effect==MoveEffect.RemoveTCondition){
				if(target.removeTempCondition(TempCondition.valueOf(curreffects.get("Condition"))))
					System.out.println(target.getPokemon().getName()+" is now cured of "+curreffects.get("Condition"));
			}
			else if(effect==MoveEffect.RemovePCondition){
				if(target.removePermCondition(PermCondition.valueOf(curreffects.get("Condition"))))
					System.out.println(target.getPokemon().getName()+" is now cured of "+curreffects.get("Condition"));
			}
			else if(effect==MoveEffect.RemoveBondCondition){
				if(target.removeBondCondition(BondCondition.valueOf(curreffects.get("Condition"))))
					System.out.println(target.getPokemon().getName()+" is no longer bound to "+userpokemon.getName()+" by "+curreffects.get("Condition"));
			}
			else if(effect==MoveEffect.RemoveProtectionCondition){
				if(target.removeProtectionCondition(ProtectionCondition.valueOf(curreffects.get("Condition"))))
					System.out.println(target.getPokemon().getName()+" is no longer protected by "+curreffects.get("Condition"));
			}
		}
	}

	private static void implementBuffNerfEffect(MoveEffect effect,Unit target){
		if(target.hasProtectionCondition(ProtectionCondition.Mist)&&!target.equals(user)){
			System.out.println(target.getPokemon().getName()+" cannot have it's stats modified because it is protected by Mist");
		}
		else if(curreffects.containsKey("Chance")&&GameData.getRandom().nextInt(100)<Integer.parseInt(curreffects.get("Chance"))){
			if(effect==MoveEffect.Buff){
				System.out.println(target.getPokemon().getName()+"'s "+curreffects.get("Stat")+" increased "+curreffects.get("Stages")+" stages.");
				target.incStat(Integer.parseInt(curreffects.get("Stages")),Stat.valueOf(curreffects.get("Stat")));
			}
			else{
				System.out.println(target.getPokemon().getName()+"'s "+curreffects.get("Stat")+" decreased "+curreffects.get("Stages")+" stages.");
				target.decStat(Integer.parseInt(curreffects.get("Stages")),Stat.valueOf(curreffects.get("Stat")));
			}
		}
	}

	private static void implementDamageEffect(Unit target){
		int damage=0;
		String power=curreffects.get("Power");
		String param=curreffects.get("Type");
		//If the user is not currently dug/flying/charging then don't do the damage portion yet.
		if(param!=null){
			if(param.equals("Dig")&&!user.isDigging())
				return;
			if(param.equals("Fly")&&!user.isFlying())
				return;
			if(param.equals("ChargeUp")&&!user.isCharging())
				return;
		}
		try{
			//pre damage calculation parameters that affect ultimate damage calculation
			int powernum=Integer.parseInt(power);
			int numtimes=1;
			if(param!=null){
				if(param.equals("DoubleAttack"))
					numtimes=2;
				else if(param.equals("TripleAttack"))
					numtimes=3;
				else if(param.equals("TripleAttack"))
					numtimes=GameData.getRandom().nextInt(4)+2;
				else if(param.equals("BeatUp")){
					for(Unit u:BattleEngine.getFriendlyUnits(user)){
						if(u.equals(user))
							continue;
						damage+=calculateDamage(u,target,null,10);
					}
				}
			}
			System.out.println("Attacking "+numtimes+" times.");
			for(int i=0;i<numtimes;i++){
				damage+=calculateDamage(user,target,GameData.getMoveType(move.getNum()),powernum);
			}
		}
		catch(Exception e){
			//parameters that determine base power or damage calculations
			if(power.equals("Invariant"))
				damage=Integer.parseInt(curreffects.get("Damage"));
			else if(power.equals("OneHitKO"))
				damage=target.getPokemon().getCurrHP();
			else if(power.equals("Flail"))
				damage=calculateDamage(user,target,GameData.getMoveType(move.getNum()),calculateFlailPower());
			else if(power.equals("Hidden Power"))
				damage=calculateDamage(user,target,calculateHiddenType(),calculateHiddenPower());
			else if(power.equals("Magnitude"))
				damage=calculateDamage(user,target,GameData.getMoveType(move.getNum()),calculateMagnitudePower());
			else if(power.equals("Level"))
				damage=userpokemon.getLevel();
			else if(power.equals("Present"))
				damage=calculatePresentDamage(target);
			else if(power.equals("Psywave"))
				damage=calculatePsywaveDamage();
			else if(power.equals("HappinessBased"))
				damage=calculateDamage(user,target,GameData.getMoveType(move.getNum()),calculateHappinessBasedPower());
			else if(power.equals("HalveHP"))
				damage=target.getPokemon().getCurrHP()/2;
		}
		if(param!=null){
			//post damage calculation parameters that affect ultimate damage calculation
			if(param.equals("DigMultiplier")&&target.isDigging()){
				System.out.println(target.getPokemon().getName()+" takes double damage from "
						+GameData.getMoveName(move.getNum())+" because it is digging");
				damage*=2;
			}
			else if(param.equals("FlyMultiplier")&&target.isFlying()){
				System.out.println(target.getPokemon().getName()+" takes double damage from "
						+GameData.getMoveName(move.getNum())+" because it is flying");
				damage*=2;
			}
			else if(param.equals("MinimizeMultiplier")&&target.isMinimized()){
				System.out.println(target.getPokemon().getName()+" takes double damage from "
						+GameData.getMoveName(move.getNum())+" because it is minimized");
				damage*=2;
			}
			else if(param.equals("Flanking")&&user.isFlanking(target)){
				System.out.println(target.getPokemon().getName()+" takes extra damage from "
						+GameData.getMoveName(move.getNum())+" because it is being flanked");
				damage*=2;
			}
			else if(param.equals("ConsecPowInc")&&user.getNumConsecUses()>0){
				damage*=Math.pow(2,user.getNumConsecUses());
				System.out.println(target.getPokemon().getName()+" takes "+Math.pow(2,user.getNumConsecUses())+" times damage "
						+" because "+GameData.getMoveName(move.getNum())+" has been used "+(user.getNumConsecUses()+1)+" turns.");
			}
			if((param.equals("CannotKill")||
					(target.getPokemon().isHolding("Focus Band")&&GameData.getRandom().nextInt(100)<Constants.FOCUS_BAND_CHANCE))
					&&damage>=target.getPokemon().getCurrHP()){
				System.out.println(target.getPokemon().getName()+" survives the attack at one life");
				damage=target.getPokemon().getCurrHP()-1;
			}
		}
		damagedone+=damage;
		target.damage(damage);
	}

	private static int calculateHappinessBasedPower(){
		int power=(int)(userpokemon.getHappiness()/2.5);
		if(power==0)
			power=1;
		return power;
	}

	private static int calculatePsywaveDamage(){
		return GameData.getRandom().nextInt((int)(userpokemon.getLevel()*1.5))+1;
	}

	private static int calculatePresentDamage(Unit target){
		int rand=GameData.getRandom().nextInt(10);
		if(rand<2){
			HashMap<String,String> map=new HashMap<String,String>();
			map.put("Percentage","25");
			implement(MoveEffect.Heal,map,target);
			return -1;
		}
		if(rand<6)
			return calculateDamage(user,target,GameData.getMoveType(move.getNum()),40);
		if(rand<9)
			return calculateDamage(user,target,GameData.getMoveType(move.getNum()),80);
		return calculateDamage(user,target,GameData.getMoveType(move.getNum()),120);

	}

	private static int calculateMagnitudePower(){
		int rand=GameData.getRandom().nextInt(100);
		if(rand<5)
			return 10;
		if(rand<15)
			return 30;
		if(rand<35)
			return 50;
		if(rand<65)
			return 70;
		if(rand<85)
			return 90;
		if(rand<95)
			return 110;
		return 150;
	}

	private static Type calculateHiddenType(){
		int[] ivs=userpokemon.getIVs();
		//based on http://bulbapedia.bulbagarden.net/wiki/Hidden_Power_(move)/Calculation
		//4*attack mod 4 + defense mod 4 (0-15)
		int p=4*(ivs[1]%4)+(ivs[2]%4);
		Type[] types={Type.Fighting,Type.Flying,Type.Poison,Type.Ground,Type.Rock,Type.Bug,Type.Ghost,Type.Steel,Type.Fire,Type.Water,
				Type.Grass,Type.Electric,Type.Psychic,Type.Ice,Type.Dragon,Type.Dark};
		return types[p];
	}

	private static int calculateHiddenPower(){
		int[] ivs=userpokemon.getIVs();
		//based on http://bulbapedia.bulbagarden.net/wiki/Hidden_Power_(move)/Calculation
		//type calculation is based on gen 2, power calculation is based on formula from gen 3+,
		//but determination of variables is the same as gen 2 (<8=0,>=8=1)
		int u=ivs[0]<8?0:1;
		int v=ivs[1]<8?0:1;
		int w=ivs[2]<8?0:1;
		int y=ivs[3]<8?0:1;
		int z=ivs[4]<8?0:1;
		int x=ivs[5]<8?0:1;
		int power=u+2*v+4*w+8*x+16*y+32*z;
		power*=40;
		power/=63;
		power+=30;
		return power;
	}

	private static int calculateFlailPower(){
		int p=(48*userpokemon.getCurrHP())/userpokemon.getStat(Stat.HP);
		if(p>32)
			return 20;
		if(p>=17)
			return 40;
		if(p>=10)
			return 80;
		if(p>=5)
			return 100;
		if(p>=2)
			return 150;
		return 200;
	}


	/**
	 * Determine if the given move uses special attack/defense for damage calculation
	 * @param movenum
	 * @return
	 */
	public static boolean usesSpecial(Type type){
		if(type==null)
			return false;
		return (type==Type.Water||type==Type.Grass||type==Type.Fire||type==Type.Ice||type==Type.Electric||type==Type.Psychic||type==Type.Dragon||type==Type.Dark);
	}

	private static int calculateDamage(Unit user, Unit defender, Type movetype,int power){
		int damage=0;
		Pokemon attackerpokemon=userpokemon;
		String param=curreffects.get("Type");
		//BASE DAMAGE CALCULATION
		double parta=(2*attackerpokemon.getLevel()+10)/250;
		double partb=0;
		int att;
		int def;
		if(usesSpecial(movetype)){
			att=user.getStat(Stat.SpecialAttack);
			def=defender.getStat(Stat.SpecialDefense);
		}
		else{
			att=user.getStat(Stat.Attack);
			def=defender.getStat(Stat.Defense);
		}
		if(param!=null&&param.equals("SelfDestruct"))
			def/=2;
		partb=att/def;
		double totalbase=(parta*partb*power)+2;
		//MODIFIERS
		//same type attack mod
		double stabmod=1;
		if(user.isType(movetype))
			stabmod=1.5;
		//type effectiveness mod
		double typemod=1;
		for(Type t:defender.getTypes()){
			typemod*=GameData.getTypeEffectivenessDamageMod(movetype,t);
		}
		//critical hit mod
		double critmod=1;
		int highcritrate=1;
		if(param!=null&&param.equals("HighCritRatio"))
			highcritrate=2;
		if(GameData.getRandom().nextInt(100)<Math.min(user.getCritRatio()*highcritrate,100))
			critmod=2;
		//held item type boost mod
		double heldmod=1;
		if(movetype==attackerpokemon.getHeldItemBoostType()){
			if(movetype==Type.Normal)
				heldmod=1.15;
			else
				heldmod=1.2;
		}
		//random mod
		double randommod=(GameData.getRandom().nextInt(16)+85)/((double)100);
		if(param!=null&&param.equals("BeatUp")&&!user.equals(MoveLogic.user))
			randommod=1;
		//burn mod
		double burnmod=1;
		if(attackerpokemon.getPcondition()==PermCondition.Burn&&!usesSpecial(movetype))
			burnmod-=Constants.BURN_PHYSICAL_ATTACK_DAMAGE_REDUCTION;
		double totalmodifier=stabmod*typemod*critmod*heldmod*randommod*burnmod;
		damage=(int)(totalbase*totalmodifier);
		System.out.println("( "+parta+" * "+partb+" * "+power+" + 2) * "+stabmod+" * "+typemod+" * "+critmod+" * "
				+heldmod+" * "+randommod+" * "+burnmod+" = "+damage);
		return damage;
	}

	public static boolean doesMoveHit(Unit defender, Unit attacker, int movenum){
		Type movetype=GameData.getMoveType(movenum);
		ArrayList<Type> types=defender.getTypes();
		if(GameData.getTypeEffectivenessDamageMod(movetype,types.get(0))==0)
			return false;
		if(types.size()>1&&GameData.getTypeEffectivenessDamageMod(movetype,types.get(1))==0)
			return false;
		if(defender.hasProtectionCondition(ProtectionCondition.Protect)||defender.hasProtectionCondition(ProtectionCondition.Detect)){
			System.out.println(defender.getPokemon().getName()+" cannot be hit because they are protected.");
			return false;
		}
		String name=GameData.getMoveName(movenum);
		if(defender.isFlying()&&!name.equals("Fly")&&!name.equals("Gust")&&!name.equals("Thunder")&&!name.equals("Twister")){
			System.out.println(GameData.getMoveName(movenum)+" could not hit because "+defender.getPokemon().getName()+" is mid-air using Fly.");
			return false;
		}
		if(defender.isDigging()&&!name.equals("Dig")&&!name.equals("Earthquake")&&!name.equals("Fissure")&&!name.equals("Magnitude")){
			System.out.println(GameData.getMoveName(movenum)+" could not hit because "+defender.getPokemon().getName()+" is underground using Dig.");
			return false;
		}
		if(name.equals("Faint Attack")||!name.equals("Swift")||!name.equals("Vital Throw"))
			return true;
		return GameData.getRandom().nextInt(100)<calculateChanceOfHitting(defender,attacker,movenum);
	}

	private static int calculateChanceOfHitting(Unit defender, Unit attacker, int movenum){
		double mod=1;
		if(attacker.isFlanking(defender))
			System.out.println(attacker.getPokemon().getName()+" is flanking "+defender.getPokemon().getName()+" so will suffer no accuracy penalty");
		else if(attacker.isFacing(defender)){
			System.out.println(attacker.getPokemon().getName()+" is facing "+defender.getPokemon().getName()+" so will take an accuracy penalty");
			mod=Constants.FRONT_FACING_ACCURACY_RATE;
		}
		else{
			System.out.println(attacker.getPokemon().getName()+" is facing "+defender.getPokemon().getName()+"'s side so will take a slight accuracy penalty");
			mod=Constants.SIDE_FACING_ACCURACY_RATE;
		}
		System.out.println(GameData.getMoveAccuracy(movenum)+"*"+defender.getEvasion()+"*"+attacker.getAccuracy()+"*"+mod);
		return round(GameData.getMoveAccuracy(movenum)*defender.getEvasion()*attacker.getAccuracy()*mod);
	}

	private static int round(double num){
		int intnum=(int)num;
		if(num<intnum+0.5)
			return intnum;
		else 
			return intnum+1;
	}

	public static void startOfTurnActions(Unit activeunit){
		Pokemon activepokemon=activeunit.getPokemon();
		PermCondition pcondition=activepokemon.getPcondition();
		if(pcondition==PermCondition.Burn||pcondition==PermCondition.Poison){
			System.out.print(activepokemon.getName()+" loses hp from "+pcondition.toString()+": from "
					+activepokemon.getCurrHP()+" ");
			activepokemon.decHP(round(Constants.BURN_POISON_HP_LOSS_RATE*activepokemon.getStat(Stat.HP)));
			System.out.println("to "+activepokemon.getCurrHP());
		}
		else if(pcondition==PermCondition.BadlyPoison){
			activeunit.incNumTurnsAfflicted(PermCondition.BadlyPoison.toString());
			System.out.print(activepokemon.getName()+" loses hp from "+pcondition.toString()+": from "
					+activepokemon.getCurrHP()+" ");
			activepokemon.decHP(round(activeunit.getNumTurnsAfflicted(PermCondition.BadlyPoison.toString())*Constants.BURN_POISON_HP_LOSS_RATE
					*activepokemon.getStat(Stat.HP)));
			System.out.println("to "+activepokemon.getCurrHP());
		}
		else if(pcondition==PermCondition.Frozen){
			int turns=activeunit.getNumTurnsAfflicted(PermCondition.Frozen.toString());
			//freeze can last 1-4 turns. 1/4 chance of ending after 1 turn, 1/3 after 2 turns, 1/2 after 3, 1/1 after 4. 
			if(turns>0&&GameData.getRandom().nextInt(100)<100/(Constants.FREEZE_MAX_TURNS+1-activeunit.getNumTurnsAfflicted(PermCondition.Frozen.toString()))){
				//thawed
				System.out.println(activepokemon.getName()+" is no longer frozen after "+turns+" turns.");
				activeunit.resetNumTurnsAfflicted(PermCondition.Frozen.toString());
				activepokemon.removePcondition(PermCondition.Frozen);
				activeunit.setCanMove(true);
				activeunit.setCanAttack(true);
			}
			else{
				System.out.println(activepokemon.getName()+" is still frozen after "+turns+" turns.");
				activeunit.incNumTurnsAfflicted(PermCondition.Frozen.toString());
				activeunit.setCanMove(false);
				activeunit.setCanAttack(false);
			}
		}
		else if(pcondition==PermCondition.Sleep){
			int turns=activeunit.getNumTurnsAfflicted(PermCondition.Sleep.toString());
			//sleep can last 1-4 turns. 1/4 chance of ending after 1 turn, 1/3 after 2 turns, 1/2 after 3, 1/1 after 4. 
			if(turns>0&&GameData.getRandom().nextInt(100)<100/(Constants.SLEEP_MAX_TURNS+1-activeunit.getNumTurnsAfflicted(PermCondition.Sleep.toString()))){
				//awaken
				System.out.println(activepokemon.getName()+" is no longer asleep after "+turns+" turns.");
				activeunit.resetNumTurnsAfflicted(PermCondition.Sleep.toString());
				activepokemon.removePcondition(PermCondition.Sleep);
				activeunit.setCanMove(true);
			}
			else{
				System.out.println(activepokemon.getName()+" is still asleep after "+turns+" turns.");
				activeunit.incNumTurnsAfflicted(PermCondition.Sleep.toString());
				activeunit.setCanMove(false);
			}
		}
		else if(activepokemon.getPcondition()==PermCondition.Paralysis){
			if(GameData.getRandom().nextInt(100)<Constants.PARALYSIS_INACTION_CHANCE){
				System.out.println(activepokemon.getName()+" is too paralyzed to do anything");
				activeunit.setCanAttack(false);
				activeunit.setCanMove(false);
			}
		}
		else if(activepokemon.getPcondition()==PermCondition.Confusion){
			int turns=activeunit.getNumTurnsAfflicted(PermCondition.Confusion.toString());
			//confusion can last 1-4 turns. 1/4 chance of ending after 1 turn, 1/3 after 2 turns, 1/2 after 3, 1/1 after 4. 
			if(turns>0&&GameData.getRandom().nextInt(100)<100/(Constants.CONFUSE_MAX_TURNS+1-activeunit.getNumTurnsAfflicted(PermCondition.Confusion.toString()))){
				//awaken
				System.out.println(activepokemon.getName()+" is no longer confused after "+turns+" turns.");
				activeunit.resetNumTurnsAfflicted(PermCondition.Confusion.toString());
				activepokemon.removePcondition(PermCondition.Confusion);
			}
			else{
				System.out.println(activepokemon.getName()+" is still confused after "+turns+" turns.");
			}
		}
		if(activeunit.hasTempCondition(TempCondition.Attract)){
			if(GameData.getRandom().nextInt(100)<Constants.ATTRACT_INACTION_CHANCE){
				System.out.println(activepokemon.getName()+" is too infatuated to attack");
				activeunit.setCanAttack(false);
			}
		}
		if(activeunit.hasTempCondition(TempCondition.Flinch)){
			System.out.println(activepokemon.getName()+" flinches");
			activeunit.setCanAttack(false);
		}
		if(activeunit.hasTempCondition(TempCondition.Trap)||activeunit.hasTempCondition(TempCondition.DamageTrap)){
			TempCondition temp=TempCondition.Trap;
			if(activeunit.hasTempCondition(TempCondition.DamageTrap))
				temp=TempCondition.DamageTrap;
			int turns=activeunit.getNumTurnsAfflicted(temp.toString());
			//trap can last 2-5 turns. 1/4 chance of ending after 2 turns, 1/3 after 3 turns, 1/2 after 4, 1/1 after 5. 
			if(turns>=Constants.TRAP_MIN_TURNS&&GameData.getRandom().nextInt(100)<100/(Constants.TRAP_MAX_TURNS+1-activeunit.getNumTurnsAfflicted(temp.toString()))){
				//break free
				System.out.println(activepokemon.getName()+" is no longer trapped after "+turns+" turns.");
				activeunit.resetNumTurnsAfflicted(temp.toString());
				activeunit.setCanMove(true);
				if(activeunit.hasTempCondition(TempCondition.Trap))
					activeunit.removeTempCondition(TempCondition.Trap);
				else
					activeunit.removeTempCondition(TempCondition.DamageTrap);
			}
			else{
				System.out.println(activepokemon.getName()+" is still trapped after "+turns+" turns.");
				activeunit.incNumTurnsAfflicted(temp.toString());
				activeunit.setCanMove(false);
				if(activeunit.hasTempCondition(TempCondition.DamageTrap)){
					int damage=round(activepokemon.getStat(Stat.HP)*Constants.TRAP_HP_LOSS_RATE);
					System.out.println(activepokemon.getName()+" takes "+damage+" damage");
					activeunit.damage(damage);
				}
			}
		}
		if(activeunit.hasTempCondition(TempCondition.Encore)){
			int turns=activeunit.getNumTurnsAfflicted(TempCondition.Encore.toString());
			//encore can last 2-5 turns. 1/4 chance of ending after 2 turns, 1/3 after 3 turns, 1/2 after 4, 1/1 after 5. 
			if(turns>=Constants.ENCORE_MIN_TURNS&&GameData.getRandom().nextInt(100)<100/(Constants.ENCORE_MAX_TURNS+1-activeunit.getNumTurnsAfflicted(TempCondition.Encore.toString()))){
				//break free
				System.out.println(activepokemon.getName()+" is no longer encored after "+turns+" turns.");
				activeunit.resetNumTurnsAfflicted(TempCondition.Encore.toString());
				activeunit.removeTempCondition(TempCondition.Encore);
			}
			else{
				System.out.println(activepokemon.getName()+" is still encored after "+turns+" turns.");
				activeunit.incNumTurnsAfflicted(TempCondition.Encore.toString());
			}
		}
		if(activepokemon.isHolding("Leftovers")){
			int hp=round(activepokemon.getStat(Stat.HP)*Constants.LEFTOVERS_HP_RECOV_RATE);
			System.out.println(activepokemon.getName()+" recovers "+hp+"HP from leftovers");
			activepokemon.incHP(hp);
		}
		if(activeunit.hasTempCondition(TempCondition.Disable)){
			int turns=activeunit.getNumTurnsAfflicted(TempCondition.Disable.toString());
			//disable can last 2-5 turns. 1/4 chance of ending after 2 turns, 1/3 after 3 turns, 1/2 after 4, 1/1 after 5. 
			if(turns>=Constants.DISABLE_MIN_TURNS&&GameData.getRandom().nextInt(100)<100/(Constants.DISABLE_MAX_TURNS+1-activeunit.getNumTurnsAfflicted(TempCondition.Disable.toString()))){
				//disable ends
				System.out.println(activepokemon.getName()+" is no longer disabled after "+turns+" turns.");
				activeunit.resetNumTurnsAfflicted(TempCondition.Disable.toString());
				activeunit.removeTempCondition(TempCondition.Disable);
				activeunit.enableDisabledMove();
			}
			else
				System.out.println(activepokemon.getName()+" is still disabled after "+turns+" turns.");
		}
		if(activeunit.hasProtectionCondition(ProtectionCondition.LightScreen)){
			int turns=activeunit.getNumTurnsProtected(ProtectionCondition.LightScreen);
			if(turns<5){
				activeunit.incNumTurnsProtected(ProtectionCondition.LightScreen);
				System.out.println(activepokemon+" is still protected by Light Screen after "+(turns+1)+" turns.");
			}
			else{
				activeunit.removeProtectionCondition(ProtectionCondition.LightScreen);
				System.out.println(activepokemon.getName()+"'s Light Screen wears off.");
			}
		}
		if(activeunit.hasProtectionCondition(ProtectionCondition.Reflect)){
			int turns=activeunit.getNumTurnsProtected(ProtectionCondition.Reflect);
			if(turns<5){
				activeunit.incNumTurnsProtected(ProtectionCondition.Reflect);
				System.out.println(activepokemon+" is still protected by Reflect after "+(turns+1)+" turns.");
			}
			else{
				activeunit.removeProtectionCondition(ProtectionCondition.Reflect);
				System.out.println(activepokemon.getName()+"'s Reflect wears off.");
			}
		}
		if(activeunit.hasProtectionCondition(ProtectionCondition.Safeguard)){
			int turns=activeunit.getNumTurnsProtected(ProtectionCondition.Safeguard);
			if(turns<5){
				activeunit.incNumTurnsProtected(ProtectionCondition.Safeguard);
				System.out.println(activepokemon+" is still protected by Safeguard after "+(turns+1)+" turns.");
			}
			else{
				activeunit.removeProtectionCondition(ProtectionCondition.Safeguard);
				System.out.println(activepokemon.getName()+"'s Safeguard wears off.");
			}
		}
		if(activeunit.hasProtectionCondition(ProtectionCondition.Protect)){
			activeunit.removeProtectionCondition(ProtectionCondition.Protect);
			System.out.println(activepokemon.getName()+"'s Protect wears off.");
		}
		if(activeunit.hasProtectionCondition(ProtectionCondition.Detect)){
			activeunit.removeProtectionCondition(ProtectionCondition.Detect);
			System.out.println(activepokemon.getName()+"'s Detect wears off.");
		}
		if(activeunit.hasProtectionCondition(ProtectionCondition.Endure)){
			activeunit.removeProtectionCondition(ProtectionCondition.Endure);
			System.out.println(activepokemon.getName()+"'s Endure wears off.");
		}
		if(activeunit.hasProtectionCondition(ProtectionCondition.Counter)){
			activeunit.removeProtectionCondition(ProtectionCondition.Counter);
			System.out.println(activepokemon.getName()+"'s Counter wears off.");
		}
		if(activeunit.hasProtectionCondition(ProtectionCondition.MirrorCoat)){
			activeunit.removeProtectionCondition(ProtectionCondition.MirrorCoat);
			System.out.println(activepokemon.getName()+"'s MirrorCoat wears off.");
		}
		if(activeunit.isRaging())
			activeunit.setRaging(false);
		if(activeunit.isCharging()){
			String move=GameData.getMoveName(activeunit.getPrevMove());
			if(move.equals("Hyper Beam")){
				activeunit.setCharging(false);
				activeunit.setHasEndedTurn(true);
				System.out.println(userpokemon.getName()+" skips its turn to recharge");
				BattleEngine.endTurn();
			}
		}
	}

	public static void endOfTurnActions(Unit activeunit){
		Pokemon activepokemon=activeunit.getPokemon();
		if(activeunit.hasTempCondition(TempCondition.Curse)){
			int damage=(int)(activepokemon.getStat(Stat.HP)*Constants.CURSE_NIGHTMARE_HP_LOSS_RATE);
			activepokemon.decHP(damage);
			System.out.println(activepokemon.getName()+" takes "+damage+" damage from their curse");
		}
		if(activeunit.hasTempCondition(TempCondition.Nightmare)){
			if(activepokemon.getPcondition()==PermCondition.Sleep){
				int damage=(int)(activepokemon.getStat(Stat.HP)*Constants.CURSE_NIGHTMARE_HP_LOSS_RATE);
				activepokemon.decHP(damage);
				System.out.println(activepokemon.getName()+" takes "+damage+" damage from their nightmare");
			}
			else{
				activeunit.removeTempCondition(TempCondition.Nightmare);
				System.out.println(activepokemon.getName()+" woke up from their nightmare");
			}
		}
		int recipientid=activeunit.hasBond(BondCondition.LeechSeed);
		if(recipientid>0){
			Unit recipient=BattleEngine.getUnitByID(recipientid);
			int damage=(int)(recipient.getPokemon().getStat(Stat.HP)*Constants.LEECH_SEED_HP_LOSS_RATE);
			recipient.getPokemon().decHP(damage);
			System.out.println(recipient.getPokemon().getName()+" takes "+damage+" damage from leech seed");
			activepokemon.incHP(damage);
			System.out.println(activepokemon.getName()+" gains "+damage+" hp from leech seed");
		}
		if(activeunit.hasTempCondition(TempCondition.PerishSong)){
			activeunit.incNumTurnsAfflicted(TempCondition.PerishSong.toString());
			System.out.println(activepokemon.getName()+" has "+(Constants.PERISH_SONG_TURNS-activeunit.getNumTurnsAfflicted(TempCondition.PerishSong.toString()))+" turns left of Perish Song".toString());
			if(activeunit.getNumTurnsAfflicted(TempCondition.PerishSong.toString())==Constants.PERISH_SONG_TURNS){
				activepokemon.decHP(activepokemon.getCurrHP());
			}
		}
		if(activeunit.hasTempCondition(TempCondition.Disable)){
			if(activeunit.hasAttacked())
				activeunit.incNumTurnsAfflicted(TempCondition.Disable.toString());
		}
	}
}
