package com.marginallyclever.robotOverlord.entity.scene.recording;

import java.io.File;

import javax.sound.midi.Track;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.log.Log;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewElementButton;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * Multitrack recording/playback for a target entity.  Child RecordingTrack entities are the tracks in the recording.
 * @author Dan Royer
 * TODO make this system undoable.
 */
public class RecordingEntity extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// where was it saved?  where was it loaded from?
	public StringEntity pathToFile = new StringEntity("File","");
	public StringEntity targetName = new StringEntity("Target","");
	public BooleanEntity validTarget = new BooleanEntity("Valid target",false);
	private PoseEntity targetEntity = null;
	
	// where is the playhead in the sequence of tracks?
	public DoubleEntity playHead = new DoubleEntity("Play head (s)",0);
	// what is the total sequence length?
	public DoubleEntity sequenceLength = new DoubleEntity("Sequence length (s)",0);
	public boolean isPlaying=false;
	
	// needed as a reference for other button actions.
	protected ViewElementButton playStop;
	
	public RecordingEntity() {
		super("Recording");
		addChild(pathToFile);
		addChild(targetName);
		addChild(validTarget);
		addChild(playHead);
		addChild(sequenceLength);
		
		targetName.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				if(o==targetName) {
					// stop watching the old entity 
					if( targetEntity != null ) {
						Log.message("Stop watching "+targetEntity.getFullPath());
						targetEntity.deleteObserver(this);
						targetEntity=null;
						validTarget.set(false);
					}
					// name has changed
					Entity e = findByPath(targetName.get());
					if(e instanceof PoseEntity) {
						// remember the new target
						targetEntity = (PoseEntity)e;
						// observe the new target
						Log.message("Start watching "+targetEntity.getFullPath());
						targetEntity.addObserver(this);
						validTarget.set(true);
					}
				} else if(o==targetEntity) {
					// target has changed, set current keyframe to the new pose.
					Log.message("Change to "+targetEntity.getFullPath());
					
				}
			}
		});
	}

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Re", "Recording Entity");
		ArrayList<FileFilter> filters = new ArrayList<FileFilter>();
		filters.add( new FileNameExtensionFilter("Any","*") );
		view.addFilename(pathToFile, filters);
		
		view.add(targetName);
		view.add(validTarget).setReadOnly(true);
		ViewElementButton toStart  = view.addButton("|◄");
		ViewElementButton keyPrev  = view.addButton("◄◄");
						  playStop = view.addButton("||");
		ViewElementButton keyNext  = view.addButton("►►");
		ViewElementButton toEnd    = view.addButton("►|");

		ViewElementButton bAddTrack = view.addButton("Add track");
		ViewElementButton bNew     = view.addButton("New");
		ViewElementButton bLoad    = view.addButton("Load");
		ViewElementButton bSave    = view.addButton("Save");

		view.popStack();
		
		toStart.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				stop();
				playHead.set(0.0);
			}
		});
		keyPrev.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				stop();
				playHead.set(findEventBefore(playHead.get()));
			}
		});
		playStop.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				if(isPlaying) stop();
				else          start();
			}
		});
		keyNext.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				stop();
				playHead.set(findEventAfter(playHead.get()));
			}
		});
		toEnd.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				stop();
				playHead.set(sequenceLength.get());
			}
		});
		
		bNew.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				resetTrackList();
				
			}
		});;
		bLoad.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				loadTrackList();
			}
		});;
		bSave.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				saveTrackList();
			}
		});
		
		bAddTrack.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				addTrack();
			}
		});
		super.getView(view);
	}

	/**
	 * Scan all RecordingTrack, find the AbstractRecordingEvent X where X.time &lt; t. 
	 * @param t
	 * @return X.time, as described.
	 */
	protected double findEventBefore(double t) {
		double nearestTime = 0.0;
		
		for(Entity track : children) {
			if(track instanceof RecordingTrack) {
				Iterator<AbstractRecordingEvent> iter = ((RecordingTrack)track).events.iterator();
				while(iter.hasNext()) {
					AbstractRecordingEvent are = iter.next();
					if(are.time>=t) break;
					nearestTime = Math.max(nearestTime,t);
				}
			}
		}
		return nearestTime;
	}
	
	/**
	 * Scan all RecordingTrack, find the AbstractRecordingEvent X where X.time &gt; t. 
	 * @param t
	 * @return X.time, as described.
	 */
	protected double findEventAfter(double t) {
		double nearestTime = Double.MAX_VALUE;

		for(Entity track : children) {
			if(track instanceof RecordingTrack) {
				Iterator<AbstractRecordingEvent> iter = ((RecordingTrack)track).events.descendingIterator();
				while(iter.hasNext()) {
					AbstractRecordingEvent are = iter.next();
					if(are.time<=t) break;
					nearestTime = Math.min(nearestTime,t);
				}
			}
		}
		return nearestTime;
	}
	
	protected void stop() {
		isPlaying=false;
		playStop.setText(isPlaying?"■":"►");
	}
	
	protected void start() {
		isPlaying=true;
		playStop.setText(isPlaying?"■":"►");
	}
	
	protected void resetTrackList() {
		stop();
		// remove all children
		while(!this.children.isEmpty()) {
			removeChild(this.children.get(0));
		}
	}
	
	protected void saveTrackList() {
		stop();
		// TODO open file; write out contents; close file
	}
	
	protected void loadTrackList() {
		stop();
		// TODO open file; read in contents; close file
	}
	
	protected void addTrack() {
		int f = 0;
		for( Entity track : children ) {
			if(track instanceof RecordingTrack) {
				String [] parts = track.getName().split("\\s+");
				int id = Integer.parseInt(parts[1]);
				f = Math.max(f,id);
			}
		}
		RecordingTrack newTrack = new RecordingTrack("Track "+(f+1));
		
		addChild(newTrack);
	}
}
