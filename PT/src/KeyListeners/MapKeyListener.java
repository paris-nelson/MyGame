package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Engines.MapEngine;
import Enums.Control;
import Global.ControlsConfig;

public class MapKeyListener implements KeyListener{

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key=e.getKeyCode();
		if(key==ControlsConfig.getKey(Control.Down))
			MapEngine.moveDown();
		else if(key==ControlsConfig.getKey(Control.Up))
			MapEngine.moveUp();
		else if(key==ControlsConfig.getKey(Control.Left))
			MapEngine.moveLeft();
		else if(key==ControlsConfig.getKey(Control.Right))
			MapEngine.moveRight();
		else if(key==ControlsConfig.getKey(Control.Start))
			MapEngine.openMenu();
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

}
