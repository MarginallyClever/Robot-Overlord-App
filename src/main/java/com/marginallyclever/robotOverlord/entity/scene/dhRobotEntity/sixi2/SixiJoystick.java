package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import java.util.Observable;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantLock;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.RemoteEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHKeyframe;
import com.marginallyclever.robotOverlord.entity.scene.modelEntity.ModelEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

public class SixiJoystick extends ModelEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9009274404516671409L;
	
	private Sixi2 target;
	
	private RemoteEntity connection = new RemoteEntity();
	private ReentrantLock lock = new ReentrantLock();
	
	DHKeyframe keyframe;
	
	public SixiJoystick() {
		setName("Sixi Joystick");
		connection.addObserver(this);
	}
	
	// TODO this is trash.  if robot is deleted this link would do what, exactly?
	// What if there was more than one Sixi?  More than one joystick?
	protected Sixi2 findRobot() {
		for( Entity e : getWorld().getChildren() ) {
			if(e instanceof Sixi2) {
				return (Sixi2)e;
			}
		}
		return null;
	}
	
	@Override
	public void update(Observable obs,Object obj) {
		if(obs == connection) {
			if(lock.isLocked()) return;
			lock.lock();
			try {
				if(target==null) {
					target = findRobot();
				}
				if(target!=null) {
					String message = (String)obj;
					//System.out.println("JOY: "+message.trim());
					
					if(keyframe==null) {
						keyframe = target.sim.getIKSolver().createDHKeyframe();
					}

					StringTokenizer tokenizer = new StringTokenizer(message);
					if(tokenizer.countTokens()<keyframe.fkValues.length) return;
					
					for(int i=0;i<keyframe.fkValues.length;++i) {
						double d = StringHelper.parseNumber(tokenizer.nextToken());
						keyframe.fkValues[i]=d;//Math.max(Math.min(d,180),-180);
					}
					keyframe.fkValues[1]*=-1;
					keyframe.fkValues[4]*=-1;
					keyframe.fkValues[1]-=90;
				}
			}
			finally {
				lock.unlock();
			}
		}
	}

	@Override
	public void update(double dt) {
		if(target==null) return;
		
		if(lock.isLocked()) return;
		lock.lock();
		try {
			target.sim.setPoseFK(keyframe);
		}
		finally {
			lock.unlock();
		}
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Sj", "Sixi Joystick");
		view.add(connection);
		view.popStack();
		super.getView(view);
	}
}
