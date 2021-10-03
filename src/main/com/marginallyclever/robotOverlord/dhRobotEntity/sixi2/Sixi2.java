package com.marginallyclever.robotOverlord.dhRobotEntity.sixi2;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Matrix4d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.dhRobotEntity.PoseFK;
import com.marginallyclever.robotOverlord.dhRobotEntity.dhTool.DHTool;
import com.marginallyclever.robotOverlord.dhRobotEntity.dhTool.Sixi2ChuckGripper;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.StringEntity;

/**
 * Sixi2 compares the simulated position and the reported live position to determine if a collision
 * has occurred and can react from there.
 * 
 * This design is similar to a Flyweight design pattern - the extrinsic (unchanging) model is shared 
 * by the intrinsic (state-dependent) poses.  
 * 
 * Put another way, the model describes the physical limits 
 * of the robot and each PoseFK is a state somewhere inside those limits.
 * 
 * @author Dan Royer
 *
 */
@Deprecated
public class Sixi2 extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3853296509642009298L;
	// the model used to render and control (the Flyweight)
	protected transient Sixi2Model model;
	// the live robot in the real world.  Controls comms with the machine.
	protected transient Sixi2Live live;
	// a simulation of the motors which should match the ideal physical behavior.
	protected transient Sixi2Sim sim;
	
	// there's also a Sixi2 that the user controls directly through the GUI to set new target states.
	// in other words, the user has no direct control over the live or sim robots.
	protected transient Sixi2Command cursor;
	
	// where to save/load commands
	protected StringEntity filename = new StringEntity("test.sixi2");
	ArrayList<Sixi2Command> playlist = new ArrayList<Sixi2Command>();
	protected boolean isPlaying = false;
	protected transient int playheadLive;
	protected transient int playheadSim;
	protected double playTimeTotal;
	
	
	public Sixi2() {
		super("Sixi2");
		// model should begin at home position.
		model = new Sixi2Model();

		model.addTool(new Sixi2ChuckGripper());
		model.setToolIndex(0);
		
		addChild(model);
		
		// the interface to the real machine
		live = new Sixi2Live(model);
		// the interface to the simulated machine.
		sim = new Sixi2Sim(model);
		// the "hot" position the user is currently looking at, which is neither live nor sim.
		setCursor(model.createCommand());
		addChild(cursor);
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, myPose);
		
		// live machine reports
		live.render(gl2);
		// user controlled version
		model.setPoseFK(cursor.getPoseFK());
		model.setDiffuseColor(1,1,1,1);
		model.render(gl2);
		// simulation claims
		sim.render(gl2);

		gl2.glPopMatrix();

		// other stuff
		super.render(gl2);
	}
	
	@Override
	public void update(double dt) {		
		if(isPlaying) {
			playTimeTotal+=dt;
			int doneCount=0;
			
			if(live.isConnected()) {
				if(live.isReadyForCommands()) {
					if( playheadLive < playlist.size() ) {
						live.addDestination(playlist.get(playheadLive++));
					} else doneCount++;
				}
			} else doneCount++;
			
			if(sim.isReadyForCommands()) {
				if( playheadSim < playlist.size() ) {
					sim.addDestination(playlist.get(playheadSim++));
				} else doneCount++;
			}
			
			if(doneCount==2) {
				Log.message("Playback queueing done @ "+StringHelper.formatTime(playTimeTotal));
				// all finished
				isPlaying=false;
				playlist.clear();
			}
		}
		live.update(dt);
		sim.update(dt);
		
		super.update(dt);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		Object o = evt.getSource();
		if(o==cursor) {
			// model state is dirty.  Set it to cursor.poseFK, which is *pretty close* to cursor.pose
			model.setPoseFK(cursor.getPoseFK());
			// now set the new cursor IK pose
			Matrix4d m = cursor.getPoseIK();
			if(model.setPoseIK(m)) {
				cursor.setPoseFK(model.getPoseFK());
			}
		}
	}
	
	@Override
	public void getView(ViewPanel view) {
		ArrayList<FileFilter> fileFilter = new ArrayList<FileFilter>();
		// supported file formats
		fileFilter.add(new FileNameExtensionFilter("Sixi2", "sixi2"));
		
		view.pushStack("S", "Sixi");
		view.addButton("Go Home").addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				model.goHome();
				sim.setPoseTo(model.getPoseFK());
				cursor.setPoseIK(model.getPoseIK());
				cursor.setPoseFK(model.getPoseFK());
			}
		});
		view.addButton("Append").addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				queueDestination((Sixi2Command)cursor);
			}
		});
		view.addButton("Time estimate").addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				double t = getTimeEstimate();
				Log.message("Time estimate: "+StringHelper.formatTime(t));
			}
		});
		view.addFilename(filename,fileFilter);
		view.addButton("New").addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(isPlaying) return;
				clearAllCommands();

				setCursor(model.createCommand());
				addChild(cursor);
				
				((RobotOverlord)getRoot()).updateEntityTree();
			}
		});
		view.addButton("Save").addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Log.message("Saving started.");
				try {
					String f = filename.get();
					if(!f.endsWith("sixi2")) {
						f+="sixi2";
						filename.set(f);
					}
					save(f);
				} catch (Exception e) {
					//e.printStackTrace();
					Log.error("Save failed.");
				}
				Log.message("Saving finished.");
			}
		});
		view.addButton("Load").addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(isPlaying) return;
				Log.message("Loading started.");
				try {
					load(filename.get());
				} catch (Exception e) {
					//e.printStackTrace();
					Log.error("Load failed.");
				}
				Log.message("Loading finished.");
			}
		});
		view.addButton("Run").addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				runProgram();
			}
			
		});
		view.addButton("Stop").addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				stopProgram();
			}
		});
		
		view.popStack();
		
		sim.getView(view);
		live.getView(view);
		
		super.getView(view);
	}

	/**
	 * @return time in seconds to run sequence.
	 */
	public double getTimeEstimate() {
		PoseFK p = sim.getPoseNow();
		
		for( Entity child : children ) {
			if(child instanceof Sixi2Command ) {
				sim.addDestination((Sixi2Command)child);
			}
		}
		double sum=sim.getTimeRemaining();
		sim.eStop();
		
		sim.setPoseNow(p);
		
		return sum;
	}
	
	protected void stopProgram() {
		if(isPlaying) return;
		isPlaying=false;
		Log.message("Playback stopped.");
	}
	
	protected void runProgram() {
		if(isPlaying) return;
		playlist.clear();
		playheadLive = 0;
		playheadSim = 0;
		playTimeTotal = 0;
		// clone the playlist so that it cannot be broken while the playback is in progress.
		for( Entity c : children ) {
			if( c instanceof Sixi2Command ) {
				try {
					playlist.add((Sixi2Command)((Sixi2Command) c).clone());
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		isPlaying=true;
		Log.message("Playback started.");
	}
	
	public void save(String name) throws Exception {
		int count=0;
		for( Entity c : children ) {
			if( c instanceof Sixi2Command ) ++count;
		}
		Log.message("Saving "+count+" elements.");
		if(count>0) {
			FileOutputStream fout = new FileOutputStream(name);
			ObjectOutputStream stream = new ObjectOutputStream(fout);
			stream.writeChars("SIXI2");
			stream.writeInt(count);
			for( Entity c : children ) {
				if( c instanceof Sixi2Command ) {
					stream.writeObject(c);
				}
			}
			fout.flush();
			fout.close();
		}
	}
	
	public void load(String name) throws Exception {
		clearAllCommands();
		FileInputStream fin = new FileInputStream(name);
		ObjectInputStream stream = new ObjectInputStream(fin);
		if(stream.readChar()=='S' 
		&& stream.readChar()=='I'
		&& stream.readChar()=='X'
		&& stream.readChar()=='I' 
		&& stream.readChar()=='2') {
			int count = stream.readInt();
			Log.message("Loading "+count+" elements.");
			for(int i=0;i<count;++i) {
				Sixi2Command c = (Sixi2Command)stream.readObject();
				addChild(c);
			}
		}
		fin.close();
		((RobotOverlord)getRoot()).updateEntityTree();
	}
	
	/**
	 * Remove all Sixi2Command children.
	 */
	public void clearAllCommands() {
		ArrayList<Entity> toKeep = new ArrayList<Entity>();
		for( Entity c : children ) {
			if( !(c instanceof Sixi2Command ) ) {
				toKeep.add(c);
			}
		}
		children.clear();
		children.addAll(toKeep);
	}

	public Sixi2Model getModel() {
		return model;
	}
	
	/**
	 * Clone this command and add it to the queue.  If this command is already in the queue, insert the copy 
	 * immediately prior to the original.  Otherwise append it to the end of the list.
	 * @param c the command to queue.
	 */
	public void queueDestination(Sixi2Command c) {
		// clone it
		try {
			Sixi2Command copy = (Sixi2Command)c.clone();
			// find the original
			int i = children.indexOf(c);
			if(i==-1) i = children.size();
			// add before original or tail of queue, whichever comes first.
			addChild(i,copy);
			((RobotOverlord)getRoot()).updateEntityTree();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void goTo(Sixi2Command command) {
		sim.addDestination(command);
		live.addDestination(command);
	}
	
	// recursively set for all children
	@Override
	public void setShowBoundingBox(boolean arg0) {
		super.setShowBoundingBox(arg0);
		model.getLink(0).setShowBoundingBox(arg0);
	}
	
	// recursively set for all children
	@Override
	public void setShowLocalOrigin(boolean arg0) {
		super.setShowLocalOrigin(arg0);
		model.getLink(0).setShowLocalOrigin(arg0);
	}

	// recursively set for all children
	@Override
	public void setShowLineage(boolean arg0) {
		super.setShowLineage(arg0);
		model.getLink(0).setShowLineage(arg0);
	}

	/**
	 * Set the cursor of the robot to this Sixi2Command.  I don't clone the Sixi2Command so 
	 * that adjusting the cursor can move the original.
	 * @param sixi2Command the command to make into the cursor.
	 */
	public void setCursor(Sixi2Command sixi2Command) {
		if(cursor != null)
			cursor.removePropertyChangeListener(this);
		
		model.setCommand(sixi2Command);
		cursor = sixi2Command;
		
		if(cursor != null)
			cursor.addPropertyChangeListener(this);
	}

	public Sixi2Command getCursor() {
		return cursor;
	}
	
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		// model should begin at home position.
		model = new Sixi2Model();
		// the interface to the real machine
		live = new Sixi2Live(model);
		// the interface to the simulated machine.
		sim = new Sixi2Sim(model);
		
		for(int i=children.size()-1;i>=0;--i) {
			Entity c = children.get(i); 
			if(c instanceof Sixi2Command) {
				setCursor((Sixi2Command)c);
				return;
			}
		}
	}

	public DHTool findLastToolBefore(Sixi2Command sixi2Command) {
		// TODO implement
		//throw new NotImplementedException("findLastToolBefore()");
		return null;
	}
}
