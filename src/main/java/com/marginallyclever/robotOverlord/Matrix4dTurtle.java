package com.marginallyclever.robotOverlord;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;


public class Matrix4dTurtle {
	public class InterpolationStep {
		public Matrix4d targetIK;
		public double   duration;
		
		public void set(InterpolationStep b) {
			targetIK.set(b.targetIK);
			duration = b.duration;
			
		}
		public void set(Matrix4d m,double t) {
			targetIK = new Matrix4d(m);
			duration=t;
		}
		public InterpolationStep(Matrix4d m,double t) {
			targetIK = new Matrix4d(m);
			duration=t;
		}
		public InterpolationStep() {
			targetIK = new Matrix4d();
		}
	};
	protected List<InterpolationStep> steps;
	protected InterpolationStep start;
	protected InterpolationStep end;
	
	// targetMatrix = (endMatrix - startMatrix) * timeSoFar/timeTotal + startMatrix
	protected Matrix4d targetMatrix;
	
	// Time of entire sequence of steps
	protected double playHead;
	protected double totalPlayTime;
	protected boolean isPlaying;
	protected double thisStepDuration;
	protected double thisStepSoFar;
	
	public Matrix4dTurtle() {
		super();
		start=new InterpolationStep();
		end=new InterpolationStep();
		reset();
	}
	
	public Matrix4dTurtle(Matrix4dTurtle b) {
		super();
		start=new InterpolationStep();
		end=new InterpolationStep();
		set(b);
	}
	
	public void reset() {
		targetMatrix = new Matrix4d();
		steps = new ArrayList<InterpolationStep>();
		playHead=0;
		totalPlayTime=0;
		isPlaying=false;
	}

	public void set(Matrix4dTurtle b) {
		// assumes everything provided by linkDescription
		start.set(b.start);
		end.set(b.end);
		targetMatrix.set(b.targetMatrix);
		steps.addAll(b.steps);
		playHead=b.playHead;
		totalPlayTime=b.totalPlayTime;
		isPlaying=b.isPlaying;
	}
	
	public void offer(Matrix4d start,Matrix4d end,double duration) {
		if(isPlaying()) return;
		
		InterpolationStep nextStep = new InterpolationStep();
		nextStep.targetIK = end;
		nextStep.duration = duration;
		steps.add(nextStep);
		
		totalPlayTime+=duration;
	}
	
	public boolean isInterpolating() {
		return ( playHead < totalPlayTime );
	}
	
	/**
	 * 
	 * @param t
	 * @return if steps null, return null.  else return InterpolationStep closest to t.
	 */
	public InterpolationStep getStepAtTime(double t) {
		if(steps.isEmpty()) return null;
		
		for( InterpolationStep step : steps) {
			if(t<=step.duration) return step;
			t-=step.duration;
		}
		return steps.get(steps.size()-1);
	}
	
	public void update(double dt,Matrix4d poseNow) {
		if(!isPlaying()) return;
		
		playHead+=dt;
		if(playHead>totalPlayTime) playHead=totalPlayTime;
		System.out.println("playing "+playHead+"/"+totalPlayTime);

		thisStepSoFar+=dt;
		if(thisStepSoFar>=thisStepDuration) {
			thisStepSoFar-=thisStepDuration;

			InterpolationStep step = getStepAtTime(playHead);
			start.set(poseNow,0);
			end=step;
			thisStepDuration=end.duration;
		}
	}

	public void render(GL2 gl2) {
		if(steps.size()<2) return;
		
		Vector3d last=null;
		for(InterpolationStep step : steps ) {
			MatrixHelper.drawMatrix(gl2, step.targetIK, 2);
			Vector3d curr = MatrixHelper.getPosition(step.targetIK);
			if(last!=null) {
				gl2.glColor3d(1, 1, 1);
				gl2.glBegin(GL2.GL_LINES);
				gl2.glVertex3d(last.x, last.y, last.z);
				gl2.glVertex3d(curr.x, curr.y, curr.z);
				gl2.glEnd();
			}
			last=curr;
		}
		Vector3d curr = MatrixHelper.getPosition(steps.get(0).targetIK);
		if(last!=null) {
			gl2.glColor3d(1, 1, 1);
			gl2.glBegin(GL2.GL_LINES);
			gl2.glVertex3d(last.x, last.y, last.z);
			gl2.glVertex3d(curr.x, curr.y, curr.z);
			gl2.glEnd();
		}
	}
	
	public Matrix4d getStartMatrix() {
		if(start==null) return null;
		return start.targetIK;
	}

	public Matrix4d getEndMatrix() {
		if(end==null) return null;
		return end.targetIK;
	}

	public double getTotalPlayTime() {
		return totalPlayTime;
	}

	public double getPlayHead() {
		return playHead;
	}
	public void setPlayhead(double t) {
		playHead=t;
	}
	
	public int getQueueSize() {
		return steps.size();
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	public void setPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;
	}

	public double getStepDuration() {
		return thisStepDuration;
	}

	public double getStepSoFar() {
		return thisStepSoFar;
	}
}
