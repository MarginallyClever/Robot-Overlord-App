package com.marginallyclever.robotOverlord.thor.tool;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.PhysicalObject;
import com.marginallyclever.robotOverlord.thor.ThorRobot;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelFactory;

public abstract class ThorTool extends PhysicalObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5418173275880663460L;
	protected Model visibleShape = null;
	protected String shapeFile = null;
	protected ThorRobot attachedTo=null;

	
	public void attachTo(ThorRobot robot) {
		attachedTo=robot;
	}
	
	public ThorRobot getAttachedTo() {
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

		visibleShape.render(gl2);
	}
	
	public void updateGUI() {}
	
	public void update(float dt) {}
}
