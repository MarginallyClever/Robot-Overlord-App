package com.marginallyclever.robotOverlord.EvilMinionTool;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.PhysicalObject;
import com.marginallyclever.robotOverlord.EvilMinion.EvilMinionRobot;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelFactory;

public abstract class EvilMinionTool extends PhysicalObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5418173275880663460L;
	protected Model visibleShape = null;
	protected String shapeFile = null;
	protected EvilMinionRobot attachedTo=null;

	
	public void attachTo(EvilMinionRobot robot) {
		attachedTo=robot;
	}
	
	public EvilMinionRobot getAttachedTo() {
		return attachedTo;
	}
	
	public void render(GL2 gl2) {
		if( visibleShape==null && shapeFile!=null ) {
			visibleShape = ModelFactory.createModelFromFilename(shapeFile);
		}
		if(!visibleShape.isLoaded()) return;

		visibleShape.render(gl2);
	}
	
	public void updateGUI() {}
	
	public void update(float dt) {}
}
