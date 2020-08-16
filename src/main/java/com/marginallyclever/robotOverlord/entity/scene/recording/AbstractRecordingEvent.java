package com.marginallyclever.robotOverlord.entity.scene.recording;

/**
 * Events on a RecordingTrack.
 * @author Dan Royer
 *
 * @param <T> type of data on this track.
 */
public class AbstractRecordingEvent {
	public long time;  // ms
	
	public AbstractRecordingEvent(long time) {
		this.time = time;
	}
	public AbstractRecordingEvent() {
		this(0);
	}
}
