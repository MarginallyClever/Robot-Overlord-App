package com.marginallyclever.robotOverlord.pTTPTPRobot.tool;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.Material;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelFactory;
import com.marginallyclever.robotOverlord.pTTPTPRobot.PTTPTPRobot;
import com.marginallyclever.robotOverlord.physicalObject.PhysicalObject;

public abstract class PTTPTPTool extends PhysicalObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5418173275880663460L;
	protected Model visibleShape = null;
	protected String shapeFile = null;
	protected PTTPTPRobot attachedTo=null;
	private Material material = null;


	public PTTPTPTool() {
		super();
		material = new Material();
	}
	
	public void attachTo(PTTPTPRobot robot) {
		attachedTo=robot;
	}
	
	public PTTPTPRobot getAttachedTo() {
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
