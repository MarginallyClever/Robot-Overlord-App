package com.marginallyclever.robotOverlord.dhRobot.robots;

import java.util.LinkedList;

import javax.vecmath.Matrix4d;


public class Matrix4dInterpolator {
	// targetMatrix = (endMatrix - startMatrix) * interpolatePoseT + startMatrix
	protected Matrix4d targetMatrix;
	
	// Time over which to interpolate.  Should reach endMatrix when interpolatePoseT=interpolateTime;
	protected double timeTotal;
	// How far into the interpolation have we reached.
	protected double timeSoFar;
	
	public class InterpolationStep {
		public Matrix4d target;
		public double time;
		
		public void set(InterpolationStep b) {
			target.set(b.target);
			time = b.time;
		}
	};
	protected LinkedList<InterpolationStep> steps;
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
		timeSoFar = 0;
		timeTotal = 0;
		steps = new LinkedList<InterpolationStep>();
	}

	public void set(Matrix4dInterpolator b) {
		// assumes everything provided by linkDescription
		start.set(b.start);
		end.set(b.end);
		targetMatrix.set(b.targetMatrix);
		timeSoFar = b.timeSoFar;
		timeTotal = b.timeTotal;
		steps.addAll(b.steps);
	}
	
	public void offer(Matrix4d target,double time) {
		InterpolationStep s = new InterpolationStep();
		s.target = target;
		s.time = time;
		steps.offer(s);
	}
	
	public boolean isInterpolating() {
		return !steps.isEmpty() || ( timeSoFar >= 0 && timeSoFar < timeTotal);
	}
	
	public void update(double dt) {
		if(timeSoFar < timeTotal) {
			//System.out.println(
			//		"Interpolating "+queue.size()
			//		+" @ "+StringHelper.formatDouble(interpolatePoseT)
			//		+" / "+StringHelper.formatDouble(interpolateTime));
			timeSoFar += dt;
		}
		
		if (timeSoFar >= timeTotal) {
			if (steps.isEmpty()) {
				// will reach final position and stop.
				timeSoFar = timeTotal;
				//System.out.println("Interpolating done.");
			} else {
				//System.out.println(queue.size()+" keys remain.  Next!");
				
				start=end;
				end=steps.poll();
				if(start==null) start=end;
				
				timeSoFar=0;
				timeTotal = end.time;
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
		return timeTotal;
	}

	public double getInterpolatePoseT() {
		return timeSoFar;
	}
	
	public int getQueueSize() {
		return steps.size();
	}
}
