package com.marginallyclever.robotOverlord.swingInterface.commands;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.swingInterface.actions.ActionEntityAdd;

/**
 * Display an Add Entity dialog box.  If an entity is selected and "ok" is pressed, add that Entity to the world. 
 * @author Dan Royer
 *
 */
public class CommandAddEntity extends JButton implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public CommandAddEntity(RobotOverlord ro) {
		super("Add...");
        getAccessibleContext().setAccessibleDescription("Add an entity to the world.");
		this.ro = ro;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		selectAndAddObject();
	}
	
	
    /**
     * select from a list of all object types.  An instance of that type is then added to the world.
     */
    public void selectAndAddObject() {
		JPanel additionList = new JPanel(new GridLayout(0, 1));
		
		JComboBox<String> additionComboBox = new JComboBox<String>();
		additionList.add(additionComboBox);
		
		// service load the types available.
		ServiceLoader<Entity> loaders = ServiceLoader.load(Entity.class);
		int loadedTypes=0;
		Iterator<Entity> i = loaders.iterator();
		while(i.hasNext()) {
			Entity lft = i.next();
			additionComboBox.addItem(lft.getName());
			++loadedTypes;
		}
		
		assert(loadedTypes!=0);

        
		int result = JOptionPane.showConfirmDialog(ro.getMainFrame(), additionList, "Add...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			String objectTypeName = additionComboBox.getItemAt(additionComboBox.getSelectedIndex());

			i = loaders.iterator();
			while(i.hasNext()) {
				Entity lft = i.next();
				String name = lft.getName();
				if(name.equals(objectTypeName)) {
					Entity newInstance = null;

					try {
						newInstance = lft.getClass().newInstance();
						// create an undoable command to add this entity.
						ro.undoableEditHappened(new UndoableEditEvent(this,new ActionEntityAdd(ro,newInstance) ) );
					} catch (InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
					}
					return;
				}
			}
			// TODO catch selected an item to load, then couldn't find object class?!  Should be impossible.
		}
    }
}
