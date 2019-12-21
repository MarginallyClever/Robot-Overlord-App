package com.marginallyclever.robotOverlord.dhRobot;

import java.util.List;
import java.util.Observable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Point3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.dhRobot.solvers.DHIKSolver;

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
		
		endEffectorMatrix.set(b.endEffectorMatrix);
		dhTool.set(b.dhTool);
		
		disablePanel = b.disablePanel;
		showBones = b.showBones;
		showPhysics = b.showPhysics;
		showAngles = b.showAngles;
		
		hitBox1 = b.hitBox1;
		hitBox2 = b.hitBox2;
		
		refreshPose();
	}


	/**
	 * Override this method to return the correct solver for your type of robot.
	 * 
	 * @return the IK solver for a specific type of robot.
	 */
	public DHIKSolver getSolverIK() {
		return solver;
	}


	/**
	 * Set the solver.
	 * @return the IK solver for a specific type of robot.
	 */
	public void setSolverIK(DHIKSolver solver0) {
		solver = solver0;
	}

	public void render(GL2 gl2) {
		boolean isDepth = gl2.glIsEnabled(GL2.GL_DEPTH_TEST);
		boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_DEPTH_TEST);
		gl2.glDisable(GL2.GL_LIGHTING);

		PrimitiveSolids.drawStar(gl2, new Vector3d(0, 0, 0), 10);

		gl2.glPushMatrix();


		int j = 0;
			for( DHLink link : links ) {
				// 3d meshes
				link.renderModel(gl2);
				if (showBones) link.renderBones(gl2);
				if (showAngles) link.renderAngles(gl2);
				if (showPhysics && link.model != null) {
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
				MatrixHelper.applyMatrix(gl2,link.pose);
	    		link.setAngleColorByRange(gl2);
				++j;
			}
			
			if (dhTool != null) {
				dhTool.render(gl2);
				if (showBones) dhTool.dhLinkEquivalent.renderBones(gl2);
				if (showAngles) dhTool.dhLinkEquivalent.renderAngles(gl2);
				//if(showPhysics && dhTool.dhLinkEquivalent.model != null) {
				//	 gl2.glColor4d(1,0,0.8,0.15);
				//	 PrimitiveSolids.drawBox(gl2,
				//		 dhTool.dhLinkEquivalent.model.getBoundBottom(),
				//		 dhTool.dhLinkEquivalent.model.getBoundTop());
				//}
			}
		gl2.glPopMatrix();
		
		MatrixHelper.drawMatrix(gl2, endEffectorMatrix, 8.0);

		if (isDepth) gl2.glEnable(GL2.GL_DEPTH_TEST);
		if (isLit) gl2.glEnable(GL2.GL_LIGHTING);
	}

	/**
	 * Update the pose matrix of each DH link, then use forward kinematics to find
	 * the end position.
	 */
	public void refreshPose() {
		endEffectorMatrix.setIdentity();

		for( DHLink link : links ) {
			// update matrix
			link.refreshPoseMatrix();
			// find cumulative matrix
			link.poseCumulative.set(endEffectorMatrix);
			endEffectorMatrix.mul(link.pose);
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

	public boolean sanityCheck(DHKeyframe keyframe) {
		if (!keyframeAnglesAreOK(keyframe))
			return false;
		if (!selfCollision(keyframe))
			return false;
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
	public boolean selfCollision(DHKeyframe keyframe) {
		boolean noCollision = true;

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

				if (hasIntersection(clone.links.get(i), clone.links.get(j))) {
					// System.out.println("Intersect "+i+"/"+j+" (1)!");
					hitBox1 = i;
					hitBox2 = j;
					noCollision = false;
					break;
				} /*
					 * if(hasIntersection(links.get(j),links.get(i))) {
					 * System.out.println("Intersect "+i+"/"+j+" (2)!"); hitBox1=i; hitBox2=j;
					 * noCollision=false; break; }
					 */
			}
			if (noCollision == false) {
				break;
			}
		}

		return noCollision;
	}

	protected boolean hasIntersection(DHLink a, DHLink b) {
		// get the normals for the box of A, which happen to be the three vectors of the
		// matrix for this joint pose.
		Vector3d[] n = new Vector3d[3];
		n[0] = new Vector3d(a.poseCumulative.m00, a.poseCumulative.m10, a.poseCumulative.m20);
		n[1] = new Vector3d(a.poseCumulative.m01, a.poseCumulative.m11, a.poseCumulative.m21);
		n[2] = new Vector3d(a.poseCumulative.m02, a.poseCumulative.m12, a.poseCumulative.m22);
		// System.out.println("matrix="+a.poseCumulative);

		// System.out.println("Acorners=");
		Point3d[] aCorners = getCornersForLink(a);
		// System.out.println("Bcorners=");
		Point3d[] bCorners = getCornersForLink(b);

		// String [] axis = {"X","Y","Z"};

		for (int i = 0; i < n.length; ++i) {
			// SATTest the normals of A against the 8 points of box A.
			// SATTest the normals of A against the 8 points of box B.
			// points of each box are a combination of the box's top/bottom values.
			double[] aLim = SATTest(n[i], aCorners);
			double[] bLim = SATTest(n[i], bCorners);
			// System.out.println("Lim "+axis[i]+" > "+n[i].x+"\t"+n[i].y+"\t"+n[i].z+" :
			// "+aLim[0]+","+aLim[1]+" vs "+bLim[0]+","+bLim[1]);

			// if the two box projections do not overlap then there is no chance of a
			// collision.
			if (!overlaps(aLim[0], aLim[1], bLim[0], bLim[1])) {
				// System.out.println("Miss");
				return false;
			}
		}

		// intersect!
		// System.out.println("Hit");
		return true;
	}

	/**
	 * find the 8 corners of the bounding box and transform them into world space.
	 * 
	 * @param link the link that contains both the model bounds and the
	 *             poseCumulative.
	 * @return the 8 transformed Point3d.
	 */
	protected Point3d[] getCornersForLink(DHLink link) {
		Point3d[] p = new Point3d[8];

		Point3d b = link.model.getBoundBottom();
		Point3d t = link.model.getBoundTop();

		p[0] = new Point3d(b.x, b.y, b.z);
		p[1] = new Point3d(b.x, b.y, t.z);
		p[2] = new Point3d(b.x, t.y, b.z);
		p[3] = new Point3d(b.x, t.y, t.z);
		p[4] = new Point3d(t.x, b.y, b.z);
		p[5] = new Point3d(t.x, b.y, t.z);
		p[6] = new Point3d(t.x, t.y, b.z);
		p[7] = new Point3d(t.x, t.y, t.z);

		for (int i = 0; i < p.length; ++i) {
			// System.out.print("\t"+p[i]);
			link.poseCumulative.transform(p[i]);
			// System.out.println(" >> "+p[i]);
		}

		return p;
	}

	protected boolean isBetween(double val, double bottom, double top) {
		return bottom <= val && val <= top;
	}

	protected boolean overlaps(double a0, double a1, double b0, double b1) {
		return isBetween(b0, a0, a1) || isBetween(a0, b0, b1);
	}

	protected double[] SATTest(Vector3d normal, Point3d[] corners) {
		double[] values = new double[2];
		values[0] = Double.MAX_VALUE; // min value
		values[1] = -Double.MAX_VALUE; // max value

		for (int i = 0; i < corners.length; ++i) {
			double dotProduct = corners[i].x * normal.x + corners[i].y * normal.y + corners[i].z * normal.z;
			if (values[0] > dotProduct)
				values[0] = dotProduct;
			if (values[1] < dotProduct)
				values[1] = dotProduct;
		}

		return values;
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
	 * Set the robot's FK values to the keyframe values and then refresh the pose.
	 * 
	 * @param keyframe
	 */
	public void setPoseFK(DHKeyframe keyframe) {
		int stop=keyframe.fkValues.length;
		int j = 0;
		
		for( DHLink link : links ) {
			if(j==stop) break;
			link.setAdjustableValue(keyframe.fkValues[j++]);
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

	public void setLiveMatrix(Matrix4d m) {
		endEffectorMatrix.set(m);
	}
	
	public boolean isDisablePanel() {
		return disablePanel;
	}

	public void setDisablePanel(boolean disablePanel) {
		this.disablePanel = disablePanel;
	}
}
