package com.marginallyclever.robotOverlord.swingInterface.commands;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.swingInterface.actions.ActionEntityRemove;

/**
 * Click this button to delete the active entity
 * @author Admin
 *
 */
@Deprecated
public class CommandRemoveMe extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	protected Entity entity;
	JButton theButton;
	
	public CommandRemoveMe(RobotOverlord ro,Entity entity) {
		super();
		
		this.entity = entity;
		this.ro = ro;

		theButton = new JButton("Remove Me");
		theButton.getAccessibleContext().setAccessibleDescription("Remove me from the world.");
		theButton.addActionListener(this);
		this.setLayout(new BorderLayout());
		this.add(theButton,BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {
		ro.undoableEditHappened(new UndoableEditEvent(this,new ActionEntityRemove(ro,entity) ) );
	}
}
