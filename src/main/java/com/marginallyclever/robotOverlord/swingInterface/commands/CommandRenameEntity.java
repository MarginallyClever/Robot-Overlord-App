package com.marginallyclever.robotOverlord.swingInterface.commands;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.swingInterface.actions.ActionEntityRename;
import com.marginallyclever.robotOverlord.swingInterface.translator.Translator;

/**
 *  
 * @author Dan Royer
 *
 */
public class CommandRenameEntity extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public CommandRenameEntity(RobotOverlord ro) {
		super(Translator.get("Rename Entity"));
        putValue(AbstractAction.SHORT_DESCRIPTION, Translator.get("Rename the selected entity, if permitted."));
		this.ro = ro;
	}

	public void actionPerformed(ActionEvent e) {
		Entity entity = ro.getPickedEntity();

		String newName = (String)JOptionPane.showInputDialog(ro.getMainFrame(),"New name:","Rename Entity",JOptionPane.PLAIN_MESSAGE,null,null,entity.getName());
		if( newName!=null && !newName.equals(entity.getName()) ) {
			ro.undoableEditHappened(new UndoableEditEvent(this,new ActionEntityRename(entity,newName) ) );
		}
	}
}
