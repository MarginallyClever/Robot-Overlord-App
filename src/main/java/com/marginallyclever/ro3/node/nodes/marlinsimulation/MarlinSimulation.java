package com.marginallyclever.ro3.node.nodes.marlinsimulation;

import com.marginallyclever.ro3.node.NodePath;
import com.marginallyclever.ro3.node.nodes.Motor;
import com.marginallyclever.ro3.node.nodes.pose.poses.Limb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>{@link MarlinSimulation} is meant to be a 1:1 Java replica of Marlin's 'Planner' and 'Motor' classes.
 * It is used to estimate the time to draw a set of gcode commands by a robot running Marlin 3D printer firmware.</p>
 * <p>Users should call {@link #bufferLine(MarlinCoordinate, double, double)}, which will add to the {@link #queue}.  The queue
 * must not exceed <code>MarlinSettings#getInteger(PlotterSettings.BLOCK_BUFFER_SIZE)</code> items in length.  Consuming
 * items from the head of the queue</p>
 */
public class MarlinSimulation {
	private static final Logger logger = LoggerFactory.getLogger(MarlinSimulation.class);
	private final MarlinCoordinate poseNow = new MarlinCoordinate();
	private final LinkedList<MarlinSimulationBlock> queue = new LinkedList<>();
	private MarlinCoordinate previousSpeed = new MarlinCoordinate();
	private double previousSafeSpeed = 0;
	private final MarlinSettings settings;

	enum JerkType {
		CLASSIC_JERK,
		JUNCTION_DEVIATION,
		DOT_PRODUCT,
		NONE,
	};
	private final JerkType jerkType = JerkType.CLASSIC_JERK;

	// Unit vector of previous path line segment
	private final MarlinCoordinate previousNormal = new MarlinCoordinate();
	
	private double previousNominalSpeed=0;
	private double junction_deviation = 0.05;

	private final MarlinCoordinate maxJerk = new MarlinCoordinate();
	
	public MarlinSimulation(MarlinSettings settings) {
		this.settings = settings;
	}
	
	/**
	 * Add this destination to the queue and attempt to optimize travel between destinations. 
	 * @param destination destination (mm)
	 * @param feedrate (mm/s)
	 * @param acceleration (mm/s/s)
	 */
	public void bufferLine(final MarlinCoordinate destination, double feedrate, double acceleration) {
		var delta = new MarlinCoordinate();
		delta.sub(destination,poseNow);

		acceleration = Math.min(settings.getDouble(MarlinSettings.MAX_ACCELERATION), acceleration);
		
		double len = delta.length();		
		double seconds = len / feedrate;
		int segments = (int)Math.ceil(seconds * settings.getInteger(MarlinSettings.SEGMENTS_PER_SECOND));
		int maxSeg = (int)Math.ceil(len / settings.getDouble(MarlinSettings.MIN_SEGMENT_LENGTH));
		segments = Math.max(1,Math.min(maxSeg,segments));
		var deltaSegment = new MarlinCoordinate(delta);
		deltaSegment.scale(1.0/segments);
		
		var temp = new MarlinCoordinate(poseNow);
		while(--segments>0) {
			temp.add(deltaSegment);
			bufferSegment(temp,feedrate,acceleration,deltaSegment);
		}
		bufferSegment(destination,feedrate,acceleration,deltaSegment);
	}

	/**
	 * return the queue of blocks.
	 * @return the queue of blocks.
	 */
	public LinkedList<MarlinSimulationBlock> getQueue() {
		return queue;
	}

	/**
	 * add this destination to the queue and attempt to optimize travel between destinations. 
	 * @param to destination position
	 * @param feedrate velocity (mm/s)
	 * @param acceleration (mm/s/s)
	 * @param cartesianDelta move (mm)
	 */
	private void bufferSegment(final MarlinCoordinate to, final double feedrate, final double acceleration,final MarlinCoordinate cartesianDelta) {
		MarlinSimulationBlock block = new MarlinSimulationBlock(to,cartesianDelta);
		block.feedrate = feedrate;

		// zero distance?  do nothing.
		if(block.distance<=6.0/80.0) return;
		
		double inverse_secs = feedrate / block.distance;
		
		// slow down if the buffer is nearly empty.
		if( queue.size() >= 2 && queue.size() <= (settings.getInteger(MarlinSettings.BLOCK_BUFFER_SIZE)/2)-1 ) {
			long segment_time_us = Math.round(1000000.0f / inverse_secs);
			long timeDiff = settings.getInteger(MarlinSettings.MIN_SEG_TIME) - segment_time_us;
			if( timeDiff>0 ) {
				double nst = segment_time_us + Math.round(2.0 * timeDiff / queue.size());
				inverse_secs = 1000000.0 / nst;
			}
		}
		
		block.nominalSpeed = block.distance * inverse_secs;
		
		// find if speed exceeds any joint max speed.
		MarlinCoordinate currentSpeed = new MarlinCoordinate(block.delta);
		currentSpeed.scale(inverse_secs);
		double speedFactor=1.0;
		double cs;
		for(double v : currentSpeed.p ) {
			cs = Math.abs(v);
			if( cs > feedrate ) {
				speedFactor = Math.min(speedFactor, feedrate/cs);
			}
		}

		// apply speed limit
		if(speedFactor<1.0) {
			currentSpeed.scale(speedFactor);
			block.nominalSpeed *= speedFactor;
		}

		block.acceleration = acceleration;
		
		// limit jerk between moves
		double vmax_junction = switch (jerkType) {
            case CLASSIC_JERK -> classicJerk(block, currentSpeed, block.nominalSpeed);
            case JUNCTION_DEVIATION -> junctionDeviationJerk(block, block.nominalSpeed);
            case DOT_PRODUCT -> dotProductJerk(block);
            default -> block.nominalSpeed;
        };

        block.allowableSpeed = maxSpeedAllowed(-block.acceleration,settings.getDouble(MarlinSettings.MINIMUM_PLANNER_SPEED),block.distance);
		block.entrySpeedMax = vmax_junction;
		block.entrySpeed = Math.min(vmax_junction, block.allowableSpeed);
		block.nominalLength = ( block.allowableSpeed >= block.nominalSpeed );
		block.recalculate = true;
		
		previousNominalSpeed = block.nominalSpeed;
		currentSpeed.set(previousSpeed);
		
		queue.add(block);
		poseNow.set(to);
		
		recalculateAcceleration();
	}
	
	private double dotProductJerk(MarlinSimulationBlock next) { 
		double vmax_junction = next.nominalSpeed * next.normal.dot(previousNormal) * 1.1;
		vmax_junction = Math.min(vmax_junction, next.nominalSpeed);
		vmax_junction = Math.max(vmax_junction, settings.getDouble(MarlinSettings.MINIMUM_PLANNER_SPEED));
		previousNormal.set(next.normal);
		
		return vmax_junction;
	}

	private double junctionDeviationJerk(MarlinSimulationBlock next,double nominalSpeed) {
		double vmax_junction = nominalSpeed;
		// Skip first block or when previousNominalSpeed is used as a flag for homing and offset cycles.
		if (!queue.isEmpty() && previousNominalSpeed > 1e-6) {
			// Compute cosine of angle between previous and current path. (prev_unit_vec is negative)
			// NOTE: Max junction velocity is computed without sin() or acos() by trig half angle identity.
			double junction_cos_theta = -previousNormal.dot(next.normal);

			// NOTE: Computed without any expensive trig, sin() or acos(), by trig half angle identity of cos(theta).
			if (junction_cos_theta > 0.999999f) {
				// For a 0 degree acute junction, just set minimum junction speed.
				vmax_junction = settings.getDouble(MarlinSettings.MINIMUM_PLANNER_SPEED);
			} else {
				// Check for numerical round-off to avoid divide by zero.
				junction_cos_theta = Math.max(junction_cos_theta, -0.999999f); 

				// Convert delta vector to unit vector
				var junction_unit_vec = new MarlinCoordinate();
				junction_unit_vec.sub(next.normal, previousNormal);
				junction_unit_vec.normalize();
				if (junction_unit_vec.length() > 0) {
					final double junction_acceleration = limit_value_by_axis_maximum(next.acceleration,junction_unit_vec, settings.getDouble(MarlinSettings.MAX_ACCELERATION));
					// Trig half angle identity. Always positive.
					final double sin_theta_d2 = Math.sqrt(0.5 * (1.0 - junction_cos_theta));

					vmax_junction = junction_acceleration * junction_deviation * sin_theta_d2 / (1.0f - sin_theta_d2);

					if (settings.getBoolean(MarlinSettings.HANDLE_SMALL_SEGMENTS)) {
						// For small moves with >135° junction (octagon) find speed for approximate arc
						if (next.distance < 1 && junction_cos_theta < -0.7071067812f) {
							double junction_theta = Math.acos(-junction_cos_theta);
							// NOTE: junction_theta bottoms out at 0.033 which avoids divide by 0.
							double limit = (next.distance * junction_acceleration) / junction_theta;
							vmax_junction = Math.min(vmax_junction, limit);
						}

					}
				}
			}

			// Get the lowest speed
			vmax_junction = Math.min(vmax_junction, next.nominalSpeed);
			vmax_junction = Math.min(vmax_junction, previousNominalSpeed);
		} else {
			// Init entry speed to zero. Assume it starts from rest. Planner will correct
			// this later.
			vmax_junction = 0;
		}

		previousNormal.set(next.normal);

		return vmax_junction;
	}

	private double limit_value_by_axis_maximum(double max_value, MarlinCoordinate junction_unit_vec,double maxAcceleration) {
	    double limit_value = max_value;

		for(int i=0;i<MarlinCoordinate.SIZE;++i) {
			if(junction_unit_vec.p[i]!=0) {
				if(limit_value * Math.abs(junction_unit_vec.p[i]) > maxAcceleration) {
					limit_value = Math.abs( maxAcceleration / junction_unit_vec.p[i] );
				}
			}
	    }
	
	    return limit_value;
	}

	private double classicJerk(MarlinSimulationBlock next,MarlinCoordinate currentSpeed,double safeSpeed) {
		boolean limited=false;
		
		for(int i=0;i<MarlinCoordinate.SIZE;++i) {
			double jerk = Math.abs(currentSpeed.p[i]),
					maxj = maxJerk.p[i];
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
		
		double vmax_junction;
		
		if(!queue.isEmpty()) {
			// look at difference between this move and previous move
			MarlinSimulationBlock prev = queue.getLast();
			if(prev.nominalSpeed > 1e-6) {				
				vmax_junction = Math.min(next.nominalSpeed,prev.nominalSpeed);
				limited=false;

				double vFactor=0;
				double smallerSpeedFactor = vmax_junction / prev.nominalSpeed;

				for(int i=0;i<MarlinCoordinate.SIZE;++i) {
					double vExit = previousSpeed.p[i] * smallerSpeedFactor;
					double vEntry = currentSpeed.p[i];
					if(limited) {
						vExit *= vFactor;
						vEntry *= vFactor;
					}
					double jerk = (vExit > vEntry) ? ((vEntry>0 || vExit<0) ? (vExit-vEntry) : Math.max(vExit, -vEntry))
												   : ((vEntry<0 || vExit>0) ? (vEntry-vExit) : Math.max(-vExit, vEntry));
					if( jerk > maxJerk.p[i] ) {
						vFactor = maxJerk.p[i] / jerk;
						limited = true;
					}
				}
				if(limited) vmax_junction *= vFactor;
				
				double vmax_junction_threshold = vmax_junction * 0.99;
				if( previousSafeSpeed > vmax_junction_threshold && safeSpeed > vmax_junction_threshold ) {
					vmax_junction = safeSpeed;
				}
			} else {
				vmax_junction = safeSpeed;
			}
		} else {
			vmax_junction = safeSpeed;
		}

		previousSafeSpeed = safeSpeed;
		
		return vmax_junction;
	}

	private void recalculateAcceleration() {
		recalculateBackwards();
		recalculateForwards();
		recalculateTrapezoids();
	}
	
	private void recalculateBackwards() {
		MarlinSimulationBlock current;
		MarlinSimulationBlock next = null;
		Iterator<MarlinSimulationBlock> ri = queue.descendingIterator();
		while(ri.hasNext()) {
			current = ri.next();
			recalculateBackwardsBetween(current,next);
			next = current;
		}
	}
	
	private void recalculateBackwardsBetween(MarlinSimulationBlock current,MarlinSimulationBlock next) {
		double top = current.entrySpeedMax;
		if(current.entrySpeed != top || (next!=null && next.recalculate)) {
			current.entrySpeed = current.nominalLength
					? top
					: Math.min( top, maxSpeedAllowed( -current.acceleration, (next!=null? next.entrySpeed : settings.getDouble(MarlinSettings.MINIMUM_PLANNER_SPEED)), current.distance));
			current.recalculate = true;
		}
	}
	
	private void recalculateForwards() {
		MarlinSimulationBlock current;
		MarlinSimulationBlock prev = null;
        for (MarlinSimulationBlock marlinSimulationBlock : queue) {
            current = marlinSimulationBlock;
            recalculateForwardsBetween(prev, current);
            prev = current;
        }
	}
	
	private void recalculateForwardsBetween(MarlinSimulationBlock prev,MarlinSimulationBlock current) {
		if(prev==null) return;
		if(!prev.nominalLength && prev.entrySpeed < current.entrySpeed) {
			double newEntrySpeed = maxSpeedAllowed(-prev.acceleration, prev.entrySpeed, prev.distance);
			if(newEntrySpeed < current.entrySpeed) {
				current.recalculate=true;
				current.entrySpeed = newEntrySpeed;
			}
		}
	}
	
	private void recalculateTrapezoids() {
		MarlinSimulationBlock current=null;
		
		double currentEntrySpeed=0, nextEntrySpeed=0;		
		for( MarlinSimulationBlock next : queue ) {
			nextEntrySpeed = next.entrySpeed;
			if(current!=null) {
				if(current.recalculate || next.recalculate) {
					current.recalculate = true;
					if( !current.busy ) {
						recalculateTrapezoidForBlock(current, currentEntrySpeed, nextEntrySpeed);
					}
					current.recalculate = false;
				}
			}
			current = next;
			currentEntrySpeed = nextEntrySpeed;
		}
		
		if(current!=null) {
			current.recalculate = true;
			if( !current.busy ) {
				recalculateTrapezoidForBlock(current, currentEntrySpeed, settings.getDouble(MarlinSettings.MINIMUM_PLANNER_SPEED));
			}
			current.recalculate = false;
		}
	}
	
	private void recalculateTrapezoidForBlock(MarlinSimulationBlock block, double entrySpeed, double exitSpeed) {
		if( entrySpeed < settings.getDouble(MarlinSettings.MINIMUM_PLANNER_SPEED) ) entrySpeed = settings.getDouble(MarlinSettings.MINIMUM_PLANNER_SPEED);
		if( exitSpeed  < settings.getDouble(MarlinSettings.MINIMUM_PLANNER_SPEED) ) exitSpeed  = settings.getDouble(MarlinSettings.MINIMUM_PLANNER_SPEED);
		
		double accel = block.acceleration;
		double accelerateD = estimateAccelerationDistance(entrySpeed, block.nominalSpeed, accel);
		double decelerateD = estimateAccelerationDistance(block.nominalSpeed, exitSpeed, -accel);
		double cruiseRate;
		double plateauD = block.distance - accelerateD - decelerateD;
		if( plateauD < 0 ) {
			// never reaches nominal v
			double d = Math.ceil(intersectionDistance(entrySpeed, exitSpeed, accel, block.distance));
			accelerateD = Math.min(Math.max(d, 0), block.distance);
			decelerateD = 0;
			plateauD = 0;
			cruiseRate = finalRate(accel,entrySpeed,accelerateD);
		} else {
			cruiseRate = block.nominalSpeed;
		}
		block.accelerateUntilD = accelerateD;
		block.decelerateAfterD = accelerateD + plateauD;
		block.entrySpeed = entrySpeed;
		block.exitSpeed = exitSpeed;
		block.plateauD = plateauD;
		
		double accelerateT = (cruiseRate - entrySpeed) / accel;
		double decelerateT = (cruiseRate - exitSpeed) / accel;
		double nominalT = plateauD/block.nominalSpeed;

		block.accelerateUntilT = accelerateT;
		block.decelerateAfterT = accelerateT + nominalT;
		block.end_s = accelerateT + nominalT + decelerateT;
		
		if(Double.isNaN(block.end_s)) {
			logger.debug("recalculateTrapezoidSegment() Uh oh");
		}
	}
	
	private double finalRate(double acceleration, double startV, double distance) {
		return Math.sqrt( (startV*startV) + 2.0 * acceleration*distance);
	}

	/**
	 * Calculate the maximum allowable speed at this point, in order to reach 'targetVelocity' using 
	 * 'acceleration' within a given 'distance'.
	 * @param acceleration (cm/s/s)
	 * @param targetVelocity (cm/s)
	 * @param distance (cm)
	*/
	private double maxSpeedAllowed( double acceleration, double targetVelocity, double distance ) {
		return Math.sqrt( (targetVelocity*targetVelocity) - 2 * acceleration * distance );
	}
	
	// (endV^2 - startV^2) / 2a
	private double estimateAccelerationDistance(final double initialRate, final double targetRate, final double accel) {
		if(accel == 0) return 0;
		return ( (targetRate*targetRate) - (initialRate*initialRate) ) / (accel * 2.0);
	}

	private double intersectionDistance(final double startRate, final double endRate, final double accel, final double distance) {
		if(accel == 0) return 0;
		return ( 2.0 * accel * distance - (startRate*startRate) + (endRate*endRate) ) / (4.0 * accel);
	}
}
