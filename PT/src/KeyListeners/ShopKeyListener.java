package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Engines.ShopEngine;
import Enums.Control;
import Global.ControlsConfig;

public class ShopKeyListener implements KeyListener {

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key=e.getKeyCode();
		if(key==ControlsConfig.getKey(Control.Left))
			ShopEngine.moveLeft();
		else if(key==ControlsConfig.getKey(Control.Right))
			ShopEngine.moveRight();
		else if(key==ControlsConfig.getKey(Control.Up))
			ShopEngine.moveUp();
		else if(key==ControlsConfig.getKey(Control.Down))
			ShopEngine.moveDown();
		else if(key==ControlsConfig.getKey(Control.Start))
			ShopEngine.select();
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

}
