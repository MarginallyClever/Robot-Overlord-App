package com.marginallyclever.robotoverlord.robots.deltarobot3;

import com.jogamp.opengl.GL2;
import com.marginallyclever.communications.session.SessionLayer;
import com.marginallyclever.convenience.Cylinder;
import com.marginallyclever.convenience.IntersectionHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.robotoverlord.entities.ShapeEntity;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.robots.Robot;
import com.marginallyclever.robotoverlord.robots.RobotEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewElementButton;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.beans.PropertyChangeEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

@Deprecated
public class DeltaRobot3 extends RobotEntity implements Robot {
	private static final Logger logger = LoggerFactory.getLogger(DeltaRobot3.class);

	// machine ID
	private long robotUID;
	private final static String hello = "HELLO WORLD! I AM DELTA ROBOT V3-";
	public final static String ROBOT_NAME = "Delta Robot 3";
	
	//machine dimensions & calibration
	public static final float BASE_TO_SHOULDER_X   =( 0.0f);  // measured in solidworks, relative to base origin
	public static final float BASE_TO_SHOULDER_Y   =( 3.77f);
	public static final float BASE_TO_SHOULDER_Z   =(18.9f);
	public static final float BICEP_LENGTH         =( 5.000f);
	public static final float FOREARM_LENGTH       =(16.50f);
	public static final float WRIST_TO_FINGER_X    =( 0.0f);
	public static final float WRIST_TO_FINGER_Y    =( 1.724f);
	public static final float WRIST_TO_FINGER_Z    =( 0.5f);  // measured in solidworks, relative to finger origin
	public static final int NUM_ARMS = 3;
	private static double HOME_X = 0.0f;
	private static double HOME_Y = 0.0f;
	private static double HOME_Z = 3.98f;

	// angle of rotation
	public final DeltaRobot3Arm [] arms = new DeltaRobot3Arm[NUM_ARMS];

	// bounding volumes for collision testing
	private Cylinder [] volumes;

	// models for 3d rendering
	private final transient ShapeEntity modelTop;
	private final transient ShapeEntity modelArm;
	private final transient ShapeEntity modelBase;
	
	// motion state testing
	final DeltaRobot3Memento motionNow = new DeltaRobot3Memento();
	final DeltaRobot3Memento motionFuture = new DeltaRobot3Memento();

	/**
	 * When a valid move is made in the simulation, set this flag to true.
	 * elsewhere, watch this flag and send gcode to the robot and reset this flag.
	 * This way the robot communication is not flooded with identical moves.
	 */
	private boolean haveArmsMoved = false;

	private final BooleanParameter draw_finger_star = new BooleanParameter("Show end effector point",true);
	private final BooleanParameter draw_base_star = new BooleanParameter("show base star",false);
	private final BooleanParameter draw_shoulder_to_elbow = new BooleanParameter("show shoulder to elbow",false);
	private final BooleanParameter draw_shoulder_star = new BooleanParameter("show shoulder star",false);
	private final BooleanParameter draw_elbow_star = new BooleanParameter("show elbow star",false);
	private final BooleanParameter draw_wrist_star = new BooleanParameter("show wrist star",false);

	private final Cylinder tube = new Cylinder();  // for drawing forearms

	/**
	 * Used by {@link Robot} interface.
	 */
	private int activeJoint = 0;

	public DeltaRobot3() {
		super();
		setName(ROBOT_NAME);

		for(int i=0;i<NUM_ARMS;++i) {
			arms[i] = new DeltaRobot3Arm(getNormalOfArmPlane(i));
		}

		setupBoundingVolumes();
		setHome(new Vector3d(0,0,0));

		tube.setRadius(0.15f);

		modelTop = new ShapeEntity("top", "/robots/DeltaRobot3/top.obj");
		modelArm = new ShapeEntity("arm", "/robots/DeltaRobot3/arm.obj");
		modelBase = new ShapeEntity("base", "/robots/DeltaRobot3/base.obj");

		modelBase.getMaterial().setDiffuseColor(1,0.8f,0.6f,1);
		modelArm.getMaterial().setDiffuseColor(1.0f, 249.0f/255.0f, 242.0f/255.0f,1);
		modelTop.getMaterial().setDiffuseColor(1.0f, 249.0f/255.0f, 242.0f/255.0f,1);
	}

	@Override
	public void getView(ViewPanel view) {
		view.startNewSubPanel("Delta robot",true);
		view.addButton("Go home").addActionEventListener((e)->goHome());
		ViewElementButton bOpen = view.addButton("Open control panel");
		bOpen.addActionEventListener((evt)-> onOpenAction() );

		view.add(draw_finger_star);
		view.add(draw_base_star);
		view.add(draw_shoulder_to_elbow);
		view.add(draw_shoulder_star);
		view.add(draw_elbow_star);
		view.add(draw_wrist_star);

		super.getView(view);
	}

	private void onOpenAction() {
		// TODO removed because it's too hard to maintain in a deprecated system.
		throw new RuntimeException("Not implemented");
	}

	private void setupBoundingVolumes() {
		// set up bounding volumes
		volumes = new Cylinder[NUM_ARMS];
		for(int i=0;i<volumes.length;++i) {
			volumes[i] = new Cylinder();
		}
		volumes[0].setRadius(3.2f);
		volumes[1].setRadius(3.0f*0.575f);
		volumes[2].setRadius(2.2f);
	}

	private Vector3d getHome() {  return new Vector3d(HOME_X,HOME_Y,HOME_Z);  }

	private void setHome(Vector3d newHome) {
		HOME_X=newHome.x;
		HOME_Y=newHome.y;
		HOME_Z=newHome.z;
		
		rebuildShoulders();

		// find the starting height of the end effector at home position
		// @TODO: project wrist-on-bicep to get more accurate distance
		double aa=(arms[0].elbow.y-arms[0].wrist.y);
		double cc=FOREARM_LENGTH;
		double bb=Math.sqrt((cc*cc)-(aa*aa));
		aa=arms[0].elbow.x-arms[0].wrist.x;
		cc=bb;
		bb=Math.sqrt((cc*cc)-(aa*aa));
		motionNow.fingerPosition.set(0,0,BASE_TO_SHOULDER_Z-bb-WRIST_TO_FINGER_Z);
		moveIfAble();
	}

    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
    	inputStream.defaultReadObject();
    }

	private void moveIfAble() {
		if(movePermitted()) {
			haveArmsMoved=true;
			finalizeMove();
		}
	}

	private void finalizeMove() {
		if(!haveArmsMoved) return;		

		haveArmsMoved=false;
		this.sendCommand("G0 X"+motionNow.fingerPosition.x
				          +" Y"+motionNow.fingerPosition.y
				          +" Z"+motionNow.fingerPosition.z
				          );
	}

	@Override
	public void render(GL2 gl2) {
		super.render(gl2);

		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, this.getPose());
		gl2.glTranslated(0,0,2);
		
		drawModel(gl2);
		drawForearms(gl2);
		drawDebugInfo(gl2);

		gl2.glPopMatrix();
	}

	private void drawModel(GL2 gl2) {
		modelBase.render(gl2);

		for(int i=0;i<NUM_ARMS;++i) {
			gl2.glPushMatrix();
			gl2.glTranslated(arms[i].shoulder.x,
					arms[i].shoulder.y,
					arms[i].shoulder.z);
			gl2.glRotated(90,0,1,0);  // model oriented wrong direction
			gl2.glRotated(60-i*(360.0f/NUM_ARMS), 1, 0, 0);
			gl2.glTranslated(0, 0, 0.125f*2.54f);  // model origin wrong
			gl2.glRotated(180-arms[i].angle,0,0,1);
			modelArm.render(gl2);
			gl2.glPopMatrix();
		}
		//top
		gl2.glPushMatrix();
		gl2.glTranslated(motionNow.fingerPosition.x,motionNow.fingerPosition.y,motionNow.fingerPosition.z);
		modelTop.render(gl2);
		gl2.glPopMatrix();
	}

	private void drawDebugInfo(GL2 gl2) {
		gl2.glDisable(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_TEXTURE);
		// debug info
		gl2.glPushMatrix();

		for(DeltaRobot3Arm arm : arms) {
			gl2.glColor3f(1,1,1);
			if(draw_shoulder_star.get()) PrimitiveSolids.drawStar(gl2, arm.shoulder,5);
			if(draw_elbow_star.get()) PrimitiveSolids.drawStar(gl2, arm.elbow,3);
			if(draw_wrist_star.get()) PrimitiveSolids.drawStar(gl2, arm.wrist,1);

			if(draw_shoulder_to_elbow.get()) {
				gl2.glBegin(GL2.GL_LINES);
				gl2.glColor3f(0,1,0);
				gl2.glVertex3d(arm.elbow.x,arm.elbow.y,arm.elbow.z);
				gl2.glColor3f(0,0,1);
				gl2.glVertex3d(arm.shoulder.x,arm.shoulder.y,arm.shoulder.z);
				gl2.glEnd();
			}
		}
		gl2.glPopMatrix();

		if(draw_finger_star.get()) {
			// draw finger center (end effector)
			gl2.glPushMatrix();
			gl2.glTranslated(
					motionNow.fingerPosition.x,
					motionNow.fingerPosition.y,
					motionNow.fingerPosition.z);
			PrimitiveSolids.drawStar(gl2, 5);
			gl2.glPopMatrix();
		}

		if(draw_base_star.get()) {
			PrimitiveSolids.drawStar(gl2, 2);
		}

		gl2.glEnable(GL2.GL_LIGHTING);
		gl2.glEnable(GL2.GL_TEXTURE);

	}

	private void drawForearms(GL2 gl2) {
		Vector3d a = new Vector3d();
		Vector3d b = new Vector3d();

		int i=0;
		for(DeltaRobot3Arm arm : arms) {
			Vector3d ortho = getNormalOfArmPlane(i);
			++i;

			a.set(arm.wrist);
			b.set(ortho);
			b.scale(1);
			a.add(b);
			tube.SetP1(a);

			a.set(arm.elbow);
			b.set(ortho);
			b.scale(1);
			a.add(b);
			tube.SetP2(a);
			tube.render(gl2);

			a.set(arm.wrist);
			b.set(ortho);
			b.scale(-1);
			a.add(b);
			tube.SetP1(a);
			a.set(arm.elbow);
			b.set(ortho);
			b.scale(-1);
			a.add(b);
			tube.SetP2(a);
			tube.render(gl2);
		}
	}

	private void setModeAbsolute() {
		if(connection!=null) this.sendCommand("G90");
	}

	private void setModeRelative() {
		if(connection!=null) this.sendCommand("G91");
	}

	private void goHome() {
		boolean isHomed = false;
		this.sendCommand("G28");
		motionNow.fingerPosition.set(HOME_X,HOME_Y,HOME_Z);  // HOME_* should match values in robot firmware.
		updateIK();
		haveArmsMoved=true;
		finalizeMove();
		isHomed =true;
	}

	// override this method to check that the software is connected to the right type of robot.
	public void dataAvailable(SessionLayer arg0, String line) {
		if(line.contains(hello)) {
			// network info
			boolean isPortConfirmed = true;
			//finalizeMove();
			setModeAbsolute();
			
			String uidString=line.substring(hello.length()).trim();
			uidString = uidString.substring(uidString.indexOf('#')+1);
			logger.info(">>> UID="+uidString);
			try {
				long uid = Long.parseLong(uidString);
				if(uid==0) {
					robotUID = getNewRobotUID();
				} else {
					robotUID = uid;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}

			setName(ROBOT_NAME+" #"+robotUID);
		}
		
		logger.info("RECV "+line);
	}

	/**
	 * based on <a href="http://www.exampledepot.com/egs/java.net/Post.html">http://www.exampledepot.com/egs/java.net/Post.html</a>
	 */
	private long getNewRobotUID() {
		long new_uid = 0;

		try {
			// Send data
			URL url = new URL("https://marginallyclever.com/deltarobot_getuid.php");
			URLConnection conn = url.openConnection();
			try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
				String line = rd.readLine();
				new_uid = Long.parseLong(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

		// did read go ok?
		if (new_uid != 0) {
			// make sure a topLevelMachinesPreferenceNode node is created
			// tell the robot it's new UID.
			this.sendCommand("UID " + new_uid);
		}
		return new_uid;
	}

	/**
	 * @return true if successful.
	 */
	private boolean updateFK() {
		Matrix4d eeOld = getEndEffector();

		updateElbowsFromAngles();
		findEndEffectorFromElbows();
		updateIKWrists(motionNow);

		Matrix4d eeNew = getEndEffector();
		notifyPropertyChangeListeners(new PropertyChangeEvent(this,"ee",eeOld,eeNew));
		return true;
	}

	/**
	 * Subtract the wrist radius so that three elbow points and the radius (bicep length) form a sphere.
	 * <a href="https://stackoverflow.com/questions/11719168/how-do-i-find-the-sphere-center-from-3-points-and-radius0">find the center of the sphere</a>.
	 * The center of the sphere is the new end effector position relative to the base.
	 */
	private void findEndEffectorFromElbows() {
		Vector3d wrist = new Vector3d(DeltaRobot3.WRIST_TO_FINGER_X,DeltaRobot3.WRIST_TO_FINGER_Y,0);
		Vector3d [] p = new Vector3d[NUM_ARMS];
		double r = wrist.length();
		for(int i=0;i<NUM_ARMS;++i) {
			p[i] = new Vector3d(arms[i].elbow);
			Vector3d n = getNormalOfArmPlane(i);
			Vector3d ortho = new Vector3d(-n.y,n.x,0);
			ortho.scale(r);
			p[i].sub(ortho);
		}
		// find the center of the sphere formed by the three points p and the radius.
		// see https://stackoverflow.com/questions/11719168/how-do-i-find-the-sphere-center-from-3-points-and-radius

		Vector3d t = IntersectionHelper.centerOfCircumscribedSphere(p[0],p[1],p[2],DeltaRobot3.FOREARM_LENGTH);
		motionNow.fingerPosition.set(t);
	}


	/**
	 * Use motor angles to find the elbow positions relative to the base.
	 */
	private void updateElbowsFromAngles() {
		for(int i=0;i<NUM_ARMS;++i) {
			arms[i].updateElbowFromAngle();
		}
	}

	/**
	 * Convert cartesian XYZ to robot motor steps.
	 * @return true if successful, false if the IK solution cannot be found.
	 */
	private boolean updateIK() {
		try {
			Matrix4d eeOld = getEndEffector();

			updateIKWrists(motionNow);
			updateShoulderAngles();


			Matrix4d eeNew = getEndEffector();
			notifyPropertyChangeListeners(new PropertyChangeEvent(this,"ee",eeOld,eeNew));
		}
		catch(AssertionError e) {
			return false;
		}

		return true;
	}

	private void updateIKWrists(DeltaRobot3Memento keyframe) {
		for(int i=0;i<NUM_ARMS;++i) {
			arms[i].updateWrist(keyframe.fingerPosition);
		}
	}

	private void updateShoulderAngles() throws AssertionError {
		for(int i=0;i<NUM_ARMS;++i) {
			arms[i].updateShoulderAngle();
		}
	}

	private Vector3d getNormalOfArmPlane(double i) {
		double v = Math.PI*2.0f * (i/(double)NUM_ARMS - 1f/((double)NUM_ARMS*2.0));
		return new Vector3d( Math.cos(v), Math.sin(v), 0);
	}
	
	private void rebuildShoulders() {
		for(int i=0;i<NUM_ARMS;++i) {
			arms[i].rebuildShoulder();
		}
	}


	//TODO check for collisions with http://geomalgorithms.com/a07-_distance.html#dist3D_Segment_to_Segment ?
	private boolean movePermitted() {/*
		// don't hit floor
		if(state.finger_tip.z<0.25f) {
			return false;
		}
		// don't hit ceiling
		if(state.finger_tip.z>50.0f) {
			return false;
		}

		// check far limit
		Vector3d temp = new Vector3d(state.finger_tip);
		temp.sub(state.shoulder);
		if(temp.length() > 50) return false;
		// check near limit
		if(temp.length() < BASE_TO_SHOULDER_MINIMUM_LIMIT) return false;
	 */
		// angle are good?
		if(!checkAngleLimits()) return false;
		// seems doable
		if(!updateIK()) return false;

		// OK
		return true;
	}

	private boolean checkAngleLimits() {
		return true;
	}

	@Override
	public Memento createKeyframe() {
		return new DeltaRobot3Memento();
	}

	@Override
	public Object get(int property) {
		switch(property) {
			case NAME: return getName();
			case NUM_JOINTS: return NUM_ARMS;
			case ACTIVE_JOINT: return activeJoint;
			case JOINT_NAME: return "Joint "+activeJoint;
			case JOINT_VALUE: return arms[activeJoint].angle;
			case JOINT_RANGE_MAX: return 180.0;
			case JOINT_RANGE_MIN: return -180.0;
			case JOINT_HAS_RANGE_LIMITS: return true;
			case JOINT_PRISMATIC: return false;
			case END_EFFECTOR: return getEndEffector();
			case END_EFFECTOR_TARGET: return vector2Matrix(motionFuture.fingerPosition);
			case TOOL_CENTER_POINT: return MatrixHelper.createIdentityMatrix4();
			case POSE: return getPoseWorld();
			case JOINT_POSE: return vector2Matrix(arms[activeJoint].shoulder);
			default :  return null;
		}
	}

	@Override
	public void set(int property, Object value) {
		switch(property) {
			case ACTIVE_JOINT: activeJoint = Math.max(0,Math.min(NUM_ARMS,(int)value));  break;
			case JOINT_VALUE: {
				arms[activeJoint].angle = (double)value;
				updateFK();
			}  break;
			case END_EFFECTOR_TARGET: {
				Matrix4d m = (Matrix4d)value;
				Vector3d t = new Vector3d();
				m.get(t);
				motionNow.fingerPosition.set(t);
				moveIfAble();
			}  break;
			case TOOL_CENTER_POINT: break;
			case POSE: setPoseWorld((Matrix4d)value);  break;
			default: break;
		}
	}

	private Matrix4d getEndEffector() {
		return vector2Matrix(motionNow.fingerPosition);
	}

	private Matrix4d vector2Matrix(Vector3d v) {
		Matrix4d m = new Matrix4d();
		m.setIdentity();
		m.setTranslation(v);
		return m;
	}
}
