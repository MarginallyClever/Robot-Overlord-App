package com.marginallyclever.robotOverlord.entity.scene.robotEntity;

import com.marginallyclever.robotOverlord.entity.basicDataTypes.RemoteEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;


/**
 * A robot visible with a physical presence in the World.  Assumed to have an NetworkConnection to a machine in real life.  
 * @author Dan Royer
 *
 */
public abstract class RobotEntity extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2228444481181973067L;
	// comms	
	protected transient RemoteEntity connection = new RemoteEntity();
	protected transient boolean isReadyToReceive;
		
	public RobotEntity() {
		super();
		setName("Robot");
	}
	
	/**
	 * Processes a single instruction meant for the robot.
	 * @param line command to send
	 * @return true if the command is sent to the robot.
	 */
	public boolean sendCommand(String command) {
		if(connection==null) return false;

		// contains a comment?  if so remove it
		int index=command.indexOf('(');
		if(index!=-1) {
			//String comment=line.substring(index+1,line.lastIndexOf(')'));
			//Log("* "+comment+NL);
			command=command.substring(0,index).trim();
			if(command.length()==0) {
				// entire line was a comment.
				return false;  // still ready to send
			}
		}

		if(!command.endsWith("\n")) {
			command+="\n";
		}
		
		// send relevant part of line to the robot
		connection.sendMessage(command);
		
		return true;
	}

	/**
	 * Each robot implementation should customize the keframe as needed. 
	 * @return an instance derived from RobotKeyframe
	 */
	public abstract RobotKeyframe createKeyframe();
	
	@Override
	public void update(double dt) {
		super.update(dt);
		connection.update(dt);
		if(connection.isConnectionOpen()) {
			// set the lock
			
			// de-queue and process all messages
			//if(data.startsWith(">")) isReadyToReceive=true;
			
			// release the lock
		}
	}
	
	public boolean isReadyToReceive() {
		return isReadyToReceive;
	}
	
	@Override
	public void getView(ViewPanel view) {
		super.getView(view);
	}
}
