package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Engines.DialogEngine;
import Enums.Control;
import Global.ControlsConfig;

public class DialogKeyListener implements KeyListener {

	@Override
	public void keyPressed(KeyEvent e) {
		//TODO:maybe looking into doing a semaphore type thing in all key listeners that sets a flag that is only
		//unset by keyrelease. or that unsets itself after method calls so it can't be interrupted.
		int key=e.getKeyCode();
		if(key==ControlsConfig.getKey(Control.Start))
			DialogEngine.next();
		else if(key==ControlsConfig.getKey(Control.Back))
			DialogEngine.previous();
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

}
