package com.marginallyclever.robotOverlord;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Display an About dialog box
 * @author Admin
 *
 */
public class ActionRemoveEntity extends JMenuItem implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public ActionRemoveEntity(RobotOverlord ro) {
		super("Add...");
        getAccessibleContext().setAccessibleDescription("Add an entity to the world.");
		this.ro = ro;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		selectAndRemoveObject();
	}
	
	
    /**
     * select from a list of all objects in the world.  the selected object is then removed and destroyed.
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
			ObjectInWorld obj = ro.getWorld().findObjectWithName(targetName);
			ro.getWorld().removeObject(obj);
	    	ro.pickCamera();
	    	return;
		}
    }
}
