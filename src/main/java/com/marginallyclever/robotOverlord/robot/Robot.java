package com.marginallyclever.robotOverlord.robot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JPanel;

import com.marginallyclever.communications.NetworkConnectionManager;
import com.jogamp.opengl.GL2;
import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.communications.NetworkConnectionListener;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.physicalObject.PhysicalObject;


/**
 * A robot visible with a physical presence in the World.  Assumed to have an NetworkConnection to a machine in real life.  
 * @author Dan Royer
 *
 */
public abstract class Robot extends PhysicalObject implements NetworkConnectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1970631551615654640L;
	
	//comms	
	protected transient String[] portsDetected=null;
	protected transient NetworkConnection connection;
	protected transient boolean isReadyToReceive;

	// animation settings
	protected transient LinkedList<RobotKeyframe> keyframes = new LinkedList<RobotKeyframe>();
	protected int keyframe_index;
	protected float keyframe_t;
	
	public enum AnimationBehavior {
		ANIMATE_ONCE,
		ANIMATE_LOOP,
	};
	public AnimationBehavior animationBehavior;
	public float animationSpeed=1.0f;

	// sending file to the robot
	private boolean running;
	private boolean paused;
    private long linesTotal;
	private long linesProcessed;
	private boolean fileOpened;
	private ArrayList<String> gcode;


	
	protected transient boolean isModelLoaded;
	
	protected transient RobotControlPanel robotPanel=null;
	
	
	public Robot() {
		super();
		isReadyToReceive=false;
		linesTotal=0;
		linesProcessed=0;
		fileOpened=false;
		paused=true;
		running=false;
		isModelLoaded=false;
		animationBehavior=AnimationBehavior.ANIMATE_LOOP;
	}
	

	public boolean isRunning() { return running; }
	public boolean isPaused() { return paused; }
	public boolean isFileOpen() { return fileOpened; }
	
	
	@Override
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		if(robotPanel == null) robotPanel = new RobotControlPanel(gui,this);
		list.add(robotPanel);
		
		return list;
	}
	
	protected void closeConnection() {
		connection.closeConnection();
		connection.removeListener(this);
		connection=null;
	}
	
	protected void openConnection() {
		NetworkConnection s = NetworkConnectionManager.requestNewConnection(null);
		if(s!=null) {
			setConnection(s);
		}
	}
	
	public NetworkConnection getConnection() {
		return this.connection;
	}
	
	public void setConnection(NetworkConnection arg0) {
		if(connection!=null && connection!=arg0) {
			closeConnection();
		}
		
		connection = arg0;
		
		if( connection != null ) {
			connection.addListener(this);
		}
	}

	
	@Override
	public void dataAvailable(NetworkConnection arg0,String data) {
		if(arg0==connection && connection!=null) {
			if(data.startsWith(">")) {
				isReadyToReceive=true;
			}
		}
		
		if(isReadyToReceive) {
			sendFileCommand();
		}
		System.out.println(data);
	}
	
	/**
	 * tell the robot to move within it's work envelope relative to the robot's current position in the envelope.
	 * @param axis the index of the axis on which to move
	 * @param direction which direction along the axis
	 */
	public void move(int axis,int direction) {

		isReadyToReceive=false;
	}
	
	/**
	 * Take the next line from the file and send it to the robot, if permitted. 
	 */
	public void sendFileCommand() {
		if(!running || paused || !fileOpened || linesProcessed>=linesTotal) return;
		
		String line;
		do {			
			// are there any more commands?
			line=gcode.get((int)linesProcessed++).trim();
			//previewPane.setLinesProcessed(linesProcessed);
			//statusBar.SetProgress(linesProcessed, linesTotal);
			// loop until we find a line that gets sent to the robot, at which point we'll
			// pause for the robot to respond.  Also stop at end of file.
		} while(!sendLineToRobot(line) && linesProcessed<linesTotal);

		isReadyToReceive=false;
		
		if(linesProcessed==linesTotal) {
			// end of file
			halt();
		}
	}

	
	/**
	 * Stop sending commands to the robot.
	 */
	public void halt() {
		// TODO add an e-stop command?
		running=false;
		paused=false;
	    linesProcessed=0;
	}

	public void start() {
		paused=false;
		running=true;
		sendFileCommand();
	}
	
	public void startAt(int lineNumber) {
		if(fileOpened && !running) {
			linesProcessed=lineNumber;
			start();
		}
	}
	
	public void pause() {
		if(running) {
			if(paused) {
				paused=false;
				// TODO: if the robot is not ready to unpause, this might fail and the program would appear to hang.
				sendFileCommand();
			} else {
				paused=true;
			}
		}
	}
	
	// Must be called by subclass to loadModels on render.
	public void render(GL2 gl2) {
		if(!isModelLoaded) {
			loadModels(gl2);
			isModelLoaded=true;
		}
		
		renderKeyframes(gl2);
	}

	/**
	 * Draw each of the individual keyframes and any {@renderInterpolation} that might exist between them.
	 * @param gl2 the render context
	 */
	protected void renderKeyframes(GL2 gl2) {
		Iterator<RobotKeyframe> i = keyframes.iterator();
		RobotKeyframe current;
		RobotKeyframe next=null;
		while(i.hasNext()) {
			current = next;
			next = i.next();
			if(current != null && next!=null) {
				current.render(gl2);
				// TODO this shouldn't be the base type
				current.renderInterpolation(gl2, next);
			}
		}
		if(next!=null) {
			next.render(gl2);
		}
	}
	
	// stub to be overridden by subclasses.
	protected void loadModels(GL2 gl2) {}
	
	/**
	 * Pr)ocesses a single instruction meant for the robot.
	 * @param line command to send
	 * @return true if the command is sent to the robot.
	 */
	public boolean sendLineToRobot(String line) {
		if(connection==null) return false;

		// contains a comment?  if so remove it
		int index=line.indexOf('(');
		if(index!=-1) {
			//String comment=line.substring(index+1,line.lastIndexOf(')'));
			//Log("* "+comment+NL);
			line=line.substring(0,index).trim();
			if(line.length()==0) {
				// entire line was a comment.
				return false;  // still ready to send
			}
		}

		// send relevant part of line to the robot
		try{
			connection.sendMessage(line);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}

	@Override
	public void lineError(NetworkConnection arg0, int lineNumber) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendBufferEmpty(NetworkConnection arg0) {
		// TODO Auto-generated method stub
		
	}
/*
	// pull the last connected port from prefs
	private void loadRecentPortFromPreferences() {
		recentPort = prefs.get("recent-port", "");
	}

	// update the prefs with the last port connected and refreshes the menus.
	public void setRecentPort(String portName) {
		prefs.put("recent-port", portName);
		recentPort = portName;
		//UpdateMenuBar();
	}
*/

	public abstract RobotKeyframe createKeyframe();
	
	public void keyframeAdd() {
		keyframes.add(keyframe_index, createKeyframe());
	}
	
	public void keyframeDelete() {
		keyframes.remove(keyframe_index);
		if(keyframe_index>keyframes.size()) keyframe_index = keyframes.size()-1;
	}
	
	public float getKeyframeT() {
		return keyframe_t;
	}
	public void setKeyframeT(float arg0) {
		keyframe_t=Math.min(Math.max(arg0, 0),1);
	}
	
	public int getKeyframeSize() {
		return keyframes.size();
	}
	public int getKeyframeIndex() {
		return keyframe_index;
	}
	public void setKeyframeIndex(int arg0) {
		keyframe_index=Math.min(Math.max(arg0, 0),keyframes.size()-1);
	}
	
	public RobotKeyframe getKeyframeNow() {
		int size=getKeyframeSize();
		if(keyframe_index>=size-1) {
			// last keyframe, nowhere to interpolate towards.
			return keyframes.getLast();
		} else {
			RobotKeyframe now = createKeyframe();
			RobotKeyframe a = keyframes.get(keyframe_index);
			RobotKeyframe b = keyframes.get(keyframe_index+1);
			now.interpolate(a, b, keyframe_t);
			return now;
		}
	}
	
	public RobotKeyframe getKeyframe(int arg0) {
		return keyframes.get(arg0);
	}
	
	public void setKeyframe(int index,RobotKeyframe element) {
		keyframes.set(index, element);
	}
	
	@Override
	public void prepareMove(float dt) {
		animate(dt);
	}
	
	protected void animate(float dt) {
		keyframe_t+=dt*animationSpeed;
		if(animationSpeed>0) {
			if(keyframe_t>1) {
				keyframe_t-=1;
				++keyframe_index;
				int size=getKeyframeSize();
				if(keyframe_index>size-1) {
					switch(animationBehavior) {
					case ANIMATE_ONCE:
						keyframe_index = size-1;
						keyframe_t=0;
						// TODO set the panel button to paused
						break;
					case ANIMATE_LOOP:
						keyframe_index=0;
						break;
					}
				}
			}
		} else if(animationSpeed<0) {
			if(keyframe_t<0) {
				keyframe_t+=1;
				--keyframe_index;
				if(keyframe_index<0) {
					switch(animationBehavior) {
					case ANIMATE_ONCE:
						keyframe_index = 0;
						keyframe_t=0;
						animationSpeed=0;
						// TODO set the panel buttonAnimatePlayPause to paused
						break;
					case ANIMATE_LOOP:
						keyframe_index+=getKeyframeSize();
						break;
					}
				}
			}
		}
	}
}
