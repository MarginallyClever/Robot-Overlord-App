package com.marginallyclever.robotOverlord.entity.modelEntity;


import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.engine.log.Log;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.Vector3dEntity;
import com.marginallyclever.robotOverlord.entity.materialEntity.MaterialEntity;
import com.marginallyclever.robotOverlord.entity.physicalEntity.PhysicalEntity;


public class ModelEntity extends PhysicalEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5888928381757734702L;
	
	protected transient Model model;
	protected transient ModelEntityPanel modelPanel;

	protected StringEntity filename = new StringEntity("Filename","");
	protected MaterialEntity material = new MaterialEntity();
	
	// model adjustments
	protected DoubleEntity scale = new DoubleEntity("Scale",1.0);
	protected Vector3dEntity rotationAdjust = new Vector3dEntity("+/- rotation");
	protected Vector3dEntity originAdjust = new Vector3dEntity("+/- origin");
	
	
	public ModelEntity() {
		super();
		setName("Model");
		addChild(filename);

		addChild(rotationAdjust);
		addChild(originAdjust);
		addChild(scale);
		
		addChild(material);
	}


	public void set(ModelEntity b) {
		super.set(b);
		scale.set(b.scale.get());
		
		filename.set(b.filename.get());
		model = b.model;
		material.set(b.material);
		originAdjust.set(b.originAdjust.get());
		rotationAdjust.set(b.rotationAdjust.get());
	}
    
	public String getModelFilename() {
		return filename.get();
	}

	/**
	 * sets the new model filename, which causes the model to be reloaded.
	 * @param newFilename
	 */
	public void setModelFilename(String newFilename) {
		// if the filename has changed, throw out the model so it will be reloaded.
		if( this.filename.get().equals(newFilename) ) return;
		
		try {
			model = ModelFactory.createModelFromFilename(newFilename);
			model.adjustScale(scale.get());
			model.adjustOrigin(originAdjust.get());
			model.adjustRotation(rotationAdjust.get());
			model.findBounds();
			// only change this after loading has completely succeeded.
			this.filename.set(newFilename);
		} catch (Exception e) {
			Log.error("Loading model failed: "+e.getLocalizedMessage());
		}
	}

	public void setModelScale(double arg0) {
		scale.set(arg0);
		if(model!=null) {
			model.adjustScale(arg0);
		}
	}
	
	public double getModelScale() {
		return scale.get();
	}

	public void setModelOrigin(double x,double y,double z) {
		originAdjust.set(x,y,z);
		if(model!=null) model.adjustOrigin(originAdjust.get());
	}

	public void setModelOrigin(Vector3d arg0) {
		originAdjust.set(arg0);
		if(model!=null) model.adjustOrigin(originAdjust.get());
	}
	
	public Vector3d getModelOrigin() {
		return new Vector3d(originAdjust.get());
	}

	public void setModelRotation(double x,double y,double z) {
		rotationAdjust.set(x,y,z);
		if(model!=null) model.adjustRotation(rotationAdjust.get());
	}

	public void setModelRotation(Vector3d arg0) {
		rotationAdjust.set(arg0);
		if(model!=null) model.adjustRotation(rotationAdjust.get());
	}
	
	public Vector3d getModelRotation() {
		return new Vector3d(rotationAdjust.get());
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
			MatrixHelper.applyMatrix(gl2, pose.get());

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

	
	public MaterialEntity getMaterial() {
		return material;
	}

	public Model getModel() {
		return model;
	}
}
