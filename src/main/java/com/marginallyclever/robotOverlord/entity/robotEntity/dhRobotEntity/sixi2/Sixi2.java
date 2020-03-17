package com.marginallyclever.robotOverlord.entity.robotEntity.dhRobotEntity.sixi2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.entity.modelEntity.ModelEntity;
import com.marginallyclever.robotOverlord.entity.primitives.PhysicalEntity;
import com.marginallyclever.robotOverlord.entity.robotEntity.RobotEntity;
import com.marginallyclever.robotOverlord.entity.robotEntity.RobotKeyframe;
import com.marginallyclever.robotOverlord.entity.robotEntity.dhRobotEntity.DHKeyframe;
import com.marginallyclever.robotOverlord.entity.robotEntity.dhRobotEntity.dhLink.DHLink;
import com.marginallyclever.robotOverlord.entity.robotEntity.dhRobotEntity.dhTool.DHTool;
import com.marginallyclever.robotOverlord.entity.world.World;
import com.marginallyclever.robotOverlord.swingInterface.InputManager;


/**
 * A controller for the simulated and live Sixi robot.
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class Sixi2 extends RobotEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3770331480833527099L;

	public enum ControlMode {
		RECORD(0,"RECORD"),
		PLAYBACK(1,"PLAYBACK");

		private int modeNumber;
		private String modeName;
		private ControlMode(int n,String s) {
			modeNumber=n;
			modeName=s;
		}
		public int toInt() {
			return modeNumber;
		}
		public String toString() {
			return modeName;
		}
		static public String [] getAll() {
			ControlMode[] allModes = ControlMode.values();
			String[] labels = new String[allModes.length];
			for(int i=0;i<labels.length;++i) {
				labels[i] = allModes[i].toString();
			}
			return labels;
		}
	};

	public enum OperatingMode {
		LIVE(0,"LIVE"),
		SIM(1,"SIM");

		private int modeNumber;
		private String modeName;
		private OperatingMode(int n,String s) {
			modeNumber=n;
			modeName=s;
		}
		public int toInt() {
			return modeNumber;
		}
		public String toString() {
			return modeName;
		}
		static public String [] getAll() {
			OperatingMode[] allModes = OperatingMode.values();
			String[] labels = new String[allModes.length];
			for(int i=0;i<labels.length;++i) {
				labels[i] = allModes[i].toString();
			}
			return labels;
		}
	}

	public Sixi2Live live = new Sixi2Live();
	public Sixi2Sim sim = new Sixi2Sim();

	public Sixi2Recording recording = new Sixi2Recording();

	// are we live or simulated?
	protected OperatingMode operatingMode=OperatingMode.SIM;

	// are we trying to record the robot?
	protected ControlMode controlMode=ControlMode.RECORD;

	protected BooleanEntity singleBlock = new BooleanEntity("Single Block",false);
	protected BooleanEntity cycleStart = new BooleanEntity("Cycle Start",false);
	protected BooleanEntity m01Break = new BooleanEntity("M01 Break",true);
	
	public Sixi2() {
		super();

		showBoundingBox.addObserver(this);
		showLocalOrigin.addObserver(this);
		showLineage.addObserver(this);
		
		setName("Sixi");
		addChild(singleBlock);
		addChild(cycleStart);
		addChild(m01Break);
		
		addChild(live);
		addChild(sim);
		
		singleBlock.addObserver(this);
		cycleStart.addObserver(this);
		m01Break.addObserver(this);

		ModelEntity anchor = new ModelEntity();
		addChild(anchor);
		anchor.setName("Base");
		anchor.setModelFilename("/Sixi2/anchor.stl");
		anchor.setModelOrigin(0, 0, 0.9);

		setShowLineage(true);
		setShowLocalOrigin(true);
		setShowBoundingBox(true);
		
		// spawn a control box as a child of the anchor.
		ModelEntity sixi2ControlBox=new ModelEntity();
		addChild(sixi2ControlBox);
		sixi2ControlBox.setName("Control Box");
		sixi2ControlBox.setModelFilename("/Sixi2/box.stl");
		sixi2ControlBox.setPosition(new Vector3d(0,39,14));
		sixi2ControlBox.setRotation(new Vector3d(Math.toRadians(90), 0, 0));
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
			List<PhysicalEntity> list = world.findPhysicalObjectsNear(target, 10);
			// If there is a tool, attach to it.
			for( PhysicalEntity po : list ) {
				if (po instanceof DHTool) {
					// probably the only one we'll find.
					sim.setTool((DHTool) po);
				}
			}
		}
	}

	@Override
	public void render(GL2 gl2) {
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
	public void driveFromKeyState(double dt) {
		/*
		// move the robot by dragging the ball in live mode
		DragBall ball = getWorld().getBall();
		
		if(!isPicked) {
			ball.setSubject(null);
			return;
		}

		if(controlMode == ControlMode.RECORD) {
			ball.setSubject(this);
			
			if (InputManager.isOn(InputManager.Source.MOUSE_LEFT)) {
				if(ball.isActivelyMoving()) {
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
				}
			}
		} else {
			ball.setSubject(null);
		}
*/
		if (sim.dhTool != null) {
			sim.dhTool.directDrive();
		}


		if(InputManager.isReleased(InputManager.Source.KEY_SPACE)) {			reset();					}
		if(InputManager.isReleased(InputManager.Source.KEY_TAB  )) {			toggleControlMode();		}
		if(InputManager.isReleased(InputManager.Source.KEY_TILDE)) {			toggleOperatingMode();		}
		if(InputManager.isReleased(InputManager.Source.KEY_S    )) {			toggleSingleBlock();		}

		if(InputManager.isReleased(InputManager.Source.KEY_DELETE)
		|| InputManager.isOn(InputManager.Source.STICK_TRIANGLE)) {
			sim.set(live);
		}

		if(InputManager.isOn(InputManager.Source.KEY_LSHIFT)
		|| InputManager.isOn(InputManager.Source.KEY_RSHIFT)) {
			if(InputManager.isReleased(InputManager.Source.KEY_ENTER)
			|| InputManager.isReleased(InputManager.Source.KEY_RETURN)) {
				if(controlMode==ControlMode.PLAYBACK) {
					toggleCycleStart();
				}
			}
		} else {
			// shift off
			if(InputManager.isReleased(InputManager.Source.KEY_ENTER)
			|| InputManager.isReleased(InputManager.Source.KEY_RETURN)) {
				if(controlMode==ControlMode.RECORD) {
					System.out.println("setCommand");
					setCommand();
				}
			}
			if(InputManager.isReleased(InputManager.Source.KEY_PLUS)) {
				System.out.println("addCommand");
				addCommand();
			}
			if(InputManager.isReleased(InputManager.Source.KEY_DELETE)) {
				System.out.println("deleteCurrentCommand");
				deleteCurrentCommand();
			}
			if(InputManager.isReleased(InputManager.Source.KEY_BACKSPACE)) {
				System.out.println("deletePreviousCommand");
				recording.deletePreviousCommand();
			}
			if(InputManager.isReleased(InputManager.Source.KEY_LESSTHAN)) {
				System.out.println("PreviousCommand");
				if(recording.hasPrev()) {
			          String line=recording.prev();
			          sim.sendCommand(line);
				}
			}
			if(InputManager.isReleased(InputManager.Source.KEY_GREATERTHAN)) {
				System.out.println("NextCommand");
		        if(recording.hasNext()) {
		            String line=recording.next();
		            sim.sendCommand(line);
		        }
			}
		}
	}

	@Override
	public void update(double dt) {
		driveFromKeyState(dt);
		
		Sixi2Model activeModel = (operatingMode == OperatingMode.LIVE) ? live : sim;
		if(activeModel.readyForCommands) {
			if(controlMode == ControlMode.RECORD) {
				if( operatingMode == OperatingMode.LIVE) {
					String line = sim.getCommand();
					System.out.println(controlMode + " " + operatingMode + " send command: "+line);
					activeModel.sendCommand(line);
				}
			} else {
				if(cycleStart.get() && recording.hasNext()) {
					String line = recording.next();
					System.out.println(controlMode + " " + operatingMode + " send command: "+line);
					activeModel.sendCommand(line);
					if(singleBlock.get()) {
						// one block at a time
						cycleStart.set(false);
					}
				} else {
					// no more recording, stop.
					cycleStart.set(false);
				}
			}
			// active model is updated when all children are updated
		}

		super.update(dt);
	}

	@Override
	public RobotKeyframe createKeyframe() {
		return null;
	}

	@Deprecated
	public String getStatusMessage() {
		String message = "";//super.getStatusMessage();
		
		Matrix4d pose=sim.getEndEffectorMatrix();
		Matrix3d m = new Matrix3d();
		pose.get(m);
		Vector3d v = MatrixHelper.matrixToEuler(m);
		message +=
				"Base @ "+this.getPosition().toString() + " Tip @ "
				+" ("+StringHelper.formatDouble(pose.m03)
				+", "+StringHelper.formatDouble(pose.m13)
				+", "+StringHelper.formatDouble(pose.m23)
				+") R("+StringHelper.formatDouble(Math.toDegrees(v.x))
				+", "+StringHelper.formatDouble(Math.toDegrees(v.y))
				+", "+StringHelper.formatDouble(Math.toDegrees(v.z))
				+")";
		return message;
	}

	/**
	 * @return a list of cuboids, or null.
	 */
	@Override
	public ArrayList<Cuboid> getCuboidList() {

		// get a list of all cuboids
		ArrayList<Cuboid> cuboidList = new ArrayList<Cuboid>();

		sim.refreshPose();

		for( DHLink link : this.sim.links ) {
			if(link.getCuboid() != null ) {
				cuboidList.addAll(link.getCuboidList());
			}
		}
		if(sim.dhTool != null) {
			cuboidList.addAll(sim.dhTool.getCuboidList());
		}

		return cuboidList;
	}

	@Override
	public void setPose(Matrix4d arg0) {
		super.setPose(arg0);
		live.refreshPose();
		sim.refreshPose();
	}

	public double getAcceleration() {
		if(operatingMode==OperatingMode.LIVE) {
			return live.getAcceleration();
		} else {
			return sim.getAcceleration();
		}
	}

	public void setAcceleration(double v) {
		if(operatingMode==OperatingMode.LIVE) {
			live.setAcceleration(v);
		}
		sim.setAcceleration(v);
	}

	public double getFeedRate() {
		if(operatingMode==OperatingMode.LIVE) {
			return live.getFeedrate();
		} else {
			return sim.getFeedrate();
		}
	}

	public void setFeedRate(double v) {
		if(operatingMode==OperatingMode.LIVE) {
			live.setFeedRate(v);
		}
		sim.setFeedRate(v);
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

	public String getCommand() {
		if(operatingMode==OperatingMode.LIVE) {
			return live.getCommand();
		} else {
			return sim.getCommand();
		}
	}

	public void reset() {
		recording.reset();
		singleBlock.set(false);
		cycleStart.set(false);
		m01Break.set(true);

		System.out.println("reset "+recording.getNumCommands());
	}

	public void toggleCycleStart() {
		cycleStart.toggle();
		//System.out.println("cycleStart="+(cycleStart?"on":"off"));
	}

	public void setCycleStart(boolean arg0) {
		cycleStart.set(arg0);
		//System.out.println("cycleStart="+(cycleStart?"on":"off"));
	}

	public boolean isCycleStart() {
		return cycleStart.get();
	}

	public void toggleSingleBlock() {
		singleBlock.toggle();
		//System.out.println("singleBlock="+(singleBlock?"on":"off"));
	}

	public boolean isSingleBlock() {
		return singleBlock.get();
	}

	public void toggleM01Break() {
		m01Break.toggle();
		//System.out.println("m01Break="+(m01Break?"on":"off"));
	}

	public boolean isM01Break() {
		return m01Break.get();
	}

	public ControlMode getControlMode() {
		return controlMode;
	}

	public void toggleControlMode() {
		controlMode = (controlMode==ControlMode.RECORD) ? ControlMode.PLAYBACK : ControlMode.RECORD;
		System.out.println("controlMode="+controlMode);

		if(controlMode==ControlMode.RECORD) {
			// move the joystick to match the simulated position?
		}

		reset();
	}

	public OperatingMode getOperatingMode() {
		return operatingMode;
	}

	public void toggleOperatingMode() {
		operatingMode = (operatingMode==OperatingMode.LIVE) ? OperatingMode.SIM : OperatingMode.LIVE;
		//System.out.println("operatingMode="+operatingMode);
	}

	public ArrayList<String> getCommandList() {
		return recording.getCommandList();
	}

	public void addCommand() {
		recording.addCommand(sim.getCommand());
	}
	
	public void deleteCurrentCommand() {
		recording.deleteCurrentCommand();
	}
	
	public void setCommand() {
		recording.setCommand(sim.getCommand());
	}

	public void loadRecording(String filename) {
		recording.loadRecording(filename);
	}

	public void saveRecording(String filename) {
		recording.saveRecording(filename);
	}

	// recursively set for all children
	public void setShowBoundingBox(boolean arg0) {
		LinkedList<PhysicalEntity> next = new LinkedList<PhysicalEntity>();
		next.add(this.sim);
		while( !next.isEmpty() ) {
			PhysicalEntity link = next.pop();
			link.showBoundingBox.set(arg0);
			for( Entity child : link.getChildren() ) {
				if( child instanceof PhysicalEntity ) {
					next.add((PhysicalEntity)child);
				}
			}
		}
	}
	
	// recursively set for all children
	public void setShowLocalOrigin(boolean arg0) {
		LinkedList<PhysicalEntity> next = new LinkedList<PhysicalEntity>();
		next.add(this.sim);
		while( !next.isEmpty() ) {
			PhysicalEntity link = next.pop();
			link.showLocalOrigin.set(arg0);
			for( Entity child : link.getChildren() ) {
				if( child instanceof PhysicalEntity ) {
					next.add((PhysicalEntity)child);
				}
			}
		}
	}

	// recursively set for all children
	public void setShowLineage(boolean arg0) {
		LinkedList<PhysicalEntity> next = new LinkedList<PhysicalEntity>();
		next.add(this.sim);
		while( !next.isEmpty() ) {
			PhysicalEntity link = next.pop();
			link.showLineage.set(arg0);
			for( Entity child : link.getChildren() ) {
				if( child instanceof PhysicalEntity ) {
					next.add((PhysicalEntity)child);
				}
			}
		}
	}

	public void openConnection() {
		live.openConnection();
	}

	public void goHome() {
	    // the home position
		DHKeyframe homeKey = sim.getIKSolver().createDHKeyframe();
		homeKey.fkValues[0]=0;
		homeKey.fkValues[1]=-90;
		homeKey.fkValues[2]=0;
		homeKey.fkValues[3]=0;
		homeKey.fkValues[4]=20;
		homeKey.fkValues[5]=0;
		sim.setPoseFK(homeKey);
	}

	public void goRest() {
	    // set rest position
		DHKeyframe restKey = sim.getIKSolver().createDHKeyframe();
		restKey.fkValues[0]=0;
		restKey.fkValues[1]=-60-90;
		restKey.fkValues[2]=85+90;
		restKey.fkValues[3]=0;
		restKey.fkValues[4]=20;
		restKey.fkValues[5]=0;
		sim.setPoseFK(restKey);
	}

	public int setCommandIndex(int newIndex) {
		return recording.setCommandIndex(newIndex);
	}
	
	/**
	 * Something this Entity is observing has changed.  Deal with it!
	 */
	@Override
	public void update(Observable o, Object arg) {
		if(o==showBoundingBox) setShowBoundingBox((boolean)arg);
		if(o==showLocalOrigin) setShowLocalOrigin((boolean)arg);
		if(o==showLineage) setShowLineage((boolean)arg);
	}
}

