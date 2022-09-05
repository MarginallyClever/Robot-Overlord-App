package com.marginallyclever.robotoverlord.swinginterface.actions;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.undoableedits.RemoveEdit;

/**
 * Click this button to delete the active entity
 * @author Admin
 *
 */
@Deprecated
public class RemoveMeAction extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	protected Entity entity;
	JButton theButton;
	
	public RemoveMeAction(RobotOverlord ro,Entity entity) {
		super();
		
		this.entity = entity;
		this.ro = ro;

		theButton = new JButton("Remove Me");
		theButton.getAccessibleContext().setAccessibleDescription("Remove me from the world.");
		theButton.addActionListener(this);
		this.setLayout(new BorderLayout());
		this.add(theButton,BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		UndoSystem.addEvent(this,new RemoveEdit(ro,entity));
	}
}
