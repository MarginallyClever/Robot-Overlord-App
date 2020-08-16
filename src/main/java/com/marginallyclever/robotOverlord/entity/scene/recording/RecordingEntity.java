package com.marginallyclever.robotOverlord.entity.scene.recording;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import com.marginallyclever.convenience.MatrixHelper;
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
	
	// list of tracks
	public LinkedList<RecordingTrackDouble> trackList = new LinkedList<RecordingTrackDouble>();
	
	// where is the playhead in the sequence of tracks?
	public DoubleEntity playHead = new DoubleEntity("Play head (s)",0);
	
	// what is the total sequence length?  (can be earlier than the last events)
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
						resetTrackList();
					}
				} else if(o==targetEntity) {
					// ignore targetEntity moves while in playing.
					if(!isPlaying) {
						setKeyToTarget(getPlayHeadMS());
					}
				}
			}
		});
		playHead.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				if(targetEntity!=null) {
					moveTargetToTime(getPlayHeadMS());
				}
			}
		});
	}

	public void moveTargetToTime(long time_ms) {
		if(targetEntity==null) return;
		
		PoseEntity pe = (PoseEntity)targetEntity;
		Vector3d targetPos = new Vector3d();
		Vector3d targetRot = new Vector3d();
		targetPos.x = trackList.get(0).getValueAt(time_ms);
		targetPos.y = trackList.get(1).getValueAt(time_ms);
		targetPos.z = trackList.get(2).getValueAt(time_ms);
		//targetRot.x = trackList.get(3).getValueAt(time_ms);
		//targetRot.y = trackList.get(4).getValueAt(time_ms);
		//targetRot.z = trackList.get(5).getValueAt(time_ms);
		
		//Matrix3d m3 = MatrixHelper.eulerToMatrix(targetRot);
		//Matrix4d m4 = new Matrix4d(m3,targetPos,1);
		Matrix4d m4 = pe.getPoseWorld();
		m4.setTranslation(targetPos);
		pe.setPoseWorld(m4);
		
		Log.message("Playback t="+time_ms+": "+targetPos.toString());
	}
	
	// set sequence track list events at time to target pose.
	public void setKeyToTarget(long time) {
		if(targetEntity==null) return;
		
		Matrix4d pw = targetEntity.getPoseWorld();
		Vector3d targetPos = MatrixHelper.getPosition(pw);
		Vector3d targetRot = MatrixHelper.matrixToEuler(pw);
		
		trackList.get(0).setValueAt(time,targetPos.x);
		trackList.get(1).setValueAt(time,targetPos.y);
		trackList.get(2).setValueAt(time,targetPos.z);
		trackList.get(3).setValueAt(time,targetRot.x);
		trackList.get(4).setValueAt(time,targetRot.y);
		trackList.get(5).setValueAt(time,targetRot.z);
		
		Log.message("Record "+targetEntity.getFullPath()+" @ "+time+": "+targetPos.toString());
	}
	
	public long getPlayHeadMS() {
		return (long)Math.floor(playHead.get()*1000);
	}
	
	@Override
	public void update(double dt) {
		super.update(dt);
		
		// are we playing?
		if(isPlaying) {
			// are we at the end?
			double ts = playHead.get();
			
			if(ts>=sequenceLength.get()) {
				// yes
				playHead.set(sequenceLength.get());
				stop();
				return;
			} else {
				// no
				// advance playHead
				playHead.set(ts+1.0/30.0); // 30FPS TODO improve this.
			}
		}
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Re", "Recording Entity");
		ArrayList<FileFilter> filters = new ArrayList<FileFilter>();
		filters.add( new FileNameExtensionFilter("Any","*") );
		view.addFilename(pathToFile, filters);
		
		view.add(targetName);
		view.add(playHead);
		view.add(sequenceLength);
		
		view.add(validTarget).setReadOnly(true);
		ViewElementButton toStart  = view.addButton("|◄");
		ViewElementButton keyPrev  = view.addButton("◄◄");
						  playStop = view.addButton("►");
		ViewElementButton keyNext  = view.addButton("►►");
		ViewElementButton toEnd    = view.addButton("►|");

		//ViewElementButton bAddTrack = view.addButton("Add track");
		//ViewElementButton bNew     = view.addButton("New");
		//ViewElementButton bLoad    = view.addButton("Load");
		//ViewElementButton bSave    = view.addButton("Save");

		view.popStack();
		
		toStart.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				start();
				playHead.set(0.0);
				stop();
			}
		});
		keyPrev.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				start();
				playHead.set(findEventTimeBefore(getPlayHeadMS())*0.001);
				stop();
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
				start();
				playHead.set(findEventTimeAfter(getPlayHeadMS())*0.001);
				stop();
			}
		});
		toEnd.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				start();
				playHead.set(sequenceLength.get());
				stop();
			}
		});
		/*
		bNew.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				resetTrackList();
				
			}
		});
		bLoad.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				loadTrackList();
			}
		});
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
		});*/
		super.getView(view);
	}

	/**
	 * Scan all RecordingTrack, find the AbstractRecordingEvent X where X.time &lt; t. 
	 * @param t
	 * @return X.time, as described.
	 */
	protected long findEventTimeBefore(long t) {
		long nearestTime = Long.MAX_VALUE;
		
		for( AbstractRecordingTrack track : trackList ) {
			Iterator<AbstractRecordingEvent> iter = track.events.iterator();
			while(iter.hasNext()) {
				AbstractRecordingEvent are = iter.next();
				if(are.time>=t) break;
				nearestTime = Math.max(nearestTime,t);
			}
		}
		return nearestTime;
	}
	
	/**
	 * Scan all RecordingTrack, find the AbstractRecordingEvent X where X.time &gt; t. 
	 * @param t
	 * @return X.time, as described.
	 */
	protected long findEventTimeAfter(long t) {
		long nearestTime = Long.MAX_VALUE;

		for(AbstractRecordingTrack track : trackList ) {
			Iterator<AbstractRecordingEvent> iter = track.events.descendingIterator();
			while(iter.hasNext()) {
				AbstractRecordingEvent are = iter.next();
				if(are.time<=t) break;
				nearestTime = Math.min(nearestTime,t);
			}
		}
		return nearestTime;
	}
	
	protected void stop() {
		isPlaying=false;
		if(playStop!=null) playStop.setText("►");
	}
	
	protected void start() {
		isPlaying=true;
		if(playStop!=null) playStop.setText("■");
	}
	
	protected void resetTrackList() {
		stop();
		playHead.set(0.0);
		trackList.clear();
		trackList.add(new RecordingTrackDouble("PosX",0));
		trackList.add(new RecordingTrackDouble("PosY",0));
		trackList.add(new RecordingTrackDouble("PosZ",0));
		trackList.add(new RecordingTrackDouble("RotX",0));
		trackList.add(new RecordingTrackDouble("RotY",0));
		trackList.add(new RecordingTrackDouble("RotZ",0));
		setKeyToTarget(0);
	}
	
	protected void saveTrackList() {
		stop();
		// TODO open file; write out contents; close file
	}
	
	protected void loadTrackList() {
		stop();
		// TODO open file; read in contents; close file
	}
	/*
	protected void addTrack() {
		int f = 0;
		for( Entity track : children ) {
			if(track instanceof RecordingTrack) {
				String [] parts = track.getName().split("\\s+");
				int id = Integer.parseInt(parts[1]);
				f = Math.max(f,id);
			}
		}
		f++;
		Log.message("Adding track "+f);
		RecordingTrack newTrack = new RecordingTrack("Track "+f);
		trackList.add(newTrack);
	}*/
}
//