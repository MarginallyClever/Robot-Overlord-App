package com.marginallyclever.robotoverlord.demos;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.shapes.Grid;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.mesh.load.MeshFactory;
import com.marginallyclever.robotoverlord.physics.ode.ODEPhysicsComponent;
import com.marginallyclever.robotoverlord.physics.ode.ODEPhysicsEngine;
import org.ode4j.math.DMatrix3;
import org.ode4j.math.DQuaternion;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.*;

import javax.vecmath.Vector3d;

/**
 * See https://github.com/tzaeschke/ode4j/blob/master/demo/src/main/java/org/ode4j/demo/DemoBuggy.java
 * @author Dan Royer
 *
 */
public class DemoSpidee implements Demo {

	@Override
	public String getName() {
		return "Crab robot SPIDEE-1";
	}

	@Override
	public void execute(RobotOverlord ro) {
		Entity sc = ro.getScene();
		
		// adjust default camera
		CameraComponent camera = ro.getCamera();
		PoseComponent pose = camera.getEntity().findFirstComponent(PoseComponent.class);
		pose.setPosition(new Vector3d(40/4f,-91/4f,106/4f));
		camera.lookAt(new Vector3d(0,0,0));
		camera.setOrbitDistance(20);
		
		// add some lights
		LightComponent light;
		Entity light0 = new Entity("Light2");
		sc.addEntity(light0);
		light0.addComponent(pose = new PoseComponent());
		light0.addComponent(light = new LightComponent());
    	pose.setPosition(new Vector3d(60,-60,160));
    	light.setDiffuse(1,1,1,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);
    	light.setAttenuationLinear(0.0014);
    	light.setAttenuationQuadratic(7*1e-6);
    	light.setDirectional(true);

		Entity spidee = new Entity("Spidee-1");
		sc.addEntity(spidee);

		spidee.addEntity(createMesh("/Spidee/body.stl"));

		spidee.addEntity(createLimb("RF",   45,true));
		spidee.addEntity(createLimb("RM",   90,true));
		spidee.addEntity(createLimb("RB",  135,true));
		spidee.addEntity(createLimb("LF",  -45,false));
		spidee.addEntity(createLimb("LM",  -90,false));
		spidee.addEntity(createLimb("LB", -135,false));
	}

	private Entity createLimb(String name,double degrees,boolean isRight) {
		DHComponent [] dh = new DHComponent[4];
		for(int i=0;i<dh.length;++i) {
			dh[i] = new DHComponent();
			dh[i].set(0,5,0,0,120,-120);
			dh[i].setVisible(true);
		}
		Entity limb = new Entity(name);
		limb.addComponent(new PoseComponent());
		limb.addComponent(new RobotComponent());

		Entity hip = new Entity("Hip");
		hip.addComponent(new PoseComponent());
		hip.addComponent(dh[0]);
		dh[0].set(4.5,11,90,degrees,degrees+30,degrees-30);
		if(isRight) hip.addEntity(createMesh("/Spidee/shoulder_right.stl"));
		else        hip.addEntity(createMesh("/Spidee/shoulder_left.stl"));

		Entity thigh = new Entity("Thigh");
		thigh.addComponent(new PoseComponent());
		thigh.addComponent(dh[1]);
		thigh.addEntity(createMesh("/Spidee/thigh.stl"));

		Entity calf = new Entity("Calf");
		calf.addComponent(new PoseComponent());
		calf.addComponent(dh[2]);
		if(isRight) calf.addEntity(createMesh("/Spidee/shin_right.stl"));
		else		calf.addEntity(createMesh("/Spidee/shin_left.stl"));

		Entity foot = new Entity("Foot");
		foot.addComponent(new PoseComponent());
		foot.addComponent(dh[3]);
		dh[3].setR(0);
		foot.addComponent(new ArmEndEffectorComponent());

		limb.addEntity(hip);
		hip.addEntity(thigh);
		thigh.addEntity(calf);
		calf.addEntity(foot);

		return limb;
	}

	private Entity createMesh(String filename) {
		MeshFromFile mff = new MeshFromFile();
		mff.setFilename(filename);

		Entity mesh = new Entity("Mesh");
		mesh.addComponent(new PoseComponent());
		mesh.addComponent(mff);
		mesh.addComponent(new MaterialComponent());

		return mesh;
	}
}
