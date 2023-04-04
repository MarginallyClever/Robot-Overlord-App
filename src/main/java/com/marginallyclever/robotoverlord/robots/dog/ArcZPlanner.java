package com.marginallyclever.robotoverlord.robots.dog;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.OpenGLHelper;

import javax.vecmath.Vector3d;
import java.security.InvalidParameterException;

@Deprecated
public class ArcZPlanner {
	private Vector3d pointFrom = new Vector3d();
	private Vector3d pointTo = new Vector3d();
	private Vector3d pointNow = new Vector3d();
	private double height = DogLeg.DEFAULT_STEP_HEIGHT;
	private double timeTotal=1;
	private double timeNow=0;
	
	public ArcZPlanner() {}
	
	public void planStep(Vector3d start,Vector3d end,double height,double timeToComplete) {
		pointFrom.set(start);
		pointTo.set(end);
		this.height = height;
		timeTotal = timeToComplete;
		timeNow = 0;
		pointNow.set(pointTo);
	}
	
	public void advance(double dt) {
		timeNow += dt;
		timeNow = Math.min(timeNow,timeTotal);
		pointNow = interpolate(timeNow);
	}
	
	public Vector3d getPointNow() {
		return pointNow;
	}	
	
	public void render(GL2 gl2,double refinement) throws InvalidParameterException {
		if(refinement<1) throw new InvalidParameterException("refinement must be >=1");

		boolean wasOn = OpenGLHelper.disableLightingStart(gl2);
		gl2.glBegin(GL2.GL_LINES);
		//gl2.glBegin(GL2.GL_LINE_STRIP);
		double ir = 1.0/refinement;
		for(double t=0;t<=1;t+=ir) {
			Vector3d i = interpolate(t);
			double c=(t*1)%1;
			gl2.glColor3d(1-c, 0, c);
			gl2.glVertex3d(i.x, i.y, i.z);
		}
		gl2.glEnd();
		OpenGLHelper.disableLightingEnd(gl2, wasOn);
	} 
	
	/**
	 * @param scale 0...1
	 * @return position along step path
	 */
	private Vector3d interpolate(double scale) {
		double z = height * Math.sin(scale*Math.PI);
		Vector3d i = new Vector3d();
		i.sub(pointTo,pointFrom);
		i.scale(scale);
		i.add(pointFrom);
		i.z+=z;
		return i;
	}
	
	public double getStepDistanceSquared() {
		Vector3d v = new Vector3d(pointTo);
		v.sub(pointFrom);
		return v.lengthSquared();
	}
}
