package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import java.io.Serializable;
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

public class Sixi2Command extends PoseEntity implements Cloneable, EntityFocusListener, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected PoseFK poseFK;
	protected transient DoubleEntity feedrateSlider = new DoubleEntity("Feedrate (deg/s)",Sixi2Model.DEFAULT_FEEDRATE);
	protected transient DoubleEntity accelerationSlider = new DoubleEntity("Acceleration (deg/s/s)",Sixi2Model.DEFAULT_ACCELERATION);
	protected transient DoubleEntity toolSlider = new DoubleEntity("Tool",0);
	protected transient DoubleEntity wait = new DoubleEntity("Wait (s)",0);
	
	public Sixi2Command() {
		super("Pose");
	}
	
	/**
	 * 
	 * @param poseFK
	 * @param feedrate >=0
	 * @param acceleration >=0
	 * @param tool angle >=0
	 * @param delay seconds >=0
	 */
	public Sixi2Command(PoseFK poseFK,double feedrate,double acceleration,double tool,double delay) {
		super("Pose");
		
		this.poseFK=(PoseFK)poseFK.clone();
		feedrateSlider.set(feedrate);
		accelerationSlider.set(acceleration);
		toolSlider.set(tool);
	}

	public PoseFK getPoseFK() {
		return poseFK;
	}

	public void setPoseFK(PoseFK poseFK) {
		this.poseFK = poseFK;
	}
	
	@Override
	protected Object clone() {
		Sixi2Command c = (Sixi2Command)super.clone();
		c.pose = (Matrix4d)pose.clone();
		c.poseFK = (PoseFK)poseFK.clone();
		c.feedrateSlider = new DoubleEntity(feedrateSlider.getName());
		c.feedrateSlider.set(feedrateSlider.get());
		c.accelerationSlider = new DoubleEntity(accelerationSlider.getName());
		c.toolSlider = new DoubleEntity(toolSlider.getName());
		c.accelerationSlider.set(accelerationSlider.get());
		return c;
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("C", "Command");
		
		view.addRange(feedrateSlider, (int)Sixi2Model.MAX_FEEDRATE, 0);
		view.addRange(accelerationSlider, (int)Sixi2Model.MAX_ACCELERATION, 0);
		view.addRange(toolSlider, 1, 0);
		view.add(wait);
		
		final Sixi2Command sc = this;
		
		view.addButton("Copy").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				Sixi2 e = findParentSixi2();
				if(e==null) return;
				e.queueDestination(sc);
			}
		});
		view.addButton("Goto").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				Sixi2 e = findParentSixi2();
				if(e==null) return;
				e.goTo(sc);
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
	public void lostFocus() {}
/*
	public void write(ObjectOutputStream stream) throws Exception {
		stream.writeObject(poseFK);
		stream.writeDouble(feedrateSlider.get());
		stream.writeDouble(accelerationSlider.get());
		stream.writeDouble(toolSlider.get());
	}
	
	public void read(ObjectInputStream stream) throws Exception {
		poseFK = (PoseFK)stream.readObject();
		feedrateSlider.set(stream.readDouble());
		accelerationSlider.set(stream.readDouble());
		toolSlider.set(stream.readDouble());
	}
*/
	/**
	 * Convert this command to a string useable by a live robot.
	 * @return
	 */
	public String poseFKToString() {
		return "G0"
			+" X"+StringHelper.formatDouble(poseFK.fkValues[0])
			+" Y"+StringHelper.formatDouble(poseFK.fkValues[1])
			+" Z"+StringHelper.formatDouble(poseFK.fkValues[2])
			+" U"+StringHelper.formatDouble(poseFK.fkValues[3])
			+" V"+StringHelper.formatDouble(poseFK.fkValues[4])
			+" W"+StringHelper.formatDouble(poseFK.fkValues[5]);
	}
	
	/**
	 * Stringify the feedrate and acceleration.
	 * @return
	 */
	public String [] getExtraStrings() {
		double w = wait.get();
		int count =  w>0 ? 2:1;
		String [] list = new String[count];
		
		list[0]="G0"
					+" F"+StringHelper.formatDouble(feedrateSlider.get())
					+" A"+StringHelper.formatDouble(accelerationSlider.get())
					+" T"+(toolSlider.get()>0.5? 1:0);
		
		if(w>0) {
			list[1] = "D4 P"+StringHelper.formatDouble(w);
		}
			
		return list; 
	}
	
	public double getWait() {
		return wait.get();
	}
}
