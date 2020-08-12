package com.marginallyclever.robotOverlord.entity.scene.recording;

import java.util.LinkedList;

public class RecordingTrack {
	// name of track
	public String trackName;
	public LinkedList<AbstractRecordingEvent> events = new LinkedList<AbstractRecordingEvent>();
	public String extraCommands;
	
	public RecordingTrack() {}
}
