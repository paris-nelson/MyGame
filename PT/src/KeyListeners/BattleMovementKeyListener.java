package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Engines.BattleEngine;
import Enums.Control;
import Global.ControlsConfig;
import Objects.BattleMovementLogic;

public class BattleMovementKeyListener implements KeyListener{

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key=e.getKeyCode();
		if(key==ControlsConfig.getKey(Control.Left))
			BattleMovementLogic.moveLeft();
		else if(key==ControlsConfig.getKey(Control.Right))
			BattleMovementLogic.moveRight();
		else if(key==ControlsConfig.getKey(Control.Up))
			BattleMovementLogic.moveUp();
		else if(key==ControlsConfig.getKey(Control.Down))
			BattleMovementLogic.moveDown();
		else if(key==ControlsConfig.getKey(Control.Start))
			BattleMovementLogic.confirmMovement();
		else if(key==ControlsConfig.getKey(Control.Back))
			BattleMovementLogic.cancelMovement();
		else if(key==KeyEvent.VK_G)
			BattleEngine.lose();
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}
