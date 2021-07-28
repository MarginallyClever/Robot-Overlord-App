package com.marginallyclever.robotOverlord.robots.dog;

import java.util.ArrayList;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.IntEntity;
import com.marginallyclever.robotOverlord.uiExposedTypes.MaterialEntity;

/**
 * Forward is negative Z axis.  Up is positive Y axis.  Right is positive X axis.
 * This way forward facing coordinate system matches normal video input.
 * @author Dan Royer
 * @since 1.6.0
 */
public class DogRobot extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5916361555293772951L;

	private double BODY_WIDTH = 12;
	private double BODY_LENGTH = 8;
	private double BODY_HEIGHT = 32;
	private double bodyScale = 0.99;
	private DogAnimator animator;

	
	private DogLeg [] legs = new DogLeg[4];
	private MaterialEntity mat = new MaterialEntity();
	private MaterialEntity mat2 = new MaterialEntity();
	private MaterialEntity matShadow = new MaterialEntity();
	private ArrayList<DogAnimator> animators = new ArrayList<DogAnimator>();
	
	public DogRobot() {
		super();
		setName("Dog Robot");
		
		for(int i=0;i<4;++i) legs[i] = new DogLeg();
		setDHParameters();
		setupAnimators();
		
		mat.setLit(true);
		mat2.setLit(true);
		mat2.setDiffuseColor(1,0,0,1);
		matShadow.setDiffuseColor(0, 0, 0, 0.4f);
	}
	
	private void setupAnimators() {
		addAnimator(new DogWalkZero());
		addAnimator(new DogWalkOne());
		addAnimator(new DogWalkTwo());
		
		animator = animators.get(0);
	}

	public void addAnimator(DogAnimator da) {
		animators.add(da);
		if(da instanceof Entity) addChild((Entity)da);
	}
	
	public void removeAnimator(DogAnimator da) {
		animators.remove(da);
		if(da instanceof Entity) removeChild((Entity)da);
	}

	public void setDHParameters() {
		// robot faces negative Z
		double w = BODY_WIDTH/2;
		double h = BODY_HEIGHT/2;

		int i=0;
		setDHParametersForLeg(i++, w, h);
		setDHParametersForLeg(i++,-w, h);
		setDHParametersForLeg(i++,-w,-h);
		setDHParametersForLeg(i++, w,-h);
	}
	
	private void setDHParametersForLeg(int i, double r, double d) {
		DogLeg leg = legs[i];
		leg.shoulderA.set(   r, d,  0,   0, 360, -360, "");
		leg.shoulderB.set(   0, 0, 90, -90, 360, -360, "");
		leg.elbow    .set(11.5, 0,  0, -45,   0, -180, "");
		leg.foot     .set(  13, 0,  0,  90, 360, -360, "");
		updateLegMatrixes(leg);
		leg.captureAngles(leg.idealStandingAngles);
		//leg.toeTarget2.set(leg.toe);
		//leg.toeTarget.set(leg.toe);
	}

	@Override
	public void update(double dt) {
		super.update(dt);
	}
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);

		if(animator!=null) animator.walk(this, gl2);
		
		drawCurrentDogPose(gl2);
		drawPointOnGroundUnderFeet(gl2);
		drawShadow(gl2);
	}
	
	private void drawShadow(GL2 gl2) {
		Vector3d [] p = getBoxCornersProjectedOnFloor();

		ConvexShadow shadow = new ConvexShadow();
		for( Vector3d pN : p ) shadow.add(pN);
		matShadow.setDiffuseColor(0, 0, 0, 0.4f);
		matShadow.render(gl2);
		shadow.renderAsFan(gl2);
	}

	private Vector3d[] getBoxCornersProjectedOnFloor() {
		double width = (BODY_WIDTH/2)*bodyScale;
		double height = (BODY_HEIGHT/2)*bodyScale;
		double length = (BODY_LENGTH/2)*bodyScale;
		Point3d [] p = PrimitiveSolids.get8PointsOfBox(
							new Point3d(-width,-length,-height),
							new Point3d( width, length, height));
		Vector3d [] p2 = new Vector3d[8];
		
		Matrix4d m = new Matrix4d();
		getPoseWorld(m);
		
		int i=0;
		for( Point3d pN : p ) {
			m.transform(pN);
			Vector3d pT = new Vector3d(pN);
			pT.z=0;
			p2[i++]=pT;
		}

		return p2;
	}

	public void setLegToAngles(DogLeg leg, double[] angles) {
		leg.shoulderA.setAngleWRTLimits(angles[0]);
		leg.shoulderB.setAngleWRTLimits(angles[1]);
		leg.elbow.setAngleWRTLimits(angles[2]);
		leg.foot.setAngleWRTLimits(angles[3]);
		updateLegMatrixes(leg);
	}

	private void drawCurrentDogPose(GL2 gl2) {
		mat.render(gl2);
		
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, pose);
		drawTorso(gl2);
		drawLegs(gl2);
		gl2.glPopMatrix();
	}

	private void drawTorso(GL2 gl2) {
		double width = (BODY_WIDTH/2)*bodyScale;
		double height = (BODY_HEIGHT/2)*bodyScale;
		double length = (BODY_LENGTH/2)*bodyScale;
		PrimitiveSolids.drawBox(gl2, 
			new Point3d(-width,-length,-height),
			new Point3d( width, length, height));
	}

	private void drawLegs(GL2 gl2) {
		boolean flag = OpenGLHelper.disableLightingStart(gl2);	
		for( DogLeg leg : legs ) leg.render(gl2);
		OpenGLHelper.disableLightingEnd(gl2, flag);
	}
	
	public void updateLegMatrixes(DogLeg leg) {
		leg.shoulderA.updateMatrix();
		leg.shoulderB.updateMatrix();
		leg.elbow.updateMatrix();
		leg.foot.updateMatrix();
		leg.toe.set(MatrixHelper.getPosition(getWorldMatrixOfToe(leg)));
	}

	private void drawPointOnGroundUnderFeet(GL2 gl2) {
		for( DogLeg leg : legs ) {
			Vector3d fp = leg.toe;
			
			gl2.glPushMatrix();
			gl2.glTranslated(fp.x,fp.y,0);
			PrimitiveSolids.drawSphere(gl2, 0.25);
			gl2.glPopMatrix();
		}
	}

	private Matrix4d getWorldMatrixOfToe(DogLeg leg) {
		Matrix4d m = getPose();
		m.mul(leg.shoulderA.pose);
		m.mul(leg.shoulderB.pose);
		m.mul(leg.elbow.pose);
		m.mul(leg.foot.pose);
		return m;
	}

	public void drawToeTarget(GL2 gl2) {
		for( DogLeg leg : legs ) {
			gl2.glPushMatrix();
			gl2.glTranslated(leg.toeTarget.x, leg.toeTarget.y, leg.toeTarget.z);

			if(leg.toeTarget.z<0.1) mat2.render(gl2);
			else mat.render(gl2);
			
			PrimitiveSolids.drawSphere(gl2, 0.5);
			gl2.glPopMatrix();
		}
		mat.render(gl2);
	}

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("D",getName());
		
		IntEntity animationChoice = new IntEntity("Animation style",0);
		animationChoice.addPropertyChangeListener((evt)->{
			animator = animators.get(animationChoice.get());
		});
		view.addComboBox(animationChoice, getAnimationNames());
		
		view.popStack();
		super.getView(view);
	}
	
	private String[] getAnimationNames() {
		int s = animators.size();
		if(s==0) return null;
		
		String[] list = new String[s];
		int i=0;
		for(DogAnimator d : animators) list[i++] = d.getName();
		return list;
	}

	public void updateAllLegMatrixes() {
		for(int i=0;i<4;++i) {
			updateLegMatrixes(legs[i]);
		}
	}

	public DogLeg getLeg(int i) {
		return legs[i];
	}

	public void moveToeTargetsSmoothly(double dt) {
		dt *= 0.25;
		dt = Math.max(Math.min(dt, 1), 0);

		Vector3d v = new Vector3d();
		for( DogLeg leg : legs ) {
			v.sub(leg.toeTarget2,leg.toeTarget);
			v.scale(dt);
			leg.toeTarget.add(v);
		}
	}

	// Move the toes towards the toeTargets.
	public void gradientDescent() {
		double [] legAngles = new double[4];
		double EPSILON = 0.001;
		
		for( DogLeg leg : legs ) {
			legAngles[0] = leg.shoulderA.theta;
			legAngles[1] = leg.shoulderB.theta;
			legAngles[2] = leg.elbow.theta;
			legAngles[3] = leg.foot.theta;
			
			double stepSize=10;
			for(int tries=0;tries<15;++tries) {
				int i;
				// work from toe to shoulder, seems to finish faster than shoulder to toe.
				for(i=legAngles.length-1;i>=0;--i) {
					if(partialDescent(leg,legAngles,i,stepSize)<EPSILON) break;
				}
				if(i>0) break;
				
				stepSize*=0.75;
			}
			setLegToAngles(leg,legAngles);
		}
	}

	// Wiggle leg joint 'i' to see which way gets a better score.
	private double partialDescent(DogLeg leg, double[] legAngles,int i,double stepSize) {
		double startAngle = legAngles[i];
		double bestAngle = startAngle;
		double startScore = scoreLeg(leg,legAngles);
		double bestScore = startScore;

		legAngles[i] = startAngle-stepSize;
		double scoreNeg = scoreLeg(leg,legAngles);
		if(bestScore>scoreNeg) {
			bestScore = scoreNeg;
			bestAngle = legAngles[i];
		}
		
		legAngles[i] = startAngle+stepSize;
		double scorePos = scoreLeg(leg,legAngles);
		if(bestScore>scorePos) {
			bestScore = scorePos;
			bestAngle = legAngles[i];
		}
		
		legAngles[i] = bestAngle;
		return bestScore;
	}

	// @return the distance from the {@code DogLeg.toe} to the {@code DogLeg.toeTarget}
	private double scoreLeg(DogLeg leg,double [] angles) {
		setLegToAngles(leg,angles);
		Vector3d fp = new Vector3d();
		fp.sub(leg.toe,leg.toeTarget);
		return fp.lengthSquared();
	}
	
	public void pushBody(Vector3d linearForce,double zTorque) {
		// move the body to match the feet
		Matrix4d wp = new Matrix4d();
		Matrix4d wp2 = new Matrix4d();
		getPoseWorld(wp);
		wp2.set(wp);
		
		Vector3d p = MatrixHelper.getPosition(wp);
		p.add(linearForce);
		MatrixHelper.setPosition(wp2, new Vector3d(0,0,0));
		
		Matrix4d rz = new Matrix4d();
		rz.rotZ(zTorque);
		wp2.mul(rz,wp);
		
		MatrixHelper.setPosition(wp2, p);
		
		setPoseWorld(wp2);
	}
}
