package com.marginallyclever.robotoverlord.demos;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.demo.CrabRobotComponent;
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

		Entity spidee = new Entity("Spidee-1");
		spidee.addComponent(new CrabRobotComponent());
		sc.addEntity(spidee);

		createMesh(spidee,"/Spidee/body.stl");

		spidee.addEntity(positionLimb(createLimb("RF",true ),   45));
		spidee.addEntity(positionLimb(createLimb("RM",true ),   0));
		spidee.addEntity(positionLimb(createLimb("RB",true ),  -45));
		spidee.addEntity(positionLimb(createLimb("LF",false),  135));
		spidee.addEntity(positionLimb(createLimb("LM",false),  180));
		spidee.addEntity(positionLimb(createLimb("LB",false), -135));
	}

	private Entity positionLimb(Entity limb,float degrees) {
		PoseComponent pose = limb.findFirstComponent(PoseComponent.class);
		double r = Math.toRadians(degrees);
		pose.setPosition(new Vector3d(Math.cos(r)*10,Math.sin(r)*10,2.6));
		pose.setRotation(new Vector3d(0,0,degrees));
		return limb;
	}

	private Entity createLimb(String name,boolean isRight) {
		DHComponent [] dh = new DHComponent[3];
		for(int i=0;i<dh.length;++i) {
			dh[i] = new DHComponent();
			dh[i].setVisible(true);
		}
		Entity limb = new Entity(name);
		limb.addComponent(new PoseComponent());
		limb.addComponent(new RobotComponent());

		Entity hip = new Entity("Hip");
		limb.addEntity(hip);
		Entity thigh = new Entity("Thigh");
		hip.addEntity(thigh);
		Entity calf = new Entity("Calf");
		thigh.addEntity(calf);
		Entity foot = new Entity("Foot");
		calf.addEntity(foot);

		hip.addComponent(dh[0]);
		dh[0].set(0,2.2,90,0,30,-30);
		if(isRight) createMesh(hip,"/Spidee/shoulder_right.obj");
		else        createMesh(hip,"/Spidee/shoulder_left.obj");

		thigh.addComponent(dh[1]);
		dh[1].set( 0,8.5,0,0,120,-120);
		createMesh(thigh,"/Spidee/thigh.obj");

		calf.addComponent(dh[2]);
		dh[2].set(0,10.5,0,0,120,-120);
		if(isRight) createMesh(calf,"/Spidee/calf_right.obj");
		else		createMesh(calf,"/Spidee/calf_left.obj");

		foot.addComponent(new PoseComponent());
		foot.addComponent(new ArmEndEffectorComponent());

		return limb;
	}

	private void createMesh(Entity parent,String filename) {
		MeshFromFile mff = new MeshFromFile();
		mff.setFilename(filename);

		Entity mesh = new Entity("Mesh");
		mesh.addComponent(new PoseComponent());
		mesh.addComponent(new MaterialComponent());
		mesh.addComponent(mff);

		parent.addEntity(mesh);

		OriginAdjustComponent oac = new OriginAdjustComponent();
		mesh.addComponent(oac);
		oac.adjust();
		mesh.removeComponent(oac);
	}
}
