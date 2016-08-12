package com.marginallyclever.robotOverlord.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.commands.CommandRemoveEntity;

/**
 * Click this button to delete the active entity
 * @author Admin
 *
 */
public class ActionRemoveMe extends JButton implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	protected Entity entity;
	
	public ActionRemoveMe(RobotOverlord ro,Entity entity) {
		super("Remove Me");
		this.entity = entity;
        getAccessibleContext().setAccessibleDescription("Remove an entity from the world.");
		this.ro = ro;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		ro.getUndoHelper().undoableEditHappened(new UndoableEditEvent(this,new CommandRemoveEntity(ro,entity) ) );
	}
}
