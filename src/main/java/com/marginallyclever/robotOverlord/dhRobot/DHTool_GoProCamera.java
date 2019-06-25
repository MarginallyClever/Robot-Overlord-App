package com.marginallyclever.robotOverlord.dhRobot;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;

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
public class DHTool_GoProCamera extends DHTool {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DHTool_GoProCamera() {
		super();
		dhLinkEquivalent.d=8;  // cm
		dhLinkEquivalent.r=50;  // cm
		dhLinkEquivalent.alpha=0;
		dhLinkEquivalent.theta=0;
		dhLinkEquivalent.flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_ALPHA;
		dhLinkEquivalent.refreshPoseMatrix();
		setDisplayName("GoPro Camera");
		
		setFilename("/gopro.stl");
		setScale(0.1f);
		// adjust the model's position and rotation.
		this.setPosition(new Vector3d(0,0,dhLinkEquivalent.d));
		Matrix4d m = new Matrix4d();
		m.setIdentity();
		m.rotX(Math.toRadians(90));
		Matrix4d m2 = new Matrix4d();
		m2.setIdentity();
		m2.rotY(Math.toRadians(90));
		m.mul(m2);
		this.setRotation(m);
	}
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
	}

	/**
	 * Read HID device to move target pose.  Currently hard-coded to PS4 joystick values. 
	 * @return true if targetPose changes.
	 */
	@Override
	public boolean directDrive() {
		Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
		boolean isDirty=false;
		final double scale=0.4;
		
        for(int i=0;i<ca.length;i++){
        	if(ca[i].getType()!=Controller.Type.STICK) continue;

        	Component[] components = ca[i].getComponents();
            for(int j=0;j<components.length;j++){
            	if(!components[j].isAnalog()) {
        			if(components[j].getPollData()==1) {
        				if(components[j].getIdentifier()==Identifier.Button._8) {
        					// L1 - dolly in
        					if(dhLinkEquivalent.r>1) {
        						dhLinkEquivalent.r-=scale;
        					}
        				}
        				if(components[j].getIdentifier()==Identifier.Button._9) {
        					// R1 - dolly out
        					dhLinkEquivalent.r+=scale;
        				}
        				//System.out.print(" B"+components[j].getIdentifier().getName());
            		}
            	}
        	}
        }
        //System.out.println();
        return isDirty;
	}
}
