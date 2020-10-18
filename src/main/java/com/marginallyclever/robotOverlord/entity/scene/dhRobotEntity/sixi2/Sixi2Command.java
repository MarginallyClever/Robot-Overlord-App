package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import java.util.Observable;
import java.util.Observer;

import javax.vecmath.Matrix4d;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.EntityFocusListener;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.PoseFK;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

public class Sixi2Command extends PoseEntity implements Cloneable, EntityFocusListener {
	public PoseFK poseFK;

	protected DoubleEntity feedrateSlider = new DoubleEntity("Feedrate",Sixi2Model.DEFAULT_FEEDRATE);
	protected DoubleEntity accelerationSlider = new DoubleEntity("Acceleration",Sixi2Model.DEFAULT_ACCELERATION);
	
	Sixi2Command(PoseFK p,double f,double a) {
		super("Pose");
		
		try {
			poseFK=(PoseFK)p.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		feedrateSlider.set(f);
		accelerationSlider.set(a);
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		Sixi2Command c = (Sixi2Command)super.clone();
		c.pose = (Matrix4d)pose.clone();
		c.poseFK = (PoseFK)poseFK.clone();
		c.feedrateSlider = new DoubleEntity(feedrateSlider.getName());
		c.feedrateSlider.set(feedrateSlider.get());
		c.accelerationSlider = new DoubleEntity(accelerationSlider.getName());
		c.accelerationSlider.set(accelerationSlider.get());
		return c;
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("C", "Command");
		
		view.addRange(feedrateSlider, (int)Sixi2Model.MAX_FEEDRATE, 0);
		view.addRange(accelerationSlider, (int)Sixi2Model.MAX_ACCELERATION, 0);
		
		final Sixi2Command sc = this;
		
		view.addButton("Copy").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				Sixi2 e = findParentSixi2();
				if(e==null) return;
				e.addDestination(sc);
			}
		});
		view.addButton("Goto").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				Sixi2 e = findParentSixi2();
				if(e==null) return;
				e.goTo(poseFK,
						(double)feedrateSlider.get(),
						(double)accelerationSlider.get());
			}
		});
		view.popStack();

		Sixi2 e = findParentSixi2();
		if(e!=null) {
			view.pushStack("FK", "Forward Kinematics");
			for(int i=0;i<poseFK.fkValues.length;++i) {
				view.addStaticText(i+" = "+StringHelper.formatDouble(poseFK.fkValues[i]));
			}
			view.popStack();
		}
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

	@Override
	public void gainedFocus() {
		Sixi2 e = findParentSixi2();
		if(e==null) return;
		e.setCursor(this);
	}

	@Override
	public void lostFocus() {
		// TODO Auto-generated method stub
		
	}
}
