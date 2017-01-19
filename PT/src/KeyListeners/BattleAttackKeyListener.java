package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Global.ControlsConfig;
import Objects.BattleAttackLogic;

public class BattleAttackKeyListener implements KeyListener {
	
	private boolean cancellable;
	
	public BattleAttackKeyListener(boolean cancellable){
		this.cancellable=cancellable;
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key=e.getKeyCode();
		if(key==ControlsConfig.LEFT)
			BattleAttackLogic.moveAttackRangeLeft();
		else if(key==ControlsConfig.RIGHT)
			BattleAttackLogic.moveAttackRangeRight();
		else if(key==ControlsConfig.UP)
			BattleAttackLogic.moveAttackRangeUp();
		else if(key==ControlsConfig.DOWN)
			BattleAttackLogic.moveAttackRangeDown();
		else if(key==ControlsConfig.START)
			BattleAttackLogic.confirmAttackRange(cancellable);
		else if(key==ControlsConfig.BACK&&cancellable)
			BattleAttackLogic.cancelAttackRange();
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}
