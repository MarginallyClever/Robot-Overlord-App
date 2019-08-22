package com.marginallyclever.robotOverlord.physicalObject;

import java.util.ArrayList;

import javax.swing.JPanel;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.EntityControlPanel;

public abstract class PhysicalObject extends Entity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1804941485489224976L;

	private Matrix4d pose;
	private transient PhysicalObjectControlPanel physicalObjectControlPanel;
	
	public PhysicalObject() {
		super();
		
		pose = new Matrix4d();
		pose.setIdentity();
	}
	
	
	/**
	 * Get the {@link EntityControlPanel} for this class' superclass, then the physicalObjectControlPanel for this class, and so on.
	 * 
	 * @param gui the main application instance.
	 * @return the list of physicalObjectControlPanels 
	 */
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		if(list==null) list = new ArrayList<JPanel>();

		physicalObjectControlPanel = new PhysicalObjectControlPanel(gui,this);
		list.add(physicalObjectControlPanel);

		return list;
	}

	// set up the future motion state of the physical object
	public void prepareMove(double dt) {}
	
	// apply the future motion state - make the future into the present
	public void finalizeMove() {}

	public Vector3d getPosition() {
		return new Vector3d(pose.m03,pose.m13,pose.m23);
	}

	public void getRotation(Vector3d arg0) {
		Matrix3d temp = new Matrix3d();
		pose.get(temp);
		arg0.set(MatrixHelper.matrixToEuler(temp));
	}
	
	public void setPosition(Vector3d pos) {
		pose.setTranslation(pos);
		if(physicalObjectControlPanel!=null) {
			physicalObjectControlPanel.updateFields();
		}
	}
	
	public Matrix4d getPose() {
		return pose;
	}
	public void setPose(Matrix4d arg0) {
		pose.set(arg0);
		if(physicalObjectControlPanel!=null) {
			physicalObjectControlPanel.updateFields();	
		}
	}
	public void setRotation(Matrix3d arg0) {
		Vector3d trans = getPosition();
		pose.set(arg0);
		pose.setTranslation(trans);
		if(physicalObjectControlPanel!=null) {
			physicalObjectControlPanel.updateFields();	
		}
	}
	public void getRotation(Matrix4d arg0) {
		arg0.set(pose);
		arg0.setTranslation(new Vector3d(0,0,0));
	}
}
