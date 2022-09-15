package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.ComponentDeleteEdit;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ComponentDeleteAction extends AbstractAction {
	private final RobotOverlord ro;
	private final Component component;

	public ComponentDeleteAction(String name, Component component, RobotOverlord ro) {
		super(name);
		this.ro = ro;
		this.component = component;
	}
	
    /**
     * select from a list of all object types.  An instance of that type is then added to the world.
     */
	@Override
	public void actionPerformed(ActionEvent event) {
		UndoSystem.addEvent(this,new ComponentDeleteEdit(ro,component.getEntity(),component));
    }
}
