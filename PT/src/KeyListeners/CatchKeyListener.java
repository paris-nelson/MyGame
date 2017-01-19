package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Global.ControlsConfig;
import Objects.CatchLogic;

public class CatchKeyListener implements KeyListener {

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key=e.getKeyCode();
		if(key==ControlsConfig.LEFT)
			CatchLogic.moveCatchRangeLeft();
		else if(key==ControlsConfig.RIGHT)
			CatchLogic.moveCatchRangeRight();
		else if(key==ControlsConfig.UP)
			CatchLogic.moveCatchRangeUp();
		else if(key==ControlsConfig.DOWN)
			CatchLogic.moveCatchRangeDown();
		else if(key==ControlsConfig.START)
			CatchLogic.confirmCatchTarget();
		else if(key==ControlsConfig.BACK)
			CatchLogic.cancelCatchRange();
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}
