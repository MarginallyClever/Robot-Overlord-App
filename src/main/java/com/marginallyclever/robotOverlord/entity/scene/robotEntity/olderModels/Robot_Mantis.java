package com.marginallyclever.robotOverlord.entity.scene.robotEntity.olderModels;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.MaterialEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotModel;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers.DHIKSolver_RTTRTR;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.RobotEntity;


/**
 * D-H robot modified for Andreas Hoelldorfer's MANTIS
 * @author Dan Royer
 * See https://hackaday.io/project/3800-3d-printable-robot-arm
 *
 */
public class Robot_Mantis extends RobotEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5773299795110988863L;
	private transient boolean isFirstTime;
	DHRobotModel live;
	
	public Robot_Mantis() {
		super();
		setName("Mantis");
		
		live = new DHRobotModel();
		live.setIKSolver(new DHIKSolver_RTTRTR());
		setupLinks(live);
		isFirstTime=true;
	}
	
	protected void setupLinks(DHRobotModel robot) {
		robot.setNumLinks(8);

		// roll
		//robot.getLink(0).d=13.44;
		robot.getLink(0).setD(24.5+2.7);
		robot.getLink(0).flags = LinkAdjust.THETA;
		robot.getLink(0).setRangeMin(-120);
		robot.getLink(0).setRangeMax(120);

		// tilt
		robot.getLink(1).flags = LinkAdjust.ALPHA;
		robot.getLink(1).setRangeMin(-72);

		// tilt
		robot.getLink(2).setD(13.9744 + 8.547);
		robot.getLink(2).flags = LinkAdjust.ALPHA;
		robot.getLink(2).setRangeMin(-83.369);
		robot.getLink(2).setRangeMax(86);
		// interim point
		robot.getLink(3).setD(0.001);  // TODO explain why this couldn't just be zero.  Solver hates it for some reason.
		robot.getLink(3).setAlpha(90);
		robot.getLink(3).flags = LinkAdjust.NONE;
		// roll
		robot.getLink(4).setD(8.547);
		robot.getLink(4).flags = LinkAdjust.THETA;
		robot.getLink(4).setRangeMin(-90);
		robot.getLink(4).setRangeMax(90);

		// tilt
		robot.getLink(5).setD(14.6855f);
		robot.getLink(5).flags = LinkAdjust.ALPHA;
		robot.getLink(5).setRangeMin(-90);
		robot.getLink(5).setRangeMax(90);
		// roll
		robot.getLink(6).setD(5.0f);
		robot.getLink(6).flags = LinkAdjust.THETA;
		robot.getLink(6).setRangeMin(-90);
		robot.getLink(6).setRangeMax(90);
		
		robot.getLink(7).flags = LinkAdjust.NONE;

		robot.refreshDHMatrixes();
	}


	public void setupModels(DHRobotModel robot) {
		try {
			robot.getLink(0).setShapeFilename("/AH/rotBaseCase.stl");
			robot.getLink(1).setShapeFilename("/AH/Shoulder_r1.stl");
			robot.getLink(2).setShapeFilename("/AH/Elbow.stl");
			robot.getLink(3).setShapeFilename("/AH/Forearm.stl");
			robot.getLink(5).setShapeFilename("/AH/Wrist_r1.stl");
			robot.getLink(6).setShapeFilename("/AH/WristRot.stl");

			for( int i=0;i<robot.getNumLinks();++i) {
				robot.getLink(i).setShapeScale(0.1);
			}
			
			robot.getLink(0).setShapeOrigin(new Vector3d(0,0,2.7));
			robot.getLink(1).setShapeRotation(new Vector3d(0,0,90));
			robot.getLink(1).setShapeOrigin(new Vector3d(0,0,0));
			robot.getLink(2).setShapeRotation(new Vector3d(90,90,90));
			robot.getLink(2).setShapeOrigin(new Vector3d(0,0.476,2.7+(13.9744 + 8.547)/2));
			robot.getLink(3).setShapeRotation(new Vector3d(180,90,90));
			robot.getLink(3).setShapeOrigin(new Vector3d(0,-5.7162,0));//0.3488,0.3917
			robot.getLink(5).setShapeOrigin(new Vector3d(0,0,0));
			robot.getLink(6).setShapeRotation(new Vector3d(-180,90,0));
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	@Override
	public void render(GL2 gl2) {
		if( isFirstTime ) {
			isFirstTime=false;
			setupModels(live);
		}
		
		
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, pose);
		
			MaterialEntity material = new MaterialEntity();
			float r=0.5f;
			float g=0.5f;
			float b=0.5f;
			material.setDiffuseColor(r,g,b,1);
			material.render(gl2);
			
			live.render(gl2);
		gl2.glPopMatrix();
		
		super.render(gl2);
	}

	@Override
	public Memento createKeyframe() {
		// TODO Auto-generated method stub
		return null;
	}

}
