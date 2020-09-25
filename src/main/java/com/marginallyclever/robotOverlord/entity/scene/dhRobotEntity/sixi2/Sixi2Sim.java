package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.ReentrantLock;

import javax.vecmath.Matrix4d;

import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewElement;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

public class Sixi2Sim extends Sixi2Model {
	protected ReentrantLock lock = new ReentrantLock();
	
	
	public Sixi2Sim() {
		super();
		setName("Sim");
		
		for(DHLink link : links ) {
			link.setDHRobot(this);
		}
		endEffector.setDHRobot(this);
		
	    readyForCommands=true;
	    
	    // set blue
	    for( DHLink link : links ) {
	    	link.getMaterial().setDiffuseColor(113f/255f, 211f/255f, 226f/255f,0.70f);
	    }
	    
		endEffectorTarget.setPoseWorld(endEffector.getPoseWorld());
	    endEffector.addObserver(this);
		endEffectorTarget.addObserver(this);
	}

	@Override 
	public void sendCommand(String command) {
		super.sendCommand(command);
	}

	@Override 
	public void update(double dt) {
		super.update(dt);
	}
	
	@Override
	public void update(Observable obs, Object obj) {
		if(obs == endEffector) {
			if(!lock.isLocked()) {
				lock.lock();
				//setPoseIK(endEffector.getPoseWorld());
				endEffectorTarget.setPoseWorld(endEffector.getPoseWorld());
				lock.unlock();
			}
		}
		if(obs==endEffectorTarget) {
			if(!lock.isLocked()) {
				lock.lock();
				setPoseIK(endEffectorTarget.getPoseWorld());
				lock.unlock();
			}
		}
		super.update(obs, obj);
	}

	@Override
	public void setPoseWorld(Matrix4d m) {}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Ss", "Sixi Sim");
		ViewElement h = view.addButton("Go to home position");
		h.addObserver(new Observer() {
			@Override
			public void update(Observable arg0, Object arg1) {
				goHome();
			}
		});
		ViewElement r = view.addButton("Go to rest position");
		r.addObserver(new Observer() {
			@Override
			public void update(Observable arg0, Object arg1) {
				goRest();
			}
		});
		view.popStack();
		super.getView(view);
	}
}
