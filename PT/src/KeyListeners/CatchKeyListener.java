package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Enums.Control;
import Global.ControlsConfig;
import Objects.CatchLogic;

public class CatchKeyListener implements KeyListener {

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key=e.getKeyCode();
		if(key==ControlsConfig.getKey(Control.Left))
			CatchLogic.moveCatchRangeLeft();
		else if(key==ControlsConfig.getKey(Control.Right))
			CatchLogic.moveCatchRangeRight();
		else if(key==ControlsConfig.getKey(Control.Up))
			CatchLogic.moveCatchRangeUp();
		else if(key==ControlsConfig.getKey(Control.Down))
			CatchLogic.moveCatchRangeDown();
		else if(key==ControlsConfig.getKey(Control.Start))
			CatchLogic.confirmCatchTarget();
		else if(key==ControlsConfig.getKey(Control.Back))
			CatchLogic.cancelCatchRange();
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}
