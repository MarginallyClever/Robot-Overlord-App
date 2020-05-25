package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import java.util.Observable;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.RemoteEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.Vector3dEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHKeyframe;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

public class Sixi2Live extends Sixi2Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = -811684331077697483L;

	public static final double SENSOR_RESOLUTION = 360.0/Math.pow(2,14); 
	public static final int MAX_HISTORY = 3;
	
	protected RemoteEntity connection = new RemoteEntity();
	protected DHKeyframe [] receivedKeyframes;
	protected long [] recievedKeyframeTimes;
	protected long receivedKeyframeCount;

	protected String lastCommand="";

	// perceived cartesian force acting on the arm based on recent joint velocities + jacobian math
	protected double[] cartesianForceDetected = {0,0,0,0,0,0};
	
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
		recievedKeyframeTimes = new long[receivedKeyframes.length];
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

	@Override
	public void sendCommand(String command) {
		if(lastCommand.contentEquals(command)) return;
		lastCommand = command;

		super.sendCommand(command);
		
		//sendCommandToRemoteEntity(command);
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
						int index = (int)(receivedKeyframeCount%MAX_HISTORY);
						DHKeyframe key0 = receivedKeyframes[index];
						key0.fkValues[0] = Double.parseDouble(tokens[1]);
						key0.fkValues[1] = Double.parseDouble(tokens[2]);
						key0.fkValues[2] = Double.parseDouble(tokens[3]);
						key0.fkValues[3] = Double.parseDouble(tokens[4]);
						key0.fkValues[4] = Double.parseDouble(tokens[5]);
						key0.fkValues[5] = Double.parseDouble(tokens[6]);
						recievedKeyframeTimes[index]=System.currentTimeMillis();
						setPoseFK(key0);
						refreshPose();

						if(receivedKeyframeCount>1) {
							int i1 = (int)((receivedKeyframeCount-1)%MAX_HISTORY);
						
							DHKeyframe key1 = receivedKeyframes[i1];
							double [] jointVelocity = new double[key1.fkValues.length];

							for( int i=0;i<cartesianForceDetected.length;++i ) cartesianForceDetected[i] = 0;
							
							// get the relative force
							long t1 = recievedKeyframeTimes[i1];  // ms
							long t0 = recievedKeyframeTimes[index];  // ms
							
							double dt = (t0-t1)*0.001;  // seconds
							for( int i=0;i<key1.fkValues.length;++i ) {
								jointVelocity[i] = (key0.fkValues[i]-key1.fkValues[i])*dt;
							}
							
							cartesianForceDetected = calculateCartesianForceFromJointVelocity(key0,jointVelocity);
						}
						
						receivedKeyframeCount++;
					} catch (NumberFormatException e) {
					}
					
					//
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
		double len = Math.sqrt(
			cartesianForceDetected[0]*cartesianForceDetected[0]+
			cartesianForceDetected[1]*cartesianForceDetected[1]+
			cartesianForceDetected[2]*cartesianForceDetected[2]);
		if(len<1) return;
		//System.out.println(len);

		int previousState = OpenGLHelper.drawAtopEverythingStart(gl2);
		boolean lightWasOn = OpenGLHelper.disableLightingStart(gl2);
		gl2.glLineWidth(4);
		
		double scale=1;

		gl2.glPushMatrix();
		// endEffector is probably at key1 rn, so the force should be drawn backwards to show where it came from.
			Matrix4d m4 = endEffector.getPoseWorld();
			gl2.glTranslated(m4.m03, m4.m13, m4.m23);

			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3d(0, 0.6, 1);
			gl2.glVertex3d(0,0,0);
			gl2.glVertex3d(
					cartesianForceDetected[0]*scale,
					cartesianForceDetected[1]*scale,
					cartesianForceDetected[2]*scale);
			
			gl2.glColor3d(1.0, 0.5, 0.5);	PrimitiveSolids.drawCircleYZ(gl2, cartesianForceDetected[3]*scale, 20);
			gl2.glColor3d(0.5, 1.0, 0.5);	PrimitiveSolids.drawCircleXZ(gl2, cartesianForceDetected[4]*scale, 20);
			gl2.glColor3d(0.5, 0.5, 1.0);	PrimitiveSolids.drawCircleXY(gl2, cartesianForceDetected[5]*scale, 20);
		
		gl2.glPopMatrix();

		gl2.glLineWidth(1);
		OpenGLHelper.disableLightingEnd(gl2, lightWasOn);
		OpenGLHelper.drawAtopEverythingEnd(gl2, previousState);
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
