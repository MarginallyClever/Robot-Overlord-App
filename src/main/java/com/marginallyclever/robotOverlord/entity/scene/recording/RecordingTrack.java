package com.marginallyclever.robotOverlord.entity.scene.recording;

import java.util.LinkedList;

import com.marginallyclever.robotOverlord.entity.Entity;

/**
 * A single track in a multi-track sequence.  A track has a name and a list of events.
 * @author aggra
 *
 */
public class RecordingTrack extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// name of track
	public LinkedList<AbstractRecordingEvent> events = new LinkedList<AbstractRecordingEvent>();
	
	public RecordingTrack() {
		super();
	}

	public RecordingTrack(String name) {
		super(name);
	}
}
