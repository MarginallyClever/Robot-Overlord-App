package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.EntityFactory;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.EditorAction;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.EntityAddEdit;

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
	protected RobotOverlord ro;
	
	public EntityAddChildAction(String name, RobotOverlord ro) {
		super(name);
		this.ro = ro;
	}
	
    /**
     * select from a list of all object types.  An instance of that type is then added to the world.
     */
	@Override
	public void actionPerformed(ActionEvent event) {
		List<Entity> list = ro.getSelectedEntities();

		JComboBox<String> additionComboBox = buildEntityComboBox();
		int result = JOptionPane.showConfirmDialog(
				ro.getMainFrame(), 
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
			JOptionPane.showMessageDialog(ro.getMainFrame(),msg);
			Log.error(msg);
		}
	}

	@Override
	public void updateEnableStatus() {
		setEnabled(ro.getSelectedEntities().size()==1);
	}
}
