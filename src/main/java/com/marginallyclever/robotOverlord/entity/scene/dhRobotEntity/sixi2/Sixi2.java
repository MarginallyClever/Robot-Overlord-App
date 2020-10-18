package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import java.util.Observable;
import java.util.Observer;

import javax.vecmath.Matrix4d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotModel;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.PoseFK;
import com.marginallyclever.robotOverlord.log.Log;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * Sixi2 compares the simulated position and the reported live position to determine if a collision
 * has occurred and can react from there.
 * 
 * This design is similar to a Flyweight design pattern - the extrinsic (unchanging) model is shared 
 * by the intrinsic (state-dependent) poses.  
 * 
 * Put another way, the model describes the physical limits 
 * of the robot and each PoseFK is a state somewhere inside those limits.
 * 
 * @author Dan Royer
 *
 */
public class Sixi2 extends PoseEntity {
	// the model used to render & control (the Flyweight)
	protected DHRobotModel model;
	// the live robot in the real world.  Controls comms with the machine.
	protected Sixi2Live live;
	// a simulation of the motors which should match the ideal physical behavior.
	protected Sixi2Sim sim;
	
	// there's also a Sixi2 that the user controls directly through the GUI to set new target states.
	// in other words, the user has no direct control over the live or sim robots.
	protected Sixi2Command cursor;
	
	public Sixi2() {
		super("Sixi2");
		// model should begin at home position.
		model = new Sixi2Model();
		// the interface to the real machine
		live = new Sixi2Live(model);
		// the interface to the simulated machine.
		sim = new Sixi2Sim(model);
		// the "cursor" or "hot" position the user is currently looking at, which is neither live nor sim.
		setCursor(new Sixi2Command(sim.getPoseNow(),
				Sixi2Model.DEFAULT_FEEDRATE,
				Sixi2Model.DEFAULT_ACCELERATION));
		cursor.addObserver(this);
		addChild(cursor);
		//addChild(live);
		//addChild(sim);
		
		// set the user controlled IK to the model, which is at home position.
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, pose);

		// live machine reports
		live.render(gl2);
		// user controlled version
		model.setPoseFK(cursor.poseFK);
		model.setDiffuseColor(1,1,1,1);
		model.render(gl2);
		// simulation claims
		sim.render(gl2);

		gl2.glPopMatrix();

		// other stuff
		super.render(gl2);
	}
	
	@Override
	public void update(double dt) {
		live.update(dt);
		sim.update(dt);
		
		super.update(dt);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if(o==cursor) {
			Matrix4d m = cursor.getPose();
			if(model.setPoseIK(m)) {
				cursor.poseFK = model.getPoseFK();
			}
		}
		super.update(o, arg);
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("S", "Sixi");
		view.addButton("Go Home").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				model.goHome();
				sim.setPoseTo(model.getPoseFK());
				cursor.setPose(model.getPoseIK());
				cursor.poseFK = model.getPoseFK();
			}
		});
		view.addButton("Append").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				try {
					addDestination((Sixi2Command)cursor.clone());
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		view.addButton("Time estimate").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				sim.eStop();
				for( Entity child : children ) {
					if(child instanceof Sixi2Command ) {
						Sixi2Command sc = (Sixi2Command)child;
						sim.AddDestination(sc.poseFK, sc.feedrateSlider.get(), sc.accelerationSlider.get());
					}
				}
				double sum=sim.getTimeRemaining();
				sim.eStop();
				Log.message("Time estimate: "+StringHelper.formatTime(sum));
			}
		});
		view.popStack();
		
		sim.getView(view);
		live.getView(view);
		
		super.getView(view);
	}
	
	public DHRobotModel getModel() {
		return model;
	}
	
	// TODO should probably be replace with Entity.addChild(index,original)
	public void addDestination(Sixi2Command c) {
		// queue it
		try {
			Sixi2Command copy = (Sixi2Command)c.clone();
			copy.setName(getUniqueChildName(c));
			
			int i;
			for(i=0;i<children.size();++i) {
				if(children.get(i)==c) break;
			}
			addChild(i,copy);
			((RobotOverlord)getRoot()).updateEntityTree();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void goTo(PoseFK poseTo, double feedrate, double acceleration) {
		sim.AddDestination(poseTo, feedrate, acceleration);
		live.AddDestination(poseTo,feedrate, acceleration);
	}
	

	// recursively set for all children
	public void setShowBoundingBox(boolean arg0) {
		super.setShowBoundingBox(arg0);
		model.getLink(0).setShowBoundingBox(arg0);
	}
	
	// recursively set for all children
	public void setShowLocalOrigin(boolean arg0) {
		super.setShowLocalOrigin(arg0);
		model.getLink(0).setShowLocalOrigin(arg0);
	}

	// recursively set for all children
	public void setShowLineage(boolean arg0) {
		super.setShowLineage(arg0);
		model.getLink(0).setShowLineage(arg0);
	}

	public void setCursor(Sixi2Command sixi2Command) {
		if(cursor != null)
			cursor.deleteObserver(this);
		
		model.setPoseFK(sixi2Command.poseFK);
		sixi2Command.setPose(model.getPoseIK());
		cursor = sixi2Command;
		
		if(cursor != null)
			cursor.addObserver(this);
	}
}
