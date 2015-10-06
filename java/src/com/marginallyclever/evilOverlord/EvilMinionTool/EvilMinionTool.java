package com.marginallyclever.evilOverlord.EvilMinionTool;

import javax.media.opengl.GL2;

import com.marginallyclever.evilOverlord.Model;
import com.marginallyclever.evilOverlord.PhysicalObject;
import com.marginallyclever.evilOverlord.EvilMinion.EvilMinionRobot;

public abstract class EvilMinionTool extends PhysicalObject {
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
