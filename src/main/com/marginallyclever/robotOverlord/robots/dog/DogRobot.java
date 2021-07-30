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
		super("Dog Robot");
		setupLegs();
		setupAnimators();
		setupMaterials();
	}
	
	private void setupLegs() {
		double w = BODY_WIDTH/2;
		double h = BODY_HEIGHT/2;
		legs[0] = new DogLeg(this, w, h);
		legs[1] = new DogLeg(this,-w, h);
		legs[2] = new DogLeg(this,-w,-h);
		legs[3] = new DogLeg(this, w,-h);
	}

	private void setupMaterials() {		
		mat.setLit(true);
		mat2.setLit(true);
		mat2.setDiffuseColor(1,0,0,1);
		matShadow.setDiffuseColor(0, 0, 0, 0.4f);
	}
	
	private void setupAnimators() {
		addAnimator(new DogWalkZero());
		addAnimator(new DogWalkOne());
		addAnimator(new DogWalkTwo());
		addAnimator(new DogWalkThree());
		
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

	@Override
	public void update(double dt) {
		super.update(dt);
	}
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);

		if(animator!=null) animator.walk(this, gl2);
		
		drawCurrentDogPose(gl2);
		drawPointOnGroundUnderToes(gl2);
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
		
		Matrix4d m = getPoseWorld();
		
		int i=0;
		for( Point3d pN : p ) {
			m.transform(pN);
			Vector3d pT = new Vector3d(pN);
			pT.z=0;
			p2[i++]=pT;
		}

		return p2;
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
		gl2.glColor4d(1,1,1,1);
		PrimitiveSolids.drawBox(gl2, 
			new Point3d(-width,-length,-height),
			new Point3d( width, length, height));
	}

	private void drawLegs(GL2 gl2) {
		boolean flag = OpenGLHelper.disableLightingStart(gl2);	
		for( DogLeg leg : legs ) leg.render(gl2);
		OpenGLHelper.disableLightingEnd(gl2, flag);
	}
	
	private void drawPointOnGroundUnderToes(GL2 gl2) {
		for( DogLeg leg : legs ) {
			Vector3d fp = leg.getPointOnFloorUnderToe();
			
			gl2.glPushMatrix();
			gl2.glTranslated(fp.x,fp.y,fp.z);
			PrimitiveSolids.drawSphere(gl2, 0.25);
			gl2.glPopMatrix();
		}
	}

	public void drawToeTarget(GL2 gl2) {
		for( DogLeg leg : legs ) {
			gl2.glPushMatrix();
			gl2.glTranslated(leg.toeTarget.x, leg.toeTarget.y, leg.toeTarget.z);

			if(leg.isTouchingTheFloor()) mat2.render(gl2);
			else						 mat.render(gl2);
			
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
		for( DogLeg leg : legs ) leg.refreshMatrixes();
	}

	public DogLeg getLeg(int i) {
		return legs[i];
	}

	public void moveToeTargetsSmoothly(double scale) {
		for( DogLeg leg : legs ) leg.moveToeTargetSmoothly(scale);
	}

	// Move the toes towards the toeTargets.
	public void gradientDescent() {
		double EPSILON = 0.001;
		
		for( DogLeg leg : legs ) {
			leg.gradientDescent(EPSILON);
		}
	}

	public void pushBody(Vector3d linearForce,double zTorque) {
		// move the body to match the feet
		Matrix4d wp = getPoseWorld();
		Matrix4d wp2 = new Matrix4d(wp);
		
		Vector3d p = MatrixHelper.getPosition(wp);
		p.add(linearForce);
		MatrixHelper.setPosition(wp2, new Vector3d(0,0,0));
		
		Matrix4d rz = new Matrix4d();
		rz.rotZ(zTorque);
		wp2.mul(rz,wp);
		
		MatrixHelper.setPosition(wp2, p);
		
		setPoseWorld(wp2);
	}



	public void setIdealStandingAngles() {
		for( DogLeg leg : legs ) leg.setIdealStandingAngles();
	}

	public void relaxShoulders() {
		for( DogLeg leg : legs ) leg.relaxShoulder();
	}
	
}
