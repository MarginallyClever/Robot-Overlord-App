package com.marginallyclever.robotoverlord.robots.dog;

import java.util.ArrayList;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.entities.PoseEntity;
import com.marginallyclever.robotoverlord.entities.ShapeEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import com.marginallyclever.robotoverlord.uiexposedtypes.IntEntity;
import com.marginallyclever.robotoverlord.uiexposedtypes.MaterialEntity;

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
	public static final int NUM_LEGS = 4;
	
	public static final double KINEMATIC_BODY_WIDTH = 8;
	public static final double KINEMATIC_BODY_LENGTH = 8;
	public static final double KINEMATIC_BODY_HEIGHT = 18.5;
	
	public static final double VISUAL_BODY_WIDTH = 12;
	public static final double VISUAL_BODY_LENGTH = 8;
	public static final double VISUAL_BODY_HEIGHT = 30;

	private DogLeg[] legs = new DogLeg[NUM_LEGS];
	
	private ArrayList<DogAnimator> animators = new ArrayList<DogAnimator>();
	private DogAnimator activeAnimator;

	private IntEntity animationChoice = new IntEntity("Animation style",0);
	private MaterialEntity matTorso = new MaterialEntity();
	private MaterialEntity matShadow = new MaterialEntity();
	private ShapeEntity torsoShape = new ShapeEntity("Torso","/SpotMicro/torso.obj");
	
	public DogRobot() {
		super("Dog Robot");
		setupLegs();
		setupAnimators();
		setupMaterials();
		fixBlenderTorsoModel();
	}

	private void fixBlenderTorsoModel() {
		torsoShape.setRotation(new Vector3d(Math.toRadians(90),Math.toRadians(180),Math.toRadians(180)));
		torsoShape.setPosition(new Vector3d(-0.7,4.1,7)); 
	}

	private void setupLegs() {
		double w = KINEMATIC_BODY_WIDTH/2;
		double h = KINEMATIC_BODY_HEIGHT/2;
		legs[0] = new DogLeg(this, w, h,1);
		legs[1] = new DogLeg(this,-w, h,-1);
		legs[2] = new DogLeg(this,-w,-h,-1);
		legs[3] = new DogLeg(this, w,-h,1);
	}

	private void setupMaterials() {
		matTorso.setLit(true);
		matTorso.setDiffuseColor(1, 1, 1, 1);
		matShadow.setDiffuseColor(0, 0, 0, 0.4f);
	}
	
	private void setupAnimators() {
		addAnimator(new DogWalkZero());
		addAnimator(new DogWalkOne());
		addAnimator(new DogWalkTwo());
		addAnimator(new DogWalkThree());
		
		activeAnimator = animators.get(0);
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
		if(activeAnimator!=null) activeAnimator.walk(this, gl2);
		drawCurrentDogPose(gl2);
		drawStabilityPolygon(gl2);
		drawShadow(gl2);
	}
	
	private void drawStabilityPolygon(GL2 gl2) {
		ConvexShadow shadow = new ConvexShadow( getToesOnFloor() );
		matShadow.render(gl2);
		shadow.renderAsLineLoop(gl2);
	}

	public ArrayList<Vector3d> getToesOnFloor() {
		ArrayList<Vector3d> p = new ArrayList<Vector3d>();
		for( DogLeg leg : legs ) {
			if( leg.isToeTouchingTheFloor() ) {
				p.add(leg.getPointOnFloorUnderToe());
			}
		}
		return p;
	}

	private void drawShadow(GL2 gl2) {
		ConvexShadow shadow = new ConvexShadow( getBoxCornersProjectedOnFloor() );
		matShadow.setDiffuseColor(0, 0, 0, 0.4f);
		matShadow.render(gl2);
		shadow.renderAsFan(gl2);
	}

	private ArrayList<Vector3d> getBoxCornersProjectedOnFloor() {
		double width = (VISUAL_BODY_WIDTH/2);
		double height = (VISUAL_BODY_HEIGHT/2);
		double length = (VISUAL_BODY_LENGTH/2);
		Point3d [] p = PrimitiveSolids.get8PointsOfBox(
							new Point3d(-width,-length,-height),
							new Point3d( width, length, height));
		ArrayList<Vector3d> p2 = new ArrayList<Vector3d>();
		Matrix4d m = getPoseWorld();
		for( Point3d pN : p ) {
			m.transform(pN);
			Vector3d pT = new Vector3d(pN);
			pT.z=0;
			p2.add(pT);
		}

		return p2;
	}

	private void drawCurrentDogPose(GL2 gl2) {
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, myPose);
		drawTorso(gl2);
		drawLegs(gl2);
		gl2.glPopMatrix();
	}

	private void drawTorso(GL2 gl2) {
		matTorso.render(gl2);
		torsoShape.render(gl2);
	}

	private void drawLegs(GL2 gl2) {
		for( DogLeg leg : legs ) leg.render(gl2);
	}
	
	public void drawPointOnGroundUnderToes(GL2 gl2) {
		for( DogLeg leg : legs ) {
			Vector3d fp = leg.getPointOnFloorUnderToe();
			
			gl2.glPushMatrix();
			gl2.glTranslated(fp.x,fp.y,fp.z);
			PrimitiveSolids.drawSphere(gl2, 0.25);
			gl2.glPopMatrix();
		}
	}

	public void drawToeTargets(GL2 gl2) {
		for( DogLeg leg : legs ) leg.drawGradientDescentTarget(gl2);
	}

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("D",getName());
		
		animationChoice.addPropertyChangeListener((evt)->{
			activeAnimator = animators.get(animationChoice.get());
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
		for( DogLeg leg : legs ) leg.gradientDescent(0.001);
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

	public void lowerFeetToGround() {
		// assumes body is right way up.
		for( DogLeg leg : legs ) leg.toeTarget2.z=0;
	}

	@Override
	public void setRotation(Matrix3d arg0) {
		super.setRotation(arg0);
		for( DogLeg leg : legs ) leg.whenBodyHasMoved();
	}
	
	@Override
	public void setPosition(Vector3d pos) {
		super.setPosition(pos);
		for( DogLeg leg : legs ) leg.whenBodyHasMoved();
	}
}
