package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Engines.BattleEngine;
import Global.ControlsConfig;

public class BattleAttackKeyListener implements KeyListener {

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key=e.getKeyCode();
		if(key==ControlsConfig.LEFT)
			BattleEngine.moveAttackRangeLeft();
		else if(key==ControlsConfig.RIGHT)
			BattleEngine.moveAttackRangeRight();
		else if(key==ControlsConfig.UP)
			BattleEngine.moveAttackRangeUp();
		else if(key==ControlsConfig.DOWN)
			BattleEngine.moveAttackRangeDown();
		else if(key==ControlsConfig.START)
			BattleEngine.confirmAttackRange();
		else if(key==ControlsConfig.BACK)
			BattleEngine.cancelAttackRange();
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}
