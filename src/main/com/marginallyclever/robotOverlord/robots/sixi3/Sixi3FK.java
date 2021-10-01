package com.marginallyclever.robotOverlord.robots.sixi3;

import java.beans.PropertyChangeEvent;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.IntersectionHelper;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.Collidable;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.shape.Mesh;
import com.marginallyclever.robotOverlord.shape.Shape;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewElementButton;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.BooleanEntity;

/**
 * Simulation of a Sixi3 robot arm with Forward Kinematics based on Denavit Hartenberg parameters.
 * It manages a set of {@link Sixi3Bone}.
 * @see <a href='https://en.wikipedia.org/wiki/Denavit%E2%80%93Hartenberg_parameters'>DH parameters</a>
 * @see <a href='https://en.wikipedia.org/wiki/Forward_kinematics'>Forward Kinematics</a>
 * @author Dan Royer
 * @since 2021-02-24
 */
public class Sixi3FK extends PoseEntity implements Collidable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2436924907127292890L;
	
	// unmoving model of the robot base.
	transient private Shape base;
	// DH parameters, meshes, physical limits.
	private ArrayList<Sixi3Bone> bones = new ArrayList<Sixi3Bone>();
	// visualize rotations?
	public BooleanEntity showAngles = new BooleanEntity("Show Angles",false);
	public BooleanEntity showEndEffector = new BooleanEntity("Show End Effector",true);
	
	public Sixi3FK() {
		super();
		setName("Sixi3FK");
		
		setupModel();
		
		for(int i=0;i<bones.size();++i) {
			Sixi3Bone b = bones.get(i);
			b.updateMatrix();
		}
	}

	/**
	 * Set up the DH link hierarchy according to the DH parameters.  Also load the shapes.  
	 * The physical location of the shapes does not match the DH linkage description of the robot, 
	 * so adjust the {@link Sixi3Bone.shapeOffset} of each bone to compensate.
	 */
	private void setupModel() {
		// load the base shape.
		base = new Shape("Base","/Sixi3b/base.3mf");
		bones.clear();
		// name d r a t max min file
		addBone("X", 8.01,     0,270,  0,170    ,-170   ,"/Sixi3b/j0.3mf");
		addBone("Y",9.131,17.889,  0,270,270+100,270-100,"/Sixi3b/j1.3mf");
		addBone("Z",    0,12.435,  0,  0,0+150  ,0-150  ,"/Sixi3b/j2.3mf");
		addBone("U",    0,     0,270,270,270+170,270-170,"/Sixi3b/j3.3mf");
		addBone("V", 5.12,     0,  0,180,360    ,0      ,"/Sixi3b/j4.3mf");
		//addBone("end effector",     0, 5.12,  0,  0,350,10,"");
		
		adjustModelOriginsToDHLinks();
	}
	
	// Use the cumulative pose of each Sixi3Bone to adjust the model origins.
	private void adjustModelOriginsToDHLinks() {
		Matrix4d current = new Matrix4d();
		current.setIdentity();
		for( Sixi3Bone bone : bones ) {
			bone.updateMatrix();
			current.mul(bone.getPose());
			Matrix4d iWP = new Matrix4d(current);
			iWP.invert();			
			bone.getShape().setPose(iWP);
		}
	}

	private void addBone(String name, double d, double r, double a, double t, double jMax, double jMin, String modelFilename) {
		Sixi3Bone b = new Sixi3Bone();
		b.set(name,d,r,a,t,jMax,jMin,modelFilename);
		b.setName(name);
		bones.add(b);
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, pose);
			drawMeshes(gl2);
			drawExtras(gl2);
		gl2.glPopMatrix();
		
		super.render(gl2);
	}
	
	private void drawMeshes(GL2 gl2) {
		base.render(gl2);

		gl2.glPushMatrix();
		for( Sixi3Bone bone : bones ) {
			// draw model with local shape offset
			bone.updateMatrix();
			MatrixHelper.applyMatrix(gl2, bone.getPose());
			bone.getShape().render(gl2);
		}
		gl2.glPopMatrix();
	}
	
	private void drawExtras(GL2 gl2) {
		// turn of textures so lines draw good
		boolean wasTex = gl2.glIsEnabled(GL2.GL_TEXTURE_2D);
		gl2.glDisable(GL2.GL_TEXTURE_2D);
		// turn off lighting so lines draw good
		boolean wasLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_LIGHTING);
		// draw on top of everything else
		int wasOver=OpenGLHelper.drawAtopEverythingStart(gl2);
		
		if(showLineage.get()) drawLineage(gl2);
		if(showAngles.get()) drawAngles(gl2);
		// bounding boxes are always relative to base?
		if(showBoundingBox.get()) drawBoundindBoxes(gl2);
		
		if(showEndEffector.get()) {
			Matrix4d m = getEndEffector();
			MatrixHelper.drawMatrix2(gl2, m, 6);
		}

		// return state if needed
		OpenGLHelper.drawAtopEverythingEnd(gl2,wasOver);
		if(wasLit) gl2.glEnable(GL2.GL_LIGHTING);
		if(wasTex) gl2.glEnable(GL2.GL_TEXTURE_2D);	
	}
	
	private void drawBoundindBoxes(GL2 gl2) {
		boolean hit = collidesWithSelf();
		if(hit) gl2.glColor3d(1, 0, 0);
		else    gl2.glColor3d(1, 1, 1);

		Matrix4d w = getPoseWorld();
		w.invert();
		
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, w);
			ArrayList<Cuboid> list = getCuboidList();
			for(Cuboid c : list) {
				gl2.glPushMatrix();
				c.getPose(w);
				MatrixHelper.applyMatrix(gl2, w);
				PrimitiveSolids.drawBoxWireframe(gl2, c.getBoundsBottom(),c.getBoundsTop());
				gl2.glPopMatrix();
			}
		gl2.glPopMatrix();
	}

	private void drawAngles(GL2 gl2) {
		boolean cullOn = gl2.glIsEnabled(GL2.GL_CULL_FACE);
		gl2.glDisable(GL2.GL_CULL_FACE);
		
		gl2.glPushMatrix();
			int j = bones.size()+1;
			for( Sixi3Bone bone : bones ) {
				bone.updateMatrix();
				double bmin = bone.getAngleMin();
				double bmax = bone.getAngleMax();
				double color = Math.abs(0.5-MathHelper.getUnitInRange(bmin,bmax,bone.theta));
				// curve of movement
				gl2.glColor4d(1,1-color,1-color,0.6);
				gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				gl2.glVertex3d(0, 0, 0);
				double diff = bone.theta-180;
				double end = Math.abs(diff);
				double dir = diff>0?1:-1;
				double radius = j;
				for(double a = 0; a<end;a+=5) {
					double s = Math.sin(Math.toRadians(-a*dir)) * radius;
					double c = Math.cos(Math.toRadians(-a*dir)) * radius;
					gl2.glVertex3d(s, c,0);
				}
				gl2.glEnd();

				MatrixHelper.applyMatrix(gl2, bone.getPose());
				--j;
			}
		gl2.glPopMatrix();

		if(cullOn) gl2.glEnable(GL2.GL_CULL_FACE);
	}

	private void drawLineage(GL2 gl2) {
		Vector3d v = new Vector3d();
		// then the bones, overtop and unlit.
		gl2.glPushMatrix();
			int j = bones.size()+1;
			for( Sixi3Bone bone : bones ) {
				bone.updateMatrix();
				// draw bone origin
				PrimitiveSolids.drawStar(gl2,j*2);
				// draw line to next bone
				bone.getPose().get(v);
				gl2.glColor3d(1, 0, 1);
				gl2.glBegin(GL2.GL_LINES);
				gl2.glVertex3d(0, 0, 0);
				gl2.glVertex3d(v.x,v.y,v.z);
				gl2.glEnd();

				MatrixHelper.applyMatrix(gl2, bone.getPose());
				--j;
			}
		gl2.glPopMatrix();
	}

	/**
	 * Find the current end effector pose, relative to the base of this robot
	 * @param m where to store the end effector pose.
	 */
	public Matrix4d getEndEffector() {
		Matrix4d m = new Matrix4d();
		m.setIdentity();
		for( Sixi3Bone bone : bones ) {
			m.mul(bone.getPose());
		}
		return m;
	}

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("FK","Forward Kinematics");
		ViewElementButton button = view.addButton("Center all");
		button.addPropertyChangeListener((evt)-> {
			double [] v = new double[bones.size()]; 
			for(int i=0;i<bones.size();++i) {
				Sixi3Bone b = bones.get(i);
				v[i]=b.getAngleMiddle();
			}
			setAngles(v);
		});
		view.add(showAngles);
		view.add(showEndEffector);
		view.popStack();
		
		super.getView(view);
	}
	
	/**
	 * 
	 * @param list where to collect the information.  Must be {@link Sixi3FK#NUM_BONES} long.
	 * @throws InvalidParameterException
	 */
	public double [] getAngles() {
		double [] list = new double[bones.size()];
		
		int i=0;
		for( Sixi3Bone bone : bones ) {
			list[i++] = bone.theta;
		}
		return list;
	}
	
	/**
	 * Update the theta angles of each bone in the robot and the FK sliders on the panel.
	 * It does not allow you to set the angle of a bone outside the angleMax/angleMin of that bone.
	 * The new values will be tested against the bone limits.
	 * If the arm moves to a new position a {@link PropertyChangeEvent} notice will be fired.  The
	 * {@link PropertyChangeEvent.propertyName} will be "ee".
	 * @param list new theta values.  Must be {@link Sixi3FK.getNumBones()} long.
	 */
	public void setAngles(double [] list) {
		boolean changed=false;
		
		int i=0;
		for( Sixi3Bone b : bones ) {
			double v = list[i++];
			double t = b.getTheta();
			b.setAngleWRTLimits(v);
			if( t != b.getTheta() )
				changed=true;
		}

		if(changed) {
			// theta values actually changed so update matrixes and get the new end effector position.
			Matrix4d eeOld = getEndEffector();
			for( Sixi3Bone b : bones ) {
				b.updateMatrix();
			}
			Matrix4d eeNew = getEndEffector();

			notifyPropertyChangeListeners(new PropertyChangeEvent(this,"ee",eeOld,eeNew));
		}
	}
	
	@Override
	public ArrayList<Cuboid> getCuboidList() {
		ArrayList<Cuboid> list = new ArrayList<Cuboid>();

		// current pose starts as root pose
		Matrix4d currentBonePose = getPoseWorld();
		Matrix4d cuboidAfter = new Matrix4d();

		list.addAll(base.getCuboidList());
		for(Cuboid c : list) {
			// add current pose to cube pose and save that as cube pose.
			cuboidAfter.mul(currentBonePose,base.getPose());
			c.setPose(cuboidAfter);
		}
		
		for( Sixi3Bone bone : bones ) {
			// add bone to current pose
			currentBonePose.mul(bone.getPose());
			
			ArrayList<Cuboid> list2 = bone.getShape().getCuboidList();
			for(Cuboid c : list2) {
				Cuboid nc = new Cuboid();
				nc.set(c);
				// add current pose to cube pose and save that as cube pose.
				cuboidAfter.mul(currentBonePose,bone.getShape().getPose());
				nc.setPose(cuboidAfter);
				list.add(nc);
			}
		}
		
		return list;
	}
	
	/**
	 * @return true if this robot currently collide with itself.
	 */
	public boolean collidesWithSelf() {
		ArrayList<Cuboid> list = getCuboidList();
		
		for(int i=0;i<list.size();++i) {
			Cuboid a = list.get(i);
			for(int j=i+2;j<list.size();++j) {
				Cuboid b = list.get(j);
				if(IntersectionHelper.cuboidCuboid(a, b)) {
					Matrix4d ma = new Matrix4d();
					Matrix4d mb = new Matrix4d();
					a.getPose(ma);
					b.getPose(mb);
					Mesh sa = a.getShape();
					Mesh sb = b.getShape();
					
					return IntersectionHelper.meshMesh(ma,sa,mb,sb);
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		String angles = "";
		String add="";
		for( Sixi3Bone b : bones ) {
			angles += add + Double.toString(b.theta);
			add=",";
		}
		return "Sixi3FK {"+angles+"}";
	}

	public boolean fromString(String s) {
		final String header="Sixi3FK {";
		if(!s.startsWith(header )) return false;
		
		// strip "Sixi3FK {" and "}" from either end.
		s=s.substring(header.length(),s.length()-1);
		// split by comma
		String [] pieces = s.split(",");
		if(pieces.length != bones.size()) return false;
		
		double [] v = new double[bones.size()];
		for(int i=0; i<bones.size(); ++i) {
			v[i] = Double.parseDouble(pieces[i]);
		}
		
		setAngles(v);
		
		return true;
	}
	
	public int getNumBones() {
		return bones.size();
	}
	
	public Sixi3Bone getBone(int i) {
		return bones.get(i);
	}
	
	/**
	 * Measures the difference between the latest end effector matrix and the target matrix.
	 * It is a combination of the linear distance and the rotation distance (collectively known as the Twist)
	 * @return the error term.
	 */
	public double getDistanceToTarget(final Matrix4d target) {
		double [] d = MatrixHelper.getCartesianBetweenTwoMatrixes(getEndEffector(), target);
		double sum=0;
		for(int i=0;i<d.length;++i) sum+=d[i];
		return sum;
	}
}
