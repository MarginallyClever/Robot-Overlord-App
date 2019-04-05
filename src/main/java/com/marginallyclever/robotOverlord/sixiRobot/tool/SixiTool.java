package com.marginallyclever.robotOverlord.sixiRobot.tool;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.sixiRobot.SixiRobot;
import com.marginallyclever.robotOverlord.material.Material;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelFactory;
import com.marginallyclever.robotOverlord.physicalObject.PhysicalObject;

public abstract class SixiTool extends PhysicalObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5418173275880663460L;
	protected Model visibleShape = null;
	protected String shapeFile = null;
	protected SixiRobot attachedTo=null;
	protected Material material = null;
		
	public SixiTool() {
		super();
		material = new Material();
	}
	
	public void attachTo(SixiRobot robot) {
		attachedTo=robot;
	}
	
	public SixiRobot getAttachedTo() {
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
	
	public void update(double dt) {}
}
