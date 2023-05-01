package com.marginallyclever.robotoverlord.robots.skycam;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.robots.PoseEntity;
import com.marginallyclever.robotoverlord.parameters.StringParameter;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Vector3d;
import java.beans.PropertyChangeEvent;
import java.io.*;
import java.util.ArrayList;

@Deprecated
public class Skycam extends PoseEntity {
	private static final Logger logger = LoggerFactory.getLogger(Skycam.class);
	
	// the model used to systems & control (the Flyweight)
	protected transient SkycamModel model;
	// the live robot in the real world.  Controls comms with the machine.
	protected transient SkycamLive live;
	// a simulation of the motors which should match the ideal physical behavior.
	protected transient SkycamSim sim;
	
	// there's also a Skycam that the user controls directly through the GUI to set new target states.
	// in other words, the user has no direct control over the live or sim robots.
	protected transient SkycamCommand cursor;
	
	// where to save/load commands
	protected StringParameter filename = new StringParameter("Filename","");
	ArrayList<SkycamCommand> playlist = new ArrayList<>();
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
		addEntity(cursor);
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, myPose);
		
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
				logger.info("Playback queueing done @ "+StringHelper.formatTime(playTimeTotal));
				// all finished
				isPlaying=false;
				playlist.clear();
			}
		} else {
			// Here
			//if(false) queueDestination(cursor);
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
			if(model.setPosition(cursor.getPosition())) {
				// model will be the closest permitted pose
				//cursor.setPosition(model.getPosition());
			}
		}
	}

	@Deprecated
	public void getView(ComponentPanelFactory view) {
		ArrayList<FileFilter> fileFilter = new ArrayList<>();
		// supported file formats
		fileFilter.add(new FileNameExtensionFilter("Skycam", "Skycam"));

		view.addButton("Go Home").addActionEventListener((evt)->{
			model.goHome();
			sim.setPoseTo(model.getPosition());
			cursor.setPosition(model.getPosition());
		});
		view.addButton("Append").addActionEventListener((evt)-> queueDestination((SkycamCommand)cursor) );
		view.addButton("Time estimate").addActionEventListener((evt)->{
			double t = getTimeEstimate();
			logger.info("Time estimate: "+StringHelper.formatTime(t));
		});
		view.addButton("New").addActionEventListener((evt)->{
			if(isPlaying) return;
			clearAllCommands();
		});
		view.addFilename(filename,fileFilter);
		view.addButton("Save").addActionEventListener((evt)->{
			logger.info("Saving started.");
			try {
				String f = filename.get();
				if(!f.endsWith("Skycam")) {
					f+="Skycam";
					filename.set(f);
				}
				save(f);
			} catch (Exception e) {
				//e.printStackTrace();
				logger.error("Save failed.");
			}
			logger.info("Saving finished.");
		});
		view.addButton("Load").addActionEventListener((evt)->{
			if(isPlaying) return;
			logger.info("Loading started.");
			try {
				load(filename.get());
			} catch (Exception e) {
				//e.printStackTrace();
				logger.error("Load failed.");
			}
			logger.info("Loading finished.");
		});
		view.addButton("Run").addActionEventListener((evt)-> runProgram() );
		view.addButton("Stop").addActionEventListener((evt)-> stopProgram() );

		live.getView(view);
		
		model.getView(view);
		
		super.getView(view);
	}

	/**
	 * @return time in seconds to run sequence.
	 */
	public double getTimeEstimate() {
		Vector3d p = sim.getPoseNow();
		
		for( Entity child : children) {
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
		logger.info("Playback stopped.");
	}
	
	protected void runProgram() {
		if(isPlaying) return;
		playlist.clear();
		playheadLive = 0;
		playheadSim = 0;
		playTimeTotal = 0;
		// clone the playlist so that it cannot be broken while the playback is in progress.
		for( Entity c : children) {
			if( c instanceof SkycamCommand ) {
				try {
					playlist.add((SkycamCommand)((SkycamCommand)c).clone());
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		isPlaying=true;
		logger.info("Playback started.");
	}
	
	public void save(String name) throws Exception {
		int count=0;
		for( Entity c : children) {
			if( c instanceof SkycamCommand ) ++count;
		}
		logger.info("Saving "+count+" elements.");
		if(count>0) {
			FileOutputStream fout = new FileOutputStream(name);
			ObjectOutputStream stream = new ObjectOutputStream(fout);
			stream.writeChars("Skycam");
			stream.writeInt(count);
			for( Entity c : children) {
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
			logger.info("Loading "+count+" elements.");
			for(int i=0;i<count;++i) {
				SkycamCommand c = (SkycamCommand)stream.readObject();
				addEntity(c);
			}
		}
		fin.close();
	}
	
	/**
	 * Remove all SkycamCommand children.
	 */
	public void clearAllCommands() {
		ArrayList<Entity> toKeep = new ArrayList<Entity>();
		for( Entity c : children) {
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
		try {
			SkycamCommand copy = (SkycamCommand)c.clone();
			// find the original
			int i = children.indexOf(c);
			if(i==-1) i = children.size();
			// add before original or tail of queue, whichever comes first.
			addEntity(i,copy);
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			cursor.removePropertyChangeListener(this);
		
		model.setPosition(SkycamCommand.getPose());
		SkycamCommand.setPosition(model.getPosition());
		cursor = SkycamCommand;
		
		if(cursor != null)
			cursor.addPropertyChangeListener(this);
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
		
		for(int i = children.size()-1; i>=0; --i) {
			Entity c = children.get(i);
			if(c instanceof SkycamCommand) {
				setCursor((SkycamCommand)c);
				return;
			}
		}
	}
}
