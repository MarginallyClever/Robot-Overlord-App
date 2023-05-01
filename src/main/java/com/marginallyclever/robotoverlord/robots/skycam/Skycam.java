package com.marginallyclever.robotoverlord.robots.skycam;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.RenderComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Vector3d;

public class Skycam extends RenderComponent {
	private static final Logger logger = LoggerFactory.getLogger(Skycam.class);
	
	// the model used to systems & control (the Flyweight)
	private final transient SkycamModel model = new SkycamModel();
	// the live robot in the real world.  Controls comms with the machine.
	private final transient SkycamLive live = new SkycamLive(model);
	// a simulation of the motors which should match the ideal physical behavior.
	private final transient SkycamSim sim = new SkycamSim(model);
	private Entity ee;
	private PoseComponent eePose;
	
	public Skycam() {
		super();
	}

	@Override
	public void setEntity(Entity entity) {
		super.setEntity(entity);
		if(entity == null) return;

		Entity maybe = entity.findByPath("./ee");
		if(maybe!=null) ee = maybe;
		else {
			ee = new Entity("ee");
			// EntityManager.addEntityToParent(ee,entity);
		}
		ee.addComponent(new PoseComponent());
		eePose = ee.getComponent(PoseComponent.class);
		eePose.setPosition(new Vector3d(0,0,0));
	}

	@Override
	public void render(GL2 gl2) {
		PoseComponent myPose = getEntity().getComponent(PoseComponent.class);

		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, myPose.getLocal());
		
		// live machine reports
		live.render(gl2);
		// user controlled version
		model.setPosition(eePose.getLocal());
		model.setDiffuseColor(1,1,1,1);
		model.render(gl2);
		// simulation claims
		sim.render(gl2);

		gl2.glPopMatrix();
	}
	
	@Override
	public void update(double dt) {
		live.update(dt);
		sim.update(dt);
		
		super.update(dt);
	}

	public SkycamModel getModel() {
		return model;
	}
}
