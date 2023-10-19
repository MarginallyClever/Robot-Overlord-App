package com.marginallyclever.robotoverlord.robots.stewartplatform.linear;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.helpers.IntersectionHelper;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.RenderComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.RemoteParameter;
import com.marginallyclever.robotoverlord.parameters.swing.ComponentSwingViewFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A linear stewart platform with 6 legs.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
@Deprecated
public class LinearStewartPlatformCore extends RenderComponent {
	private static final Logger logger = LoggerFactory.getLogger(LinearStewartPlatformCore.class);
	
	public final DoubleParameter SLIDE_TRAVEL = new DoubleParameter("SLIDE_TRAVEL", 10.0);  // cm
	public final DoubleParameter ARM_LENGTH = new DoubleParameter("ARM_LENGTH",15.0362);  // cm
	public final DoubleParameter BASE_X = new DoubleParameter("BASE_X",6.0968);  // cm
	public final DoubleParameter BASE_Y = new DoubleParameter("BASE_Y",1.6000);  // cm
	public final DoubleParameter BASE_Z = new DoubleParameter("BASE_Z",7.8383);  // cm
	public final DoubleParameter EE_X = new DoubleParameter("EE_X", 3.6742);  // cm
	public final DoubleParameter EE_Y = new DoubleParameter("EE_Y", 0.7500);  // cm
	public final DoubleParameter EE_Z = new DoubleParameter("EE_Z",-2.4000);  // cm

	private final BooleanParameter debugEEPoints = new BooleanParameter("debugEEPoints", false);
	private final BooleanParameter debugSlides = new BooleanParameter("debugSlides", true);
	private final BooleanParameter debugArms = new BooleanParameter("debugArms", false);
	
	private Entity ee = new Entity("ee");
	private PoseComponent eePose;

	protected LinearStewartPlatformArm[] arms = {
			new LinearStewartPlatformArm(),
			new LinearStewartPlatformArm(),
			new LinearStewartPlatformArm(),
			new LinearStewartPlatformArm(),
			new LinearStewartPlatformArm(),
			new LinearStewartPlatformArm()
	};

	private final RemoteParameter connection = new RemoteParameter();
	private final DoubleParameter velocity = new DoubleParameter("velocity",5);
	private final DoubleParameter acceleration = new DoubleParameter("acceleration",200);

	protected final MaterialComponent material = new MaterialComponent();

	public LinearStewartPlatformCore() {
		super();

		//connection.addPropertyChangeListener(this);

		// apply some default materials.
		material.setAmbientColor(0, 0, 0, 1);
		material.setDiffuseColor(1f,1f,1f,1);
		material.setEmissionColor(0, 0, 0, 1);
		material.setLit(true);
		material.setShininess(0);

		calculateEndEffectorPointsOneTime();
		calculateBasePointsOneTime();
	}

	@Override
	public void onAttach() {
		Entity maybe = getEntity().findByPath("./ee");
		ee = (maybe!=null) ? maybe : new Entity("ee");
		eePose = ee.getComponent(PoseComponent.class);
		eePose.setPosition(new Vector3d(0,0,BASE_Z.get()+Math.abs(EE_Z.get())+ARM_LENGTH.get()));
	}

	/**
	 * Calculate end effector points - the center of each magnetic ball at the end effector.
	 * end effector points are ordered counter-clockwise, looking down on the machine.
	 * <pre>
	 *      1
	 *  2       0 <-- first
	 *      x     <-- center
	 *  3       5 <-- last
	 *      4</pre>
	 */
	protected void calculateEndEffectorPointsOneTime() {
		Vector3d vx = new Vector3d();
		Vector3d vy = new Vector3d();
		Vector3d tx = new Vector3d();
		Vector3d ty = new Vector3d();

		for(int i=0;i<arms.length;++i) {
			double r = Math.toRadians(60.0+120.0*i/2.0);
			double c = Math.cos(r);
			double s = Math.sin(r);
			vx.set(c,s,0);
			vy.set(-s,c,0);
			tx.scale( EE_X.get(),vx);
			ty.scale(-EE_Y.get(),vy);
			arms[i].pEE.add(tx,ty);
			arms[i].pEE.z=EE_Z.get();
			++i;
			tx.scale( EE_X.get(),vx);
			ty.scale( EE_Y.get(),vy);
			arms[i].pEE.add(tx,ty);
			arms[i].pEE.z=EE_Z.get();
		}

	}

	/**
	 * Calculate base of linear slides.
	 * linear slides are ordered counter-clockwise, looking down on the machine.
	 * <pre>
	 *     1
	 *  2     0 <-- first
	 *     x    <-- center
	 *  3     5 <-- last
	 *     4</pre>
	 */
	protected void calculateBasePointsOneTime() {
		Vector3d vx = new Vector3d();
		Vector3d vy = new Vector3d();
		Vector3d tx = new Vector3d();
		Vector3d ty = new Vector3d();

		int [] indexes = {0,5,2,1,4,3};

		for(int i=0;i<arms.length;++i) {
			double r = Math.toRadians(120.0*i/2.0);
			double c = Math.cos(r);
			double s = Math.sin(r);
			vx.set(c,s,0);
			vy.set(-s,c,0);
			tx.scale( BASE_X.get(),vx);
			ty.scale( BASE_Y.get(),vy);
			arms[indexes[i]].pBase.add(tx,ty);
			++i;
			tx.scale( BASE_X.get(),vx);
			ty.scale(-BASE_Y.get(),vy);
			arms[indexes[i]].pBase.add(tx,ty);
		}
	}


	@Override
	public void update(double dt) {
		connection.update(dt);
		super.update(dt);

		Matrix4d eeMatrix = eePose.getLocal();

		// use calculated end effector points to find same points after EE moves.
		for (LinearStewartPlatformArm linearStewartPlatformArm : arms) {
			eeMatrix.transform(linearStewartPlatformArm.pEE, linearStewartPlatformArm.pEE2);
		}

		// We have pEE2 and pBase.  one end of the rod is at pEE2[n].  
		// The sphere formed by pDD2[n] and ARM_LENGTH intersects the vertical line at bBase[n] twice.
		// The first intersection traveling up is the one we want.
		Ray ray = new Ray();
		ray.setDirection(new Vector3d(0,0,1));
		for (LinearStewartPlatformArm arm : arms) {
			ray.setOrigin(arm.pBase);
			arm.linearPosition = IntersectionHelper.raySphere(ray, arm.pEE2, ARM_LENGTH.get()) - BASE_Z.get();
			arm.pSlide.set(arm.pBase);
			arm.pSlide.z += arm.linearPosition + BASE_Z.get();
		}
	}
	
	@Override
	public void render(GL3 gl) {
		PoseComponent myPose = getEntity().getComponent(PoseComponent.class);
		drawBase(gl);
		drawTopPlate(gl);
		if(debugEEPoints.get()) drawDebugEEPoints(gl);
		if(debugSlides.get()) drawDebugSlides(gl);
		if(debugArms.get()) drawDebugArms(gl);
	}

	private void drawBase(GL3 gl) {
		//MatrixHelper.drawMatrix(gl, MatrixHelper.createIdentityMatrix4(),5);
	}

	private void drawTopPlate(GL3 gl) {
		//MatrixHelper.drawMatrix(gl, getEndEffectorPose(),5);
	}

	public Matrix4d getEndEffectorPose() {
		return eePose.getLocal();
	}

	private void drawDebugEEPoints(GL3 gl) {
		Vector3d eeCenter = MatrixHelper.getPosition(eePose.getLocal());/*
		gl.glColor3d(1, 0, 0);
		gl.glBegin(GL3.GL_LINES);
		for (LinearStewartPlatformArm arm : arms) {
			gl.glVertex3d(eeCenter.x, eeCenter.y, eeCenter.z);
			gl.glVertex3d(arm.pEE2.x,
					arm.pEE2.y,
					arm.pEE2.z);
			gl.glColor3d(0, 0, 0);
		}
		gl.glEnd();*/
	}

	private void drawDebugSlides(GL3 gl)  {
		for(int i=0;i<arms.length;++i) {
			renderOneLinearSlide(gl,
					arms[i].pSlide,
					BASE_Z.get(),
					BASE_Z.get()+SLIDE_TRAVEL.get(),
					i==0);
		}
	}

	protected void drawDebugArms(GL3 gl) {/*
		gl.glColor3d(1, 0, 0);
		gl.glBegin(GL3.GL_LINES);
		for(int i=0;i<arms.length;++i) {
			gl.glVertex3d(arms[i].pEE2.x,
						   arms[i].pEE2.y,
						   arms[i].pEE2.z);
			gl.glVertex3d( arms[i].pSlide.x,
							arms[i].pSlide.y,
							arms[i].pSlide.z);
			gl.glColor3d(0, 0, 0);
		}
		gl.glEnd();*/
	}

	private void renderOneLinearSlide(GL3 gl,Point3d p,double min,double max,boolean first) {/*
		gl.glBegin(GL3.GL_LINES);
		if(first) gl.glColor3d(1, 0, 0);
		else      gl.glColor3d(0, 1, 0);
		gl.glVertex3d(p.x, p.y, min);
		gl.glVertex3d(p.x, p.y, p.z);
		gl.glColor3d(0, 0, 1);
		gl.glVertex3d(p.x, p.y, p.z);
		gl.glVertex3d(p.x, p.y, max);
		gl.glEnd();*/
	}
	
	@Deprecated
	public void getView(ComponentSwingViewFactory view) {
		view.add(connection);
		view.addButton("GOTO EE").addActionEventListener((evt)->gotoPose());
		view.addButton("GOTO ZERO").addActionEventListener((evt)->{
			String message = "G0"
					+" F"+ StringHelper.formatDouble(velocity.get())
					+" A"+StringHelper.formatDouble(acceleration.get())
					+" X0"
					+" Y0"
					+" Z0"
					+" U0"
					+" V0"
					+" W0";
			logger.info(message);
			connection.sendMessage(message);
			Matrix4d ident = new Matrix4d();
			ident.setIdentity();
			ident.setTranslation(new Vector3d(0,0,BASE_Z.get()+Math.abs(EE_Z.get())+ARM_LENGTH.get()));
			eePose.setLocalMatrix4(ident);
		});
		view.addButton("Factory Reset").addActionEventListener((evt)->{
			for(int i=0;i<6;++i) {
				connection.sendMessage("M101 A"+i+" B-1000 T0");
				// wait while it saves...
				try {
					Thread.sleep(2500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		view.addRange(velocity, 20, 1);
		view.addRange(acceleration, 1000, 0);
	}

	private void gotoPose() {
		float scale=-10;
		String message = "G0"
				+" F"+StringHelper.formatDouble(velocity.get())
				+" A"+StringHelper.formatDouble(acceleration.get())
				+" X"+StringHelper.formatDouble(arms[0].linearPosition*scale)
				+" Y"+StringHelper.formatDouble(arms[1].linearPosition*scale)
				+" Z"+StringHelper.formatDouble(arms[2].linearPosition*scale)
				+" U"+StringHelper.formatDouble(arms[3].linearPosition*scale)
				+" V"+StringHelper.formatDouble(arms[4].linearPosition*scale)
				+" W"+StringHelper.formatDouble(arms[5].linearPosition*scale);
		logger.info(message);
		connection.sendMessage(message);
	}
}
