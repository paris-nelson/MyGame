package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Engines.UnitPlacementEngine;
import Enums.Control;
import Global.ControlsConfig;

public class UnitPlacementKeyListener implements KeyListener {

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key=e.getKeyCode();
		if(key==ControlsConfig.getKey(Control.Up))
			UnitPlacementEngine.moveUp();
		else if(key==ControlsConfig.getKey(Control.Down))
			UnitPlacementEngine.moveDown();
		else if(key==ControlsConfig.getKey(Control.Left))
			UnitPlacementEngine.cycleLeft();
		else if(key==ControlsConfig.getKey(Control.Right))
			UnitPlacementEngine.cycleRight();
		else if(key==ControlsConfig.getKey(Control.Start))
			UnitPlacementEngine.select();
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}
