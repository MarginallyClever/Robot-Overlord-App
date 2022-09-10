package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.ComponentFactory;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.ComponentPanel;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.AddComponentEdit;
import com.marginallyclever.robotoverlord.swinginterface.edits.DeleteComponentEdit;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Display an `Add Component` dialog box.  If an {@link Component} is selected and
 * "ok" is pressed, add that Component to the world.
 * @author Dan Royer
 *
 */
public class DeleteComponentAction extends AbstractAction {
	private final RobotOverlord ro;
	private final Component component;

	public DeleteComponentAction(String name,Component component,RobotOverlord ro) {
		super(name);
		this.ro = ro;
		this.component = component;
	}
	
    /**
     * select from a list of all object types.  An instance of that type is then added to the world.
     */
	@Override
	public void actionPerformed(ActionEvent event) {
		UndoSystem.addEvent(this,new DeleteComponentEdit(ro,component.getEntity(),component));
    }
}
