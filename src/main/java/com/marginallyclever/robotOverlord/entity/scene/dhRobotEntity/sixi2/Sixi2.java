package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;
import com.marginallyclever.robotOverlord.entity.scene.Scene;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.dhTool.DHTool;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.entity.scene.modelEntity.ModelEntity;
import com.marginallyclever.robotOverlord.log.Log;
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

	public Sixi2Model live = new Sixi2Live();
	public Sixi2Model sim = new Sixi2Sim();

	public Sixi2Recording recording = new Sixi2Recording();
	public SixiJoystick joystick = new SixiJoystick();

	// are we live or simulated?  deep philosophical questions.
	protected IntEntity operatingMode = new IntEntity("Operating mode",OperatingMode.SIM.toInt());
	// are we trying to record the robot?
	protected IntEntity controlMode = new IntEntity("Control mode",ControlMode.RECORD.toInt());

	protected BooleanEntity singleBlock = new BooleanEntity("Single Block",false);
	protected BooleanEntity cycleStart = new BooleanEntity("Cycle Start",false);
	protected BooleanEntity m01Break = new BooleanEntity("M01 Break",true);
	
	public Sixi2() {
		super();
		setName("Sixi 2");
		
		addChild(live);
		addChild(sim);
		addChild(joystick);
		
		addChild(operatingMode);
		addChild(controlMode);
		
		addChild(singleBlock);
		addChild(cycleStart);
		
		singleBlock.addObserver(this);
		cycleStart.addObserver(this);
		m01Break.addObserver(this);

		//setShowLineage(true);
		//setShowLocalOrigin(true);
		//setShowBoundingBox(true);
		
		// spawn a control box as a child of the anchor.
		ModelEntity sixi2ControlBox=new ModelEntity();
		addChild(sixi2ControlBox);
		sixi2ControlBox.setName("Control Box");
		sixi2ControlBox.setModelFilename("/Sixi2/box.obj");
		sixi2ControlBox.setPosition(new Vector3d(0,39,14));
		sixi2ControlBox.setRotation(new Vector3d(Math.toRadians(90), 0, 0));
		sixi2ControlBox.getMaterial().setTextureFilename("/Sixi2/sixi.png");
		sixi2ControlBox.getMaterial().setDiffuseColor(1, 1, 1, 1);
		sixi2ControlBox.getMaterial().setAmbientColor(1, 1, 1, 1);
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
					Log.message("setCommand");
					setCommand();
				}
			}
			if(InputManager.isReleased(InputManager.Source.KEY_PLUS)) {
				Log.message("addCommand");
				addCommand();
			}
			if(InputManager.isReleased(InputManager.Source.KEY_DELETE)) {
				Log.message("deleteCurrentCommand");
				deleteCurrentCommand();
			}
			if(InputManager.isReleased(InputManager.Source.KEY_BACKSPACE)) {
				Log.message("deletePreviousCommand");
				recording.deletePreviousCommand();
			}
			if(InputManager.isReleased(InputManager.Source.KEY_LESSTHAN)) {
				Log.message("PreviousCommand");
				if(recording.hasPrev()) {
			          String line=recording.prev();
			          sim.sendCommand(line);
				}
			}
			if(InputManager.isReleased(InputManager.Source.KEY_GREATERTHAN)) {
				Log.message("NextCommand");
		        if(recording.hasNext()) {
		            String line=recording.next();
		            sim.sendCommand(line);
		        }
			}
		}
	}

	
	@Override
	public void update(double dt) {
		//driveFromKeyState(dt);
		
		Sixi2Model activeModel = (operatingMode.get() == OperatingMode.LIVE.toInt()) ? live : sim;
		if(activeModel.readyForCommands) {
			if(controlMode.get() == ControlMode.RECORD.toInt()) {
				if(activeModel == live) {
					String line = sim.getCommand();
					//Log.message(controlMode + " " + operatingMode + " send command: "+line);
					live.sendCommand(line);
				}
			} else {
				if(cycleStart.get() && recording.hasNext()) {
					String line = recording.next();
					//Log.message(controlMode + " " + operatingMode + " send command: "+line);
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
	public void sendCommand(String command) {
		if(operatingMode.get()==OperatingMode.LIVE.toInt()) {
			live.sendCommand(command);
		}
		sim.sendCommand(command);
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

		Log.message("reset "+recording.getNumCommands());
	}

	public void toggleCycleStart() {
		cycleStart.toggle();
		//Log.message("cycleStart="+(cycleStart?"on":"off"));
	}

	public void setCycleStart(boolean arg0) {
		cycleStart.set(arg0);
		//Log.message("cycleStart="+(cycleStart?"on":"off"));
	}

	public boolean isCycleStart() {
		return cycleStart.get();
	}

	public void toggleSingleBlock() {
		singleBlock.toggle();
		//Log.message("singleBlock="+(singleBlock?"on":"off"));
	}

	public boolean isSingleBlock() {
		return singleBlock.get();
	}

	public void toggleM01Break() {
		m01Break.toggle();
		//Log.message("m01Break="+(m01Break?"on":"off"));
	}

	public boolean isM01Break() {
		return m01Break.get();
	}

	public void toggleControlMode() {
		controlMode.set( (controlMode.get()==ControlMode.RECORD.toInt()) ? ControlMode.PLAYBACK.toInt() : ControlMode.RECORD.toInt() );
		Log.message("controlMode="+controlMode);

		if(controlMode.get()==ControlMode.RECORD.toInt()) {
			// move the joystick to match the simulated position?
		}

		reset();
	}

	public void toggleOperatingMode() {
		operatingMode.set( (operatingMode.get()==OperatingMode.LIVE.toInt()) ? OperatingMode.SIM.toInt() : OperatingMode.LIVE.toInt() );
		//Log.message("operatingMode="+operatingMode);
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

	public void goHome() {
		sim.goHome();
	}

	public void goRest() {
	    sim.goRest();
	}

	public int setCommandIndex(int newIndex) {
		return recording.setCommandIndex(newIndex);
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("S","Sixi");
		view.addComboBox(operatingMode,OperatingMode.getAll());
		view.addComboBox(controlMode,ControlMode.getAll());
		
		view.add(singleBlock);
		view.add(cycleStart);
		view.add(m01Break);
		
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

