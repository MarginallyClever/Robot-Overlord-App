package com.marginallyclever.robotOverlord.dhRobot;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Point3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.InputListener;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.physicalObject.PhysicalObject;
import com.marginallyclever.robotOverlord.robot.Robot;
import com.marginallyclever.robotOverlord.robot.RobotKeyframe;
import com.marginallyclever.robotOverlord.world.World;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Component.Identifier;

/**
 * A robot designed using D-H parameters.
 * @author Dan Royer
 *
 */
public abstract class DHRobot extends Robot implements InputListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * {@value links} a list of DHLinks describing the kinematic chain.
	 */
	LinkedList<DHLink> links;
	
	/**
	 * {@value poseNow} keyframe describing the current pose of the kinematic chain.
	 */
	DHKeyframe poseNow;
	
	/**
	 * {@value panel} the GUI panel for controlling this robot.
	 */
	DHRobotPanel panel;
	
	/**
	 * {@value endMatrix} the world frame pose of the last link in the kinematic chain.
	 */
	Matrix4d endMatrix;

	/**
	 * {@value targetPose} the pose the IK is trying to move towards.
	 */
	public Matrix4d targetPose;

	/**
	 * {@value targetPose} the pose the IK would solve when the robot is at "home" position.
	 */
	public Matrix4d homePose;
	
	/**
	 * {@value dhTool} a DHTool current attached to the arm.
	 */
	DHTool dhTool;
	
	/**
	 * {@value drawSkeleton} true if the skeleton should be visualized on screen.  Default is false.
	 */
	boolean drawSkeleton;
	
	
	public DHRobot() {
		super();
		links = new LinkedList<DHLink>();
		endMatrix = new Matrix4d();
		targetPose = new Matrix4d();
		homePose = new Matrix4d();
		
		drawSkeleton=false;
		setupLinks();
		poseNow = (DHKeyframe)createKeyframe();
		refreshPose();

		homePose.set(endMatrix);
		targetPose.set(endMatrix);
	}
	
	/**
	 * Override this method with your robot's setup.
	 */
	public abstract void setupLinks();
	
	/**
	 * Override this method to return the correct solver for your type of robot.
	 * @return the IK solver for a specific type of robot.
	 */
	public abstract DHIKSolver getSolverIK();


	@Override
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		
		panel = new DHRobotPanel(gui,this);
		list.add(panel);
		
		return list;
	}
	
	@Override
	public void render(GL2 gl2) {
		if(!drawSkeleton) return;

		boolean isDepth = gl2.glIsEnabled(GL2.GL_DEPTH_TEST);
		boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_DEPTH_TEST);
		gl2.glDisable(GL2.GL_LIGHTING);

		gl2.glPushMatrix();
			Vector3d position = this.getPosition();
			gl2.glTranslated(position.x, position.y, position.z);
			
			gl2.glPushMatrix();
				Iterator<DHLink> i = links.iterator();
				while(i.hasNext()) {
					DHLink link = i.next();
					link.renderPose(gl2);
				}
			gl2.glPopMatrix();
			
			MatrixHelper.drawMatrix(gl2, 
					new Vector3d((float)endMatrix.m03,(float)endMatrix.m13,(float)endMatrix.m23),
					new Vector3d((float)endMatrix.m00,(float)endMatrix.m10,(float)endMatrix.m20),
					new Vector3d((float)endMatrix.m01,(float)endMatrix.m11,(float)endMatrix.m21),
					new Vector3d((float)endMatrix.m02,(float)endMatrix.m12,(float)endMatrix.m22)
					);

		gl2.glPopMatrix();
		if(isDepth) gl2.glEnable(GL2.GL_DEPTH_TEST);
		if(isLit) gl2.glEnable(GL2.GL_LIGHTING);
		
		drawTargetPose(gl2);
	}
	
	/**
	 * Update the pose matrix of each DH link, then use forward kinematics to find the end position.
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
	public void setPosition(Vector3d pos) {
		super.setPosition(pos);
		refreshPose();
		if(panel!=null) panel.updateEnd();
	}

	/**
	 * Attach the nearest tool
	 * Detach the active tool if there is one.
	 */
	public void toggleATC() {
		if(dhTool!=null) {
			// we have a tool, release it.
			removeTool();
			return;
		}
		
		// we have no tool.  Look out into the world...
		Entity p=parent;
		while(p!=null) {
			if(p instanceof World) {
				break;
			}
		}
		if(p==null || !(p instanceof World)) {
			// World not found.  The toggle!  It does nothing!
		}

		// Request from the world "is there a tool at the position of the end effector"?
		World world = (World)p;
		
		Point3d target = new Point3d(this.endMatrix.m03,this.endMatrix.m13,this.endMatrix.m23);
		List<PhysicalObject> list = world.findPhysicalObjectsNear(target,10);

		// If there is a tool, attach to it.
		Iterator<PhysicalObject> iter = list.iterator();
		while(iter.hasNext()) {
			PhysicalObject po = iter.next();
			if(po instanceof DHTool) {
				// probably the only one we'll find.
				addTool((DHTool)po);
			}
		}
	}
	
	
	public void addTool(DHTool arg0) {
		removeTool();
		dhTool = arg0;
		arg0.heldBy = this;
	}
	
	public void removeTool() {
		if(dhTool!=null) {
			dhTool.heldBy = null;
		}
		dhTool = null;
	}
	
	public DHTool getCurrentTool() {
		return dhTool;
	}
	
	@Override
	public RobotKeyframe createKeyframe() {
		return new DHKeyframe(getSolverIK().getSolutionSize());
	}
	
	@Override
	public void inputUpdate() {		
        boolean isDirty=false;
        
        if(animationSpeed==0) {
        	// if we are in direct drive mode
        	isDirty=directDrive();
        }
        
        if(isDirty) {
        	// attempt to solve IK
        	DHIKSolver solver = this.getSolverIK();
        	DHKeyframe keyframe = (DHKeyframe)createKeyframe();
        	solver.solve(this,targetPose,keyframe);
        	if(solver.solutionFlag==DHIKSolver.ONE_SOLUTION) {
        		if(keyframeAnglesAreOK(keyframe)) {
	        		// Solved!  update robot pose with fk.
	        		if(connection!=null && connection.isOpen()) {
	        			if(isReadyToReceive) {
	        				// If the sum of the absolute difference of each joint is smaller than some epsilon, don't send it to the robot.
	        				double sum=0;
	        				//String message="";
	        				for(int i=0;i<keyframe.fkValues.length;++i) {
	        					double v = Math.abs(poseNow.fkValues[i]-keyframe.fkValues[i]);
	        					sum += v;
	        					//message += (long)(v*1000)+" \t";
	        				}
        					//System.out.println(AnsiColors.RED+message+AnsiColors.RESET);
	        				if(sum>0.01) {
	        					// update the live connected robot, which will come back through dataAvailable() to update the pose.
		        				sendPoseToRobot(keyframe);
		        				isReadyToReceive=false;
	        				}
		        		}
	        		} else {
	        			// no connected robot, update the pose directly.
	            		this.setRobotPose(keyframe);
	        		}
        		}
        	}
        }
	}
	
	/**
	 * Robot is connected and ready to receive.  Send the current FK values to the robot.
	 * @param keyframe
	 */
	public abstract void sendPoseToRobot(DHKeyframe keyframe);
	
	
	public void drawTargetPose(GL2 gl2) {
		gl2.glPushMatrix();
		
		double[] mat = new double[16];
		mat[ 0] = targetPose.m00;
		mat[ 1] = targetPose.m10;
		mat[ 2] = targetPose.m20;
		mat[ 3] = targetPose.m30;
		mat[ 4] = targetPose.m01;
		mat[ 5] = targetPose.m11;
		mat[ 6] = targetPose.m21;
		mat[ 7] = targetPose.m31;
		mat[ 8] = targetPose.m02;
		mat[ 9] = targetPose.m12;
		mat[10] = targetPose.m22;
		mat[11] = targetPose.m32;
		mat[12] = targetPose.m03;
		mat[13] = targetPose.m13;
		mat[14] = targetPose.m23;
		mat[15] = targetPose.m33;
		gl2.glMultMatrixd(mat, 0);

		boolean isDepth = gl2.glIsEnabled(GL2.GL_DEPTH_TEST);
		boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_DEPTH_TEST);
		gl2.glDisable(GL2.GL_LIGHTING);
		MatrixHelper.drawMatrix(gl2, 
				new Vector3d(0,0,0),
				new Vector3d(1,0,0),
				new Vector3d(0,1,0),
				new Vector3d(0,0,1));
		if(isDepth) gl2.glEnable(GL2.GL_DEPTH_TEST);
		if(isLit) gl2.glEnable(GL2.GL_LIGHTING);
		gl2.glPopMatrix();
	}
	
	/**
	 * Read HID device to move target pose.  Currently hard-coded to PS4 joystick values. 
	 * @return true if targetPose changes.
	 */
	public boolean directDrive() {
		Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
		boolean isDirty=false;
		double scale=0.4;
		double scaleTurn=0.15;
		double deadzone=0.1;
		
        for(int i=0;i<ca.length;i++){
        	if(ca[i].getType()!=Controller.Type.STICK) continue;

        	Component[] components = ca[i].getComponents();
            for(int j=0;j<components.length;j++){
            	if(!components[j].isAnalog()) {
        			if(components[j].getPollData()==1) {
        				if(components[j].getIdentifier()==Identifier.Button._0) {
        					// square
            			}
        				if(components[j].getIdentifier()==Identifier.Button._1) {
        					// x
        					this.toggleATC();
        				}
        				if(components[j].getIdentifier()==Identifier.Button._2) {
           					// circle
        					//System.out.print(components[j].getPollData()+"\t");
        				}
        				if(components[j].getIdentifier()==Identifier.Button._3) {
           					// triangle
                			targetPose.set(homePose);
                			isDirty=true;
        				}
            		}
            	} else {
	            	if(components[j].getIdentifier()==Identifier.Axis.Z) {
	            		// right analog stick, + is right -1 is left
	            		double v = components[j].getPollData();
	            		if(Math.abs(v)<deadzone) continue;
	            		isDirty=true;
	            		Matrix4d temp = new Matrix4d();
	            		temp.rotY(v*scaleTurn);
	            		targetPose.mul(temp);
	            	}
	            	if(components[j].getIdentifier()==Identifier.Axis.RZ) {
	            		// right analog stick, + is down -1 is up
	            		double v = components[j].getPollData();
	            		if(Math.abs(v)<deadzone) continue;
	            		isDirty=true;
	            		Matrix4d temp = new Matrix4d();
	            		temp.rotX(v*scaleTurn);
	            		targetPose.mul(temp);
	            	}
	            	
	            	if(components[j].getIdentifier()==Identifier.Axis.RY) {
	            		// right trigger, +1 is pressed -1 is unpressed
	            		double v = components[j].getPollData();
	            		if(Math.abs(v)<-1+deadzone) continue;
	            		isDirty=true;
	            		targetPose.m23-=((v+1)/2)*scale;
	            	}
	            	if(components[j].getIdentifier()==Identifier.Axis.RX) {
	            		// left trigger, +1 is pressed -1 is unpressed
	            		double v = components[j].getPollData();
	            		if(Math.abs(v)<-1+deadzone) continue;
	            		isDirty=true;
	            		targetPose.m23+=((v+1)/2)*scale;
	            	}
	            	if(components[j].getIdentifier()==Identifier.Axis.X) {
	            		// left analog stick, +1 is right -1 is left
	            		double v = components[j].getPollData();
	            		if(Math.abs(v)<deadzone) continue;
	            		isDirty=true;
	            		targetPose.m13+=v*scale;
	            	}
	            	if(components[j].getIdentifier()==Identifier.Axis.Y) {
	            		// left analog stick, -1 is up +1 is down
	            		double v = components[j].getPollData();
	            		if(Math.abs(v)<deadzone) continue;
	            		isDirty=true;
	            		targetPose.m03+=v*scale;
	            	}
            	}
            	/*System.out.print("\t"+components[j].getName()+
            			":"+(components[j].isAnalog()?"Abs":"Rel")+
            			":"+(components[j].isAnalog()?"An":"Di")+
               			":"+(components[j].getPollData()));*/
        	}
        }
        
        return isDirty;
	}

	/**
	 * Perform a sanity check.  Make sure the angles in the keyframe are within the joint range limits. 
	 * @param keyframe
	 * @return
	 */
	public boolean keyframeAnglesAreOK(DHKeyframe keyframe) {
		Iterator<DHLink> i = this.links.iterator();
		int j=0;
		while(i.hasNext()) {
			DHLink link = i.next();
			if((link.flags & DHLink.READ_ONLY_THETA)==0) {
				double v = keyframe.fkValues[j++];
				if(link.rangeMax<v || link.rangeMin>v) {
					System.out.println("FK theta "+j+":"+v+" out ("+link.rangeMin+" to "+link.rangeMax+")");
					return false;
				}
			}
			if((link.flags & DHLink.READ_ONLY_D    )==0) {
				double v = keyframe.fkValues[j++];
				if(link.rangeMax<v || link.rangeMin>v) {
					System.out.println("FK D "+j+":"+v+" out ("+link.rangeMin+" to "+link.rangeMax+")");
					return false;
				}
			}
			if((link.flags & DHLink.READ_ONLY_ALPHA)==0) {
				double v = keyframe.fkValues[j++];
				if(link.rangeMax<v || link.rangeMin>v) {
					System.out.println("FK alpha "+j+":"+v+" out ("+link.rangeMin+" to "+link.rangeMax+")");
					return false;
				}
			}
			if((link.flags & DHLink.READ_ONLY_R    )==0) {
				double v = keyframe.fkValues[j++];
				if(link.rangeMax<v || link.rangeMin>v) {
					System.out.println("FK R "+j+":"+v+" out ("+link.rangeMin+" to "+link.rangeMax+")");
					return false;
				}
			}
		}
		
    	return true;
	}
	
	
	/**
	 * Set the robot's FK values to the keyframe values and then refresh the pose.
	 * @param keyframe
	 */
	public void setRobotPose(DHKeyframe keyframe) {
		if(poseNow!=keyframe) {
			poseNow.set(keyframe);
		}
		Iterator<DHLink> i = this.links.iterator();
		int j=0;
		while(i.hasNext()) {
			DHLink link = i.next();
			if((link.flags & DHLink.READ_ONLY_THETA)==0) link.theta = keyframe.fkValues[j++];
			if((link.flags & DHLink.READ_ONLY_D    )==0) link.d     = keyframe.fkValues[j++];
			if((link.flags & DHLink.READ_ONLY_ALPHA)==0) link.alpha = keyframe.fkValues[j++];
			if((link.flags & DHLink.READ_ONLY_R    )==0) link.r     = keyframe.fkValues[j++];
		}
		
    	this.refreshPose();
	}
	
	/**
	 * Get the robot's FK values to the keyframe.
	 * @param keyframe
	 */
	public DHKeyframe getRobotPose() {
		DHKeyframe keyframe = (DHKeyframe)this.createKeyframe();
		keyframe.set(poseNow);
		return keyframe;
	}
}
