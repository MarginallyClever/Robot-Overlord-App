package com.marginallyclever.robotOverlord.physics.ode;

import javax.vecmath.Matrix4d;
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
	public void update(double dt) {
		if(!(geom instanceof DPlane)) {
			Matrix4d m = ODEConverter.getMatrix4d(geom);
			setPoseWorld(m);
		}
		super.update(dt);
	}
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
		
		gl2.glPushMatrix();
			gl2.glDisable(GL2.GL_TEXTURE_2D);
			mat.render(gl2);
		
			if(geom instanceof DBox) drawBox(gl2);
			else if(geom instanceof DSphere) drawSphere(gl2);
			else if(geom instanceof DPlane) drawPlane(gl2);
			else logger.error("render() unknown type "+geom.getClass().getName());
		
		gl2.glPopMatrix();
	}

	private void drawPlane(GL2 gl2) {
		DPlane plane = (DPlane)geom;
		Vector3d nz = ODEConverter.getVector3d(plane.getNormal());
		Vector3d p = new Vector3d(nz);
		p.scale(plane.getDepth());
		Vector3d ny = new Vector3d();
		if(nz.x>nz.y) {
			if(nz.x>nz.z) ny.set(nz.y,nz.x,nz.z);
			else ny.set(nz.x,nz.z,nz.z);
		} else {
			if(nz.y>nz.z) ny.set(nz.y,nz.x,nz.z);
			else ny.set(nz.x,nz.z,nz.z);
		}
		Vector3d nx = new Vector3d();
		nx.cross(nz, ny);
		ny.cross(nx, nz);
		MatrixHelper.drawMatrix(gl2, p, nx, ny, nx);
		Matrix4d m = new Matrix4d(
				nx.x,nx.y,nx.z,p.x,
				ny.x,ny.y,ny.z,p.y,
				nz.x,nz.y,nz.z,p.z,
				0,0,0,1);
		MatrixHelper.applyMatrix(gl2, m);

		PrimitiveSolids.drawCircleXY(gl2, 10, 40);
		PrimitiveSolids.drawStar(gl2, 10);
	}

	private void drawSphere(GL2 gl2) {
		MatrixHelper.applyMatrix(gl2, ODEConverter.getMatrix4d(geom));
		PrimitiveSolids.drawSphere(gl2, ((DSphere)geom).getRadius());
	}

	private void drawBox(GL2 gl2) {
		MatrixHelper.applyMatrix(gl2, ODEConverter.getMatrix4d(geom));
		Vector3d top = ODEConverter.getVector3d(((DBox)geom).getLengths());
		top.scale(0.5);
		Vector3d bottom = new Vector3d(-top.x,-top.y,-top.z);
		PrimitiveSolids.drawBox(gl2, bottom, top);
	}
}
