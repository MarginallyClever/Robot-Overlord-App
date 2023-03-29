package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.UnicodeIcon;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.ComponentDeleteEdit;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ComponentDeleteAction extends AbstractAction {
	private final RobotOverlord ro;
	private final Component component;

	public ComponentDeleteAction(Component component, RobotOverlord ro) {
		super(Translator.get("ComponentDeleteAction.name"));
		this.ro = ro;
		this.component = component;
		putValue(Action.SHORT_DESCRIPTION, Translator.get("ComponentDeleteAction.shortDescription"));
		putValue(Action.SMALL_ICON,new UnicodeIcon("🗑"));
	}
	
    /**
     * select from a list of all object types.  An instance of that type is then added to the world.
     */
	@Override
	public void actionPerformed(ActionEvent event) {
		UndoSystem.addEvent(this,new ComponentDeleteEdit(ro,component.getEntity(),component));
    }
}
