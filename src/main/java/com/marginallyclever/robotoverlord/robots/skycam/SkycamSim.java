package com.marginallyclever.robotoverlord.robots.skycam;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Vector3d;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A simulation of a robot's movement over time.
 * @author Dan Royer
 *
 */
@Deprecated
public class SkycamSim {
	private static final Logger logger = LoggerFactory.getLogger(SkycamSim.class);

	protected SkycamModel model;
	
	// poseTo represents the desired destination. It could be null if there is none.  Roughly equivalent to SkycamLive.poseSent.
	public Vector3d poseTo;
	// poseNow is the current position.  Roughly equivalent to SkycamLive.poseReceived.
	protected Vector3d poseNow;
	
	// the sequence of poses to drive towards.
	protected LinkedList<SkycamSimSegment> queue = new LinkedList<>();
	protected boolean readyForCommands;

	public SkycamSim(SkycamModel model) {
		super();
		
		this.model = model;
		
		// I assume the simulated robot starts at the home position.
		setPoseNow(model.getPosition());
		
		readyForCommands=true;
	}

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
	}

	/**
	 * override this to change behavior of joints over time.
	 * @param seg the segment to update
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
			logger.info(a+" "+n+" "+d+" -> "+p+" / "+seg.distance + " = "+fraction+": "
					+seg.start+" + "+seg.delta+" "+seg.end_s+" / "+seg.now_s+" / "+seg.start_s+" = ");
		}
		
		// set pos = start + delta * fraction
		poseNow.set(seg.delta);
		poseNow.scale(fraction);
		poseNow.add(seg.start);
		
		if(verbose) logger.info(poseNow.toString());
	}

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
	}

	protected void setPoseNow(Vector3d poseNow) {
		this.poseNow = poseNow;
	}
}
