package com.marginallyclever.robotOverlord.dhRobot;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3f;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.material.Material;
import com.marginallyclever.robotOverlord.robot.Robot;
import com.marginallyclever.robotOverlord.robot.RobotKeyframe;

/**
 * A robot designed using D-H parameters.
 * @author Dan Royer
 *
 */
public abstract class DHRobot extends Robot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	LinkedList<DHLink> links;
	DHKeyframe poseNow;
	DHPanel panel;
	Matrix4d endMatrix;
	boolean drawSkeleton;
	
	public DHRobot() {
		super();
		setDisplayName("DHRobot");
		links = new LinkedList<DHLink>();
		endMatrix = new Matrix4d();
		drawSkeleton=true;
		
		setupLinks();
		
		refreshPose();
	}
	
	/**
	 * Override this method with your robot's setup.
	 */
	public abstract void setupLinks();
	
	@Override
	public RobotKeyframe createKeyframe() {
		return new DHKeyframe();
	}

	@Override
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		
		panel = new DHPanel(gui,this);
		list.add(panel);
		
		return list;
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		Vector3f position = this.getPosition();
		gl2.glTranslatef(position.x, position.y, position.z);
		
		Iterator<DHLink> i = links.iterator();

		// Draw models
		float r=1;
		float g=217f/255f;
		float b=33f/255f;
		Material mat = new Material();
		mat.setDiffuseColor(r,g,b,1);
		mat.render(gl2);
		
		gl2.glPushMatrix();
		while(i.hasNext()) {
			DHLink link = i.next();
			link.renderModel(gl2);
		}
		gl2.glPopMatrix();

		// Draw skeleton
		if(drawSkeleton) {
			gl2.glPushMatrix();
			boolean isDepth = gl2.glIsEnabled(GL2.GL_DEPTH_TEST);
			boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
			gl2.glDisable(GL2.GL_DEPTH_TEST);
			gl2.glDisable(GL2.GL_LIGHTING);
	
			i = links.iterator();
			while(i.hasNext()) {
				DHLink link = i.next();
				link.renderPose(gl2);
			}
			gl2.glPopMatrix();
			MatrixHelper.drawMatrix(gl2, 
					new Vector3f((float)endMatrix.m03,(float)endMatrix.m13,(float)endMatrix.m23),
					new Vector3f((float)endMatrix.m00,(float)endMatrix.m10,(float)endMatrix.m20),
					new Vector3f((float)endMatrix.m01,(float)endMatrix.m11,(float)endMatrix.m21),
					new Vector3f((float)endMatrix.m02,(float)endMatrix.m12,(float)endMatrix.m22)
					);
			
			if(isDepth) gl2.glEnable(GL2.GL_DEPTH_TEST);
			if(isLit) gl2.glEnable(GL2.GL_LIGHTING);
	
			gl2.glPopMatrix();
		}
	}
	
	/**
	 * Update the pose matrix of each DH link and also use forward kinematics to find the {@end} position.
	 */
	public void refreshPose() {
		endMatrix.setIdentity();
		
		Iterator<DHLink> i = links.iterator();
		while(i.hasNext()) {
			DHLink link = i.next();
			// update matrix
			link.refreshPoseMatrix();
			// find cumulative matrix
			endMatrix.mul(link.pose);
			link.poseCumulative.set(endMatrix);
		}
	}
	
	/**
	 * Adjust the number of links in this robot
	 * @param newSize must be greater than 0
	 */
	public void setNumLinks(int newSize) {
		if(newSize<1) newSize=1;
		
		int oldSize = links.size();
		while(oldSize>newSize) {
			oldSize--;
			links.pop();
		}
		while(oldSize<newSize) {
			oldSize++;
			links.push(new DHLink());
		}
	}

	/**
	 * Adjust the world transform of the robot
	 * @param pos the new world position for the local origin of the robot.
	 */
	@Override
	public void setPosition(Vector3f pos) {
		super.setPosition(pos);
		refreshPose();
		if(panel!=null) panel.updateEnd();
	}
}
