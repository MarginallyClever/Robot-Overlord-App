package com.marginallyclever.robotOverlord.matrixInterpoationTest;

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
	public Matrix4d start;
	public Matrix4d end;
	public Matrix4d result;
	public double dir;
	
	
	public MatrixInterpolationTest() {
		super();
		
		setDisplayName("matrixInterpolationtest");

		start = new Matrix4d();
		end = new Matrix4d();
		result = new Matrix4d();
		dir=1;
		alpha=0;

		start.set(MatrixHelper.eulerToMatrix(new Vector3d(Math.random()*Math.PI*2, Math.random()*Math.PI*2, Math.random()*Math.PI*2)));
		end  .set(MatrixHelper.eulerToMatrix(new Vector3d(Math.random()*Math.PI*2, Math.random()*Math.PI*2, Math.random()*Math.PI*2)));
		start.setTranslation(new Vector3d(Math.random()*20-10,Math.random()*20-10,Math.random()*20));
		end  .setTranslation(new Vector3d(Math.random()*20-10,Math.random()*20-10,Math.random()*20));
	}
	
	@Override
	public void update(double dt) {
		alpha+=dir*dt;
		
		if(alpha>1) {
			alpha=1;
			dir=-dir;
		}
		if(alpha<0) {
			alpha=0;
			dir=-dir;
		}
		
	}

	@Override
	public void render(GL2 gl2) {
		MatrixHelper.interpolate(start,end,alpha,result);
		MatrixHelper.drawMatrix(gl2, start, 3);
		MatrixHelper.drawMatrix(gl2, end, 3);
		MatrixHelper.drawMatrix(gl2, result, 3);
	}
}
