package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Engines.PCEngine;
import Enums.Control;
import Global.ControlsConfig;

public class PCKeyListener implements KeyListener{

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key=e.getKeyCode();
		if(key==ControlsConfig.getKey(Control.Down))
			PCEngine.moveDown();
		else if(key==ControlsConfig.getKey(Control.Up))
			PCEngine.moveUp();
		else if(key==ControlsConfig.getKey(Control.Left))
			PCEngine.moveLeft();
		else if(key==ControlsConfig.getKey(Control.Right))
			PCEngine.moveRight();
		else if(key==ControlsConfig.getKey(Control.Start))
			PCEngine.select();
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

}
