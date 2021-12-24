package com.marginallyclever.robotOverlord.physics.ode;

import javax.vecmath.Vector3d;

import org.ode4j.ode.DBox;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DPlane;
import org.ode4j.ode.DSphere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.uiExposedTypes.MaterialEntity;

public class ODEPhysicsEntity extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3498630114673883847L;
	private static final Logger logger = LoggerFactory.getLogger(ODEPhysicsEntity.class);
	private DGeom geom;
	private MaterialEntity mat = new MaterialEntity();
	
	public ODEPhysicsEntity(DGeom g) {
		super(ODEPhysicsEntity.class.getSimpleName());
		addChild(mat);
		geom=g;
	}
	
	public DGeom getGeom() {
		return geom;
	}
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
		
		gl2.glPushMatrix();
			gl2.glDisable(GL2.GL_TEXTURE_2D);
			mat.render(gl2);
			MatrixHelper.applyMatrix(gl2, ODEPhysicsEngine.getMatrix4d(geom));
		
			if(geom instanceof DBox) drawBox(gl2);
			else if(geom instanceof DSphere) drawSphere(gl2);
			else if(geom instanceof DPlane) drawPlane(gl2);
			else logger.error("render() unknown type "+geom.getClass().getName());
		
		gl2.glPopMatrix();
	}

	private void drawPlane(GL2 gl2) {
		DPlane plane = (DPlane)geom;
		PrimitiveSolids.drawStar(gl2, 10);
	}

	private void drawSphere(GL2 gl2) {
		PrimitiveSolids.drawSphere(gl2, ((DSphere)geom).getRadius());
	}

	private void drawBox(GL2 gl2) {
		Vector3d top = ODEPhysicsEngine.getVector3d(((DBox)geom).getLengths());
		top.scale(0.5);
		Vector3d bottom = new Vector3d(-top.x,-top.y,-top.z);
		PrimitiveSolids.drawBox(gl2, bottom, top);
	}
}
