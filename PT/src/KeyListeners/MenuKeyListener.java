package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Engines.MenuEngine;
import Enums.Control;
import Global.ControlsConfig;

public class MenuKeyListener implements KeyListener{

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key=e.getKeyCode();
		if(key==ControlsConfig.getKey(Control.Down))
			MenuEngine.moveDown();
		else if(key==ControlsConfig.getKey(Control.Up))
			MenuEngine.moveUp();
		else if(key==ControlsConfig.getKey(Control.Start))
			MenuEngine.select();
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

}
