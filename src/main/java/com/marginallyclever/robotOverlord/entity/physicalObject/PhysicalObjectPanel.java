package com.marginallyclever.robotOverlord.entity.physicalObject;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PanelHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectBoolean;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectVector3d;

/**
 * The user interface for an Entity.
 * @author Dan Royer
 *
 */
public class PhysicalObjectPanel extends JPanel implements ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private PhysicalObject entity;
	
	private transient UserCommandSelectVector3d setPosition;
	private transient UserCommandSelectVector3d setRotation;
	public transient UserCommandSelectBoolean drawBoundingBox;
	public transient UserCommandSelectBoolean drawLocalOrigin;
	public transient UserCommandSelectBoolean drawConnectionToChildren;

	/**
	 * @param ro the application instance
	 * @param entity The entity controlled by this panel
	 */
	public PhysicalObjectPanel(RobotOverlord ro,PhysicalObject entity) {
		super();
		
		this.entity = entity;
		this.setName("Physical Object");
		this.setBorder(new EmptyBorder(5,5,5,5));
		this.setLayout(new GridBagLayout());

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
		this.add(drawBoundingBox=new UserCommandSelectBoolean(ro,"Draw Bounding Box",entity.shouldDrawBoundingBox()),con1);
		drawBoundingBox.addChangeListener(this);
		
		con1.gridy++;
		this.add(drawLocalOrigin=new UserCommandSelectBoolean(ro,"Draw Local Origin",entity.shouldDrawLocalOrigin()),con1);
		drawLocalOrigin.addChangeListener(this);
		
		con1.gridy++;
		this.add(drawConnectionToChildren=new UserCommandSelectBoolean(ro,"Draw Connection To Children",entity.shouldDrawConnectionToChildren()),con1);
		drawConnectionToChildren.addChangeListener(this);
		
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
		if(subject==drawBoundingBox) {
			entity.setDrawBoundingBox(drawBoundingBox.getValue());
		}
		if(subject==drawLocalOrigin) {
			entity.setDrawLocalOrigin(drawLocalOrigin.getValue());
		}
		if(subject==drawConnectionToChildren) {
			entity.setDrawConnectionToChildren(drawConnectionToChildren.getValue());
		}
		
	}
}
