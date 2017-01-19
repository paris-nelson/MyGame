package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Engines.UnitPlacementEngine;
import Global.ControlsConfig;

public class UnitPlacementKeyListener implements KeyListener {

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key=e.getKeyCode();
		if(key==ControlsConfig.UP)
			UnitPlacementEngine.moveUp();
		else if(key==ControlsConfig.DOWN)
			UnitPlacementEngine.moveDown();
		else if(key==ControlsConfig.LEFT)
			UnitPlacementEngine.cycleLeft();
		else if(key==ControlsConfig.RIGHT)
			UnitPlacementEngine.cycleRight();
		else if(key==ControlsConfig.START)
			UnitPlacementEngine.select();
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}
