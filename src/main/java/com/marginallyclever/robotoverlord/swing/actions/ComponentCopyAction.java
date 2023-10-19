package com.marginallyclever.robotoverlord.swing.actions;

import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.swing.UnicodeIcon;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.swing.EditorAction;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Makes a deep copy of the selected {@link Entity}.
 */
public class ComponentCopyAction extends AbstractAction implements EditorAction {
	private Component component;

	public ComponentCopyAction() {
		super(Translator.get("ComponentCopyAction.name"));
		putValue(SMALL_ICON,new UnicodeIcon("📋"));
		putValue(SHORT_DESCRIPTION, Translator.get("ComponentCopyAction.shortDescription"));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK) );
	}

	public void setComponent(Component component) {
		this.component = component;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		Clipboard.setCopiedComponents(component);
	}

	private Entity makeDeepCopy(Entity entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateEnableStatus() {
		setEnabled(!Clipboard.getSelectedEntities().isEmpty());
	}
}
