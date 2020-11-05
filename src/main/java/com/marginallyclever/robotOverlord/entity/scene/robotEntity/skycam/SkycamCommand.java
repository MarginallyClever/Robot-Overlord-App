package com.marginallyclever.robotOverlord.entity.scene.robotEntity.skycam;

import java.io.Serializable;
import java.util.Observable;
import java.util.Observer;

import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.EntityFocusListener;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

public class SkycamCommand extends PoseEntity implements Cloneable, EntityFocusListener, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4367169419590887821L;

	protected transient DoubleEntity feedrateSlider = new DoubleEntity("Feedrate",SkycamModel.DEFAULT_FEEDRATE);
	protected transient DoubleEntity accelerationSlider = new DoubleEntity("Acceleration",SkycamModel.DEFAULT_ACCELERATION);

	public SkycamCommand() {
		super("Pose");
	}
	
	public SkycamCommand(Vector3d p,double feedrate,double acceleration) {
		super("Pose");
		
		setPosition(p);
		feedrateSlider.set(feedrate);
		accelerationSlider.set(acceleration);
	}

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("C", "Command");
		
		view.addRange(feedrateSlider, (int)SkycamModel.MAX_FEEDRATE, 0);
		view.addRange(accelerationSlider, (int)SkycamModel.MAX_ACCELERATION, 0);
		
		final SkycamCommand sc = this;
		
		view.addButton("Copy").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				Skycam myParent = findParentSkycam();
				if(myParent==null) return;
				myParent.queueDestination(sc);
			}
		});
		view.addButton("Goto").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				Skycam e = findParentSkycam();
				if(e==null) return;
				e.goTo(sc);
			}
		});
		view.popStack();

		super.getView(view);
	}
	
	protected Skycam findParentSkycam() {
		Entity e = getParent();
		while(e!=null) {
			if(e instanceof Skycam) {
				return (Skycam)e;
			}
			e = e.getParent();
		}
		return null;
	}

	@Override
	public void gainedFocus() {
		Skycam e = findParentSkycam();
		if(e==null) return;
		e.setCursor(this);
	}

	@Override
	public void lostFocus() {}

	@Override
	public Object clone() {
		SkycamCommand c = (SkycamCommand)super.clone();
		c.feedrateSlider = new DoubleEntity(feedrateSlider.getName());
		c.feedrateSlider.set(feedrateSlider.get());
		c.accelerationSlider = new DoubleEntity(accelerationSlider.getName());
		c.accelerationSlider.set(accelerationSlider.get());
		return c;
	}

	public String poseFKToString() {
		Vector3d p = this.getPosition();
		return "G0"
				+" X"+StringHelper.formatDouble(p.x)
				+" Y"+StringHelper.formatDouble(p.y)
				+" Z"+StringHelper.formatDouble(p.z);
	}
	
	/**
	 * Stringify the feedrate and acceleration.
	 * @return
	 */
	public String getFAAsString() {
		return "G0"
			+" F"+StringHelper.formatDouble(feedrateSlider.get())
			+" A"+StringHelper.formatDouble(accelerationSlider.get());
	}
}
