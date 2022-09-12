package com.marginallyclever.robotoverlord.demos;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.LightComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.entities.BoxEntity;
import com.marginallyclever.robotoverlord.entities.ShapeEntity;
import com.marginallyclever.robotoverlord.robots.robotarm.RobotArmIK;
import com.marginallyclever.robotoverlord.robots.robotarm.implementations.*;
import com.marginallyclever.robotoverlord.robots.robotarm.robotArmInterface.RobotArmInterface;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class RobotArmsDemo implements Demo {
	@Override
	public String getName() {
		return "Robot Arms";
	}
	
	@Override
	public void execute(RobotOverlord ro) {
		Entity sc = ro.getScene();
		
		// adjust default camera
		CameraComponent camera = ro.getCamera();
		PoseComponent pose = camera.getEntity().findFirstComponent(PoseComponent.class);
		pose.setPosition(new Vector3d(50,-50,70));
		camera.lookAt(new Vector3d(0,0,-20));
		
		// add some lights
		LightComponent light;
		Entity light0 = new Entity();
		sc.addEntity(light0);
		light0.addComponent(pose = new PoseComponent());
		light0.addComponent(light = new LightComponent());
    	pose.setPosition(new Vector3d(60,-60,160));
    	light.setDiffuse(1,1,1,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);
    	light.setAttenuationLinear(0.0014);
    	light.setAttenuationQuadratic(7*1e-6);
    	light.setDirectional(true);
    	
		// add some collision bounds
		BoxEntity box;
		
		sc.addEntity(box = new BoxEntity());
		box.setName("Front wall");
		box.setSize(233.5,1,100);
		box.setPosition(new Vector3d(69.75,65,50));
		box.getMaterial().setDiffuseColor(0f/255f,169f/255f,255f/255f,1f);
		
		sc.addEntity(box = new BoxEntity());
		box.setName("Back wall");
		box.setSize(180,1,100);
		box.setPosition(new Vector3d(-47.5,-25.5,50));
		box.setRotation(new Vector3d(0, 0, Math.toRadians(-90)));
		box.getMaterial().setDiffuseColor(0f/255f,169f/255f,255f/255f,1f);

		ShapeEntity table = new ShapeEntity("Table","/table.stl");
		sc.addEntity(table);
		table.setPosition(new Vector3d(0,0,-0.75));
		table.getMaterial().setDiffuseColor(255f/255f,192f/255f,128f/255f,1f);

		// add robots
		Vector3d p = new Vector3d();

		addArm(new Sixi3_5axis(),p,sc,ro);
		p.x+=50;
		addArm(new Sixi3_6axis(),p,sc,ro);
		p.x+=50;
		addArm(new Sixi2(),p,sc,ro);
		p.x+=50;
		addArm(new Thor(),p,sc,ro);
		p.x+=50;
		addArm(new Mantis(),p,sc,ro);
		
		//p.x=0;
		//p.y=-25;
		//addArm(new Sixi1(),p,sc,ro);
	}
	
	private void addArm(RobotArmIK arm,Vector3d p,Entity sc,RobotOverlord ro) {
		arm.setPosition(p);
		sc.addEntity(arm);
		
		//Matrix4d m = new Matrix4d();
		//m.setIdentity();
		//m.setTranslation(new Vector3d(10,0,0));
		//s3.setToolCenterPoint(m);
		//openControlDialog(ro,s3);
	}
	
	@SuppressWarnings("unused")
	private void openControlDialog(RobotOverlord ro,RobotArmIK arm) {
        new Thread(new Runnable() {
            @Override
			public void run() {
            	RobotArmInterface i = new RobotArmInterface(arm); 
            	JDialog frame = new JDialog(ro.getMainFrame(),arm.getName());
        		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        		frame.add(i);
        		frame.pack();
        		frame.addWindowListener(new WindowAdapter() {
        			@Override
        			public void windowClosing(WindowEvent e) {
        				i.closeConnection();
        			}
        		});
        		frame.setVisible(true);
            }
        }).start();
	}
}
