package Enums;

import Global.Constants;

public enum Weather {
	Sun,Rain,Sand,None;
	
	public static int getDuration(Weather w) {
		switch(w) {
		case Sun:
			return Constants.SUNNY_DAY_DURATION;
		case Rain:
			return Constants.RAIN_DANCE_DURATION;
		case Sand:
			return Constants.SANDSTORM_DURATION;
		case None:
			return 0;
		default:
			return 0;
		}
	}
}
