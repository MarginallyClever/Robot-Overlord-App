package com.marginallyclever.robotOverlord.entity;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.convenience.PanelHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandRemoveMe;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectString;

/**
 * The user interface for an {@link Entity}.
 * @author Dan Royer
 *
 */
public class EntityPanel extends JPanel implements ChangeListener {
	private static final long serialVersionUID = 1L;
	private Entity entity;
	private transient UserCommandSelectString setName;

	/**
	 * @param ro the application instance
	 * @param entity The entity controlled by this panel
	 */
	public EntityPanel(RobotOverlord ro,Entity entity) {
		super();
		
		this.entity = entity;

		this.setName("Entity");
		this.setLayout(new GridBagLayout());
		this.setBorder(new EmptyBorder(0,0,0,0));

		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();

		if(ro.getWorld().hasEntity(entity)) {
			this.add(new UserCommandRemoveMe(ro,entity),con1);
			con1.gridy++;
		}

		con1.weighty=1;  // last item gets weight 1.
		this.add(setName=new UserCommandSelectString(ro,"name",entity.getName()), con1);
		con1.gridy++;
		setName.addChangeListener(this);
	}
	
	
	/**
	 * Call by an {@link Entity} when it's details change so that they are reflected on the panel.
	 * This might be better as a listener pattern.
	 */
	public void updateFields() {
	}
	
	
	/**
	 * Called by the UI when the user presses buttons on the panel.
	 * @param e the {@link ChangeEvent} details
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		Object subject = e.getSource();
		
		if( subject==setName ) {
			String name = entity.getName();
			String newName = setName.getValue();
			if(!newName.equals(name)) {
				entity.setName(newName);
			}
		}
	}
}
