package com.marginallyclever.robotOverlord.dhRobot;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import javax.vecmath.Point3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.InputManager;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.physicalObject.PhysicalObject;
import com.marginallyclever.robotOverlord.robot.Robot;
import com.marginallyclever.robotOverlord.robot.RobotKeyframe;
import com.marginallyclever.robotOverlord.world.World;

/**
 * A robot designed using D-H parameters.
 * @author Dan Royer
 */
public abstract class DHRobot extends Robot {
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
	protected boolean drawAsSelected;
	
	public static final int POSE_HISTORY_LENGTH = 500; 
	
	/**
	 * The solver for this type of robot
	 */
	protected DHIKSolver solver;
	
	/**
	 * Used by inputUpdate to solve pose and instruct robot where to go.
	 */
	protected DHKeyframe solutionKeyframe;
	
	protected boolean showBones;  // show D-H representation of each link
	protected boolean showPhysics;  // show bounding boxes of each link
	protected boolean showAngles;  // show current angle and limit of each link
	boolean rotateOnWorldAxies;  // which style of rotation?
	
	protected int hitBox1, hitBox2;  // display which hitboxes are colliding

	public DHRobot() {
		super();
		
		setShowBones(false);
		setShowPhysics(false);
		setShowAngles(true);
		rotateOnWorldAxies=false;
		
		links = new LinkedList<DHLink>();
		endMatrix = new Matrix4d();
		targetPose = new Matrix4d();
		oldPose = new Matrix4d();
		homePose = new Matrix4d();
		
		drawAsSelected=false;
		hitBox1=-1;
		hitBox2=-1;
		setupLinks();
		calculateJacobians();
		
		solver = this.getSolverIK();
		
		poseNow = (DHKeyframe)createKeyframe();
		solutionKeyframe = (DHKeyframe)createKeyframe();
		
		refreshPose();

		homePose.set(endMatrix);
		targetPose.set(endMatrix);
		
		dhTool = new DHTool();  // default tool = no tool
	}
	
	/**
	 * This is not how jacobians are calculated.
	 * Jacobian is a matrix of equations, not a single solution.
	 * See also https://arxiv.org/ftp/arxiv/papers/1707/1707.04821.pdf
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
			Vector3d linkP = new Vector3d(
					link.poseCumulative.m03,
					link.poseCumulative.m13,
					link.poseCumulative.m23);
			Vector3d z = new Vector3d(
					link.poseCumulative.m02,
					link.poseCumulative.m12,
					link.poseCumulative.m22);
			Vector3d temp=new Vector3d();
			temp.sub(P,linkP);
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
		if(!drawAsSelected) return;

		boolean isDepth = gl2.glIsEnabled(GL2.GL_DEPTH_TEST);
		boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_DEPTH_TEST);
		gl2.glDisable(GL2.GL_LIGHTING);

		gl2.glPushMatrix();
			Vector3d position = this.getPosition();
			gl2.glTranslated(position.x, position.y, position.z);
			
			gl2.glPushMatrix();
				Iterator<DHLink> i = links.iterator();
				int j=0;
				while(i.hasNext()) {
					DHLink link = i.next();
					if(showBones) link.renderBones(gl2);
					if(showAngles) link.renderAngles(gl2);
					if(showPhysics && link.model != null) {
						if(j==hitBox1 || j==hitBox2) {
							gl2.glColor4d(1,0  ,0.8,0.15);
						} else {
							gl2.glColor4d(1,0.8,0  ,0.15);
						}
						PrimitiveSolids.drawBox(gl2,
								link.model.getBoundBottom(),
								link.model.getBoundTop());
					}
					link.applyMatrix(gl2);
					++j;
				}
				if(dhTool!=null) {
					if(showBones) dhTool.dhLinkEquivalent.renderBones(gl2);
					if(showAngles) dhTool.dhLinkEquivalent.renderAngles(gl2);/*
					if(showPhysics && dhTool.dhLinkEquivalent.model != null) {
						gl2.glColor4d(1,0,0.8,0.15);
						PrimitiveSolids.drawBox(gl2,
								dhTool.dhLinkEquivalent.model.getBoundBottom(),
								dhTool.dhLinkEquivalent.model.getBoundTop());
					}*/
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
			link.poseCumulative.set(endMatrix);
			// find cumulative matrix
			endMatrix.mul(link.pose);
		}
		if(dhTool!=null) {
			dhTool.refreshPose(endMatrix);
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
		dhTool.setParent(this);
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
			dhTool.setParent(null);
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
		
		if(InputManager.keyState[0]==1) {}  // square
		if(InputManager.keyState[1]==1) {  // X
			//this.toggleATC();
		}
		if(InputManager.keyState[2]==1) {}  // circle
		if(InputManager.keyState[3]==1) {// triangle
			targetPose.set(homePose);
			isDirty=true;
		}
		if(InputManager.keyState[4]==1) {  // R1
			if(canTargetPoseRotateZ()) {
	    		isDirty=true;
	    		double vv = 1;
	    		if(dhTool!=null && dhTool.dhLinkEquivalent.r>1) {
	    			vv/=dhTool.dhLinkEquivalent.r;
	    		}
	    		Matrix4d temp = new Matrix4d();
	    		temp.rotZ(vv*scaleTurn);
	    		if(rotateOnWorldAxies) {
	    			Vector4d v=new Vector4d();
	    			targetPose.getColumn(3, v);
	    			targetPose.setTranslation(new Vector3d(0,0,0));
	    			targetPose.mul(temp,targetPose);
	    			targetPose.setTranslation(new Vector3d(v.x,v.y,v.z));
	    		} else {
	    			targetPose.mul(temp);
	    		}
			}
    	}
		if(InputManager.keyState[5]==1) {  // L1
			if(canTargetPoseRotateZ()) {
	    		isDirty=true;
	    		double vv = 1;
	    		if(dhTool!=null && dhTool.dhLinkEquivalent.r>1) {
	    			vv/=dhTool.dhLinkEquivalent.r;
	    		}
	    		Matrix4d temp = new Matrix4d();
	    		temp.rotZ(-vv*scaleTurn);
	    		if(rotateOnWorldAxies) {
	    			Vector4d v=new Vector4d();
	    			targetPose.getColumn(3, v);
	    			targetPose.setTranslation(new Vector3d(0,0,0));
	    			targetPose.mul(temp,targetPose);
	    			targetPose.setTranslation(new Vector3d(v.x,v.y,v.z));
	    		} else {
	    			targetPose.mul(temp);
	    		}
			}
		}
		
		int dD=(int)InputManager.keyState[8];
		if(dD!=0) {
			dhTool.dhLinkEquivalent.d+=dD*scaleDolly;
			if(dhTool.dhLinkEquivalent.d<0) dhTool.dhLinkEquivalent.d=0;
			isDirty=true;
		}		
		int dR=(int)InputManager.keyState[9];
		if(dR!=0) {
			dhTool.dhLinkEquivalent.r+=dR*scale;
			if(dhTool.dhLinkEquivalent.r<0) dhTool.dhLinkEquivalent.r=0;
			isDirty=true;
		}
		
		if(InputManager.keyState[10]!=0) {  // right stick, right/left
			// right analog stick, + is right -1 is left
			if(canTargetPoseRotateY()) {
	    		isDirty=true;
	    		Matrix4d temp = new Matrix4d();
	    		temp.rotY(InputManager.keyState[10]*scaleTurn);
	    		if(rotateOnWorldAxies) {
	    			Vector4d v=new Vector4d();
	    			targetPose.getColumn(3, v);
	    			targetPose.setTranslation(new Vector3d(0,0,0));
	    			targetPose.mul(temp,targetPose);
	    			targetPose.setTranslation(new Vector3d(v.x,v.y,v.z));
	    		} else {
	    			targetPose.mul(temp);
	    		}
			}
		}
		if(InputManager.keyState[11]!=0) {  // right stick, down/up
			if(canTargetPoseRotateX()) {
	    		isDirty=true;
	    		Matrix4d temp = new Matrix4d();
	    		temp.rotX(InputManager.keyState[11]*scaleTurn);
	    		if(rotateOnWorldAxies) {
	    			Vector4d v=new Vector4d();
	    			targetPose.getColumn(3, v);
	    			targetPose.setTranslation(new Vector3d(0,0,0));
	    			targetPose.mul(temp,targetPose);
	    			targetPose.setTranslation(new Vector3d(v.x,v.y,v.z));
	    		} else {
	    			targetPose.mul(temp);
	    			/*
		    		// experiment to improve rotation
					Matrix4d temp2 = new Matrix4d(links.get(links.size()-2).poseCumulative);
		    		temp2.setTranslation(new Vector3d(0,0,0));
		    		Matrix4d iMat = new Matrix4d(temp2);
		    		iMat.invert();
	    			temp2.mul(temp,temp2);
	    			Vector4d v=new Vector4d();
	    			targetPose.getColumn(3, v);
	    			targetPose.setTranslation(new Vector3d(0,0,0));
	    			targetPose.mul(iMat,targetPose);
	    			targetPose.mul(temp2,targetPose);
	    			targetPose.setTranslation(new Vector3d(v.x,v.y,v.z));
	    			*/
	    		}
			}
    	}
		if(InputManager.keyState[12]!=-1) {  // r2, +1 is pressed -1 is unpressed
    		isDirty=true;
    		targetPose.m23+=((InputManager.keyState[12]+1)/2)*scale;
		}
		if(InputManager.keyState[13]!=-1) { // l2, +1 is pressed -1 is unpressed
    		isDirty=true;
    		targetPose.m23-=((InputManager.keyState[13]+1)/2)*scale;
		}
		if(InputManager.keyState[14]!=0) {  // left stick, right/left
    		isDirty=true;
    		targetPose.m13+=InputManager.keyState[14]*scale;
		}
		if(InputManager.keyState[15]!=0) {  // left stick, down/up
    		isDirty=true;
    		targetPose.m03+=InputManager.keyState[15]*scale;
		}

        if(dhTool!=null) {
        	isDirty |= dhTool.directDrive();
        }
        
        return isDirty;
	}
	
	
	/**
	 * Direct Drive Mode means that we're not playing animation of any kind.
	 * That means no gcode running, no scrubbing on a timeline, or any other kind of external control.
	 * @return true if we're in direct drive mode. 
	 */
	protected boolean inDirectDriveMode() {
		return animationSpeed==0;
	}
	
	
	@Override
	public void update(double dt) {
		//update1(dt);
		update2(dt);
	}
	
	protected void update2(double dt) {
        // If the move is illegal then I need a way to rewind.  Keep the old pose for rewinding.
        oldPose.set(targetPose);

        if(inDirectDriveMode()) {
        	if(driveFromKeyState()) {
            	// Attempt to solve IK
            	solver.solve(this,targetPose,solutionKeyframe);
            	if(solver.solutionFlag==DHIKSolver.ONE_SOLUTION) {
            		// Solved!  Are angles OK for this robot?
            		if(sanityCheck(solutionKeyframe)) {
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
            			// failed sanity check
                		targetPose.set(oldPose);
                	}
            	} else {
            		// No valid IK solution.
            		targetPose.set(oldPose);
            	}
        	}
        } else {
        	// not in direct drive mode.
        	// are we playing a gcode file?

        	// The normal process is to send as many commands as fast the brain can handle.
        	// it will then queue the commands and - where appropriate - optimize speed between commands.
        	// The Arduino Mega in the Sixi 2 is too slow to calculate IK so they have to be done on the PC.
        	// The Arduino Mega in the Sixi 2 only understands joint-angle values in the robot arm.
        	//
        	// If I send only the (potentially distant) target pose as joint-angle values, the robot will move in large arcs.
        	// I may need to send several sub-moves, close enough together that the result looks straight.  I don't yet know how small
        	// those moves will be.
        	//
        	// I cannot algorithmically interpolate between joint-angle values in a straight line.
        	// I can algorithmically interpolate between two matrixes (see com.marginallyclever.convenience.MatrixHelper.interpolate())
        	// For this reason I store target poses as matrixes in PC instead of joint-angle values.
        	// 
        	// Inverse Kinematics (IK) in the PC will then convert a matrix into angle values.
        	//
        	// If path-splitting is enabled {
	        	// I can compare the distance/angle between the two matrixes in the list.
	        	// If the distance/angle is more than some value, I want to split the movement into sub-commands.
	        	// Splitting commands means finding the matrix at the split points, which means interpolating between two matrixes.  
	        	// MatrixHelper.interpolate() can be used here.
        		// then the list gets bigger to include the intermediate matrixes
        	// }
        	// Once I have the list matrixes, I run the IK solver on each matrix, which generates a list of angle values.
        	// Then I send the matrixes to the robot as fast as it can handle them.
        }
	}
	
	protected void update1(double dt) {
        boolean isDirty=false;
        
        // If the move is illegal then I need a way to rewind.  Keep the old pose for rewinding.
        oldPose.set(targetPose);

        if(inDirectDriveMode()) {
        	isDirty=driveFromKeyState();
        }
        
        //isDirty = RecordingManager.manageBoolean(isDirty);
        if(isDirty) {
        	//RecordingManager.manageMatrix4d(targetPose);
	    	
        	// Attempt to solve IK
        	solver.solve(this,targetPose,solutionKeyframe);
        	if(solver.solutionFlag==DHIKSolver.ONE_SOLUTION) {
        		// Solved!  Are angles OK for this robot?
        		if(sanityCheck(solutionKeyframe)) {
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
        			// failed sanity check
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
/*
		// experiment to improve rotation.  
		Matrix4d temp = new Matrix4d();
		final double scaleTurn=0.15;
		temp.rotX(1*scaleTurn);
		
    		Matrix4d temp2 = new Matrix4d(links.get(links.size()-2).poseCumulative);
    		//MatrixHelper.drawMatrix(gl2, temp2,15);
    		Vector4d v2=new Vector4d();
			temp2.getColumn(3, v2);
    		temp2.setTranslation(new Vector3d(0,0,0));
    		Matrix4d iMat = new Matrix4d(temp2);
    		iMat.invert();
			temp2.mul(temp,temp2);
			temp2.setTranslation(new Vector3d(v2.x,v2.y,v2.z));
			//MatrixHelper.drawMatrix(gl2, temp2,10);
    		temp2.setTranslation(new Vector3d(0,0,0));
			
			Vector4d v=new Vector4d();
			Matrix4d temp3 = new Matrix4d(targetPose);
			MatrixHelper.drawMatrix(gl2, temp3,10);
			temp3.getColumn(3, v);
			temp3.setTranslation(new Vector3d(0,0,0));
			temp3.mul(iMat);
			temp3.mul(temp2);
			temp3.setTranslation(new Vector3d(v.x,v.y,v.z));

			MatrixHelper.drawMatrix(gl2, temp3,5);
		*/
		
		
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
	

	public boolean sanityCheck(DHKeyframe keyframe) {
		if(!keyframeAnglesAreOK(keyframe)) return false;
		if(!selfCollision(keyframe)) return false;
		return true;
	}

	/**
	 * Test physical bounds of link N against all links <N-1 and all links >N+1
	 * We're using separating Axis Theorem.  See https://gamedev.stackexchange.com/questions/25397/obb-vs-obb-collision-detection
	 * @param keyframe the angles at time of test
	 * @return true if there are no collisions
	 */
	public boolean selfCollision(DHKeyframe keyframe) {
		boolean noCollision=true;
		// save the live pose
		DHKeyframe saveKeyframe = this.getRobotPose();
		// set the test pose
		this.setRobotPose(keyframe);

		hitBox1=-1;
		hitBox2=-1;
		
		int size=links.size();
		for(int i=0;i<size;++i) {
			if(links.get(i).model==null) continue;
			
			for(int j=i+3;j<size;++j) {
				if(links.get(j).model==null) continue;
				
				if(hasIntersection(links.get(i),links.get(j))) {
					//System.out.println("Intersect "+i+"/"+j+" (1)!");
					hitBox1=i;
					hitBox2=j;
					noCollision=false;
					break;
				}/*
				if(hasIntersection(links.get(j),links.get(i))) {
					System.out.println("Intersect "+i+"/"+j+" (2)!");
					hitBox1=i;
					hitBox2=j;
					noCollision=false;
					break;
				}*/
			}
			if(noCollision==false) {
				break;
			}
		}

		// set the live pose
		this.setRobotPose(saveKeyframe);
		
		return noCollision;
	}
	
	
	protected boolean hasIntersection(DHLink a,DHLink b) {
		// get the normals for the box of A, which happen to be the three vectors of the matrix for this joint pose.
		Vector3d [] n = new Vector3d[3];
		n[0] = new Vector3d(a.poseCumulative.m00,a.poseCumulative.m10,a.poseCumulative.m20);
		n[1] = new Vector3d(a.poseCumulative.m01,a.poseCumulative.m11,a.poseCumulative.m21);
		n[2] = new Vector3d(a.poseCumulative.m02,a.poseCumulative.m12,a.poseCumulative.m22);
		//System.out.println("matrix="+a.poseCumulative);
		
		//System.out.println("Acorners=");
		Point3d [] aCorners = getCornersForLink(a);
		//System.out.println("Bcorners=");
		Point3d [] bCorners = getCornersForLink(b);

		//String [] axis = {"X","Y","Z"};
		
		for(int i=0;i<n.length;++i) {
			// SATTest the normals of A against the 8 points of box A.
			// SATTest the normals of A against the 8 points of box B.
			// points of each box are a combination of the box's top/bottom values.
			double [] aLim = SATTest(n[i],aCorners);
			double [] bLim = SATTest(n[i],bCorners);
			//System.out.println("Lim "+axis[i]+" > "+n[i].x+"\t"+n[i].y+"\t"+n[i].z+" : "+aLim[0]+","+aLim[1]+" vs "+bLim[0]+","+bLim[1]);

			// if the two box projections do not overlap then there is no chance of a collision.
			if(!overlaps(aLim[0],aLim[1],bLim[0],bLim[1])) {
				//System.out.println("Miss");
				return false;
			}
		}
		
		// intersect!
		//System.out.println("Hit");
		return true;
	}
	
	/**
	 * find the 8 corners of the bounding box and transform them into world space.
	 * @param link the link that contains both the model bounds and the poseCumulative.
	 * @return the 8 transformed Point3d.
	 */
	protected Point3d [] getCornersForLink(DHLink link) {
		Point3d [] p = new Point3d[8];

		Point3d b=link.model.getBoundBottom();
		Point3d t=link.model.getBoundTop();
		
		p[0]=new Point3d(b.x,b.y,b.z);
		p[1]=new Point3d(b.x,b.y,t.z);
		p[2]=new Point3d(b.x,t.y,b.z);
		p[3]=new Point3d(b.x,t.y,t.z);
		p[4]=new Point3d(t.x,b.y,b.z);
		p[5]=new Point3d(t.x,b.y,t.z);
		p[6]=new Point3d(t.x,t.y,b.z);
		p[7]=new Point3d(t.x,t.y,t.z);

		for(int i=0;i<p.length;++i) {
			//System.out.print("\t"+p[i]);
			link.poseCumulative.transform(p[i]);
			//System.out.println(" >> "+p[i]);
		}
		
		return p;
	}
	
	protected boolean isBetween(double val,double bottom,double top) {
		return bottom <= val && val <= top;
	}

	protected boolean overlaps(double a0,double a1,double b0,double b1) {
		return isBetween(b0,a0,a1) || isBetween(a0,b0,b1); 		
	}
	
	protected double [] SATTest(Vector3d normal,Point3d [] corners) {
		double [] values = new double[2];
		values[0]= Double.MAX_VALUE;  // min value
		values[1]=-Double.MAX_VALUE;  // max value
		
		for(int i=0;i<corners.length;++i) {
			double dotProduct = corners[i].x * normal.x
							  + corners[i].y * normal.y
							  + corners[i].z * normal.z;
			if(values[0]>dotProduct) values[0]=dotProduct;
			if(values[1]<dotProduct) values[1]=dotProduct;
		}
		
		return values;
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
	
	@Override
	public void pick() {
		this.refreshPose();
		targetPose.set(endMatrix);
		drawAsSelected=true;
	}
	
	@Override
	public void unPick() {
		drawAsSelected=false;
	}
	

	public boolean isShowBones() {
		return showBones;
	}

	public void setShowBones(boolean showBones) {
		this.showBones = showBones;
		if(panel!=null) panel.setShowBones(showBones);
	}

	public void setShowBonesPassive(boolean showBones) {
		this.showBones = showBones;
	}

	public boolean isShowPhysics() {
		return showPhysics;
	}

	public void setShowPhysics(boolean showPhysics) {
		this.showPhysics = showPhysics;
		if(panel!=null) panel.setShowPhysics(showPhysics);
	}

	public void setShowPhysicsPassive(boolean showPhysics) {
		this.showPhysics = showPhysics;
	}

	public boolean isShowAngles() {
		return showAngles;
	}

	public void setShowAngles(boolean showAngles) {
		this.showAngles = showAngles;
		if(panel!=null) panel.setShowAngles(showAngles);
	}

	public void setShowAnglesPassive(boolean showAngles) {
		this.showAngles = showAngles;
	}
	
	protected boolean canTargetPoseRotateX() {
		return true;
	}
	protected boolean canTargetPoseRotateY() {
		return true;
	}
	protected boolean canTargetPoseRotateZ() {
		return true;
	}
	
	/**
	 * Generate gcode that will allow complete serialization of the robot's state.
	 * Might require additional gcode for tools held by the robot.
	 * @return
	 */
	public String generateGCode() {
		return "";
	}
	
	public void parseGCode(String str) {}
}
