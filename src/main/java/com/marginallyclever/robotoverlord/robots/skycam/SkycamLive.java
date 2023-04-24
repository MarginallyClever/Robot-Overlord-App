package com.marginallyclever.robotoverlord.robots.skycam;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.parameters.RemoteParameter;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

import javax.vecmath.Vector3d;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.LinkedList;

@Deprecated
public class SkycamLive extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6543522320148593138L;
	protected SkycamModel model;
	// roughly equivalent to SkycamSim.poseTo
	protected Vector3d poseSent;
	// roughly equivalent to SkycamSim.poseNow
	protected Vector3d poseReceived;
	// connection to robot outside this app. 
	protected RemoteParameter connection = new RemoteParameter();

	protected transient LinkedList<PoseAtTime<Vector3d>> received = new LinkedList<PoseAtTime<Vector3d>>();  
	public static final int RECEIVED_BUFFER_LEN = 3;

	protected boolean waitingForOpenConnection;
	protected boolean readyForCommands;

	protected double[] cartesianForceMeasured = {0,0,0,0,0,0,0};
	protected double[] jointVelocityMeasured = {0,0,0,0,0,0,0};
	
	
	public SkycamLive(SkycamModel model) {
		super("Skycam Live");
		
		this.model = model;
		
		connection.addPropertyChangeListener(this);
		
		readyForCommands = false;
		waitingForOpenConnection = true;
	}

	@Override
	public void getView(ViewPanel view) {
		view.startNewSubPanel("Live",true);
		view.add(connection);
		super.getView(view);
	}
	
	@Override
	public void update(double dt) {
		super.update(dt);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		Object o = evt.getSource();
		if(o == connection) {
			readConnectionData((String)o);
		}
	}
	
	protected void readConnectionData(String data) {
		boolean unhandled = true;

		// if(!confirmChecksumOK(data)) return;
		
		if (waitingForOpenConnection) {
			// the moment connection is opened for the first time.
			waitingForOpenConnection = false;
			// turn on STRICT mode where checksums and line numbers must ALWAYS be provided.
			connection.sendMessageGuaranteed("D50 S1");
		}

		if (data.startsWith("> ")) {
			// can only be ready if also done waiting for open connection.
			readyForCommands = !waitingForOpenConnection;
			//if(readyForCommands) logger.info("SIXI READY");
			data = data.substring(2);
		}

		// all other data should probably update model
		if (data.startsWith("D17")) {
			unhandled = false;
			data = data.trim();

			String[] tokens = data.split("\\s+");
			if (tokens.length >= 7) {
				try {
					Vector3d key0 = new Vector3d();
					key0.x = StringHelper.parseNumber(tokens[1]);
					key0.y = StringHelper.parseNumber(tokens[2]);
					key0.z = StringHelper.parseNumber(tokens[3]);
					PoseAtTime<Vector3d> pat = new PoseAtTime<Vector3d>(key0,System.currentTimeMillis());
					received.add(pat);
					setPoseReceived(key0);
					if(received.size() > RECEIVED_BUFFER_LEN) {
						received.pop();
					}
					// TODO use received buffer to estimate forces on end effector?
				} catch (NumberFormatException e) {
				}
			}
		}

		if (unhandled) {
			data = data.replace("\n", "");
			//logger.info("Unhandled: "+data);
		}
	}
	
	@Override
	public void render(GL2 gl2) {
		// draw poseReceived first so it takes precedence in the z buffers
		if(poseReceived!=null) {
			model.setPosition(poseReceived);
			model.setDiffuseColor(1, 0, 0, 1);
			model.render(gl2);
		}
		if(poseSent!=null) {
			model.setPosition(poseSent);
			model.setDiffuseColor(1, 0, 0, 0.25f);
			model.render(gl2);
		}		
		super.render(gl2);
	}

	public Vector3d getPoseSent() {
		return poseSent;
	}

	private void setPoseSent(Vector3d newPoseSent) {
		this.poseSent=newPoseSent;
	}

	public Vector3d getPoseReceived() {
		return poseReceived;
	}

	protected void setPoseReceived(Vector3d poseReceived) {
		this.poseReceived = (poseReceived==null) ? null : (Vector3d)poseReceived.clone();
	}
	
	public boolean isReadyForCommands() {
		return readyForCommands;
	}
	
	public boolean addDestination(final SkycamCommand command) {
		if(!isConnected() || !readyForCommands) return false;
		
		//connection.sendMessageGuaranteed(command.getFAAsString());
		connection.sendMessageGuaranteed(command.poseFKToString());
		setPoseSent(command.getPosition());
		readyForCommands = false;
		//logger.info("Sent "+command.poseFKToString());
		return true;
	}

	public void eStop() {
		connection.sendMessageGuaranteed("M112");
	}
	
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		received = new LinkedList<PoseAtTime<Vector3d>>();  
	}

	public boolean isConnected() {
		return connection.isConnectionOpen();
	}
}
