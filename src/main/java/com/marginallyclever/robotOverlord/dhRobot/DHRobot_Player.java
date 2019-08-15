package com.marginallyclever.robotOverlord.dhRobot;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.world.World;

/**
 * 
 * @author Dan Royer
 */
public class DHRobot_Player extends Entity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected DHRobot target;
	protected BufferedReader gcodeFile;
	
	public DHRobot_Player() {
		super();
		
		setDisplayName("DHRobot_Player");
	}
	
	@Override
	public void update(double dt) {
		if(target==null) {
			// no target found.
			Entity w = this.getParent(); 
			if(w instanceof World) {
				Iterator<Entity> entities = w.getChildren().iterator();
				while(entities.hasNext()) {
					Entity e = entities.next();
					if(e instanceof DHRobot) {
						target=(DHRobot)e;
						System.out.println("Target found.");
						break;
					}
				}
			}
		}
		
		if(gcodeFile==null) {
			openFileNow();
		}
		
		if(target!=null && gcodeFile!=null) {
			if((target.getConnection()!=null && target.isReadyToReceive()) || !target.isInterpolating() ) {
				try {
					String line = gcodeFile.readLine();
					if(line==null) {
						openFileNow();
					} else {
						target.parseGCode(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	protected void openFileNow() {
		// gcode not yet loaded
		try {
			gcodeFile = new BufferedReader(new FileReader("C:\\Users\\Admin\\Desktop\\sixi2test.ngc"));
			System.out.println("File opened.");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
	}
}
