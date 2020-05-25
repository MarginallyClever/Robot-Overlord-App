package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import java.nio.IntBuffer;
import java.util.Observable;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.RemoteEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.Vector3dEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHKeyframe;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

public class Sixi2Live extends Sixi2Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = -811684331077697483L;
	
	public static final int MAX_HISTORY = 3;
	
	protected RemoteEntity connection = new RemoteEntity();
	protected DHKeyframe [] receivedKeyframes;
	protected long receivedKeyframeCount;

	protected Vector3dEntity[] PIDs = new Vector3dEntity[6];

	protected boolean waitingForOpenConnection;

	public Sixi2Live() {
		super();
		setName("Live");
		addChild(connection);

		connection.addObserver(this);

		for (int i = 0; i < PIDs.length; ++i) {
			PIDs[i] = new Vector3dEntity("PID " + links.get(i).getLetter(), 0, 0, 0.0);
			addChild(PIDs[i]);
			PIDs[i].addObserver(this);
		}

		// where to store incoming position data
		receivedKeyframes = new DHKeyframe[3];
		for(int i=0;i<receivedKeyframes.length;++i) {
			receivedKeyframes[i] = getIKSolver().createDHKeyframe();
		}
		receivedKeyframeCount=0;
		
		waitingForOpenConnection = true;
	}

	@Override
	public void update(double dt) {
		// TODO Compare jacobian estimated force with reported position to determine compliance.
		super.update(dt);
		
		if (!connection.isConnectionOpen()) {
			waitingForOpenConnection = true;
		}
	}

	protected String lastCommand="";

	@Override
	public void sendCommand(String command) {
		if(lastCommand.contentEquals(command)) return;
		lastCommand = command;

		super.sendCommand(command);
		
		sendCommandToRemoteEntity(command);
	}

	protected void sendCommandToRemoteEntity(String command) {
		if(command == null) return;
		
		// remove any end-of-line characters or whitespace.
		command.trim();
		if(command.isEmpty()) return;

		// remove any comment
		int index = command.indexOf('(');
		if (index != -1) {
			command = command.substring(0, index);
		}

		reportDataSent(command);

		// DO IT
		connection.sendMessage(command);
		// while we wait for reply don't flood the robot with too much data.
		readyForCommands = false;
	}

	public void reportDataSent(String msg) {
		//Log.message("SIX SEND " + msg.trim());
	}

	public void reportDataReceived(String msg) {
		if (msg.trim().isEmpty())
			return;
		//Log.message("SIX RECV " + msg.trim());
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);

		for (int i = 0; i < PIDs.length; ++i) {
			if (o == PIDs[i]) {
				Vector3d newValue = PIDs[i].get();
				String message = "M306 L" + i + " P" + newValue.x + " I" + newValue.y + " D" + newValue.z;
				sendCommandToRemoteEntity(message);
				return;
			}
		}

		if (o == connection) {
			String data = (String) arg;

			boolean unhandled = true;

			// if(!confirmChecksumOK(data)) return;

			reportDataReceived(data);

			if (data.startsWith("> ")) {
				// can only be ready if also done waiting for open connection.
				readyForCommands = !waitingForOpenConnection;
				if(readyForCommands) {
					//Log.message("SIX READY");
				}
				data = data.substring(2);
			}

			// all other data should probably update model
			if (data.startsWith("D17")) {
				unhandled = false;
				data = data.trim();

				String[] tokens = data.split("\\s+");
				if (tokens.length >= 7) {
					try {
						int i = (int)(receivedKeyframeCount%MAX_HISTORY);
						receivedKeyframeCount++;
						receivedKeyframes[i].fkValues[0] = Double.parseDouble(tokens[1]);
						receivedKeyframes[i].fkValues[1] = Double.parseDouble(tokens[2]);
						receivedKeyframes[i].fkValues[2] = Double.parseDouble(tokens[3]);
						receivedKeyframes[i].fkValues[3] = Double.parseDouble(tokens[4]);
						receivedKeyframes[i].fkValues[4] = Double.parseDouble(tokens[5]);
						receivedKeyframes[i].fkValues[5] = Double.parseDouble(tokens[6]);
						setPoseFK(receivedKeyframes[i]);
						refreshPose();
					} catch (NumberFormatException e) {
					}
				}
			}

			if (unhandled) {
				data = data.replace("\n", "");
				// Log.message(AnsiColors.PURPLE+data+AnsiColors.RESET);
			} else {
				// wait until we received something meaningful before we start blasting our data
				// out.
				if (waitingForOpenConnection) {
					waitingForOpenConnection = false;
					sendCommandToRemoteEntity("D50 S1");
					sendCommandToRemoteEntity("D50 S1");
					sendCommandToRemoteEntity("D50 S1");
					// send once
					for (int i = 0; i < PIDs.length; ++i) {
						Vector3d newValue = PIDs[i].get();
						String message = "M306 L" + i + " P" + newValue.x + " I" + newValue.y + " D" + newValue.z;
						sendCommandToRemoteEntity(message);
					}
					readyForCommands = false;

					receivedKeyframeCount=0;
				}
			}
		}
	}

	@Override
	public void setPoseWorld(Matrix4d m) {
	}
	
	// See https://studywolf.wordpress.com/2013/09/02/robot-control-jacobians-velocity-and-force/
	public void renderCartesianForce(GL2 gl2) {
		if(receivedKeyframeCount<2) return;
		
		int i1 = (int)((receivedKeyframeCount-1)%MAX_HISTORY);
		int i0 = (int)((receivedKeyframeCount-2)%MAX_HISTORY);
		DHKeyframe key1 = receivedKeyframes[i1];
		DHKeyframe key0 = receivedKeyframes[i0];

		double[][] jacobian = approximateJacobian(key0);
		double[] cartesian = {0,0,0,0,0,0};
		double [] jointVelocities = {0,0,0,0,0,0};
		
		// get the relative force
		final double SENSOR_RESOLUTION = 360.0/Math.pow(2,14); 
		final double dt=1.0/10.0; // sample time in firmware
		for( int i=0;i<key1.fkValues.length;++i ) {
			jointVelocities[i] = (key1.fkValues[i] - key0.fkValues[i])*dt;
			if(Math.abs(jointVelocities[i])<SENSOR_RESOLUTION) {
				jointVelocities[i]=0;
			}
		}
		
		for( int i=0;i<key1.fkValues.length;++i ) {
			for( int j=0;j<key1.fkValues.length;++j ) {
				cartesian[j] += jacobian[i][j] * jointVelocities[i];
			}
		}
		System.out.println(receivedKeyframeCount+":"+
		StringHelper.formatDouble(jointVelocities[0])+"\t"+
		StringHelper.formatDouble(jointVelocities[1])+"\t"+
		StringHelper.formatDouble(jointVelocities[2])+"\t"+
		StringHelper.formatDouble(jointVelocities[3])+"\t"+
		StringHelper.formatDouble(jointVelocities[4])+"\t"+
		StringHelper.formatDouble(jointVelocities[5])+"\t");
		//System.out.println(receivedKeyframeCount+":"+cartesian[0]+"\t"+cartesian[1]+"\t"+cartesian[2]);
		
		double len = Math.sqrt(
			cartesian[0]*cartesian[0]+
			cartesian[1]*cartesian[1]+
			cartesian[2]*cartesian[2]);
		if(len<1) return;
		//System.out.println(len);
				
		gl2.glPushMatrix();
		// endEffector is probably at key1 rn, so the force should be drawn backwards to show where it came from.
		MatrixHelper.applyMatrix(gl2, endEffector.poseWorld);

		boolean lightWasOn = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_LIGHTING);
		
		IntBuffer depthFunc = IntBuffer.allocate(1);
		gl2.glGetIntegerv(GL2.GL_DEPTH_FUNC, depthFunc);
		gl2.glDepthFunc(GL2.GL_ALWAYS);
		gl2.glLineWidth(4);
		
		double scale=1;
		
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3d(0, 0.6, 1);
		gl2.glVertex3d(0,0,0);
		gl2.glVertex3d(
				cartesian[0]*scale,
				cartesian[1]*scale,
				cartesian[2]*scale);
		
		gl2.glColor3d(1, 0, 0.6);
		gl2.glVertex3d(0,0,0);
		gl2.glVertex3d(
				cartesian[3]*scale,
				cartesian[4]*scale,
				cartesian[5]*scale);
		gl2.glEnd();
		
		gl2.glDepthFunc(depthFunc.get());
		if(lightWasOn) gl2.glEnable(GL2.GL_LIGHTING);
		
		gl2.glPopMatrix();
	}

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Sl", "Sixi Live");
		view.add(connection);
		for (int i = 0; i < PIDs.length; ++i) {
			view.add(PIDs[i]);
		}

		view.popStack();
		super.getView(view);
	}
}
