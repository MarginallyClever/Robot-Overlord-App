package com.marginallyclever.robotOverlord.demos;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.Light;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.demoAssets.Box;
import com.marginallyclever.robotOverlord.robotArmInterface.RobotArmInterface;
import com.marginallyclever.robotOverlord.robots.robotArm.RobotArmFK;
import com.marginallyclever.robotOverlord.robots.robotArm.RobotArmIK;
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
    	light.lightIndex=1;
    	light.setPosition(new Vector3d(60,-60,160));
    	light.setDiffuse(1,1,1,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);
    	light.attenuationLinear.set(0.0014);
    	light.attenuationQuadratic.set(7*1e-6);
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
		//box.setSize(160,1,110);
		//box.setPosition(new Vector3d(59.5,0,-2.5));
/*
		// adjust grid
		GridEntity grid = new GridEntity();
		addChild(grid);
		grid.width.set(140);
		grid.height.set(90);
		grid.setPosition(new Vector3d(60.0,0,-0.5));
*/
    	// add a sixi robot
		//Sixi2 sixi2=new Sixi2();
		//addChild(sixi2);
		//Sixi3FK s0 = new Sixi3FK();
		//sc.addChild(s0);

		Vector3d p = new Vector3d();

		addArm(new Sixi3(),p,sc,ro);
		p.x+=50;
		addArm(new Sixi2(),p,sc,ro);
		p.x+=50;
		addArm(new Thor(),p,sc,ro);
		p.x+=50;
		addArm(new Mantis(),p,sc,ro);

		p.x=0;
		p.y=-25;
	}
	
	private void addArm(RobotArmFK arm,Vector3d p,Entity sc,RobotOverlord ro) {
		RobotArmIK s3 = new RobotArmIK(arm);
		s3.setPosition(p);
		sc.addChild(s3);
		//openControlDialog(ro,s3);
	}
	
	private void openControlDialog(RobotOverlord ro,RobotArmIK arm) {
        new Thread(new Runnable() {
            @Override
			public void run() {
            	JDialog frame = new JDialog(ro.getMainFrame(),arm.getName());
        		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        		frame.add(new RobotArmInterface(arm));
        		frame.pack();
        		frame.setVisible(true);
            }
        }).start();
	}
}
