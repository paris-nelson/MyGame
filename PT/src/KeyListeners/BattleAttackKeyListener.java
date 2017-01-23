package KeyListeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Enums.Control;
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
		if(key==ControlsConfig.getKey(Control.Left))
			BattleAttackLogic.moveAttackRangeLeft();
		else if(key==ControlsConfig.getKey(Control.Right))
			BattleAttackLogic.moveAttackRangeRight();
		else if(key==ControlsConfig.getKey(Control.Up))
			BattleAttackLogic.moveAttackRangeUp();
		else if(key==ControlsConfig.getKey(Control.Down))
			BattleAttackLogic.moveAttackRangeDown();
		else if(key==ControlsConfig.getKey(Control.Start))
			BattleAttackLogic.confirmAttackRange(cancellable);
		else if(key==ControlsConfig.getKey(Control.Back)&&cancellable)
			BattleAttackLogic.cancelAttackRange();
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}
