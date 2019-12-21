package com.marginallyclever.robotOverlord.dhRobot.robots;

import java.util.LinkedList;
import java.util.Queue;

import javax.vecmath.Matrix4d;


public class Matrix4dInterpolator {
	// targetMatrix = (endMatrix - startMatrix) * interpolatePoseT + startMatrix
	protected Matrix4d targetMatrix;
	
	// Time over which to interpolate.  Should reach endMatrix when interpolatePoseT=interpolateTime;
	protected double interpolateTime;
	// How far into the interpolation have we reached.
	protected double interpolatePoseT;
	
	public class InterpolationStep {
		public Matrix4d target;
		public double time;
		
		public void set(InterpolationStep b) {
			target.set(b.target);
			time = b.time;
		}
	};
	protected Queue<InterpolationStep> queue;
	protected InterpolationStep start;
	protected InterpolationStep end;
	
	public Matrix4dInterpolator() {
		super();
		reset();
	}
	
	public Matrix4dInterpolator(Matrix4dInterpolator b) {
		set(b);
	}
	
	public void reset() {
		targetMatrix = new Matrix4d();
		interpolatePoseT = 0;
		interpolateTime = 0;
		queue = new LinkedList<InterpolationStep>();
	}

	public void set(Matrix4dInterpolator b) {
		// assumes everything provided by linkDescription
		start.set(b.start);
		end.set(b.end);
		targetMatrix.set(b.targetMatrix);
		interpolatePoseT = b.interpolatePoseT;
		interpolateTime = b.interpolateTime;
		queue.addAll(b.queue);
	}
	
	public void offer(Matrix4d target,double time) {
		InterpolationStep s = new InterpolationStep();
		s.target = target;
		s.time = time;
		queue.offer(s);
	}
	
	public boolean isInterpolating() {
		return !queue.isEmpty() || ( interpolatePoseT >= 0 && interpolatePoseT < interpolateTime);
	}
	
	public void update(double dt) {
		if (interpolatePoseT >= interpolateTime) {
			if (queue.isEmpty()) {
				return;
			}
		}
		
		if(interpolatePoseT < interpolateTime) {
			//System.out.println(
			//		"Interpolating "+queue.size()
			//		+" @ "+StringHelper.formatDouble(interpolatePoseT)
			//		+" / "+StringHelper.formatDouble(interpolateTime));
			interpolatePoseT += dt;
		}
		
		if (interpolatePoseT >= interpolateTime) {
			if (queue.isEmpty()) {
				// will reach final position and stop.
				interpolatePoseT = interpolateTime;
				//System.out.println("Interpolating done.");
			} else {
				//System.out.println(queue.size()+" keys remain.  Next!");
				
				start=end;
				end=queue.poll();
				if(start==null) start=end;
				
				interpolateTime = end.time;
				interpolatePoseT=0;
			}
		}
	}

	public Matrix4d getStartMatrix() {
		return start.target;
	}

	public Matrix4d getEndMatrix() {
		return end.target;
	}

	public double getInterpolateTime() {
		return interpolateTime;
	}

	public double getInterpolatePoseT() {
		return interpolatePoseT;
	}
}
