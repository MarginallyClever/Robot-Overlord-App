package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.EditorAction;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.EntityRenameEdit;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 *  
 * @author Dan Royer
 *
 */
public class EntityRenameAction extends AbstractAction implements EditorAction {
	private final RobotOverlord ro;
	
	public EntityRenameAction(String name, RobotOverlord ro) {
		super(name);
		this.ro = ro;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		List<Entity> entityList = ro.getSelectedEntities();
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
			UndoSystem.addEvent(this,new EntityRenameEdit(ro,e,newName));
		}
	}

	@Override
	public void updateEnableStatus() {
		setEnabled(ro.getSelectedEntities().size()==1);
	}
}
