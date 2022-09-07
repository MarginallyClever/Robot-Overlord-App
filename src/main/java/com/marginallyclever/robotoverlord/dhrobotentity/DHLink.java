package com.marginallyclever.robotoverlord.dhrobotentity;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotoverlord.Collidable;
import com.marginallyclever.robotoverlord.entities.PoseEntity;
import com.marginallyclever.robotoverlord.entities.ShapeEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewElement;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import com.marginallyclever.robotoverlord.uiexposedtypes.DoubleEntity;
import com.marginallyclever.robotoverlord.uiexposedtypes.StringEntity;

/**
 * Denavit–Hartenberg parameters
 * @author Dan Royer
 * See https://en.wikipedia.org/wiki/Denavit%E2%80%93Hartenberg_parameters
 */
@Deprecated
public class DHLink extends PoseEntity implements Collidable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3049913430394239397L;

	// Changes visual quality of angle range curve.  Must be a whole number >=2
	// TODO should be in the view, not the model.
	public final static double ANGLE_RANGE_STEPS=20;

	public enum LinkAdjust {
		NONE (0x0,"None" ),
		D    (0x1,"D"    ),
		THETA(0x2,"Theta"),
		R    (0x4,"R"    ),
		ALPHA(0x8,"Alpha"),
		ALL  (0xf,"All"  );  // for editing
		
		private int number;
		private String name;
		private LinkAdjust(int n,String s) {
			number=n;
			name=s;
		}
		public int toInt() {
			return number;
		}
		@Override
		public String toString() {
			return name;
		}
		static public String [] getAll() {
			LinkAdjust[] allModes = LinkAdjust.values();
			String[] labels = new String[allModes.length];
			for(int i=0;i<labels.length;++i) {
				labels[i] = allModes[i].toString();
			}
			return labels;
		}
	};
	
	@JsonIgnore
	public LinkAdjust flags;

	// the gcode letter representing this link
	public StringEntity letter = new StringEntity("Letter","");
	
	// length (mm) along previous Z to the common normal
	public DoubleEntity d = new DoubleEntity("D");
	// angle (degrees) about previous Z, from old X to new X
	public DoubleEntity theta = new DoubleEntity("Theta");
	// length (mm) of the common normal. Assuming a revolute joint, this is the radius about previous Z
	public DoubleEntity r = new DoubleEntity("R");
	// angle (degrees) about common normal, from old Z axis to new Z axis
	public DoubleEntity alpha = new DoubleEntity("Alpha");
	// adjustable link range limits
	public DoubleEntity rangeMin = new DoubleEntity("Range min", -90.0);
	// adjustable link range limits
	public DoubleEntity rangeMax = new DoubleEntity("Range max", 90.0);
	// not used yet
	public DoubleEntity maxTorque = new DoubleEntity("max torque (Nm)",Double.MAX_VALUE);
	// not used yet
	public DoubleEntity maxAcceleration = new DoubleEntity("max accel (deg/s)",Double.MAX_VALUE);
	// not used yet
	// dynamics are described in a 4x4 matrix
	//     [ Ixx Ixy Ixz XgM ]
	// J = [ Iyx Iyy Iyz YgM ]
	//     [ Izx Izy Izz ZgM ]
	//     [ XgM YgM ZgM  M  ]
	// where mass M, Ng is the center of mass, and I terms represent the inertia.
	//public Matrix4dEntity inertia = new Matrix4dEntity();
	
	protected ShapeEntity shapeEntity = new ShapeEntity();
	
	public DHLink() {
		super();
		setName("DHLink");

		addChild(d);
		addChild(r);
		addChild(theta);
		addChild(alpha);
		
		d.addPropertyChangeListener(this);
		r.addPropertyChangeListener(this);
		alpha.addPropertyChangeListener(this);
		theta.addPropertyChangeListener(this);
		
		addChild(rangeMin);
		addChild(rangeMax);

		addChild(maxAcceleration);
		addChild(maxTorque);

		flags=LinkAdjust.THETA;
	}
	
	public DHLink(DHLink arg0) {
		super();
		
		set(arg0);
	} 
	
	public void set(DHLink arg0) {
		setName(arg0.getName());

		flags = arg0.flags;
		d.set(arg0.d.get());
		theta.set(arg0.theta.get());
		r.set(arg0.r.get());
		alpha.set(arg0.alpha.get());
		shapeEntity.set(arg0.shapeEntity);
		rangeMin.set(arg0.rangeMin.get());
		rangeMax.set(arg0.rangeMax.get());
		/*
		maxVelocity.set(arg0.maxVelocity);
		maxAcceleration.set(arg0.maxAcceleration);
		maxTorque.set(arg0.maxTorque);
		inertia.set(arg0.inertia);
		*/
	}
	
	
	/**
	 * Set up the pose based on D-H parameters, then update the worldPose.
	 * Equivalent to T(n) = TransZ(d) * RotZ(theta) * TransX(r) * RotX(alpha)
	 */
	public void refreshDHMatrix() {
		double t=theta.get();
		double a=alpha.get();
		assert(!Double.isNaN(t));
		assert(!Double.isNaN(a));
		assert(!Double.isNaN(r.get()));
		assert(!Double.isNaN(d.get()));
		double ct = Math.cos(Math.toRadians(t));
		double ca = Math.cos(Math.toRadians(a));
		double st = Math.sin(Math.toRadians(t));
		double sa = Math.sin(Math.toRadians(a));
		Matrix4d m = new Matrix4d();
		
		m.m00 = ct;		m.m01 = -st*ca;		m.m02 = st*sa;		m.m03 = r.get()*ct;
		m.m10 = st;		m.m11 = ct*ca;		m.m12 = -ct*sa;		m.m13 = r.get()*st;
		m.m20 = 0;		m.m21 = sa;			m.m22 = ca;			m.m23 = d.get();
		m.m30 = 0;		m.m31 = 0;			m.m32 = 0;			m.m33 = 1;
		
		//Log.message(letter.get() + "="+m);
		setPose(m);
	}

	@Override
	public void update(double dt) {
		super.update(dt);
	}
	
	@Override
	public void render(GL2 gl2) {
		// preserve original material color
		double [] diffuse = shapeEntity.getMaterial().getDiffuseColor();
		// change material color - more red when near angle limits 
		setAngleColorByRange(gl2);
		
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, myPose);
			shapeEntity.render(gl2);
		gl2.glPopMatrix();
		
		// restore original material color
		shapeEntity.getMaterial().setDiffuseColor(diffuse[0],diffuse[1],diffuse[2],diffuse[3]);
		
		super.render(gl2);
	}
	
	/**
	 * Render the min/max/current angle for one link in a D-H chain.  
	 * Changes the current render matrix!  Clean up after yourself!  
	 * @param gl2 the render context
	 */
	public void renderAngles(GL2 gl2) {
		// draw the angle range
		double k;
		final double scale=10;
		
		boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_LIGHTING);

		double mid=getRangeCenter();
		
		gl2.glColor3f(0, 0, 0);
		if(flags == LinkAdjust.THETA) {
			// display the curve around z (in the xy plane)
			gl2.glPushMatrix();
			gl2.glTranslated(0, 0, d.get());
			gl2.glScaled(scale, scale, scale);
			gl2.glColor4d(0,0,0,0.35);
			gl2.glBegin(GL2.GL_LINE_STRIP);
			gl2.glVertex3d(0, 0, 0);
			for(k=0;k<=ANGLE_RANGE_STEPS;++k) {
				double j=(getRange())*(k/ANGLE_RANGE_STEPS)+rangeMin.get();
				gl2.glVertex3d(
						Math.cos(Math.toRadians(j)), 
						Math.sin(Math.toRadians(j)), 
						0);
			}
			gl2.glVertex3d(0, 0, 0);
			gl2.glEnd();
			setAngleColorByRange(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
			gl2.glVertex3d(0, 0, 0);
			double steps = Math.floor(Math.abs(mid-theta.get()));
			for(k=0;k<steps;++k) {
				double j = (theta.get()-mid)*(k/steps)+mid;
				gl2.glVertex3d(
						Math.cos(Math.toRadians(j)), 
						Math.sin(Math.toRadians(j)), 
						0);
			}
			gl2.glEnd();
			gl2.glPopMatrix();
		}
		if(flags == LinkAdjust.D) {
			// display the prismatic nature of d
			gl2.glPushMatrix();
			gl2.glBegin(GL2.GL_LINES);
			gl2.glVertex3d(0,  1, rangeMin.get());
			gl2.glVertex3d(0, -1, rangeMin.get());
			gl2.glVertex3d(0,  0, rangeMin.get());
			gl2.glVertex3d(0,  0, rangeMax.get());
			gl2.glVertex3d(0,  1, rangeMax.get());
			gl2.glVertex3d(0, -1, rangeMax.get());
			gl2.glVertex3d(0,  1, d.get());
			gl2.glVertex3d(0, -1, d.get());
			gl2.glEnd();
			gl2.glPopMatrix();
		}
		if(flags == LinkAdjust.ALPHA) {
			// display the curve around x (in the yz plane)
			gl2.glPushMatrix();
			gl2.glTranslated(r.get(), 0, d.get());
			gl2.glRotated(this.theta.get(), 0, 0, 1);
			gl2.glScaled(scale, scale, scale);
			gl2.glColor4d(0,0,0,0.35);
			gl2.glBegin(GL2.GL_LINE_STRIP);
			gl2.glVertex3d(0, 0, 0);
			for(k=0;k<=ANGLE_RANGE_STEPS;++k) {
				double j=getRange()*(k/ANGLE_RANGE_STEPS)+rangeMin.get();
				gl2.glVertex3d(
						0,
						Math.cos(Math.toRadians(j)),
						Math.sin(Math.toRadians(j)));
			}
			gl2.glVertex3d(0, 0, 0);
			gl2.glEnd();
			setAngleColorByRange(gl2);
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
			gl2.glVertex3d(0, 0, 0);
			double steps = Math.floor(Math.abs(mid-alpha.get()));
			for(k=0;k<steps;++k) {
				double j = (alpha.get()-mid)*(k/steps)+mid;
				gl2.glVertex3d(0,
						Math.cos(Math.toRadians(j)), 
						Math.sin(Math.toRadians(j))
						);
			}
			gl2.glEnd();/*
			gl2.glVertex3d(0, 0, 0);
			gl2.glVertex3d(
					0,
					Math.cos(Math.toRadians(this.alpha)),
					Math.sin(Math.toRadians(this.alpha)));
			gl2.glEnd();*/
			gl2.glPopMatrix();
		}
		if(flags == LinkAdjust.R) {
			// display the prismatic nature of r
			gl2.glPushMatrix();
			gl2.glTranslated(0, 0, d.get());
			gl2.glRotated(this.theta.get(), 0, 0, 1);
			gl2.glBegin(GL2.GL_LINES);
			double rm = rangeMin.get();
			gl2.glVertex3d(rm     ,  1, 0);
			gl2.glVertex3d(rm     , -1, 0);
			gl2.glVertex3d(rm     ,  0, 0);
			gl2.glVertex3d(rm     ,  0, 0);
			gl2.glVertex3d(rm     ,  1, 0);
			gl2.glVertex3d(rm     , -1, 0);
			gl2.glVertex3d(r.get(),  1, 0);
			gl2.glVertex3d(r.get(), -1, 0);
			gl2.glEnd();
			gl2.glPopMatrix();
		}
		if(isLit) gl2.glEnable(GL2.GL_LIGHTING);
	}
	
	/**
	 * color the angle line green in the safe zone, red near the limits
	 * @param gl2 the render context
	 */
	public void setAngleColorByRange(GL2 gl2) {
		if(flags == LinkAdjust.NONE) return;
		if(rangeMax.get()==rangeMin.get()) return;  // no range limit?
		
		double a= (flags == LinkAdjust.THETA) ? theta.get() : alpha.get();
		double halfRange = getRange()/2;
		double midRange = getRangeCenter();
		float safety = (float)(Math.abs(a-midRange)/halfRange);
		safety*=safety*safety;  // squared
		//gl2.glColor4d(safety,1-safety,0,0.5);
//		float [] diffuse = {safety,1-safety,0,0};
		
		double [] original = shapeEntity.getMaterial().getDiffuseColor();
		
		original[0]+=safety;
		original[1]-=safety;
		original[2]-=safety;
		
		shapeEntity.getMaterial().setDiffuseColor(original[0],original[1],original[2],original[3]);
	}
	
	public boolean hasAdjustableValue() {
		return flags != LinkAdjust.NONE;
	}
	
	/**
	 * In any DHLink there should only be one parameter that changes in value.  Return that value.
	 */
	public double getAdjustableValue() {
		switch(flags) {
		case D    :  return getD();
		case R    :  return getR();
		case THETA:  return getTheta();
		case ALPHA:  return getAlpha();
		default   :  return 0;
		}
	}
	
	/**
	 * Set the (one) adjustable value, after making sure it is within the range limits.
	 */
	public void setAdjustableValue(double v) {
		//Log.message("Adjust begins");
		v = Math.max(Math.min(v, rangeMax.get()), rangeMin.get());
		switch(flags) {
		case D    :  setD    (v);  break;
		case THETA:  setTheta(v);  break;
		case R    :  setR    (v);  break;
		case ALPHA:  setAlpha(v);  break;
		default   :  break;
		}
		//Log.message("Adjust ends");
	}

	public double getD() {
		return d.get();
	}

	public void setD(double v) {
		d.set(v);
	}

	public double getTheta() {
		return theta.get();
	}

	public void setTheta(double v) {
		theta.set(v);
	}

	public double getR() {
		return r.get();
	}

	public void setR(double v) {
		r.set(v);
	}

	public double getAlpha() {
		return alpha.get();
	}

	public void setAlpha(double v) {
		alpha.set(v);
	}

	public double getRangeMin() {
		return rangeMin.get();
	}

	public void setRangeMin(double v) {
		rangeMin.set(v);
	}

	public double getRangeMax() {
		return rangeMax.get();
	}

	public void setRangeMax(double v) {
		rangeMax.set(v);
	}
	
	public void setRange(double rangeMin,double rangeMax) {
		setRangeMin(rangeMin);
		setRangeMax(rangeMax);
	}
	
	public double getRange() {
		return rangeMax.get()-rangeMin.get();
	}

	public double getRangeCenter() {
		return (rangeMax.get()+rangeMin.get())/2.0;
	}

	public void setLetter(String letter) {
		this.letter.set( letter );
		this.setName( letter );
		d.setName(letter + " D");
		r.setName(letter + " R");
		theta.setName(letter + " Theta");
		alpha.setName(letter + " Alpha");
	}
	
	public String getLetter() {
		return letter.get();
	}

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("DH","DHLink");

		ViewElement vt, va, vd, vr;
		if(flags==LinkAdjust.THETA) {
			vt = view.addRange(theta,
					(int)Math.floor(rangeMax.get()),
					(int)Math.ceil(rangeMin.get()));
		} else {
			vt = view.add(theta);
		}
		
		if(flags==LinkAdjust.ALPHA) {
			va = view.addRange(alpha,
					(int)Math.floor(rangeMax.get()),
					(int)Math.ceil(rangeMin.get()));
		} else {
			va = view.add(alpha);
		}
		
		vd = view.add(d);
		vr = view.add(r);
		
		ViewElement vMin = view.add(rangeMin);
		ViewElement vMax = view.add(rangeMax);
		
		//*
		// set the fields to be read only
		// TODO make readable when designing new linkages
		vd.setReadOnly(0==(flags.toInt() & LinkAdjust.D    .toInt()));
		vt.setReadOnly(0==(flags.toInt() & LinkAdjust.THETA.toInt()));
		vr.setReadOnly(0==(flags.toInt() & LinkAdjust.R    .toInt()));
		va.setReadOnly(0==(flags.toInt() & LinkAdjust.ALPHA.toInt()));
		va.setReadOnly(0==(flags.toInt() & LinkAdjust.ALPHA.toInt()));
		vMin.setReadOnly(flags!=LinkAdjust.ALL);
		vMax.setReadOnly(flags!=LinkAdjust.ALL);
		//*/
		view.popStack();
		
		super.getView(view);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		refreshDHMatrix();
	}
	
	@Override
	public void setPoseWorld(Matrix4d newPose) {
		Matrix4d newRelativePose; 
		if(parent instanceof PoseEntity) {
			PoseEntity pe = (PoseEntity)parent;
			newRelativePose = pe.getPoseWorld();
			newRelativePose.invert();
			newRelativePose.mul(newPose);
		} else {
			newRelativePose = new Matrix4d(newPose);
		}
		
		setPose(newRelativePose);
	}
	
	/**
	 * Ask this entity "can you move to newPose?"
	 * @param newWorldPose the desired world pose of the link
	 * @return true if it can.
	 */
	public boolean canMoveTowards(Matrix4d newWorldPose) {
		if( parent instanceof DHLink || parent instanceof DHRobotModel ) {
			if( !this.getLetter().isEmpty() ) {
				Matrix4d oldPose = getPoseWorld();
				// we have newPose ...but is it something this DHLink could do?
				// For D-H links, the convention is that rotations are always around the Z axis.  the Z axis of each matrix should match.
				// TODO Today this is the only case I care about. make it better later.
	
				// difference in position
				double dx =Math.abs(newWorldPose.m03-oldPose.m03);
				double dy =Math.abs(newWorldPose.m13-oldPose.m13);
				double dz =Math.abs(newWorldPose.m23-oldPose.m23);
				if(dx+dy+dz>1e-6) return false;
	
				// difference in z axis
				dx =Math.abs(newWorldPose.m02-oldPose.m02);
				dy =Math.abs(newWorldPose.m12-oldPose.m12);
				dz =Math.abs(newWorldPose.m22-oldPose.m22);
				if(dx+dy+dz>1e-6) return false;
			}
		}
		// we made it here, move is legal!
		return true;
	}
	
	@Override
	public boolean canBeRenamed() {
		return false;
	}

	public void setShapeFilename(String modelFilename) {
		shapeEntity.setShapeFilename(modelFilename);
	}

	public void setShapeScale(double d) {
		shapeEntity.setShapeScale(d);
	}

	public void setTextureFilename(String string) {
		shapeEntity.getMaterial().setTextureFilename(string);
	}

	public void setShapeRotation(Vector3d vector3d) {
		shapeEntity.setShapeRotation(vector3d);
	}

	public void setShapeOrigin(Vector3d vector3d) {
		shapeEntity.setShapeOrigin(vector3d);
	}

	public void setShapeMatrix(Matrix4d matrix4d) {
		shapeEntity.setPose(matrix4d);
	}

	/**
	 * Convenience method to set DH parameters
	 * @param d
	 * @param r
	 * @param alpha
	 * @param theta
	 */
	public void setDHParams(double d, double r, double alpha, double theta) {
		setD(d);
		setR(r);
		setAlpha(alpha);
		setTheta(theta);
	}

	/**
	 * @return a list of {@link Cuboid} relative to the world.
	 */
	@Override
	public ArrayList<Cuboid> getCuboidList() {
		ArrayList<Cuboid> list = shapeEntity.getCuboidList();
		Matrix4d m = getPoseWorld();
		Matrix4d m3 = new Matrix4d();
				
		for( Cuboid c : list ) {
			m3.mul(m,c.getPose());
			c.setPose(m3);
		}
		return list;
	}
}
