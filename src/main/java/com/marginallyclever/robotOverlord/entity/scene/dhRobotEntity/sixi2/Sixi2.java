package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import java.util.List;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.entity.scene.Scene;
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
	public Sixi2Model live = new Sixi2Live();
	public Sixi2Model sim = new Sixi2Sim();
	//public SixiJoystick joystick = new SixiJoystick();
	
	public Sixi2() {
		super();
		setName("Sixi 2");
		
		addChild(live);
		addChild(sim);
		//addChild(joystick);

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

		((Sixi2Live)live).renderCartesianForce(gl2);
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

		if(InputManager.isReleased(InputManager.Source.KEY_DELETE)
		|| InputManager.isOn(InputManager.Source.STICK_TRIANGLE)) {
			sim.set(live);
		}
	}

	
	@Override
	public void update(double dt) {
		//driveFromKeyState(dt);
		
		if(live.readyForCommands) {
			//Log.message(controlMode + " " + operatingMode + " send command: "+line);
			live.sendCommand(sim.getCommand());
		}

		super.update(dt);
	}

	@Override
	public void setPose(Matrix4d arg0) {
		super.setPose(arg0);
		live.refreshPose();
		sim.refreshPose();
	}

	public double getAcceleration() {
		return sim.getAcceleration();
	}

	public void setAcceleration(double v) {
		sim.setAcceleration(v);
	}

	public double getFeedRate() {
		return sim.getFeedrate();
	}

	public void setFeedRate(double v) {
		sim.setFeedRate(v);
	}

	/**
	 * Processes a single instruction meant for the robot.
	 * @param line command to send
	 * @return true if the command is sent to the robot.
	 */
	public void sendCommand(String command) {
		sim.sendCommand(command);
	}

	public String getCommand() {
		return sim.getCommand();
	}

	public void goHome() {
		sim.goHome();
	}

	public void goRest() {
	    sim.goRest();
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("S","Sixi");/*
		TODO finish me - a way to type + send gcode to the robot, read responses.  
		view.addButton("Open console").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				MessageMonitor mm = new MessageMonitor();
				mm.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			}
		});*/
		view.popStack();
		
		super.getView(view);
	}
}

