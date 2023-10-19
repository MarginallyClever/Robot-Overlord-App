package com.marginallyclever.robotoverlord.swing.actions;

import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.ComponentFactory;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.swing.ComponentFactoryPanel;
import com.marginallyclever.robotoverlord.swing.UnicodeIcon;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.swing.componentmanagerpanel.ComponentManagerPanel;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.edits.ComponentAddEdit;
import com.marginallyclever.robotoverlord.swing.translator.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Display an `Add Component` dialog box.  If an {@link Component} is selected and
 * "ok" is pressed, add that Component to the world.
 * @author Dan Royer
 *
 */
public class ComponentAddAction extends AbstractAction {
	private static final Logger logger = LoggerFactory.getLogger(ComponentAddAction.class);

	protected final ComponentManagerPanel componentManagerPanel;

	public ComponentAddAction(ComponentManagerPanel componentManagerPanel) {
		super(Translator.get("ComponentAddAction.name"));
		putValue(SMALL_ICON,new UnicodeIcon("+"));
        putValue(SHORT_DESCRIPTION, Translator.get("ComponentAddAction.shortDescription"));
		this.componentManagerPanel = componentManagerPanel;
	}
	
    /**
     * select from a list of all object types.  An instance of that type is then added to the world.
     */
	@Override
	public void actionPerformed(ActionEvent event) {
		List<Entity> list = Clipboard.getSelectedEntities();

		ComponentFactoryPanel panel = new ComponentFactoryPanel();

		int result = JOptionPane.showConfirmDialog(
				SwingUtilities.getWindowAncestor(componentManagerPanel),
				panel,
				(String)this.getValue(AbstractAction.NAME), 
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			for(Entity parent : list) {
				createInstanceOf(parent,panel.getSelectedClassName());
			}
			componentManagerPanel.refreshContentsFromClipboard();
		}
    }

	private void createInstanceOf(Entity parent,String className) {
		try {
			Component newInstance = ComponentFactory.load(className);
			if(newInstance != null) UndoSystem.addEvent(new ComponentAddEdit(componentManagerPanel,parent,newInstance));
		} catch (Exception e) {
			String msg = "Failed to instance "+className+": "+e.getLocalizedMessage();
			JOptionPane.showMessageDialog(componentManagerPanel,msg);
			logger.error(msg);
		}
	}
}
