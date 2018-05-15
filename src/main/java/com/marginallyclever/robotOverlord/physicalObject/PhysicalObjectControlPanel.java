package com.marginallyclever.robotOverlord.physicalObject;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Vector3f;

import com.marginallyclever.robotOverlord.CollapsiblePanel;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectVector3f;

/**
 * The user interface for an {@link Entity}.
 * @author Dan Royer
 *
 */
public class PhysicalObjectControlPanel extends JPanel implements ChangeListener {
	private static final long serialVersionUID = 1L;
	private PhysicalObject entity;
	private transient UserCommandSelectVector3f setPosition;

	/**
	 * @param ro the application instance
	 * @param entity The entity controlled by this panel
	 */
	public PhysicalObjectControlPanel(RobotOverlord ro,PhysicalObject entity) {
		super();
		
		this.entity = entity;

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.weighty=1;
		c.anchor=GridBagConstraints.NORTHWEST;
		c.fill=GridBagConstraints.HORIZONTAL;
		
		CollapsiblePanel oiwPanel = new CollapsiblePanel("Physical Object");
		this.add(oiwPanel,c);
		JPanel contents = oiwPanel.getContentPane();
		
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.CENTER;
		
		contents.add(setPosition = new UserCommandSelectVector3f(ro,"position",entity.getPosition()),con1);
		con1.gridy++;
		setPosition.addChangeListener(this);
	}
	
	
	/**
	 * Call by an {@link Entity} when it's details change so that they are reflected on the panel.
	 * This might be better as a listener pattern.
	 */
	public void updateFields() {
		Vector3f pos = entity.getPosition();
		setPosition.setValue(pos);
	}
	
	
	/**
	 * Called by the UI when the user presses buttons on the panel.
	 * @param e the {@link ChangeEvent} details
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		Object subject = e.getSource();
		
		if( subject==setPosition ) {
			Vector3f pos = entity.getPosition();
			Vector3f newPos = setPosition.getValue();
			if(!newPos.equals(pos)) {
				entity.setPosition(newPos);
			}
		}
	}
}
