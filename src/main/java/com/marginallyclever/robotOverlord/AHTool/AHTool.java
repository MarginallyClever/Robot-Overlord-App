package com.marginallyclever.robotOverlord.AHTool;

import com.jogamp.opengl.GL2;

import com.marginallyclever.robotOverlord.Model;
import com.marginallyclever.robotOverlord.PhysicalObject;
import com.marginallyclever.robotOverlord.AHRobot.AHRobot;

public abstract class AHTool extends PhysicalObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5418173275880663460L;
	protected Model visibleShape = null;
	protected String shapeFile = null;
	protected AHRobot attachedTo=null;

	
	public void attachTo(AHRobot robot) {
		attachedTo=robot;
	}
	
	public AHRobot getAttachedTo() {
		return attachedTo;
	}
	
	public void render(GL2 gl2) {
		if( visibleShape==null && shapeFile!=null ) {
			visibleShape = Model.loadModel(shapeFile);
		}
		if( visibleShape.isLoaded()==false ) return;

		visibleShape.render(gl2);
	}
	
	public void updateGUI() {}
	
	public void update(float dt) {}
}
