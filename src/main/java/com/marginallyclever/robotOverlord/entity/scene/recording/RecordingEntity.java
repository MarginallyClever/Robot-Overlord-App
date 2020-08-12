package com.marginallyclever.robotOverlord.entity.scene.recording;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.log.Log;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewElement;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * Records (or plays back) one target entity
 * 
 * @author Dan Royer
 *
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
	
	public LinkedList<RecordingTrack> trackList = new LinkedList<RecordingTrack>(); 
	
	public RecordingEntity() {
		super("Recording");
		addChild(pathToFile);
		addChild(targetName);
		addChild(validTarget);
		//addChild(keyframe);
		
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
		view.addButton("New").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				resetTrackList();
				
			}
		});;
		view.addButton("Load").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				loadTrackList();
			}
		});;
		view.addButton("Save").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				saveTrackList();
			}
		});
		view.popStack();
		super.getView(view);
	}
	
	protected void resetTrackList() {
		trackList = new LinkedList<RecordingTrack>(); 
	}
	
	protected void saveTrackList() {
		
	}
	
	protected void loadTrackList() {
		
	}
}
