package com.marginallyclever.robotOverlord.entity.scene.robotEntity.olderModels;

import java.util.Observable;

import javax.vecmath.Matrix4d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.MaterialEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers.DHIKSolver_GradientDescent;
import com.marginallyclever.robotOverlord.entity.scene.modelEntity.ModelEntity;


public class Sixi1 extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1397367244262210747L;
	protected MaterialEntity material;
	DHRobotEntity live = new DHRobotEntity();
	DHLink endEffector = new DHLink();

	public Sixi1() {
		super();
		setName("Sixi 1");
		
		addChild(live);
		
		live.setIKSolver(new DHIKSolver_GradientDescent());

		live.setNumLinks(6);

		showBoundingBox.addObserver(this);
		showLocalOrigin.addObserver(this);
		showLineage.addObserver(this);

		ModelEntity base = new ModelEntity();
		addChild(base);
		base.setName("Base");
		base.setModelFilename("/Sixi/anchor.obj");
		base.getMaterial().setDiffuseColor(0.89f,0.0f,0.0f,1.0f);
		
		live.links.get(0).setName("X");
		live.links.get(0).setD(25);
		live.links.get(0).setAlpha(90);
		live.links.get(0).setTheta(0);
		live.links.get(0).setR(0);
		live.links.get(0).setRangeMin(-120);
		live.links.get(0).setRangeMax(120);

		// tilt
		live.links.get(1).setName("Y");
		live.links.get(1).setRangeMin(-72);
		live.links.get(1).setTheta(90-11.3);
		live.links.get(1).setAlpha(0);
		live.links.get(1).setR(25.4951);
		live.links.get(1).setRangeMax(180);
		live.links.get(1).setRangeMin(0);
		
		// tilt
		live.links.get(2).setName("Z");
		live.links.get(2).setD(0);
		live.links.get(2).setR(20.6155);
		live.links.get(2).setTheta(180-154.7);
		live.links.get(2).setAlpha(90);
		live.links.get(2).setRangeMin(36-180);
		live.links.get(2).setRangeMax(36);
		live.links.get(2).flags = LinkAdjust.ALL;
		
		// roll
		live.links.get(3).setName("U");
		live.links.get(3).setAlpha(180-166);
		live.links.get(3).setD(0);
		live.links.get(3).setR(0);
		live.links.get(3).setTheta(-90);
		live.links.get(3).setRangeMin(-90);
		live.links.get(3).setRangeMax(90);
		live.links.get(3).flags = LinkAdjust.ALL;

		// tilt
		live.links.get(4).setName("V");
		live.links.get(4).setAlpha(-90);
		live.links.get(4).setD(0);
		live.links.get(4).setR(0);
		live.links.get(4).setRangeMin(-120);
		live.links.get(4).setRangeMax(120);
		
		// roll
		live.links.get(5).setName("W");
		live.links.get(5).setAlpha(0);
		live.links.get(5).setD(3.9527);
		live.links.get(5).setR(0);
		live.links.get(5).setRangeMin(-90);
		live.links.get(5).setRangeMax(90);

		boolean attach=false;
		if(attach) {
			live.links.get(0).setModelFilename("/Sixi/shoulder.obj");
			live.links.get(1).setModelFilename("/Sixi/bicep.obj");
			live.links.get(2).setModelFilename("/Sixi/elbow.obj");
			live.links.get(3).setModelFilename("/Sixi/forearm.obj");
			live.links.get(4).setModelFilename("/Sixi/wrist.obj");
			live.links.get(5).setModelFilename("/Sixi/hand.obj");
		} else {
			addChild(new ModelEntity("/Sixi/shoulder.obj"));
			addChild(new ModelEntity("/Sixi/bicep.obj"));
			addChild(new ModelEntity("/Sixi/elbow.obj"));
			addChild(new ModelEntity("/Sixi/forearm.obj"));
			addChild(new ModelEntity("/Sixi/wrist.obj"));
			addChild(new ModelEntity("/Sixi/hand.obj"));
		}

		endEffector.setName("End Effector");
		live.links.get(5).addChild(endEffector);
		endEffector.addObserver(this);

		for(DHLink link : live.links ) {
			link.setDHRobot(live);
		}
		endEffector.setDHRobot(live);
		
		// update this world pose and all my children's poses all the way down.
		this.updatePoseWorld();
		
		// Use the poseWorld for each DHLink to adjust the model origins.
		for(int i=0;i<live.links.size();++i) {
			DHLink bone=live.links.get(i);
			if(bone.getModel()!=null) {
				Matrix4d iWP = bone.getPoseWorld();
				iWP.invert();
				bone.getModel().adjustMatrix(iWP);
				bone.getMaterial().setTextureFilename("/Sixi2/sixi.png");
				bone.getMaterial().setDiffuseColor(0.89f,0.0f,0.0f,1.0f);
			}
		}
		
		live.setShowLineage(true);
	}
	
	@Override
	public void render(GL2 gl2) {
		// update this world pose and all my children's poses all the way down.
		this.updatePoseWorld();
		
		super.render(gl2);
	}
	
	@Override
	public void update(Observable obs, Object obj) {
		if(obs == endEffector.poseWorld) {
			live.setPoseIK(endEffector.getPoseWorld());
		}
	}
}
