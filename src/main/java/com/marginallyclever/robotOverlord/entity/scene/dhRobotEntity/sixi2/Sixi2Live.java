package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import java.util.Observable;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

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
	protected RemoteEntity connection = new RemoteEntity();
	protected DHKeyframe receivedKeyframe;

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
		receivedKeyframe = getIKSolver().createDHKeyframe();
		waitingForOpenConnection = true;
	}

	@Override
	public void update(double dt) {
		// TODO Compare jacobian estimated force with reported position to determine
		// compliance.
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
		Log.message("SIX SEND " + msg.trim());
	}

	public void reportDataReceived(String msg) {
		if (msg.trim().isEmpty())
			return;
		Log.message("SIX RECV " + msg.trim());
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
					Log.message("SIX READY");
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
						receivedKeyframe.fkValues[0] = Double.parseDouble(tokens[1]);
						receivedKeyframe.fkValues[1] = Double.parseDouble(tokens[2]);
						receivedKeyframe.fkValues[2] = Double.parseDouble(tokens[3]);
						receivedKeyframe.fkValues[3] = Double.parseDouble(tokens[4]);
						receivedKeyframe.fkValues[4] = Double.parseDouble(tokens[5]);
						receivedKeyframe.fkValues[5] = Double.parseDouble(tokens[6]);
						setPoseFK(receivedKeyframe);
						refreshPose();
					} catch (Exception e) {
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
				}
			}
		}
	}

	@Override
	public void setPoseWorld(Matrix4d m) {
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
