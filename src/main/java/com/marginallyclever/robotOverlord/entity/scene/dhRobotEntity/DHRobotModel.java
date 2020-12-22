package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity;

import java.util.List;
import java.util.ArrayList;

import javax.vecmath.Matrix4d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.IntersectionHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.entity.scene.Scene;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.dhTool.DHTool;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers.DHIKSolver;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * Physical description of a robot designed using D-H parameters.  The model contains unchanging details such as mass, length,
 * range of motion, maximum force of each motor, collision bounds for each bone, and triangle mesh for each bone.
 * 
 * A physical description may be in any one of many states.  Put another way, all valid states exist within the bounds set by
 * a physical description.
 * 
 * All possible tools for a robot exist somewhere within this space, so they must also be within this model.  Then the current
 * tool in use is a part of the state, and any sub-information (tool wear, gripper claw position, etc) are part of the state.
 * 
 * @author Dan Royer
 */
public class DHRobotModel extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4684338773503409014L;

	// a list of DHLinks describing the kinematic chain.
	protected List<DHLink> links = new ArrayList<DHLink>();
	
	// The solver for this type of robot.  A solver figures out how to translate FK to/from IK
	protected transient DHIKSolver ikSolver;
	
	// all the possible tools for this arm.  
	protected List<DHTool> allTools = new ArrayList<DHTool>(); 
	// the selected tool (-1 for none)
	protected int toolIndex=-1;

	// more debug output, please.
	static final boolean VERBOSE=false;

	public DHRobotModel() {
		super();
		setName("DHRobot");
	}
	
	public DHRobotModel(DHIKSolver solver) {
		this();
		setIKSolver(solver);
	}
	
	public DHRobotModel(DHRobotModel b) {
		this();
		set(b);
	}

	public void set(DHRobotModel b) {
		super.set(b);
		setIKSolver(b.ikSolver);
		// remove any exiting links from other robot to be certain.
		setNumLinks(b.getNumLinks());
		// copy my links to the next robot
		for(int i=0;i<b.getNumLinks();++i) {
			links.get(i).set(b.links.get(i));
		}
		
		toolIndex = b.toolIndex;
		
		refreshPose();
	}

	public void setIKSolver(DHIKSolver solver0) {
		ikSolver = solver0;
	}
	
	/**
	 * @return the IK solver for a specific type of robot.
	 */
	protected DHIKSolver getIKSolver() {
		return ikSolver;
	}
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
		if(toolIndex!=-1) {
			gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, getPoseIK());
			getCurrentTool().render(gl2);
			gl2.glPopMatrix();
		}
	}
	
	// shorthand
	public PoseFK createPoseFK() {
		return ikSolver.createPoseFK();
	}

	public Matrix4d getParentMatrix() {
		if( parent == null || !(parent instanceof PoseEntity) ) {
			Matrix4d m = new Matrix4d();
			m.setIdentity();
			return m;
		} else {
			return ((PoseEntity)parent).getPose();
		}
	}
	
	/**
	 * Update the pose matrix of each DH link, then use forward kinematics to find
	 * the end position.
	 */
	public void refreshPose() {
		for( DHLink link : links ) {
			link.refreshPoseMatrix();
		}
		
		if(toolIndex>0) {
			//allTools.get(toolIndex).refreshPoseMatrix();
		}
	}

	/**
	 * Adjust the number of links in this robot
	 * 
	 * @param newSize must be >= 0
	 */
	public void setNumLinks(int newSize) {
		if(newSize < 0) newSize = 0;

		links.clear();
		
		// count the number of existing children.
		Entity prev=this;
		boolean found;
		int s=0;
		while(prev.getChildren().size()>0 && s<newSize) {
			found=false;
			for( Entity c : prev.getChildren() ) {
				if(c instanceof DHLink ) {
					links.add((DHLink)c);
					prev=c;
					++s;
					found=true;
					break;
				}
			}
			// in case there are children but none are DHLinks
			if(found==false) break;
		}

		// if the number is too low, add more.
		while(s<newSize) {
			DHLink newLink = new DHLink();
			links.add(newLink);
			prev.addChild(newLink);
			prev = newLink;
			++s;
		}
		
		// if the number is too high, delete the remaining children.
		prev.getChildren().clear();
	}

	/**
	 * checks that the keyframe is sane that no collisions occur.
	 * @param keyframe
	 * @return false if the keyframe is not sane or a collision occurs.
	 */
	public boolean sanityCheck(PoseFK keyframe) {
		if(!poseAnglesAreOK(keyframe)) {
			if(VERBOSE) Log.message("Bad angles");
			return false;
		}
		if(collidesWithSelf(keyframe)) {
			if(VERBOSE) Log.message("Collides with self");
			return false;
		}
		if(collidesWithWorld(keyframe))	{
			if(VERBOSE) Log.message("Collides with world");
			return false;
		}
		return true;
	}
		
	/**
	 * Test physical bounds of link N against all links &lt;N-1 and all links
	 * &gt;N+1 We're using separating Axis Theorem. See
	 * https://gamedev.stackexchange.com/questions/25397/obb-vs-obb-collision-detection
	 * 
	 * @param keyframe the angles at time of test
	 * @return true if there are no collisions
	 */
	public boolean collidesWithSelf(PoseFK futureKey) {
		PoseFK originalKey = getPoseFK();
		// move the clone to the keyframe pose
		setPoseFK(futureKey);
		
		int size = links.size();
		for (int i = 0; i < size; ++i) {
			if (links.get(i).getModel() == null)
				continue;

			for (int j = i + 2; j < size; ++j) {
				if (links.get(j).getModel() == null)
					continue;

				if (IntersectionHelper.cuboidCuboid(
						links.get(i).getCuboid(),
						links.get(j).getCuboid())) {
						Log.message("Self collision between "+
									i+":"+links.get(i).getName()+" and "+
									j+":"+links.get(j).getName());

					setPoseFK(originalKey);
					return true;
				}
			}
		}

		setPoseFK(originalKey);
		return false;
	}

	/**
	 * Test physical bounds of all links with the world.
	 * We're using separating Axis Theorem. See https://gamedev.stackexchange.com/questions/25397/obb-vs-obb-collision-detection
	 * 
	 * @param keyframe the angles at time of test
	 * @return false if there are no collisions
	 */
	public boolean collidesWithWorld(PoseFK futureKey) {
		// is this robot in the world?
		Entity rootEntity = getRoot();
		if( !(rootEntity instanceof RobotOverlord) ) {
			// no!
			return false;
		}
		// yes!
		RobotOverlord ro = (RobotOverlord)rootEntity;
		// Does RobotOverlord contain a Scene?
		Scene scene = ro.getWorld();
		if(scene==null) return false;
		// yes!  We have all the prerequisites.		
		// save the original key
		PoseFK originalKey = getPoseFK();
		// move to the future key
		setPoseFK(futureKey);
		// test for intersection
		boolean result = scene.collisionTest(this.getCuboidList());
		// clean up and report results
		setPoseFK(originalKey);
		return result;
	}

	/**
	 * Perform a sanity check. Make sure the angles in the {@link PoseFK} are within the
	 * joint range limits.
	 * 
	 * @param keyframe
	 * @return
	 */
	public boolean poseAnglesAreOK(PoseFK keyframe) {
		for(int j = 0;j<getNumNonToolLinks();++j) {
			DHLink link = getLink(j);
			if(!link.hasAdjustableValue()) continue;
			double v = keyframe.fkValues[j];
			if ( link.rangeMax.get() < v || link.rangeMin.get() > v) {
				if(VERBOSE) {
					Log.message("FK "+ link.flags + j + ":" + v + " out (" + link.rangeMin.get() + " to " + link.rangeMax.get() + ")");
				}
				return false;
			}
		}

		return true;
	}
	
	/**
	 * @return matrix of end effector.  matrix is relative to the robot origin.
	 */
	public Matrix4d getPoseIK() {
		Matrix4d m = getLink(getNumLinks()-1).getPoseWorld();
		return m;
	}
	
	/**
	 * Use an IK pose of the end effector to find the FK pose of the robot.  If it can be found, set that FK pose.
	 * matrix is relative to the robot origin.
	 * 
	 * @param m end effector world pose matrix.
	 * @return true if sane position set.  false, nothing changed.
	 */
	public boolean setPoseIK(Matrix4d m) {
		PoseFK newPose = isPoseIKSane(m); 
		if( newPose != null ) {
			setPoseFK(newPose);
			return true;
		}
		return false;
	}
	
	/**
	 * Verifies if the requested end effector pose of the robot is reachable and sane.
	 * Leaves the robot in the state it was found except for the newPose/oldPose keyframes, which are only used for this method. 
	 * 
	 * @param m desired end effector relative to the robot's origin.
	 * @return the PoseFK if sane.  null if not sane.
	 */
	public PoseFK isPoseIKSane(final Matrix4d m) {
		PoseFK poseFKold = ikSolver.createPoseFK();
		PoseFK poseFKnew = ikSolver.createPoseFK();
		poseFKold.set(getPoseFK());
		
		boolean isSane = false;
		
		// the solver should NEVER change the current model.  it only attempts to find one possible solution.
		DHIKSolver.SolutionType s = ikSolver.solveWithSuggestion(this, m, poseFKnew, poseFKold);
		if(VERBOSE) Log.message("new: "+poseFKnew + "\t"+s);
		if (s == DHIKSolver.SolutionType.ONE_SOLUTION) {
			if (sanityCheck(poseFKnew)) {
				if(VERBOSE) Log.message("Sane");
				isSane = true;
			} else if(VERBOSE) Log.message("isPoseIKSane() insane");
		} else if(VERBOSE) Log.message("isPoseIKSane() not one solution");
		
		if(!isSane) {
			Log.message("No");
			return null;
		} else {
			return poseFKnew;
		}
	}
	
	/**
	 * Set the robot's FK values to the keyframe values and then refresh the pose.
	 * This method is used by others to verify for collisions, so this method
	 * cannot verify collisions itself.
	 * @param keyframe
	 */
	public void setPoseFK(PoseFK keyframe) {
		assert(keyframe.fkValues.length==getNumNonToolLinks());
		
		for(int j = 0;j<keyframe.fkValues.length;++j) {
			DHLink link = getLink(j);
			if(link.hasAdjustableValue()) {
				link.setAdjustableValue(keyframe.fkValues[j]);
			}
		}

		refreshPose();
	}

	/**
	 * Get the robot's FK pose.
	 * 
	 * @return keyframe of this pose
	 */
	public PoseFK getPoseFK() {
		PoseFK keyframe = ikSolver.createPoseFK();
		assert(keyframe.fkValues.length==getNumNonToolLinks());

		for(int j = 0;j<keyframe.fkValues.length;++j) {
			DHLink link = getLink(j);
			if(link.hasAdjustableValue()) {
				keyframe.fkValues[j] = link.getAdjustableValue();
			}
		}
		return keyframe;
	}

	public int getNumLinks() {
		return links.size();
	}

	public DHLink getLink(int i) {
		return links.get(i);
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Dh", "DH shortcuts");
		
		for( DHLink link : links ) {
			view.addRange(link.theta,
					(int)Math.floor(link.rangeMax.get()),
					(int)Math.ceil(link.rangeMin.get()));
		}
		view.popStack();
		super.getView(view);
	}

	/**
	 * @return a list of cuboids, or null.
	 */
	public ArrayList<Cuboid> getCuboidList() {
		ArrayList<Cuboid> cuboidList = new ArrayList<Cuboid>();

		refreshPose();

		for( DHLink link : links ) {
			if(link.getCuboid() != null ) {
				cuboidList.addAll(link.getCuboidList());
			}
		}
		if(toolIndex>0) {
			DHTool t = allTools.get(toolIndex);
			t.pose.set(links.get(links.size()-1).poseWorld);
			t.refreshPoseMatrix();
			cuboidList.addAll(t.getCuboidList());
		}

		return cuboidList;
	}
	
	public void setDiffuseColor(float r,float g,float b,float a) {
		for(int i=0;i<getNumLinks();++i) {
			getLink(i).getMaterial().setDiffuseColor(r,g,b,a);
		}
	}
	
	public void addTool(DHTool t) {
		allTools.add(t);
	}
	
	public void removeTool(DHTool t) {
		allTools.remove(t);
	}
	
	// the tool should be the child of the last link in the chain
	public void setToolIndex(int arg0) {
		if(toolIndex==arg0) return;
		
		toolIndex = arg0;
	}
	
	/**
	 * @return the tool index.  -1 means 'no tool'.
	 */
	public int getToolIndex() {
		return toolIndex;
	}

	public DHTool getCurrentTool() {
		return allTools.get(toolIndex);
	}

	protected int getNumNonToolLinks() {
		int linkCount = getNumLinks();
		if(toolIndex>=0) linkCount--;
		return linkCount;
	}
	
	/**
	 * Override this to set your robot at home position.  Called on creation.
	 */
	public void goHome() {}
}
