package com.marginallyclever.robotOverlord.arm5.tool;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.arm5.EvilMinionRobot;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelFactory;
import com.marginallyclever.robotOverlord.physicalObject.PhysicalObject;

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
