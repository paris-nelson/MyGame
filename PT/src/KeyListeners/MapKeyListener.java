package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Engines.MapEngine;
import Global.ControlsConfig;

public class MapKeyListener implements KeyListener{

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key=e.getKeyCode();
		if(key==ControlsConfig.DOWN)
			MapEngine.moveDown();
		else if(key==ControlsConfig.UP)
			MapEngine.moveUp();
		else if(key==ControlsConfig.LEFT)
			MapEngine.moveLeft();
		else if(key==ControlsConfig.RIGHT)
			MapEngine.moveRight();
		else if(key==ControlsConfig.START)
			MapEngine.openMenu();
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

}
