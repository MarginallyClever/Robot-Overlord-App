package com.marginallyclever.robotOverlord.entity;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.entity.scene.shapeEntity.ShapeEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * Simulation of a Sixi3 robot arm.
 * Registered in {@code com.marginallyclever.robotOverlord.entity.Entity}
 * @see <a href='https://en.wikipedia.org/wiki/Denavit%E2%80%93Hartenberg_parameters'>DH parameters</a>
 * @author Dan Royer
 * @since 2020-02-24
 *
 */
public class Sixi3 extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2436924907127292890L;

	// measurements from Fusion360 model.
	private static final double BASE_HEIGHT=8.0;
	private static final double LENGTH_A=2.95;
	private static final double LENGTH_B=8.2564;
	private static final double CONNECTOR_HEIGHT=0.7;
	private static final double HAND_HEIGHT=1.25;
	
	private static final String ACTUATOR_MODEL   = "/Sixi3/actuator 2021-02-25.obj";
	private static final String HAND_MODEL       = "/Sixi3/Sixi3 Hand DIN EN ISO 9409-1-50-4-M6 v6.obj";
	private static final String BASE_MODEL       = "/Sixi3/base v7.obj";
	private static final String ACTUATOR_TEXTURE = "/Sixi3/actuator-texture.png";
	
	// math representation.
	private class Sixi3Link {
		// D-H parameters combine to make this matrix which is relative to the parent.
		public Matrix4d pose = new Matrix4d();
		// length (mm) along previous Z to the common normal
		public double d;
		// angle (degrees) about previous Z, from old X to new X
		public double theta;
		// length (mm) of the common normal. Assuming a revolute joint, this is the radius about previous Z
		public double r;
		// angle (degrees) about common normal, from old Z axis to new Z axis
		public double alpha;

		// model and relative offset from DH origin
		public ShapeEntity shape;
		public Matrix4d shapeOffset = new Matrix4d();
		
		public Sixi3Link() {
			shapeOffset.setIdentity();
		}
		
		public void set(double rr,double dd,double aa,double tt,String shapeFilename) {
			d=dd;
			r=rr;
			alpha=aa;
			theta=tt;
			shape = new ShapeEntity(shapeFilename);
		}
		
		public void updateMatrix() {
			assert(!Double.isNaN(theta));
			assert(!Double.isNaN(alpha));
			assert(!Double.isNaN(r));
			assert(!Double.isNaN(d));
			double ct = Math.cos(Math.toRadians(theta));
			double ca = Math.cos(Math.toRadians(alpha));
			double st = Math.sin(Math.toRadians(theta));
			double sa = Math.sin(Math.toRadians(alpha));
			
			pose.m00 = ct;		pose.m01 = -st*ca;		pose.m02 = st*sa;		pose.m03 = r*ct;
			pose.m10 = st;		pose.m11 = ct*ca;		pose.m12 = -ct*sa;		pose.m13 = r*st;
			pose.m20 = 0;		pose.m21 = sa;			pose.m22 = ca;			pose.m23 = d;
			pose.m30 = 0;		pose.m31 = 0;			pose.m32 = 0;			pose.m33 = 1;
		}
	};
	
	private Sixi3Link [] links = new Sixi3Link[6];

	// unmoving model of the robot base.
	private ShapeEntity base;

	// end effector
	private Matrix4d ee = new Matrix4d();

	// forward kinematics sliders
	private DoubleEntity J0 = new DoubleEntity("J0",0);
	private DoubleEntity J1 = new DoubleEntity("J1",0);
	private DoubleEntity J2 = new DoubleEntity("J2",0);
	private DoubleEntity J3 = new DoubleEntity("J3",0);
	private DoubleEntity J4 = new DoubleEntity("J4",0);
	private DoubleEntity J5 = new DoubleEntity("J5",0);

	// axis for ik
	static private final String [] axisLabels = new String[] { "X","Y","Z","Xr","Yr","Zr"};
	// which axis do we want to move?
	private IntEntity axisChoice = new IntEntity("Jog direction",0);
	// how fast do we want to move?
	private DoubleEntity axisAmount = new DoubleEntity("Jog speed",0);
	// target end effector pose
	private PoseEntity ee2 = new PoseEntity();
	private boolean applyingIK;

	// how big a step to take with each partial descent?
	private double [] samplingDistances = { 0,0,0,0,0,0 };
	
	
	public Sixi3() {
		super();
		setName("Sixi3");

		addChild(ee2);
		
		setupModel();
		
		J0.addPropertyChangeListener(this);
		J1.addPropertyChangeListener(this);
		J2.addPropertyChangeListener(this);
		J3.addPropertyChangeListener(this);
		J4.addPropertyChangeListener(this);
		J5.addPropertyChangeListener(this);
		axisChoice.addPropertyChangeListener(this);
		ee2.addPropertyChangeListener(this);
		
		J0.set(180.0);
		J1.set(180.0);
		J2.set(180.0);
		J3.set(180.0);
		J4.set(180.0);
		J5.set(180.0);
		
		applyingIK=true;

		for( Sixi3Link bone : links ) {
			bone.updateMatrix();
		}
		
		getEndEffector(ee);
		ee2.setPose(ee);
		
		applyingIK=false;
	}

	/**
	 * Set up the DH link hierarchy acording to the DH parameters.
	 * Also load the shapes.
	 * Also the physical location of the shapes does not match the DH linkage description of the robot, 
	 * so adjust the {@link Sixi3Link.shapeOffset} of each bone.
	 */
	private void setupModel() {
		// memory allocation
		for( int i=0;i<links.length;++i ) {
			links[i] = new Sixi3Link();
		}
		
		// The DH parameters
		double d0=BASE_HEIGHT+LENGTH_A;
		double r2=(LENGTH_B+CONNECTOR_HEIGHT)*2;
		double d3=LENGTH_A+LENGTH_B;
		double d4=d3;
		double d5=LENGTH_A+HAND_HEIGHT;
		links[0].set(0 ,d0,-90,0,ACTUATOR_MODEL);
		links[1].set(r2, 0,  0,0,ACTUATOR_MODEL);
		links[2].set(0 , 0, 90,0,ACTUATOR_MODEL);
		links[3].set(0 ,d3, 90,0,ACTUATOR_MODEL);
		links[4].set(0 ,d4, 90,0,ACTUATOR_MODEL);
		links[5].set(0 ,d5,  0,0,HAND_MODEL    );

		// load the base shape.
		base = new ShapeEntity(BASE_MODEL);
		
		// adjust the shape offsets.
		Matrix4d m0 = new Matrix4d();
		Matrix4d m1 = new Matrix4d();
		Matrix4d m2 = new Matrix4d();

		m0.rotX(Math.toRadians(90));
		m1.rotY(Math.toRadians(-90));
		m2.mul(m1,m0);
		links[0].shapeOffset.set(m2);
		links[0].shapeOffset.m13=LENGTH_A;

		links[1].shapeOffset.setIdentity();
		links[1].shapeOffset.m03=-(LENGTH_B+CONNECTOR_HEIGHT)*2;
		links[1].shapeOffset.m23=LENGTH_B;
		
		m0.rotX(Math.toRadians(180));
		m1.rotZ(Math.toRadians(-90));
		m2.mul(m1,m0);
		links[2].shapeOffset.set(m2);
		links[2].shapeOffset.m23=LENGTH_A;

		m0.rotX(Math.toRadians(180));
		m1.rotZ(Math.toRadians(-90));
		m2.mul(m1,m0);
		links[3].shapeOffset.set(m2);
		links[3].shapeOffset.m23=LENGTH_A;

		m0.rotZ(Math.toRadians(90));
		m1.rotX(Math.toRadians(180));
		m2.mul(m1,m0);
		links[4].shapeOffset.set(m2);
		links[4].shapeOffset.m23=LENGTH_A;

		links[5].shapeOffset.rotZ(Math.toRadians(-90));
		links[5].shapeOffset.m23=0;
		
		// set material properties for each part of this model.
		base.getMaterial().setTextureFilename(ACTUATOR_TEXTURE);
		for( Sixi3Link bone : links ) {
			bone.shape.getMaterial().setTextureFilename(ACTUATOR_TEXTURE);
		}
	}
	
	@Override
	public void update(double dt) {
		super.update(dt);

		Matrix4d m0=new Matrix4d();
		Matrix4d m1=new Matrix4d();
		Matrix4d m2=new Matrix4d();
		m0.rotX(Math.toRadians(180));
		m1.rotZ(Math.toRadians(90));
		m2.mul(m1,m0);
		links[2].shapeOffset.set(m2);
		links[2].shapeOffset.m23=LENGTH_A;
		
		// get the end effector
		getEndEffector(ee);
		
		if(axisAmount.get()!=0) {
			double aa = axisAmount.get();
			int ac = axisChoice.get();

			aa*=dt;

			Matrix4d target = ee2.getPose();
			
			Vector3d p = new Vector3d(target.m03,target.m13,target.m23);
			target.setTranslation(new Vector3d(0,0,0));
			Matrix4d r = new Matrix4d();
			r.setIdentity();
			
			switch(ac) {
			case 0:				p.x+=aa;					break;
			case 1:				p.y+=aa;					break;
			case 2:				p.z+=aa;					break;
			case 3:				r.rotX(Math.toRadians(aa));	break;
			case 4:				r.rotY(Math.toRadians(aa));	break;
			case 5:				r.rotZ(Math.toRadians(aa));	break;
			default: 										break;
			}
			target.mul(r);
			target.setTranslation(p);
			ee2.setPose(target);
			// which will cause a propertyChange event
			
			axisAmount.set(0.0);
		}
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, pose);
		
			gl2.glPushMatrix();
				// draw the meshes
				drawMeshes(gl2);
				drawExtras(gl2);
			gl2.glPopMatrix();
		
			MatrixHelper.drawMatrix2(gl2, ee, 6);
		gl2.glPopMatrix();
		
		super.render(gl2);
	}
	
	private void drawMeshes(GL2 gl2) {
		base.render(gl2);

		gl2.glPushMatrix();
		{
			Sixi3Link bone = links[0];
			// draw model with local shape offset
			bone.updateMatrix();
			MatrixHelper.applyMatrix(gl2, bone.pose);
			
			gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, bone.shapeOffset);
			bone.shape.render(gl2);
			gl2.glPopMatrix();
		}
		//1-2
		{
			Sixi3Link bone = links[1];
			// draw model with local shape offset
			bone.updateMatrix();
			MatrixHelper.applyMatrix(gl2, bone.pose);
			
			gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, bone.shapeOffset);
			bone.shape.render(gl2);
			// special only for bone 2
			gl2.glTranslated((LENGTH_B+CONNECTOR_HEIGHT)*2,0,0);
			gl2.glRotated(180, 0, 0, 1);
			bone.shape.render(gl2);
			gl2.glPopMatrix();
		}
		// 3
		{
			Sixi3Link bone = links[2];
			// draw model with local shape offset
			bone.updateMatrix();
			MatrixHelper.applyMatrix(gl2, bone.pose);
			
			gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, bone.shapeOffset);
			bone.shape.render(gl2);
			gl2.glPopMatrix();
		}
		// 4
		{
			Sixi3Link bone = links[3];
			// draw model with local shape offset
			bone.updateMatrix();
			MatrixHelper.applyMatrix(gl2, bone.pose);

			gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, bone.shapeOffset);
			bone.shape.render(gl2);
			gl2.glPopMatrix();
		}
		// 5
		{
			Sixi3Link bone = links[4];
			// draw model with local shape offset
			bone.updateMatrix();
			MatrixHelper.applyMatrix(gl2, bone.pose);

			gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, bone.shapeOffset);
			bone.shape.render(gl2);
			gl2.glPopMatrix();
		}
		// 6
		{
			Sixi3Link bone = links[5];
			// draw model with local shape offset
			bone.updateMatrix();
			MatrixHelper.applyMatrix(gl2, bone.pose);

			gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, bone.shapeOffset);
			bone.shape.render(gl2);
			gl2.glPopMatrix();
		}
		gl2.glPopMatrix();
	}
	
	private void drawExtras(GL2 gl2) {
		Vector3d v = new Vector3d();
		
		// turn of textures so lines draw good
		boolean wasTex = gl2.glIsEnabled(GL2.GL_TEXTURE_2D);
		gl2.glDisable(GL2.GL_TEXTURE_2D);
		// turn off lighting so lines draw good
		boolean wasLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_LIGHTING);
		// draw on top of everything else
		int wasOver=OpenGLHelper.drawAtopEverythingStart(gl2);
		gl2.glPushMatrix();
		if(showLineage.get()) {
			// then the bones, overtop and unlit.
			int j=links.length+1;
			for(int i=0;i<links.length;++i) {
				Sixi3Link bone = links[i];
				bone.updateMatrix();
				PrimitiveSolids.drawStar(gl2,j--);
	
				bone.pose.get(v);
				gl2.glColor3d(1, 1, 1);
				gl2.glBegin(GL2.GL_LINES);
				gl2.glVertex3d(0, 0, 0);
				gl2.glVertex3d(v.x,v.y,v.z);
				gl2.glEnd();
				// draw origin of next bone
				MatrixHelper.applyMatrix(gl2, bone.pose);
			}
		}
		if(showBoundingBox.get()) {
			ArrayList<Cuboid> list = getCuboidList();
			for(Cuboid c : list) {
				c.render(gl2);
			}
		}
		gl2.glPopMatrix();

		// return state if needed
		OpenGLHelper.drawAtopEverythingEnd(gl2,wasOver);
		if(wasLit) gl2.glEnable(GL2.GL_LIGHTING);
		if(wasTex) gl2.glEnable(GL2.GL_TEXTURE_2D);	
	}
	
	/**
	 * Find the current end effector pose, relative to the base of this robot
	 * @param m where to store the end effector pose.
	 */
	private void getEndEffector(Matrix4d m) {
		m.setIdentity();
		for( Sixi3Link bone : links ) {
			m.mul(bone.pose);
		}
	}
	
	/**
	 * Measures the difference beween the latest end effector matrix and the target matrix
	 * @return
	 */
	private double gradientDescentErrorTerm() {
		// Scale the "handles" used.  Bigger scale, greater rotation compensation.
		final double GRADIENT_DESCENT_ERROR_TERM_ROTATION_SCALE = 100;
		
		Matrix4d m = new Matrix4d();
		getEndEffector(m);
		Matrix4d target = ee2.getPose();
		
		// linear difference in centers
		Vector3d c0 = new Vector3d();
		Vector3d c1 = new Vector3d();
		m.get(c0);
		target.get(c1);
		c1.sub(c0);
		double dC = c1.lengthSquared();
		
		// linear difference in X handles
		Vector3d x0 = MatrixHelper.getXAxis(target);
		Vector3d x1 = MatrixHelper.getXAxis(m);
		x1.scale(GRADIENT_DESCENT_ERROR_TERM_ROTATION_SCALE);
		x0.scale(GRADIENT_DESCENT_ERROR_TERM_ROTATION_SCALE);
		x1.sub(x0);
		double dX = x1.lengthSquared();
		
		// linear difference in Y handles
		Vector3d y0 = MatrixHelper.getYAxis(target);
		Vector3d y1 = MatrixHelper.getYAxis(m);
		y1.scale(GRADIENT_DESCENT_ERROR_TERM_ROTATION_SCALE);
		y0.scale(GRADIENT_DESCENT_ERROR_TERM_ROTATION_SCALE);
		y1.sub(y0);
		double dY = y1.lengthSquared();		

	    // now sum these to get the error term.
		return dC+dX+dY;
	}
	
	private double partialGradientDescent(int i) {
		Sixi3Link bone = links[i];
		// get the current error term F.
		double oldValue = bone.theta;
		double Fx = gradientDescentErrorTerm();

		// move F+D, measure again.
		bone.theta = oldValue + samplingDistances[i];
		bone.updateMatrix();
		double FxPlusD = gradientDescentErrorTerm();

		// move F-D, measure again.
		bone.theta = oldValue - samplingDistances[i];
		bone.updateMatrix();
		double FxMinusD = gradientDescentErrorTerm();

		// restore the old value
		bone.theta = oldValue;
		bone.updateMatrix();

		// if F+D and F-D have more error than F, try smaller step size next time. 
		if( FxMinusD > Fx && FxPlusD > Fx ) {
			// If we somehow are *exactly* fit then Fx is zero and /0 is bad.
			if( Fx != 0 ) {
				samplingDistances[i] *= Math.min(FxMinusD, FxPlusD) / Fx;
			}
			return 0;
		}
		
		double gradient = ( FxPlusD - Fx ) / samplingDistances[i];
		return gradient;
	}

	/**
	 * Use gradient descent to move the end effector closer to the target. 
	 * @return true if the margin of error is within the threshold.
	 */
	private boolean gradientDescent() {
		// How many times should I try to get closer?
		final int iterations = 50;
		
		// When distanceToTarget() score is within threshold then stop. 
		final double threshold = 0.001;
		
		// how much of that partial descent to actually apply?
		double learningRate=0.125;
		
		// the sensor resolution is 2^15 (32768 bits, 0.011 degree)
		final double initialSampleSize = 0.005;
		samplingDistances[0]=initialSampleSize; 
		samplingDistances[1]=initialSampleSize;
		samplingDistances[2]=initialSampleSize;
		samplingDistances[3]=initialSampleSize;
		samplingDistances[4]=initialSampleSize;
		samplingDistances[5]=initialSampleSize;
		
		double dtt=gradientDescentErrorTerm();
		if(dtt<threshold) return true;

		for(int j=0;j<iterations;++j) {
			// seems to work better descending from the finger than ascending from the base.
			//for( int i=0; i<links.length; ++i ) {  // ascending mode
			for( int i=links.length-1; i>=0; --i ) {  // descending mode
				Sixi3Link bone = links[i];

				double oldValue = bone.theta;
				double gradient = partialGradientDescent(i);
				double newValue = oldValue - gradient * learningRate; 
				// cap the value to something sane
				bone.theta = Math.max(Math.min(newValue, 350-1e-6), 10+1e-6);
				bone.updateMatrix();
		
				dtt=gradientDescentErrorTerm();
				if(dtt<threshold) return true;
			}
		}
		
		// Probably always false?
		return dtt<threshold;
	}
	
	/**
	 * When GUI elements are changed they each cause a {@link PropertyChangeEvent}.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		Object src = evt.getSource();

		if(src == axisChoice) {
			axisAmount.set(0.0);
		}
		if(src == ee2 && evt.getPropertyName().contentEquals("pose")) {
			// gradient descent towards ee2
			gradientDescent();

			if(!applyingIK) {
				applyingIK=true;
				J0.set(links[0].theta);
				J1.set(links[1].theta);
				J2.set(links[2].theta);
				J3.set(links[3].theta);
				J4.set(links[4].theta);
				J5.set(links[5].theta);
				applyingIK=false;
			}
		} else {
			if(!applyingIK) {
				applyingIK=true;
				axisAmount.set(0.0);
				if(src == J0) links[0].theta=J0.get();
				if(src == J1) links[1].theta=J1.get();
				if(src == J2) links[2].theta=J2.get();
				if(src == J3) links[3].theta=J3.get();
				if(src == J4) links[4].theta=J4.get();
				if(src == J5) links[5].theta=J5.get();
				getEndEffector(ee);
				ee2.setPose(ee);
				applyingIK=false;
			}
		}
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("K","Kinematics");
		view.addRange(J0, 350, 10);
		view.addRange(J1, 350, 10);
		view.addRange(J2, 350, 10);
		view.addRange(J3, 350, 10);
		view.addRange(J4, 350, 10);
		view.addRange(J5, 350, 10);

		view.addComboBox(axisChoice, axisLabels);
		view.addRange(axisAmount, 5, -5);
		view.popStack();
		
		super.getView(view);
	}
	
	@Override
	public ArrayList<Cuboid> getCuboidList() {
		ArrayList<Cuboid> list = super.getCuboidList();
		for( Sixi3Link bone : links ) {
			list.addAll(bone.shape.getCuboidList());
		}
		list.addAll(base.getCuboidList());
		return list;
	}
}
