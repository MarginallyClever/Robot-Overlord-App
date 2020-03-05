package com.marginallyclever.robotOverlord.entity.physicalObject;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PanelHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectVector3d;
import com.marginallyclever.robotOverlord.entity.robot.sixi2.Sixi2.ControlMode;

/**
 * The user interface for an Entity.
 * @author Dan Royer
 *
 */
public class PhysicalObjectPanel extends JPanel implements ChangeListener, ItemListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private PhysicalObject entity;
	private transient UserCommandSelectVector3d setPosition;
	private transient UserCommandSelectVector3d setRotation;
	public JCheckBox drawBoundingBox;
	public JCheckBox drawLocalOrigin;
	public JCheckBox drawConnectionToChildren;

	/**
	 * @param ro the application instance
	 * @param entity The entity controlled by this panel
	 */
	public PhysicalObjectPanel(RobotOverlord ro,PhysicalObject entity) {
		super();
		
		this.entity = entity;
		this.setName("Physical Object");
		this.setLayout(new GridBagLayout());
		this.setBorder(new EmptyBorder(0,0,0,0));

		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();
		
		this.add(setPosition = new UserCommandSelectVector3d(ro,"position",entity.getPosition()),con1);
		setPosition.addChangeListener(this);

		con1.gridy++;
		Vector3d temp = new Vector3d();
		entity.getRotation(temp);
		temp.scale(180/Math.PI);
		this.add(setRotation = new UserCommandSelectVector3d(ro,"rotation",temp),con1);
		setRotation.addChangeListener(this);
		
		con1.gridy++;
		this.add(drawBoundingBox=new JCheckBox(),con1);
		drawBoundingBox.setText("Draw Bounding Box");
		drawBoundingBox.addItemListener(this);
		drawBoundingBox.setSelected(entity.isShouldDrawBoundingBox());
		
		con1.gridy++;
		this.add(drawLocalOrigin=new JCheckBox(),con1);
		drawLocalOrigin.setText("Draw Local Origin");
		drawLocalOrigin.addItemListener(this);
		drawLocalOrigin.setSelected(entity.isShouldDrawLocalOrigin());
		
		con1.gridy++;
		this.add(drawConnectionToChildren=new JCheckBox(),con1);
		drawConnectionToChildren.setText("Draw Connection To Children");
		drawConnectionToChildren.addItemListener(this);
		drawConnectionToChildren.setSelected(entity.isShouldDrawConnectionToChildren());
		
		PanelHelper.ExpandLastChild(this, con1);
	}
	
	
	/**
	 * Call by an Entity when it's details change so that they are reflected on the panel.
	 * This might be better as a listener pattern.
	 */
	public void updateFields() {
		setPosition.setValue(entity.getPosition());
		Vector3d temp = new Vector3d();
		entity.getRotation(temp);
		temp.scale(180.0/Math.PI);
		setRotation.setValue(temp);
	}
	
	
	/**
	 * Called by the UI when the user presses buttons on the panel.
	 * @param e the {@link ChangeEvent} details
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		Object subject = e.getSource();
		
		if( subject==setPosition ) {
			Vector3d pos = entity.getPosition();
			Vector3d newPos = setPosition.getValue();
			if(!newPos.epsilonEquals(pos, 1e-4)) {
				entity.setPosition(newPos);
			}
		}
		if( subject==setRotation ) {
			Vector3d temp = new Vector3d();
			entity.getRotation(temp);
			Vector3d newPos = setRotation.getValue();
			newPos.scale(Math.PI/180.0);
			if(!newPos.epsilonEquals(temp, 1e-4)) {
				entity.setRotation(MatrixHelper.eulerToMatrix(newPos));
			}
		}
	}


	@Override
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		
		if(source==drawBoundingBox) {
			boolean newState = entity.isShouldDrawBoundingBox() ? false : true;
			entity.setShouldDrawBoundingBox(newState);
		}
		if(source==drawLocalOrigin) {
			boolean newState = entity.isShouldDrawLocalOrigin() ? false : true;
			entity.setShouldDrawLocalOrigin(newState);
		}
		if(source==drawConnectionToChildren) {
			boolean newState = entity.isShouldDrawConnectionToChildren() ? false : true;
			entity.setShouldDrawConnectionToChildren(newState);
		}
		
	}
}
