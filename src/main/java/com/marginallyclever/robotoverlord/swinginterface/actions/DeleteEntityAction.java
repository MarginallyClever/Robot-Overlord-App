package com.marginallyclever.robotoverlord.swinginterface.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.EditorAction;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.RemoveEntityEdit;

/**
 * @author Dan Royer
 */
public class DeleteEntityAction extends AbstractAction implements EditorAction {
	private final RobotOverlord ro;
	
	public DeleteEntityAction(String name, RobotOverlord ro) {
		super(name);
		this.ro = ro;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		UndoSystem.addEvent(this,new RemoveEntityEdit((String)this.getValue(Action.NAME),ro,ro.getSelectedEntities()));
	}

	@Override
	public void updateEnableStatus() {
		setEnabled(!ro.getSelectedEntities().isEmpty());
	}
}
