package com.marginallyclever.robotOverlord.robots.deltaRobot3;

import com.jogamp.opengl.GL2;
import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.convenience.BoundingVolume;
import com.marginallyclever.convenience.Cylinder;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.robotOverlord.robots.RobotEntity;
import com.marginallyclever.robotOverlord.shape.Shape;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.BooleanEntity;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class DeltaRobot3 extends RobotEntity {
	@Serial
	private static final long serialVersionUID = 5991551452979216237L;
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
	public final DeltaRobot3Arm [] arms = new DeltaRobot3Arm[3];

	// bounding volumes for collision testing
	private Cylinder [] volumes;

	// models for 3d rendering
	private final transient Shape modelTop;
	private final transient Shape modelArm;
	private final transient Shape modelBase;
	
	// motion state testing
	private final DeltaRobot3Memento motionNow = new DeltaRobot3Memento();
	private final DeltaRobot3Memento motionFuture = new DeltaRobot3Memento();

	// control panel
	private transient DeltaRobot3Panel controlPanel;
	
	// keyboard history
	private float aDir, bDir, cDir;
	private float xDir, yDir, zDir;

	// network info
	private  boolean isPortConfirmed;
	
	// misc
	private double speed;
	private boolean isHomed = false;

	/**
	 * When a valid move is made in the simulation, set this flag to true.
	 * elsewhere, watch this flag and send gcode to the robot and reset this flag.
	 * This way the robot communication is not flooded with identical moves.
	 */
	private boolean haveArmsMoved = false;

	private final BooleanEntity draw_finger_star = new BooleanEntity("Show end effector point",true);
	private final BooleanEntity draw_base_star = new BooleanEntity("show base star",false);
	private final BooleanEntity draw_shoulder_to_elbow = new BooleanEntity("show shoulder to elbow",false);
	private final BooleanEntity draw_shoulder_star = new BooleanEntity("show shoulder star",false);
	private final BooleanEntity draw_elbow_star = new BooleanEntity("show elbow star",false);
	private final BooleanEntity draw_wrist_star = new BooleanEntity("show wrist star",false);

	private final Cylinder tube = new Cylinder();  // for drawing forearms

	public DeltaRobot3() {
		super();
		setName(ROBOT_NAME);

		for(int i=0;i<3;++i) {
			arms[i] = new DeltaRobot3Arm();
		}

		setupBoundingVolumes();
		setHome(new Vector3d(0,0,0));
		
		isPortConfirmed=false;
		speed=2;
		aDir = 0.0f;
		bDir = 0.0f;
		cDir = 0.0f;
		xDir = 0.0f;
		yDir = 0.0f;
		zDir = 0.0f;

		tube.setRadius(0.15f);

		modelTop = new Shape("top","/DeltaRobot3/top.obj");
		modelArm = new Shape("arm","/DeltaRobot3/arm.obj");
		modelBase = new Shape("base","/DeltaRobot3/base.obj");

		modelBase.getMaterial().setDiffuseColor(1,0.8f,0.6f,1);
		modelArm.getMaterial().setDiffuseColor(1.0f, 249.0f/255.0f, 242.0f/255.0f,1);
		modelTop.getMaterial().setDiffuseColor(1.0f, 249.0f/255.0f, 242.0f/255.0f,1);
	}

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("De","Delta robot");
		view.addButton("Go home").addActionEventListener((e)->goHome());
		view.popStack();
		view.pushStack("Re","Render");
		view.add(draw_finger_star);
		view.add(draw_base_star);
		view.add(draw_shoulder_to_elbow);
		view.add(draw_shoulder_star);
		view.add(draw_elbow_star);
		view.add(draw_wrist_star);
		view.popStack();
		super.getView(view);
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
	
	
	public Vector3d getHome() {  return new Vector3d(HOME_X,HOME_Y,HOME_Z);  }
	
	
	public void setHome(Vector3d newHome) {
		HOME_X=newHome.x;
		HOME_Y=newHome.y;
		HOME_Z=newHome.z;
		
		rebuildShoulders(motionNow);
		updateIKWrists(motionNow);

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
	

    private void readObject(ObjectInputStream inputStream)
            throws IOException, ClassNotFoundException
    {
    	inputStream.defaultReadObject();
    }

	private void moveCartesian(double delta) {
		boolean changed=false;
		
		float dv = (float)getSpeed();
		
		if (xDir!=0) {  motionFuture.fingerPosition.x += dv * xDir;  changed=true;  xDir=0;  }
		if (yDir!=0) {  motionFuture.fingerPosition.y += dv * yDir;	 changed=true;  yDir=0;  }
		if (zDir!=0) {  motionFuture.fingerPosition.z += dv * zDir;	 changed=true;  zDir=0;  }
		
		if(changed) {
			moveIfAble();
		}
	}
	
	
	public void moveIfAble() {
		if(movePermitted()) {
			haveArmsMoved=true;
			finalizeMove();
			if(controlPanel!=null) controlPanel.update();
		}
	}
	
	
	private void moveJoints(double delta) {
		boolean changed=false;
		int i;
		
		double [] angles = new double[NUM_ARMS];
		
		for(i=0;i<NUM_ARMS;++i) {
			angles[i] = arms[i].angle;
		}
		
		// movement
		float dv=(float)getSpeed();

		// if continuous, adjust speed over time
		//float dv *= delta;
		
		if (aDir!=0) {  arms[0].angle -= dv * aDir;  changed=true;  aDir=0;  }
		if (bDir!=0) {  arms[1].angle -= dv * bDir;  changed=true;  bDir=0;  }
		if (cDir!=0) {  arms[2].angle += dv * cDir;  changed=true;  cDir=0;  }
		
		// if not continuous, set *Dir to zero.
		
		if(changed) {
			if(checkAngleLimits()) {
				updateFK();
				haveArmsMoved=true;
			} else {
				for(i=0;i<NUM_ARMS;++i) {
					arms[i].angle= angles[i];
				}
			}
		}
	}
	
	@Override
	public void update(double delta) {
		moveCartesian(delta);
		moveJoints(delta);
	}


	public void finalizeMove() {
		if(!haveArmsMoved) return;		

		haveArmsMoved=false;
		this.sendCommand("G0 X"+motionNow.fingerPosition.x
				          +" Y"+motionNow.fingerPosition.y
				          +" Z"+motionNow.fingerPosition.z
				          );
		if(controlPanel!=null) controlPanel.update();
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


	public void setModeAbsolute() {
		if(connection!=null) this.sendCommand("G90");
	}
	
	
	public void setModeRelative() {
		if(connection!=null) this.sendCommand("G91");
	}

	
	public void goHome() {
		isHomed=false;
		this.sendCommand("G28");
		motionFuture.fingerPosition.set(HOME_X,HOME_Y,HOME_Z);  // HOME_* should match values in robot firmware.
		updateIK();
		haveArmsMoved=true;
		finalizeMove();
		isHomed=true;
		
		if(controlPanel!=null) controlPanel.update();
	}
	

	// override this method to check that the software is connected to the right type of robot.
	public void dataAvailable(NetworkSession arg0,String line) {
		if(line.contains(hello)) {
			isPortConfirmed=true;
			//finalizeMove();
			setModeAbsolute();
			
			String uidString=line.substring(hello.length()).trim();
			uidString = uidString.substring(uidString.indexOf('#')+1);
			Log.message(">>> UID="+uidString);
			try {
				long uid = Long.parseLong(uidString);
				if(uid==0) {
					robotUID = getNewRobotUID();
				} else {
					robotUID = uid;
				}
				if(controlPanel!=null) controlPanel.update();
			}
			catch(Exception e) {
				e.printStackTrace();
			}

			setName(ROBOT_NAME+" #"+robotUID);
		}
		
		Log.message("RECV "+line);
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
			try (
                    final InputStream connectionInputStream = conn.getInputStream();
                    final Reader inputStreamReader = new InputStreamReader(connectionInputStream, StandardCharsets.UTF_8);
                    final BufferedReader rd = new BufferedReader(inputStreamReader)
					) {
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
	
	
	public boolean isPortConfirmed() {
		return isPortConfirmed;
	}
	
	
	public BoundingVolume [] getBoundingVolumes() {
		// TODO finish me
		return volumes;
	}
	
	public void setSpeed(double newSpeed) {
		speed=newSpeed;
	}
	public double getSpeed() {
		return speed;
	}
		
	public boolean isHomed() {
		return isHomed;
	}

	
	public boolean updateFK() {
		return true;
	}

	/**
	 * Convert cartesian XYZ to robot motor steps.
	 * @return true if successful, false if the IK solution cannot be found.
	 */
	public boolean updateIK() {
		try {
			updateIKWrists(motionNow);
			updateIKShoulderAngles();
		}
		catch(AssertionError e) {
			return false;
		}

		return true;
	}


	private void updateIKWrists(DeltaRobot3Memento keyframe) {
		Vector3d n1 = new Vector3d(),o1 = new Vector3d(),temp = new Vector3d();
		double c,s;
		int i;
		for(i=0;i<NUM_ARMS;++i) {
			DeltaRobot3Arm arma=arms[i];

			Vector3d ortho = getNormalOfArmPlane(i);
			c=ortho.x;
			s=ortho.y;

			//n1 = n* c + o*s;
			Vector3d forward = MatrixHelper.getXAxis(this.myPose);
			Vector3d right   = MatrixHelper.getYAxis(this.myPose);
			Vector3d up      = MatrixHelper.getZAxis(this.myPose);

			n1.set(forward);
			n1.scale(c);
			temp.set(right);
			temp.scale(s);
			n1.add(temp);
			n1.normalize();
			//o1 = n*-s + o*c;
			o1.set(forward);
			o1.scale(-s);
			temp.set(right);
			temp.scale(c);
			o1.add(temp);
			o1.normalize();
			//n1.scale(-1);


			//arma.wrist = this.finger_tip + n1*T2W_X + this.base_up*T2W_Z - o1*T2W_Y;
			//armb.wrist = this.finger_tip + n1*T2W_X + this.base_up*T2W_Z + o1*T2W_Y;
			arma.wrist.set(n1);
			arma.wrist.scale(DeltaRobot3.WRIST_TO_FINGER_X);
			arma.wrist.add(keyframe.fingerPosition);
			temp.set(up);
			temp.scale(DeltaRobot3.WRIST_TO_FINGER_Z);
			arma.wrist.add(temp);
			temp.set(o1);
			temp.scale(DeltaRobot3.WRIST_TO_FINGER_Y);
			arma.wrist.sub(temp);
		}
	}
	

	private void updateIKShoulderAngles() throws AssertionError {
		Vector3d w = new Vector3d(),wop = new Vector3d(),temp = new Vector3d(),r = new Vector3d();
		double a,b,d,r1,r0,hh,y,x;

		int i;
		for(i=0;i<NUM_ARMS;++i) {
			DeltaRobot3Arm arm = arms[i];

			// project wrist position onto plane of bicep (wop)
			Vector3d ortho = getNormalOfArmPlane(i);

			//w = arm.wrist - arm.shoulder
			w.set(arm.wrist);
			w.sub(arm.shoulder);

			//a=w | ortho;
			a = w.dot( ortho );
			//wop = w - (ortho * a);
			temp.set(ortho);
			temp.scale(a);
			wop.set(w);
			wop.sub(temp);

			// we need to find wop-elbow to calculate the angle at the shoulder.
			// wop-elbow is not the same as wrist-elbow.
			b=Math.sqrt(DeltaRobot3.FOREARM_LENGTH*DeltaRobot3.FOREARM_LENGTH - a*a);
			if(Double.isNaN(b)) throw new AssertionError();

			// use intersection of circles to find elbow point.
			//a = (r0r0 - r1r1 + d*d ) / (2*d) 
			r1=b;  // circle 1 centers on wrist
			r0=DeltaRobot3.BICEP_LENGTH;  // circle 0 centers on shoulder
			d=wop.length();
			// distance along wop to the midpoint between the two possible intersections
			a = ( r0 * r0 - r1 * r1 + d*d ) / ( 2.0f*d );

			// now find the midpoint
			// normalize wop
			//wop /= d;
			wop.scale(1.0f/d);
			//temp=arm.shoulder+(wop*a);
			temp.set(wop);
			temp.scale(a);
			temp.add(arm.shoulder);
			// with a and r0 we can find h, the distance from midpoint to intersections.
			hh=Math.sqrt(r0*r0-a*a);
			if(Double.isNaN(hh)) throw new AssertionError();
			// get a normal to the line wop in the plane orthogonal to ortho
			r.cross(ortho,wop);
			r.scale(hh);
			arm.elbow.set(temp);
			//if(i%2==0) arm.elbow.add(r);
			//else
				arm.elbow.sub(r);

			temp.sub(arm.elbow,arm.shoulder);
			y=-temp.z;
			temp.z=0;
			x=temp.length();
			// use atan2 to find theta
			if( ( arm.shoulderToElbow.dot( temp ) ) < 0 ) x=-x;
			arm.angle= Math.toDegrees(Math.atan2(-y,x));
		}
	}

	Vector3d getNormalOfArmPlane(double i) {
		double v = Math.PI*2.0f * (i/3.0f - 1f/6f);
		return new Vector3d( Math.cos(v), Math.sin(v), 0);
	}
	
	private void rebuildShoulders(DeltaRobot3Memento keyframe) {
		Vector3d n1=new Vector3d(),o1=new Vector3d(),temp=new Vector3d();
		int i;
		for(i=0;i<3;++i) {
			DeltaRobot3Arm arma = arms[i];
			Vector3d ortho = getNormalOfArmPlane(i);

			Vector3d forward = MatrixHelper.getXAxis(this.myPose);
			Vector3d right   = MatrixHelper.getYAxis(this.myPose);
			Vector3d up      = MatrixHelper.getZAxis(this.myPose);

			//n1 = n* c + o*s;
			n1.set(forward);
			n1.scale(ortho.x);
			temp.set(right);
			temp.scale(ortho.y);
			n1.add(temp);
			n1.normalize();
			//o1 = n*-s + o*c;
			o1.set(forward);
			o1.scale(-ortho.y);
			temp.set(right);
			temp.scale(ortho.x);
			o1.add(temp);
			o1.normalize();
			//n1.scale(-1);


			//		    arma.shoulder = n1*BASE_TO_SHOULDER_X + motion_future.base_up*BASE_TO_SHOULDER_Z - o1*BASE_TO_SHOULDER_Y;
			arma.shoulder.set(n1);
			arma.shoulder.scale(DeltaRobot3.BASE_TO_SHOULDER_X);
			temp.set(up);
			temp.scale(DeltaRobot3.BASE_TO_SHOULDER_Z);
			arma.shoulder.add(temp);
			temp.set(o1);
			temp.scale(DeltaRobot3.BASE_TO_SHOULDER_Y);
			arma.shoulder.sub(temp);
			arma.shoulder.add(MatrixHelper.getPosition(this.myPose));

			//		    arma.elbow = n1*BASE_TO_SHOULDER_X + motion_future.base_up*BASE_TO_SHOULDER_Z - o1*(BASE_TO_SHOULDER_Y+BICEP_LENGTH);
			arma.elbow.set(n1);
			arma.elbow.scale(DeltaRobot3.BASE_TO_SHOULDER_X);
			temp.set(up);
			temp.scale(DeltaRobot3.BASE_TO_SHOULDER_Z);
			arma.elbow.add(temp);
			temp.set(o1);
			temp.scale(DeltaRobot3.BASE_TO_SHOULDER_Y+DeltaRobot3.BICEP_LENGTH);
			arma.elbow.sub(temp);
			//arma.shoulder.add(this.base);		    

			arma.shoulderToElbow.set(o1);
			arma.shoulderToElbow.scale(-1);
		}
	}


	//TODO check for collisions with http://geomalgorithms.com/a07-_distance.html#dist3D_Segment_to_Segment ?
	public boolean movePermitted() {/*
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


	public boolean checkAngleLimits() {
		// machine specific limits
		/*
		if (state.angle_0 < -180) return false;
		if (state.angle_0 >  180) return false;
		if (state.angle_2 <  -20) return false;
		if (state.angle_2 >  180) return false;
		if (state.angle_1 < -150) return false;
		if (state.angle_1 >   80) return false;
		if (state.angle_1 < -state.angle_2+ 10) return false;
		if (state.angle_1 > -state.angle_2+170) return false;

		if (state.angle_3 < -180) return false;
		if (state.angle_3 >  180) return false;
		if (state.angle_4 < -180) return false;
		if (state.angle_4 >  180) return false;
		if (state.angle_5 < -180) return false;
		if (state.angle_5 >  180) return false;
		 */	
		return true;
	}


	@Override
	public Memento createKeyframe() {
		return new DeltaRobot3Memento();
	}
}
