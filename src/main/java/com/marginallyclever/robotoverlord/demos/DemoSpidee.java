package com.marginallyclever.robotoverlord.demos;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.Scene;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.demo.CrabRobotComponent;
import com.marginallyclever.robotoverlord.components.shapes.Grid;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.systems.OriginAdjustSystem;

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
	public void execute(Scene scene) {

		// adjust default camera
		CameraComponent camera = scene.getCamera();
		PoseComponent pose = camera.getEntity().findFirstComponent(PoseComponent.class);
		pose.setPosition(new Vector3d(40/4f,-91/4f,106/4f));
		camera.lookAt(new Vector3d(0,0,0));
		camera.setOrbitDistance(50);

		// add a grid
		Entity gridEntity = new Entity("Grid");
		Grid grid = new Grid();
		gridEntity.addComponent(grid);
		scene.addEntityToParent(gridEntity,scene.getRoot());
		grid.setWidth(100);
		grid.setLength(100);
		MaterialComponent mat = gridEntity.findFirstComponent(MaterialComponent.class);
		mat.setDiffuseColor(0,0,0,0);
		mat.setLit(false);

		// add spidee
		Entity spidee = new Entity("Spidee-1");
		createCrab(spidee,scene);
		scene.addEntityToParent(spidee,scene.getRoot());
	}

	public void createCrab(Entity entity,Scene scene) {
		CrabRobotComponent crab = new CrabRobotComponent();
		entity.addComponent(crab);
		entity.addComponent(new PoseComponent());

		scene.addEntityToParent(createMesh("/robots/Spidee/body.stl",new ColorRGB(0x3333FF)),entity);


		// 0   5
		// 1 x 4
		// 2   3
		RobotComponent[] legs = new RobotComponent[6];
		legs[0] = createLimb(scene,crab,"LF",0,false,  135);
		legs[1] = createLimb(scene,crab,"LM",1,false,  180);
		legs[2] = createLimb(scene,crab,"LB",2,false, -135);
		legs[3] = createLimb(scene,crab,"RB",3,true,   -45);
		legs[4] = createLimb(scene,crab,"RM",4,true,     0);
		legs[5] = createLimb(scene,crab,"RF",5,true,    45);

		int i=0;
		for(RobotComponent leg : legs) {
			crab.setLeg(i++,leg);
			scene.addEntityToParent(leg.getEntity(),entity);
		}
	}

	private RobotComponent createLimb(Scene scene,CrabRobotComponent crab,String name,int index,boolean isRight, float degrees) {
		DHComponent[] dh = new DHComponent[3];
		for(int i=0;i<dh.length;++i) {
			dh[i] = new DHComponent();
			dh[i].setVisible(false);
		}
		Entity limb = createPoseEntity(name);

		Entity hip = createPoseEntity(CrabRobotComponent.HIP);
		scene.addEntityToParent(hip,limb);
		Entity thigh = createPoseEntity(CrabRobotComponent.THIGH);
		scene.addEntityToParent(thigh,hip);
		Entity calf = createPoseEntity(CrabRobotComponent.CALF);
		scene.addEntityToParent(calf,thigh);
		Entity foot = createPoseEntity(CrabRobotComponent.FOOT);
		scene.addEntityToParent(foot,calf);

		hip.addComponent(dh[0]);
		dh[0].set(0,2.2,90,0,60,-60,true);
		if(isRight) scene.addEntityToParent(createMesh("/robots/Spidee/shoulder_right.obj",new ColorRGB(0x9999FF)),hip);
		else        scene.addEntityToParent(createMesh("/robots/Spidee/shoulder_left.obj",new ColorRGB(0x9999FF)),hip);

		thigh.addComponent(dh[1]);
		dh[1].set( 0,8.5,0,0,106,-72,true);
		scene.addEntityToParent(createMesh("/robots/Spidee/thigh.obj",new ColorRGB(0xFFFFFF)),thigh);

		calf.addComponent(dh[2]);
		dh[2].set(0,10.5,0,0,15,-160,true);
		if(isRight) scene.addEntityToParent(createMesh("/robots/Spidee/calf_right.obj",new ColorRGB(0xFFFF99)),calf);
		else		scene.addEntityToParent(createMesh("/robots/Spidee/calf_left.obj",new ColorRGB(0xFFFF99)),calf);

		foot.addComponent(new ArmEndEffectorComponent());

		// position limb
		PoseComponent pose = limb.findFirstComponent(PoseComponent.class);
		double r = Math.toRadians(degrees);
		pose.setPosition(new Vector3d(Math.cos(r)*10,Math.sin(r)*10,2.6));
		pose.setRotation(new Vector3d(0,0,degrees));

		// Done at the end so RobotComponent can find all bones DHComponents.
		RobotComponent robot = new RobotComponent();
		limb.addComponent(robot);

		crab.setInitialPointOfContact(limb,index);

		return robot;
	}

	private Entity createPoseEntity(String name) {
		Entity result = new Entity(name);
		result.addComponent(new PoseComponent());
		return result;
	}

	private Entity createMesh(String filename, ColorRGB color) {
		Entity mesh = createPoseEntity("Mesh");

		MaterialComponent mc = new MaterialComponent();
		mc.setDiffuseColor(color.red/255.0,color.green/255.0,color.blue/255.0,1);
		mesh.addComponent(mc);

		MeshFromFile mff = new MeshFromFile();
		mff.setFilename(filename);
		mesh.addComponent(mff);

		OriginAdjustSystem oas = new OriginAdjustSystem();
		oas.adjustOne(mesh);

		return mesh;
	}
}
