package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Engines.ShopEngine;
import Global.ControlsConfig;

public class ShopKeyListener implements KeyListener {

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key=e.getKeyCode();
		if(key==ControlsConfig.LEFT)
			ShopEngine.moveLeft();
		else if(key==ControlsConfig.RIGHT)
			ShopEngine.moveRight();
		else if(key==ControlsConfig.UP)
			ShopEngine.moveUp();
		else if(key==ControlsConfig.DOWN)
			ShopEngine.moveDown();
		else if(key==ControlsConfig.START)
			ShopEngine.select();
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

}
