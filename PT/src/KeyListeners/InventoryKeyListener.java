package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Engines.InventoryEngine;
import Enums.Control;
import Global.ControlsConfig;

public class InventoryKeyListener implements KeyListener {

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key=e.getKeyCode();
		if(key==ControlsConfig.getKey(Control.Up))
			InventoryEngine.moveUp();
		else if(key==ControlsConfig.getKey(Control.Down))
			InventoryEngine.moveDown();
		else if(key==ControlsConfig.getKey(Control.Start))
			InventoryEngine.select();
		else if(key==ControlsConfig.getKey(Control.Left))
			InventoryEngine.moveLeft();
		else if(key==ControlsConfig.getKey(Control.Right))
			InventoryEngine.moveRight();
	}

	@Override
	public void keyReleased(KeyEvent e) {
	
	}

}
