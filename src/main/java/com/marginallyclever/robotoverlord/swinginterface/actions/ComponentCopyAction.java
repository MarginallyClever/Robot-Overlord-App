package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.UnicodeIcon;
import com.marginallyclever.robotoverlord.swinginterface.EditorAction;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.ComponentDeleteEdit;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Makes a deep copy of the selected {@link com.marginallyclever.robotoverlord.Entity}.
 */
public class ComponentCopyAction extends AbstractAction implements EditorAction {
	protected final RobotOverlord ro;
	protected final Component component;

	public ComponentCopyAction(Component component, RobotOverlord ro) {
		super(Translator.get("ComponentCopyAction.name"));
		this.ro=ro;
		this.component = component;
		putValue(Action.SMALL_ICON,new UnicodeIcon("📋"));
		putValue(Action.SHORT_DESCRIPTION, Translator.get("ComponentCopyAction.shortDescription"));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK) );
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		ro.setCopiedComponents(component);
	}

	private Entity makeDeepCopy(Entity entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateEnableStatus() {
		setEnabled(!ro.getSelectedEntities().isEmpty());
	}
}
