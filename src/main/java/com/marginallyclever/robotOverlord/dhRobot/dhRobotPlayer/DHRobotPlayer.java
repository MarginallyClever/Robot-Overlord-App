package com.marginallyclever.robotOverlord.dhRobot.dhRobotPlayer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.vecmath.Matrix4d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.dhRobot.DHRobot;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.model.ModelFactory;
import com.marginallyclever.robotOverlord.modelInWorld.ModelInWorld;
import com.marginallyclever.robotOverlord.world.World;

/**
 * 
 * @author Dan Royer
 */
public class DHRobotPlayer extends ModelInWorld {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected DHRobot target;
	protected BufferedReader gcodeFile;
	protected DHRobotPlayerPanel panel;
	protected String fileToPlay;
	
	protected boolean isCycleStart,isLoop,isSingleBlock;
	
	protected ArrayList<Matrix4d> poses = new ArrayList<Matrix4d>();


	public boolean isCycleStart() {
		return isCycleStart;
	}

	public void setCycleStart(boolean isCycleStart) {
		this.isCycleStart = isCycleStart;
	}

	public boolean isLoop() {
		return isLoop;
	}

	public void setLoop(boolean isLoop) {
		this.isLoop = isLoop;
	}

	public boolean isSingleBlock() {
		return isSingleBlock;
	}

	public void setSingleBlock(boolean isSingleBlock) {
		this.isSingleBlock = isSingleBlock;
	}

	public DHRobotPlayer() {
		super();
		
		setDisplayName("DHRobotPlayer");
		isCycleStart=false;
		isLoop=false;
		isSingleBlock=false;
		
		try {
			this.model = ModelFactory.createModelFromFilename("/Sixi2/box.stl",0.1f);
			this.adjustRotation(90, 0, 0);
			this.adjustOrigin(0,0,0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
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
	

	@Override
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		
		// remove material panel
		list.remove(list.size()-1);
		// remove physical object panel
		list.remove(list.size()-1);

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
		
		if(target==null || gcodeFile==null) return;
		
		if((target.getConnection()!=null && target.isReadyToReceive()) || 
			(target.getConnection()==null && !target.isInterpolating()) ) {
			if(!isCycleStart) return;
			
			if(isSingleBlock) {
				isCycleStart=false;
				if(panel!=null) panel.buttonCycleStart.setSelected(false);
			}
			try {
				String line="";
				do {
					// read in a line
					line = gcodeFile.readLine();
					// eat blank lines
				} while(line!=null && line.trim().isEmpty());
				if(line==null) {
					// end of file!
					if(isLoop) {
						// restart
						openFileNow();
					}
				} else {
					// found a non-empty line
					target.parseGCode(line);
					poses.add(target.getTargetMatrix());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);

		if(poses.size()==0) return;

		boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_LIGHTING);
		
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2,target.getMatrix());
		
		Matrix4d p0 = poses.get(0);
		MatrixHelper.drawMatrix(gl2, p0, 1);
		for(int i=1;i<poses.size();++i) {
			Matrix4d p1 = poses.get(i);
			MatrixHelper.drawMatrix2(gl2, p1, 3);
			gl2.glColor4d(1, 1, 1, 0.75);
			gl2.glBegin(GL2.GL_LINES);
			gl2.glVertex3d(p0.m03, p0.m13, p0.m23);
			gl2.glVertex3d(p1.m03, p1.m13, p1.m23);
			gl2.glEnd();
			p0=p1;
		}
		gl2.glPopMatrix();
		
		if(isLit) gl2.glEnable(GL2.GL_LIGHTING);
	}
	
	protected void openFileNow() {
		// gcode not yet loaded
		if(fileToPlay==null) return;

		try {
			gcodeFile = new BufferedReader(new FileReader(fileToPlay));
			System.out.println("File opened.");
			poses.clear();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	protected void closeFileNow() {
		if(gcodeFile==null) return;
		try {
			gcodeFile.close();
			gcodeFile=null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		panel.buttonCycleStart.setSelected(false);
	}

	public void reset() {
		stop();
		closeFileNow();
		openFileNow();
	}
	
	public DHRobot getTarget() {
		return target;
	}
}
