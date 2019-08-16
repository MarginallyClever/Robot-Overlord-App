package com.marginallyclever.robotOverlord.dhRobot.dhRobotPlayer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.dhRobot.DHRobot;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.world.World;

/**
 * 
 * @author Dan Royer
 */
public class DHRobotPlayer extends Entity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected DHRobot target;
	protected BufferedReader gcodeFile;
	protected DHRobotPlayerPanel panel;
	protected String fileToPlay;
	
	protected String getFileToPlay() {
		return fileToPlay;
	}


	protected void setFileToPlay(String arg0) {
		if(fileToPlay==null || !fileToPlay.equals(arg0)) {
			this.fileToPlay = arg0;
			
			if(gcodeFile!=null) {
				try {
					gcodeFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				gcodeFile=null;
			}
		}
	}


	public DHRobotPlayer() {
		super();
		
		setDisplayName("DHRobot_Player");
	}
	

	@Override
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		
		panel = new DHRobotPlayerPanel(gui,this);
		list.add(panel);
		
		return list;
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
		if(fileToPlay==null) return;

		try {
			gcodeFile = new BufferedReader(new FileReader(fileToPlay));
			System.out.println("File opened.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
	}
}
