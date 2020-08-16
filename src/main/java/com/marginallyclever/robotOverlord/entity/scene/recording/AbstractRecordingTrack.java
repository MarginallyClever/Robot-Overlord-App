package com.marginallyclever.robotOverlord.entity.scene.recording;

import java.util.LinkedList;
import com.marginallyclever.robotOverlord.entity.Entity;

/**
 * A single track in a multi-track sequence.  A track has a name and a list of events.
 * @author aggra
 *
 */
public class AbstractRecordingTrack extends Entity  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// name of track
	String name;
	// 
	public LinkedList<AbstractRecordingEvent> events = new LinkedList<AbstractRecordingEvent>();
	
	public AbstractRecordingTrack() {
		super();
	}

	public AbstractRecordingTrack(String name) {
		super(name);
	}
}
