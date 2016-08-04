package com.marginallyclever.robotOverlord;

import java.util.ArrayList;

import javax.swing.JPanel;
import javax.vecmath.Vector3f;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelFactory;


public class ModelInWorld extends ObjectInWorld {
	/**
	 * 
	 */
	private static final long serialVersionUID = 180224086839215506L;
	
	protected String filename = null;
	protected transient Model model;
	protected transient ModelInWorldPanel modelPanel;
	
	// model render scale
	protected float scaleX=1, scaleY=1, scaleZ=1;
	
	
	public ModelInWorld() {}

	
	public String getFilename() {
		return filename;
	}
	
	
	public ArrayList<JPanel> getControlPanels(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getControlPanels(gui);
		if(list==null) list = new ArrayList<JPanel>();
		
		modelPanel = new ModelInWorldPanel(gui,this);
		list.add(modelPanel);

		return list;
	}


	public void setFilename(String newFilename) {
		// if the filename has changed, throw out the model so it will be reloaded.
		if( this.filename != newFilename ) {
			this.filename = newFilename;
			model=null;
		}
	}

	public void setScaleX(float arg0) {		scaleX=arg0;	}
	public void setScaleY(float arg0) {		scaleY=arg0;	}
	public void setScaleZ(float arg0) {		scaleZ=arg0;	}
	public float getScaleX() {		return scaleX;	}
	public float getScaleY() {		return scaleY;	}
	public float getScaleZ() {		return scaleZ;	}
	
	@Override
	public String getDisplayName() {
		return "Model";
	}
	
	
	public void render(GL2 gl2) {
		if( model==null && filename != null ) {
			model = ModelFactory.createModelFromFilename(filename);
		}
		if( model==null ) {
			// draw placeholder
			gl2.glColor3f(1, 0, 0);
			PrimitiveSolids.drawBox(gl2,  10.0f, 0.1f, 0.1f);
			gl2.glColor3f(0, 1, 0);
			PrimitiveSolids.drawBox(gl2,   0.1f,10.0f, 0.1f);
			gl2.glColor3f(0, 0, 1);
			gl2.glPushMatrix();
			gl2.glTranslatef(0, 0, -5);
			PrimitiveSolids.drawBox(gl2,   0.1f, 0.1f,10.0f);
			gl2.glPopMatrix();
			return;
		}

		Vector3f p = getPosition();
		
		material.render(gl2);
		gl2.glPushMatrix();
			gl2.glTranslatef(p.x, p.y, p.z);
			//gl2.glScaled(scaleX, scaleY, scaleZ);
			model.render(gl2);
		gl2.glPopMatrix();
	}
}
