package com.marginallyclever.robotOverlord.swingInterface.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.EntityFactory;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.swingInterface.translator.Translator;
import com.marginallyclever.robotOverlord.swingInterface.undoableEdits.AddEntityEdit;

/**
 * Display an Add Entity dialog box.  If an entity is selected and "ok" is pressed, add that Entity to the world. 
 * @author Dan Royer
 *
 */
public class AddEntityAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public AddEntityAction(RobotOverlord ro) {
		super(Translator.get("Add Entity"));
        putValue(SHORT_DESCRIPTION, Translator.get("Add an entity to the world."));
		this.ro = ro;
	}
	
    /**
     * select from a list of all object types.  An instance of that type is then added to the world.
     */
	@Override
	public void actionPerformed(ActionEvent event) {
		JComboBox<String> additionComboBox = buildEntityComboBox();
		int result = JOptionPane.showConfirmDialog(
				ro.getMainFrame(), 
				additionComboBox, 
				(String)this.getValue(AbstractAction.NAME), 
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			createInstanceOf(additionComboBox.getItemAt(additionComboBox.getSelectedIndex()));
		}
    }

	private JComboBox<String> buildEntityComboBox() {
		JComboBox<String> box = new JComboBox<String>();
		ArrayList<String> names = EntityFactory.getAllEntityNames();
		for( String n : names ) box.addItem(n);
		return box;
	}

	private void createInstanceOf(String className) {
		try {
			Entity newInstance = EntityFactory.load(className);
			if(newInstance != null) ro.undoableEditHappened(new UndoableEditEvent(this,new AddEntityEdit(ro,newInstance)));
		} catch (Exception e) {
			String msg = "Failed to instance "+className+": "+e.getLocalizedMessage();
			JOptionPane.showMessageDialog(ro.getMainFrame(),msg);
			Log.error(msg);
		}
	}
}
