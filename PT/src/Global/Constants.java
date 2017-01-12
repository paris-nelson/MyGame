package Global;

public class Constants {
	//Happiness variables
	public static final int HAPPINESS_LOST_ON_FAINT=1;
	public static final int HAPPINES_GAINED_ON_LEVEL_UP_OR_VITAMIN=5;
	public static final int HAPPINESS_GAINED_ON_EVOLUTION=10;
	public static final int MAX_HAPPINESS=255;
	public static final int HAPPINESS_EVOLUTION_THRESHOLD=220;
	
	//Basic parameters
	public static final int PRIZE_MONEY_BASE=20;
	public static final int PRIZE_MONEY_INCREMEMENT=10;
	public static final int NUM_ITEMS=132;
	public static final int NUM_MOVES=251;
	public static final int NUM_POKEMON=251;
	public static final int MAX_ITEM_QUANTITY=99;
	public static final int MAX_PC_POKEMON_QUANTITY=6;
	public static final int NUM_LOOT_LVL_THRESHOLDS=39;
	public static final int NUM_TRAINERS=132;
	public static final int SCREEN_WIDTH=1325;
	public static final int SCREEN_HEIGHT=1000;
	public static final String PATH=System.getProperty("user.dir")+"\\";
			//"C:\\Users\\paris.nelson\\Documents\\PT\\PT\\";
	public static final int MAX_MONEY=999999;
	
	//MapEngine variables
	public static final int WILD_ENCOUNTER_RATE=18;
	public static final int LEGEND_ENCOUNTER_RATE=50;
	public static final int TRAINER_ICON_SIZE=50;
	public static final int MAIN_MAP_MOVEMENT_SPEED=1;
	public static final int SUB_MAP_MOVEMENT_SPEED=3;
	public static final int LOGICAL_MAP_WIDTH=125;
	public static final int LOGICAL_MAP_HEIGHT=58;
	public static final int LOGICAL_MAP_RATIO=10;

	//BattleEngine params
	public static final int BATTLEFIELD_WIDTH=12;
	public static final int BATTLEFIELD_HEIGHT=12;
	public static final int MOVEMENT_RANGE_MIN=2;
	public static final int MOVEMENT_RANGE_MAX=6;
	public static final int SPEED_PER_UNIT_MOVEMENT_RANGE=76;
	public static final int TILE_SIZE=56;
	public static final double SIDE_FACING_ACCURACY_RATE=.9;
	public static final double FRONT_FACING_ACCURACY_RATE=.8;
	
	//Move specific constants
	public static final double BURN_PHYSICAL_ATTACK_DAMAGE_REDUCTION=.33;
	public static final double BURN_POISON_HP_LOSS_RATE=.0833;
	public static final double TRAP_HP_LOSS_RATE=.0625;
	public static final double LEECH_SEED_HP_LOSS_RATE=.0833;
	public static final double CURSE_NIGHTMARE_HP_LOSS_RATE=.25;
	public static final int PERISH_SONG_TURNS=4;
	public static final int TRAP_MIN_TURNS=2;
	public static final int TRAP_MAX_TURNS=5;
	public static final int ENCORE_MIN_TURNS=2;
	public static final int ENCORE_MAX_TURNS=5;
	public static final int CONFUSE_MAX_TURNS=4;
	public static final int SLEEP_MAX_TURNS=4;
	public static final int FREEZE_MAX_TURNS=4;
	public static final int DISABLE_MIN_TURNS=2;
	public static final int DISABLE_MAX_TURNS=5;
	public static final int PARALYSIS_INACTION_CHANCE=25;
	public static final int ATTRACT_INACTION_CHANCE=50;
	public static final double SPIKES_DAMAGE_RATE=.125;
	public static final double MINIMIZE_RATIO=.75;
	public static final int PAY_DAY_MULTIPLIER=5;
	
	//item specific constants
	public static final double LEFTOVERS_HP_RECOV_RATE=.0833;
	public static final int QUICK_CLAW_CHANCE=20;
	public static final int FOCUS_BAND_CHANCE=20;
	public static final int KINGS_ROCK_FLINCH_CHANCE=10;
}
