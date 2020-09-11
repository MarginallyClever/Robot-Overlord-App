package com.marginallyclever.robotOverlord.entity.scene.recording2;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.vecmath.Matrix4d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.PoseFK;
import com.marginallyclever.robotOverlord.log.Log;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewElementButton;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

public class Recording2Entity extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected StringEntity  subjectEntityPath = new StringEntity("Robot","");
	protected DHRobotEntity subject;
	protected PoseEntity    subjectEE;
	
	protected boolean 		isPlaying;
	protected double        playHead;
	protected double        totalPlayTime;
	protected RobotTask     playHeadEntity;
	protected Entity        track;
	
	public Recording2Entity() {
		super("Recording2");
		
		isPlaying=false;
		totalPlayTime=0;
		playHead=0;
		
		track = new Entity("Track");
		this.addChild(track);
		
		subjectEntityPath.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				if(subject!=null) {
					Log.message("Stop following "+subject.getFullPath());
					subject=null;
					subjectEE=null;
				}
				
				Entity e = findByPath(subjectEntityPath.get());
				if( e instanceof DHRobotEntity ) {
					Log.message("Start following "+e.getFullPath());
					subject = (DHRobotEntity)e;
					subjectEE = (PoseEntity)subject.findByPath("End Effector Target");
					setPoseWorld(subject.getPoseWorld());
				}
				stop();
				rewind();
			}
		});
	}

	void stop() {
		isPlaying=false;
	}
	
	void play() {
		isPlaying=true;
	}
	
	void rewind() {
		playHead=0;
		
		ArrayList<Entity> kids = track.getChildren();
		
		playHeadEntity = kids.isEmpty() ? null : (RobotTask)track.getChildren().get(0);
	}
	
	@Override
	public void update(double dt) {
		super.update(dt);
		
		if(isPlaying) {
			playHead += dt;
			// recursively walk through all children
		}
	}
	
	protected void walkChildren(RobotTask node) {
		
	}
	
	double estimateTimeBetweePoses(RobotTask A, RobotTask B) {
		if( subject == null ) return 0;
		
		PoseFK oldPose = subject.getPoseFK();
		
		subject.setPoseIK(A.getPoseWorld());
		PoseFK fkA = subject.getPoseFK();
		subject.setPoseIK(B.getPoseWorld());
		PoseFK fkB = subject.getPoseFK();
					
		subject.setPoseFK(oldPose);
		
		// find largest fk change between poses
		//int largestAxis=0;
		double largestAmount = Math.abs(fkB.fkValues[0]-fkA.fkValues[0]);
		for( int i = 1; i < fkA.fkValues.length; ++i ) {
			double dfk = Math.abs(fkB.fkValues[i]-fkA.fkValues[i]);
			if( largestAmount < dfk ) {
				largestAmount = dfk;
				//largestAxis = i;
			}
		}
		double travelTime = 0;
		//double travelTime = largestAmount / subject.getMaxV();
		
		return travelTime;
	}
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
		
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, getPose());
		for( Entity c : track.getChildren() ) {
			if( c instanceof RobotTask ) {
				RobotTask rt = (RobotTask)c;
				MatrixHelper.drawMatrix(gl2, rt.getPose(), 15);
			}
		}
		gl2.glPopMatrix();
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("R2", "Recording2");
		view.addStaticText("Choose a robot:");
		view.addEntitySelector(subjectEntityPath);
		
		view.addButton("Add Task").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				RobotTask newTask = new RobotTask();
				track.addChild(newTask);
				((RobotOverlord)parent.getRoot()).updateEntityTree();
				Matrix4d newPose = subjectEE.getPoseWorld();
				Matrix4d invParentPose = subject.getPoseWorld();
				invParentPose.invert();
				newPose.mul(invParentPose);
				newTask.setPose(newPose);
			}
		});
		
		ViewElementButton bPlay = view.addButton("►"); 
		bPlay.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				if(isPlaying) {
					stop();
					rewind();
					bPlay.setText("►");
				} else {
					play();
					bPlay.setText("■");
				}
			}
		});

		
		ViewElementButton bNew = view.addButton("New"); 
		bNew.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				stop();
				rewind();
				
				while( !track.getChildren().isEmpty() ) {
					track.getChildren().remove(0);
				}
				((RobotOverlord)parent.getRoot()).updateEntityTree();
			}
		});
		view.popStack();
		
		super.getView(view);
	}
}
