package com.marginallyclever.robotOverlord.dhRobot;

import java.util.Iterator;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.material.Material;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelFactory;

/**
 * Unfinished UArm implementation of DHRobot
 * @author Dan Royer
 * @see https://buildmedia.readthedocs.org/media/pdf/uarmdocs/latest/uarmdocs.pdf
 */
public class DHRUArm extends DHRobot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private transient Model base = null;
	private transient Model shoulder = null;
	/*
	private transient Model bicep = null;
	private transient Model elbowHorn = null;
	private transient Model forearm = null;
	private transient Model wrist = null;
	private transient Model elbow = null;
	private transient Model wristTendon1 = null;
	private transient Model wristTendon2 = null;
	private transient Model forearmTendon = null;
	*/

	public DHRUArm() {
		super();
		setDisplayName("DHRUArm");
	}
	
	@Override
	public void setupLinks() {
		setNumLinks(5);
		// roll
		links.get(0).d=2.75;
		links.get(0).theta=0;
		links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(0).rangeMin=-160;
		links.get(0).rangeMax=160;
		// tilt
		links.get(1).alpha=0;
		links.get(1).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(1).rangeMin=-72;
		// tilt
		links.get(2).d=15.0;
		links.get(2).alpha=0;
		links.get(2).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		// interim point
		links.get(3).d=16.0;
		links.get(3).alpha=0;
		links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		// end effector
		links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;

		try {

			base = ModelFactory.createModelFromFilename("/uArm/1.STL",(float)(1.0/25.4));
			shoulder = ModelFactory.createModelFromFilename("/uArm/2.STL",(float)(1.0/25.4));
			/*
			elbowHorn = ModelFactory.createModelFromFilename("/uArm/4.STL",(float)(1.0/25.4));
			forearmTendon = ModelFactory.createModelFromFilename("/uArm/6.STL",(float)(1.0/25.4));
			bicep = ModelFactory.createModelFromFilename("/uArm/3.STL",(float)(1.0/25.4));
			forearm = ModelFactory.createModelFromFilename("/uArm/9.STL",(float)(1.0/25.4));
			wrist = ModelFactory.createModelFromFilename("/uArm/10.STL",(float)(1.0/25.4));
			wristTendon1 = ModelFactory.createModelFromFilename("/uArm/5.STL",(float)(1.0/25.4));
			wristTendon2 = ModelFactory.createModelFromFilename("/uArm/8.STL",(float)(1.0/25.4));
			elbow = ModelFactory.createModelFromFilename("/uArm/7.STL",(float)(1.0/25.4));
			 */
			links.get(0).model = base;
			links.get(1).model = shoulder;
			//links.get(0).model = ModelFactory.createModelFromFilename("/Sixi2/anchor.stl",0.1f);
			//links.get(1).model = ModelFactory.createModelFromFilename("/Sixi2/shoulder.stl",0.1f);
			//links.get(2).model = ModelFactory.createModelFromFilename("/Sixi2/bicep.stl",0.1f);
			//links.get(3).model = ModelFactory.createModelFromFilename("/Sixi2/forearm.stl",0.1f);
			//links.get(4).model = ModelFactory.createModelFromFilename("/Sixi2/hand.stl",0.1f);
			
			//links.get(0).model.adjustOrigin(new Vector3f(0, 0, 2.75f));
			//links.get(1).model.adjustOrigin(new Vector3f(2.75f,8.1f,5f));
			//links.get(1).model.adjustRotation(new Vector3f(0,90,-90));
			
			//links.get(0).model.adjustOrigin(new Vector3f(0, 0, 5.150f));
			//links.get(0).model.adjustRotation(new Vector3f(90,90,0));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void render(GL2 gl2) {
		links.get(0).model.adjustOrigin(new Vector3d(0, 0, 0.75f));
		
		gl2.glPushMatrix();
			Vector3d position = this.getPosition();
			gl2.glTranslated(position.x, position.y, position.z);
			
			// Draw models
			Material mat = new Material();
			mat.setDiffuseColor(
					0.75f*247.0f/255.0f,
					0.75f*233.0f/255.0f,
					0.75f*215.0f/255.0f, 1);
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
	
	public DHIKSolver getSolverIK() {
		return new DHIKSolveRTT();
	}
}
