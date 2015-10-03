package com.marginallyclever.evilOverlord.ArmTool;

import javax.media.opengl.GL2;

import com.marginallyclever.evilOverlord.Model;
import com.marginallyclever.evilOverlord.ObjectInWorld;
import com.marginallyclever.evilOverlord.Arm5.Arm5Robot;

public abstract class ArmTool extends ObjectInWorld {
	protected Model visibleShape = null;
	protected String shapeFile = null;
	protected Arm5Robot attachedTo=null;

	
	public void attachTo(Arm5Robot robot) {
		attachedTo=robot;
	}
	
	public Arm5Robot getAttachedTo() {
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
