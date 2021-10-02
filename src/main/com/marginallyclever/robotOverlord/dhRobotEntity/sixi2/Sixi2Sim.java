package com.marginallyclever.robotOverlord.dhRobotEntity.sixi2;

import java.util.Iterator;
import java.util.LinkedList;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.dhRobotEntity.DHRobotModel;
import com.marginallyclever.robotOverlord.dhRobotEntity.PoseFK;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * A simulation of a robot's movement over time.
 * @author Dan Royer
 *
 */
@Deprecated
public class Sixi2Sim extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8618746034128255505L;

	protected DHRobotModel model;
	
	// poseTo represents the desired destination. It could be null if there is none.  Roughly equivalent to Sixi2Live.poseSent.
	public PoseFK poseTo;
	// poseNow is the current position.  Roughly equivalent to Sixi2Live.poseReceived.
	protected PoseFK poseNow;
	
	// the sequence of poses to drive towards.
	protected LinkedList<Sixi2SimSegment> queue = new LinkedList<Sixi2SimSegment>();
	protected boolean readyForCommands;
	protected double previousSafeSpeed = 0;

	public Sixi2Sim(DHRobotModel model) {
		super("Sixi2 Sim");
		
		this.model = model;
		
		// I assume the simulated robot starts at the home position.
		setPoseNow(model.getPoseFK());
		
		readyForCommands=true;
	}
	
	@Override
	public void update(double dt) {
		if(!queue.isEmpty()) {
			Sixi2SimSegment seg = queue.getFirst();
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
			
			readyForCommands=(queue.size()<Sixi2Model.MAX_SEGMENTS); 
		}
		
		super.update(dt);
	}

	/**
	 * override this to change behavior of joints over time.
	 * @param dt
	 */
	protected void updatePositions(Sixi2SimSegment seg) {
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
			System.out.print(a+" "+n+" "+d+" -> "+p+" / "+seg.distance + " = "+fraction+": ");
			for(int i=0;i<poseNow.fkValues.length;++i) {
				System.out.print(StringHelper.formatDouble(seg.start.fkValues[i])+" ");
			}
			System.out.print(fraction+" + ");
	
			for(int i=0;i<poseNow.fkValues.length;++i) {
				System.out.print(StringHelper.formatDouble(seg.delta.fkValues[i])+" ");
			}
			System.out.print(seg.end_s+" / "+seg.now_s+" / "+seg.start_s+" : "+fraction+" = ");
		}
		
		// set pos = start + delta * fraction
		for(int i=0;i<poseNow.fkValues.length;++i) {
			poseNow.fkValues[i] = seg.start.fkValues[i] + (seg.delta.fkValues[i]) * fraction;
			if(verbose) System.out.print(StringHelper.formatDouble(poseNow.fkValues[i])+" ");
		}
		if(verbose) System.out.println();
	}
	
	@Override
	public void render(GL2 gl2) {
		// draw now first so it takes precedence in the z buffers
		if(poseNow!=null) {
			model.setPoseFK(poseNow);
			model.setDiffuseColor(0.0f, 1.0f, 0, 1f);
			model.render(gl2);
		}
		if(poseTo!=null) {
			model.setPoseFK(poseTo);
			model.setDiffuseColor(0.0f, 1.0f, 0, 0.25f);
			model.render(gl2);
		}		
		super.render(gl2);
	}
	
	@Override
	public void getView(ViewPanel view) {
		super.getView(view);
	}
	
	public PoseFK getPoseTo() {
		return poseTo;
	}

	public void setPoseTo(PoseFK newPoseTo) {
		this.poseTo = newPoseTo;
	}

	public PoseFK getPoseNow() {
		return poseNow;
	}

	protected void setPoseNow(PoseFK poseNow) {
		this.poseNow = poseNow;
	}

	/**
	 * add this destination to the queue and attempt to optimize travel between destinations. 
	 * @param command
	 */
	public boolean addDestination(final Sixi2Command command) {
		setPoseTo(command.poseFK);
		double feedrate = command.feedrateSlider.get();
		double acceleration = command.accelerationSlider.get();
		
		PoseFK start = (!queue.isEmpty()) ? queue.getLast().end : poseNow;
		
		Sixi2SimSegment next = new Sixi2SimSegment(start,command.poseFK);
		
		// zero distance?  do nothing.
		if(next.distance==0) return true;
		
		double timeToEnd = next.distance / feedrate;

		// slow down if the buffer is nearly empty.
		if( queue.size() > 0 && queue.size() <= (Sixi2Model.MAX_SEGMENTS/2)-1 ) {
			if( timeToEnd < Sixi2Model.MIN_SEGMENT_TIME ) {
				timeToEnd += (Sixi2Model.MIN_SEGMENT_TIME-timeToEnd)*2.0 / queue.size();
			}
		}
		
		next.nominalSpeed = next.distance / timeToEnd;
		
		// find if speed exceeds any joint max speed.
		PoseFK currentSpeed = model.createPoseFK();
		double speedFactor=1.0;
		for(int i=0;i<currentSpeed.fkValues.length;++i) {
			currentSpeed.fkValues[i] = next.delta.fkValues[i] / timeToEnd;
			double cs = Math.abs(currentSpeed.fkValues[i]);
			double maxFr = Sixi2Model.MAX_JOINT_FEEDRATE;
			if( cs > maxFr ) speedFactor = Math.min(speedFactor, maxFr/cs);
		}
		// apply speed limit
		if(speedFactor<1.0) {
			for(int i=0;i<currentSpeed.fkValues.length;++i) {
				currentSpeed.fkValues[i]*=speedFactor;
			}
			next.nominalSpeed *= speedFactor;
		}
		
		next.acceleration = acceleration;

		// limit jerk between moves
		double safeSpeed = next.nominalSpeed;
		boolean limited=false;
		for(int i=0;i<next.delta.fkValues.length;++i) {
			double jerk = Math.abs(currentSpeed.fkValues[i]),
					maxj = Sixi2Model.MAX_JERK[i];
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
			Sixi2SimSegment prev = queue.getLast();
			if(prev.nominalSpeed > 1e-6) {				
				vmax_junction = Math.min(next.nominalSpeed,prev.nominalSpeed);
				limited=false;

				double vFactor=0;
				double smallerSpeedFactor = vmax_junction / prev.nominalSpeed;
				
				for(int i=0;i<prev.normal.fkValues.length;++i) {
					double vExit = prev.normal.fkValues[i]* smallerSpeedFactor;
					double vEntry = currentSpeed.fkValues[i];
					if(limited) {
						vExit *= vFactor;
						vEntry *= vFactor;
					}
					double jerk = (vExit > vEntry) ? ((vEntry>0 || vExit<0) ? (vExit-vEntry) : Math.max(vExit, -vEntry))
												   : ((vEntry<0 || vExit>0) ? (vEntry-vExit) : Math.max(-vExit, vEntry));
					if( jerk > Sixi2Model.MAX_JERK[i] ) {
						vFactor = Sixi2Model.MAX_JERK[i] / jerk;
						limited = true;
					}
				}
				if(limited) {
					vmax_junction *= vFactor;
				}
				
				double vmax_junction_threshold = vmax_junction * 0.99;
				if( previousSafeSpeed > vmax_junction_threshold &&
				    safeSpeed > vmax_junction_threshold ) {
					vmax_junction = safeSpeed;
				}
			}
		} else {
			vmax_junction = safeSpeed;
		}
		
		previousSafeSpeed = safeSpeed;
		
		double allowableSpeed = maxSpeedAllowed(-next.acceleration,0,next.distance);
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
		Sixi2SimSegment current;
		Sixi2SimSegment next = null;
		Iterator<Sixi2SimSegment> ri = queue.descendingIterator();
		while(ri.hasNext()) {
			current = ri.next();
			recalculateBackwardsBetween(current,next);
			next = current;
		}
	}
	
	protected void recalculateBackwardsBetween(Sixi2SimSegment current,Sixi2SimSegment next) {
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
		Sixi2SimSegment current;
		Sixi2SimSegment prev = null;
		Iterator<Sixi2SimSegment> ri = queue.iterator();
		while(ri.hasNext()) {
			current = ri.next();
			recalculateForwardsBetween(prev,current);
			prev = current;
		}
	}
	
	protected void recalculateForwardsBetween(Sixi2SimSegment prev,Sixi2SimSegment current) {
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
		Sixi2SimSegment current=null;
		
		boolean nextDirty;
		double currentEntrySpeed=0, nextEntrySpeed=0;
		int size = queue.size();
		
		for(int i=0;i<size;++i) {
			current = queue.get(i);
			int j = i+1;
			if(j<size) {
				Sixi2SimSegment next = queue.get(j);
				nextEntrySpeed = next.entrySpeed;
				nextDirty = next.recalculate;
			} else {
				nextEntrySpeed=0;
				nextDirty=false;
			}
			if( current.recalculate || nextDirty ) {
				current.recalculate = true;
				if( !current.busy ) {
					recalculateTrapezoidSegment(current, currentEntrySpeed, nextEntrySpeed);
				}
				current.recalculate = false;
			}
			current.recalculate=false;
			currentEntrySpeed = nextEntrySpeed;
		}
	}
	
	protected void recalculateTrapezoidSegment(Sixi2SimSegment seg, double entrySpeed, double exitSpeed) {
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
		for( Sixi2SimSegment s : queue ) {
			sum += s.end_s - s.now_s;
		}
		return sum;
	}
	
	public boolean isReadyForCommands() {
		return readyForCommands;
	}
}
