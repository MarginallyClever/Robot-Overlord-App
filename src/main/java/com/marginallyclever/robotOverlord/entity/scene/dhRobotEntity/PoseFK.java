package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.memento.Memento;

/**
 * DHKeyframe contains the time, IK end effector, and FK values for a DHRobot in a given pose.
 * Linked together DHKeyframes describe animation between poses.
 * @author Dan Royer
 *
 */
public class PoseFK implements Memento {	
	public double [] fkValues;
	
	public PoseFK() {}
	
	public PoseFK(int size) {
		fkValues=new double[size];
	}
	
	public PoseFK(double [] arg0) {
		fkValues=new double[arg0.length];
		set(arg0);
	}
	
	
	public void interpolate(PoseFK a, PoseFK b, double t) {
		if(this.fkValues.length == a.fkValues.length && 
			a.fkValues.length == b.fkValues.length) {
			for(int i=0;i<this.fkValues.length;++i) {
				double c=a.fkValues[i];
				double d=b.fkValues[i];
				this.fkValues[i] = (d-c)*t+c; 
			}
		}
	}
	
	public void set(PoseFK arg0) {
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

	@Override
	public void save(OutputStream arg0) throws IOException {
		DataOutputStream out = new DataOutputStream(arg0);
		for( double v : fkValues ) {
			out.writeDouble(v);
		}
	}

	@Override
	public void load(InputStream arg0) throws IOException {
		DataInputStream out = new DataInputStream(arg0);
		for(int i=0;i<fkValues.length;++i) {
			fkValues[i] = out.readDouble();
		}
	}
}
