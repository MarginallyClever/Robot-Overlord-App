package com.marginallyclever.robotOverlord.robots.sixi3;

import java.beans.PropertyChangeEvent;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.IntersectionHelper;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.Collidable;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.shape.Mesh;
import com.marginallyclever.robotOverlord.shape.Shape;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewElementButton;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.BooleanEntity;

/**
 * Simulation of a Sixi3 robot arm with Forward Kinematics based on Denavit Hartenberg parameters.
 * It manages a set of {@link Sixi3Bone}.
 * @see <a href='https://en.wikipedia.org/wiki/Denavit%E2%80%93Hartenberg_parameters'>DH parameters</a>
 * @see <a href='https://en.wikipedia.org/wiki/Forward_kinematics'>Forward Kinematics</a>
 * @author Dan Royer
 * @since 2021-02-24
 */
public class Sixi3FK extends PoseEntity implements Collidable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2436924907127292890L;
	
	// unmoving model of the robot base.
	transient private Shape base;
	// DH parameters, meshes, physical limits.
	private ArrayList<Sixi3Bone> bones = new ArrayList<Sixi3Bone>();
	// visualize rotations?
	public BooleanEntity showAngles = new BooleanEntity("Show Angles",false);

	public Sixi3FK() {
		super();
		setName("Sixi3FK");
		
		setupModel();
		
		for(int i=0;i<bones.size();++i) {
			Sixi3Bone b = bones.get(i);
			b.setSliderName("J"+(i));
			final int j=i;
			b.slider.addPropertyChangeListener((evt)->{
				double [] v = getFKValues();
				double d = b.slider.get();
				if( v[j] != d ) {
					v[j] = d;
					setFKValues(v);
					updateSliders();
				}
			});
			b.updateMatrix();
		}
	}

	/**
	 * Set up the DH link hierarchy according to the DH parameters.  Also load the shapes.  
	 * The physical location of the shapes does not match the DH linkage description of the robot, 
	 * so adjust the {@link Sixi3Bone.shapeOffset} of each bone to compensate.
	 */
	private void setupModel() {
		// load the base shape.
		base = new Shape("Base","/Sixi3b/base.3mf");
		bones.clear();
		// name d r a t max min file
		addBone("j0", 8.01,     0,270,  0,170    ,-170   ,"/Sixi3b/j0.3mf");
		addBone("j1",9.131,17.889,  0,270,270+100,270-100,"/Sixi3b/j1.3mf");
		addBone("j2",    0,12.435,  0,  0,0+150  ,0-150  ,"/Sixi3b/j2.3mf");
		addBone("j3",    0,     0,270,270,270+170,270-170,"/Sixi3b/j3.3mf");
		addBone("j4", 5.12,     0,  0,  0,360    ,0      ,"/Sixi3b/j4.3mf");
		//addBone("end effector",     0, 5.12,  0,  0,350,10,"");
		
		adjustModelOriginsToDHLinks();
	}
	
	// Use the cumulative pose of each Sixi3Bone to adjust the model origins.
	private void adjustModelOriginsToDHLinks() {
		Matrix4d current = new Matrix4d();
		current.setIdentity();
		for( Sixi3Bone bone : bones ) {
			bone.updateMatrix();
			current.mul(bone.getPose());
			Matrix4d iWP = new Matrix4d(current);
			iWP.invert();			
			bone.shape.setPose(iWP);
		}
	}

	private void addBone(String name, double d, double r, double a, double t, double jMax, double jMin, String modelFilename) {
		Sixi3Bone b = new Sixi3Bone();
		b.set(name,d,r,a,t,jMax,jMin,modelFilename);
		b.setSliderName(name);
		bones.add(b);
	}

	@Override
	public void update(double dt) {
		super.update(dt);
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, pose);
			drawMeshes(gl2);
			drawExtras(gl2);
		gl2.glPopMatrix();
		
		super.render(gl2);
	}
	
	private void drawMeshes(GL2 gl2) {
		base.render(gl2);

		gl2.glPushMatrix();
		for( Sixi3Bone bone : bones ) {
			// draw model with local shape offset
			bone.updateMatrix();
			MatrixHelper.applyMatrix(gl2, bone.getPose());
			bone.shape.render(gl2);
		}
		gl2.glPopMatrix();
	}
	
	private void drawExtras(GL2 gl2) {
		// turn of textures so lines draw good
		boolean wasTex = gl2.glIsEnabled(GL2.GL_TEXTURE_2D);
		gl2.glDisable(GL2.GL_TEXTURE_2D);
		// turn off lighting so lines draw good
		boolean wasLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_LIGHTING);
		// draw on top of everything else
		int wasOver=OpenGLHelper.drawAtopEverythingStart(gl2);
		
		if(showLineage.get()) drawLineage(gl2);
		if(showAngles.get()) drawAngles(gl2);
		// bounding boxes are always relative to base?
		if(showBoundingBox.get()) drawBoundindBoxes(gl2);
		
		if(showLocalOrigin.get()) {
			Matrix4d m = new Matrix4d();
			getEndEffector(m);
			MatrixHelper.drawMatrix2(gl2, m, 6);
		}

		// return state if needed
		OpenGLHelper.drawAtopEverythingEnd(gl2,wasOver);
		if(wasLit) gl2.glEnable(GL2.GL_LIGHTING);
		if(wasTex) gl2.glEnable(GL2.GL_TEXTURE_2D);	
	}
	
	private void drawBoundindBoxes(GL2 gl2) {
		boolean hit = collidesWithSelf();
		if(hit) gl2.glColor3d(1, 0, 0);
		else    gl2.glColor3d(1, 1, 1);

		Matrix4d w = getPoseWorld();
		w.invert();
		
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, w);
			ArrayList<Cuboid> list = getCuboidList();
			for(Cuboid c : list) {
				gl2.glPushMatrix();
				c.getPose(w);
				MatrixHelper.applyMatrix(gl2, w);
				PrimitiveSolids.drawBoxWireframe(gl2, c.getBoundsBottom(),c.getBoundsTop());
				gl2.glPopMatrix();
			}
		gl2.glPopMatrix();
	}

	private void drawAngles(GL2 gl2) {
		boolean cullOn = gl2.glIsEnabled(GL2.GL_CULL_FACE);
		gl2.glDisable(GL2.GL_CULL_FACE);
		
		gl2.glPushMatrix();
			int j = bones.size()+1;
			for( Sixi3Bone bone : bones ) {
				bone.updateMatrix();
				double bmin = bone.getAngleMin();
				double bmax = bone.getAngleMax();
				double color = Math.abs(0.5-MathHelper.getUnitInRange(bmin,bmax,bone.theta));
				// curve of movement
				gl2.glColor4d(1,1-color,1-color,0.6);
				gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glVertex3d(0, 0, 0);
				double diff = bone.theta-180;
				double end = Math.abs(diff);
				double dir = diff>0?1:-1;
				double radius = j;
				for(double a = 0; a<end;a+=5) {
					double s = Math.sin(Math.toRadians(-a*dir)) * radius;
					double c = Math.cos(Math.toRadians(-a*dir)) * radius;
					gl2.glVertex3d(s, c,0);
				}
				gl2.glEnd();

				MatrixHelper.applyMatrix(gl2, bone.getPose());
				--j;
			}
		gl2.glPopMatrix();

		if(cullOn) gl2.glEnable(GL2.GL_CULL_FACE);
	}

	private void drawLineage(GL2 gl2) {
		Vector3d v = new Vector3d();
		// then the bones, overtop and unlit.
		gl2.glPushMatrix();
			int j = bones.size()+1;
			for( Sixi3Bone bone : bones ) {
				bone.updateMatrix();
				// draw bone origin
				PrimitiveSolids.drawStar(gl2,j*2);
				// draw line to next bone
				bone.getPose().get(v);
				gl2.glColor3d(1, 0, 1);
				gl2.glBegin(GL2.GL_LINES);
				gl2.glVertex3d(0, 0, 0);
				gl2.glVertex3d(v.x,v.y,v.z);
				gl2.glEnd();

				MatrixHelper.applyMatrix(gl2, bone.getPose());
				--j;
			}
		gl2.glPopMatrix();
	}

	/**
	 * Find the current end effector pose, relative to the base of this robot
	 * @param m where to store the end effector pose.
	 */
	public void getEndEffector(Matrix4d m) {
		m.setIdentity();
		for( Sixi3Bone bone : bones ) {
			m.mul(bone.getPose());
		}
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("FK","Forward Kinematics");
		for( Sixi3Bone b : bones ) {
			b.getView(view);
		}
		ViewElementButton button = view.addButton("Center all");
		button.addPropertyChangeListener((evt)-> {
			double [] v = new double[bones.size()];
			for(int i=0;i<bones.size();++i) {
				Sixi3Bone b = bones.get(i);
				v[i]=b.getAngleMiddle();
			}
			setFKValues(v);
			updateSliders();
		});
		view.add(showAngles);
		view.popStack();
		
		super.getView(view);
	}
	
	/**
	 * Refresh the FK sliders in the GUI.
	 */
	protected void updateSliders() {
		for( Sixi3Bone b : bones ) {
			b.slider.set(b.theta);
		}	
	}

	/**
	 * 
	 * @param list where to collect the information.  Must be {@link Sixi3FK#NUM_BONES} long.
	 * @throws InvalidParameterException
	 */
	public double [] getFKValues() {
		double [] list = new double[bones.size()];
		
		int i=0;
		for( Sixi3Bone bone : bones ) {
			list[i++] = bone.theta;
		}
		return list;
	}
	
	/**
	 * Update the theta angles of each bone in the robot and the FK sliders on the panel.
	 * It does not allow you to set the angle of a bone outside the angleMax/angleMin of that bone.
	 * @param list new theta values.  Must be {@link Sixi3FK#NUM_BONES} long.
	 * @return true if new values are different from old values.
	 * @throws InvalidParameterException list is the wrong length.
	 */
	public boolean setFKValues(double [] list) throws InvalidParameterException {
		if(list==null) {
			throw new InvalidParameterException("list cannot be null.");
		}
		if(bones.size() != list.length) {
			throw new InvalidParameterException("list length must match number of bones ("+bones.size()+")");
		}

		boolean changed=false;
		
		int i=0;
		for( Sixi3Bone b : bones ) {
			double v = list[i++];
			double t = b.getTheta(); 
			if( t != v ) {
				b.setAngleWRTLimits(v);
				if( t != b.getTheta() )
					changed=true;
			}
		}

		if(changed) {
			// theta values actually changed so update matrixes and get the new end effector position.
			Matrix4d eeOld = new Matrix4d();
			getEndEffector(eeOld);
			for( Sixi3Bone b : bones ) {
				b.updateMatrix();
			}
			Matrix4d eeNew = new Matrix4d();
			getEndEffector(eeNew);
			notifyPropertyChangeListeners(new PropertyChangeEvent(this,"ee",eeOld,eeNew));
		}
		
		return changed;
	}
	
	@Override
	public ArrayList<Cuboid> getCuboidList() {
		ArrayList<Cuboid> list = new ArrayList<Cuboid>();

		// current pose starts as root pose
		Matrix4d currentBonePose = getPoseWorld();
		Matrix4d cuboidAfter = new Matrix4d();

		list.addAll(base.getCuboidList());
		for(Cuboid c : list) {
			// add current pose to cube pose and save that as cube pose.
			cuboidAfter.mul(currentBonePose,base.getPose());
			c.setPose(cuboidAfter);
		}
		
		for( Sixi3Bone bone : bones ) {
			// add bone to current pose
			currentBonePose.mul(bone.getPose());
			
			ArrayList<Cuboid> list2 = bone.shape.getCuboidList();
			for(Cuboid c : list2) {
				Cuboid nc = new Cuboid();
				nc.set(c);
				// add current pose to cube pose and save that as cube pose.
				cuboidAfter.mul(currentBonePose,bone.shape.getPose());
				nc.setPose(cuboidAfter);
				list.add(nc);
			}
		}
		
		return list;
	}
	
	/**
	 * @return true if this robot currently collide with itself.
	 */
	public boolean collidesWithSelf() {
		ArrayList<Cuboid> list = getCuboidList();
		
		for(int i=0;i<list.size();++i) {
			Cuboid a = list.get(i);
			for(int j=i+2;j<list.size();++j) {
				Cuboid b = list.get(j);
				if(IntersectionHelper.cuboidCuboid(a, b)) {
					Matrix4d ma = new Matrix4d();
					Matrix4d mb = new Matrix4d();
					a.getPose(ma);
					b.getPose(mb);
					Mesh sa = a.getShape();
					Mesh sb = b.getShape();
					
					return IntersectionHelper.meshMesh(ma,sa,mb,sb);
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		String angles = "";
		String add="";
		for( Sixi3Bone b : bones ) {
			angles += add + Double.toString(b.theta);
			add=",";
		}
		return "Sixi3FK {"+angles+"}";
	}

	public boolean fromString(String s) {
		final String header="Sixi3FK {";
		if(!s.startsWith(header )) return false;
		
		// strip "Sixi3FK {" and "}" from either end.
		s=s.substring(header.length(),s.length()-1);
		// split by comma
		String [] pieces = s.split(",");
		if(pieces.length != bones.size()) return false;
		
		double [] v = new double[bones.size()];
		for(int i=0; i<bones.size(); ++i) {
			v[i] = Double.parseDouble(pieces[i]);
		}
		
		setFKValues(v);
		updateSliders();
		
		return true;
	}
	
	/**
	 * Given the current pose of the robot, find the approximate jacobian, which
	 * describe the relationship between joint velocity and cartesian velocity.
	 * @See <a href='https://robotacademy.net.au/masterclass/velocity-kinematics-in-3d/?lesson=346'>Robot Academy tutorial</a>
	 * @param jacobian 
	 * 	a 6x6 matrix that will be filled with the jacobian.  
	 *  The first three columns are translation component. 
	 *  The last three columns are the rotation component.
	 */
	public void getApproximateJacobian(double [][] jacobian) {
		double ANGLE_STEP_SIZE_DEGREES=0.001;  // degrees
		
		double [] oldAngles = getFKValues();
		double [] newAngles = new double[oldAngles.length];
		
		Matrix4d T = new Matrix4d();
		Matrix4d Tnew = new Matrix4d();
		getEndEffector(T);
		
		// for all adjustable joints
		for(int i=0;i<bones.size();++i) {
			// use anglesB to get the hand matrix after a tiny adjustment on one joint.
			for(int j=0;j<bones.size();++j) {
				newAngles[j]=oldAngles[j];
			}
			newAngles[i]+=ANGLE_STEP_SIZE_DEGREES;
			setFKValues(newAngles);
			
			// Tnew will be different from T because of the changes in setPoseFK().
			getEndEffector(Tnew);
			
			// use the finite difference in the two matrixes
			// aka the approximate the rate of change (aka the integral, aka the velocity)
			// in one column of the jacobian matrix at this position.
			Matrix4d dT = new Matrix4d();
			dT.sub(Tnew,T);
			dT.mul(1.0/Math.toRadians(ANGLE_STEP_SIZE_DEGREES));
			
			jacobian[i][0]=dT.m03;
			jacobian[i][1]=dT.m13;
			jacobian[i][2]=dT.m23;

			// find the rotation part
			// these initialT and initialTd were found in the comments on
			// https://robotacademy.net.au/masterclass/velocity-kinematics-in-3d/?lesson=346
			// and used to confirm that our skew-symmetric matrix match theirs.
			/*
			double[] initialT = {
					 0,  0   , 1   ,  0.5963,
					 0,  1   , 0   , -0.1501,
					-1,  0   , 0   , -0.01435,
					 0,  0   , 0   ,  1 };
			double[] initialTd = {
					 0, -0.01, 1   ,  0.5978,
					 0,  1   , 0.01, -0.1441,
					-1,  0   , 0   , -0.01435,
					 0,  0   , 0   ,  1 };
			T.set(initialT);
			Td.set(initialTd);
			dT.sub(Td,T);
			dT.mul(1.0/Math.toRadians(ANGLE_STEP_SIZE_DEGREES));//*/
			
			//Log.message("T="+T);
			//Log.message("Td="+Td);
			//Log.message("dT="+dT);
			Matrix3d T3 = new Matrix3d(
					T.m00,T.m01,T.m02,
					T.m10,T.m11,T.m12,
					T.m20,T.m21,T.m22);
			//Log.message("R="+R);
			Matrix3d dT3 = new Matrix3d(
					dT.m00,dT.m01,dT.m02,
					dT.m10,dT.m11,dT.m12,
					dT.m20,dT.m21,dT.m22);
			//Log.message("dT3="+dT3);
			Matrix3d skewSymmetric = new Matrix3d();
			
			T3.transpose();  // inverse of a rotation matrix is its transpose
			skewSymmetric.mul(dT3,T3);
			
			//Log.message("SS="+skewSymmetric);
			//[  0 -Wz  Wy]
			//[ Wz   0 -Wx]
			//[-Wy  Wx   0]
			
			jacobian[i][3]=skewSymmetric.m12;
			jacobian[i][4]=skewSymmetric.m20;
			jacobian[i][5]=skewSymmetric.m01;
		}
		
		// undo our changes.
		setFKValues(oldAngles);
	}
	
	/**
	 * @param jacobian
	 *		the 6x6 jacobian matrix.
	 * @param jointVelocity 
	 * 		joint velocity in degrees.  
	 * @param cartesianVelocity 
	 * 		6 doubles - the XYZ translation and UVW rotation forces on the end effector.
	 *		Will be filled with new values
	 */
	public void getCartesianFromJoint(final double [][] jacobian, final double [] jointVelocity, double [] cartesianVelocity) {
		// vector-matrix multiplication (y = x^T A)
		int j,k;
		double sum;
		for(j = 0; j < 6; ++j) {
			sum=0;
			for(k = 0; k < 6; ++k) {
				sum += jacobian[k][j] * Math.toRadians(jointVelocity[k]);
			}
			cartesianVelocity[j] = sum;
		}
	}
	
	/**
	 * Use the jacobian to get the joint velocity from the cartesian velocity.
	 * @param jacobian
	 *		the 6x6 jacobian matrix.
	 * @param cartesianVelocity 
	 * 		6 doubles - the XYZ translation and UVW rotation forces on the end effector.
	 * @param jointVelocity 
	 * 		joint velocity in degrees.  Will be filled with the new velocity.
	 * @return false if joint velocities have NaN values
	 */
	public boolean getJointFromCartesian(final double [][] jacobian, final double[] cartesianVelocity,double [] jointVelocity) {
		double[][] inverseJacobian = MatrixHelper.invert(jacobian);
		
		// vector-matrix multiplication (y = x^T A)
		int j,k;
		double sum;
		for(j = 0; j < 6; ++j) {
			sum=0;
			for(k = 0; k < 6; ++k) {
				sum += inverseJacobian[k][j] * cartesianVelocity[k];
			}
			if(Double.isNaN(sum)) return false;
			jointVelocity[j] = Math.toDegrees(sum);
		}
		
		return true;
	}
	
	/**
	 * Use Quaternions to interpolate between two matrixes and estimate the velocity needed to
	 * travel the distance (both linear and rotational) in the desired time.
	 * @param mStart 
	 * 	matrix of start pose
	 * @param mEnd 
	 * 	matrix of end pose
	 * @param cartesianDistance
	 *  6 doubles that will be filled with the XYZ translation and UVW rotation.
	 */
	public void getCartesianBetweenTwoMatrixes(final Matrix4d mStart,final Matrix4d mEnd,double[] cartesianDistance) {
		Vector3d p0 = new Vector3d();
		Vector3d p1 = new Vector3d();
		Vector3d dp = new Vector3d();
		mStart.get(p0);
		mEnd.get(p1);
		dp.sub(p1,p0);

		// get the 3x3 part of the matrixes
		Matrix3d mStart3 = new Matrix3d();
		Matrix3d mEnd3 = new Matrix3d();
		mStart.set(mStart3);
		mEnd.set(mEnd3);
		// then use the 3x3 to get the quaternions
		Quat4d q0 = new Quat4d();
		Quat4d q1 = new Quat4d();
		q0.set(mStart3);
		q1.set(mEnd3);
		// then get the difference in quaternions.  diff * q0 = q1 --> diff = q1 * invert(q0)
		Quat4d qDiff = new Quat4d();
		qDiff.mulInverse(q1,q0);
		
		// get the radian roll/pitch/yaw
		double [] rpy = MathHelper.quatToEuler(qDiff);
		
		cartesianDistance[0]=dp.x;
		cartesianDistance[1]=dp.y;
		cartesianDistance[2]=dp.z;
		cartesianDistance[3]=-rpy[0];
		cartesianDistance[4]=-rpy[1];
		cartesianDistance[5]=-rpy[2];
	}


	public int getNumBones() {
		return bones.size();
	}
}
