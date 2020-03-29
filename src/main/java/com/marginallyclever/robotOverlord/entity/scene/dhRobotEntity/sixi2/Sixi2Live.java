package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import javax.vecmath.Matrix4d;

import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.communications.NetworkConnectionListener;
import com.marginallyclever.convenience.AnsiColors;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.RemoteEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHKeyframe;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

public class Sixi2Live extends Sixi2Model implements NetworkConnectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -811684331077697483L;
	protected RemoteEntity connection = new RemoteEntity();
	protected DHKeyframe receivedKeyframe;
	
	public Sixi2Live() {
		super();
		setName("Live");
		addChild(connection);

		connection.addObserver(this);
		
		// where to store incoming position data
		receivedKeyframe = getIKSolver().createDHKeyframe();		
	}

	@Override
	public void update(double dt) {
		// Sixi2Live does nothing on update?
		// Could compare jacobian estimated force with reported position to determine compliance.
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
		
		connection.sendMessage(command);
		
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
		}

		if(unhandled) {
			data=data.replace("\n", "");
			//System.out.println(AnsiColors.PURPLE+data+AnsiColors.RESET);
		}
	}

	@Override
	public void setPoseWorld(Matrix4d m) {}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Sl", "Sixi Live");
		view.add(connection);
		
		view.popStack();
		endEffector.getView(view);
	}
}
