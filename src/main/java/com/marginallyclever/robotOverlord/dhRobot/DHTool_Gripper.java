package com.marginallyclever.robotOverlord.dhRobot;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelFactory;
import com.marginallyclever.robotOverlord.physicalObject.PhysicalObject;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Component.Identifier;

/**
 * DHTool is a model that has a DHLink equivalence.
 * In this way it can perform transforms and have sub-links.
 * @author Dan Royer
 *
 */
public class DHTool_Gripper extends DHTool {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * A PhysicalObject, if any, being held by the tool.  Assumes only one object can be held.
	 */
	PhysicalObject subjectBeingHeld;
	
	private double gripperServoAngle=90;
	
	private Model linkage;
	private Model finger;
	
	public DHTool_Gripper() {
		super();
		dhLinkEquivalent.d=11.9082;  // cm
		dhLinkEquivalent.refreshPoseMatrix();
		
		setDisplayName("Gripper");
		
		setFilename("/Sixi2/beerGripper/base.stl");
		setScale(0.1f);
		setPosition(new Vector3d(0,0,4.1));
	}
	
	public void render(GL2 gl2) {
		super.render(gl2);
		
		if(linkage==null) {
			try {
				linkage= ModelFactory.createModelFromFilename("/Sixi2/beerGripper/linkage.stl",0.1f);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(finger==null) {
			try {
				finger= ModelFactory.createModelFromFilename("/Sixi2/beerGripper/finger.stl",0.1f);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}

		if(this.model!=null) {
			gl2.glPushMatrix();
			gl2.glTranslated(0, -0.91, 4.1);
			gl2.glRotated(180, 0, 1, 0);
			this.model.render(gl2);
			gl2.glPopMatrix();
		}
		
		//render gripper model
		gl2.glPushMatrix();
		gl2.glTranslated(2.7/2, 0, 4.1);
		gl2.glRotated(this.gripperServoAngle, 0, 1, 0);
		if(linkage!=null) {
			linkage.render(gl2);
		}
		gl2.glPopMatrix();
		
		gl2.glPushMatrix();
		gl2.glTranslated(1.1/2, 0, 5.9575);
		gl2.glRotated(this.gripperServoAngle, 0, 1, 0);
		if(linkage!=null) {
			linkage.render(gl2);
		}
		gl2.glPopMatrix();
		
		gl2.glPushMatrix();
		gl2.glTranslated(-2.7/2, 0, 4.1);
		gl2.glRotated(-this.gripperServoAngle, 0, 1, 0);
		if(linkage!=null) {
			linkage.render(gl2);
		}
		gl2.glPopMatrix();
		
		gl2.glPushMatrix();
		gl2.glTranslated(-1.1/2, 0, 5.9575);
		gl2.glRotated(-this.gripperServoAngle, 0, 1, 0);
		if(linkage!=null) {
			linkage.render(gl2);
		}
		gl2.glPopMatrix();
		
		if(finger!=null) {
			double c=Math.cos(Math.toRadians(gripperServoAngle));
			double s=Math.sin(Math.toRadians(gripperServoAngle));
			gl2.glPushMatrix();
			gl2.glTranslated(-2.7/2-s*4.1, 0, 4.1+c*4.1);
			gl2.glScaled(1,1,-1);
			//gl2.glTranslated(s*4.1, 2.7/2, 4.1+c*4.1);
			finger.render(gl2);
			gl2.glPopMatrix();
			
			gl2.glPushMatrix();
			gl2.glTranslated(2.7/2+s*4.1, 0, 4.1+c*4.1);
			gl2.glScaled(-1,1,-1);
			finger.render(gl2);
			gl2.glPopMatrix();
		}
	}

	/**
	 * Read HID device to move target pose.  Currently hard-coded to PS4 joystick values. 
	 * @return true if targetPose changes.
	 */
	@Override
	public boolean directDrive() {
		Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
		boolean isDirty=false;
		final double scaleGrip=1.8;
		final double scaleDolly=0.4;
		
        for(int i=0;i<ca.length;i++){
        	if(ca[i].getType()!=Controller.Type.STICK) continue;

        	Component[] components = ca[i].getComponents();
            for(int j=0;j<components.length;j++){
            	if(!components[j].isAnalog()) {

    				if(components[j].getIdentifier()==Identifier.Axis.POV) {
    					// D-pad buttons
    					float pollData = components[j].getPollData();
    					if(pollData ==Component.POV.DOWN) {
        					// L1 - dolly in
        					if(dhLinkEquivalent.r>1) {
        						dhLinkEquivalent.r-=scaleDolly;
        					}
    					} else if(pollData ==Component.POV.UP) {
        					// R1 - dolly out
        					dhLinkEquivalent.r+=scaleDolly;
    					} else if(pollData ==Component.POV.LEFT) {
        					// close grip
        					if(gripperServoAngle>0) {
        						gripperServoAngle-=scaleGrip;
        					}
    					} else if(pollData ==Component.POV.RIGHT) {
        					// open grip
        					if(gripperServoAngle<70) {
        						gripperServoAngle+=scaleGrip;
        					}
    					}
    				}
            	}
        	}
        }
        //System.out.println();
        return isDirty;
	}
}
