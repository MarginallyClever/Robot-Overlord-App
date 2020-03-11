package com.marginallyclever.robotOverlord.entity.robot.sixi2;

import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.communications.NetworkConnectionListener;
import com.marginallyclever.communications.NetworkConnectionManager;
import com.marginallyclever.convenience.AnsiColors;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHKeyframe;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHLink;

public class Sixi2Live extends Sixi2Model implements NetworkConnectionListener {
	protected transient NetworkConnection connection;
	protected DHKeyframe receivedKeyframe;
	
	public Sixi2Live() {
		super();
		setName("Live");

	    // set yellow
	    for( DHLink link : links ) {
	    	link.getMaterial().setDiffuseColor(1,217f/255f,33f/255f,1);
	    }
		
		// where to store incoming position data
		receivedKeyframe = getIKSolver().createDHKeyframe();		
	}
	
	public void closeConnection() {
		connection.closeConnection();
		connection.removeListener(this);
		connection=null;
	}
	
	public void openConnection() {
		connection = NetworkConnectionManager.requestNewConnection(null);
		if(connection!=null) {
			connection.addListener(this);
			sendCommand("D20");
		}
	}

	@Override
	public void update(double dt) {
		// we don't do anything, we just report on what the live robot says.
		if(connection!=null) {
			connection.update();
		}
	}
	
	@Override
	public void sendCommand(String command) {
		if(command==null) return;

		// contains a comment?  if so remove it
		int index=command.indexOf('(');
		if(index!=-1) {
			//String comment=line.substring(index+1,line.lastIndexOf(')'));
			//Log("* "+comment+NL);
			command=command.substring(0,index).trim();
			if(command.length()==0) {
				// entire line was a comment.
				return;  // still ready to send
			}
		}

		if(!command.endsWith("\n")) {
			command+="\n";
		}
		
		System.out.print(">>>> "+command);
		
		try {
			connection.sendMessage(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	    // wait for reply
	    readyForCommands=false;
	}
	
	@Override
	public void lineError(NetworkConnection arg0, int lineNumber) {
		readyForCommands=false;
	}

	@Override
	public void sendBufferEmpty(NetworkConnection arg0) {
	    // just because the buffer is empty does not mean the robot is ready to receive. 
	}

	@SuppressWarnings("unused")
	@Override
	public void dataAvailable(NetworkConnection arg0, String data) {
		if(data.startsWith(">")) {
			data=data.substring(1).trim();
			readyForCommands=true;
		}

		boolean unhandled=true;
		
		// all other data should probably update model
		if (data.startsWith("D17")) {
			unhandled=false;
			
			String[] tokens = data.split("\\s+");
			if(tokens.length>=7) {
				try {
					receivedKeyframe.fkValues[0]=Double.parseDouble(tokens[1]);
					receivedKeyframe.fkValues[1]=Double.parseDouble(tokens[2]);
					receivedKeyframe.fkValues[2]=Double.parseDouble(tokens[3]);
					receivedKeyframe.fkValues[3]=Double.parseDouble(tokens[4]);
					receivedKeyframe.fkValues[4]=Double.parseDouble(tokens[5]);
					receivedKeyframe.fkValues[5]=Double.parseDouble(tokens[6]);

					if(false) {
						String message = "D17 "
					    		+" X"+(StringHelper.formatDouble(receivedKeyframe.fkValues[0]))
					    		+" Y"+(StringHelper.formatDouble(receivedKeyframe.fkValues[1]))
					    		+" Z"+(StringHelper.formatDouble(receivedKeyframe.fkValues[2]))
					    		+" U"+(StringHelper.formatDouble(receivedKeyframe.fkValues[3]))
					    		+" V"+(StringHelper.formatDouble(receivedKeyframe.fkValues[4]))
					    		+" W"+(StringHelper.formatDouble(receivedKeyframe.fkValues[5]));
						System.out.println(AnsiColors.BLUE+message+AnsiColors.RESET);
					}
					//data = data.replace('\n', ' ');

					//smoothing to new position
					//DHKeyframe inter = solver.createDHKeyframe();
					//inter.interpolate(poseNow,receivedKeyframe, 0.5);
					//this.setRobotPose(inter);

					setPoseFK(receivedKeyframe);

				} catch(Exception e) {}
			}

			refreshPose();
			if (dhRobotPanel != null && !isDisablePanel()) {
				dhRobotPanel.updateEnd();
			}
		}

		if(unhandled) {
			data=data.replace("\n", "");
			//System.out.println(AnsiColors.PURPLE+data+AnsiColors.RESET);
		}
	}
/*
	// pull the last connected port from prefs
	private String loadRecentPortFromPreferences() {
		return prefs.get("recent-port", "");
	}

	// update the prefs with the last port connected and refreshes the menus.
	public void setRecentPort(String portName) {
		prefs.put("recent-port", portName);
	}
*/
}
