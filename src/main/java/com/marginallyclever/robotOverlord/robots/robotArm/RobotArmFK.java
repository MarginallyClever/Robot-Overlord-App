package com.marginallyclever.robotOverlord.robots.robotArm;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.Serial;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.IntersectionHelper;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.robots.Robot;
import com.marginallyclever.robotOverlord.shape.Mesh;
import com.marginallyclever.robotOverlord.shape.ShapeEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewElementButton;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.uiExposedTypes.TextureEntity;

/**
 * Simulation of a robot arm with Forward Kinematics based on Denavit Hartenberg parameters.
 * It manages a set of {@link RobotArmBone} and a tool center position.
 * @see <a href="https://en.wikipedia.org/wiki/Denavit%E2%80%93Hartenberg_parameters">DH parameters</a>
 * @see <a href="https://en.wikipedia.org/wiki/Forward_kinematics">Forward Kinematics</a>
 * @author Dan Royer
 * @since 2021-02-24
 */
public class RobotArmFK extends PoseEntity implements Robot {
	@Serial
	private static final long serialVersionUID = -2436924907127292890L;
	
	private ShapeEntity base;
	private List<RobotArmBone> bones = new ArrayList<>();
	private RobotEndEffectorTarget endEffectorTarget = new RobotEndEffectorTarget("End Effector");
	private final PoseEntity toolCenterPoint = new PoseEntity("Tool Center Point");
	private final BooleanEntity showSkeleton = new BooleanEntity("Show Skeleton",false);
	private final BooleanEntity showAngles = new BooleanEntity("Show Angles",false);
	private final BooleanEntity drawForceAndTorque = new BooleanEntity("Show forces and torques",false);

	private int activeJoint = 0;

	public RobotArmFK(String name) {
		super(name);

		addChild(endEffectorTarget);
		endEffectorTarget.addChild(toolCenterPoint);
		endEffectorTarget.setArm(this);

		loadModel();
		
		updateEndEffectorPosition();
	}
	
	public RobotArmFK() {
		this(RobotArmFK.class.getSimpleName());
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		RobotArmFK b = (RobotArmFK)super.clone();
		b.bones = new ArrayList<>();
		for( RobotArmBone i : bones ) {
			b.bones.add((RobotArmBone)(i.clone()));
		}
		b.endEffectorTarget = (RobotEndEffectorTarget)endEffectorTarget.clone();
		b.endEffectorTarget.setArm(b);
		
		return b;
	}

	protected void loadModel() {
		bones.clear();
	}

	/**
	 * Use the cumulative pose of each {@link RobotArmBone} to adjust the model origins.
	 */
	protected void adjustModelOriginsToDHLinks() {
		Matrix4d current = new Matrix4d();
		current.setIdentity();
		for( RobotArmBone bone : bones ) {
			bone.updateMatrix();
			current.mul(bone.getPose());
			Matrix4d iWP = new Matrix4d(current);
			iWP.invert();
			bone.getShape().setPose(iWP);
		}
	}

	/**
	 * Use the cumulative pose of each {@link RobotArmBone} to adjust the center of mass of each bone.
	 */
	protected void adjustCenterOfMassToDHLinks() {
		Matrix4d current = new Matrix4d();
		current.setIdentity();
		for( RobotArmBone bone : bones ) {
			bone.updateMatrix();
			current.mul(bone.getPose());
			Matrix4d iWP = new Matrix4d(current);
			iWP.invert();
			Point3d p = bone.getCenterOfMass();
			iWP.transform(p);
			bone.setCenterOfMass(p);
		}
	}
	
	protected void setTextureFilename(String filename) {
		base.getMaterial().setTextureFilename(filename);
		for( RobotArmBone bone : bones ) {
			bone.setTextureFilename(filename);
		}
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, myPose);
			drawMeshes(gl2);
			drawExtras(gl2);
		gl2.glPopMatrix();

		super.render(gl2);
	}

	private void drawMeshes(GL2 gl2) {
		base.render(gl2);

		gl2.glPushMatrix();
		for( RobotArmBone bone : bones ) {
			// draw model with local shape offset
			bone.updateMatrix();
			MatrixHelper.applyMatrix(gl2, bone.getPose());
			bone.getShape().render(gl2);
		}
		gl2.glPopMatrix();
	}
	
	private void drawExtras(GL2 gl2) {
		boolean isTex = OpenGLHelper.disableTextureStart(gl2);
		int depthWasOn = OpenGLHelper.drawAtopEverythingStart(gl2);
		boolean lightWasOn = OpenGLHelper.disableLightingStart(gl2);
		
		if(showSkeleton.get()) drawSkeleton(gl2);
		if(showAngles.get()) drawAngles(gl2);
		if(drawForceAndTorque.get()) drawForceAndTorque(gl2);
		if(showBoundingBox.get()) drawBoundingBoxes(gl2);
		
		OpenGLHelper.disableLightingEnd(gl2,lightWasOn);
		OpenGLHelper.drawAtopEverythingEnd(gl2, depthWasOn);
		OpenGLHelper.disableTextureEnd(gl2,isTex);
	}
	
	private void drawBoundingBoxes(GL2 gl2) {
		boolean hit = collidesWithSelf();
		if(hit) gl2.glColor3d(1, 0, 0);
		else    gl2.glColor3d(1, 1, 1);

		Matrix4d w = getPoseWorld();
		w.invert();
		
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, w);
			ArrayList<Cuboid> list = getCuboidList();
			for(Cuboid c : list) {
				gl2.glPushMatrix();
				MatrixHelper.applyMatrix(gl2, c.getPose());
				PrimitiveSolids.drawBoxWireframe(gl2, c.getBoundsBottom(),c.getBoundsTop());
				gl2.glPopMatrix();
			}
		gl2.glPopMatrix();
	}

	private void drawAngles(GL2 gl2) {
		boolean cullOn = gl2.glIsEnabled(GL2.GL_CULL_FACE);
		gl2.glDisable(GL2.GL_CULL_FACE);
		
		gl2.glPushMatrix();
			int j = bones.size()+1;
			for( RobotArmBone bone : bones ) {
				bone.updateMatrix();
				double bMin = bone.getAngleMin();
				double bMax = bone.getAngleMax();
				double color = Math.abs(0.5-MathHelper.getUnitInRange(bMin,bMax,bone.theta));
				// curve of movement
				gl2.glColor4d(1,1-color,1-color,0.6);
				gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glVertex3d(0, 0, 0);
				double diff = bone.theta-180;
				double end = Math.abs(diff);
				double dir = diff>0?1:-1;
				double radius = j;
				for(double a = 0; a<end;a+=5) {
					double s = Math.sin(Math.toRadians(-a*dir)) * radius;
					double c = Math.cos(Math.toRadians(-a*dir)) * radius;
					gl2.glVertex3d(s, c,0);
				}
				gl2.glEnd();

				MatrixHelper.applyMatrix(gl2, bone.getPose());
				--j;
			}
		gl2.glPopMatrix();

		if(cullOn) gl2.glEnable(GL2.GL_CULL_FACE);
	}

	public void drawSkeleton(GL2 gl2) {
		boolean isTex = OpenGLHelper.disableTextureStart(gl2);
		int depthWasOn = OpenGLHelper.drawAtopEverythingStart(gl2);
		boolean lightWasOn = OpenGLHelper.disableLightingStart(gl2);
		
		Vector3d v = new Vector3d();
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, myPose);
		
			int j = bones.size()+1;
			for( RobotArmBone bone : bones ) {
				bone.updateMatrix();
				// draw bone origin
				PrimitiveSolids.drawStar(gl2,j*2);
				// draw line to next bone
				bone.getPose().get(v);
				gl2.glColor3d(1, 0, 1);
				gl2.glBegin(GL2.GL_LINES);
				gl2.glVertex3d(0, 0, 0);
				gl2.glVertex3d(v.x,v.y,v.z);
				gl2.glEnd();

				MatrixHelper.applyMatrix(gl2, bone.getPose());
				--j;
			}
		gl2.glPopMatrix();
		
		OpenGLHelper.disableLightingEnd(gl2,lightWasOn);
		OpenGLHelper.drawAtopEverythingEnd(gl2, depthWasOn);
		OpenGLHelper.disableTextureEnd(gl2,isTex);
	}
	
	private void drawForceAndTorque(GL2 gl2) {
		TextureEntity tex = new TextureEntity("/center-of-mass.png");

		gl2.glPushMatrix();
			for( RobotArmBone bone : bones ) {
				bone.updateMatrix();
				MatrixHelper.applyMatrix(gl2, bone.getPose());
				// draw center of mass
				tex.render(gl2);
				gl2.glColor3d(1, 1, 1);
				PrimitiveSolids.drawBillboard(gl2,bone.getCenterOfMass(),1,1);
				gl2.glDisable(GL2.GL_TEXTURE_2D);
				// draw forces
				gl2.glBegin(GL2.GL_LINES);
				Vector3d v = bone.getLinearVelocity();	gl2.glColor3d(1, 0, 0);  gl2.glVertex3d(0, 0, 0);  gl2.glVertex3d(v.x,v.y,v.z);
				Vector3d a = bone.getAngularVelocity();	gl2.glColor3d(0, 1, 0);  gl2.glVertex3d(0, 0, 0);  gl2.glVertex3d(a.x,a.y,a.z);
				gl2.glEnd();
			}
		gl2.glPopMatrix();
	}
	
	/**
	 * @return the offset from the end effector to the tool center point.
	 */
	public Matrix4d getToolCenterPointOffset() {
		return toolCenterPoint.getPose();
	}
	
	public void setToolCenterPointOffset(Matrix4d tcpNew) {
		Matrix4d tcpOld = toolCenterPoint.getPose();
		this.toolCenterPoint.setPose(tcpNew);

		notifyPropertyChangeListeners(new PropertyChangeEvent(this,"tcpOffset",tcpOld,tcpNew));
	}

	private void setToolCenterPoint() {

	}

	/**
	 * @return the end effector + tool center pose relative to the base of this robot.
	 */
	public Matrix4d getToolCenterPoint() {
		Matrix4d m = getEndEffector();
		m.mul(toolCenterPoint.getPose());
		return m;
	}

	@Override
	public Object get(int property) {
		switch(property) {
			case NAME: return getName();
			case NUM_JOINTS: return getNumBones();
			case ACTIVE_JOINT: return activeJoint;
			case JOINT_NAME: return getBone(activeJoint).getName();
			case JOINT_VALUE: return getBone(activeJoint).getTheta();
			case JOINT_RANGE_MAX: return getBone(activeJoint).getAngleMax();
			case JOINT_RANGE_MIN: return getBone(activeJoint).getAngleMin();
			case JOINT_HAS_RANGE_LIMITS: return true;
			case JOINT_PRISMATIC: return false;
			case END_EFFECTOR: return getEndEffector();
			case END_EFFECTOR_TARGET: return getEndEffectorTarget().getPose();
			case TOOL_CENTER_POINT: return getToolCenterPoint();
			case POSE: return getPoseWorld();
			case JOINT_POSE: {
				Matrix4d m = new Matrix4d();
				m.setIdentity();
				for(int i=0;i<=activeJoint;++i) {
					m.mul(getBone(i).getPose());
				}
				return m;
			}
			case JOINT_HOME: {
				return getBone(activeJoint).getHome();
			}
			default :  return null;
		}
	}

	@Override
	public void set(int property, Object value) {
		switch(property) {
			case ACTIVE_JOINT: activeJoint = Math.max(0,Math.min(getNumBones(),(int)value));  break;
			case JOINT_VALUE: {
				getBone(activeJoint).setAngleWRTLimits((double)value);
				updateEndEffectorPosition();
			}  break;
			case END_EFFECTOR_TARGET: getEndEffectorTarget().moveTowards((Matrix4d)value);  break;
			case TOOL_CENTER_POINT: setToolCenterPointOffset((Matrix4d)value);  break;
			case POSE: setPoseWorld((Matrix4d)value);  break;
			case JOINT_HOME: getBone(activeJoint).setHome((double)value);  break;
			default: break;
		}
	}

	/**
	 * @return the current end effector pose, relative to the base of this robot
	 */
	public Matrix4d getEndEffector() {
		Matrix4d m = new Matrix4d();
		m.setIdentity();
		for( RobotArmBone bone : bones ) {
			m.mul(bone.getPose());
		}
		
		return m;
	}

	public RobotEndEffectorTarget getEndEffectorTarget() {
		return endEffectorTarget;
	}

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("FK","Forward Kinematics");
		ViewElementButton button = view.addButton("Center all");
		button.addActionEventListener((evt)->{
			double [] v = new double[bones.size()]; 
			for(int i=0;i<bones.size();++i) {
				RobotArmBone b = bones.get(i);
				v[i]=b.getAngleMiddle();
			}
			setAngles(v);
		});
		view.add(showSkeleton);
		view.add(showAngles);
		view.add(drawForceAndTorque);

		ViewElementButton bOpen = view.addButton("Open edit panel");
		bOpen.addActionEventListener((evt)-> onOpenAction() );
		ViewElementButton bSnapshot = view.addButton("Save snapshot");
		bSnapshot.addActionEventListener((evt)->{
			try {
				save();
			} catch(Exception e) {
				Log.error(e.getMessage());
				JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		
		view.popStack();
		
		super.getView(view);
	}

	private void onOpenAction() {
		JFrame parent = null;
		
		Entity e = this.getRoot();
		if(e instanceof RobotOverlord) {
			parent = ((RobotOverlord)e).getMainFrame();
		}
		
		final RobotArmFK me = this;
		final JFrame parentFrame = parent;

		new Thread(() -> {
			try {
				JDialog frame = new JDialog(parentFrame,getName());
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.add(new RobotArmBuilder(me));
				frame.pack();
				frame.setVisible(true);
			} catch (CloneNotSupportedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}).start();
	}

	/**
	 * @return a list of doubles with the angles in degrees.  {@link RobotArmFK#getNumBones()} long.
	 */
	public double [] getAngles() {
		double [] list = new double[bones.size()];
		
		int i=0;
		for( RobotArmBone bone : bones ) {
			list[i++] = bone.theta;
		}
		return list;
	}
	
	/**
	 * Update the theta angles of each bone in the robot and the FK sliders on the panel.
	 * It does not allow you to set the angle of a bone outside the angleMax/angleMin of that bone.
	 * The new values will be tested against the bone limits.
	 * If the arm moves to a new position a {@link PropertyChangeEvent} notice will be fired.  The
	 * {@link PropertyChangeEvent#getPropertyName()} will be "ee".
	 * @param list new theta values.  Must be {@link RobotArmFK#getNumBones()} long.
	 */
	public void setAngles(double [] list) {
		boolean changed=false;
		
		int i=0;
		for( RobotArmBone b : bones ) {
			double v = list[i++];
			double t = b.getTheta();
			b.setAngleWRTLimits(v);
			if( t != b.getTheta() )
				changed=true;
		}

		if(changed) {
			// theta values actually changed so update matrices and get the new end effector position.
			updateEndEffectorPosition();
		}
	}
	
	private void updateEndEffectorPosition() {
		Matrix4d eeOld = getEndEffector();

		for( RobotArmBone b : bones ) {
			b.updateMatrix();
		}

		endEffectorTarget.setPose(getEndEffector());

		Matrix4d eeNew = getEndEffector();
		notifyPropertyChangeListeners(new PropertyChangeEvent(this,"ee",eeOld,eeNew));
	}
	
	public ArrayList<Cuboid> getCuboidList() {
		// current pose starts as root pose
		Matrix4d currentBonePose = getPoseWorld();
		Matrix4d cuboidAfter = new Matrix4d();

		ArrayList<Cuboid> list = new ArrayList<>(base.getCuboidList());
		for(Cuboid c : list) {
			// add current pose to cube pose and save that as cube pose.
			cuboidAfter.mul(currentBonePose,base.getPose());
			c.setPose(cuboidAfter);
		}
		
		for( RobotArmBone bone : bones ) {
			// add bone to current pose
			currentBonePose.mul(bone.getPose());
			
			ArrayList<Cuboid> list2 = bone.getShape().getCuboidList();
			for(Cuboid c : list2) {
				Cuboid nc = new Cuboid();
				nc.set(c);
				// add current pose to cube pose and save that as cube pose.
				cuboidAfter.mul(currentBonePose,bone.getShape().getPose());
				nc.setPose(cuboidAfter);
				list.add(nc);
			}
		}
		
		return list;
	}
	
	/**
	 * @return true if this robot currently collide with itself.
	 */
	public boolean collidesWithSelf() {
		ArrayList<Cuboid> list = getCuboidList();
		
		for(int i=0;i<list.size();++i) {
			Cuboid a = list.get(i);
			for(int j=i+2;j<list.size();++j) {
				Cuboid b = list.get(j);
				if(IntersectionHelper.cuboidCuboid(a, b)) {
					Matrix4d ma = a.getPose();
					Matrix4d mb = b.getPose();
					Mesh sa = a.getShape();
					Mesh sb = b.getShape();
					
					return IntersectionHelper.meshMesh(ma,sa,mb,sb);
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder contents = new StringBuilder(getName() +
				"," + getPose().toString()
				+ "," + base.getModelFilename()
				+ "," + showAngles.get()
				+ "," + getToolCenterPointOffset());
		
		String angles="";
		String add="";
		for( RobotArmBone b : bones ) {
			contents.append(add).append(b.toString());
			add=",\n";
		}
		
		contents.append("[").append(angles).append("]");
		return this.getClass().getSimpleName()+" ["+contents+"]";
	}

	public void fromString(String s) throws Exception {
		final String header = RobotArmFK.class.getSimpleName()+" [";
		if(!s.startsWith(header)) throw new IOException("missing header.");
		
		// strip header and "}" from either end.
		s=s.substring(header.length(),s.length()-1);
		
		// split by comma
		String [] pieces = s.split(",");
		if(pieces.length != bones.size()) throw new IOException("found "+pieces.length+" bones, expected "+bones.size());
		// TODO set number of bones to pieces.length, avoids exception.
		
		for(int i=0; i<bones.size(); ++i) {
			bones.get(i).fromString(pieces[i]);
		}
		
		updateEndEffectorPosition();
	}
	
	public void addBone(RobotArmBone bone) {
		bones.add(bone);
	}
	
	public int getNumBones() {
		return bones.size();
	}
	
	public RobotArmBone getBone(int i) {
		return bones.get(i);
	}
	
	public ShapeEntity getBaseShape() {
		return base;
	}
	
	public void setBaseShape(ShapeEntity s) {
		base=s;
	}
	
	/**
	 * Measures the difference between the latest end effector matrix and the target matrix.
	 * It is a combination of the linear distance and the rotation distance (collectively known as the Twist)
	 * @return the error term.
	 */
	public double getDistanceToTarget(final Matrix4d target) {
		double [] d = MatrixHelper.getCartesianBetweenTwoMatrixes(getEndEffector(), target);
		double sum=0;
		for (double v : d) sum += Math.abs(v);
		return sum;
	}
	
	public void addGravity(Vector3d g) {
		for(RobotArmBone b : bones) {
			Vector3d f = b.getForce();
			f.add(g);
			b.setForce(f);
		}
	}
	
	public void zeroForces() {
		Vector3d zero = new Vector3d();
		for(RobotArmBone b : bones) {
			b.setForce(zero);
			b.setTorque(zero);
		}
	}
	
	public void zeroVelocities() {
		Vector3d zero = new Vector3d();
		for(RobotArmBone b : bones) {
			b.setLinearVelocity(zero);
			b.setAngularVelocity(zero);
		}
	}
	
	/**
	 * Compute the inverse dynamics using recursive Newton-Euler algorithm.
	 * Uses the current position, velocity, and acceleration of each joint.<br>
	 * <br>
	 * See <a href="https://www.gamedeveloper.com/programming/create-your-own-inverse-dynamics-in-unity">https://www.gamedeveloper.com/programming/create-your-own-inverse-dynamics-in-unity</a>
	 * @return the torque at each joint
	 */
	public double [] getTorques() {
		//rootToFinal();
		//finalToRoot();
	
		double [] p = new double[getNumBones()];
		for(int i=0;i<getNumBones();++i) {
			p[i] = bones.get(i).getTorque().length();
		}
		return p;
	}
	
	public void save() throws Exception {
		// TODO expand this later with more choice of file formats?
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");  
	    LocalDateTime now = LocalDateTime.now();
	    String filePath = this.getClass().getSimpleName()+"-"+dtf.format(now)+".urdf";
		Log.message("Saving as "+filePath);
	    
		RobotArmSaveToRO saver = new RobotArmSaveToRO();
		saver.save(filePath,this);
	}
}
