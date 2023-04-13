package com.marginallyclever.robotoverlord.robots.skycam;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

import javax.vecmath.Vector3d;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A simulation of a robot's movement over time.
 * @author Dan Royer
 *
 */
@Deprecated
public class SkycamSim extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6890055884739855994L;

	protected SkycamModel model;
	
	// poseTo represents the desired destination. It could be null if there is none.  Roughly equivalent to SkycamLive.poseSent.
	public Vector3d poseTo;
	// poseNow is the current position.  Roughly equivalent to SkycamLive.poseReceived.
	protected Vector3d poseNow;
	
	// the sequence of poses to drive towards.
	protected LinkedList<SkycamSimSegment> queue = new LinkedList<SkycamSimSegment>();
	protected boolean readyForCommands;

	public SkycamSim(SkycamModel model) {
		super("Skycam Sim");
		
		this.model = model;
		
		// I assume the simulated robot starts at the home position.
		setPoseNow(model.getPosition());
		
		readyForCommands=true;
	}
	
	@Override
	public void update(double dt) {
		if(!queue.isEmpty()) {
			SkycamSimSegment seg = queue.getFirst();
			seg.busy=true;
			seg.now_s+=dt;
			double diff = 0;
			if(seg.now_s > seg.end_s) {
				diff = seg.now_s-seg.end_s;
				seg.now_s = seg.end_s;
			}
			
			updatePositions(seg);
			
			if(seg.now_s== seg.end_s) {
				queue.pop();
				// make sure the remainder isn't lost.
				if(!queue.isEmpty()) {
					queue.getFirst().now_s = diff;
				}
			}
			
			readyForCommands=(queue.size()<SkycamModel.MAX_SEGMENTS); 
		}
		
		super.update(dt);
	}

	/**
	 * override this to change behavior of joints over time.
	 * @param dt
	 */
	protected void updatePositions(SkycamSimSegment seg) {
		if(poseNow==null) return;

		double dt = (seg.now_s - seg.start_s);
		
		// I need to know how much time has been spent accelerating, cruising, and decelerating in this segment.
		// acceleratingT will be in the range 0....seg.accelerateUntilT
		double acceleratingT = Math.min(dt,seg.accelerateUntilT);
		// deceleratingT will be in the range 0....(seg.end_s-seg.decelerateAfterT)
		double deceleratingT = Math.max(dt,seg.decelerateAfterT) - seg.decelerateAfterT;
		// nominalT will be in the range 0....(seg.decelerateAfterT-seg.accelerateUntilT)
		double nominalT = Math.min(Math.max(dt,seg.accelerateUntilT),seg.decelerateAfterT) - seg.accelerateUntilT;
		
		// now find the distance moved in each of those sections.
		double a = (seg.entrySpeed * acceleratingT) + (0.5 * seg.acceleration * acceleratingT*acceleratingT);
		double n = seg.nominalSpeed * nominalT;
		double d = (seg.nominalSpeed * deceleratingT) - (0.5 * seg.acceleration * deceleratingT*deceleratingT);
		double p = a+n+d;
		
		// find the fraction of the total distance travelled
		double fraction = p / seg.distance;
		fraction = Math.min(Math.max(fraction, 0), 1);
		
		boolean verbose=false;
		if(verbose) {
			Log.message(a+" "+n+" "+d+" -> "+p+" / "+seg.distance + " = "+fraction+": "
					+seg.start+" + "+seg.delta+" "+seg.end_s+" / "+seg.now_s+" / "+seg.start_s+" = ");
		}
		
		// set pos = start + delta * fraction
		poseNow.set(seg.delta);
		poseNow.scale(fraction);
		poseNow.add(seg.start);
		
		if(verbose) Log.message(poseNow.toString());
	}
	
	@Override
	public void render(GL2 gl2) {
		// draw now first so it takes precedence in the z buffers
		if(poseNow!=null) {
			model.setPosition(poseNow);
			model.setDiffuseColor(0.0f, 1.0f, 0, 1f);
			model.render(gl2);
		}
		if(poseTo!=null) {
			model.setPosition(poseTo);
			model.setDiffuseColor(0.0f, 1.0f, 0, 0.25f);
			model.render(gl2);
		}		
		super.render(gl2);
	}
	
	@Override
	public void getView(ViewPanel view) {
		super.getView(view);
	}
	
	public Vector3d getPoseTo() {
		return poseTo;
	}

	public void setPoseTo(Vector3d newPoseTo) {
		this.poseTo = newPoseTo;
	}

	public Vector3d getPoseNow() {
		return poseNow;
	}

	protected void setPoseNow(Vector3d poseNow) {
		this.poseNow = poseNow;
	}

	/**
	 * add this destination to the queue and attempt to optimize travel between destinations. 
	 * @param command
	 */
	public boolean addDestination(final SkycamCommand command) {
		setPoseTo(command.getPosition());
		double feedrate = command.feedrateSlider.get();
		double acceleration = command.accelerationSlider.get();
		
		Vector3d start = (!queue.isEmpty()) ? queue.getLast().end : poseNow;
		
		SkycamSimSegment next = new SkycamSimSegment(start,command.getPosition());
		
		// zero distance?  do nothing.
		if(next.distance==0) return true;
		
		double timeToEnd = next.distance / feedrate;

		// slow down if the buffer is nearly empty.
		if( queue.size() > 0 && queue.size() <= (SkycamModel.MAX_SEGMENTS/2)-1 ) {
			if( timeToEnd < SkycamModel.MIN_SEGMENT_TIME ) {
				timeToEnd += (SkycamModel.MIN_SEGMENT_TIME-timeToEnd)*2.0 / queue.size();
			}
		}
		
		next.nominalSpeed = next.distance / timeToEnd;
		
		// find if speed exceeds any joint max speed.
		Vector3d currentSpeed = new Vector3d(next.delta);
		double speedFactor=1.0;
		currentSpeed.scale(1.0/timeToEnd);
		
		double maxFr = SkycamModel.MAX_JOINT_FEEDRATE;
		double [] cs1 = { currentSpeed.x, currentSpeed.y, currentSpeed.z };
		for(int i=0;i< cs1.length;++i) {
			double cs = Math.abs(cs1[i]);
			if( cs > maxFr ) speedFactor = Math.min(speedFactor, maxFr/cs);
		}
		// apply speed limit
		if(speedFactor<1.0) {
			for(int i=0;i< cs1.length;++i) {
				cs1[i] *= speedFactor;
			}
			currentSpeed.scale(speedFactor);
			next.nominalSpeed *= speedFactor;
		}
		
		next.acceleration = acceleration;

		// limit jerk between moves
		double safeSpeed = next.nominalSpeed;
		boolean limited=false;
		double [] d = { next.delta.x, next.delta.y, next.delta.z };
		
		for(int i=0;i<d.length;++i) {
			double jerk = Math.abs(cs1[i]),
					maxj = SkycamModel.MAX_JERK[i];
			if( jerk > maxj ) {
				if(limited) {
					double mjerk = maxj * next.nominalSpeed;
					if( jerk * safeSpeed > mjerk ) safeSpeed = mjerk/jerk;
				} else {
					safeSpeed *= maxj / jerk;
					limited=true;
				}
			}
		}
		
		double vmax_junction = 0;
		if(queue.size()>0) { 
			// look at difference between this move and previous move
			SkycamSimSegment prev = queue.getLast();
			if(prev.nominalSpeed > 1e-6) {				
				vmax_junction = Math.min(next.nominalSpeed,prev.nominalSpeed);
				limited=false;

				double vFactor=0;
				double smallerSpeedFactor = vmax_junction / prev.nominalSpeed;

				double [] n = { prev.normal.x, prev.normal.y, prev.normal.z };
				for(int i=0;i<n.length;++i) {
					double vExit = n[i]* smallerSpeedFactor;
					double vEntry = cs1[i];
					if(limited) {
						vExit *= vFactor;
						vEntry *= vFactor;
					}
					double jerk = (vExit > vEntry) ? ((vEntry>0 || vExit<0) ? (vExit-vEntry) : Math.max(vExit, -vEntry))
												   : ((vEntry<0 || vExit>0) ? (vEntry-vExit) : Math.max(-vExit, vEntry));
					if( jerk > SkycamModel.MAX_JERK[i] ) {
						vFactor = SkycamModel.MAX_JERK[i] / jerk;
						limited = true;
					}
				}
				if(limited) {
					vmax_junction *= vFactor;
				}
				
				double vmax_junction_threshold = vmax_junction * 0.99;
				if( // previous_safe_speed > vmax_junction_threshold &&
				    safeSpeed > vmax_junction_threshold ) {
					vmax_junction = safeSpeed;
				}
			}
		} else {
			vmax_junction = safeSpeed;
		}
		
		// previous_safe_speed = safe_speed
		
		double allowableSpeed = maxSpeedAllowed(-next.accelerateUntilD,0,next.distance);
		next.entrySpeedMax = vmax_junction;
		next.entrySpeed = Math.min(vmax_junction, allowableSpeed);
		next.nominalLength = ( allowableSpeed >= next.nominalSpeed );
		next.recalculate=true;
		next.now_s=0;
		next.start_s=0;
		next.end_s=0;
		
		recalculateTrapezoidSegment(next,next.entrySpeed, 0);
		
		queue.add(next);
		
		recalculateAcceleration();
		
		return true;
	}
	
	protected void recalculateAcceleration() {
		recalculateBackwards();
		recalculateForwards();
		recalculateTrapezoids();
	}
	
	protected void recalculateBackwards() {
		SkycamSimSegment current;
		SkycamSimSegment next = null;
		Iterator<SkycamSimSegment> ri = queue.descendingIterator();
		while(ri.hasNext()) {
			current = ri.next();
			recalculateBackwardsBetween(current,next);
			next = current;
		}
	}
	
	protected void recalculateBackwardsBetween(SkycamSimSegment current,SkycamSimSegment next) {
		double top = current.entrySpeedMax;
		if(current.entrySpeed != top || (next!=null && next.recalculate)) {
			double newEntrySpeed = current.nominalLength 
					? top
					: Math.min( top, maxSpeedAllowed( -current.acceleration, (next!=null? next.entrySpeed : 0), current.distance));
			current.entrySpeed = newEntrySpeed;
			current.recalculate = true;
		}
	}
	
	protected void recalculateForwards() {
		SkycamSimSegment current;
		SkycamSimSegment prev = null;
		Iterator<SkycamSimSegment> ri = queue.iterator();
		while(ri.hasNext()) {
			current = ri.next();
			recalculateForwardsBetween(prev,current);
			prev = current;
		}
	}
	
	protected void recalculateForwardsBetween(SkycamSimSegment prev,SkycamSimSegment current) {
		if(prev==null) return;
		if(!prev.nominalLength && prev.entrySpeed < current.entrySpeed) {
			double newEntrySpeed = maxSpeedAllowed(-prev.acceleration, prev.entrySpeed, prev.distance);
			if(newEntrySpeed < current.entrySpeed) {
				current.recalculate=true;
				current.entrySpeed = newEntrySpeed;
			}
		}
	}
	
	protected void recalculateTrapezoids() {
		SkycamSimSegment current=null;
		
		boolean nextDirty;
		double currentEntrySpeed=0, nextEntrySpeed=0;
		int size = queue.size();
		
		for(int i=0;i<size;++i) {
			current = queue.get(i);
			int j = i+1;
			if(j<size) {
				SkycamSimSegment next = queue.get(i+1);
				nextEntrySpeed = next.entrySpeed;
				nextDirty = next.recalculate;
			} else {
				nextEntrySpeed=0;
				nextDirty=false;
			}
			if( current.recalculate || nextDirty ) {
				current.recalculate = true;
				if( !current.busy  ) {
					recalculateTrapezoidSegment(current, currentEntrySpeed, nextEntrySpeed);
				}
				current.recalculate = false;
			}
			current.recalculate=false;
			currentEntrySpeed = nextEntrySpeed;
		}
	}
	
	protected void recalculateTrapezoidSegment(SkycamSimSegment seg, double entrySpeed, double exitSpeed) {
		if( entrySpeed < 0 ) entrySpeed = 0;
		if( exitSpeed < 0 ) exitSpeed = 0;
		
		double accel = seg.acceleration;
		double accelerateD = Math.ceil( estimateAccelerationDistance(entrySpeed, seg.nominalSpeed, accel));
		double decelerateD = Math.floor( estimateAccelerationDistance(seg.nominalSpeed, exitSpeed, -accel));
		double plateauD = seg.distance - accelerateD - decelerateD;
		if( plateauD < 0 ) {
			double half = Math.ceil(intersectionDistance(entrySpeed, exitSpeed, accel, seg.distance));
			accelerateD = Math.min(Math.max(half, 0), seg.distance);
			plateauD = 0;
		}
		seg.accelerateUntilD = accelerateD;
		seg.decelerateAfterD = accelerateD + plateauD;
		
		double nominalT = plateauD/seg.nominalSpeed;
		
		// d = vt + 0.5att.  it's a quadratic, so t = -v +/- sqrt( v*v -2ad ) / a
		double nA = maxSpeedAllowed(-seg.acceleration,entrySpeed,accelerateD);
		double accelerateT = ( -entrySpeed + nA ) / seg.acceleration;
		
		double nD = maxSpeedAllowed(-seg.acceleration,exitSpeed,decelerateD);
		double decelerateT = ( -exitSpeed + nD ) / seg.acceleration;
		seg.accelerateUntilT = accelerateT;
		seg.decelerateAfterT = accelerateT + nominalT;
		seg.end_s = accelerateT + nominalT + decelerateT;
		seg.entrySpeed = entrySpeed;
		seg.exitSpeed = exitSpeed;
	}

	/**
	 * Calculate the maximum allowable speed at this point, in order to reach 'targetVelocity' using 
	 * 'acceleration' within a given 'distance'.
	 * @param acceleration
	 * @param targetVelocity
	 * @param distance
	*/
	protected double maxSpeedAllowed( double acceleration, double targetVelocity, double distance ) {
		return Math.sqrt( (targetVelocity*targetVelocity) - 2 * acceleration * distance );
	}
	
	protected double estimateAccelerationDistance(final double initialRate, final double targetRate, final double accel) {
		if(accel == 0) return 0;
		return ( (targetRate*targetRate) - (initialRate*initialRate) ) / (accel * 2);
	}

	protected double intersectionDistance(final double startRate, final double endRate, final double accel, final double distance) {
		if(accel == 0) return 0;
		return ( 2.0 * accel * distance - (startRate*startRate) + (endRate*endRate) ) / (4.0 * accel);
	}

	public void eStop() {
		queue.clear();
	}

	public double getTimeRemaining() {
		double sum=0;
		for( SkycamSimSegment s : queue ) {
			sum += s.end_s - s.now_s;
		}
		return sum;
	}
	
	public boolean isReadyForCommands() {
		return readyForCommands;
	}
}
