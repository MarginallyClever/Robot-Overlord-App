package com.marginallyclever.robotOverlord.entity.scene.recording;

/**
 * Events on a RecordingTrack.
 * @author Dan Royer
 *
 * @param <T> type of data on this track.
 */
public class RecordingEvent<T> extends AbstractRecordingEvent {
	public T value;
	
	public RecordingEvent() {
		super(0);
	}

	public RecordingEvent(double time,T value) {
		super(time);
		this.value=value;
	}
}
