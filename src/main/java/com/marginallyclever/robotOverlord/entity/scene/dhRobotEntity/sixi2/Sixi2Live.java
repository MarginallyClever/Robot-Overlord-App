package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import java.util.LinkedList;
import java.util.Observable;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.RemoteEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotModel;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.PoseFK;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * Everything known about the state of the live robot
 * @author Dan Royer
 *
 */
public class Sixi2Live extends Entity {
	
	protected DHRobotModel model;
	// roughly equivalent to Sixi2Sim.poseTo
	protected PoseFK poseSent;
	// roughly equivalent to Sixi2Sim.poseNow
	protected PoseFK poseReceived;
	// connection to robot outside this app. 
	protected RemoteEntity connection = new RemoteEntity();

	protected LinkedList<PoseAtTime> received = new LinkedList<PoseAtTime>();  
	public static final int RECEIVED_BUFFER_LEN = 3;

	protected boolean waitingForOpenConnection;
	protected boolean remoteIsReadyForCommands;

	protected double[] cartesianForceMeasured = {0,0,0,0,0,0,0};
	protected double[] jointVelocityMeasured = {0,0,0,0,0,0,0};
	
	
	public Sixi2Live(DHRobotModel model) {
		super("Sixi2 Live");
		
		this.model = model;
		
		connection.addObserver(this);
		
		waitingForOpenConnection = true;
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("L", "Live");
		view.add(connection);
		view.popStack();
		super.getView(view);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if(o == connection) {
			readConnectionData((String)arg);
		}
		super.update(o, arg);
	}
	
	@Override
	public void update(double dt) {
		super.update(dt);
	}
	
	protected void readConnectionData(String data) {
		boolean unhandled = true;

		// if(!confirmChecksumOK(data)) return;

		if (data.startsWith("> ")) {
			// can only be ready if also done waiting for open connection.
			remoteIsReadyForCommands = !waitingForOpenConnection;
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
					PoseFK key0 = model.createPoseFK();
					key0.fkValues[0] = StringHelper.parseNumber(tokens[1]);
					key0.fkValues[1] = StringHelper.parseNumber(tokens[2]);
					key0.fkValues[2] = StringHelper.parseNumber(tokens[3]);
					key0.fkValues[3] = StringHelper.parseNumber(tokens[4]);
					key0.fkValues[4] = StringHelper.parseNumber(tokens[5]);
					key0.fkValues[5] = StringHelper.parseNumber(tokens[6]);
					PoseAtTime pat = new PoseAtTime();
					pat.p=key0;
					pat.t=System.currentTimeMillis();
					received.add(pat);
					setPoseReceived(key0);
					if(received.size() > RECEIVED_BUFFER_LEN) {
						received.pop();
					}

					int s = received.size();
					if(s>1) {
						int i1 = (int)((s-2) % RECEIVED_BUFFER_LEN);
					
						PoseFK key1 = received.get(i1).p;

						for( int i=0;i<cartesianForceMeasured.length;++i ) cartesianForceMeasured[i] = 0;
						
						// get the relative force
						long t1 = received.get(i1).t;  // ms
						long t0 = pat.t;  // ms
						
						double dt = (t0-t1)*0.001;  // seconds
						for( int i=0;i<key1.fkValues.length;++i ) {
							jointVelocityMeasured[i] = (key0.fkValues[i]-key1.fkValues[i])*dt;
						}
						
						cartesianForceMeasured = JacobianHelper.getCartesianForceFromJointVelocity(model,jointVelocityMeasured);
					}
					
				} catch (NumberFormatException e) {
				}
				
				//generateAndSendCommand(1.0/30.0);
			}
		}

		if (unhandled) {
			data = data.replace("\n", "");
			//Log.message("Unhandled: "+data);
		} else {/*
			// wait until we received something meaningful before we start blasting our data
			// out.
			if (waitingForOpenConnection) {
				waitingForOpenConnection = false;
				sendCommandToRemoteEntity("D50 S1",true);
				// send once
				for (int i = 0; i < PIDs.length; ++i) {
					Vector3d newValue = PIDs[i].get();
					String message = "M306 L" + i + " P" + newValue.x + " I" + newValue.y + " D" + newValue.z;
					sendCommandToRemoteEntity(message,true);
				}
				remoteIsReadyForCommands = false;

				receivedKeyframeCount=0;
			}*/
		}
	}
	
	@Override
	public void render(GL2 gl2) {
		// draw now first so it takes precedence in the z buffers
		if(poseReceived!=null) {
			model.setPoseFK(poseReceived);
			model.setDiffuseColor(1, 0, 0, 1);
			model.render(gl2);
		}
		if(poseSent!=null) {
			model.setPoseFK(poseSent);
			model.setDiffuseColor(1, 0, 0, 0.25f);
			model.render(gl2);
		}		
		super.render(gl2);
	}

	public PoseFK getPoseSent() {
		return poseSent;
	}

	@SuppressWarnings("unused")
	private void setPoseSent(PoseFK poseSent) {
		try {
			this.poseSent = (PoseFK)poseSent.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public PoseFK getPoseReceived() {
		return poseReceived;
	}

	protected void setPoseReceived(PoseFK poseReceived) {
		try {
			this.poseReceived = (PoseFK)poseReceived.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void AddDestination(PoseFK poseTo, double feedrate, double acceleration) {
		if(connection.isConnectionOpen()) {
			connection.sendMessageGuaranteed("G0"
					+" X"+StringHelper.formatDouble(poseTo.fkValues[0])
					+" Y"+StringHelper.formatDouble(poseTo.fkValues[1])
					+" Z"+StringHelper.formatDouble(poseTo.fkValues[2])
					+" U"+StringHelper.formatDouble(poseTo.fkValues[3])
					+" V"+StringHelper.formatDouble(poseTo.fkValues[4])
					+" W"+StringHelper.formatDouble(poseTo.fkValues[5])
					+" F"+StringHelper.formatDouble(feedrate)
					+" A"+StringHelper.formatDouble(acceleration)
					);
			setPoseSent(poseTo);
		}
	}
}
