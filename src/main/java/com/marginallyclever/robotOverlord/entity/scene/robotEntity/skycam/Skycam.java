package com.marginallyclever.robotOverlord.entity.scene.robotEntity.skycam;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.log.Log;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

public class Skycam extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7308619886170592734L;
	
	// the model used to render & control (the Flyweight)
	protected transient SkycamModel model;
	// the live robot in the real world.  Controls comms with the machine.
	protected transient SkycamLive live;
	// a simulation of the motors which should match the ideal physical behavior.
	protected transient SkycamSim sim;
	
	// there's also a Skycam that the user controls directly through the GUI to set new target states.
	// in other words, the user has no direct control over the live or sim robots.
	protected transient SkycamCommand cursor;
	
	// where to save/load commands
	protected StringEntity filename = new StringEntity("");
	ArrayList<SkycamCommand> playlist = new ArrayList<SkycamCommand>();
	protected boolean isPlaying = false;
	protected transient int playheadLive;
	protected transient int playheadSim;
	protected double playTimeTotal;
	
	
	public Skycam() {
		super("Skycam");
		// model should begin at home position.
		model = new SkycamModel();
		// the interface to the real machine
		live = new SkycamLive(model);
		// the interface to the simulated machine.
		sim = new SkycamSim(model);
		// the "hot" position the user is currently looking at, which is neither live nor sim.
		setCursor(new SkycamCommand(model.getPosition(),
				SkycamModel.DEFAULT_FEEDRATE,
				SkycamModel.DEFAULT_ACCELERATION));
		addChild(cursor);
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, pose);
		
		// live machine reports
		live.render(gl2);
		// user controlled version
		model.setPosition(cursor.getPose());
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
	public void update(Observable o, Object arg) {
		if(o==cursor) {
			if(model.setPosition(cursor.getPosition())) {
				// model will be the closest permitted pose
				//cursor.setPosition(model.getPosition());
			}
		}
		super.update(o, arg);
	}
	
	@Override
	public void getView(ViewPanel view) {
		ArrayList<FileFilter> fileFilter = new ArrayList<FileFilter>();
		// supported file formats
		fileFilter.add(new FileNameExtensionFilter("Skycam", "Skycam"));
		
		view.pushStack("S", "Sixi");
		view.addButton("Go Home").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				model.goHome();
				sim.setPoseTo(model.getPosition());
				cursor.setPosition(model.getPosition());
			}
		});
		view.addButton("Append").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				queueDestination((SkycamCommand)cursor.clone());
			}
		});
		view.addButton("Time estimate").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				double t = getTimeEstimate();
				Log.message("Time estimate: "+StringHelper.formatTime(t));
			}
		});
		view.addButton("New").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				if(isPlaying) return;
				clearAllCommands();
				((RobotOverlord)getRoot()).updateEntityTree();
			}
		});
		view.addFilename(filename,fileFilter);
		view.addButton("Save").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				Log.message("Saving started.");
				try {
					String f = filename.get();
					if(!f.endsWith("Skycam")) {
						f+="Skycam";
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
		view.addButton("Load").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
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
		view.addButton("Run").addObserver(new Observer() {
			@Override
			public void update(Observable arg0, Object arg1) {
				runProgram();
			}
			
		});
		view.addButton("Stop").addObserver(new Observer() {
			@Override
			public void update(Observable arg0, Object arg1) {
				stopProgram();
			}
		});
		
		view.popStack();
		
		sim.getView(view);
		live.getView(view);
		
		model.getView(view);
		
		super.getView(view);
	}

	/**
	 * @return time in seconds to run sequence.
	 */
	public double getTimeEstimate() {
		Vector3d p = sim.getPoseNow();
		
		for( Entity child : children ) {
			if(child instanceof SkycamCommand ) {
				sim.addDestination((SkycamCommand)child);
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
			if( c instanceof SkycamCommand ) {
				playlist.add((SkycamCommand)((SkycamCommand)c).clone());
			}
		}
		isPlaying=true;
		Log.message("Playback started.");
	}
	
	public void save(String name) throws Exception {
		int count=0;
		for( Entity c : children ) {
			if( c instanceof SkycamCommand ) ++count;
		}
		Log.message("Saving "+count+" elements.");
		if(count>0) {
			FileOutputStream fout = new FileOutputStream(name);
			ObjectOutputStream stream = new ObjectOutputStream(fout);
			stream.writeChars("Skycam");
			stream.writeInt(count);
			for( Entity c : children ) {
				if( c instanceof SkycamCommand ) {
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
				SkycamCommand c = (SkycamCommand)stream.readObject();
				addChild(c);
			}
		}
		fin.close();
		((RobotOverlord)getRoot()).updateEntityTree();
	}
	
	/**
	 * Remove all SkycamCommand children.
	 */
	public void clearAllCommands() {
		ArrayList<Entity> toKeep = new ArrayList<Entity>();
		for( Entity c : children ) {
			if( !(c instanceof SkycamCommand ) ) {
				toKeep.add(c);
			}
		}
		children.clear();
		children.addAll(toKeep);
	}

	public SkycamModel getModel() {
		return model;
	}
	
	/**
	 * Clone this command and add it to the queue.  If this command is already in the queue, insert the copy 
	 * immediately prior to the original.  Otherwise append it to the end of the list.
	 * @param c the command to queue.
	 */
	public void queueDestination(SkycamCommand c) {
		// clone it
		SkycamCommand copy = (SkycamCommand)c.clone();
		// find the original
		int i = children.indexOf(c);
		if(i==-1) i = children.size();
		// add before original or tail of queue, whichever comes first.
		addChild(i,copy);
		((RobotOverlord)getRoot()).updateEntityTree();
	}

	public void goTo(SkycamCommand command) {
		sim.addDestination(command);
		live.addDestination(command);
	}

	/**
	 * Set the cursor of the robot to this SkycamCommand.  I don't clone the SkycamCommand so 
	 * that adjusting the cursor can move the original.
	 * @param SkycamCommand the command to make into the cursor.
	 */
	public void setCursor(SkycamCommand SkycamCommand) {
		if(cursor != null)
			cursor.deleteObserver(this);
		
		model.setPosition(SkycamCommand.getPose());
		SkycamCommand.setPosition(model.getPosition());
		cursor = SkycamCommand;
		
		if(cursor != null)
			cursor.addObserver(this);
	}

	public SkycamCommand getCursor() {
		return cursor;
	}
	
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		// model should begin at home position.
		model = new SkycamModel();
		// the interface to the real machine
		live = new SkycamLive(model);
		// the interface to the simulated machine.
		sim = new SkycamSim(model);
		
		for(int i=children.size()-1;i>=0;--i) {
			Entity c = children.get(i); 
			if(c instanceof SkycamCommand) {
				setCursor((SkycamCommand)c);
				return;
			}
		}
	}
}
