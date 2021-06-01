package com.marginallyclever.robotOverlord.swingInterface.commands;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.RobotOverlord;
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

	public void actionPerformed(ActionEvent event) {
		ArrayList<Entity> entityList = ro.getSelectedEntities();
		if(entityList.size()!=1) {
			Log.error("Rename more than one entity at the same time?!");
			return;
		}
		Entity e = entityList.get(0);

		String newName = (String)JOptionPane.showInputDialog(
				ro.getMainFrame(),
				"New name:",
				"Rename Entity",
				JOptionPane.PLAIN_MESSAGE,null,null,e.getName());
		if( newName!=null && !newName.equals(e.getName()) ) {
			ro.undoableEditHappened(new UndoableEditEvent(this,new ActionEntityRename(ro,e,newName) ) );
		}
	}
}
