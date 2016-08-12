package com.marginallyclever.robotOverlord;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Vector3f;

import com.marginallyclever.robotOverlord.actions.ActionRemoveMe;
import com.marginallyclever.robotOverlord.actions.ActionSelectString;
import com.marginallyclever.robotOverlord.actions.ActionSelectVector3f;

public class EntityPanel extends JPanel implements ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Entity entity;
	private transient ActionSelectVector3f setPosition;
	private transient ActionSelectString setName;
	
	public EntityPanel(RobotOverlord ro,Entity entity) {
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
		
		CollapsiblePanel oiwPanel = new CollapsiblePanel("Entity");
		this.add(oiwPanel,c);
		JPanel contents = oiwPanel.getContentPane();
		
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.CENTER;

		contents.add(setName=new ActionSelectString(ro,"Name",entity.getDisplayName()), con1);
		con1.gridy++;
		setName.addChangeListener(this);
		
		if(ro.getWorld().hasEntity(entity)) {
			contents.add(new ActionRemoveMe(ro,entity),c);
			con1.gridy++;
		}
		
		contents.add(setPosition = new ActionSelectVector3f(ro,"Position",entity.getPosition()),con1);
		con1.gridy++;
		setPosition.addChangeListener(this);
	}
	
	
	public void updateFields() {
		Vector3f pos = entity.getPosition();
		setPosition.setValue(pos);
	}
	
	
	@Override
	public void stateChanged(ChangeEvent e) {
		Object subject = e.getSource();
		
		if( subject==setPosition ) {
			Vector3f pos = entity.getPosition();
			Vector3f newPos = setPosition.getValue();
			if(!newPos.equals(pos)) {
				entity.setPosition(newPos);
			}
		} else if( subject==setName ) {
			String name = entity.getDisplayName();
			String newName = setName.getValue();
			if(!newName.equals(name)) {
				entity.setDisplayName(newName);
			}
		}
	}
}
