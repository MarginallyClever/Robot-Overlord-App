package com.marginallyclever.robotOverlord.engine.undoRedo.commands;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.undoRedo.actions.UndoableActionRemoveEntity;
import com.marginallyclever.robotOverlord.entity.Entity;

/**
 * Click this button to delete the active entity
 * @author Admin
 *
 */
public class UserCommandRemoveMe extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	protected Entity entity;
	JButton theButton;
	
	public UserCommandRemoveMe(RobotOverlord ro,Entity entity) {
		super();
		
		this.entity = entity;
		this.ro = ro;

		theButton = new JButton("Remove Me");
		theButton.getAccessibleContext().setAccessibleDescription("Remove me from the world.");
		theButton.addActionListener(this);
		this.setLayout(new BorderLayout());
		this.setBorder(new EmptyBorder(5,0,5,0));
		this.add(theButton,BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {
		ro.getUndoHelper().undoableEditHappened(new UndoableEditEvent(this,new UndoableActionRemoveEntity(ro,entity) ) );
	}
}
