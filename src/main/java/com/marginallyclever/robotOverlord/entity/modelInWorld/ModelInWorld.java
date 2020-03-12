package com.marginallyclever.robotOverlord.entity.modelInWorld;

import java.util.ArrayList;

import javax.swing.JPanel;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.log.Log;
import com.marginallyclever.robotOverlord.engine.model.Model;
import com.marginallyclever.robotOverlord.engine.model.ModelFactory;
import com.marginallyclever.robotOverlord.entity.material.Material;
import com.marginallyclever.robotOverlord.entity.physicalObject.PhysicalObject;


public class ModelInWorld extends PhysicalObject {
	protected String filename = "";
	protected transient Model model;
	protected transient ModelInWorldPanel modelPanel;
	protected Material material = new Material();
	
	// model adjustments during loading
	protected float scale;
	protected Vector3d originAdjust = new Vector3d();
	protected Vector3d rotationAdjust = new Vector3d();
	
	
	public ModelInWorld() {
		super();
		setName("Model");
		scale=1;
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
	

	@Override
	public ArrayList<JPanel> getContextPanels(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanels(gui);
		if(list==null) list = new ArrayList<JPanel>();
		
		modelPanel = new ModelInWorldPanel(gui,this);
		list.add(modelPanel);
		
		ArrayList<JPanel> list2 = material.getContextPanels(gui);
		list.add(list2.get(list2.size()-1));
		
		return list;
	}
    
	public String getModelFilename() {
		return filename;
	}

	/**
	 * sets the new model filename, which causes the model to be reloaded.
	 * @param newFilename
	 */
	public void setModelFilename(String newFilename) {
		// if the filename has changed, throw out the model so it will be reloaded.
		if( this.filename.equals(newFilename) ) return;
		
		try {
			model = ModelFactory.createModelFromFilename(newFilename);
			model.setScale(scale);
			model.adjustOrigin(originAdjust);
			model.adjustRotation(rotationAdjust);
			model.findBounds();
			// only change this after loading has completely succeeded.
			this.filename = newFilename;
		} catch (Exception e) {
			Log.error("Loading model failed: "+e.getLocalizedMessage());
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
	public void update(double dt) {
		super.update(dt);
	}

	/**
	 * obeys super.updatePoseWorld, Updates cuboid
	 */
	@Override
	public void updatePoseWorld() {
		super.updatePoseWorld();
		
		// set up the physical limits
		if(model != null) {
			cuboid.set(model.getCuboid());
			cuboid.setPoseWorld(poseWorld);
		}
	}
	
	@Override
	public void render(GL2 gl2) {	
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, pose);

			material.render(gl2);
			if( model==null ) {
				// draw placeholder
				PrimitiveSolids.drawBox(gl2, 1, 1, 1);
				PrimitiveSolids.drawStar(gl2,3.0);
			} else {
				model.render(gl2);
			}
		gl2.glPopMatrix();
		
		// draw children
		// physicalObject also calls applyMatrix() so this has to happen outside of the matrix push/pop
		super.render(gl2);
	}

	
	public Material getMaterial() {
		return material;
	}

	public Model getModel() {
		return model;
	}
}
