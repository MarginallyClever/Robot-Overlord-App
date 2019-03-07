package com.marginallyclever.robotOverlord.sixi2Robot.tool;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.sixi2Robot.Sixi2Robot;
import com.marginallyclever.robotOverlord.material.Material;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelFactory;
import com.marginallyclever.robotOverlord.physicalObject.PhysicalObject;

public abstract class Sixi2Tool extends PhysicalObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5418173275880663460L;
	protected Model visibleShape = null;
	protected String shapeFile = null;
	protected Sixi2Robot attachedTo=null;
	protected Material material = null;
		
	public Sixi2Tool() {
		super();
		material = new Material();
	}
	
	public void attachTo(Sixi2Robot robot) {
		attachedTo=robot;
	}
	
	public Sixi2Robot getAttachedTo() {
		return attachedTo;
	}
	
	public void render(GL2 gl2) {
		if( visibleShape==null && shapeFile!=null ) {
			try {
				visibleShape = ModelFactory.createModelFromFilename(shapeFile);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		if(!visibleShape.isLoaded()) return;

		material.render(gl2);
		visibleShape.render(gl2);
	}
	
	public void updateGUI() {}
	
	public void update(float dt) {}
}
