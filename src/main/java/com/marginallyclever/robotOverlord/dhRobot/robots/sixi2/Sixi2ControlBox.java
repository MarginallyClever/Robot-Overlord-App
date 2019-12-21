package com.marginallyclever.robotOverlord.dhRobot.robots.sixi2;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JPanel;
import javax.vecmath.Matrix4d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.model.ModelFactory;
import com.marginallyclever.robotOverlord.modelInWorld.ModelInWorld;
import com.marginallyclever.robotOverlord.robot.RobotKeyframe;
import com.marginallyclever.robotOverlord.world.World;

/**
 * 
 * @author Dan Royer
 */
public class Sixi2ControlBox extends ModelInWorld {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected Sixi2 target;
	protected BufferedReader gcodeFile;
	protected Sixi2ControlBoxPanel panel;
	protected String fileToPlay;
	
	protected boolean isCycleStart,isLoop,isSingleBlock;
	
	protected transient LinkedList<RobotKeyframe> keyframes;
	protected transient int keyframe_index;
	protected transient float keyframe_t;
	protected transient boolean isDrawingKeyframes;
	
	public enum AnimationBehavior {
		ANIMATE_ONCE,
		ANIMATE_LOOP,
	};
	public AnimationBehavior animationBehavior;
	public double animationSpeed;
	
	protected ArrayList<Matrix4d> poses = new ArrayList<Matrix4d>();


	public boolean isCycleStart() {
		return isCycleStart;
	}

	public void setCycleStart(boolean isCycleStart) {
		this.isCycleStart = isCycleStart;
	}

	public boolean isLoop() {
		return isLoop;
	}

	public void setLoop(boolean isLoop) {
		this.isLoop = isLoop;
	}

	public boolean isSingleBlock() {
		return isSingleBlock;
	}

	public void setSingleBlock(boolean isSingleBlock) {
		this.isSingleBlock = isSingleBlock;
	}

	public Sixi2ControlBox() {
		super();
		
		setDisplayName("DHRobotPlayer");
		isCycleStart=false;
		isLoop=false;
		isSingleBlock=false;
		
		animationBehavior=AnimationBehavior.ANIMATE_LOOP;
		animationSpeed=0.0f;
		keyframes = new LinkedList<RobotKeyframe>();
		
		try {
			this.model = ModelFactory.createModelFromFilename("/Sixi2/box.stl",0.1f);
			this.adjustRotation(90, 0, 0);
			this.adjustOrigin(0,0,0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected String getFileToPlay() {
		return fileToPlay;
	}


	protected void setFileToPlay(String arg0) {
		if(fileToPlay==null || !fileToPlay.equals(arg0)) {
			this.fileToPlay = arg0;
			
			if(gcodeFile!=null) {
				try {
					gcodeFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				gcodeFile=null;
			}
		}
	}
	

	@Override
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		
		// remove material panel
		list.remove(list.size()-1);
		// remove model panel
		list.remove(list.size()-1);

		panel = new Sixi2ControlBoxPanel(gui,this);
		list.add(panel);
		
		return list;
	}

	// TODO this is trash.  if robot is deleted this link would do what, exactly?
	// should probably be a subscription model.
	protected Sixi2 findRobot() {
		Entity w = this.getParent(); 
		if(w instanceof World) {
			for(Entity e : w.getChildren() ) {
				if(e instanceof Sixi2) {
					return (Sixi2)e;
				}
			}
		}
		return null;
	}
	
	@Override
	public void update(double dt) {
		if(target==null) {
			target = findRobot();
		}
		
		if(gcodeFile==null) {
			openFileNow();
		}
		
		if(target==null || gcodeFile==null) return;
		
		if((target.getConnection()!=null && target.isReadyToReceive()) || 
			(target.getConnection()==null && !target.interpolator.isInterpolating()) ) {
			if(!isCycleStart) return;
			
			if(isSingleBlock) {
				isCycleStart=false;
				if(panel!=null) panel.buttonCycleStart.setSelected(false);
			}
			try {
				String line="";
				do {
					// read in a line
					line = gcodeFile.readLine();
					// eat blank lines
				} while(line!=null && line.trim().isEmpty());
				if(line==null) {
					// end of file!
					if(isLoop) {
						// restart
						openFileNow();
					}
				} else {
					// found a non-empty line
					target.parseGCode(line);
					poses.add(target.ghost.getEndEffectorMatrix());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);

		if(poses.size()==0) return;

		boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_LIGHTING);
		
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2,target.getMatrix());
		
		Matrix4d p0 = poses.get(0);
		MatrixHelper.drawMatrix(gl2, p0, 1);
		for(int i=1;i<poses.size();++i) {
			Matrix4d p1 = poses.get(i);
			MatrixHelper.drawMatrix2(gl2, p1, 3);
			gl2.glColor4d(1, 1, 1, 0.75);
			gl2.glBegin(GL2.GL_LINES);
			gl2.glVertex3d(p0.m03, p0.m13, p0.m23);
			gl2.glVertex3d(p1.m03, p1.m13, p1.m23);
			gl2.glEnd();
			p0=p1;
		}
		gl2.glPopMatrix();
		
		if(isLit) gl2.glEnable(GL2.GL_LIGHTING);

		
		if(isDrawingKeyframes) {
			renderKeyframes(gl2);
		}
	}

	/**
	 * Draw each of the individual keyframes and any renderInterpolation that might exist between them.
	 * @param gl2 the render context
	 */
	protected void renderKeyframes(GL2 gl2) {
		Iterator<RobotKeyframe> i = keyframes.iterator();
		RobotKeyframe current;
		RobotKeyframe next=null;
		while(i.hasNext()) {
			current = next;
			next = i.next();
			if(current != null && next!=null) {
				current.render(gl2);
				current.renderInterpolation(gl2, next);
			}
		}
		if(next!=null) {
			next.render(gl2);
			if(animationBehavior==AnimationBehavior.ANIMATE_LOOP) {
				next.renderInterpolation(gl2, keyframes.getFirst());
			}
		}
	}

	public RobotKeyframe keyframeAddNow() {
		int newIndex = keyframe_index+1;
		RobotKeyframe newKey = getKeyframeNow();
		keyframes.add(newIndex, newKey);
		keyframe_index=newIndex;
		keyframe_t=0;
		
		return newKey;
	}

	public RobotKeyframe keyframeAdd() {
		if(target==null) return null;
		RobotKeyframe newKey = target.createKeyframe();
		keyframes.add(newKey);
		keyframe_index = keyframes.size()-1;
		keyframe_t=0;
		
		return newKey;
	}
	
	public void keyframeDelete() {
		// there must always be at least one keyframe
		if(keyframes.size()<=1) return;
		
		keyframes.remove(keyframe_index);
		if(keyframe_index>0 && keyframe_index>=keyframes.size()) {
			keyframe_index = keyframes.size()-1;
		}
	}
	
	public float getKeyframeT() {
		return keyframe_t;
	}
	public void setKeyframeT(float arg0) {
		keyframe_t=Math.min(Math.max(arg0, 0),1);
	}
	
	public int getKeyframeSize() {
		return keyframes.size();
	}
	public int getKeyframeIndex() {
		return keyframe_index;
	}
	public void setKeyframeIndex(int arg0) {
		keyframe_index=Math.min(Math.max(arg0, 0),keyframes.size()-1);
	}
	
	public void saveKeyframes(String filePath) {
		try {
			 
            FileOutputStream fileOut = new FileOutputStream(filePath);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeInt(keyframes.size());
            Iterator<RobotKeyframe> i = keyframes.iterator();
            while(i.hasNext()) {
            	Object serObj = i.next();
            	objectOut.writeObject(serObj);
            }
            objectOut.close();
            System.out.println("Keyframes saved.");
 
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
	public void loadKeyframes(String filePath) {
		try {
			keyframes.clear();
			
            FileInputStream fileIn = new FileInputStream(filePath);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            int size = objectIn.readInt();
            for(int i=0;i<size;++i) {
            	keyframes.push((RobotKeyframe)objectIn.readObject());
            }
            objectIn.close();
            System.out.println("Keyframes loaded.");
 
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
	protected void openFileNow() {
		// gcode not yet loaded
		if(fileToPlay==null) return;

		try {
			gcodeFile = new BufferedReader(new FileReader(fileToPlay));
			System.out.println("File opened.");
			poses.clear();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	protected void closeFileNow() {
		if(gcodeFile==null) return;
		try {
			gcodeFile.close();
			gcodeFile=null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		panel.buttonCycleStart.setSelected(false);
	}

	public void reset() {
		stop();
		closeFileNow();
		openFileNow();
	}
	
	public Sixi2 getTarget() {
		return target;
	}

	
	public void setIsDrawingKeyframes( boolean arg0 ) {
		isDrawingKeyframes=arg0;
	}
	
	public boolean getIsDrawingKeyframes() {
		return isDrawingKeyframes;
	}


	public double getAnimationSpeed() {
		return animationSpeed;
	}

	/**
	 * Adjust animation speed and disable GUI elements (when animation speed !=0)
	 * @param animationSpeed
	 */
	public void setAnimationSpeed(double animationSpeed) {
		this.animationSpeed = animationSpeed;
		panel.keyframeEditSetEnable(animationSpeed==0);
	}
	
	protected void animate(double dt) {
		keyframe_t+=dt*animationSpeed;
		if(animationSpeed>0) {
			if(keyframe_t>1) {
				keyframe_t-=1;
				++keyframe_index;
				int size=getKeyframeSize();
				if(keyframe_index>size-1) {
					switch(animationBehavior) {
					case ANIMATE_ONCE:
						keyframe_index = size-1;
						keyframe_t=0;
						animationSpeed=0;
						// TODO set the panel buttonAnimatePlayPause to paused
						break;
					case ANIMATE_LOOP:
						keyframe_index=0;
						break;
					}
				}
			}
		} else if(animationSpeed<0) {
			if(keyframe_t<0) {
				keyframe_t+=1;
				--keyframe_index;
				if(keyframe_index<0) {
					switch(animationBehavior) {
					case ANIMATE_ONCE:
						keyframe_index = 0;
						keyframe_t=0;
						animationSpeed=0;
						// TODO set the panel buttonAnimatePlayPause to paused
						break;
					case ANIMATE_LOOP:
						keyframe_index+=getKeyframeSize();
						break;
					}
				}
			}
		}
	}
	
	public RobotKeyframe getKeyframe(int arg0) {
		return keyframes.get(arg0);
	}
	
	public void setKeyframe(int index,RobotKeyframe element) {
		keyframes.set(index, element);
	}
	
	
	private RobotKeyframe getKeyframeNow() {
		if( target==null ) return null;
		
		int size=getKeyframeSize();
		if(keyframe_index>=size-1) {
			if(animationBehavior==AnimationBehavior.ANIMATE_LOOP) {
				RobotKeyframe now = target.createKeyframe();
				RobotKeyframe a = keyframes.get(size-1);
				RobotKeyframe b = keyframes.get(0);
				now.interpolate(a, b, keyframe_t);
				return now;
			} else {
				return keyframes.get(size-1);
			}
		} else {
			RobotKeyframe now = target.createKeyframe();
			RobotKeyframe a = keyframes.get(keyframe_index);
			RobotKeyframe b = keyframes.get(keyframe_index+1);
			now.interpolate(a, b, keyframe_t);
			return now;
		}
	}
}
