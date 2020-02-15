package com.marginallyclever.robotOverlord.dhRobot;

import java.util.List;
import java.util.Observable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.IntersectionTester;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.dhRobot.solvers.DHIKSolver;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.physicalObject.PhysicalObject;
import com.marginallyclever.robotOverlord.robot.Robot;
import com.marginallyclever.robotOverlord.world.World;

/**
 * A robot designed using D-H parameters.
 * 
 * @author Dan Royer
 */
public class DHRobot extends Observable implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6759482413128098203L;
	
	// a list of DHLinks describing the kinematic chain.
	public List<DHLink> links;
	// The solver for this type of robot
	protected DHIKSolver solver;

	// the last link in the kinematic chain.
	protected Matrix4d endEffectorMatrix;

	// a DHTool attached to the arm.
	public DHTool dhTool;
	
	// the GUI panel for controlling this robot.
	protected DHRobotPanel panel;
	protected boolean disablePanel;

	protected boolean showBones; // show D-H representation of each link
	protected boolean showPhysics; // show bounding boxes of each link
	protected boolean showAngles; // show current angle and limit of each link
	
	protected int hitBox1, hitBox2; // display which hitboxes are colliding

	protected PhysicalObject parent;
	

	public DHRobot() {
		super();
		reset();
	}
	
	public DHRobot(DHRobot b) {
		super();
		reset();
		set(b);
	}
		
	public void reset() {
		links = new ArrayList<DHLink>();

		endEffectorMatrix = new Matrix4d();
		// set default tool = no tool
		dhTool = new DHTool();
		
		disablePanel = false;
		setShowBones(false);
		setShowPhysics(false);
		setShowAngles(false);

		hitBox1 = -1;
		hitBox2 = -1;
	}

	public void set(DHRobot b) {
		// remove any exiting links from other robot to be certain.
		links.clear();
		// copy my links to the next robot
		for( DHLink link : b.links ) {
			// copy my links into the other robot.
			links.add(new DHLink(link));
		}
		solver = b.solver;
		endEffectorMatrix.set(b.endEffectorMatrix);
		dhTool.set(b.dhTool);
		
		disablePanel = b.disablePanel;
		showBones = b.showBones;
		showPhysics = b.showPhysics;
		showAngles = b.showAngles;
		
		hitBox1 = b.hitBox1;
		hitBox2 = b.hitBox2;
		
		parent = b.parent;
		
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


	/**
	 * Set the solver.
	 * @return the IK solver for a specific type of robot.
	 */
	public void setIKSolver(DHIKSolver solver0) {
		solver = solver0;
	}

	public void render(GL2 gl2) {
		gl2.glPushMatrix();
	
			float [] original = new float[4];
			
			gl2.glGetFloatv(GL2.GL_CURRENT_COLOR, original, 0);
			
			// 3d meshes
			for( DHLink link : links ) {
				gl2.glColor4f(original[0],original[1],original[2],original[3]);
				link.render(gl2);
				link.setAngleColorByRange(gl2);
				MatrixHelper.applyMatrix(gl2, link.pose);
			}
			gl2.glColor4f(original[0],original[1],original[2],original[3]);
			gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, original,0);
	
			if (dhTool != null) {
				dhTool.render(gl2);
			}
		gl2.glPopMatrix();
		
		gl2.glPushMatrix();
			// now simplify look & feel for line drawings.
			boolean isDepth = gl2.glIsEnabled(GL2.GL_DEPTH_TEST);
			boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
	
			
			int j = 0;
			for( DHLink link : links ) {
				if (showBones )	link.renderBones (gl2);
				if (showAngles)	link.renderAngles(gl2);
				if (showPhysics) {
					if( link.model != null) {
						// Draw the bounding box of the model as the physical bounding box
						// TODO there are better shapes for collision detection?
						if (j == hitBox1 || j == hitBox2) {
							// hit
							gl2.glColor4d(1, 0, 0.8, 0.15);
						} else {
							// no hit
							gl2.glColor4d(1, 0.8, 0, 0.15);
						}
						PrimitiveSolids.drawBox(gl2, link.model.getBoundBottom(), link.model.getBoundTop());
					}
				}
				MatrixHelper.applyMatrix(gl2,link.pose);
				++j;
			}
				
			if (dhTool != null) {
				if (showBones ) dhTool.dhLink.renderBones (gl2);
				if (showAngles) dhTool.dhLink.renderAngles(gl2);
				//if(showPhysics && dhTool.dhLinkEquivalent.model != null) {
				//	 gl2.glColor4d(1,0,0.8,0.15);
				//	 PrimitiveSolids.drawBox(gl2,
				//		 dhTool.dhLinkEquivalent.model.getBoundBottom(),
				//		 dhTool.dhLinkEquivalent.model.getBoundTop());
				//}
			}

			// at the end effector position
			PrimitiveSolids.drawStar(gl2, new Vector3d(0, 0, 0), 8);
		gl2.glPopMatrix();

		if (isDepth) gl2.glEnable(GL2.GL_DEPTH_TEST);
		if (isLit) gl2.glEnable(GL2.GL_LIGHTING);
		// end simplify
	}

	public Matrix4d getParentMatrix() {
		if( parent == null ) {
			return new Matrix4d();
		} else {
			return parent.getMatrix();
		}
	}
	
	/**
	 * Update the pose matrix of each DH link, then use forward kinematics to find
	 * the end position.
	 */
	public void refreshPose() {
		endEffectorMatrix.set(getParentMatrix());

		for( DHLink link : links ) {
			// update matrix
			link.refreshPoseMatrix();
			// find cumulative matrix
			link.poseCumulative.set(endEffectorMatrix);
			endEffectorMatrix.mul(link.pose);

			// set up the physical limits
			link.cuboid.setMatrix(link.poseCumulative);
			if( link.model != null ) {
				link.cuboid.setBounds(link.model.getBoundTop(), link.model.getBoundBottom());
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

		int oldSize = links.size();
		while (oldSize > newSize) {
			oldSize--;
			links.remove(0);
		}
		while (oldSize < newSize) {
			oldSize++;
			links.add(new DHLink());
		}
	}

	public void setTool(DHTool arg0) {
		dhTool = arg0;
		this.panel.updateActiveTool(dhTool);
	}

	public void removeTool() {
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
	public boolean collidesWithSelf(DHKeyframe keyframe) {
		// create a clone of the robot
		DHRobot clone = new DHRobot(this);
		// move the clone to the keyframe pose
		clone.setPoseFK(keyframe);
		
		// check for collisions

		hitBox1 = -1;
		hitBox2 = -1;

		int size = clone.links.size();
		for (int i = 0; i < size; ++i) {
			if (clone.links.get(i).model == null)
				continue;

			for (int j = i + 3; j < size; ++j) {
				if (clone.links.get(j).model == null)
					continue;

				if (IntersectionTester.cuboidCuboid(
						clone.links.get(i).cuboid,
						clone.links.get(j).cuboid)) {
					// System.out.println("Intersect "+i+"/"+j+" (1)!");
					hitBox1 = i;
					hitBox2 = j;
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Test physical bounds of all links with the world.
	 * We're using separating Axis Theorem. See https://gamedev.stackexchange.com/questions/25397/obb-vs-obb-collision-detection
	 * 
	 * @param keyframe the angles at time of test
	 * @return true if there are no collisions
	 */
	public boolean collidesWithWorld(DHKeyframe keyframe) {
		if( this.parent == null ) return false;

		// create a clone of the robot
		DHRobot clone = new DHRobot(this);
		// move the clone to the keyframe pose
		clone.setPoseFK(keyframe);

		// get a list of all cuboids
		ArrayList<Cuboid> cuboidList = new ArrayList<Cuboid>();
		
		for( DHLink link : clone.links ) {
			if(link.cuboid != null ) {
				cuboidList.add(link.cuboid);
			}
		}

		// make a list of entities to ignore and add the parent.
		ArrayList<Entity> ignoreList = new ArrayList<Entity>();
		if(parent instanceof Robot) {
			// this way robot ghost will not collide with robot live.
			ignoreList.add(parent);
		}
		
		PhysicalObject p = (PhysicalObject)parent;
		World world = p.getWorld();
		return world.collisionTest(cuboidList,ignoreList);
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
					System.out.println(
							"FK theta " + j + ":" + v + " out (" + link.rangeMin + " to " + link.rangeMax + ")");
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
					System.out.println(
							"FK alpha " + j + ":" + v + " out (" + link.rangeMin + " to " + link.rangeMax + ")");
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
	 * @param r the DHRobot to set
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
	 * 
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
	 * Set the robot's FK values to the keyframe values and then refresh the pose.
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
	
	public boolean isShowBones() {
		return showBones;
	}

	public void setShowBones(boolean arg0) {
		this.showBones = arg0;
		if (panel != null)
			panel.setShowBones(arg0);
	}

	public boolean isShowPhysics() {
		return showPhysics;
	}

	public void setShowPhysics(boolean arg0) {
		this.showPhysics = arg0;
		if (panel != null)
			panel.setShowPhysics(arg0);
	}

	public void setShowAngles(boolean arg0) {
		showAngles = arg0;
	}

	public boolean isShowAngles() {
		return showAngles;
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

	public PhysicalObject getParent() {
		return parent;
	}

	public void setParent(PhysicalObject parent) {
		this.parent = parent;
	}
}
