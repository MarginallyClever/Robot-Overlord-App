package com.marginallyclever.robotOverlord.entity.modelInWorld;

import java.util.ArrayList;

import javax.swing.JPanel;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.model.Model;
import com.marginallyclever.robotOverlord.engine.model.ModelFactory;
import com.marginallyclever.robotOverlord.entity.material.Material;
import com.marginallyclever.robotOverlord.entity.physicalObject.PhysicalObject;


public class ModelInWorld extends PhysicalObject {
	protected String filename = null;
	protected transient Model model;
	protected transient ModelInWorldPanel modelPanel;
	protected Material material;
	
	// model adjustments during loading
	protected float scale=1;
	protected Vector3d originAdjust;
	protected Vector3d rotationAdjust;
	
	
	public ModelInWorld() {
		super();
		originAdjust = new Vector3d();
		rotationAdjust = new Vector3d();
		material = new Material();
		setName("Model");
	}

	public void set(ModelInWorld b) {
		super.set(b);
		filename = b.filename;
		model = b.model;
		material.set(b.material);

		scale = b.scale;
		originAdjust.set(b.originAdjust);
		rotationAdjust.set(b.rotationAdjust);
	}
	
	public String getFilename() {
		return filename;
	}
	

	@Override
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
			
			try {
				model = ModelFactory.createModelFromFilename(newFilename);
				model.setScale(scale);
				model.adjustOrigin(originAdjust);
				model.adjustRotation(rotationAdjust);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void setModelScale(float arg0) {
		scale=arg0;
		if(model!=null) {
			model.setScale(arg0);
		}
	}
	
	public float getModelScale() {
		return scale;
	}

	public void setModelOrigin(double x,double y,double z) {
		originAdjust.x=x;
		originAdjust.y=y;
		originAdjust.z=z;
		if(model!=null) model.adjustOrigin(originAdjust);
	}

	public void setModelOrigin(Vector3d arg0) {
		originAdjust.set(arg0);
		if(model!=null) model.adjustOrigin(originAdjust);
	}
	
	public Vector3d getModelOrigin() {
		return new Vector3d(originAdjust);
	}

	public void setModelRotation(double x,double y,double z) {
		rotationAdjust.x=x;
		rotationAdjust.y=y;
		rotationAdjust.z=z;
		if(model!=null) model.adjustRotation(rotationAdjust);
	}

	public void setModelRotation(Vector3d arg0) {
		rotationAdjust.set(arg0);
		if(model!=null) model.adjustRotation(rotationAdjust);
	}
	
	public Vector3d getModelRotation() {
		return new Vector3d(rotationAdjust);
	}
	

	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();

			MatrixHelper.applyMatrix(gl2, this.getPose());
			
			// TODO: this should probably be an option that can be toggled.
			// It is here to fix scaling of the entire model.  It won't work when the model is scaled unevenly.
			gl2.glEnable(GL2.GL_NORMALIZE);
	
			material.render(gl2);
			
			if( model==null ) {
				// draw placeholder
				PrimitiveSolids.drawBox(gl2, 1, 1, 1);
				PrimitiveSolids.drawStar(gl2,new Vector3d(0,0,0),3f);
			} else {
				model.render(gl2);
			}
			
		gl2.glPopMatrix();
		
		super.render(gl2);
	}

	
	public Material getMaterial() {
		return material;
	}

	public Model getModel() {
		return model;
	}
}
