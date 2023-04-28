package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.EntityFactory;
import com.marginallyclever.robotoverlord.EntityManager;
import com.marginallyclever.robotoverlord.swinginterface.UnicodeIcon;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.swinginterface.EditorAction;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.EntityAddEdit;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
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
	private final EntityManager entityManager;
	
	public EntityAddChildAction(EntityManager entityManager) {
		super(Translator.get("EntityAddChildAction.name"));
		this.entityManager = entityManager;
		putValue(SMALL_ICON,new UnicodeIcon("+"));
		putValue(SHORT_DESCRIPTION, Translator.get("EntityAddChildAction.shortDescription"));
	}
	
    /**
     * select from a list of all object types.  An instance of that type is then added to the world.
     */
	@Override
	public void actionPerformed(ActionEvent event) {
		Component source = (Component) event.getSource();
		JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(source);

		List<Entity> list = Clipboard.getSelectedEntities();

		JComboBox<String> additionComboBox = buildEntityComboBox();
		int result = JOptionPane.showConfirmDialog(
				parentFrame,
				additionComboBox, 
				(String)this.getValue(AbstractAction.NAME), 
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			String name = additionComboBox.getItemAt(additionComboBox.getSelectedIndex());
			for(Entity parent : list) {
				createInstanceOf(parent,name,parentFrame);
			}
		}
    }

	private JComboBox<String> buildEntityComboBox() {
		JComboBox<String> box = new JComboBox<>();
		ArrayList<String> names = EntityFactory.getAllEntityNames();
		for( String n : names ) box.addItem(n);
		return box;
	}

	private void createInstanceOf(Entity parent,String className,JFrame parentFrame) {
		try {
			Entity newInstance = EntityFactory.load(className);
			if(newInstance != null) UndoSystem.addEvent(this,new EntityAddEdit(entityManager,parent,newInstance));
		} catch (Exception e) {
			String msg = "Failed to instance "+className+": "+e.getLocalizedMessage();
			JOptionPane.showMessageDialog(parentFrame,msg);
			logger.error(msg);
		}
	}

	@Override
	public void updateEnableStatus() {
		setEnabled(Clipboard.getSelectedEntities().size()==1);
	}
}
