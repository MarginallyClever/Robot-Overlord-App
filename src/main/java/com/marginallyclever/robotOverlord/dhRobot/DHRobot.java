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
	 * {@value targetPose} the pose the IK is trying to move towards.  Includes the tool held by the robot. 
	 */
	public Matrix4d targetPose;

	/**
	 * {@value oldPose} the last valid pose.  Used in case the IK solver fails to solve the targetPose. 
	 */
	public Matrix4d oldPose;

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
	
	public static final int POSE_HISTORY_LENGTH = 500; 
	
	/**
	 * The solver for this type of robot
	 */
	protected DHIKSolver solver;
	
	/**
	 * Used by inputUpdate to solve pose and instruct robot where to go.
	 */
	protected DHKeyframe solutionKeyframe;
	
	protected DHRobotRecording record;

	// A record of the state of the human input device
	public final static int MAX_KEYS = 20;
	public double [] keyState = new double[MAX_KEYS];
	

	public DHRobot() {
		super();
		links = new LinkedList<DHLink>();
		endMatrix = new Matrix4d();
		targetPose = new Matrix4d();
		oldPose = new Matrix4d();
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
		
		dhTool = new DHTool();  // default tool = no tool
		
		record = new DHRobotRecording(this);
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
	 * Note: Is called by Robot constructor, so it must use getSolverIK().
	 */
	@Override
	public RobotKeyframe createKeyframe() {
		return new DHKeyframe(getSolverIK().getSolutionSize());
	}

	/**
	 * @return true if targetPose changes.
	 */
	public boolean driveFromKeyState() {
		boolean isDirty=false;
		final double scale=0.4;
		final double scaleDolly=0.4;
		final double scaleTurn=0.15;
		
		if(keyState[0]==1) {}  // square
		if(keyState[1]==1) {  // X
			//this.toggleATC();
		}
		if(keyState[2]==1) {}  // circle
		if(keyState[3]==1) {// triangle
			targetPose.set(homePose);
			isDirty=true;
		}
		if(keyState[4]==1) {  // R1
    		isDirty=true;
    		double v = 1;
    		if(dhTool!=null && dhTool.dhLinkEquivalent.r>1) {
    			v/=dhTool.dhLinkEquivalent.r;
    		}
    		Matrix4d temp = new Matrix4d();
    		temp.rotZ(v*scaleTurn);
    		targetPose.mul(temp);
    	}
		if(keyState[5]==1) {  // L1
    		isDirty=true;
    		double v = 1;
    		if(dhTool!=null && dhTool.dhLinkEquivalent.r>1) {
    			v/=dhTool.dhLinkEquivalent.r;
    		}
    		Matrix4d temp = new Matrix4d();
    		temp.rotZ(-v*scaleTurn);
    		targetPose.mul(temp);
		}
		
		int dD=(int)keyState[8];
		if(dD!=0) {
			dhTool.dhLinkEquivalent.d+=dD*scaleDolly;
			if(dhTool.dhLinkEquivalent.d<0) dhTool.dhLinkEquivalent.d=0;
			isDirty=true;
		}		
		int dR=(int)keyState[9];
		if(dR!=0) {
			dhTool.dhLinkEquivalent.r+=dR*scale;
			if(dhTool.dhLinkEquivalent.r<0) dhTool.dhLinkEquivalent.r=0;
			isDirty=true;
		}
		
		if(keyState[10]!=0) {  // right stick, right/left
			// right analog stick, + is right -1 is left
    		isDirty=true;
    		Matrix4d temp = new Matrix4d();
    		temp.rotY(keyState[10]*scaleTurn);
    		targetPose.mul(temp);
		}
		if(keyState[11]!=0) {  // right stick, down/up
    		isDirty=true;
    		Matrix4d temp = new Matrix4d();
    		temp.rotX(keyState[11]*scaleTurn);
    		targetPose.mul(temp);
    	}
		if(keyState[12]!=-1) {  // r2, +1 is pressed -1 is unpressed
    		isDirty=true;
    		targetPose.m23-=((keyState[12]+1)/2)*scale;
		}
		if(keyState[13]!=-1) { // l2, +1 is pressed -1 is unpressed
    		isDirty=true;
    		targetPose.m23+=((keyState[13]+1)/2)*scale;
		}
		if(keyState[14]!=0) {  // left stick, right/left
    		isDirty=true;
    		targetPose.m13+=keyState[14]*scale;
		}
		if(keyState[15]!=0) {  // left stick, down/up
    		isDirty=true;
    		targetPose.m03+=keyState[15]*scale;
		}

        if(dhTool!=null) {
        	isDirty |= dhTool.directDrive(keyState);
        }
        
        return isDirty;
	}
	
	
	protected void processHumanInput() {
		Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
		
		for(int i=0;i<keyState.length;++i) {
			keyState[i]=0;
		}
		
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
        				if(components[j].getIdentifier()==Identifier.Button._0) keyState[0] = 1;  // square
        				if(components[j].getIdentifier()==Identifier.Button._1) keyState[1] = 1;  // x
        				if(components[j].getIdentifier()==Identifier.Button._2) keyState[2] = 1;  // circle
        				if(components[j].getIdentifier()==Identifier.Button._3) keyState[3] = 1;  // triangle
        				if(components[j].getIdentifier()==Identifier.Button._4) keyState[4] = 1;  // L1?
        				if(components[j].getIdentifier()==Identifier.Button._5) keyState[5] = 1;  // R1?
        				if(components[j].getIdentifier()==Identifier.Button._8) keyState[6] = 1;  // share button
        				if(components[j].getIdentifier()==Identifier.Button._9) keyState[7] = 1;  // option button
            		}
    				if(components[j].getIdentifier()==Identifier.Axis.POV) {
    					// D-pad buttons
    					float pollData = components[j].getPollData();
							 if(pollData == Component.POV.DOWN ) keyState[8] = -1;
						else if(pollData == Component.POV.UP   ) keyState[8] =  1;
    					else if(pollData == Component.POV.LEFT ) keyState[9] = -1;
    					else if(pollData == Component.POV.RIGHT) keyState[9] =  1;
    				}
            	} else {
            		double v = components[j].getPollData();
            		final double DEADZONE=0.1;
            		double deadzone = DEADZONE;  // components[j].getDeadZone() is very small?
            			 if(v> deadzone) v=(v-deadzone)/(1.0-deadzone);  // scale 0....1
            		else if(v<-deadzone) v=(v+deadzone)/(1.0-deadzone);  // scale 0...-1
            		else continue;  // inside dead zone, ignore.
            		
	            	if(components[j].getIdentifier()==Identifier.Axis.Z ) keyState[10]=v;  // right analog stick, + is right -1 is left
	            	if(components[j].getIdentifier()==Identifier.Axis.RZ) keyState[11]=v;  // right analog stick, + is down -1 is up
	            	if(components[j].getIdentifier()==Identifier.Axis.RY) keyState[12]=v;  // R2, +1 is pressed -1 is unpressed
	            	if(components[j].getIdentifier()==Identifier.Axis.RX) keyState[13]=v;  // L2, +1 is pressed -1 is unpressed
	            	if(components[j].getIdentifier()==Identifier.Axis.X ) keyState[14]=v;  // left analog stick, +1 is right -1 is left
	            	if(components[j].getIdentifier()==Identifier.Axis.Y ) keyState[15]=v;  // left analog stick, -1 is up +1 is down
            	}
        	}
        }
	}
	
	@Override
	public void inputUpdate() {		
        boolean isDirty=false;

        record.step();
    	
        // If the move is illegal then I need a way to rewind.  Keep the old pose for rewinding.
        oldPose.set(targetPose);

        // update the keyState array based on input devices.
        processHumanInput();
        
        // manage the keyState
        record.manageArrayOfDoubles(keyState);
        // apply the keyState

        if(animationSpeed==0) {
        	// if we are in direct drive mode
        	isDirty=driveFromKeyState();
        }
        
        isDirty = record.manageBoolean(isDirty);
        
        if(isDirty) {
        	record.manageMatrix4d(targetPose);
	    	
        	// Attempt to solve IK
        	solver.solve(this,targetPose,solutionKeyframe);
        	if(solver.solutionFlag==DHIKSolver.ONE_SOLUTION) {
        		// Solved!  Are angles OK for this robot?
        		if(keyframeAnglesAreOK(solutionKeyframe)) {
	        		// Yes!  Are we connected to a live robot?        			
	        		if(connection!=null && connection.isOpen() && isReadyToReceive) {
	        			// Send our internal data to the robot.  Each robot probably has its own post-processor.
        				sendNewStateToRobot(solutionKeyframe);
        				// We'll let the robot set isReadyToReceive true when it can.  This prevents flooding the robot with data.
        				isReadyToReceive=false;
	        		} else {
	        			// No connected robot, update the pose directly.
	            		this.setRobotPose(solutionKeyframe);
	        		}
        		} else {
        			// Bad angles, robot out of permitted range.
            		targetPose.set(oldPose);
            	}
        	} else {
        		// No valid IK solution.
        		targetPose.set(oldPose);
        	}
        }
	}
	
	/**
	 * Robot is connected and ready to receive.  Send the current FK values to the robot.
	 * Post-process translate the FK values and send them, along with tool state, etc. 
	 * @param keyframe
	 */
	public abstract void sendNewStateToRobot(DHKeyframe keyframe);
	
	
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
					//System.out.println("FK theta "+j+":"+v+" out ("+link.rangeMin+" to "+link.rangeMax+")");
					return false;
				}
			}
			if((link.flags & DHLink.READ_ONLY_D    )==0) {
				double v = keyframe.fkValues[j++];
				if(link.rangeMax<v || link.rangeMin>v) {
					//System.out.println("FK D "+j+":"+v+" out ("+link.rangeMin+" to "+link.rangeMax+")");
					return false;
				}
			}
			if((link.flags & DHLink.READ_ONLY_ALPHA)==0) {
				double v = keyframe.fkValues[j++];
				if(link.rangeMax<v || link.rangeMin>v) {
					//System.out.println("FK alpha "+j+":"+v+" out ("+link.rangeMin+" to "+link.rangeMax+")");
					return false;
				}
			}
			if((link.flags & DHLink.READ_ONLY_R    )==0) {
				double v = keyframe.fkValues[j++];
				if(link.rangeMax<v || link.rangeMin>v) {
					//System.out.println("FK R "+j+":"+v+" out ("+link.rangeMin+" to "+link.rangeMax+")");
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
    	if(this.panel!=null) this.panel.updateEnd();
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

	public boolean isRecording() {
		return record.isRecording;
	}

	public void setRecording(boolean newIsRecording) {
		record.setRecording(newIsRecording);
		if(panel!=null) panel.buttonRecord.setText("Stop");
	}

	public boolean isPlaying() {
		return record.isPlaying;
	}

	public void setPlaying(boolean newIsPlaying) {
		record.setPlaying(newIsPlaying);
		if(panel!=null) panel.buttonPlay.setText("Stop");
	}
	
	public void recordingHasStopped() {
		if(panel!=null) panel.buttonRecord.setText("Record");
	}
	public void playingHasStopped() {
		if(panel!=null) panel.buttonPlay.setText("Play");
	}
}
