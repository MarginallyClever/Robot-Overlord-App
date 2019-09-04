package com.marginallyclever.robotOverlord.modelInWorld;

import java.util.ArrayList;

import javax.swing.JPanel;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.material.Material;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelFactory;
import com.marginallyclever.robotOverlord.physicalObject.PhysicalObject;


public class ModelInWorld extends PhysicalObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 180224086839215506L;
	
	protected String filename = null;
	protected transient Model model;
	protected transient ModelInWorldPanel modelPanel;
	protected Material material;
	
	// model render scale
	protected float scale=1;
	// model adjusted origin
	protected Vector3d originAdjust;
	protected Vector3d rotationAdjust;
	
	
	public ModelInWorld() {
		super();
		originAdjust = new Vector3d();
		rotationAdjust = new Vector3d();
		material = new Material();
		setDisplayName("Model");
	}

	
	public String getFilename() {
		return filename;
	}
	
	
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		if(list==null) list = new ArrayList<JPanel>();
		
		modelPanel = new ModelInWorldPanel(gui,this);
		list.add(modelPanel);
		
		ArrayList<JPanel> list2 = material.getContextPanel(gui);
		list.add(list2.get(list2.size()-1));
		
		return list;
	}


	public void setFilename(String newFilename) {
		// if the filename has changed, throw out the model so it will be reloaded.
		if( this.filename != newFilename ) {
			this.filename = newFilename;
			model=null;
		}
	}

	public void setScale(float arg0) {
		scale=arg0;
		if(model!=null) {
			model.setScale(arg0);
		}
	}
	
	public float getScale() {
		return scale;
	}

	public void adjustOrigin(double x,double y,double z) {
		originAdjust.x=x;
		originAdjust.y=y;
		originAdjust.z=z;
		if(model!=null) model.adjustOrigin(originAdjust);
	}

	public void adjustOrigin(Vector3d arg0) {
		originAdjust.set(arg0);;
		if(model!=null) model.adjustOrigin(originAdjust);
	}
	
	public Vector3d getAdjustOrigin() {
		return new Vector3d(originAdjust);
	}

	public void adjustRotation(double x,double y,double z) {
		rotationAdjust.x=x;
		rotationAdjust.y=y;
		rotationAdjust.z=z;
		if(model!=null) model.adjustRotation(rotationAdjust);
	}

	public void adjustRotation(Vector3d arg0) {
		rotationAdjust.set(arg0);;
		if(model!=null) model.adjustRotation(rotationAdjust);
	}
	
	public Vector3d getAdjustRotation() {
		return new Vector3d(rotationAdjust);
	}
	
	
	public void render(GL2 gl2) {
		if( model==null && filename != null ) {
			try {
				model = ModelFactory.createModelFromFilename(filename);
				model.setScale(scale);
				model.adjustOrigin(originAdjust);
				model.adjustRotation(rotationAdjust);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		gl2.glPushMatrix();

		MatrixHelper.applyMatrix(gl2, this.getMatrix());
		
		// TODO: this should probably be an option that can be toggled.
		// It is here to fix scaling of the entire model.  It won't work when the model is scaled unevenly.
		gl2.glEnable(GL2.GL_NORMALIZE);

		if( model==null ) {
			// draw placeholder
			PrimitiveSolids.drawStar(gl2,new Vector3d(0,0,0),10f);
		} else {
			material.render(gl2);
			model.render(gl2);
		}
		
		gl2.glPopMatrix();
	}
}
