package com.marginallyclever.robotOverlord.entity.scene.recording;

/**
 * Events on a RecordingTrack.
 * @author Dan Royer
 *
 * @param <T> type of data on this track.
 */
public class RecordingEvent<T> extends AbstractRecordingEvent implements Comparable<RecordingEvent<T>> {
	public T value;
	
	public RecordingEvent() {
		super(0);
	}

	public RecordingEvent(long time,T value) {
		super(time);
		this.value=value;
	}

	@Override
	public int compareTo(RecordingEvent<T> arg0) {
		return (int)(arg0.time - this.time);
	}
}
