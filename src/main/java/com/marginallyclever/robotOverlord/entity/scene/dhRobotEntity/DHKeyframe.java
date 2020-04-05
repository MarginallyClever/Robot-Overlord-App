package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.RobotKeyframe;

/**
 * DHKeyframe contains the time, IK end effector, and FK values for a DHRobot in a given pose.
 * Linked together DHKeyframes describe animation between poses.
 * @author Dan Royer
 *
 */
public class DHKeyframe implements RobotKeyframe {	
	public double [] fkValues;
	
	public DHKeyframe() {}
	
	public DHKeyframe(int size) {
		fkValues=new double[size];
	}
	
	public DHKeyframe(double [] arg0) {
		fkValues=new double[arg0.length];
		set(arg0);
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
	}
	
	public void set(double [] arg0) {
		if(fkValues.length != arg0.length) return;
		
		for(int i=0;i<arg0.length;++i) {
			fkValues[i]=arg0[i];
		}
	}
	
	public String toString() {
		String str="", add="";
		for(int i=0;i<fkValues.length;++i) {
			str+=add+StringHelper.formatDouble(fkValues[i]);
			add="\t";
		}
		return str;
	}
}
