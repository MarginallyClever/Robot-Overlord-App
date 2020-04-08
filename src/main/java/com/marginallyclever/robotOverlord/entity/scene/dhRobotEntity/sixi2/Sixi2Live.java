package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import java.util.Observable;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.AnsiColors;
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
	protected RemoteEntity connection = new RemoteEntity();
	protected DHKeyframe receivedKeyframe;
	
	protected Vector3dEntity [] PIDs = new Vector3dEntity[6];
	
	public Sixi2Live() {
		super();
		setName("Live");
		addChild(connection);
		
		connection.addObserver(this);

		for(int i=0;i<PIDs.length;++i) {
			PIDs[i] = new Vector3dEntity("PID "+links.get(i).getLetter(),2,0.1,0.0001);
			addChild(PIDs[i]);
			PIDs[i].addObserver(this);
		}
		
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
		
		System.out.print(">>"+command);
		
		connection.sendMessage(command);
		
	    // wait for reply
	    readyForCommands=false;
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);

		for(int i=0;i<PIDs.length;++i) {
			if(o==PIDs[i]) {
				Vector3d newValue = PIDs[i].get();
				String message = "M306 L"+i+" P"+newValue.x+" I"+newValue.y+" D"+newValue.z;
				System.out.println("<<"+message);
				connection.sendMessage(message);
				return;
			}
		}
		
		if(o == connection) {
			String data = (String)arg;
	
			boolean unhandled=true;
			
			// all other data should probably update model
			if (data.startsWith("D17")) {
				if(data.endsWith("\n")) {
					// strip the return character, if any
					data = data.substring(0,data.length()-1);
				}
				//System.out.println("<<"+data);
				
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
						
						setPoseFK(receivedKeyframe);
						refreshPose();
					} catch(Exception e) {}
				}
				
				readyForCommands=true;
			}
	
			if(unhandled) {
				data=data.replace("\n", "");
				//System.out.println(AnsiColors.PURPLE+data+AnsiColors.RESET);
			}
		}
	}

	@Override
	public void setPoseWorld(Matrix4d m) {}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Sl", "Sixi Live");
		view.add(connection);
		for(int i=0;i<PIDs.length;++i) {
			view.add(PIDs[i]);
		}

		view.popStack();
		endEffector.getView(view);
	}
}
