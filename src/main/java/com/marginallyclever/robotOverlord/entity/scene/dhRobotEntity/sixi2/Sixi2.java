package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import java.util.Observable;
import java.util.Observer;

import javax.vecmath.Matrix4d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.PoseFK;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * Sixi2 compares the simulated position and the reported live position to determine if a collision
 * has occurred and can react from there.
 * @author Dan Royer
 *
 */
public class Sixi2 extends PoseEntity {
	// the model used to render & control.
	public Sixi2Model model;
	// the live robot in the real world.
	public Sixi2Live live;
	// the simulated robot.
	public Sixi2Sim sim;
	
	// aka joint angles
	protected PoseFK userControlledFK;
	// aka end effector
	protected PoseEntity userControlledIK;
	
	protected DoubleEntity feedrateSlider = new DoubleEntity("Feedrate",Sixi2FirmwareSettings.DEFAULT_FEEDRATE);
	protected DoubleEntity accelerationSlider = new DoubleEntity("Acceleration",Sixi2FirmwareSettings.DEFAULT_ACCELERATION);
	
	public Sixi2() {
		super("Sixi2");
		// model should begin at home position.
		model = new Sixi2Model();
		// the interface to the real machine
		live = new Sixi2Live(model);
		// the interface to the simulated machine.
		sim = new Sixi2Sim(model);
		userControlledFK = model.createPoseFK();
		userControlledIK = new PoseEntity("End Effector");
		userControlledIK.addObserver(this);
		//addChild(live);
		//addChild(sim);
		addChild(userControlledIK);
		
		// set the user controlled IK to the model, which is at home position.
		userControlledIK.setPose(model.getPoseIK());
		userControlledFK = model.getPoseFK();
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, pose);

		// live machine reports
		live.render(gl2);
		// simulation claims
		sim.render(gl2);
		// user controlled version
		model.setPoseFK(userControlledFK);
		model.setDiffuseColor(1,1,1,1);
		model.render(gl2);

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
		if(o==userControlledIK) {
			Matrix4d m = userControlledIK.getPose();
			if(model.setPoseIK(m)) {
				userControlledFK = model.getPoseFK();
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
				userControlledIK.setPose(model.getPoseIK());
				userControlledFK = model.getPoseFK();
			}
		});
		view.addRange(feedrateSlider, (int)Sixi2FirmwareSettings.MAX_FEEDRATE, 0);
		view.addRange(accelerationSlider, (int)Sixi2FirmwareSettings.MAX_ACCELERATION, 0);
		
		view.addButton("Go Here").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				addDestination(userControlledFK,
						(double)feedrateSlider.get(),
						(double)accelerationSlider.get());
			}
		});
		view.popStack();
		
		sim.getView(view);
		live.getView(view);
		
		super.getView(view);
	}
	
	public void addDestination(PoseFK poseTo,double feedrate,double acceleration) {
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
		super.setShowBoundingBox(arg0);
		model.getLink(0).setShowLocalOrigin(arg0);
	}

	// recursively set for all children
	public void setShowLineage(boolean arg0) {
		super.setShowBoundingBox(arg0);
		model.getLink(0).setShowLineage(arg0);
	}
}
