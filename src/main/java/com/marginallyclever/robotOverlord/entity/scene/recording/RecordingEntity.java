package com.marginallyclever.robotOverlord.entity.scene.recording;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.convenience.memento.MementoOriginator;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.log.Log;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewElementButton;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * Record and play back a set of states for any Entity that implements the MementoOriginator interface.
 * @author Dan Royer
 *
 */
public class RecordingEntity extends Entity {
	public StringEntity 		subjectEntityPath = new StringEntity("Subject","");
	protected PoseEntity 		subject;
	protected MementoOriginator originator;
	
	protected boolean 				isPlaying;
	protected double        		playHead;
	protected double        		totalPlayTime;
	protected RecordingKeyframe     playHeadEntity;
	protected Entity        		track;
	
	public RecordingEntity() {
		super("Recording");
		
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
				}
				
				Entity e = findByPath(subjectEntityPath.get());
				if( e instanceof PoseEntity && e instanceof MementoOriginator ) {
					Log.message("Start following "+e.getFullPath());
					subject = (PoseEntity)e;
					originator = (MementoOriginator)e;
				}
				stop();
				rewind();
			}
		});
	}
	
	void stop() {
		Log.message("Action:Stop");
		isPlaying=false;
	}
	
	void play() {
		Log.message("Action:Play");
		isPlaying=true;
	}
	
	void rewind() {
		Log.message("Action:Rewind");
		playHead=0;
		playHeadEntity = null;
	}
	
	@Override
	public void update(double dt) {
		super.update(dt);
		
		if(isPlaying) {
			playHead += dt;
			// recursively walk through all children
			ArrayList<Entity> kids = track.getChildren();
			double startT=0;
			double endT=0;
			RecordingKeyframe k0 = (RecordingKeyframe)kids.get(0);
			int size = kids.size();
			int i;
			for( i=1; i<size+1; ++i ) {
				RecordingKeyframe k1 = (RecordingKeyframe)kids.get(i%size);
				// we're trying to find the task on either side of the playhead.
				// when sum <= playHead and sum+t > playHead then prev is before playhead and rk is after
				endT = startT+k1.time.get();
				if(startT <= playHead && endT > playHead) {
					//rk is the child we've been looking for.
					if( playHeadEntity != k0) {
						Log.message("Playback:task "+(i-1));
						// entering this task, send command once.
						//subject.sendCommand(k0.extra.get());
						originator.setState(k0.getMemento());
						playHeadEntity = k0;
					}
					if(endT==startT) {
						// 0 time
						// TODO don't let this be possible
					}
				}
				startT=endT;
				k0=k1;
			}
			
			if(i==size+1 && playHead>=endT) {
				Log.message("Playback:End");
				stop();
				rewind();
			}
		}
	}
	
	protected void walkChildren(RecordingKeyframe node) {
		
	}
		
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
		
		if(subject==null) return;
		
		Memento m = originator.getState();
		
		for( Entity c : track.getChildren() ) {
			if( c instanceof RecordingKeyframe ) {
				originator.setState(((RecordingKeyframe)c).getMemento());
				subject.render(gl2);
			}
		}
		
		originator.setState(m);
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("R2", "Recording2");
		view.addStaticText("Choose a robot:");
		view.addEntitySelector(subjectEntityPath);
		
		view.addButton("Add Task").addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				RecordingKeyframe newTask = new RecordingKeyframe();
				track.addChild(newTask);
				((RobotOverlord)parent.getRoot()).updateEntityTree();
				newTask.setMemento(originator.getState());
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
