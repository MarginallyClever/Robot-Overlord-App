package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.EditorAction;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.EntityDeleteEdit;

import javax.swing.*;
import java.awt.event.ActionEvent;

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
		UndoSystem.addEvent(this,new EntityDeleteEdit((String)this.getValue(Action.NAME),ro,ro.getSelectedEntities()));
	}

	@Override
	public void updateEnableStatus() {
		setEnabled(!ro.getSelectedEntities().isEmpty());
	}
}
