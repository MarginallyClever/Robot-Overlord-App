package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.EntityFactory;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.UnicodeIcon;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.swinginterface.EditorAction;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.EntityAddEdit;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Display an Add Entity dialog box.  If an entity is selected and "ok" is pressed, add that Entity to the world. 
 * @author Dan Royer
 *
 */
public class EntityAddChildAction extends AbstractAction implements EditorAction {
	private static final Logger logger = LoggerFactory.getLogger(EntityAddChildAction.class);

	protected JComponent parentComponent;
	
	public EntityAddChildAction(JComponent parentComponent) {
		super(Translator.get("EntityAddChildAction.name"));
		putValue(SMALL_ICON,new UnicodeIcon("+"));
		putValue(SHORT_DESCRIPTION, Translator.get("EntityAddChildAction.shortDescription"));
		this.parentComponent = parentComponent;
	}
	
    /**
     * select from a list of all object types.  An instance of that type is then added to the world.
     */
	@Override
	public void actionPerformed(ActionEvent event) {
		List<Entity> list = Clipboard.getSelectedEntities();

		JComboBox<String> additionComboBox = buildEntityComboBox();
		int result = JOptionPane.showConfirmDialog(
				parentComponent,
				additionComboBox, 
				(String)this.getValue(AbstractAction.NAME), 
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			String name = additionComboBox.getItemAt(additionComboBox.getSelectedIndex());
			for(Entity parent : list) {
				createInstanceOf(parent,name);
			}
		}
    }

	private JComboBox<String> buildEntityComboBox() {
		JComboBox<String> box = new JComboBox<>();
		ArrayList<String> names = EntityFactory.getAllEntityNames();
		for( String n : names ) box.addItem(n);
		return box;
	}

	private void createInstanceOf(Entity parent,String className) {
		try {
			Entity newInstance = EntityFactory.load(className);
			if(newInstance != null) UndoSystem.addEvent(this,new EntityAddEdit(parent,newInstance));
		} catch (Exception e) {
			String msg = "Failed to instance "+className+": "+e.getLocalizedMessage();
			JOptionPane.showMessageDialog(parentComponent,msg);
			logger.error(msg);
		}
	}

	@Override
	public void updateEnableStatus() {
		setEnabled(Clipboard.getSelectedEntities().size()==1);
	}
}
