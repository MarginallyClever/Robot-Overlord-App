package com.marginallyclever.robotOverlord.engine.dhRobot;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Matrix4d;

import com.marginallyclever.convenience.IntersectionTester;
import com.marginallyclever.robotOverlord.engine.dhRobot.solvers.DHIKSolver;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.material.Material;
import com.marginallyclever.robotOverlord.entity.modelInWorld.ModelInWorld;
import com.marginallyclever.robotOverlord.entity.physicalObject.PhysicalObject;

/**
 * A robot designed using D-H parameters.
 * 
 * @author Dan Royer
 */
public class DHRobot extends ModelInWorld {
	// a list of DHLinks describing the kinematic chain.
	public List<DHLink> links;
	// The solver for this type of robot
	protected transient DHIKSolver solver;

	// the last link in the kinematic chain.
	protected Matrix4d endEffectorMatrix;

	// a DHTool attached to the arm.
	public DHTool dhTool;
	
	// the GUI panel for controlling this robot.
	protected DHRobotPanel panel;
	protected boolean disablePanel;

	public Material material;
	

	public DHRobot() {
		super();
		reset();
	}
	
	public DHRobot(DHRobot b) {
		super();
		reset();
		set(b);
	}
	
	private void reset() {
		setName("DHRobot");
		
		links = new ArrayList<DHLink>();

		endEffectorMatrix = new Matrix4d();
		// set default tool = no tool
		dhTool = new DHTool();
		
		material = new Material();
		disablePanel = false;

	}

	public void set(DHRobot b) {
		super.set(b);
		// remove any exiting links from other robot to be certain.
		setNumLinks(b.getNumLinks());
		
		solver = b.solver;

		endEffectorMatrix.set(b.endEffectorMatrix);
		
		// copy my links to the next robot
		for(int i=0;i<b.getNumLinks();++i) {
			links.get(i).set(b.links.get(i));
		}
		dhTool.set(b.dhTool);

		disablePanel = b.disablePanel;
		
		refreshPose();
	}


	/**
	 * Override this method to return the correct solver for your type of robot.
	 * 
	 * @return the IK solver for a specific type of robot.
	 */
	public DHIKSolver getIKSolver() {
		return solver;
	}

	public void setIKSolver(DHIKSolver solver0) {
		solver = solver0;
	}
	

	public Matrix4d getParentMatrix() {
		if( parent == null || !(parent instanceof PhysicalObject) ) {
			Matrix4d m = new Matrix4d();
			m.setIdentity();
			return m;
		} else {
			return ((PhysicalObject)parent).getPose();
		}
	}
	
	/**
	 * Update the pose matrix of each DH link, then use forward kinematics to find
	 * the end position.
	 */
	public void refreshPose() {
		endEffectorMatrix.set(getParentMatrix());

		if(model != null) {
			cuboid.set(model.getCuboid());
		}
		
		for( DHLink link : links ) {
			// update matrix
			link.refreshPoseMatrix();
			// find cumulative matrix
			endEffectorMatrix.mul(link.getPose());
			link.poseCumulative.set(endEffectorMatrix);

			// set up the physical limits
			if(link.getModel()!=null) {
				link.cuboid.set(link.getModel().getCuboid());
				link.cuboid.setPoseWorld(link.poseCumulative);
			}
		}
		if (dhTool != null) {
			dhTool.refreshPose(endEffectorMatrix);
		}
	}

	/**
	 * Adjust the number of links in this robot
	 * 
	 * @param newSize must be greater than 0
	 */
	public void setNumLinks(int newSize) {
		if (newSize < 1)
			newSize = 1;

		links.clear();
		Entity prev=this;
		for(int s = 0; s < newSize;++s) {
			DHLink newLink = new DHLink();
			prev.addChild(newLink);
			links.add(newLink);
			prev=newLink;
		}
	}

	// the tool should be the child of the last link in the chain
	public void setTool(DHTool arg0) {
		dhTool = arg0;
		links.get(links.size()-1).addChild(arg0);
		this.panel.updateActiveTool(arg0);
	}

	public void removeTool() {
		// the child of the last link in the chain is the tool
		links.get(links.size()-1).removeChild(dhTool);
		dhTool = null;
		this.panel.updateActiveTool(null);
	}

	public DHTool getCurrentTool() {
		return dhTool;
	}

	/**
	 * checks that the keyframe is sane that no collisions occur.
	 * @param keyframe
	 * @return false if the keyframe is not sane or a collision occurs.
	 */
	public boolean sanityCheck(DHKeyframe keyframe) {
		if(!keyframeAnglesAreOK(keyframe))	return false;
		if(collidesWithSelf(keyframe))		return false;
		if(collidesWithWorld(keyframe))		return false;
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
	public boolean collidesWithSelf(DHKeyframe futureKey) {
		DHKeyframe originalKey = solver.createDHKeyframe();
		getPoseFK(originalKey);
		// move the clone to the keyframe pose
		setPoseFK(futureKey);
		
		int size = links.size();
		for (int i = 0; i < size; ++i) {
			if (links.get(i).getModel() == null)
				continue;

			for (int j = i + 2; j < size; ++j) {
				if (links.get(j).getModel() == null)
					continue;

				if (IntersectionTester.cuboidCuboid(
						links.get(i).getCuboid(),
						links.get(j).getCuboid())) {
						System.out.println("Self collision between "+
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
	 * @return true if there are no collisions
	 */
	public boolean collidesWithWorld(DHKeyframe futureKey) {
		if( this.parent == null ) {
			return false;
		}
		
		// create a clone of the robot
		DHKeyframe originalKey = solver.createDHKeyframe();
		getPoseFK(originalKey);
		// move the clone to the keyframe pose
		setPoseFK(futureKey);
		boolean result = getWorld().collisionTest((PhysicalObject)parent); 
		setPoseFK(originalKey);
		
		return result;
	}

	/**
	 * Perform a sanity check. Make sure the angles in the keyframe are within the
	 * joint range limits.
	 * 
	 * @param keyframe
	 * @return
	 */
	public boolean keyframeAnglesAreOK(DHKeyframe keyframe) {
		Iterator<DHLink> i = links.iterator();
		int j = 0;
		while (i.hasNext()) {
			DHLink link = i.next();
			if ((link.flags & DHLink.READ_ONLY_THETA) == 0) {
				double v = keyframe.fkValues[j++];
				if (link.rangeMax < v || link.rangeMin > v) {
					System.out.println("FK theta " + j + ":" + v + " out (" + link.rangeMin + " to " + link.rangeMax + ")");
					return false;
				}
			}
			if ((link.flags & DHLink.READ_ONLY_D) == 0) {
				double v = keyframe.fkValues[j++];
				if (link.rangeMax < v || link.rangeMin > v) {
					System.out.println("FK D " + j + ":" + v + " out (" + link.rangeMin + " to " + link.rangeMax + ")");
					return false;
				}
			}
			if ((link.flags & DHLink.READ_ONLY_ALPHA) == 0) {
				double v = keyframe.fkValues[j++];
				if (link.rangeMax < v || link.rangeMin > v) {
					System.out.println("FK alpha " + j + ":" + v + " out (" + link.rangeMin + " to " + link.rangeMax + ")");
					return false;
				}
			}
			if ((link.flags & DHLink.READ_ONLY_R) == 0) {
				double v = keyframe.fkValues[j++];
				if (link.rangeMax < v || link.rangeMin > v) {
					System.out.println("FK R " + j + ":" + v + " out (" + link.rangeMin + " to " + link.rangeMax + ")");
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Find the forward kinematic pose of robot r that would give an end effector matrix matching m.
	 * If the FK pose is found, set the adjustable values of the links to said pose.
	 * @param m the matrix of the finger tip of the robot, relative to the base of the robot.
	 */
	public boolean setPoseIK(Matrix4d m) {
		DHKeyframe oldPose = solver.createDHKeyframe();
		getPoseFK(oldPose);
		DHKeyframe newPose = solver.createDHKeyframe();
		
		DHIKSolver.SolutionType s = solver.solveWithSuggestion(this, m, newPose,oldPose);
		if (s == DHIKSolver.SolutionType.ONE_SOLUTION) {
			if (sanityCheck(newPose)) {
				setPoseFK(newPose);
				return true;
			} else {
				//System.out.println("setPoseIK() insane");
			}
		} else {
			///System.out.println("setPoseIK() impossible");
		}
		return false;
	}

	/**
	 * Set the robot's FK values to the keyframe values and then refresh the pose.
	 * This method is used by others to verify for collisions, so this method
	 * cannot verify collisions itself.
	 * @param keyframe
	 */
	public void setPoseFK(DHKeyframe keyframe) {
		int stop=keyframe.fkValues.length;
		int j = 0;
		
		for( DHLink link : links ) {
			if(j==stop) break;
			if(link.hasAdjustableValue()) {
				link.setAdjustableValue(keyframe.fkValues[j++]);
			}
		}

		refreshPose();
		if (panel != null && !isDisablePanel()) {
			panel.updateEnd();
		}
	}

	/**
	 * Store the robot's FK values in the keyframe.
	 * 
	 * @param keyframe to set
	 */
	public void getPoseFK(DHKeyframe keyframe) {
		int stop=keyframe.fkValues.length;
		int j = 0;

		for( DHLink link : links ) {
			if(j==stop) break;
			if(link.hasAdjustableValue()) {
				keyframe.fkValues[j++] = link.getAdjustableValue();
			}
		}
	}

	public int getNumLinks() {
		return links.size();
	}

	public DHLink getLink(int i) {
		return links.get(i);
	}

	public Matrix4d getEndEffectorMatrix() {
		return new Matrix4d(endEffectorMatrix);
	}
	
	public boolean isDisablePanel() {
		return disablePanel;
	}

	public void setDisablePanel(boolean disablePanel) {
		this.disablePanel = disablePanel;
	}
}
