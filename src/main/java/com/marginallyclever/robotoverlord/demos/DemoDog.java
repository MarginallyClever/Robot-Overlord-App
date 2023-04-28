package com.marginallyclever.robotoverlord.demos;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.EntityManager;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.demo.DogRobotComponent;
import com.marginallyclever.robotoverlord.components.shapes.Box;
import com.marginallyclever.robotoverlord.components.shapes.Cylinder;
import com.marginallyclever.robotoverlord.components.shapes.Grid;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;

import javax.vecmath.Vector3d;

/**
 * @author Dan Royer
 */
public class DemoDog implements Demo {

	@Override
	public String getName() {
		return "Dog robot Spot Micro";
	}

	@Override
	public void execute(EntityManager entityManager) {
		// adjust default camera
		CameraComponent camera = entityManager.getCamera();
		PoseComponent pose = camera.getEntity().findFirstComponent(PoseComponent.class);
		pose.setPosition(new Vector3d(40/4f,-91/4f,106/4f));
		camera.lookAt(new Vector3d(0,0,0));
		camera.setOrbitDistance(50);

		// add a grid
		Entity gridEntity = new Entity("Grid");
		Grid grid = new Grid();
		gridEntity.addComponent(grid);
		entityManager.addEntityToParent(gridEntity, entityManager.getRoot());
		grid.setWidth(100);
		grid.setLength(100);
		MaterialComponent mat = gridEntity.findFirstComponent(MaterialComponent.class);
		mat.setDiffuseColor(0,0,0,0);
		mat.setLit(false);

		// add dog
		Entity dog = new Entity("SpotMicro");
		createDog(dog, entityManager);
		entityManager.addEntityToParent(dog, entityManager.getRoot());
	}

	public void createDog(Entity myEntity, EntityManager entityManager) {
		DogRobotComponent dog = new DogRobotComponent();
		myEntity.addComponent(dog);

		myEntity.addComponent(new PoseComponent());
		PoseComponent myPose = myEntity.findFirstComponent(PoseComponent.class);
		myPose.setPosition(new Vector3d(0,0,5.4));
		myPose.setRotation(new Vector3d(90,0,0));

		Entity mesh = createMesh("/robots/SpotMicro/torso.obj",new ColorRGB(0xffffff));
		entityManager.addEntityToParent(mesh,myEntity);
		PoseComponent meshPose = mesh.findFirstComponent(PoseComponent.class);
		meshPose.setRotation(new Vector3d(90,180,180));
		meshPose.setPosition(new Vector3d(-0.7,4.1,7));


		// head
		// 0   2
		// 1   1
		double w = DogRobotComponent.KINEMATIC_BODY_WIDTH/2;
		double h = DogRobotComponent.KINEMATIC_BODY_HEIGHT/2;
		int i=0;
		RobotComponent[] legs = new RobotComponent[4];
		legs[i] = createLimb(entityManager,dog,"RF",i, true, -w, h, 1);  i++;
		legs[i] = createLimb(entityManager,dog,"RB",i, true, -w,-h, 1);  i++;
		legs[i] = createLimb(entityManager,dog,"LF",i,false,  w, h, 1);  i++;
		legs[i] = createLimb(entityManager,dog,"LB",i,false,  w,-h, 1);  i++;

		i=0;
		for(RobotComponent leg : legs) {
			dog.setLeg(i,leg);
			entityManager.addEntityToParent(leg.getEntity(),myEntity);
			dog.setInitialPointOfContact(leg.getEntity(),i++);
		}
	}

	private RobotComponent createLimb(EntityManager entityManager, DogRobotComponent dog, String name, int index, boolean isRight, double r, double d, double s) {
		DHComponent[] dh = new DHComponent[4];
		for(int i=0;i<dh.length;++i) {
			dh[i] = new DHComponent();
			dh[i].setVisible(true);
		}
		Entity limb = createPoseEntity(name);
		PoseComponent limbPose = limb.findFirstComponent(PoseComponent.class);
		limbPose.setPosition(new Vector3d(r,0,d));

		entityManager.addEntityToParent(createCylinder(4,2.1,new ColorRGB(0x9999FF)),limb);

		Entity hip = createPoseEntity(DogRobotComponent.HIP);
		entityManager.addEntityToParent(hip,limb);
		Entity thigh = createPoseEntity(DogRobotComponent.THIGH);
		entityManager.addEntityToParent(thigh,hip);
		Entity calf = createPoseEntity(DogRobotComponent.CALF);
		entityManager.addEntityToParent(calf,thigh);
		Entity foot = createPoseEntity(DogRobotComponent.FOOT);
		entityManager.addEntityToParent(foot,calf);

		hip.addComponent(dh[0]);
		dh[0].set( 0, 0, 90*(isRight?1:-1), 90, 360, -360,true);
		entityManager.addEntityToParent(createCylinder(5,2,new ColorRGB(0xFFFFFF)),hip);

		thigh.addComponent(dh[1]);
		dh[1].set(-3.5 * s, 11.5, 0, 135*(isRight?-1:1), 360, -360,true);
		entityManager.addEntityToParent(createBox(dh[1].getR(),1,new ColorRGB(0xFFFF99)),thigh);

		calf.addComponent(new ArmEndEffectorComponent());
		calf.addComponent(dh[2]);
		dh[2].set(0, 13, 0, 90*(isRight?-1:1), 360, -360,true);
		entityManager.addEntityToParent(createBox(dh[2].getR(),0.7,new ColorRGB(0xFFFF66)),calf);

		foot.addComponent(new ArmEndEffectorComponent());

		// Done at the end so RobotComponent can find all bones DHComponents.
		RobotComponent robot = new RobotComponent();
		limb.addComponent(robot);

		dog.setInitialPointOfContact(limb,index);

		return robot;
	}

	private Entity createBox(double r, double v,ColorRGB color) {
		Entity result = new Entity("Mesh");

		PoseComponent pose = new PoseComponent();
		result.addComponent(pose);
		pose.setPosition(new Vector3d(-r/2,0,0));
		pose.setScale(new Vector3d(r,v*2,v*2));

		MaterialComponent material = new MaterialComponent();
		result.addComponent(material);
		material.setDiffuseColor(color.red/255.0f, color.green/255.0f, color.blue/255.0f,1.0);

		result.addComponent(new Box());

		return result;
	}

	private Entity createCylinder(double r,double v,ColorRGB color) {
		Entity result = new Entity("Mesh");

		PoseComponent pose = new PoseComponent();
		result.addComponent(pose);
		pose.setScale(new Vector3d(v*2,v*2,r));

		MaterialComponent material = new MaterialComponent();
		result.addComponent(material);
		material.setDiffuseColor(color.red/255.0f, color.green/255.0f, color.blue/255.0f,1.0);

		result.addComponent(new Cylinder());

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

		return mesh;
	}

	private Entity createPoseEntity(String name) {
		Entity result = new Entity(name);
		result.addComponent(new PoseComponent());
		return result;
	}
}
