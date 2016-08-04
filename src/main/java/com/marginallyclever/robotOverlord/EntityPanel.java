package com.marginallyclever.robotOverlord;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Vector3f;

public class EntityPanel extends JPanel implements ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Entity entity;
	private transient ActionSelectNumber fieldX,fieldY,fieldZ;
	
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

		CollapsiblePanel oiwPanel = new CollapsiblePanel("Move");
		this.add(oiwPanel,c);
		JPanel contents = oiwPanel.getContentPane();
		
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.CENTER;
		
		contents.add(fieldX = new ActionSelectNumber(ro,"X",entity.getPosition().x),con1);
		con1.gridy++;
		contents.add(fieldY = new ActionSelectNumber(ro,"Y",entity.getPosition().x),con1);
		con1.gridy++;
		contents.add(fieldZ = new ActionSelectNumber(ro,"Z",entity.getPosition().x),con1);
		con1.gridy++;

		fieldX.addChangeListener(this);
		fieldY.addChangeListener(this);
		fieldZ.addChangeListener(this);
	}
	

	public void updateFields() {
		fieldX.setValue(entity.getPosition().x);
		fieldY.setValue(entity.getPosition().y);
		fieldZ.setValue(entity.getPosition().z);
	}


	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		Vector3f pos = entity.getPosition();
		pos.x = fieldX.getValue();
		pos.y = fieldY.getValue();
		pos.z = fieldZ.getValue();
		entity.setPosition(pos);
	}
}
