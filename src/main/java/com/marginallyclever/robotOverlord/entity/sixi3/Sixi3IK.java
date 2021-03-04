package com.marginallyclever.robotOverlord.entity.sixi3;

import java.beans.PropertyChangeEvent;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * {@link Sixi3IK} is a {@link Sixi3FK} with added Inverse Kinematics.  
 * Registered in {@code com.marginallyclever.robotOverlord.entity.Entity}
 * @see <a href='https://en.wikipedia.org/wiki/Inverse_kinematics'>Inverse Kinematics</a>
 * @author Dan Royer
 * @since 2021-02-24
 *
 */
public class Sixi3IK extends Sixi3FK {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7778520191789995554L;

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
	private double [] samplingDistances;
	
	public Sixi3IK() {
		super();
		setName("Sixi 3");

		samplingDistances = new double[links.length];
		
		axisChoice.addPropertyChangeListener(this);

		addChild(ee2);
		ee2.addPropertyChangeListener(this);
		
		applyingIK=true;
		ee2.setPose(ee);
		
		applyingIK=false;
	}
	
	@Override
	public void update(double dt) {
		super.update(dt);
		
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
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("IK","Inverse Kinematics");
		view.addComboBox(axisChoice, axisLabels);
		view.addRange(axisAmount, 5, -5);
		view.popStack();
		
		super.getView(view);
	}
	
	/**
	 * When GUI elements are changed they each cause a {@link PropertyChangeEvent}.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		Object src = evt.getSource();

		if(src == axisChoice) {
			if(axisAmount != null) axisAmount.set(0.0);
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
				if(axisAmount != null) axisAmount.set(0.0);
				if(src == J0) links[0].theta=J0.get();
				if(src == J1) links[1].theta=J1.get();
				if(src == J2) links[2].theta=J2.get();
				if(src == J3) links[3].theta=J3.get();
				if(src == J4) links[4].theta=J4.get();
				if(src == J5) links[5].theta=J5.get();
				getEndEffector(ee);
				if(ee2 != null) ee2.setPose(ee);
				applyingIK=false;
			}
		}
	}
}
