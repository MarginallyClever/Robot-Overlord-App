package com.marginallyclever.robotOverlord.demos;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.Light;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.demos.demoAssets.Box;
import com.marginallyclever.robotOverlord.robots.robotArm.RobotArmFK;
import com.marginallyclever.robotOverlord.robots.robotArm.RobotArmIK;
import com.marginallyclever.robotOverlord.robots.robotArm.implementations.Mantis;
import com.marginallyclever.robotOverlord.robots.robotArm.implementations.Sixi2;
import com.marginallyclever.robotOverlord.robots.robotArm.implementations.Sixi3_5axis;
import com.marginallyclever.robotOverlord.robots.robotArm.implementations.Sixi3_6axis;
import com.marginallyclever.robotOverlord.robots.robotArm.implementations.Thor;
import com.marginallyclever.robotOverlord.robots.robotArm.robotArmInterface.RobotArmInterface;
import com.marginallyclever.robotOverlord.shape.Shape;

public class RobotArmsDemo implements Demo {
	@Override
	public String getName() {
		return "Robot Arms";
	}
	
	@Override
	public void execute(RobotOverlord ro) {
		ro.newScene();
		Entity sc = ro.getScene();
		
		// adjust default camera
		ro.camera.setPosition(new Vector3d(50,-50,70));
		ro.camera.lookAt(new Vector3d(0,0,-20));
		ro.camera.update(0);
		
		// add some lights
    	Light light;

		sc.addChild(light = new Light());
		light.setName("Light");
    	light.setPosition(new Vector3d(60,-60,160));
    	light.setDiffuse(1,1,1,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);
    	light.setAttenuationLinear(0.0014);
    	light.setAttenuationQuadratic(7*1e-6);
    	light.setDirectional(true);
    	
		// add some collision bounds
		Box box;
		
		sc.addChild(box = new Box());
		box.setName("Front wall");
		box.setSize(233.5,1,100);
		box.setPosition(new Vector3d(69.75,65,50));
		box.getMaterial().setDiffuseColor(0f/255f,169f/255f,255f/255f,1f);
		
		sc.addChild(box = new Box());
		box.setName("Back wall");
		box.setSize(180,1,100);
		box.setPosition(new Vector3d(-47.5,-25.5,50));
		box.setRotation(new Vector3d(0, 0, Math.toRadians(-90)));
		box.getMaterial().setDiffuseColor(0f/255f,169f/255f,255f/255f,1f);

		Shape table = new Shape("Table","/table.stl");
		sc.addChild(table);
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
	
	private void addArm(RobotArmFK arm,Vector3d p,Entity sc,RobotOverlord ro) {
		RobotArmIK s3 = new RobotArmIK(arm);
		s3.setPosition(p);
		sc.addChild(s3);
		Matrix4d m = new Matrix4d();
		
		m.setIdentity();
		m.setTranslation(new Vector3d(10,0,0));
		s3.setToolCenterPoint(m);
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
