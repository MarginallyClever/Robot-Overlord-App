package com.marginallyclever.robotoverlord.robots.skycam;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.parameters.RemoteParameter;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;

import javax.vecmath.Vector3d;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.LinkedList;

@Deprecated
public class SkycamLive {
	protected SkycamModel model;
	// roughly equivalent to SkycamSim.poseTo
	protected Vector3d poseSent;
	// roughly equivalent to SkycamSim.poseNow
	protected Vector3d poseReceived;
	// connection to robot outside this app. 
	protected RemoteParameter connection = new RemoteParameter();

	protected boolean waitingForOpenConnection;
	protected boolean readyForCommands;
	
	public SkycamLive(SkycamModel model) {
		super();
		
		this.model = model;
		
		//connection.addPropertyChangeListener(this);
		
		readyForCommands = false;
		waitingForOpenConnection = true;
	}

	@Deprecated
	public void getView(ComponentPanelFactory view) {
		view.add(connection);
	}

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
	}
}
