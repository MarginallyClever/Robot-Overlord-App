package com.marginallyclever.robotOverlord.entity.scene.recording;

import java.util.Iterator;

/**
 * A single track in a multi-track sequence.  A track has a name and a list of events.
 * @author aggra
 *
 */
public class RecordingTrackDouble extends AbstractRecordingTrack {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public double defaultValue; 

	/**
	 * Constructor.
	 * @param name
	 * @param defaultValue value returned when getValueAt() is called and there is no data on the track.
	 */
	public RecordingTrackDouble(String name,double defaultValue) {
		super(name);
		this.defaultValue=defaultValue;
	}

	public AbstractRecordingEvent addKey(long time,double value) {
		RecordingEvent<Double> re = new RecordingEvent<Double>(time,value);
		int index=0;
		for(AbstractRecordingEvent a : events) {
			if(a.time>=time) {
				events.add(index, re);
				return re;
			}
		}
		events.addLast(re);
		return re;
	}
	
	// find nearest key[0] before or at time.
	// find nearest key[1] after time.
	public AbstractRecordingEvent [] getKeysNearest(long time) {
		if(events.isEmpty()) return null;
		
		// time cannot be less than zero.
		if(time<0) time=0;

		AbstractRecordingEvent [] keys = {null,null};
		
		Iterator<AbstractRecordingEvent> iter = events.iterator();
		AbstractRecordingEvent a=null;
		while(iter.hasNext()) {
			a = iter.next();
			if(a.time<= time) {
				keys[0] = a;
			} else {
				break;
			}
		}
		// iter is at or after time.
		if(a!=null) {
			// if there are no keys after this, a is the last key.
			// if a is already past time, a is the first key after time.
			if(a.time>time || !iter.hasNext()) {
				keys[1]=a;
			} else {
				// we are exactly at time and there is another key after this.
				keys[1]=iter.next();
			}
		}
		
		return keys;
	}
	
	public double getValueAt(long time) {
		if(events.isEmpty()) return defaultValue;
		// time cannot be less than zero.
		if(time<0) time=0;
		
		AbstractRecordingEvent [] keys = getKeysNearest(time);
		
		if(keys[0]==null || keys[1]==null) return defaultValue;
		
		// Linear interpolate.
		// TODO interpolate better later.
		long t10 = keys[1].time - keys[0].time;
		if(t10==0) t10=1;
		
		long tv0 = time - keys[0].time;
		double f = (double)tv0 / (double)t10;
		f = Math.min(f, 1);  // limit f [0...1]
		
		@SuppressWarnings("unchecked")
		double v1 = ((RecordingEvent<Double>)keys[1]).value; 
		@SuppressWarnings("unchecked")
		double v0 = ((RecordingEvent<Double>)keys[0]).value; 

		double v = (v1 - v0) * f + v0;
		
		return v;
	}
	
	void setValueAt(long time,double newValue) {
		// time cannot be less than zero.
		if(time<0) time=0;

		if(events.size()==0) {
			// no keys, add one at this time
			addKey(time,newValue);
		}

		// is there a key at exactly this time?
		for( AbstractRecordingEvent a : events ) {
			if(a.time== time) {
				// yes!
				@SuppressWarnings("unchecked")
				RecordingEvent<Double> a2 = (RecordingEvent<Double>)a;
				if(a2.value!=newValue) {
					setChanged();
					a2.value = newValue;
					notifyObservers();
				}
				return;
			}
		}
		// no
		addKey(time,newValue);
	}
}
