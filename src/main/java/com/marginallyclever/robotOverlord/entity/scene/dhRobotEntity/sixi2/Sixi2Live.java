package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import java.util.Observable;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.RemoteEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.Vector3dEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHKeyframe;
import com.marginallyclever.robotOverlord.log.Log;
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
	protected boolean remoteIsReadyForCommands;

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
		if (!connection.isConnectionOpen()) {
			waitingForOpenConnection = true;
		}
		
		// START this is equivalent to super.super.update(dt)
		for(Entity e : children ) {
			e.update(dt);
		}
		// END
	}
	
	/**
	 * Find the cartesian (linear) force to move from current position to target.
	 * Use Jacobians to convert cartesian to joint angle velocities. 
	 * @param dt
	 */
	protected void generateAndSendCommand(double dt) {
		// get interpolated future pose
    	double tTotal = timeTarget - timeStart;
		timeNow += dt;
	    double t = timeNow-timeStart;
	    
		double ratioFuture = (t+dt) / tTotal;
		if(ratioFuture>1) ratioFuture=1;
		
		Matrix4d interpolatedMatrixNow = new Matrix4d(endEffector.getPoseWorld());
		//MatrixHelper.interpolate(mFrom,mTarget, ratioNow   , interpolatedMatrixNow);
		//Matrix4d interpolatedMatrixFuture = new Matrix4d();
		//MatrixHelper.interpolate(mFrom,mTarget, ratioFuture, interpolatedMatrixFuture);
		Matrix4d interpolatedMatrixFuture = mTarget;

		// get cartesian force
		double[] cfdn = {0,0,0,0,0,0};
		getCartesianForceBetweenTwoPoses(interpolatedMatrixNow, interpolatedMatrixFuture, dt, cartesianForceDesired);

		// cap cartesian force
		double nx = cfdn[0];
		double ny = cfdn[1];
		double nz = cfdn[2];
		double ox = cartesianForceDesired[0];
		double oy = cartesianForceDesired[1];
		double oz = cartesianForceDesired[2];
		
		double MAX_LINEAR_VELOCITY    =100;  // must be >0
		double MAX_LINEAR_ACCELERATION= 50;  // must be >0
		
		double newLinearVel = Math.sqrt(nx*nx + ny*ny + nz*nz);
		newLinearVel = Math.min(newLinearVel , MAX_LINEAR_VELOCITY);
		double oldLinearVel = Math.sqrt(ox*ox + oy*oy + oz*oz);
		double dv = newLinearVel-oldLinearVel;
		double ratio = 1;
		if(Math.abs(dv)>MAX_LINEAR_ACCELERATION) {
			ratio = MAX_LINEAR_ACCELERATION / Math.abs(dv);
			nx = ox*ratio;
			ny = oy*ratio;
			nz = oz*ratio;
		}
		double nv = dv * ratio;
		//Log.message("dv="+dv+" nv="+nv);
		cfdn[0]=nx;
		cfdn[1]=ny;
		cfdn[2]=nz;
				
		for(int i=0;i<cfdn.length;++i) {
			cartesianForceDesired[i]=cfdn[i];
		}
				
		DHKeyframe keyframe = getIKSolver().createDHKeyframe();
		getPoseFK(keyframe);
		
		if(!getJointVelocityFromCartesianForce(keyframe,cartesianForceDesired,jointVelocityDesired)) return;
		
		//TODO Nm max force / moi = max velocity.  don't forget gearbox efficiency!
		//capJointVelocity(jointVelocityDesired);

		// we have poseFKTarget and jointVelocityDesired.
		
	    // message format is G0 Xnn Ynn Znn Unn Vnn Wnn Fnn Ann Nnn Pnn Qnn Rnn Snn Tnn*chk
	    // chk is checksum of the entire string
		String cmd1 = "G0";
		cmd1 += " X"+StringHelper.formatDouble(poseFKTarget[0]);
		cmd1 += " Y"+StringHelper.formatDouble(poseFKTarget[1]);
		cmd1 += " Z"+StringHelper.formatDouble(poseFKTarget[2]);
		cmd1 += " U"+StringHelper.formatDouble(poseFKTarget[3]);
		cmd1 += " V"+StringHelper.formatDouble(poseFKTarget[4]);
		cmd1 += " W"+StringHelper.formatDouble(poseFKTarget[5]);
		
		//cmd1 += " F"+StringHelper.formatDouble(getFeedrate());
		//cmd1 += " A"+StringHelper.formatDouble(getAcceleration());

		String cmd2 = "G0";
		cmd2 += " K"+StringHelper.formatDouble(jointVelocityDesired[0]);
		cmd2 += " P"+StringHelper.formatDouble(jointVelocityDesired[1]);
		cmd2 += " Q"+StringHelper.formatDouble(jointVelocityDesired[2]);
		cmd2 += " R"+StringHelper.formatDouble(jointVelocityDesired[3]);
		cmd2 += " S"+StringHelper.formatDouble(jointVelocityDesired[4]);
		cmd2 += " T"+StringHelper.formatDouble(jointVelocityDesired[5]);
		
		//System.out.println(cmd);
		sendCommandToRemoteEntity(cmd1,false);
		//sendCommandToRemoteEntity(cmd2,false);

		//Log.message("Live SEND " + cmd1.trim());
		//Log.message("Live SEND " + cmd2.trim());
	}

	@Override
	public void sendCommand(String command) {
		//if(lastCommand.contentEquals(command)) return;
		//lastCommand = command;

		super.sendCommand(command);
		
		//sendCommandToRemoteEntity(command,true);
	}
	
	protected String cleanUpMessage(String command) {
		if(!command.isEmpty()) {
			// remove any comment
			int index = command.indexOf('(');
			if (index != -1) {
				command = command.substring(0, index);
			}
			// remove any end-of-line characters or whitespace.
			command.trim();
		}
		return command;
	}

	protected void sendCommandToRemoteEntity(String command,boolean guaranteed) {
		if(command == null) return;
		
		command = cleanUpMessage(command);
		
		// DO IT
		if(guaranteed) {
			connection.sendMessageGuaranteed(command);
		} else {
			connection.sendMessage(command);
		}
		// while we wait for reply don't flood the robot with too much data.
		readyForCommands = true;
		remoteIsReadyForCommands = false;
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);

		for (int i = 0; i < PIDs.length; ++i) {
			if (o == PIDs[i]) {
				Vector3d newValue = PIDs[i].get();
				String message = "M306 L" + i + " P" + newValue.x + " I" + newValue.y + " D" + newValue.z;
				//sendCommandToRemoteEntity(message,true);
				return;
			}
		}

		if (o == connection) {
			String data = (String) arg;

			boolean unhandled = true;

			// if(!confirmChecksumOK(data)) return;

			if (data.startsWith("> ")) {
				// can only be ready if also done waiting for open connection.
				remoteIsReadyForCommands =!waitingForOpenConnection;
				//if(remoteIsReadyForCommands) Log.message("SIX READY");
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
							
							cartesianForceDetected = getCartesianForceFromJointVelocity(key0,jointVelocity);
						}
						
						receivedKeyframeCount++;
					} catch (NumberFormatException e) {
					}
					
					generateAndSendCommand(1.0/30.0);
				}
			}

			if (unhandled) {
				data = data.replace("\n", "");
				//Log.message("Unhandled: "+data);
			} else {
				// wait until we received something meaningful before we start blasting our data
				// out.
				if (waitingForOpenConnection) {
					waitingForOpenConnection = false;
					sendCommandToRemoteEntity("D50 S1",true);
					// send once
					for (int i = 0; i < PIDs.length; ++i) {
						Vector3d newValue = PIDs[i].get();
						String message = "M306 L" + i + " P" + newValue.x + " I" + newValue.y + " D" + newValue.z;
						//sendCommandToRemoteEntity(message,true);
					}
					remoteIsReadyForCommands = false;

					receivedKeyframeCount=0;
				}
			}
		}
	}

	@Override
	public void setPoseWorld(Matrix4d m) {}
	
	// See https://studywolf.wordpress.com/2013/09/02/robot-control-jacobians-velocity-and-force/
	public void renderCartesianForce(GL2 gl2) {
		int previousState = OpenGLHelper.drawAtopEverythingStart(gl2);
		boolean lightWasOn = OpenGLHelper.disableLightingStart(gl2);
		gl2.glLineWidth(4);
		
		double scale=1;

		gl2.glPushMatrix();
			Matrix4d m4 = endEffector.getPoseWorld();
			gl2.glTranslated(m4.m03, m4.m13, m4.m23);

			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3d(0, 0.6, 1);
			gl2.glVertex3d(0,0,0);
			gl2.glVertex3d(
					cartesianForceDetected[0]*scale,
					cartesianForceDetected[1]*scale,
					cartesianForceDetected[2]*scale);
			gl2.glEnd();
			
			PrimitiveSolids.drawCircleYZ(gl2, cartesianForceDetected[3]*scale, 20);
			PrimitiveSolids.drawCircleXZ(gl2, cartesianForceDetected[4]*scale, 20);
			PrimitiveSolids.drawCircleXY(gl2, cartesianForceDetected[5]*scale, 20);

			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3d(1, 0, 0.6);
			gl2.glVertex3d(0,0,0);
			gl2.glVertex3d(
					cartesianForceDesired[0]*scale,
					cartesianForceDesired[1]*scale,
					cartesianForceDesired[2]*scale);
			gl2.glEnd();
			
			//PrimitiveSolids.drawCircleYZ(gl2, cartesianForceDesired[3]*scale, 20);
			//PrimitiveSolids.drawCircleXZ(gl2, cartesianForceDesired[4]*scale, 20);
			//PrimitiveSolids.drawCircleXY(gl2, cartesianForceDesired[5]*scale, 20);

			
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
