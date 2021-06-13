package com.marginallyclever.robotOverlord.swingInterface.actions;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.robotOverlord.Entity;
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
		JPanel additionList = new JPanel(new GridLayout(0, 1));
		JComboBox<String> additionComboBox = buildEntityComboBox();
		additionList.add(additionComboBox);

		int result = JOptionPane.showConfirmDialog(ro.getMainFrame(), additionList, (String)this.getValue(AbstractAction.NAME), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			String objectTypeName = additionComboBox.getItemAt(additionComboBox.getSelectedIndex());

			Entity newInstance = createInstanceOf(objectTypeName);
			if(newInstance != null) ro.undoableEditHappened(new UndoableEditEvent(this,new AddEntityEdit(ro,newInstance)));
		}
    }

	private JComboBox<String> buildEntityComboBox() {
		JComboBox<String> additionComboBox = new JComboBox<String>();
		
		Iterator<Entity> i = ServiceLoader.load(Entity.class).iterator();
		while(i.hasNext()) {
			Entity lft = i.next();
			additionComboBox.addItem(lft.getName());
		}

		return additionComboBox;
	}

	private Entity createInstanceOf(String objectTypeName) {
		Iterator<Entity> i = ServiceLoader.load(Entity.class).iterator();
		while(i.hasNext()) {
			Entity lft = i.next();
			String name = lft.getName();
			if(name.equals(objectTypeName)) {
				try {
					return lft.getClass().getDeclaredConstructor().newInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
}
