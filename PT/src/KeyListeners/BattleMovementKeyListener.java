package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Engines.BattleEngine;
import Global.ControlsConfig;
import Objects.BattleMovementLogic;

public class BattleMovementKeyListener implements KeyListener{

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key=e.getKeyCode();
		if(key==ControlsConfig.LEFT)
			BattleMovementLogic.moveLeft();
		else if(key==ControlsConfig.RIGHT)
			BattleMovementLogic.moveRight();
		else if(key==ControlsConfig.UP)
			BattleMovementLogic.moveUp();
		else if(key==ControlsConfig.DOWN)
			BattleMovementLogic.moveDown();
		else if(key==ControlsConfig.START)
			BattleMovementLogic.confirmMovement();
		else if(key==ControlsConfig.BACK)
			BattleMovementLogic.cancelMovement();
		else if(key==KeyEvent.VK_G)
			BattleEngine.lose();
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}
