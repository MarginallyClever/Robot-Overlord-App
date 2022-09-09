package com.marginallyclever.robotoverlord.demos;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.LightComponent;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.shapes.Box;
import com.marginallyclever.robotoverlord.components.shapes.Grid;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.components.shapes.Sphere;

import javax.vecmath.Vector3d;

public class BasicDemo implements Demo {
	@Override
	public String getName() {
		return "Basic";
	}
	
	@Override
	public void execute(RobotOverlord ro) {
		ro.newScene();
		Entity scene = ro.getScene();
		
		// adjust default camera
		CameraComponent camera = scene.findFirstComponent(CameraComponent.class);
		PoseComponent pose = camera.getEntity().getComponent(PoseComponent.class);
		pose.setPosition(new Vector3d(40,-91,106));
		camera.setPan(-16);
		camera.setTilt(53);
		camera.setZoom(100);
		
		// add some lights
    	LightComponent light;
		Entity light0 = new Entity("LightB");
		scene.addEntity(light0);
		light0.addComponent(pose = new PoseComponent());
		light0.addComponent(light = new LightComponent());
    	pose.setPosition(new Vector3d(60,-60,160));
    	light.setDiffuse(0.8f,0.8f,1,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);

		light0 = new Entity("LightC");
		scene.addEntity(light0);
		light0.addComponent(pose = new PoseComponent());
		light0.addComponent(light = new LightComponent());
    	pose.setPosition(new Vector3d(-60,60,-160));
    	light.setDiffuse(1,0.8f,0.8f,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);

		Entity gridEntity = new Entity("Floor");
		MaterialComponent mat = new MaterialComponent();
		gridEntity.addComponent(pose = new PoseComponent());
		gridEntity.addComponent(mat);
		Grid grid = new Grid();
		gridEntity.addComponent(grid);
		scene.addEntity(gridEntity);
		mat.setDiffuseColor(0.5,0.5,0.5,1);
		mat.setLit(false);

		Entity boxEntity = new Entity("Box");
		boxEntity.addComponent(pose = new PoseComponent());
		Box box = new Box();
		boxEntity.addComponent(box);
		boxEntity.addComponent(new MaterialComponent());
		scene.addEntity(boxEntity);
		pose.setPosition(new Vector3d(-10,0,0));

		Entity sphereEntity = new Entity("Sphere");
		sphereEntity.addComponent(pose = new PoseComponent());
		Sphere sphere = new Sphere();
		sphereEntity.addComponent(sphere);
		sphereEntity.addComponent(new MaterialComponent());
		scene.addEntity(sphereEntity);
		pose.setPosition(new Vector3d(10,0,0));

		Entity meshEntity = new Entity("Mesh");
		meshEntity.addComponent(pose = new PoseComponent());
		MeshFromFile mesh = new MeshFromFile();
		meshEntity.addComponent(mesh);
		meshEntity.addComponent(new MaterialComponent());
		mesh.setFilename("/Sixi3b/j0.obj");
		scene.addEntity(meshEntity);
		pose.setPosition(new Vector3d(0,0,0));
	}
}
