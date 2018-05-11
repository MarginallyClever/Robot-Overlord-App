package com.marginallyclever.robotOverlord.commands;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.actions.UndoableActionRemoveEntity;
import com.marginallyclever.robotOverlord.entity.Entity;

/**
 * Remove a selected entity from the world.
 * @author Admin
 *
 */
public class UserCommandRemoveEntity extends JMenuItem implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public UserCommandRemoveEntity(RobotOverlord ro) {
		super("Remove...");
        getAccessibleContext().setAccessibleDescription("Remove an entity from the world.");
		this.ro = ro;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		selectAndRemoveObject();
	}
	
	
    /**
     * Select from a list of all objects in the world.  The selected object is then removed and destroyed.
     */
    public void selectAndRemoveObject() {
		JPanel additionList = new JPanel(new GridLayout(0, 1));
		
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTH;

		JComboBox<String> removeComboBox = new JComboBox<String>();
		additionList.add(removeComboBox);
		
		// service load the types available.
		List<String> names = ro.getWorld().namesOfAllObjects();
		Iterator<String> i = names.iterator();
		while(i.hasNext()) {
			String name = i.next();
			removeComboBox.addItem(name);
		}

        
		int result = JOptionPane.showConfirmDialog(ro.getMainFrame(), additionList, "Remove...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			String targetName = removeComboBox.getItemAt(removeComboBox.getSelectedIndex());
			Entity targetInstance = ro.getWorld().findObjectWithName(targetName);

			ro.getUndoHelper().undoableEditHappened(new UndoableEditEvent(this,new UndoableActionRemoveEntity(ro,targetInstance) ) );

	    	return;
		}
    }
}
