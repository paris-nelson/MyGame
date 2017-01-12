package Objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import Engines.BattleEngine;
import Enums.MoveEffect;
import Enums.PermCondition;
import Enums.ProtectionCondition;
import Enums.Stat;
import Enums.Type;
import Global.Constants;
import Global.GameData;

public class MoveLogic {
	private static Unit user;
	private static ArrayList<Unit> targets;
	private static Move move;
	private static HashMap<MoveEffect,HashMap<String,String>> effects;
	private static HashMap<String,String> curreffects;


	public static void implementEffects(Unit thisuser,ArrayList<Unit> thistargets,Move thismove){
		user=thisuser;
		targets=thistargets;
		move=thismove;
		effects=GameData.getMoveEffects(move.getNum());
		HashMap<MoveEffect,HashMap<String,String>> selftargetting=getSelfTargettingEffects();
		int count=0;
		for(Unit u:thistargets){
			if(doesMoveHit(u,user,move.getNum())){
				if(count==0){
					if(user.getPrevMove()==move.getNum())
						user.incNumConsecUses();
					else
						user.resetConsecUses();
				}
				count++;
				for(MoveEffect effect:effects.keySet()){
					implement(effect,effects.get(effect),u);
				}
				if(user.getPokemon().isHolding("King's Rock")&&GameData.getMoveEffects(move.getNum()).size()==1
						&&effects.containsKey(MoveEffect.Damage)&&GameData.getRandom().nextInt(100)<Constants.KINGS_ROCK_FLINCH_CHANCE){
					HashMap<String,String> map=new HashMap<String,String>();
					map.put("Condition","Flinch");
					implement(MoveEffect.GiveTCondition,map,u);
				}
			}
		}
		for(MoveEffect effect:selftargetting.keySet()){
			if(effect==MoveEffect.MissRecoil&&count>0)
				continue;
			implement(effect,selftargetting.get(effect),user);
		}
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
		if(effect==MoveEffect.Damage){
			implementDamageEffect(target);
		}
	}

	private static void implementDamageEffect(Unit target){
		int damage=0;
		String power=curreffects.get("Power");
		String param=curreffects.get("Type");
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
				damage=target.getStat(Stat.HP);
			else if(power.equals("Flail"))
				damage=calculateDamage(user,target,GameData.getMoveType(move.getNum()),calculateFlailPower());
			else if(power.equals("Hidden Power"))
				damage=calculateDamage(user,target,calculateHiddenType(),calculateHiddenPower());
			else if(power.equals("Magnitude"))
				damage=calculateDamage(user,target,GameData.getMoveType(move.getNum()),calculateMagnitudePower());
			else if(power.equals("Level"))
				damage=user.getPokemon().getLevel();
			else if(power.equals("Present"))
				damage=calculatePresentDamage(target);
			else if(power.equals("Psywave"))
				damage=calculatePsywaveDamage();
			else if(power.equals("HappinessBased"))
				damage=calculateDamage(user,target,GameData.getMoveType(move.getNum()),calculateHappinessBasedPower());
			else if(power.equals("HalveHP"))
				damage=target.getStat(Stat.HP)/2;
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
					&&damage>=target.getStat(Stat.HP)){
				System.out.println(target.getPokemon().getName()+" survives the attack at one life");
				damage=target.getStat(Stat.HP)-1;
			}
		}
		target.damage(damage);
	}

	private static int calculateHappinessBasedPower(){
		int power=(int)(user.getPokemon().getHappiness()/2.5);
		if(power==0)
			power=1;
		return power;
	}

	private static int calculatePsywaveDamage(){
		return GameData.getRandom().nextInt((int)(user.getPokemon().getLevel()*1.5))+1;
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
		int[] ivs=user.getPokemon().getIVs();
		//based on http://bulbapedia.bulbagarden.net/wiki/Hidden_Power_(move)/Calculation
		//4*attack mod 4 + defense mod 4 (0-15)
		int p=4*(ivs[1]%4)+(ivs[2]%4);
		Type[] types={Type.Fighting,Type.Flying,Type.Poison,Type.Ground,Type.Rock,Type.Bug,Type.Ghost,Type.Steel,Type.Fire,Type.Water,
				Type.Grass,Type.Electric,Type.Psychic,Type.Ice,Type.Dragon,Type.Dark};
		return types[p];
	}

	private static int calculateHiddenPower(){
		int[] ivs=user.getPokemon().getIVs();
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
		int p=(48*user.getStat(Stat.HP))/user.getPokemon().getStat(Stat.HP);
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
		Pokemon attackerpokemon=user.getPokemon();
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
		double randommod=(GameData.getRandom().nextInt(16)+85)/100;
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
		if(defender.hasProtectionCondition(ProtectionCondition.Protect)||defender.hasProtectionCondition(ProtectionCondition.Detect)){
			System.out.println(defender.getPokemon().getName()+" cannot be hit because they are protected.");
			return false;
		}
		if(defender.isFlying()&&movenum!=85&&movenum!=230&&movenum!=240&&movenum!=72){
			System.out.println(GameData.getMoveName(movenum)+" could not hit because "+defender.getPokemon().getName()+" is mid-air using Fly.");
			return false;
		}
		if(defender.isDigging()&&movenum!=43&&movenum!=55&&movenum!=67&&movenum!=114){
			System.out.println(GameData.getMoveName(movenum)+" could not hit because "+defender.getPokemon().getName()+" is underground using Dig.");
			return false;
		}
		if(movenum==62||movenum==221||movenum==243)
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
}