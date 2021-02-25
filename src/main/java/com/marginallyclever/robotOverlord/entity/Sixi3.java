package com.marginallyclever.robotOverlord.entity;

import java.beans.PropertyChangeEvent;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.MaterialEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.entity.scene.shapeEntity.Shape;
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

	private static final double BASE_HEIGHT=8.0;
	private static final double LENGTH_A=2.95;
	private static final double LENGTH_B=8.2564;
	private static final double CONNECTOR_HEIGHT=0.7;
	private static final double HAND_HEIGHT=1.25;
	
	private static final String FILE_ACTUATOR = "C:\\Users\\aggra\\Desktop\\actuator 2020-feb\\actuator 2021-02-24-02.stl";
	private static final String FILE_HAND = "C:\\Users\\aggra\\Desktop\\actuator 2020-feb\\Sixi3 Hand DIN EN ISO 9409-1-50-4-M6 v6.stl";
	private static final String FILE_BASE = "C:\\Users\\aggra\\Desktop\\actuator 2020-feb\\base v7.stl";

	// math shape of robot
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
		public Shape shape = new Shape();
		public Matrix4d shapeOffset = new Matrix4d();
		
		public Sixi3Link() {
			shapeOffset.setIdentity();
		}
		
		public void set(double rr,double dd,double aa,double tt,String shapeFilename) {
			d=dd;
			r=rr;
			alpha=aa;
			theta=tt;
			try {
				shape = ShapeEntity.createModelFromFilename(shapeFilename);
			} catch(Exception e) {
				System.out.println("Sixi3Link cannot load '"+shapeFilename+"': "+e.getLocalizedMessage());
				e.printStackTrace();
			}
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
	private Shape base;

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
	private IntEntity axisChoice = new IntEntity("axisChoice",0);
	// axis amount for ik
	private DoubleEntity axisAmount = new DoubleEntity("axisAmount",0);
	// target end effector pose
	private Matrix4d ee2 = new Matrix4d();
	private boolean applyingIK;
	

	// For the sixi robot arm, the max reach is 800mm and the sensor resolution is 2^15 (32768 bits, 0.011 degree)
	private static final double SENSOR_RESOLUTION = 360.0/Math.pow(2,15);
	private static final int ITERATIONS = 30;
	// Scale the "handles" used in distanceToTarget().  Bigger scale, greater rotation compensation
	private static final double CORRECTIVE_FACTOR = 100;
	// If distanceToTarget() score is within threshold, quit with success. 
	private static final double THRESHOLD = 0.1;
	// how big a step to take with each partial descent?
	private double [] samplingDistances = { 0,0,0,0,0,0 };
	// how much of that partial descent to actually apply?
	private double learningRate=0;
	
	
	public Sixi3() {
		super();
		setName("Sixi3");

		setupModel();
		
		J0.addPropertyChangeListener(this);
		J1.addPropertyChangeListener(this);
		J2.addPropertyChangeListener(this);
		J3.addPropertyChangeListener(this);
		J4.addPropertyChangeListener(this);
		J5.addPropertyChangeListener(this);
		axisChoice.addPropertyChangeListener(this);
		
		J0.set(180.0);
		J1.set(180.0);
		J2.set(180.0);
		J3.set(180.0);
		J4.set(180.0);
		J5.set(180.0);
		
		applyingIK=false;

		getEndEffector(ee);
		ee2.set(ee);
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
		links[0].set(0 ,d0,-90,0,FILE_ACTUATOR);
		links[1].set(r2, 0,  0,0,FILE_ACTUATOR);
		links[2].set(0 , 0,-90,0,FILE_ACTUATOR);
		links[3].set(0 ,d3,-90,0,FILE_ACTUATOR);
		links[4].set(0 ,d4, 90,0,FILE_ACTUATOR);
		links[5].set(0 ,d5,  0,0,FILE_HAND);

		// load the base shape.
		try {
			base = ShapeEntity.createModelFromFilename(FILE_BASE);
		} catch(Exception e) {
			System.out.println("Sixi3Link cannot load '"+FILE_BASE+"': "+e.getLocalizedMessage());
			e.printStackTrace();
		}
		
		// the models are 10x too big.
		base.adjustScale(0.1);
		links[1].shape.adjustScale(0.1);
		links[5].shape.adjustScale(0.1);

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
		m1.rotZ(Math.toRadians(90));
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
	}
	
	@Override
	public void update(double dt) {
		super.update(dt);
		
		// get the end effector
		getEndEffector(ee);
		
		if(axisAmount.get()!=0) {
			double aa = axisAmount.get();
			int ac = axisChoice.get();
			System.out.println(axisLabels[ac]+"="+aa);
			aa*=dt;

			Vector3d p = new Vector3d(ee2.m03,ee2.m13,ee2.m23);
			ee2.setTranslation(new Vector3d(0,0,0));
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
			ee2.mul(r);
			ee2.setTranslation(p);
			
			// gradient descent towards ee2
			gradientDescent();
			
			applyingIK=true;
			J0.set(links[0].theta);
			J1.set(links[1].theta);
			J2.set(links[2].theta);
			J3.set(links[3].theta);
			J4.set(links[4].theta);
			J5.set(links[5].theta);
			applyingIK=false;
		}
	}
	
	private void getEndEffector(Matrix4d m) {
		m.setIdentity();
		for( Sixi3Link bone : links ) {
			m.mul(bone.pose);
		}
	}
	
	private double distanceToTarget() {
		Matrix4d m = new Matrix4d();
		getEndEffector(m);
		
		// linear difference in centers
		Vector3d c0 = new Vector3d();
		Vector3d c1 = new Vector3d();
		m.get(c0);
		ee2.get(c1);
		c1.sub(c0);
		double dC = c1.lengthSquared();
		
		// linear difference in X handles
		Vector3d x0 = MatrixHelper.getXAxis(ee2);
		Vector3d x1 = MatrixHelper.getXAxis(m);
		x1.scale(CORRECTIVE_FACTOR);
		x0.scale(CORRECTIVE_FACTOR);
		x1.sub(x0);
		double dX = x1.lengthSquared();
		
		// linear difference in Y handles
		Vector3d y0 = MatrixHelper.getYAxis(ee2);
		Vector3d y1 = MatrixHelper.getYAxis(m);
		y1.scale(CORRECTIVE_FACTOR);
		y0.scale(CORRECTIVE_FACTOR);
		y1.sub(y0);
		double dY = y1.lengthSquared();		

	    // now sum these to get the error term.
		return dC+dX+dY;
	}
	
	private double partialGradientDescent(int i) {
		Sixi3Link bone = links[i];
		double oldValue = bone.theta;
		double Fx = distanceToTarget();

		bone.theta = oldValue + samplingDistances[i];
		bone.updateMatrix();
		double FxPlusD = distanceToTarget();

		bone.theta = oldValue - samplingDistances[i];
		bone.updateMatrix();
		double FxMinusD = distanceToTarget();

		bone.theta = oldValue;
		bone.updateMatrix();

		if( FxMinusD > Fx && FxPlusD > Fx ) {
			if( Fx == 0 ) {
				samplingDistances[i] *= 2.0/3.0;
			} else {
				samplingDistances[i] *= Math.min(FxMinusD, FxPlusD) / Fx;
			}
			return 0;
		}
		
		double gradient = ( FxPlusD - Fx ) / samplingDistances[i];
		return gradient;
	}

	private void gradientDescent() {
		// these need to be reset each run.
		learningRate=0.125;
		samplingDistances[0]=SENSOR_RESOLUTION; 
		samplingDistances[1]=SENSOR_RESOLUTION;
		samplingDistances[2]=SENSOR_RESOLUTION;
		samplingDistances[3]=SENSOR_RESOLUTION;
		samplingDistances[4]=SENSOR_RESOLUTION;
		samplingDistances[5]=SENSOR_RESOLUTION;
		
		double dtt=10;
		
		for(int iter=0;iter<ITERATIONS;++iter) {
			// seems to work better ascending than descending
			//for( int i=0; i<robot.getNumLinks(); ++i ) {
			for( int i=links.length-1; i>=0; --i ) {
				Sixi3Link bone = links[i];

				double oldValue = bone.theta;
				double gradient = partialGradientDescent(i);
				double newValue = oldValue - gradient * learningRate; 
				// cap the value to something sane
				bone.theta = Math.max(Math.min(newValue, 350-1e-6), 10+1e-6);
				bone.updateMatrix();
		
				dtt=distanceToTarget();
				if(dtt<THRESHOLD) break;
			}
			if(dtt<THRESHOLD) break;
		}
	}
	
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		Object src = evt.getSource();
		
		if(!applyingIK) {
			axisAmount.set(0.0);
			if(src == J0) links[0].theta=J0.get();
			if(src == J1) links[1].theta=J1.get();
			if(src == J2) links[2].theta=J2.get();
			if(src == J3) links[3].theta=J3.get();
			if(src == J4) links[4].theta=J4.get();
			if(src == J5) links[5].theta=J5.get();
			getEndEffector(ee);
			ee2.set(ee);
		}
		if(src == axisChoice) {
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
				if(showLineage.get()) {
					// then the bones, overtop and unlit.
					drawBones(gl2);
				}
			gl2.glPopMatrix();
		
			MatrixHelper.drawMatrix2(gl2, ee, 6);
			MatrixHelper.drawMatrix(gl2, ee2, 4);
		gl2.glPopMatrix();

		
		super.render(gl2);
	}
	
	private void drawMeshes(GL2 gl2) {
		MaterialEntity mat = new MaterialEntity();
		mat.setDiffuseColor(1.0f, 0.8f, 0.0f, 1.0f);
		mat.render(gl2);
		
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
	
	private void drawBones(GL2 gl2) {
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
		gl2.glPopMatrix();

		// return state if needed
		OpenGLHelper.drawAtopEverythingEnd(gl2,wasOver);
		if(wasLit) gl2.glEnable(GL2.GL_LIGHTING);
		if(wasTex) gl2.glEnable(GL2.GL_TEXTURE_2D);	
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
}
