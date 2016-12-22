package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Engines.MenuEngine;
import Global.ControlsConfig;

public class MenuKeyListener implements KeyListener{

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key=e.getKeyCode();
		if(key==ControlsConfig.DOWN)
			MenuEngine.moveDown();
		else if(key==ControlsConfig.UP)
			MenuEngine.moveUp();
		else if(key==ControlsConfig.START)
			MenuEngine.select();
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

}
