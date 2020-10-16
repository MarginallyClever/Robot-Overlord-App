package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import java.util.Observable;
import java.util.Observer;

import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.PoseFK;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

public class Sixi2Command extends PoseEntity implements Cloneable {
	public PoseFK pose;

	protected DoubleEntity feedrateSlider = new DoubleEntity("Feedrate",Sixi2Model.DEFAULT_FEEDRATE);
	protected DoubleEntity accelerationSlider = new DoubleEntity("Acceleration",Sixi2Model.DEFAULT_ACCELERATION);
	
	Sixi2Command(PoseFK p,double f,double a) {
		super("Pose");
		
		try {
			pose=(PoseFK)p.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		feedrateSlider.set(f);
		accelerationSlider.set(a);
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		Sixi2Command c = (Sixi2Command)super.clone();
		c.pose = (PoseFK)pose.clone();
		return c;
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("C", "Command");
		
		view.addRange(feedrateSlider, (int)Sixi2Model.MAX_FEEDRATE, 0);
		
		view.addRange(accelerationSlider, (int)Sixi2Model.MAX_ACCELERATION, 0);
		
		view.addButton("Queue").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				Sixi2 e = findParentSixi2();
				if(e==null) return;
				e.addDestination(pose,
						(double)feedrateSlider.get(),
						(double)accelerationSlider.get());
			}
		});
		view.addButton("Goto").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				Sixi2 e = findParentSixi2();
				if(e==null) return;
				e.goTo(pose,
						(double)feedrateSlider.get(),
						(double)accelerationSlider.get());
			}
		});
		view.popStack();
		super.getView(view);
	}
	
	protected Sixi2 findParentSixi2() {
		Entity e = getParent();
		while(e!=null) {
			if(e instanceof Sixi2) {
				return (Sixi2)e;
			}
			e = e.getParent();
		}
		return null;
	}
}
