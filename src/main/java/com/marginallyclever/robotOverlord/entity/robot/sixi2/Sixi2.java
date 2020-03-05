package com.marginallyclever.robotOverlord.entity.robot.sixi2;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.Matrix4dTurtle;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.DragBall;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHTool;
import com.marginallyclever.robotOverlord.entity.physicalObject.PhysicalObject;
import com.marginallyclever.robotOverlord.entity.robot.Robot;
import com.marginallyclever.robotOverlord.entity.robot.RobotKeyframe;
import com.marginallyclever.robotOverlord.entity.robot.sixi2.sixi2ControlBox.Sixi2ControlBox;
import com.marginallyclever.robotOverlord.entity.world.World;
import com.marginallyclever.robotOverlord.uiElements.InputManager;


public class Sixi2 extends Robot {
	
	public enum ControlMode {
		REAL_TIME("REAL_TIME"),
		RECORD("RECORD");
		
		private String modeName;
		private ControlMode(String s) {
			modeName=s;
		}
		public String toString() {
			return modeName;
		}
	};
	
	public enum OperatingMode {
		LIVE("LIVE"),
		SIM("SIM");
		
		private String modeName;
		private OperatingMode(String s) {
			modeName=s;
		}
		public String toString() {
			return modeName;
		}
	}

	protected Sixi2Panel sixi2Panel;

	public Sixi2Live live = new Sixi2Live();
	public Sixi2Sim sim = new Sixi2Sim();

	public Sixi2Recording recording = new Sixi2Recording();
	
	protected DragBall ball;

	// true if the skeleton should be visualized on screen. Default is false.
	protected boolean isPicked = false;

	// are we live or simulated?
	protected OperatingMode operatingMode=OperatingMode.SIM;
	// are we trying to drive the robot live?
	protected ControlMode controlMode=ControlMode.REAL_TIME;
	
	public Matrix4dTurtle interpolator = new Matrix4dTurtle();
	protected Matrix4d interpolatedMatrix = new Matrix4d();

	protected boolean singleBlock = false;
	protected boolean cycleStart = false;
	protected boolean m01Break = true;
	
	public Sixi2() {
		super();
		setName("Sixi");
		
		addChild(live);
		addChild(sim);
		
		ball = new DragBall();
		ball.setParent(this);
		
		// spawn a control box.
		// TODO this box is a child, which is wrong.
		// I think only tools should be children.
		Sixi2ControlBox sixi2ControlBox=new Sixi2ControlBox();
		this.addChild(sixi2ControlBox);
		sixi2ControlBox.setPosition(new Vector3d(0,39,14));
		sixi2ControlBox.setRotation(new Vector3d(0, 0, Math.toRadians(90)));
		sixi2ControlBox.getMaterial().setDiffuseColor(1,217f/255f,33f/255f,1);
	}

	/**
	 * Attach the nearest tool Detach the active tool if there is one.
	 */
	public void toggleATC() {
		if (sim.dhTool != null) {
			// we have a tool, release it.
			sim.removeTool();
			return;
		}

		// we have no tool. Look out into the world...
		World world = getWorld();
		if (world != null) {
			// Request from the world "is there a tool at the position of the end effector"?
			Vector3d target = new Vector3d();
			sim.getEndEffectorMatrix().get(target);
			List<PhysicalObject> list = world.findPhysicalObjectsNear(target, 10);

			// If there is a tool, attach to it.
			for( PhysicalObject po : list ) {
				if (po instanceof DHTool) {
					// probably the only one we'll find.
					sim.setTool((DHTool) po);
				}
			}
		}
	}
	
	
	@Override
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		
		// hide the dhrobot panel because we'll replace it with our own.
		sixi2Panel = new Sixi2Panel(gui,this);
		list.add(sixi2Panel);
		
		return list;
	}
	
	
	@Override
	public void render(GL2 gl2) {		
		for( DHLink link : sim.links ) {
			link.refreshPoseMatrix();
		}
		
		IntBuffer depthFunc = IntBuffer.allocate(1);
		gl2.glGetIntegerv(GL2.GL_DEPTH_FUNC, depthFunc);
		boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDepthFunc(GL2.GL_ALWAYS);
		gl2.glDisable(GL2.GL_LIGHTING);

		interpolator.render(gl2);
		if(interpolator.isInterpolating()) {
			MatrixHelper.drawMatrix2(gl2, interpolatedMatrix, 2);
		}
		
		if(isPicked && inDirectDriveMode()) {
			ball.render(gl2);
		}
		
		if (isLit) gl2.glEnable(GL2.GL_LIGHTING);
		gl2.glDepthFunc(depthFunc.get(0));
		
		// then draw the target pose, aka the ghost.
		super.render(gl2);
	}

	@Override
	public void dataAvailable(NetworkConnection arg0,String data) {
		live.dataAvailable(arg0, data);
		
		super.dataAvailable(arg0, data);
	}
	

	/**
	 * move the finger tip of the arm if the InputManager says so. The direction and
	 * torque of the movement is controlled by a frame of reference.
	 * 
	 * @return true if targetPose changes.
	 */
	public boolean driveFromKeyState(double dt) {
		ball.setSubjectMatrix(sim.links.get(sim.getNumLinks()-1).getPoseCumulative());
		ball.setCameraMatrix(getWorld().getCamera().getPose());	
		
		boolean isDirty = false;
		/*
		final double scale = 10*dt;
		final double scaleDolly = 10*dt;
		//if (InputManager.isOn(InputManager.STICK_SQUARE)) {}
		//if (InputManager.isOn(InputManager.STICK_CIRCLE)) {}
		//if (InputManager.isOn(InputManager.STICK_X)) {}
		if (InputManager.isOn(InputManager.STICK_TRIANGLE)) {
			ghost.setPoseFK(homeKey);
			ghost.setTargetMatrix(ghost.getLiveMatrix());
			isDirty = true;
		}
		if (InputManager.isOn(InputManager.STICK_TOUCHPAD)) {
			// this.toggleATC();
		}

		int dD = (int) InputManager.rawValue(InputManager.STICK_DPADY);
		if (dD != 0) {
			double d =dhTool.dhLinkEquivalent.getD() + dD * scaleDolly; 
			dhTool.dhLinkEquivalent.setD(Math.max(d, 0));
			isDirty = true;
		}
		int dR = (int) InputManager.rawValue(InputManager.STICK_DPADX); // dpad left/right
		if (dR != 0) {
			double r =dhTool.dhLinkEquivalent.getR() + dR * scale; 
			dhTool.dhLinkEquivalent.setR(Math.max(r,0));
			isDirty = true;
		}

		// https://robotics.stackexchange.com/questions/12782/how-rotate-a-point-around-an-arbitrary-line-in-3d
		if (InputManager.isOn(InputManager.STICK_L1) != InputManager.isOn(InputManager.STICK_R1)) {
			if (canTargetPoseRotateZ()) {
				isDirty = true;
				double vv = scaleTurnRadians;
				if (dhTool != null && dhTool.dhLinkEquivalent.r > 1) {
					vv /= dhTool.dhLinkEquivalent.r;
				}

				rollZ(InputManager.isOn(InputManager.STICK_L1) ? vv : -vv);
			}
		}

		if (InputManager.rawValue(InputManager.STICK_RX) != 0) {
			if (canTargetPoseRotateY()) {
				isDirty = true;
				rollY(InputManager.rawValue(InputManager.STICK_RX) * scaleTurnRadians);
			}
		}
		if (InputManager.rawValue(InputManager.STICK_RY) != 0) {
			if (canTargetPoseRotateX()) {
				isDirty = true;
				rollX(InputManager.rawValue(InputManager.STICK_RY) * scaleTurnRadians);
			}
		}
		if (InputManager.rawValue(InputManager.STICK_R2) != -1) {
			isDirty = true;
			translate(getForward(), ((InputManager.rawValue(InputManager.STICK_R2) + 1) / 2) * scale);
		}
		if (InputManager.rawValue(InputManager.STICK_L2) != -1) {
			isDirty = true;
			translate(getForward(), ((InputManager.rawValue(InputManager.STICK_L2) + 1) / 2) * -scale);
		}
		if (InputManager.rawValue(InputManager.STICK_LX) != 0) {
			isDirty = true;
			translate(getRight(), InputManager.rawValue(InputManager.STICK_LX) * scale);
		}
		if (InputManager.rawValue(InputManager.STICK_LY) != 0) {
			isDirty = true;
			translate(getUp(), InputManager.rawValue(InputManager.STICK_LY) * -scale);
		}*/

		/* adjust distance to point around which the hand is rotating.
		if (InputManager.isOn(InputManager.KEY_LCONTROL) || InputManager.isOn(InputManager.KEY_RCONTROL)) {
			
			if (InputManager.rawValue(InputManager.MOUSE_Y) != 0) {
				double d = dhTool.dhLinkEquivalent.getD()
						+ InputManager.rawValue(InputManager.MOUSE_Y) * scaleDolly;
				dhTool.dhLinkEquivalent.setD(Math.max(d, 0));
				isDirty = true;
			}
			if (InputManager.rawValue(InputManager.MOUSE_X) != 0) {
				double r = dhTool.dhLinkEquivalent.getR() 
						+ InputManager.rawValue(InputManager.MOUSE_X) * scaleDolly;
				dhTool.dhLinkEquivalent.setR(Math.max(r, 0));
				isDirty = true;
			}
		}*/

		
		ball.update(dt);
		
		if (InputManager.isOn(InputManager.Source.MOUSE_LEFT)) {
			if(ball.isActivelyMoving())
			{
				Matrix4d worldPose = new Matrix4d(ball.getResultMatrix());
				
				// The ghost only accepts poses in it's frame of reference.
				// so we have to account for the robot's pose in world space.
				//Matrix4d iRoot = new Matrix4d(this.matrix);
				//iRoot.invert();
				//worldPose.mul(iRoot);
				
				//System.out.println("Update begins");
				sim.setPoseIK(worldPose);
				//System.out.println("Update ends");
				//System.out.println(MatrixHelper.getPosition(worldPose));
				isDirty=true;
			}
		}
		
		if (sim.dhTool != null) {
			isDirty |= sim.dhTool.directDrive();
		}
		
		if(controlMode==ControlMode.REAL_TIME) {
			if (InputManager.isReleased(InputManager.Source.KEY_RETURN) 
				|| InputManager.isReleased(InputManager.Source.KEY_ENTER)
				|| InputManager.isReleased(InputManager.Source.STICK_X) 
					) {
				// commit move!
				// if we have a live connection, send it.
				live.sendCommand(sim.getCommand());
			}
		}

		if (InputManager.isOn(InputManager.Source.KEY_DELETE)
			|| InputManager.isOn(InputManager.Source.STICK_TRIANGLE)) {
			sim.set(live);
		}

		return isDirty;
	}
	
	@Override
	public void update(double dt) {
		super.update(dt);

		live.update(dt);
		sim.update(dt);

		if (inDirectDriveMode() && isPicked) {
			driveFromKeyState(dt);
		}
	}
	
	/**
	 * Direct Drive Mode means that we're not playing animation of any kind. That
	 * means no gcode running, no scrubbing on a timeline, or any other kind of
	 * external control.
	 * 
	 * @return true if we're in direct drive mode.
	 */
	protected boolean inDirectDriveMode() {
		return true;// interpolatePoseT>=1.0 ;
	}


	@Override
	public void pick() {
		isPicked = true;
	}

	@Override
	public void unPick() {
		isPicked = false;
	}

	protected boolean canSubjectRotateX() {
		return true;
	}

	protected boolean canSubjectRotateY() {
		return true;
	}

	protected boolean canSubjectRotateZ() {
		return true;
	}

	@Override
	public RobotKeyframe createKeyframe() {
		return null;
	}

	@Override
	public String getStatusMessage() {
		Matrix4d pose=sim.getEndEffectorMatrix();
		Matrix3d m = new Matrix3d();
		pose.get(m);
		Vector3d v = MatrixHelper.matrixToEuler(m);
		String message = 
				"Base @ "+this.getPosition().toString() + " Tip @ "
				+" ("+StringHelper.formatDouble(pose.m03)
				+", "+StringHelper.formatDouble(pose.m13)
				+", "+StringHelper.formatDouble(pose.m23)
				+") R("+StringHelper.formatDouble(Math.toDegrees(v.x))
				+", "+StringHelper.formatDouble(Math.toDegrees(v.y))
				+", "+StringHelper.formatDouble(Math.toDegrees(v.z))
				+")";
		if(ball.isActivelyMoving) {
			message += " "+ball.getStatusMessage();
		}
		return message;
	}


	/**
	 * 
	 * @return a list of cuboids, or null.
	 */
	@Override
	public ArrayList<Cuboid> getCuboidList() {

		// get a list of all cuboids
		ArrayList<Cuboid> cuboidList = new ArrayList<Cuboid>();
		
		sim.refreshPose();
		
		for( DHLink link : this.sim.links ) {
			if(link.getCuboid() != null ) {
				cuboidList.add(link.getCuboid());
			}
		}

		return cuboidList;
	}
	
	@Override
	public void setPose(Matrix4d arg0) {
		super.setPose(arg0);
		live.refreshPose();
		sim.refreshPose();
	}


	public void reset() {
		singleBlock = false;
		cycleStart = false;
		m01Break = true;
	}

	public void toggleCycleStart() {
		cycleStart = !cycleStart;
	}

	public void toggleSingleBlock() {
		singleBlock = !singleBlock;
	}

	public void toggleM01Break() {
		m01Break = !m01Break;
	}
	
	public ControlMode getControlMode() { return controlMode; }

	public void toggleControlMode() {
		controlMode = (controlMode==ControlMode.REAL_TIME) ? ControlMode.RECORD : ControlMode.REAL_TIME;

		if(controlMode==ControlMode.REAL_TIME) {
			// move the joystick to match the simulated position?
		}

		reset();
	}

	/**
	 * Processes a single instruction meant for the robot.
	 * @param line command to send
	 * @return true if the command is sent to the robot.
	 */
	@Override
	public boolean sendCommand(String command) {
		if(operatingMode==OperatingMode.LIVE) {
			live.sendCommand(command);
		}
		sim.sendCommand(command);
		
		return true;
	}

	public double getAcceleration() {
		if(operatingMode==OperatingMode.LIVE) {
			return live.getAcceleration();
		} else {
			return sim.getAcceleration();
		}
	}
	
	public double getFeedRate() {
		if(operatingMode==OperatingMode.LIVE) {
			return live.getFeedrate();
		} else {
			return sim.getFeedrate();
		}
	}

	public void setAcceleration(double v) {
		if(operatingMode==OperatingMode.LIVE) {
			live.setAcceleration(v);
		}
		sim.setAcceleration(v);
	}

	public void setFeedRate(double v) {
		if(operatingMode==OperatingMode.LIVE) {
			live.setFeedRate(v);
		}
		sim.setFeedRate(v);
	}

	public String getCommand() {
		if(operatingMode==OperatingMode.LIVE) {
			return live.getCommand();
		} else {
			return sim.getCommand();
		}
	}

	public boolean isPicked() {
		return isPicked;
	}

	public boolean isSingleBlock() {
		return singleBlock;
	}

	public boolean isCycleStart() {
		return cycleStart;
	}

	public boolean isM01Break() {
		return m01Break;
	}
}
