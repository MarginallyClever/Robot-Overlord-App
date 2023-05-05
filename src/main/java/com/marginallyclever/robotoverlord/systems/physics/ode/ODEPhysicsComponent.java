package com.marginallyclever.robotoverlord.systems.physics.ode;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import org.ode4j.ode.DBox;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DPlane;
import org.ode4j.ode.DSphere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

/**
 * A component that holds a reference to an ODE geometry.
 *
 * @author Dan Royer
 * @since 2.0?
 */
public class ODEPhysicsComponent extends Component {
	private static final Logger logger = LoggerFactory.getLogger(ODEPhysicsComponent.class);
	private DGeom geom;
	private PoseComponent myPose;
	
	public ODEPhysicsComponent(DGeom g) {
		super();
		geom=g;
	}
	
	public DGeom getGeom() {
		return geom;
	}
	
	@Override
	public void update(double dt) {
		if(myPose==null) myPose = getEntity().getComponent(PoseComponent.class);
		if(myPose!=null) {
			if(!(geom instanceof DPlane)) {
				myPose.setWorld(ODEConverter.getMatrix4d(geom));
			}
		}
	}

	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		if(geom instanceof DBox) drawBox(gl2);
		else if(geom instanceof DSphere) drawSphere(gl2);
		else if(geom instanceof DPlane) drawPlane(gl2);
		else logger.error("systems() unknown type "+geom.getClass().getName());
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
