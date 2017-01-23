package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Engines.ControlsEngine;
import Enums.Control;
import Global.ControlsConfig;

public class ControlsKeyListener implements KeyListener {

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		int key=e.getKeyCode();
		if(ControlsEngine.isSelected()){
			ControlsEngine.map(key);
		}
		else{
			if(key==ControlsConfig.getKey(Control.Up))
				ControlsEngine.previous();
			else if(key==ControlsConfig.getKey(Control.Down)&&ControlsEngine.isOnControlsList())
				ControlsEngine.next();
			else if(key==ControlsConfig.getKey(Control.Left))
				ControlsEngine.moveLeft();
			else if(key==ControlsConfig.getKey(Control.Right))
				ControlsEngine.moveRight();
			else if(key==ControlsConfig.getKey(Control.Start))
				ControlsEngine.select();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

}
