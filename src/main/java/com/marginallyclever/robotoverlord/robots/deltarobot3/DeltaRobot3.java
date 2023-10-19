package com.marginallyclever.robotoverlord.robots.deltarobot3;

import com.marginallyclever.communications.session.SessionLayer;
import com.marginallyclever.convenience.Cylinder;
import com.marginallyclever.convenience.helpers.IntersectionHelper;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.ComponentDependency;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.parameters.RemoteParameter;
import com.marginallyclever.robotoverlord.parameters.swing.ViewElementButton;
import com.marginallyclever.robotoverlord.parameters.swing.ComponentSwingViewFactory;
import com.marginallyclever.robotoverlord.robots.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * A 3-arm delta robot.  Each arm is of the rotary style.
 *
 * @author Dan Royer
 * @since before 2.0.0
 */
@Deprecated
@ComponentDependency(components = {PoseComponent.class, MaterialComponent.class})
public class DeltaRobot3 extends Component implements Robot {
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

	// models for 3d rendering
	private final transient MeshFromFile modelTop;
	private final transient MeshFromFile modelArm;
	private final transient MeshFromFile modelBase;
	
	// motion state testing
	final Vector3d motionNow = new Vector3d();
	final Vector3d motionFuture = new Vector3d();

	/**
	 * When a valid move is made in the simulation, set this flag to true.
	 * elsewhere, watch this flag and send gcode to the robot and reset this flag.
	 * This way the robot communication is not flooded with identical moves.
	 */
	private boolean haveArmsMoved = false;
	private final Cylinder tube = new Cylinder();  // for drawing forearms

	protected transient RemoteParameter connection = new RemoteParameter();
	protected transient boolean isReadyToReceive;

	/**
	 * Used by {@link Robot} interface.
	 */
	private int activeJoint = 0;

	public DeltaRobot3() {
		super();

		for(int i=0;i<NUM_ARMS;++i) {
			arms[i] = new DeltaRobot3Arm(getNormalOfArmPlane(i));
		}

		setupBoundingVolumes();
		setHome(new Vector3d(0,0,0));

		tube.setRadius(0.15f);

		modelTop = new MeshFromFile("/com/marginallyclever/robotoverlord/robots/deltarobot3/top.obj");
		modelArm = new MeshFromFile("/com/marginallyclever/robotoverlord/robots/deltarobot3/arm.obj");
		modelBase = new MeshFromFile("/com/marginallyclever/robotoverlord/robots/deltarobot3/base.obj");

		//modelBase.getMaterial().setDiffuseColor(1,0.8f,0.6f,1);
		//modelArm.getMaterial().setDiffuseColor(1.0f, 249.0f/255.0f, 242.0f/255.0f,1);
		//modelTop.getMaterial().setDiffuseColor(1.0f, 249.0f/255.0f, 242.0f/255.0f,1);
	}

	@Deprecated
	public void getView(ComponentSwingViewFactory view) {
		view.addButton("Go home").addActionEventListener((e)->goHome());
		ViewElementButton bOpen = view.addButton("Open control panel");
		bOpen.addActionEventListener((evt)-> onOpenAction() );
	}

	private void onOpenAction() {
		// TODO removed because it's too hard to maintain in a deprecated system.
		throw new RuntimeException("Not implemented");
	}

	private void setupBoundingVolumes() {
		// set up bounding volumes
		// bounding volumes for collision testing
		Cylinder[] volumes = new Cylinder[NUM_ARMS];
		for(int i = 0; i< volumes.length; ++i) {
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
		motionNow.set(0,0,BASE_TO_SHOULDER_Z-bb-WRIST_TO_FINGER_Z);
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
		this.sendCommand("G0 X"+motionNow.x
				          +" Y"+motionNow.y
				          +" Z"+motionNow.z
				          );
	}

	/**
	 * Processes a single instruction meant for the robot.
	 * @param command command to send
	 * @return true if the command is sent to the robot.
	 */
	public boolean sendCommand(String command) {
		if(connection==null) return false;

		// contains a comment?  if so remove it
		int index=command.indexOf('(');
		if(index!=-1) {
			//String comment=line.substring(index+1,line.lastIndexOf(')'));
			//Log("* "+comment+NL);
			command=command.substring(0,index).trim();
			if(command.length()==0) {
				// entire line was a comment.
				return false;  // still ready to send
			}
		}

		if(!command.endsWith("\n")) {
			command+="\n";
		}

		// send relevant part of line to the robot
		connection.sendMessage(command);

		return true;
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
		motionNow.set(HOME_X,HOME_Y,HOME_Z);  // HOME_* should match values in robot firmware.
		updateIK();
		haveArmsMoved=true;
		finalizeMove();
		isHomed =true;
	}

	public boolean isReadyToReceive() {
		return isReadyToReceive;
	}

	@Override
	public void update(double dt) {
		super.update(dt);
		connection.update(dt);
		if (connection.isConnectionOpen()) {
			// set the lock

			// de-queue and process all messages
			//if(data.startsWith(">")) isReadyToReceive=true;

			// release the lock
		}
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
		motionNow.set(t);
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

	private void updateIKWrists(Vector3d keyframe) {
		for(int i=0;i<NUM_ARMS;++i) {
			arms[i].updateWrist(keyframe);
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
		return updateIK();

		// OK
	}

	private boolean checkAngleLimits() {
		return true;
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
			case END_EFFECTOR_TARGET: return vector2Matrix(motionFuture);
			case TOOL_CENTER_POINT: return MatrixHelper.createIdentityMatrix4();
			case POSE: return getPoseWorld();
			case JOINT_POSE: return vector2Matrix(arms[activeJoint].shoulder);
			default :  return null;
		}
	}

	private Matrix4d getPoseWorld() {
		return getEntity().getComponent(PoseComponent.class).getWorld();
	}

	private void setPoseWorld(Matrix4d m) {
		getEntity().getComponent(PoseComponent.class).setWorld(m);
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
				motionNow.set(t);
				moveIfAble();
			}  break;
			case TOOL_CENTER_POINT: break;
			case POSE: setPoseWorld((Matrix4d)value);  break;
			default: break;
		}
	}

	private Matrix4d getEndEffector() {
		return vector2Matrix(motionNow);
	}

	private Matrix4d vector2Matrix(Vector3d v) {
		Matrix4d m = new Matrix4d();
		m.setIdentity();
		m.setTranslation(v);
		return m;
	}

	private final List<PropertyChangeListener> listeners = new ArrayList<>();

	@Override
	public void addPropertyChangeListener(PropertyChangeListener p) {
		listeners.add(p);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener p) {
		listeners.remove(p);
	}

	private void notifyPropertyChangeListeners(PropertyChangeEvent ee) {
		for(PropertyChangeListener p : listeners) {
			p.propertyChange(ee);
		}
	}
}
