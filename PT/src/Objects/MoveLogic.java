package Objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Engines.BattleEngine;
import Enums.BondCondition;
import Enums.EffectType;
import Enums.MoveImpact;
import Enums.PermCondition;
import Enums.ProtectionCondition;
import Enums.Stat;
import Enums.TempCondition;
import Enums.Type;
import Enums.Weather;
import Global.Constants;
import Global.GameData;

public class MoveLogic {
	private static Unit user;
	private static Pokemon userpokemon;
	private static ArrayList<Unit> targets;
	private static Move move;
	private static List<MoveEffect> effects;
//	private static HashMap<String,String> curreffects;
	private static int damagedone;

	private static final boolean DEBUG=false;


	public static void implementEffects(Unit thisuser,ArrayList<Unit> thistargets,Move thismove){
		user=thisuser;
		userpokemon=user.getPokemon();
		targets=thistargets;
		move=thismove;
		effects=GameData.getMoveEffects(move.getNum());
		int count=0;
		damagedone=0;
		if(DEBUG) {
			System.out.println("Targets: "+targets.size());
			for(Unit u:targets) {
				System.out.println(u.getName());
			}
		}
		for(Unit u:targets){
			if(doesMoveHit(u,user,move.getNum())){
				if(count==0){
					if(user.getPrevMove()==move.getNum())
						user.incNumConsecUses();
					else
						user.resetConsecUses();
				}
				count++;
				for(MoveEffect effect:effects){
					System.out.println("Effect: "+effect.toString());
					implement(effect,u);
					//TODO: self effects should only be done once per move not
					//pertarget. but still need to preserve order of effetcs...
					
					//TODO: unimplemented effects: 
					// defense curl rollout buff,
					// destiny bond
					// detect, protect, endure effects as well as diminishing returns
					// dig and fly damage effects? might rethink how these work.
					// disable?
					// dream eater?
					// encore?
					// selfdestruct should be after damage
					 //faint attack not missing
					// flame wheel being usable while frozen and thawing
					//foresight? implementation is probably ok but need to enforce condition
					//future sight has lots of factors are all implemented?
					//fury cutter max power and reset
					//fissure and guillotine accuracy. fissure can hit dig targets
					//haze seems to mistakenly remove p conditions?
					//check hidden power
					//check hyper beam
					//check light screen
					//lockon/mindreader
					//magnitude
					//metronome
					//mimic, mirror move, etc
					//minimize
					//mirror coat
					//mist
					//outrage/petal dance
					//payday
					//how to handle perish song?
					//psych up
					//psywave dmg
					//pursuit flanking dmg
					//rage attackreaction
					//rapid spin all effects
					
					
					
				}
				if(userpokemon.isHolding("King's Rock")&&effects.size()==1
						&&effects.get(0).getType()==EffectType.Damage&&GameData.getRandom().nextInt(100)<Constants.KINGS_ROCK_FLINCH_CHANCE){
					HashMap<String,String> map=new HashMap<String,String>();
					map.put("Condition","Flinch");
					implement(new MoveEffect(EffectType.GiveTCondition,MoveImpact.EnemyOnly,map),u);
				}
			}
			else{
				System.out.println("The move does not hit");
				user.resetConsecUses();
			}
		}
	}

	private static void implement(MoveEffect effect,Unit target){
		if(effect.getImpact()==MoveImpact.EnemyOnly&&user.isFriendlyWith(target))
			return;
		if(effect.getImpact()==MoveImpact.AllyOnly&&!user.isFriendlyWith(target))
			return;
		if(effect.getImpact()==MoveImpact.Self&&!user.equals(target))
			return;
		EffectType effectType=effect.getType();
		if(effectType==EffectType.Damage){
			implementDamageEffect(effect,target);
			if(!target.getPokemon().isFainted()&&target.isRaging()){
				System.out.println(target.getName()+"'s rage grows.");
				target.incStat(1,Stat.Attack);
			}
		}
		else if(effectType==EffectType.Buff||effectType==EffectType.Nerf)
			implementBuffNerfEffect(effect,target);
		else if(effectType==EffectType.PayDay)
			BattleEngine.incPayDayVal(userpokemon.getLevel());
		else if(effect.toString().contains("Condition"))
			implementConditionEffect(effect,target);
		else if(effectType==EffectType.HealthSteal){
			int percentage=damagedone/2;
			userpokemon.incHP(percentage);
			System.out.println(userpokemon.getName()+" heals "+percentage+" HP.");
		}
		else if(effectType==EffectType.Recoil){
			int percentage=damagedone*round((Double.parseDouble(effect.getParam("Percentage"))/100));
			user.damage(percentage);
			System.out.println(userpokemon.getName()+" takes "+percentage+" recoil damage.");
		}
		else if(effectType==EffectType.HealthSac){
			System.out.println(userpokemon.getName()+" loses half of their max HP");
			userpokemon.decHP(userpokemon.getStat(Stat.HP)/2,"sacrificing their health");
		}
		else if(effectType==EffectType.Conversion){
			user.setTypes(target.getTypes());
			System.out.println(userpokemon.getName()+"'s types have been changed to "+user.getTypes());
		}
		else if(effectType==EffectType.Conversion2){
			Type lastmovetype=GameData.getMoveType(target.getPrevMove());
			ArrayList<Type> newtype=new ArrayList<Type>();
			newtype.add(GameData.getResistantType(lastmovetype));
			user.setTypes(newtype);
			System.out.println(userpokemon.getName()+"'s types have been changed to "+user.getTypes().get(0));
		}
		else if(effectType==EffectType.SelfDestruct){
			System.out.println(userpokemon.getName()+" self destructs");
			userpokemon.decHP(userpokemon.getCurrHP(),"self destruct");
		}
		else if(effectType==EffectType.StatStageReset){
			System.out.println(userpokemon.getName()+"'s stats are reset to unmodified values");
			user.clearStatMods();
		}
		else if(effectType==EffectType.Recharge){
			System.out.println(userpokemon.getName()+" must recharge next turn");
			user.isCharging();
			user.setCanMove(false);
		}
		else if(effectType==EffectType.Heal){
			int amount=target.getPokemon().getStat(Stat.HP)*round((Double.parseDouble(effect.getParam("Percentage"))/100));
			System.out.println(target.getName()+" heals "+amount+" HP.");
			target.getPokemon().incHP(amount);
		}
		else if(effectType==EffectType.TimeHeal){
			double percent=Constants.HEAL_RIGHT_TIME_PERCENTAGE;
			if(Enums.Time.valueOf(effect.getParam("Time"))!=GameData.getTime())
				percent=Constants.HEAL_WRONG_TIME_PERCENTAGE;
			Weather currweather=BattleEngine.getWeather();
			if(currweather==Weather.Rain||currweather==Weather.Sand)
				percent/=2;
			else if(currweather==Weather.Sun)
				percent*=2;
			int amount=round(target.getPokemon().getStat(Stat.HP)*percent);
			System.out.println(target.getName()+" heals "+amount+" HP.");
			target.getPokemon().incHP(amount);
		}
		else if(effectType==EffectType.PsychUp){
			System.out.println(userpokemon.getName()+" copies "+target.getName()+"'s stat modifications");
			user.copyStatMods(target);
		}
		else if(effectType==EffectType.Spite){
			Move move=userpokemon.getMove(user.getPrevMove());
			move.decCurrPP(GameData.getRandom().nextInt(Constants.SPITE_MAX_PP-Constants.SPITE_MIN_PP+1)+Constants.SPITE_MIN_PP);
			System.out.println(userpokemon.getName()+"'s move "+GameData.getMoveName(move.getNum())+" PP reduced to "+move.getCurrPP());
		}
		else if(effectType==EffectType.Splash)
			System.out.println(userpokemon.getName()+" splashes around, doing nothing.");
		else if(effectType==EffectType.Thief){
			if(target.getPokemon().getHeldID()!=-1&&userpokemon.getHeldID()==-1&&GameData.getRandom().nextInt(100)<Constants.THIEF_STEAL_CHANCE){
				int id=target.getPokemon().removeHeldItem();
				System.out.println(userpokemon.getName()+" steals "+GameData.getItemName(id)+" from "+target.getName());
				userpokemon.holdItem(id);
			}
		}
		else if(effectType==EffectType.RemoveSpikes){
			System.out.println("Spikes are removed from the battlefield");
			BattleEngine.removeSpikes();
		}
		else if(effectType==EffectType.Rage){
			System.out.println(userpokemon.getName()+" has become enraged");
			user.setRaging(true);
		}
		else if(effectType==EffectType.BatonPass){
			if(BattleEngine.canMoveTo(user,target.getCoordinates(),true)&&BattleEngine.canMoveTo(target,user.getCoordinates(),true)){
				System.out.println(userpokemon.getName()+" swapping positions with "+target.getName());
				swapPositions(user,target);
			}
			else
				System.out.println("One or more illegal destinations involved in psoition swamp between "+userpokemon.getName()+" and "+target.getName());
		}
		else if(effectType==EffectType.RandomSwap){
			ArrayList<Unit> options=BattleEngine.getFriendlyUnits(target);
			options.remove(target);
			Unit othertarget=options.get(GameData.getRandom().nextInt(options.size()));
			if(BattleEngine.canMoveTo(target,othertarget.getCoordinates(),true)&&BattleEngine.canMoveTo(othertarget,target.getCoordinates(),true)){
				System.out.println(othertarget.getName()+" swapping positions with "+target.getName());
				swapPositions(othertarget,target);
			}
			else
				System.out.println("One or more illegal destinations involved in psoition swamp between "+othertarget.getName()+" and "+target.getName());
		}
		else if(effectType==EffectType.Dig&&!user.isDigging()){
			user.setDigging(true);
			System.out.println(userpokemon.getName()+" digs down to attack");
		}
		else if(effectType==EffectType.Fly&&!user.isFlying()){
			user.setFlying(true);
			System.out.println(userpokemon.getName()+" flies up to attack");
		}
		else if(effectType==EffectType.ChargeUp){
			//sunny day should make solar beam fire immediately
			if(GameData.getMoveName(move.getNum()).equals("Solar Beam")&&BattleEngine.getWeather()==Weather.Sun) {
				if(!user.isCharging())
					System.out.println(userpokemon.getName()+" fires solarbeam immediately because of sunny day");
				user.setCharging(false);
			}
			else {
				user.setCharging(!user.isCharging());
				if(user.isCharging())
					System.out.println(userpokemon.getName()+" charges up to attack");
				else
					System.out.println(userpokemon.getName()+" is done charging");
			}
		}
		else if(effectType==EffectType.Weather){
			Weather newconditions=Weather.valueOf(effect.getParam("Conditions"));
			if(newconditions==BattleEngine.getWeather()){
				System.out.println("The weather is already in condition: "+newconditions);
			}
			else{
				BattleEngine.setWeather(newconditions);
				System.out.println("The weather changes to: "+newconditions);
			}
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
		String chance=effect.getParam("Chance");
		if(chance==null||GameData.getRandom().nextInt(100)<Integer.parseInt(chance)){
			String condition=effect.getParam("Condition");
			if(effect.getType()==EffectType.GiveTCondition){
				if(target.addTempCondition(TempCondition.valueOf(condition)))
					System.out.println(target.getName()+" is now afflicted by "+condition);
			}
			else if(effect.getType()==EffectType.GivePCondition){
				PermCondition pcondition=PermCondition.valueOf(condition);
				if(pcondition==PermCondition.Frozen&&BattleEngine.getWeather()==Weather.Sun)
					System.out.println("Pokemon cannot be frozen due to Sunny Day");
				else if(target.addPermCondition(pcondition))
					System.out.println(target.getName()+" is now afflicted by "+condition);
			}
			else if(effect.getType()==EffectType.GiveBondCondition){
				if(target.addBondCondition(BondCondition.valueOf(condition),target.getID()))
					System.out.println(target.getName()+" is now bound to "+userpokemon.getName()+" by "+condition);
			}
			else if(effect.getType()==EffectType.GiveProtectionCondition){
				if(target.addProtectionCondition(ProtectionCondition.valueOf(condition)))
					System.out.println(target.getName()+" is now protected by "+condition);
			}
			else if(effect.getType()==EffectType.RemoveTCondition){
				if(target.removeTempCondition(TempCondition.valueOf(condition)))
					System.out.println(target.getName()+" is now cured of "+condition);
			}
			else if(effect.getType()==EffectType.RemovePCondition){
				if(target.removePermCondition(PermCondition.valueOf(condition)))
					System.out.println(target.getName()+" is now cured of "+condition);
			}
			else if(effect.getType()==EffectType.RemoveBondCondition){
				if(target.removeBondCondition(BondCondition.valueOf(condition)))
					System.out.println(target.getName()+" is no longer bound to "+userpokemon.getName()+" by "+condition);
			}
			else if(effect.getType()==EffectType.RemoveProtectionCondition){
				if(target.removeProtectionCondition(ProtectionCondition.valueOf(condition)))
					System.out.println(target.getName()+" is no longer protected by "+condition);
			}
		}
	}

	private static void implementBuffNerfEffect(MoveEffect effect,Unit target){
		if(target.hasProtectionCondition(ProtectionCondition.Mist)&&!target.equals(user)){
			System.out.println(target.getName()+" cannot have it's stats modified because it is protected by Mist");
		}//TODO: looks like only buffs/nerfs with a chance of success go through, but guaranteed buffs/nerfs dont
		else if(effect.getParam("Chance")!=null&&GameData.getRandom().nextInt(100)<Integer.parseInt(effect.getParam("Chance"))){
			String stages=effect.getParam("Stages");
			String stat=effect.getParam("Stat");
			if(effect.getType()==EffectType.Buff){
				System.out.println(target.getName()+"'s "+stat+" increased "+stages+" stages.");
				target.incStat(Integer.parseInt(stages),Stat.valueOf(stat));
			}
			else if (effect.getType()==EffectType.Nerf){
				System.out.println(target.getName()+"'s "+stat+" decreased "+stages+" stages.");
				target.decStat(Integer.parseInt(stages),Stat.valueOf(stat));
			}
		}
	}

	private static void implementDamageEffect(MoveEffect effect,Unit target){
		int damage=0;
		String power=effect.getParam("Power");
		String param=effect.getParam("Type");
		if(param!=null){
			//If the user is not currently dug/flying then don't do the damage portion yet.
			if(param.equals("Dig")&&!user.isDigging())
				return;
			if(param.equals("Fly")&&!user.isFlying())
				return;
			//If user is charging we're not ready yet
			if(param.equals("ChargeUp")&&user.isCharging())
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
						//TODO: ally must be not fainted and not affected by a status condition?
						if(u.equals(user))
							continue;
						damage+=calculateDamage(effect,u,target,null,10);
					}
				}
			}
			System.out.println("Attacking "+numtimes+" times.");
			for(int i=0;i<numtimes;i++){
				damage+=calculateDamage(effect,user,target,GameData.getMoveType(move.getNum()),powernum);
			}
		}
		catch(Exception e){
			//parameters that determine base power or damage calculations
			if(power.equals("Invariant"))
				damage=Integer.parseInt(effect.getParam("Damage"));
			else if(power.equals("OneHitKO"))
				damage=target.getPokemon().getCurrHP();
			else if(power.equals("Flail"))
				damage=calculateDamage(effect,user,target,GameData.getMoveType(move.getNum()),calculateFlailPower());
			else if(power.equals("Hidden Power"))
				damage=calculateDamage(effect,user,target,calculateHiddenType(),calculateHiddenPower());
			else if(power.equals("Magnitude"))
				damage=calculateDamage(effect,user,target,GameData.getMoveType(move.getNum()),calculateMagnitudePower());
			else if(power.equals("Level"))
				damage=userpokemon.getLevel();
			else if(power.equals("Present"))
				damage=calculatePresentDamage(effect,target);
			else if(power.equals("Psywave"))
				damage=calculatePsywaveDamage();
			else if(power.equals("HappinessBased"))
				damage=calculateDamage(effect,user,target,GameData.getMoveType(move.getNum()),calculateHappinessBasedPower());
			else if(power.equals("HalveHP"))
				damage=target.getPokemon().getCurrHP()/2;
		}
		if(param!=null){
			//post damage calculation parameters that affect ultimate damage calculation
			if(param.equals("DigMultiplier")&&target.isDigging()){
				System.out.println(target.getName()+" takes double damage from "
						+GameData.getMoveName(move.getNum())+" because it is digging");
				damage*=2;
			}
			else if(param.equals("FlyMultiplier")&&target.isFlying()){
				System.out.println(target.getName()+" takes double damage from "
						+GameData.getMoveName(move.getNum())+" because it is flying");
				damage*=2;
			}
			else if(param.equals("MinimizeMultiplier")&&target.isMinimized()){
				System.out.println(target.getName()+" takes double damage from "
						+GameData.getMoveName(move.getNum())+" because it is minimized");
				damage*=2;
			}
			else if(param.equals("Flanking")&&user.isFlanking(target)){
				System.out.println(target.getName()+" takes extra damage from "
						+GameData.getMoveName(move.getNum())+" because it is being flanked");
				damage*=2;
			}
			else if(param.equals("ConsecPowInc")&&user.getNumConsecUses()>0){
				damage*=Math.pow(2,user.getNumConsecUses());
				System.out.println(target.getName()+" takes "+Math.pow(2,user.getNumConsecUses())+" times damage "
						+" because "+GameData.getMoveName(move.getNum())+" has been used "+(user.getNumConsecUses()+1)+" turns.");
			}
			if((param.equals("CannotKill")||
					(target.getPokemon().isHolding("Focus Band")&&GameData.getRandom().nextInt(100)<Constants.FOCUS_BAND_CHANCE))
					&&damage>=target.getPokemon().getCurrHP()){
				System.out.println(target.getName()+" survives the attack at one life");
				damage=target.getPokemon().getCurrHP()-1;
			}
		}
		damagedone+=damage;
		target.damage(damage);
		Pokemon p=target.getPokemon();
		System.out.println(p.getName()+" has "+p.getCurrHP()+" HP remaining");
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

	private static int calculatePresentDamage(MoveEffect effect,Unit target){
		int rand=GameData.getRandom().nextInt(10);
		if(rand<2){
			HashMap<String,String> map=new HashMap<String,String>();
			map.put("Percentage","25");
			implement(new MoveEffect(EffectType.Heal,effect.getImpact(), map),target);
			return -1;
		}
		if(rand<6)
			return calculateDamage(effect,user,target,GameData.getMoveType(move.getNum()),40);
		if(rand<9)
			return calculateDamage(effect,user,target,GameData.getMoveType(move.getNum()),80);
		return calculateDamage(effect,user,target,GameData.getMoveType(move.getNum()),120);

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

	private static int calculateDamage(MoveEffect effect,Unit user, Unit defender, Type movetype,int power){
		int damage=0;
		Pokemon attackerpokemon=userpokemon;
		String param=effect.getParam("Type");
		//BASE DAMAGE CALCULATION
		double parta=2*(double)attackerpokemon.getLevel()/5+2;
		double partb=0;
		int att;
		int def;
		if(DEBUG) {
			System.out.println("Special Attack: "+user.getStat(Stat.SpecialAttack));
			System.out.println("Special Attack: "+user.getPokemon().getStat(Stat.SpecialAttack));
			System.out.println("Attack: "+user.getStat(Stat.Attack));
			System.out.println("Attack: "+user.getPokemon().getStat(Stat.Attack));
			System.out.println("Special Defense: "+user.getStat(Stat.SpecialDefense));
			System.out.println("Special Defense: "+user.getPokemon().getStat(Stat.SpecialDefense));
			System.out.println("Defense: "+user.getStat(Stat.Defense));
			System.out.println("Defense: "+user.getPokemon().getStat(Stat.Defense));
		}
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
		partb=((double)att)/def;
		double totalbase=(parta*partb*power)/50.0+2;
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
		//weather mod
		double weathermod=1;
		Weather currweather=BattleEngine.getWeather();
		if((currweather==Weather.Sun&&movetype==Type.Fire)||(currweather==Weather.Rain&&movetype==Type.Water))
			weathermod=1.5;
		else if((currweather==Weather.Sun&&movetype==Type.Water)||(currweather==Weather.Rain&&movetype==Type.Fire)||(currweather==Weather.Rain&&GameData.getMoveName(move.getNum()).equals("Solar Beam")))
			weathermod=.5;
		double totalmodifier=stabmod*typemod*critmod*heldmod*randommod*burnmod*weathermod*Constants.DMG_SCALING_RATE;
		damage=(int)(totalbase*totalmodifier);
		System.out.println("( "+parta+" * "+att+"/"+def+" * "+power+" / 50 + 2) * "+stabmod+" * "+typemod+" * "+critmod+" * "
				+heldmod+" * "+randommod+" * "+burnmod+" * "+weathermod+" * "+Constants.DMG_SCALING_RATE+" = "+damage);
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
			System.out.println(defender.getName()+" cannot be hit because they are protected.");
			return false;
		}
		String name=GameData.getMoveName(movenum);
		if(defender.isFlying()&&!name.equals("Fly")&&!name.equals("Gust")&&!name.equals("Thunder")&&!name.equals("Twister")){
			System.out.println(GameData.getMoveName(movenum)+" could not hit because "+defender.getName()+" is mid-air using Fly.");
			return false;
		}
		if(defender.isDigging()&&!name.equals("Dig")&&!name.equals("Earthquake")&&!name.equals("Fissure")&&!name.equals("Magnitude")){
			System.out.println(GameData.getMoveName(movenum)+" could not hit because "+defender.getName()+" is underground using Dig.");
			return false;
		}
		if(name.equals("Faint Attack")||!name.equals("Swift")||!name.equals("Vital Throw"))
			return true;
		return GameData.getRandom().nextInt(100)<calculateChanceOfHitting(defender,attacker,movenum);
	}

	private static int calculateChanceOfHitting(Unit defender, Unit attacker, int movenum){
		double mod=1;
		if(attacker.isFlanking(defender))
			System.out.println(attacker.getName()+" is flanking "+defender.getName()+" so will suffer no accuracy penalty");
		else if(attacker.isFacing(defender)){
			System.out.println(attacker.getName()+" is facing "+defender.getName()+" so will take an accuracy penalty");
			mod=Constants.FRONT_FACING_ACCURACY_RATE;
		}
		else{
			System.out.println(attacker.getName()+" is facing "+defender.getName()+"'s side so will take a slight accuracy penalty");
			mod=Constants.SIDE_FACING_ACCURACY_RATE;
		}
		//TODO:some moves like buffs report -1% chance of hitting because they cant miss. account for this
		double acc=GameData.getMoveAccuracy(movenum);
		if(GameData.getMoveName(movenum).equals("Thunder")){
			if(BattleEngine.getWeather()==Weather.Rain){
				System.out.println("Rain Dance makes Thunder 100% accurate");
				acc=100.0;
			}
			else if(BattleEngine.getWeather()==Weather.Sun){
				System.out.println("Sunny Day makes Thunder 50% accurate");
				acc=50.0;
			}
		}
		System.out.println(acc+"*"+defender.getEvasion()+"*"+attacker.getAccuracy()+"*"+mod);
		return round(acc*defender.getEvasion()*attacker.getAccuracy()*mod);
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
			activepokemon.decHP(round(Constants.BURN_POISON_HP_LOSS_RATE*activepokemon.getStat(Stat.HP)),"burns");
			System.out.println("to "+activepokemon.getCurrHP());
		}
		else if(pcondition==PermCondition.BadlyPoison){
			activeunit.incNumTurnsAfflicted(PermCondition.BadlyPoison.toString());
			System.out.print(activepokemon.getName()+" loses hp from "+pcondition.toString()+": from "
					+activepokemon.getCurrHP()+" ");
			activepokemon.decHP(round(activeunit.getNumTurnsAfflicted(PermCondition.BadlyPoison.toString())*Constants.BURN_POISON_HP_LOSS_RATE
					*activepokemon.getStat(Stat.HP)),"poison");
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
				activeunit.setHasTakenAction(true);
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
		}//TODO: is this implemented?
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
		if(BattleEngine.getWeather()==Weather.Sand) {
			if(!activeunit.getTypes().contains(Type.Rock)&&!activeunit.getTypes().contains(Type.Steel)&&!activeunit.getTypes().contains(Type.Ground)) {
				int damage=(int)(activepokemon.getStat(Stat.HP)*Constants.SANDSTORM_HP_LOSS_RATE);
				activepokemon.decHP(damage,"sandstorm");
			}
		}
		if(activeunit.hasTempCondition(TempCondition.Curse)){
			int damage=(int)(activepokemon.getStat(Stat.HP)*Constants.CURSE_NIGHTMARE_HP_LOSS_RATE);
			activepokemon.decHP(damage,"curse");
			System.out.println(activepokemon.getName()+" takes "+damage+" damage from their curse");
		}
		if(activeunit.hasTempCondition(TempCondition.Nightmare)){
			if(activepokemon.getPcondition()==PermCondition.Sleep){
				int damage=(int)(activepokemon.getStat(Stat.HP)*Constants.CURSE_NIGHTMARE_HP_LOSS_RATE);
				activepokemon.decHP(damage,"nightmare");
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
			recipient.getPokemon().decHP(damage,"leech seed");
			System.out.println(recipient.getName()+" takes "+damage+" damage from leech seed");
			activepokemon.incHP(damage);
			System.out.println(activepokemon.getName()+" gains "+damage+" hp from leech seed");
		}
		if(activeunit.hasTempCondition(TempCondition.PerishSong)){
			activeunit.incNumTurnsAfflicted(TempCondition.PerishSong.toString());
			System.out.println(activepokemon.getName()+" has "+(Constants.PERISH_SONG_TURNS-activeunit.getNumTurnsAfflicted(TempCondition.PerishSong.toString()))+" turns left of Perish Song".toString());
			if(activeunit.getNumTurnsAfflicted(TempCondition.PerishSong.toString())==Constants.PERISH_SONG_TURNS){
				activepokemon.decHP(activepokemon.getCurrHP(),"perish song");
			}
		}
		if(activeunit.hasTempCondition(TempCondition.Disable)){
			if(activeunit.hasAttacked())
				activeunit.incNumTurnsAfflicted(TempCondition.Disable.toString());
		}
	}

	public static void delete(){
		user=null;
		userpokemon=null;
		targets=null;
		move=null;
		effects=null;
	}
}
