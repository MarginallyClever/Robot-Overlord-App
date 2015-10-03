package com.marginallyclever.evilOverlord;

import javax.media.opengl.GL2;


public class ModelInWorld extends ObjectInWorld {
	protected Model model;
	
	public ModelInWorld() {}
	
	
	public void loadModel(String filename) {
		model = Model.loadModel(filename);
	}
	
	public void render(GL2 gl2) {
		if(model==null) return;
		gl2.glPushMatrix();
			gl2.glTranslatef(position.x, position.y, position.z);
			model.render(gl2);
		gl2.glPopMatrix();
	}
}
