package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Engines.PCEngine;
import Global.ControlsConfig;

public class PCKeyListener implements KeyListener{

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key=e.getKeyCode();
		if(key==ControlsConfig.DOWN)
			PCEngine.moveDown();
		else if(key==ControlsConfig.UP)
			PCEngine.moveUp();
		else if(key==ControlsConfig.LEFT)
			PCEngine.moveLeft();
		else if(key==ControlsConfig.RIGHT)
			PCEngine.moveRight();
		else if(key==ControlsConfig.START)
			PCEngine.select();
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

}
