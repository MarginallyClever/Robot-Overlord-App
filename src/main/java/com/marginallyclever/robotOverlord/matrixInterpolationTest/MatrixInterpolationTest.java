package com.marginallyclever.robotOverlord.matrixInterpolationTest;

import java.util.ArrayList;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.entity.Entity;

public class MatrixInterpolationTest extends Entity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public double alpha;
	public ArrayList<Matrix4d> points;
	public int index;
	public Matrix4d result;
	public double dir;
	
	
	public MatrixInterpolationTest() {
		super();
		
		setDisplayName("matrixInterpolationtest");

		points = new ArrayList<Matrix4d>();
		for(int i=0;i<5;++i) {
			Matrix4d temp = new Matrix4d();
			temp.set(MatrixHelper.eulerToMatrix(new Vector3d(Math.random()*Math.PI*2, Math.random()*Math.PI*2, Math.random()*Math.PI*2)));
			temp.setTranslation(new Vector3d(Math.random()*20-10,Math.random()*20-10,Math.random()*20));
			points.add(temp);
		}
		result = new Matrix4d();
		dir=1;
		alpha=0;
	}
	
	@Override
	public void update(double dt) {
		alpha+=dir*dt;
		
		//pingPong();
		loop();
	}
	
	public void loop() {
		if(alpha>1) {
			index = (index+1) % points.size();
			alpha-=1;
		}
	}
	
	public void pingPong() {
		if(alpha>1) {
			index++;
			if(index>points.size()-2) {
				index = points.size()-2;
				dir=-1;
				alpha=1;
			} else {
				alpha-=1;
			}
		}
		if(alpha<0) {
			index--;
			if(index<0) {
				index=0;
				dir=1;
				alpha=0;
			} else {
				alpha+=1;
			}
		}
		
	}

	@Override
	public void render(GL2 gl2) {
		Matrix4d start = points.get(index);
		Matrix4d end = points.get((index+1)%points.size());
		MatrixHelper.interpolate(start,end,alpha,result);
		MatrixHelper.drawMatrix(gl2, result, 4);
		
		for( Matrix4d m : points ) {
			MatrixHelper.drawMatrix(gl2, m, 3);
		}
	}
}
