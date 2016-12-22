package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Engines.InventoryEngine;
import Global.ControlsConfig;

public class InventoryKeyListener implements KeyListener {

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key=e.getKeyCode();
		if(key==ControlsConfig.UP)
			InventoryEngine.moveUp();
		else if(key==ControlsConfig.DOWN)
			InventoryEngine.moveDown();
		else if(key==ControlsConfig.START)
			InventoryEngine.select();
		else if(key==ControlsConfig.LEFT)
			InventoryEngine.moveLeft();
		else if(key==ControlsConfig.RIGHT)
			InventoryEngine.moveRight();
	}

	@Override
	public void keyReleased(KeyEvent e) {
	
	}

}
