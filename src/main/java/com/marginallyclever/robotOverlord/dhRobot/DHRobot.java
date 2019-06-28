package com.marginallyclever.robotOverlord.dhRobot;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
	 * {@value targetPose} the pose the IK is trying to move towards.  Includes the tool held by the robot. 
	 */
	public Matrix4d targetPose;

	/**
	 * {@value targetPose} the pose the IK would solve when the robot is at "home" position.
	 */
	public Matrix4d homePose;
	
	/**
	 * {@value dhTool} a DHTool current attached to the arm.
	 */
	protected  DHTool dhTool;
	
	/**
	 * {@value drawSkeleton} true if the skeleton should be visualized on screen.  Default is false.
	 */
	protected boolean drawSkeleton;
	
	/**
	 * {@value poseHistory} records DHIKSolver.getSolutionSize() wide showing the robot's pose over time.
	 */
	protected Queue<double[]> poseHistory;
	
	public static final int POSE_HISTORY_LENGTH = 500; 
	
	/**
	 * The solver for this type of robot
	 */
	protected DHIKSolver solver;
	
	/**
	 * Used by inputUpdate to solve pose and instruct robot where to go.
	 */
	protected DHKeyframe solutionKeyframe;
	
	
	public DHRobot() {
		super();
		links = new LinkedList<DHLink>();
		endMatrix = new Matrix4d();
		targetPose = new Matrix4d();
		homePose = new Matrix4d();
		
		drawSkeleton=false;
		setupLinks();
		calculateJacobians();
		
		solver = this.getSolverIK();
		
		poseNow = (DHKeyframe)createKeyframe();
		
		solutionKeyframe = (DHKeyframe)createKeyframe();
		
		refreshPose();

		homePose.set(endMatrix);
		targetPose.set(endMatrix);
		
		poseHistory = new LinkedList<double[]>();
		dhTool = new DHTool();  // default tool = no tool
	}
	
	/**
	 * @see https://arxiv.org/ftp/arxiv/papers/1707/1707.04821.pdf
	 */
	public void calculateJacobians() {
		// pose was refreshed at the end of setupLinks()
		Vector3d P = new Vector3d(
				endMatrix.m03,
				endMatrix.m13,
				endMatrix.m23);
		
		Vector3d [][] a = new Vector3d[2][links.size()];
		int j=0;
		Iterator<DHLink> i = links.iterator();
		while(i.hasNext()) {
			DHLink link = i.next();
			Vector3d v = new Vector3d(
					link.poseCumulative.m03,
					link.poseCumulative.m13,
					link.poseCumulative.m23);
			Vector3d z = new Vector3d(
					link.poseCumulative.m02,
					link.poseCumulative.m12,
					link.poseCumulative.m22);
			Vector3d temp=new Vector3d();
			temp.set(P);
			temp.sub(v);
			Vector3d result = new Vector3d();
			result.cross(z, temp);
			a[0][j]=result;
			a[1][j]=z;
			++j;
		}
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
				if(dhTool!=null) {
					dhTool.dhLinkEquivalent.renderPose(gl2);
				}
			gl2.glPopMatrix();
			
			MatrixHelper.drawMatrix2(gl2, 
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
		if(dhTool!=null) {
			// update matrix
			dhTool.dhLinkEquivalent.refreshPoseMatrix();
			// find cumulative matrix
			endMatrix.mul(dhTool.dhLinkEquivalent.pose);
			dhTool.dhLinkEquivalent.poseCumulative.set(endMatrix);
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
				setTool((DHTool)po);
			}
		}
	}
	
	
	public void setTool(DHTool arg0) {
		removeTool();
		dhTool = arg0;
		if(arg0!=null) {
			// add the tool offset to the targetPose.
			dhTool.dhLinkEquivalent.refreshPoseMatrix();
			Matrix4d toolPose = new Matrix4d(dhTool.dhLinkEquivalent.pose);
			targetPose.mul(toolPose);
			// tell the tool it is being held.
			arg0.heldBy = this;
		}
		this.panel.updateActiveTool(dhTool);
	}
	
	public void removeTool() {
		if(dhTool!=null) {
			// subtract the tool offset from the targetPose.
			dhTool.dhLinkEquivalent.refreshPoseMatrix();
			Matrix4d inverseToolPose = new Matrix4d(dhTool.dhLinkEquivalent.pose);
			inverseToolPose.invert();
			targetPose.mul(inverseToolPose);
			// tell the tool it is no longer held.
			dhTool.heldBy = null;
		}
		dhTool = null;
	}
	
	public DHTool getCurrentTool() {
		return dhTool;
	}
	
	/**
	 * Note: Is called by Robot constructor, so .
	 */
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
        	solver.solve(this,targetPose,solutionKeyframe);
        	if(solver.solutionFlag==DHIKSolver.ONE_SOLUTION) {
        		if(keyframeAnglesAreOK(solutionKeyframe)) {
	        		// Solved!  update robot pose with fk.
        			
	        		if(connection!=null && connection.isOpen()) {
	        			if(isReadyToReceive) {
	        				// If the sum of the absolute difference of each joint is smaller than some epsilon, don't send it to the robot.
	        				double sum=0;
	        				//String message="";
	        				for(int i=0;i<solutionKeyframe.fkValues.length;++i) {
	        					double v = Math.abs(poseNow.fkValues[i]-solutionKeyframe.fkValues[i]);
	        					sum += v;
	        					//message += (long)(v*1000)+" \t";
	        				}
        					//System.out.println(AnsiColors.RED+message+AnsiColors.RESET);
	        				if(sum>0.01) {  // mm?
	        					// update the live connected robot, which will come back through dataAvailable() to update the pose.
		        				sendPoseToRobot(solutionKeyframe);
		        				isReadyToReceive=false;
	        				}
		        		}
	        		} else {
	        			// no connected robot, update the pose directly.
	            		this.setRobotPose(solutionKeyframe);
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
					new Vector3d(5,0,0),
					new Vector3d(0,5,0),
					new Vector3d(0,0,5));
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
		final double scale=0.4;
		final double scaleTurn=0.15;
		final double DEADZONE=0.1;
		
        for(int i=0;i<ca.length;i++){
        	//System.out.println(ca[i].getType());
        	if(ca[i].getType()!=Controller.Type.STICK) continue;

        	Component[] components = ca[i].getComponents();
            for(int j=0;j<components.length;j++){
            	/*
            	System.out.println("\t"+components[j].getName()+
            			":"+components[j].getIdentifier().getName()+
            			":"+(components[j].isAnalog()?"Abs":"Rel")+
            			":"+(components[j].isAnalog()?"Analog":"Digital")+
            			":"+(components[j].getDeadZone())+
               			":"+(components[j].getPollData()));*/
            	
            	if(!components[j].isAnalog()) {
        			if(components[j].getPollData()==1) {
        				if(components[j].getIdentifier()==Identifier.Button._0) {
        					// square
            			}
        				if(components[j].getIdentifier()==Identifier.Button._1) {
        					// x
        					//this.toggleATC();
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
        				if(components[j].getIdentifier()==Identifier.Button._4) {
    	            		// right analog stick, + is right -1 is left
    	            		isDirty=true;
    	            		double v = scaleTurn;
    	            		if(dhTool!=null && dhTool.dhLinkEquivalent.r!=0) {
    	            			v=4/dhTool.dhLinkEquivalent.r;
    	            		}
    	            		Matrix4d temp = new Matrix4d();
    	            		temp.rotZ(v*scaleTurn);
    	            		targetPose.mul(temp);
        				}
        				if(components[j].getIdentifier()==Identifier.Button._5) {
    	            		// right analog stick, + is right -1 is left
    	            		isDirty=true;
    	            		double v = scaleTurn;
    	            		if(dhTool!=null && dhTool.dhLinkEquivalent.r!=0) {
    	            			v=4/dhTool.dhLinkEquivalent.r;
    	            		}
    	            		Matrix4d temp = new Matrix4d();
    	            		temp.rotZ(v*-scaleTurn);
    	            		targetPose.mul(temp);
        				}
            		}
            	} else {
	            	if(components[j].getIdentifier()==Identifier.Axis.Z) {
	            		// right analog stick, + is right -1 is left
	            		double v = components[j].getPollData();
	            		if(Math.abs(v)<DEADZONE) continue;
	            		isDirty=true;
	            		Matrix4d temp = new Matrix4d();
	            		temp.rotY(v*scaleTurn);
	            		targetPose.mul(temp);
	            	}
	            	if(components[j].getIdentifier()==Identifier.Axis.RZ) {
	            		// right analog stick, + is down -1 is up
	            		double v = components[j].getPollData();
	            		if(Math.abs(v)<DEADZONE) continue;
	            		isDirty=true;
	            		Matrix4d temp = new Matrix4d();
	            		temp.rotX(v*scaleTurn);
	            		targetPose.mul(temp);
	            	}
	            	
	            	if(components[j].getIdentifier()==Identifier.Axis.RY) {
	            		// right trigger, +1 is pressed -1 is unpressed
	            		double v = components[j].getPollData();
	            		if(Math.abs(v)<-1+DEADZONE) continue;
	            		isDirty=true;
	            		targetPose.m23-=((v+1)/2)*scale;
	            	}
	            	if(components[j].getIdentifier()==Identifier.Axis.RX) {
	            		// left trigger, +1 is pressed -1 is unpressed
	            		double v = components[j].getPollData();
	            		if(Math.abs(v)<-1+DEADZONE) continue;
	            		isDirty=true;
	            		targetPose.m23+=((v+1)/2)*scale;
	            	}
	            	if(components[j].getIdentifier()==Identifier.Axis.X) {
	            		// left analog stick, +1 is right -1 is left
	            		double v = components[j].getPollData();
	            		if(Math.abs(v)<DEADZONE) continue;
	            		isDirty=true;
	            		targetPose.m13+=v*scale;
	            	}
	            	if(components[j].getIdentifier()==Identifier.Axis.Y) {
	            		// left analog stick, -1 is up +1 is down
	            		double v = components[j].getPollData();
	            		if(Math.abs(v)<DEADZONE) continue;
	            		isDirty=true;
	            		targetPose.m03+=v*scale;
	            	}
            	}
        	}
        }
        
        if(dhTool!=null) {
        	isDirty |= dhTool.directDrive();
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

		// Record the pose history.  Might be useful in future.
		if(poseHistory.size()>=POSE_HISTORY_LENGTH) {
			poseHistory.remove();
		}
		poseHistory.add(keyframe.fkValues);
		
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
	/*
	@Override
	public boolean hasPickName(int name) {
		// if any of the DHLinks have this pick name, return true.
		return pickName==name;
	}*/
}
