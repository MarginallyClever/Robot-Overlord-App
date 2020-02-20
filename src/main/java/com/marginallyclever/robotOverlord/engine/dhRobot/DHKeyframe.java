package com.marginallyclever.robotOverlord.engine.dhRobot;

import java.io.IOException;

import javax.vecmath.Matrix4d;

import org.json.simple.JSONObject;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.entity.robot.RobotKeyframe;

/**
 * DHKeyframe contains the time, IK end effector, and FK values for a DHRobot in a given pose.
 * Linked together DHKeyframes describe animation between poses.
 * @author Dan Royer
 *
 */
public class DHKeyframe implements RobotKeyframe {
	public double time;
	
	public Matrix4d poseIK;
	
	public double [] fkValues;
	

	public DHKeyframe(int size) {
		fkValues=new double[size];
		poseIK = new Matrix4d();
	}
	
	public DHKeyframe() {
		poseIK = new Matrix4d();
	}
	
	@Override
	public void interpolate(RobotKeyframe a, RobotKeyframe b, double t) {
		DHKeyframe dha = (DHKeyframe)a;
		DHKeyframe dhb = (DHKeyframe)b;
		if(this.fkValues.length == dha.fkValues.length && 
			dha.fkValues.length == dhb.fkValues.length) {
			for(int i=0;i<this.fkValues.length;++i) {
				double c=dha.fkValues[i];
				double d=dhb.fkValues[i];
				this.fkValues[i] = (d-c)*t+c; 
			}
		}
	}

	@Override
	public void render(GL2 gl2) {
		// TODO Auto-generated method stub
	}

	@Override
	public void renderInterpolation(GL2 gl2, RobotKeyframe arg1) {
		// TODO Auto-generated method stub
	}
	
	public void set(DHKeyframe arg0) {
		assert(arg0!=null);
		assert(arg0.fkValues.length>0);
		if(fkValues==null || fkValues.length!=arg0.fkValues.length) {
			fkValues = new double[arg0.fkValues.length];
		}
		for(int i=0;i<arg0.fkValues.length;++i) {
			fkValues[i] = arg0.fkValues[i];
		}
		poseIK.set(arg0.poseIK);
		time=arg0.time;
	}

	@Override
	public JSONObject toJSON() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fromJSON(JSONObject arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}
}
