package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;
import com.marginallyclever.robotOverlord.entity.scene.Scene;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHKeyframe;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.dhTool.DHTool;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.entity.scene.modelEntity.ModelEntity;
import com.marginallyclever.robotOverlord.swingInterface.InputManager;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;


/**
 * A controller for the simulated and live Sixi robot.
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class Sixi2 extends PoseEntity {
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
	protected IntEntity operatingMode = new IntEntity("Operating mode",OperatingMode.SIM.toInt());
	// are we trying to record the robot?
	protected IntEntity controlMode = new IntEntity("Control mode",ControlMode.RECORD.toInt());

	protected BooleanEntity singleBlock = new BooleanEntity("Single Block",false);
	protected BooleanEntity cycleStart = new BooleanEntity("Cycle Start",false);
	protected BooleanEntity m01Break = new BooleanEntity("M01 Break",true);
	
	public Sixi2() {
		super();
		setName("Sixi");

		showBoundingBox.addObserver(this);
		showLocalOrigin.addObserver(this);
		showLineage.addObserver(this);
		
		addChild(operatingMode);
		addChild(controlMode);
		
		addChild(singleBlock);
		addChild(cycleStart);
		addChild(m01Break);
		
		addChild(live);
		addChild(sim);
		
		singleBlock.addObserver(this);
		cycleStart.addObserver(this);
		m01Break.addObserver(this);

		setShowLineage(true);
		setShowLocalOrigin(true);
		setShowBoundingBox(false);
		
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
		Scene world = getWorld();
		if (world != null) {
			// Request from the world "is there a tool at the position of the end effector"?
			Vector3d target = MatrixHelper.getPosition(sim.endEffector.getPoseWorld());
			List<PoseEntity> list = world.findPhysicalObjectsNear(target, 10);
			// If there is a tool, attach to it.
			for( PoseEntity po : list ) {
				if (po instanceof DHTool) {
					// probably the only one we'll find.
					sim.setTool((DHTool) po);
				}
			}
		}
	}

	@Override
	public void render(GL2 gl2) {
		// convenient place for a debugging breakpoint
		super.render(gl2);
	}

	/**
	 * move the finger tip of the arm if the InputManager says so. The direction and
	 * torque of the movement is controlled by a frame of reference.
	 *
	 * @return true if targetPose changes.
	 */
	public void driveFromKeyState(double dt) {
		if (sim.dhTool != null) {
			sim.dhTool.directDrive();
		}

		if(InputManager.isReleased(InputManager.Source.KEY_SPACE)) {	reset();				}
		if(InputManager.isReleased(InputManager.Source.KEY_TAB  )) {	toggleControlMode();	}
		if(InputManager.isReleased(InputManager.Source.KEY_TILDE)) {	toggleOperatingMode();	}
		if(InputManager.isReleased(InputManager.Source.KEY_S    )) {	toggleSingleBlock();	}

		if(InputManager.isReleased(InputManager.Source.KEY_DELETE)
		|| InputManager.isOn(InputManager.Source.STICK_TRIANGLE)) {
			sim.set(live);
		}

		if(InputManager.isOn(InputManager.Source.KEY_LSHIFT)
		|| InputManager.isOn(InputManager.Source.KEY_RSHIFT)) {
			if(InputManager.isReleased(InputManager.Source.KEY_ENTER)
			|| InputManager.isReleased(InputManager.Source.KEY_RETURN)) {
				if(controlMode.get()==ControlMode.PLAYBACK.toInt()) {
					toggleCycleStart();
				}
			}
		} else {
			// shift off
			if(InputManager.isReleased(InputManager.Source.KEY_ENTER)
			|| InputManager.isReleased(InputManager.Source.KEY_RETURN)) {
				if(controlMode.get()==ControlMode.RECORD.toInt()) {
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
		
		Sixi2Model activeModel = (operatingMode.get() == OperatingMode.LIVE.toInt()) ? live : sim;
		if(activeModel.readyForCommands) {
			if(controlMode.get() == ControlMode.RECORD.toInt()) {
				if(activeModel == live) {
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

	@Deprecated
	public String getStatusMessage() {
		String message = "";//super.getStatusMessage();
		
		Matrix4d pose=sim.endEffector.getPoseWorld();
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
		if(operatingMode.get()==OperatingMode.LIVE.toInt()) {
			return live.getAcceleration();
		} else {
			return sim.getAcceleration();
		}
	}

	public void setAcceleration(double v) {
		if(operatingMode.get()==OperatingMode.LIVE.toInt()) {
			live.setAcceleration(v);
		}
		sim.setAcceleration(v);
	}

	public double getFeedRate() {
		if(operatingMode.get()==OperatingMode.LIVE.toInt()) {
			return live.getFeedrate();
		} else {
			return sim.getFeedrate();
		}
	}

	public void setFeedRate(double v) {
		if(operatingMode.get()==OperatingMode.LIVE.toInt()) {
			live.setFeedRate(v);
		}
		sim.setFeedRate(v);
	}

	/**
	 * Processes a single instruction meant for the robot.
	 * @param line command to send
	 * @return true if the command is sent to the robot.
	 */
	public boolean sendCommand(String command) {
		if(operatingMode.get()==OperatingMode.LIVE.toInt()) {
			live.sendCommand(command);
		}
		sim.sendCommand(command);

		return true;
	}

	public String getCommand() {
		if(operatingMode.get()==OperatingMode.LIVE.toInt()) {
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

	public void toggleControlMode() {
		controlMode.set( (controlMode.get()==ControlMode.RECORD.toInt()) ? ControlMode.PLAYBACK.toInt() : ControlMode.RECORD.toInt() );
		System.out.println("controlMode="+controlMode);

		if(controlMode.get()==ControlMode.RECORD.toInt()) {
			// move the joystick to match the simulated position?
		}

		reset();
	}

	public void toggleOperatingMode() {
		operatingMode.set( (operatingMode.get()==OperatingMode.LIVE.toInt()) ? OperatingMode.SIM.toInt() : OperatingMode.LIVE.toInt() );
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
		LinkedList<PoseEntity> next = new LinkedList<PoseEntity>();
		next.add(this.sim.getLink(0));
		while( !next.isEmpty() ) {
			PoseEntity link = next.pop();
			link.showBoundingBox.set(arg0);
			for( Entity child : link.getChildren() ) {
				if( child instanceof PoseEntity ) {
					next.add((PoseEntity)child);
				}
			}
		}
		this.showBoundingBox.set(arg0);
	}
	
	// recursively set for all children
	public void setShowLocalOrigin(boolean arg0) {
		LinkedList<PoseEntity> next = new LinkedList<PoseEntity>();
		next.add(this.sim.getLink(0));
		while( !next.isEmpty() ) {
			PoseEntity link = next.pop();
			link.showLocalOrigin.set(arg0);
			for( Entity child : link.getChildren() ) {
				if( child instanceof PoseEntity ) {
					next.add((PoseEntity)child);
				}
			}
		}
		this.showLocalOrigin.set(arg0);
	}

	// recursively set for all children
	public void setShowLineage(boolean arg0) {
		LinkedList<PoseEntity> next = new LinkedList<PoseEntity>();
		next.add(this.sim.getLink(0));
		while( !next.isEmpty() ) {
			PoseEntity link = next.pop();
			link.showLineage.set(arg0);
			for( Entity child : link.getChildren() ) {
				if( child instanceof PoseEntity ) {
					next.add((PoseEntity)child);
				}
			}
		}
		this.showLineage.set(arg0);
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
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("S","Sixi");
		view.addComboBox(operatingMode,OperatingMode.getAll());
		view.addComboBox(controlMode,ControlMode.getAll());
		
		view.add(singleBlock);
		view.add(cycleStart);
		view.add(m01Break);
		
		view.add(showBoundingBox);
		view.add(showLocalOrigin);
		view.add(showLineage);
		
		// TODO add sliders to adjust FK values?
		/*
		for(int i=0;i<sim.links.length;++i) {
		  view.addAngle?
		  (
		  	sim.links.get(0).theta,
		  	sim.links.get(0).rangeMax.get(),
		  	sim.links.get(0).rangeMin.get()
		  );
		}
		*/
		view.popStack();
		
		super.getView(view);
	}
}

