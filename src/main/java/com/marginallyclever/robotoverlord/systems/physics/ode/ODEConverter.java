package com.marginallyclever.robotoverlord.systems.physics.ode;

import org.ode4j.math.*;
import org.ode4j.ode.DGeom;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

/**
 * Converts between ODE and Java.vecmath classes
 * @author Dan Royer
 */
public class ODEConverter {
	/**
	 * @param in {@link DMatrix3C}
	 * @return equivalent {@link Matrix3d}
	 */
	public static Matrix3d getMatrix3d(DMatrix3C in) {
		Matrix3d out = new Matrix3d();
		
		out.m00=in.get00();
		out.m01=in.get01();
		out.m02=in.get02();
		
		out.m10=in.get10();
		out.m11=in.get11();
		out.m12=in.get12();
	
		out.m20=in.get20();
		out.m21=in.get21();
		out.m22=in.get22();
		
		return out;
	}

	/**
	 * @param in {@link Matrix3d}
	 * @return equivalent {@link DMatrix3}
	 */
	public static DMatrix3 getDMatrix3(Matrix3d in) {
		DMatrix3 out = new DMatrix3();
		
		out.set00(in.m00);
		out.set01(in.m01);
		out.set02(in.m02);
	       
		out.set10(in.m10);
		out.set11(in.m11);
		out.set12(in.m12);
	       
		out.set20(in.m20);
		out.set21(in.m21);
		out.set22(in.m22);
		
		return out;
	}

	/**
	 * @param v {@link DVector3C}
	 * @return equivalent {@link Vector3d}
	 */
	public static Vector3d getVector3d(DVector3C v) {
		return new Vector3d(v.get0(),v.get1(),v.get2());
	}

	/**
	 * @param v {@link Vector3d}
	 * @return equivalent {@link DVector3}
	 */
	public static DVector3 getDVector3(Vector3d v) {
		return new DVector3(v.x,v.y,v.z);
	}

	/**
	 * @param q {@link DQuaternionC}
	 * @return equivalent {@link Quat4d}
	 */
	public static Quat4d getQuat4d(DQuaternionC q) {
		return new Quat4d(q.get1(),q.get2(),q.get3(),q.get0());
	}

	/**
	 * @param q {@link Quat4d}
	 * @return equivalent {@link DQuaternion}
	 */
	public static DQuaternion getDQuaternion(Quat4d q) {
		return new DQuaternion(q.w,q.x,q.y,q.z);
	}

	/**
	 * Build a {@link Matrix4d} from the orientation and position of a {@link DGeom}
	 * @param geom {@link DGeom}
	 * @return equivalent {@link Matrix4d}
	 */
	public static Matrix4d getMatrix4d(DGeom geom) {
		Matrix4d m = new Matrix4d();
		m.set(ODEConverter.getQuat4d(geom.getQuaternion()));
		m.setTranslation(ODEConverter.getVector3d(geom.getPosition()));
		return m;
	}

}
