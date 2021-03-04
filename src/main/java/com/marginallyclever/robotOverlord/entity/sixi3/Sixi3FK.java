package com.marginallyclever.robotOverlord.entity.sixi3;

import java.util.ArrayList;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.scene.Collidable;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.entity.scene.shapeEntity.ShapeEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * Simulation of a Sixi3 robot arm with Forward Kinematics based on Denavit Hartenberg parameters.
 * It manages a set of {@link Sixi3Link}.
 * @see <a href='https://en.wikipedia.org/wiki/Denavit%E2%80%93Hartenberg_parameters'>DH parameters</a>
 * @see <a href='https://en.wikipedia.org/wiki/Forward_kinematics'>Forward Kinematics</a>
 * @author Dan Royer
 * @since 2021-02-24
 *
 */
public class Sixi3FK extends PoseEntity implements Collidable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2436924907127292890L;

	// measurements from Fusion360 model.
	private static final double BASE_HEIGHT=8.0;
	private static final double LENGTH_A=2.95;
	private static final double LENGTH_B=8.2564;
	private static final double CONNECTOR_HEIGHT=0.7;
	private static final double HAND_HEIGHT=1.25;
	
	//private static final double ACTUATOR_MASS = 1.061427; // kg
	//private static final double CONNECTOR_MASS = 0.085692; // kg
	//private static final double HAND_MASS = 0.114065; // kg
	
	private static final String ACTUATOR_MODEL   = "/Sixi3/actuator 2021-02-25.obj";
	private static final String BICEP_MODEL      = "/Sixi3/bicep.obj";
	private static final String HAND_MODEL       = "/Sixi3/Sixi3 Hand DIN EN ISO 9409-1-50-4-M6 v6.obj";
	private static final String BASE_MODEL       = "/Sixi3/base v7.obj";
	private static final String ACTUATOR_TEXTURE = "/Sixi3/actuator-texture.png";
	
	protected Sixi3Link [] links = new Sixi3Link[6];

	// unmoving model of the robot base.
	private ShapeEntity base;

	// end effector
	protected Matrix4d ee = new Matrix4d();

	// forward kinematics sliders
	protected DoubleEntity J0 = new DoubleEntity("J0",0);
	protected DoubleEntity J1 = new DoubleEntity("J1",0);
	protected DoubleEntity J2 = new DoubleEntity("J2",0);
	protected DoubleEntity J3 = new DoubleEntity("J3",0);
	protected DoubleEntity J4 = new DoubleEntity("J4",0);
	protected DoubleEntity J5 = new DoubleEntity("J5",0);
	
	
	public Sixi3FK() {
		super();
		setName("Sixi 3");
		
		setupModel();
		
		J0.addPropertyChangeListener(this);
		J1.addPropertyChangeListener(this);
		J2.addPropertyChangeListener(this);
		J3.addPropertyChangeListener(this);
		J4.addPropertyChangeListener(this);
		J5.addPropertyChangeListener(this);
		
		J0.set(180.0);
		J1.set(180.0);
		J2.set(180.0);
		J3.set(180.0);
		J4.set(180.0);
		J5.set(180.0);

		for( Sixi3Link bone : links ) {
			bone.updateMatrix();
		}
		
		getEndEffector(ee);
	}

	/**
	 * Set up the DH link hierarchy according to the DH parameters.  Also load the shapes.  
	 * The physical location of the shapes does not match the DH linkage description of the robot, 
	 * so adjust the {@link Sixi3Link.shapeOffset} of each bone to compensate.
	 */
	private void setupModel() {
		// memory allocation
		for( int i=0;i<links.length;++i ) {
			links[i] = new Sixi3Link();
		}
		
		// The DH parameters
		double d0=BASE_HEIGHT+LENGTH_A;
		double r2=(LENGTH_B+CONNECTOR_HEIGHT)*2;
		double d3=LENGTH_A+LENGTH_B;
		double d4=d3;
		double d5=LENGTH_A+HAND_HEIGHT;
		links[0].set(0 ,d0,-90,0,ACTUATOR_MODEL);
		links[1].set(r2, 0,  0,0,BICEP_MODEL   );
		links[2].set(0 , 0, 90,0,ACTUATOR_MODEL);
		links[3].set(0 ,d3, 90,0,ACTUATOR_MODEL);
		links[4].set(0 ,d4, 90,0,ACTUATOR_MODEL);
		links[5].set(0 ,d5,  0,0,HAND_MODEL    );

		// load the base shape.
		base = new ShapeEntity(BASE_MODEL);
		
		// adjust the shape offsets.
		Matrix4d m0 = new Matrix4d();
		Matrix4d m1 = new Matrix4d();
		Matrix4d m2 = new Matrix4d();

		m0.rotX(Math.toRadians(90));
		m1.rotY(Math.toRadians(-90));
		m2.mul(m1,m0);
		m2.m13=LENGTH_A;
		links[0].shape.setPose(m2);

		m2.setIdentity();
		m2.rotX(Math.toRadians(-90));
		m2.m03=-(LENGTH_B+CONNECTOR_HEIGHT)*2;
		m2.m23=LENGTH_B;
		links[1].shape.setPose(m2);
		
		m0.rotX(Math.toRadians(180));
		m1.rotZ(Math.toRadians(-90));
		m2.mul(m1,m0);
		m2.m23=LENGTH_A;
		links[2].shape.setPose(m2);

		m0.rotX(Math.toRadians(180));
		m1.rotZ(Math.toRadians(-90));
		m2.mul(m1,m0);
		m2.m23=LENGTH_A;
		links[3].shape.setPose(m2);

		m0.rotZ(Math.toRadians(90));
		m1.rotX(Math.toRadians(180));
		m2.mul(m1,m0);
		m2.m23=LENGTH_A;
		links[4].shape.setPose(m2);

		m2.rotZ(Math.toRadians(-90));
		m2.m23=0;
		links[5].shape.setPose(m2);
		
		// set material properties for each part of this model.
		base.getMaterial().setTextureFilename(ACTUATOR_TEXTURE);
		for( Sixi3Link bone : links ) {
			bone.shape.getMaterial().setTextureFilename(ACTUATOR_TEXTURE);
		}
	}
	
	@Override
	public void update(double dt) {
		super.update(dt);

		Matrix4d m0=new Matrix4d();
		Matrix4d m1=new Matrix4d();
		Matrix4d m2=new Matrix4d();
		m0.rotX(Math.toRadians(180));
		m1.rotZ(Math.toRadians(90));
		m2.mul(m1,m0);
		m2.m23=LENGTH_A;
		links[2].shape.setPose(m2);
		
		// update the end effector
		getEndEffector(ee);
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, pose);

			gl2.glPushMatrix();
				// draw the meshes
				drawMeshes(gl2);
				drawExtras(gl2);
			gl2.glPopMatrix();
		
			MatrixHelper.drawMatrix2(gl2, ee, 6);
		gl2.glPopMatrix();
		
		super.render(gl2);
	}
	
	private void drawMeshes(GL2 gl2) {
		base.render(gl2);

		gl2.glPushMatrix();
		for( Sixi3Link bone : links ) {
			// draw model with local shape offset
			bone.updateMatrix();
			MatrixHelper.applyMatrix(gl2, bone.pose);
			bone.shape.render(gl2);
		}
		gl2.glPopMatrix();
	}
	
	private void drawExtras(GL2 gl2) {
		Vector3d v = new Vector3d();
		
		// turn of textures so lines draw good
		boolean wasTex = gl2.glIsEnabled(GL2.GL_TEXTURE_2D);
		gl2.glDisable(GL2.GL_TEXTURE_2D);
		// turn off lighting so lines draw good
		boolean wasLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_LIGHTING);
		// draw on top of everything else
		int wasOver=OpenGLHelper.drawAtopEverythingStart(gl2);
		gl2.glPushMatrix();
		if(showLineage.get()) {
			// then the bones, overtop and unlit.
			int j=links.length+1;
			for(int i=0;i<links.length;++i) {
				Sixi3Link bone = links[i];
				bone.updateMatrix();
				// draw bone origin
				PrimitiveSolids.drawStar(gl2,j--);
	
				bone.pose.get(v);
				gl2.glColor3d(1, 0, 1);
				gl2.glBegin(GL2.GL_LINES);
				gl2.glVertex3d(0, 0, 0);
				gl2.glVertex3d(v.x,v.y,v.z);
				gl2.glEnd();
				MatrixHelper.applyMatrix(gl2, bone.pose);
			}
		}
		gl2.glPopMatrix();
		
		// bounding boxes are always relative to base?
		if(showBoundingBox.get()) {
			Matrix4d w = new Matrix4d();
			ArrayList<Cuboid> list = getCuboidList();
			for(Cuboid c : list) {
				gl2.glPushMatrix();
				c.getPose(w);
				MatrixHelper.applyMatrix(gl2, w);
				c.render(gl2);
				gl2.glPopMatrix();
			}
		}

		// return state if needed
		OpenGLHelper.drawAtopEverythingEnd(gl2,wasOver);
		if(wasLit) gl2.glEnable(GL2.GL_LIGHTING);
		if(wasTex) gl2.glEnable(GL2.GL_TEXTURE_2D);	
	}
	
	/**
	 * Find the current end effector pose, relative to the base of this robot
	 * @param m where to store the end effector pose.
	 */
	protected void getEndEffector(Matrix4d m) {
		m.setIdentity();
		for( Sixi3Link bone : links ) {
			m.mul(bone.pose);
		}
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("FK","Forward Kinematics");
		view.addRange(J0, 350, 10);
		view.addRange(J1, 350, 10);
		view.addRange(J2, 350, 10);
		view.addRange(J3, 350, 10);
		view.addRange(J4, 350, 10);
		view.addRange(J5, 350, 10);
		view.popStack();
		
		super.getView(view);
	}
	
	/**
	 * The elements in this {@link Cuboid} list are relative the base of this robot.
	 * Since the {@link ShapeEntity} of a {@link @Sixi3Link} is not the same place as the bone represented by that link,
	 * The {@link Cuboid} for each {@link @ShapeEntity} has to be adjusted by both the bone pose (relative to base) and 
	 * then the shape entity pose.
	 */
	@Override
	public ArrayList<Cuboid> getCuboidList() {
		ArrayList<Cuboid> list = new ArrayList<Cuboid>();

		Matrix4d currentBonePose = new Matrix4d();
		Matrix4d cuboidAfter = new Matrix4d();

		// current pose starts as root pose
		getPoseWorld(currentBonePose);

		list.addAll(base.getCuboidList());
		for(Cuboid c : list) {
			// add current pose to cube pose and save that as cube pose.
			cuboidAfter.mul(currentBonePose,base.getPose());
			c.setPose(cuboidAfter);
		}
		
		for( Sixi3Link bone : links ) {
			// add bone to current pose
			currentBonePose.mul(bone.pose);
			
			ArrayList<Cuboid> list2 = bone.shape.getCuboidList();
			for(Cuboid c : list2) {
				Cuboid nc = new Cuboid();
				nc.set(c);
				// add current pose to cube pose and save that as cube pose.
				cuboidAfter.mul(currentBonePose,bone.shape.getPose());
				nc.setPose(cuboidAfter);
				list.add(nc);
			}
		}
		
		return list;
	}
}
