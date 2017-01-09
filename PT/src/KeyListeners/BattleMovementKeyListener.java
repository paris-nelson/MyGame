package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Engines.BattleEngine;
import Global.ControlsConfig;

public class BattleMovementKeyListener implements KeyListener{

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key=e.getKeyCode();
		if(key==ControlsConfig.LEFT)
			BattleEngine.moveLeft();
		else if(key==ControlsConfig.RIGHT)
			BattleEngine.moveRight();
		else if(key==ControlsConfig.UP)
			BattleEngine.moveUp();
		else if(key==ControlsConfig.DOWN)
			BattleEngine.moveDown();
		else if(key==ControlsConfig.START)
			BattleEngine.confirmMovement();
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}
