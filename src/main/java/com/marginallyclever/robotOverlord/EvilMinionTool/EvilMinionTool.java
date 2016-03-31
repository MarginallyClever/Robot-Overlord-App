package com.marginallyclever.robotOverlord.EvilMinionTool;

import javax.media.opengl.GL2;

import com.marginallyclever.robotOverlord.Model;
import com.marginallyclever.robotOverlord.PhysicalObject;
import com.marginallyclever.robotOverlord.EvilMinion.EvilMinionRobot;

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
			visibleShape = Model.loadModel(shapeFile);
		}
		if( visibleShape.isLoaded()==false ) return;

		visibleShape.render(gl2);
	}
	
	public void updateGUI() {}
	
	public void update(float dt) {}
}
