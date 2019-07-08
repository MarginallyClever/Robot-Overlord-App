package com.marginallyclever.robotOverlord.dhRobot;

import java.util.Iterator;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.material.Material;
import com.marginallyclever.robotOverlord.model.ModelFactory;

/**
 * FANUC cylindrical coordinate robot GMF M-100
 * @author Dan Royer
 *
 */
public class DHRobot_SCARA_NM extends DHRobot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public boolean isFirstTime;

	public DHRobot_SCARA_NM() {
		super();
		setDisplayName("SCARA NM");
		isFirstTime=true;
	}
	
	@Override
	public void setupLinks() {
		setNumLinks(5);

		// roll
		links.get(0).d=13.784;
		links.get(0).r=15;
		links.get(0).theta=0;
		links.get(0).alpha=0;
		links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(0).rangeMin=-40;
		links.get(0).rangeMax=240;
		
		// roll
		links.get(1).d=0;
		links.get(1).r=13.0;
		links.get(1).theta=0;
		links.get(1).alpha=0;
		links.get(1).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;		
		links.get(1).rangeMin=-120;
		links.get(1).rangeMax=120;
		// slide
		links.get(2).d=-18.5+7.574;
		links.get(2).theta=0;
		links.get(2).alpha=0;
		links.get(2).flags = DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(2).rangeMin=-18.5;
		links.get(2).rangeMax=-18.5+7.574;
		// roll
		links.get(3).d=0;
		links.get(3).theta=0;
		links.get(3).alpha=0;
		links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(3).rangeMin=-180;
		links.get(3).rangeMax=180;

		links.get(4).d=0;
		links.get(4).theta=0;
		links.get(4).alpha=0;
		links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(4).rangeMin=0;
		links.get(4).rangeMax=0;
		
		this.refreshPose();
	}

	public void setupModels() {
		try {
			if(links.get(0).model==null) links.get(0).model = ModelFactory.createModelFromFilename("/SCARA_NM/Scara_base.stl",0.1f);
			if(links.get(1).model==null) links.get(1).model = ModelFactory.createModelFromFilename("/SCARA_NM/Scara_arm1.stl",0.1f);
			if(links.get(2).model==null) links.get(2).model = ModelFactory.createModelFromFilename("/SCARA_NM/Scara_arm2.stl",0.1f);
			if(links.get(4).model==null) links.get(4).model = ModelFactory.createModelFromFilename("/SCARA_NM/Scara_screw.stl",0.1f);
			
			links.get(0).model.adjustOrigin(new Vector3d(-8,0,0));
			links.get(1).model.adjustOrigin(new Vector3d(-15,8,-13.784));
			links.get(1).model.adjustRotation(new Vector3d(0,0,-90));

			links.get(2).model.adjustOrigin(new Vector3d(-13,8,-13.784));
			links.get(2).model.adjustRotation(new Vector3d(0,0,-90));

			links.get(4).model.adjustOrigin(new Vector3d(-8,0,-13.784));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void render(GL2 gl2) {
		if( isFirstTime ) {
			isFirstTime=false;
			setupModels();
		}
		
		gl2.glPushMatrix();
			Vector3d position = this.getPosition();
			gl2.glTranslated(position.x, position.y, position.z);
			
			// Draw models
			float r=0.5f;
			float g=0.5f;
			float b=0.5f;
			Material mat = new Material();
			mat.setDiffuseColor(r,g,b,1);
			mat.render(gl2);
			
			gl2.glPushMatrix();
				Iterator<DHLink> i = links.iterator();
				while(i.hasNext()) {
					DHLink link = i.next();
					link.renderModel(gl2);
				}
			gl2.glPopMatrix();
		gl2.glPopMatrix();
		
		super.render(gl2);
	}
	
	@Override
	public DHIKSolver getSolverIK() {
		return new DHIKSolver_SCARA();
	}
	
	@Override
	public void pick() {
		this.refreshPose();
		targetPose.set(endMatrix);
		//disabled until there are models to render.
		drawAsSelected=true;
	}
	
	@Override
	public void unPick() {
		//disabled until there are models to render.
		drawAsSelected=false;
	}

	public void sendNewStateToRobot(DHKeyframe keyframe) {}
}
