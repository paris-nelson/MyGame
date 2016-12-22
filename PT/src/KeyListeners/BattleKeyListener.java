package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Engines.BattleEngine;
import Global.ControlsConfig;

public class BattleKeyListener implements KeyListener{

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key=e.getKeyCode();
		if(key==ControlsConfig.START)
			BattleEngine.openPlayerMenu();	
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}
