package com.marginallyclever.robotOverlord.sixiJoystick;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JPanel;

import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.communications.NetworkConnectionListener;
import com.marginallyclever.communications.NetworkConnectionManager;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.dhRobot.DHKeyframe;
import com.marginallyclever.robotOverlord.dhRobot.DHRobot;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.modelInWorld.ModelInWorld;

public class SixiJoystick extends ModelInWorld implements NetworkConnectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1645097210021141638L;
	
	private DHRobot target;
	private SixiJoystickPanel panel;
	private NetworkConnection connection;
	private ReentrantLock lock;
	DHKeyframe keyframe;
	
	public SixiJoystick() {
		setDisplayName("Sixi Joystick");
		lock = new ReentrantLock();
	}
	
	
	@Override
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);

		// remove material panel
		list.remove(list.size()-1);
		// remove model panel
		list.remove(list.size()-1);
		
		if(panel == null) panel = new SixiJoystickPanel(gui,this);
		list.add(panel);
		
		return list;
	}
	
	public void closeConnection() {
		connection.closeConnection();
		connection.removeListener(this);
		connection=null;
	}
	
	public void openConnection() {
		NetworkConnection s = NetworkConnectionManager.requestNewConnection(null);
		if(s!=null) {
			connection = s;
			connection.addListener(this);
		}
	}
	
	
	public NetworkConnection getConnection() {
		return this.connection;
	}

	// TODO this is trash.  if robot is deleted this link would do what, exactly?
	// should probably be a subscription model.
	protected DHRobot findRobot() {
		Iterator<Entity> entities = getWorld().getChildren().iterator();
		while(entities.hasNext()) {
			Entity e = entities.next();
			if(e instanceof DHRobot) {
				return (DHRobot)e;
			}
		}
		return null;
	}
	
	@Override
	public void lineError(NetworkConnection arg0, int lineNumber) {}
	@Override
	public void sendBufferEmpty(NetworkConnection arg0) {}
	
	@Override
	// data arrives asynchronously, so make sure to 
	public void dataAvailable(NetworkConnection arg0, String data) {
		if(lock.isLocked()) return;
		lock.lock();
		try {
			if(target==null) {
				target = findRobot();
				if(target!=null) {
					 keyframe = (DHKeyframe)target.createKeyframe();
				}
			}
			if(target!=null) {
				StringTokenizer tokenizer = new StringTokenizer(data);
				if(tokenizer.countTokens()<6) return;
				
				for(int i=0;i<6;++i) {
					double d = StringHelper.parseNumber(tokenizer.nextToken());
					keyframe.fkValues[i]=Math.max(Math.min(d,180),-180);
				}
				keyframe.fkValues[1]*=-1;
				keyframe.fkValues[4]*=-1;
			}
		}
		finally {
			lock.unlock();
		}
	}

	@Override
	public void update(double dt) {
		if(target!=null) {
			if(lock.isLocked()) return;
			lock.lock();
			try {
				DHKeyframe saveKeyframe = target.getRobotPose();
				target.setDisablePanel(true);
				target.setLivePose(keyframe);
				target.setTargetMatrix(target.getLiveMatrix());
				target.setLivePose(saveKeyframe);
				target.setDisablePanel(false);
			}
			finally {
				lock.unlock();
			}
		}
	}
}
