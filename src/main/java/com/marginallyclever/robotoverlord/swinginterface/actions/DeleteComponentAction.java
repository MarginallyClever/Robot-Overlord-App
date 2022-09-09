package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.ComponentFactory;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.AddComponentEdit;
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
	protected final RobotOverlord ro;

	public DeleteComponentAction(RobotOverlord ro) {
		super(Translator.get("Add Component"));
        putValue(SHORT_DESCRIPTION, Translator.get("Add a component to the world."));
		this.ro = ro;
	}
	
    /**
     * select from a list of all object types.  An instance of that type is then added to the world.
     */
	@Override
	public void actionPerformed(ActionEvent event) {
		List<Entity> list = ro.getSelectedEntities();

		JComboBox<String> additionComboBox = buildComponentComboBox();
		int result = JOptionPane.showConfirmDialog(
				ro.getMainFrame(), 
				additionComboBox, 
				(String)this.getValue(AbstractAction.NAME), 
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			for(Entity parent : list) {
				createInstanceOf(parent,additionComboBox.getItemAt(additionComboBox.getSelectedIndex()));
			}
			ro.updateSelectEntities();
		}
    }

	private JComboBox<String> buildComponentComboBox() {
		JComboBox<String> box = new JComboBox<>();
		ArrayList<String> names = ComponentFactory.getAllComponentNames();
		for( String n : names ) box.addItem(n);
		return box;
	}

	private void createInstanceOf(Entity parent,String className) {
		try {
			Component newInstance = ComponentFactory.load(className);
			if(newInstance != null) UndoSystem.addEvent(this,new AddComponentEdit(ro,parent,newInstance));
		} catch (Exception e) {
			String msg = "Failed to instance "+className+": "+e.getLocalizedMessage();
			JOptionPane.showMessageDialog(ro.getMainFrame(),msg);
			Log.error(msg);
		}
	}
}
