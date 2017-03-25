package com.marginallyclever.robotOverlord.actions;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.commands.CommandAddEntity;
import com.marginallyclever.robotOverlord.robot.Robot;

/**
 * Display an About dialog box
 * @author Admin
 *
 */
public class ActionAddEntity extends JMenuItem implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public ActionAddEntity(RobotOverlord ro) {
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
		
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTH;
		
		JComboBox<String> additionComboBox = new JComboBox<String>();
		additionList.add(additionComboBox);
		
		// service load the types available.
		ServiceLoader<Entity> loaders = ServiceLoader.load(Entity.class);
		Iterator<Entity> i = loaders.iterator();
		while(i.hasNext()) {
			Entity lft = i.next();
			additionComboBox.addItem(lft.getDisplayName());
		}

        
		int result = JOptionPane.showConfirmDialog(ro.getMainFrame(), additionList, "Add...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			String objectTypeName = additionComboBox.getItemAt(additionComboBox.getSelectedIndex());

			i = loaders.iterator();
			while(i.hasNext()) {
				Entity lft = i.next();
				String name = lft.getDisplayName();
				if(name.equals(objectTypeName)) {
					Entity newInstance = null;

					try {
						newInstance = lft.getClass().newInstance();
						if(newInstance instanceof Robot ) {
							((Robot) newInstance).setConnectionManager(ro.getConnectionManager());
						}
						
						ro.getUndoHelper().undoableEditHappened(new UndoableEditEvent(this,new CommandAddEntity(ro,newInstance) ) );
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
