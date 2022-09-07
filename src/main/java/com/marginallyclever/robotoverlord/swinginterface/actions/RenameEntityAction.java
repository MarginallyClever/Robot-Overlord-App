package com.marginallyclever.robotoverlord.swinginterface.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import com.marginallyclever.robotoverlord.swinginterface.undoableedits.RenameEdit;

/**
 *  
 * @author Dan Royer
 *
 */
public class RenameEntityAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RobotOverlord ro;
	
	public RenameEntityAction(RobotOverlord ro) {
		super(Translator.get("Rename Entity"));
        putValue(AbstractAction.SHORT_DESCRIPTION, Translator.get("Rename the selected entity, if permitted."));
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
			UndoSystem.addEvent(this,new RenameEdit(ro,e,newName));
		}
	}
}
