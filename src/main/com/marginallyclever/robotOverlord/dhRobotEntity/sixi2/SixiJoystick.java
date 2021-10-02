package com.marginallyclever.robotOverlord.dhRobotEntity.sixi2;

import java.beans.PropertyChangeEvent;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantLock;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.dhRobotEntity.PoseFK;
import com.marginallyclever.robotOverlord.shape.Shape;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.RemoteEntity;


@Deprecated
public class SixiJoystick extends Shape {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3463275255789595886L;

	private Sixi2 target;
	
	private RemoteEntity connection = new RemoteEntity();
	private ReentrantLock lock = new ReentrantLock();

	PoseFK [] keyframeSamples = new PoseFK[10];  // more samples = slower response but smoother results.
	
	PoseFK keyframe;
	
	public SixiJoystick() {
		setName("Sixi Joystick");
		connection.addPropertyChangeListener(this);
	}
	
	// TODO this is trash.  if robot is deleted this link would do what, exactly?
	// What if there was more than one Sixi?  More than one joystick?
	protected Sixi2 findRobot() {
		if(parent instanceof Sixi2) {
			return (Sixi2)parent;
		}
		
		for( Entity e : getWorld().getChildren() ) {
			if(e instanceof Sixi2) {
				return (Sixi2)e;
			}
		}
		
		return null;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		Object o = evt.getSource();
		
		if(o == connection) {
			if(lock.isLocked()) return;
			lock.lock();
			try {
				if(target==null) {
					
					target = findRobot();
					if(target!=null) {
						Sixi2Model model = (Sixi2Model)target.getModel();

						if(keyframe==null) {
							keyframe = model.createPoseFK();
							for(int j=0;j<keyframeSamples.length;++j) {
								keyframeSamples[j]= model.createPoseFK();
							}
						}
					}
				}
				if(target!=null) {
					String message = (String)o;
					//Log.message("JOY: "+message.trim());
					
					int i,j;
					
					// age the samples
					for(j=1;j<keyframeSamples.length;++j) {
						keyframeSamples[j-1].set(keyframeSamples[j]);
					}

					StringTokenizer tokenizer = new StringTokenizer(message);
					if(tokenizer.countTokens()<keyframe.fkValues.length) return;
					
					// update the last sample
					j=keyframeSamples.length-1;
					for(i=0;i<keyframe.fkValues.length;++i) {
						double d = StringHelper.parseNumber(tokenizer.nextToken());
						keyframeSamples[j].fkValues[i]=d;//Math.max(Math.min(d,180),-180);
					}
					keyframeSamples[j].fkValues[1]*=-1;
					keyframeSamples[j].fkValues[4]*=-1;
					keyframeSamples[j].fkValues[1]-=90;
					
					// update the average
					for(i=0;i<keyframe.fkValues.length;++i) {
						keyframe.fkValues[i] = 0;
						for(j=1;j<keyframeSamples.length;++j) {
							keyframe.fkValues[i] += keyframeSamples[j].fkValues[i];
						}
						keyframe.fkValues[i] /= keyframeSamples.length;
					}
				}
			}
			catch(NumberFormatException e) {
				e.printStackTrace();
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
			Sixi2Command sc = target.getCursor();
			if(sc!=null) {
				// set the cursor to the joystick position
				Sixi2Model m = (Sixi2Model) target.getModel();
				m.setPoseFK(keyframe);
				sc.setPoseWorld(m.getPoseIK());
			}
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
