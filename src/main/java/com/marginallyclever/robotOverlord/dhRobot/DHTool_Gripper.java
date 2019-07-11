package com.marginallyclever.robotOverlord.dhRobot;

import java.util.Iterator;
import java.util.List;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.InputManager;
import com.marginallyclever.robotOverlord.camera.Camera;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.light.Light;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelFactory;
import com.marginallyclever.robotOverlord.physicalObject.PhysicalObject;
import com.marginallyclever.robotOverlord.world.World;


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
	private static final long serialVersionUID = 127023987907031123L;

	/**
	 * A PhysicalObject, if any, being held by the tool.  Assumes only one object can be held.
	 */
	private transient PhysicalObject subjectBeingHeld;
	private transient Matrix4d heldRelative;
	
	private double gripperServoAngle=90;
	
	private Model linkage;
	private Model finger;
	
	private transient boolean wasGripping;
	
	public DHTool_Gripper() {
		super();
		dhLinkEquivalent.d=11.9082;  // cm
		dhLinkEquivalent.refreshPoseMatrix();
		
		heldRelative = new Matrix4d();
		
		setDisplayName("Gripper");
		
		setFilename("/Sixi2/beerGripper/base.stl");
		setScale(0.1f);
		
		setPosition(new Vector3d(0.91,0,4.1));

		Matrix4d r = new Matrix4d();
		r.setIdentity();
		r.rotX(Math.toRadians(180));
		Matrix4d r2 = new Matrix4d();
		r2.setIdentity();
		r2.rotZ(Math.toRadians(90));
		r.mul(r2);
		this.setRotation(r);
		
		wasGripping=false;
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

		//render gripper model
		if(model==null) return;
		if(linkage==null) return;
		if(finger==null) return;

		material.render(gl2);
		
		gl2.glPushMatrix();
		gl2.glRotated(90, 0, 0, 1);
			// model rendered by super
		
			gl2.glPushMatrix();
			gl2.glTranslated(2.7/2, 0, 4.1);
			gl2.glRotated(this.gripperServoAngle, 0, 1, 0);
			linkage.render(gl2);
			gl2.glPopMatrix();
			
			gl2.glPushMatrix();
			gl2.glTranslated(1.1/2, 0, 5.9575);
			gl2.glRotated(this.gripperServoAngle, 0, 1, 0);
			linkage.render(gl2);
			gl2.glPopMatrix();
			
			gl2.glPushMatrix();
			gl2.glTranslated(-2.7/2, 0, 4.1);
			gl2.glRotated(-this.gripperServoAngle, 0, 1, 0);
			linkage.render(gl2);
			gl2.glPopMatrix();
			
			gl2.glPushMatrix();
			gl2.glTranslated(-1.1/2, 0, 5.9575);
			gl2.glRotated(-this.gripperServoAngle, 0, 1, 0);
			linkage.render(gl2);
			
			gl2.glPopMatrix();

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
		
		gl2.glPopMatrix();
	}

	/**
	 * Read HID device to move target pose.  Currently hard-coded to PS4 joystick values. 
	 * @return true if targetPose changes.
	 */
	@Override
	public boolean directDrive() {
		boolean isDirty=false;
		final double scaleGrip=1.8;
		
		if(InputManager.keyState[2]==1 && !wasGripping) { // circle
			wasGripping=true;
			//System.out.println("Circle");
			// grab release
			if(subjectBeingHeld==null) {
				// get the object at the targetPos
				// attach it to this object
				//System.out.println("Grab");
				subjectBeingHeld = findObjectNear(10);
				if(subjectBeingHeld != null) {
					// find the relative transform so that its angle doesn't change?
					Matrix4d held = new Matrix4d(subjectBeingHeld.getPose());
					Matrix4d iEnd = new Matrix4d(dhLinkEquivalent.poseCumulative);
					iEnd.invert();
					heldRelative.mul(iEnd,held);
					//heldRelative.transform(dp);
					//heldRelative.m03=dp.x;
					//heldRelative.m13=dp.y;
					//heldRelative.m23=dp.z;
				}
			} else {
				//System.out.println("Release");
				// release the object being held, somehow.
				subjectBeingHeld=null;
			}
		}
		if(InputManager.keyState[2]==0) wasGripping=false;
		
        if(InputManager.keyState[6]==1) {
			if(gripperServoAngle<70) {
				gripperServoAngle+=scaleGrip;
				isDirty=true;
			}
        }
        if(InputManager.keyState[7]==1) {
			if(gripperServoAngle>0) {
				gripperServoAngle-=scaleGrip;
				isDirty=true;
			}
        }

        return isDirty;
	}
	
	public void refreshPose(Matrix4d endMatrix) {
		super.refreshPose(endMatrix);
		if(subjectBeingHeld!=null) {
			Matrix4d finalPose = new Matrix4d();
			finalPose.mul(endMatrix,heldRelative);
			subjectBeingHeld.setPose(finalPose);
		}
	}
	
	public PhysicalObject findObjectNear(double radius) {
		//System.out.println("Finding world...");
		Entity p=parent;
		while(p!=null) {
			//System.out.println("\t"+p.getDisplayName());
			if(p instanceof World) {
				break;
			}
			p=p.getParent();
		}
		if(p==null || !(p instanceof World)) {
			//System.out.println("World not found");
			return null;
		}

		//System.out.println("Asking world...");
		
		// World, please tell me who is near my grab point.
		World world = (World)p;
		Point3d target = new Point3d(dhLinkEquivalent.poseCumulative.m03,
									 dhLinkEquivalent.poseCumulative.m13,
									 dhLinkEquivalent.poseCumulative.m23);
		List<PhysicalObject> list = world.findPhysicalObjectsNear(target,radius);

		// Check the list for anything that is not this tool and not this robot.
		Iterator<PhysicalObject> iter = list.iterator();
		while(iter.hasNext()) {
			PhysicalObject po = iter.next();
			if(po==parent) continue;
			if(po==this) continue;
			if(po instanceof Light) continue;
			if(po instanceof Camera) continue;
			//System.out.println("  selected "+po.getDisplayName());
			return po;  // found!
		}
		return null;
	}
}
