package Enums;

public enum MoveImpact {
	EnemyOnly,FriendOnly,Self,Any
	//TODO:now to actually add these impacts to the moveeffects file. need to manually determine for each effect whats right.
	//for self effects,need to remove the target=self and add impact=self instead in the right place. This will also be a good
	//point to finally go through movestrings and fix them up as well.
}
