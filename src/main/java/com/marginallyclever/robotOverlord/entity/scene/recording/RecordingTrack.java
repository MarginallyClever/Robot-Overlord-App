package com.marginallyclever.robotOverlord.entity.scene.recording;

import java.util.LinkedList;

/**
 * A single track in a multi-track sequence.  A track has a name and a list of events.
 * @author aggra
 *
 */
public class RecordingTrack {
	// name of track
	public String trackName;
	public LinkedList<AbstractRecordingEvent> events = new LinkedList<AbstractRecordingEvent>();
	
	public RecordingTrack() {}
}
