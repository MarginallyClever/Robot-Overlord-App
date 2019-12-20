package com.marginallyclever.robotOverlord.dhRobot.robots.sixi2;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JPanel;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.convenience.AnsiColors;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.DragBall;
import com.marginallyclever.robotOverlord.InputManager;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.dhRobot.DHKeyframe;
import com.marginallyclever.robotOverlord.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.dhRobot.DHRobot;
import com.marginallyclever.robotOverlord.dhRobot.solvers.DHIKSolver_RTTRTR;
import com.marginallyclever.robotOverlord.material.Material;
import com.marginallyclever.robotOverlord.model.ModelFactory;
import com.marginallyclever.robotOverlord.world.World;


public class Sixi2 extends DHRobot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean isFirstTime;
	public Material materialLive;
	public Material materialGhost;
	public boolean once = false;
	protected double feedrate;
	protected double acceleration;
	
	DHKeyframe receivedKeyframe;
	protected Sixi2Panel sixi2Panel;
	
	// contains a ghost
	public DHRobot ghost;

	protected DragBall ball;
	protected DHKeyframe homeKey;
	protected DHKeyframe restKey;
	
	
	public Sixi2() {
		super(new DHIKSolver_RTTRTR());
		setDisplayName("Sixi 2");

		// create one copy of the DH links for the ghost robot
		ghost = new DHRobot(new DHIKSolver_RTTRTR(),this);

		isFirstTime=true;
		receivedKeyframe = (DHKeyframe)createKeyframe();

		homeKey = (DHKeyframe)createKeyframe();
		getPoseFK(homeKey);

		restKey = (DHKeyframe)createKeyframe();
		restKey.fkValues[0]=0;
		restKey.fkValues[1]=-60;
		restKey.fkValues[2]=85;
		restKey.fkValues[3]=0;
		restKey.fkValues[4]=20;
		restKey.fkValues[5]=0;
		
		ball = new DragBall();
		ball.setParent(this);
		

		materialLive = new Material();
		materialLive.setDiffuseColor(1,217f/255f,33f/255f,1);
		
		materialGhost = new Material();
		materialGhost.setDiffuseColor(113f/255f, 211f/255f, 226f/255f,0.5f);
	}
	
	
	@Override
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		
		// hide the dhrobot panel because we'll replace it with our own.
		sixi2Panel = new Sixi2Panel(gui,this);
		
		list.remove(list.size()-1);
		list.add(sixi2Panel);
		
		return list;
	}
	
	@Override
	protected void setupLinks(DHRobot robot) {
		robot.setNumLinks(8);
		// roll anchor
		robot.links.get(0).setD(13.44);
		robot.links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(0).rangeMin=-120;
		robot.links.get(0).rangeMax=120;
		// tilt shoulder
		robot.links.get(1).setTheta(90);
		robot.links.get(1).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		robot.links.get(1).rangeMin=-72;
		// tilt elbow
		robot.links.get(2).setD(44.55);
		robot.links.get(2).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		robot.links.get(2).rangeMin=-83.369;
		robot.links.get(2).rangeMax=86;
		// interim point
		robot.links.get(3).setD(4.7201);
		robot.links.get(3).setAlpha(90);
		robot.links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		// roll ulna
		robot.links.get(4).setD(28.805);
		robot.links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(4).rangeMin=-175;
		robot.links.get(4).rangeMax=175;

		// tilt picassobox
		robot.links.get(5).setD(11.8);
		robot.links.get(5).setAlpha(25);
		robot.links.get(5).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		robot.links.get(5).rangeMin=-120;
		robot.links.get(5).rangeMax=120;
		// roll hand
		robot.links.get(6).setD(3.9527);
		robot.links.get(6).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(6).rangeMin=-180;
		robot.links.get(6).rangeMax=180;
		
		robot.links.get(7).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		
		robot.refreshPose();
	}
	
	public void setupModels(DHRobot robot) {
		try {
			robot.links.get(0).model = ModelFactory.createModelFromFilename("/Sixi2/anchor2.stl",0.1f);
			robot.links.get(1).model = ModelFactory.createModelFromFilename("/Sixi2/shoulder2.stl",0.1f);
			robot.links.get(2).model = ModelFactory.createModelFromFilename("/Sixi2/bicep2.stl",0.1f);
			robot.links.get(3).model = ModelFactory.createModelFromFilename("/Sixi2/forearm2.stl",0.1f);
			robot.links.get(5).model = ModelFactory.createModelFromFilename("/Sixi2/tuningFork2.stl",0.1f);
			robot.links.get(6).model = ModelFactory.createModelFromFilename("/Sixi2/picassoBox2.stl",0.1f);
			robot.links.get(7).model = ModelFactory.createModelFromFilename("/Sixi2/hand2.stl",0.1f);

			double ELBOW_TO_ULNA_Y = -28.805;
			double ELBOW_TO_ULNA_Z = 4.7201;
			double ULNA_TO_WRIST_Y = -11.800;
			double ULNA_TO_WRIST_Z = 0;
			double ELBOW_TO_WRIST_Y = ELBOW_TO_ULNA_Y + ULNA_TO_WRIST_Y;  //-40.605
			double ELBOW_TO_WRIST_Z = ELBOW_TO_ULNA_Z + ULNA_TO_WRIST_Z;  // 4.7201
			//double WRIST_TO_HAND = 8.9527;

			robot.links.get(0).model.adjustOrigin(new Vector3d(0, 0, 5.15));
			robot.links.get(1).model.adjustOrigin(new Vector3d(0, 0, -5.3));
			robot.links.get(2).model.adjustOrigin(new Vector3d(-1.82, 0, 9));
			robot.links.get(3).model.adjustOrigin(new Vector3d(0,ELBOW_TO_WRIST_Y,ELBOW_TO_WRIST_Z));
			robot.links.get(5).model.adjustOrigin(new Vector3d(0, 0, -ULNA_TO_WRIST_Y));
			robot.links.get(7).model.adjustOrigin(new Vector3d(0,0,-3.9527));

			robot.links.get(0).model.adjustRotation(new Vector3d(90,180,0));
			robot.links.get(1).model.adjustRotation(new Vector3d(90,-90,0));
			robot.links.get(2).model.adjustRotation(new Vector3d(90,0,0));
			robot.links.get(3).model.adjustRotation(new Vector3d(-90,0,180));
			robot.links.get(5).model.adjustRotation(new Vector3d(0,180,0));
			robot.links.get(6).model.adjustRotation(new Vector3d(180,0,180));
			robot.links.get(7).model.adjustRotation(new Vector3d(180,0,180));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void render(GL2 gl2) {
		if( isFirstTime ) {
			isFirstTime=false;
			setupModels(this);
			setupModels(ghost);
		}

		// draw the live robot
		materialLive.render(gl2);

		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, this.getMatrix());
			
			// Draw models
			gl2.glPushMatrix();
				Iterator<DHLink> i = links.iterator();
				while(i.hasNext()) {
					DHLink link = i.next();
					link.renderModel(gl2);
				}
				if(dhTool!=null) {
					dhTool.render(gl2);
				}
			gl2.glPopMatrix();
		
		gl2.glPopMatrix();
		
		// then draw the target pose, aka the ghost.
		super.render(gl2);
	}

	@Override
	public void drawTargetPose(GL2 gl2) {
		// draw the ghost pose
		materialGhost.render(gl2);
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, this.getMatrix());
			
			// Draw models
			gl2.glPushMatrix();
				Iterator<DHLink> i = ghost.links.iterator();
				while(i.hasNext()) {
					DHLink link = i.next();
					//if(showAngles) link.renderAngles(gl2);
					link.renderModel(gl2);
		    		materialGhost.render(gl2);
		    		link.setAngleColorByRange(gl2);
				}
				if(ghost.dhTool!=null) {
					ghost.dhTool.render(gl2);
					ghost.dhTool.dhLinkEquivalent.renderAngles(gl2);
				}
			gl2.glPopMatrix();
		gl2.glPopMatrix();
		
		if(drawAsSelected && inDirectDriveMode()) {
    		IntBuffer depthFunc = IntBuffer.allocate(1);
			gl2.glGetIntegerv(GL2.GL_DEPTH_FUNC, depthFunc);
			boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
			gl2.glDepthFunc(GL2.GL_ALWAYS);
			gl2.glDisable(GL2.GL_LIGHTING);

			gl2.glPushMatrix();
				MatrixHelper.applyMatrix(gl2, this.getMatrix());
			if (InputManager.isOn(InputManager.KEY_LSHIFT) || InputManager.isOn(InputManager.KEY_RSHIFT)) {
				ball.renderRotation(gl2);
			} else {
				ball.renderTranslation(gl2);
			}
			gl2.glPopMatrix();
			gl2.glDepthFunc(depthFunc.get(0));
			if (isLit) gl2.glEnable(GL2.GL_LIGHTING);
		}
		
    	super.drawTargetPose(gl2);
	}
	
	
	public void sendNewStateToRobot(DHKeyframe keyframe) {
		if(once == false) {
			once = true;
			// turn off error flags in firmware
			sendLineToRobot("D20");
		}
		
		String message = "G0"
			    		+" X"+(StringHelper.formatDouble(keyframe.fkValues[0]))
			    		+" Y"+(StringHelper.formatDouble(keyframe.fkValues[1]))
			    		+" Z"+(StringHelper.formatDouble(keyframe.fkValues[2]))
			    		+" U"+(StringHelper.formatDouble(keyframe.fkValues[3]))
			    		+" V"+(StringHelper.formatDouble(keyframe.fkValues[4]))
			    		+" W"+(StringHelper.formatDouble(keyframe.fkValues[5]))
			    		+" F"+(StringHelper.formatDouble(feedrate))
			    	    +" A"+(StringHelper.formatDouble(acceleration))
			    	    ;

		if(dhTool!=null) {
			double t=dhTool.getAdjustableValue();
			message += " T"+(t);
			System.out.println("Servo="+t);
		}
		
		System.out.println(AnsiColors.RED+message+AnsiColors.RESET);
		sendLineToRobot(message);
		isReadyToReceive=false;
	}

	/**
	 * read D17 values from sixi robot
	 */
	@Override
	@SuppressWarnings("unused")
	public void dataAvailable(NetworkConnection arg0,String data) {
		if(data.contains("D17")) {
			if(data.startsWith(">")) {
				data=data.substring(1).trim();
			}

			if(data.startsWith("D17")) {
				String [] dataParts = data.split("\\s");
				if(dataParts.length>=7) {
					try {
						receivedKeyframe.fkValues[0]=Double.parseDouble(dataParts[1]);
						receivedKeyframe.fkValues[1]=Double.parseDouble(dataParts[2]);
						receivedKeyframe.fkValues[2]=Double.parseDouble(dataParts[3]);
						receivedKeyframe.fkValues[3]=Double.parseDouble(dataParts[4]);
						receivedKeyframe.fkValues[4]=Double.parseDouble(dataParts[5]);
						receivedKeyframe.fkValues[5]=Double.parseDouble(dataParts[6]);

						if(false) {
							String message = "D17 "
						    		+" X"+(StringHelper.formatDouble(receivedKeyframe.fkValues[0]))
						    		+" Y"+(StringHelper.formatDouble(receivedKeyframe.fkValues[1]))
						    		+" Z"+(StringHelper.formatDouble(receivedKeyframe.fkValues[2]))
						    		+" U"+(StringHelper.formatDouble(receivedKeyframe.fkValues[3]))
						    		+" V"+(StringHelper.formatDouble(receivedKeyframe.fkValues[4]))
						    		+" W"+(StringHelper.formatDouble(receivedKeyframe.fkValues[5]));
							System.out.println(AnsiColors.BLUE+message+AnsiColors.RESET);
						}
						//data = data.replace('\n', ' ');

						//smoothing to new position
						//DHKeyframe inter = (DHKeyframe)createKeyframe();
						//inter.interpolate(poseNow,receivedKeyframe, 0.5);
						//this.setRobotPose(inter);

						setPoseFK(receivedKeyframe);

					} catch(Exception e) {}
				}
			}
			return;
		} else {
			data=data.replace("\n", "");
			System.out.println(AnsiColors.PURPLE+data+AnsiColors.RESET);
		}
		super.dataAvailable(arg0, data);
	}
	

	/**
	 * End matrix (the pose of the robot arm gripper) is stored on the PC in the text format "G0 X.. Y.. Z.. I.. J.. K.. T..\n"
	 * where XYZ are translation values and IJK are euler rotations of the matrix.
	 * @return the text format for the current pose.
	 */
	public String generateGCode() {
		Matrix3d m1 = new Matrix3d();
		Vector3d t1 = new Vector3d();
		//assert(endMatrix.get(m1, t1)==1);  // get returns scale, which should be 1.
		
		// get the end matrix, which includes any tool, of the ghost.
		ghost.getLiveMatrix().get(m1, t1);
		
		//Vector3d e1 = MatrixHelper.matrixToEuler(m1);
		
		String message = "G0"
				+" X"+StringHelper.formatDouble(t1.x)
				+" Y"+StringHelper.formatDouble(t1.y)
				+" Z"+StringHelper.formatDouble(t1.z)
				//+" I"+StringHelper.formatDouble(Math.toDegrees(e1.x))
				//+" J"+StringHelper.formatDouble(Math.toDegrees(e1.y))
				//+" K"+StringHelper.formatDouble(Math.toDegrees(e1.z))
				+" F"+StringHelper.formatDouble(feedrate)
				+" A"+StringHelper.formatDouble(acceleration)
				;
		if(dhTool!=null) {
			// add special tool commands
			message += dhTool.generateGCode();
		}
		return message;
	}
	
	/**
	 * End matrix (the pose of the robot arm gripper) is stored on the PC in the text format "G0 X.. Y.. Z.. I.. J.. K.. T..\n"
	 * where XYZ are translation values and IJK are euler rotations of the matrix.
	 * The text, if parsed ok, will set the current end matrix of the ghost.
	 * @param line the text format of one pose.
	 */
	public void parseGCode(String line) {
		if(line.trim().length()==0) return;

		StringTokenizer tokens = new StringTokenizer(line);
		if(tokens.countTokens()==0) return;
		tokens.nextToken();
		
		if( line.startsWith("G4 ") || line.startsWith("G04 ") ) {
			// dwell

			while(tokens.hasMoreTokens()) {
				String token = tokens.nextToken();
				switch(token.charAt(0)) {
				case 'S':  dwellTime += 1000 * Double.parseDouble(token.substring(1));  break;  // seconds
				case 'P':  dwellTime +=        Double.parseDouble(token.substring(1));  break;  // milliseconds
				default:  break;
				}
			}
		}
		if( line.startsWith("G0 ") || line.startsWith("G00 ") ) {
			Vector3d t1 = new Vector3d();
			Matrix3d m1 = new Matrix3d();
			targetMatrix.get(m1,t1);
			Vector3d e1 = MatrixHelper.matrixToEuler(m1);
			boolean isDirty=false;
			
			while(tokens.hasMoreTokens()) {
				String token = tokens.nextToken();
				
				switch(token.charAt(0)) {
				case 'X':  isDirty=true;  t1.x = Double.parseDouble(token.substring(1));  break;
				case 'Y':  isDirty=true;  t1.y = Double.parseDouble(token.substring(1));  break;
				case 'Z':  isDirty=true;  t1.z = Double.parseDouble(token.substring(1));  break;
				//case 'I':  isDirty=true;  e1.x = Math.toRadians(Double.parseDouble(token.substring(1)));  break;
				//case 'J':  isDirty=true;  e1.y = Math.toRadians(Double.parseDouble(token.substring(1)));  break;
				//case 'K':  isDirty=true;  e1.z = Math.toRadians(Double.parseDouble(token.substring(1)));  break;
				case 'F':  isDirty=true;  feedrate = Double.parseDouble(token.substring(1));  break;
				case 'A':  isDirty=true;  acceleration = Double.parseDouble(token.substring(1));  break;
				default:  break;
				}
			}
			
			if(isDirty) {
				// changing the target pose of the ghost
				m1 = MatrixHelper.eulerToMatrix(e1);
				Matrix4d m2=new Matrix4d();
				m2.set(m1);
				m2.setTranslation(t1);
				ghost.setTargetMatrix(m2);
			}
			
			if(ghost.dhTool!=null) {
				ghost.dhTool.parseGCode(line);
			}
		}
	}

	/**
	 * Use Forward Kinematics to approximate the Jacobian matrix for Sixi.
	 * See also https://robotacademy.net.au/masterclass/velocity-kinematics-in-3d/?lesson=346
	 */
	public double [][] approximateJacobian(DHKeyframe keyframe) {
		double [][] jacobian = new double[6][6];
		
		double ANGLE_STEP_SIZE_DEGREES=0.5;  // degrees
		
		DHKeyframe keyframe2 = (DHKeyframe)createKeyframe();

		// use anglesA to get the hand matrix
		DHRobot clone = new DHRobot(this);
		clone.setPoseFK(keyframe);
		Matrix4d T = new Matrix4d(clone.getLiveMatrix());
		
		// for all joints
		int i,j;
		for(i=0;i<6;++i) {
			// use anglesB to get the hand matrix after a tiiiiny adjustment on one joint.
			for(j=0;j<6;++j) {
				keyframe2.fkValues[j]=keyframe.fkValues[j];
			}
			keyframe2.fkValues[i]+=ANGLE_STEP_SIZE_DEGREES;

			clone.setPoseFK(keyframe2);
			Matrix4d Tnew = new Matrix4d(clone.getLiveMatrix());
			
			// use the finite difference in the two matrixes
			// aka the approximate the rate of change (aka the integral, aka the velocity)
			// in one column of the jacobian matrix at this position.
			Matrix4d dT = new Matrix4d();
			dT.sub(Tnew,T);
			dT.mul(1.0/Math.toRadians(ANGLE_STEP_SIZE_DEGREES));
			
			jacobian[i][0]=dT.m03;
			jacobian[i][1]=dT.m13;
			jacobian[i][2]=dT.m23;


			// find the rotation part
			// these initialT and initialTd were found in the comments on
			// https://robotacademy.net.au/masterclass/velocity-kinematics-in-3d/?lesson=346
			// and used to confirm that our skew-symmetric matrix match theirs.
			/*
			double[] initialT = {
					 0,  0   , 1   ,  0.5963,
					 0,  1   , 0   , -0.1501,
					-1,  0   , 0   , -0.01435,
					 0,  0   , 0   ,  1 };
			double[] initialTd = {
					 0, -0.01, 1   ,  0.5978,
					 0,  1   , 0.01, -0.1441,
					-1,  0   , 0   , -0.01435,
					 0,  0   , 0   ,  1 };
			T.set(initialT);
			Td.set(initialTd);
			dT.sub(Td,T);
			dT.mul(1.0/Math.toRadians(ANGLE_STEP_SIZE_DEGREES));//*/
			
			//System.out.println("T="+T);
			//System.out.println("Td="+Td);
			//System.out.println("dT="+dT);
			Matrix3d T3 = new Matrix3d(
					T.m00,T.m01,T.m02,
					T.m10,T.m11,T.m12,
					T.m20,T.m21,T.m22);
			//System.out.println("R="+R);
			Matrix3d dT3 = new Matrix3d(
					dT.m00,dT.m01,dT.m02,
					dT.m10,dT.m11,dT.m12,
					dT.m20,dT.m21,dT.m22);
			//System.out.println("dT3="+dT3);
			Matrix3d skewSymmetric = new Matrix3d();
			
			T3.transpose();  // inverse of a rotation matrix is its transpose
			skewSymmetric.mul(dT3,T3);
			
			//System.out.println("SS="+skewSymmetric);
			//[  0 -Wz  Wy]
			//[ Wz   0 -Wx]
			//[-Wy  Wx   0]
			
			jacobian[i][3]=skewSymmetric.m12;
			jacobian[i][4]=skewSymmetric.m20;
			jacobian[i][5]=skewSymmetric.m01;
		}
		
		return jacobian;
	}


	public double getFeedrate() {
		return feedrate;
	}


	public void setFeedrate(double feedrate) {
		this.feedrate = feedrate;
	}


	public double getAcceleration() {
		return acceleration;
	}


	public void setAcceleration(double acceleration) {
		this.acceleration = acceleration;
	}

	public Matrix4d getGhostTargetMatrixWorldSpace() {
		Matrix4d targetMatrixWorldSpace = new Matrix4d();
		targetMatrixWorldSpace.mul(this.getMatrix(),ghost.getTargetMatrix());
		return targetMatrixWorldSpace; 
	}

	/**
	 * move the finger tip of the arm if the InputManager says so. The direction and
	 * torque of the movement is controlled by a frame of reference.
	 * 
	 * @return true if targetPose changes.
	 */
	public boolean driveFromKeyState(double dt) {
		Matrix4d m = getGhostTargetMatrixWorldSpace();
		Vector3d trans = new Vector3d(m.m03,m.m13,m.m23);
		
		Matrix4d frameOfRef;
		switch(frameOfReferenceIndex) {
		case FRAME_FINGER:
			// use the robot's finger tip as the frame of reference
			frameOfRef = getGhostTargetMatrixWorldSpace();
			frameOfRef.invert();
			break;
		case FRAME_CAMERA:
			// use the camera as the frame of reference.
			frameOfRef = new Matrix4d(MatrixHelper.lookAt(trans, getWorld().getCamera().getPosition()));
			
			//frameOfRef = new Matrix4d(getWorld().getCamera().getMatrix());
			frameOfRef.m03=0;
			frameOfRef.m13=0;
			frameOfRef.m23=0;

			m.set(frameOfRef);
			m.setTranslation(trans);
			break;
		case FRAME_WORLD:
		default:
			// use the world as the frame of reference.
			frameOfRef = new Matrix4d(World.getPose());
			m.set(frameOfRef);
			m.setTranslation(trans);
			break;
		}

		ball.setMatrix(m);
		ghost.getTargetMatrix().get(ball.targetMatrixToSave);
		frameOfRef.get(ball.FORToSave);

		if (InputManager.isOn(InputManager.KEY_LSHIFT) || InputManager.isOn(InputManager.KEY_RSHIFT)) {
			ball.updateRotation(dt);
		} else {
			ball.updateTranslation(dt);
		}
		
		boolean isDirty = false;
		final double scale = 10*dt;
		final double scaleDolly = 10*dt;

		//if (InputManager.isOn(InputManager.STICK_SQUARE)) {}
		//if (InputManager.isOn(InputManager.STICK_CIRCLE)) {}
		//if (InputManager.isOn(InputManager.STICK_X)) {}
		if (InputManager.isOn(InputManager.STICK_TRIANGLE)) {
			ghost.setPoseFK(homeKey);
			ghost.setTargetMatrix(ghost.getLiveMatrix());
			isDirty = true;
		}
		if (InputManager.isOn(InputManager.STICK_TOUCHPAD)) {
			// this.toggleATC();
		}

		int dD = (int) InputManager.rawValue(InputManager.STICK_DPADY);
		if (dD != 0) {
			double d =dhTool.dhLinkEquivalent.getD() + dD * scaleDolly; 
			dhTool.dhLinkEquivalent.setD(Math.max(d, 0));
			isDirty = true;
		}
		int dR = (int) InputManager.rawValue(InputManager.STICK_DPADX); // dpad left/right
		if (dR != 0) {
			double r =dhTool.dhLinkEquivalent.getR() + dR * scale; 
			dhTool.dhLinkEquivalent.setR(Math.max(r,0));
			isDirty = true;
		}
/*
		// https://robotics.stackexchange.com/questions/12782/how-rotate-a-point-around-an-arbitrary-line-in-3d
		if (InputManager.isOn(InputManager.STICK_L1) != InputManager.isOn(InputManager.STICK_R1)) {
			if (canTargetPoseRotateZ()) {
				isDirty = true;
				double vv = scaleTurnRadians;
				if (dhTool != null && dhTool.dhLinkEquivalent.r > 1) {
					vv /= dhTool.dhLinkEquivalent.r;
				}

				rollZ(frameOfReference, InputManager.isOn(InputManager.STICK_L1) ? vv : -vv);
			}
		}

		if (InputManager.rawValue(InputManager.STICK_RX) != 0) {
			if (canTargetPoseRotateY()) {
				isDirty = true;
				rollY(frameOfReference, InputManager.rawValue(InputManager.STICK_RX) * scaleTurnRadians);
			}
		}
		if (InputManager.rawValue(InputManager.STICK_RY) != 0) {
			if (canTargetPoseRotateX()) {
				isDirty = true;
				rollX(frameOfReference, InputManager.rawValue(InputManager.STICK_RY) * scaleTurnRadians);
			}
		}
		if (InputManager.rawValue(InputManager.STICK_R2) != -1) {
			isDirty = true;
			translate(getForward(), ((InputManager.rawValue(InputManager.STICK_R2) + 1) / 2) * scale);
		}
		if (InputManager.rawValue(InputManager.STICK_L2) != -1) {
			isDirty = true;
			translate(getForward(), ((InputManager.rawValue(InputManager.STICK_L2) + 1) / 2) * -scale);
		}
		if (InputManager.rawValue(InputManager.STICK_LX) != 0) {
			isDirty = true;
			translate(getRight(), InputManager.rawValue(InputManager.STICK_LX) * scale);
		}
		if (InputManager.rawValue(InputManager.STICK_LY) != 0) {
			isDirty = true;
			translate(getUp(), InputManager.rawValue(InputManager.STICK_LY) * -scale);
		}*/
		

		if (InputManager.isOn(InputManager.MOUSE_LEFT)) {
			if (InputManager.isOn(InputManager.KEY_LSHIFT) || InputManager.isOn(InputManager.KEY_RSHIFT)) {
				if(ball.wasPressed) {
					double da=ball.angleRadNow - ball.angleRadSaved;

					if(da!=0) {
						switch(ball.nearestPlaneSaved) {
						case 0 : if(canTargetPoseRotateX()) rollX(da);	break;
						case 1 : if(canTargetPoseRotateY()) rollY(da);	break;
						default: if(canTargetPoseRotateZ()) rollZ(da);	break;
						}
					}
					isDirty = true;
				}
			}/*
			 else if (InputManager.isOn(InputManager.KEY_LCONTROL) || InputManager.isOn(InputManager.KEY_RCONTROL)) {
				
				if (InputManager.rawValue(InputManager.MOUSE_Y) != 0) {
					double d = dhTool.dhLinkEquivalent.getD()
							+ InputManager.rawValue(InputManager.MOUSE_Y) * scaleDolly;
					dhTool.dhLinkEquivalent.setD(Math.max(d, 0));
					isDirty = true;
				}
				if (InputManager.rawValue(InputManager.MOUSE_X) != 0) {
					double r = dhTool.dhLinkEquivalent.getR() 
							+ InputManager.rawValue(InputManager.MOUSE_X) * scaleDolly;
					dhTool.dhLinkEquivalent.setR(Math.max(r, 0));
					isDirty = true;
				}
			}*/ else {
				if(ball.wasPressed) {
					double dx = InputManager.rawValue(InputManager.MOUSE_X) * scale * 0.5;
					double dy = InputManager.rawValue(InputManager.MOUSE_Y) * -scale * 0.5;
					double da=0;
					
					switch(ball.majorAxisSlideDirection) {
					case DragBall.SLIDE_XPOS:  da= dx;	break;
					case DragBall.SLIDE_XNEG:  da=-dx;  break;
					case DragBall.SLIDE_YPOS:  da= dy;  break;
					case DragBall.SLIDE_YNEG:  da=-dy;  break;
					}
					
					if(da!=0) {
						switch(ball.majorAxisSaved) {
						case 1 : translate(getForward(), da);	break;
						case 2 : translate(getRight()  , da);	break;
						default: translate(getUp()     , da);	break;
						}
					}
					isDirty = true;
				}
			}
		}

		if (dhTool != null) {
			isDirty |= dhTool.directDrive();
		}

		if (InputManager.isReleased(InputManager.KEY_RETURN) 
				|| InputManager.isReleased(InputManager.KEY_ENTER)
				|| InputManager.isReleased(InputManager.STICK_X) 
				|| immediateDriving) {
			// commit move!
			if(connection != null && connection.isOpen()) {
				DHKeyframe key = (DHKeyframe)createKeyframe();
				ghost.getPoseFK(key);
				sendNewStateToRobot(key);
			} else {
				InterpolationStep s = new InterpolationStep();
				s.target = ghost.getTargetMatrix();
				s.feedrate = getFeedrate();
				interpolationQueue.offer(s);
			}
		}

		if (InputManager.isOn(InputManager.KEY_DELETE) || InputManager.isOn(InputManager.STICK_TRIANGLE)) {
			// reset targetpose to endmatrix.
			ghost.setTargetMatrix(liveMatrix);
		}

		return isDirty;
	}

	public Vector3d getForward() {
		Matrix3d frameOfReference = ball.FORSaved;
		return new Vector3d(frameOfReference.m00, frameOfReference.m10, frameOfReference.m20);
	}

	public Vector3d getRight() {
		Matrix3d frameOfReference = ball.FORSaved;
		return new Vector3d(frameOfReference.m01, frameOfReference.m11, frameOfReference.m21);
	}

	public Vector3d getUp() {
		Matrix3d frameOfReference = ball.FORSaved;
		return new Vector3d(frameOfReference.m02, frameOfReference.m12, frameOfReference.m22);
	}
	
	protected void rotationInternal(Matrix3d rotation) {
		// multiply robot origin by target matrix to get target matrix in world space.
		
		// invert frame of reference to transform world target matrix into frame of reference space.
		Matrix3d FOR = new Matrix3d(ball.FORSaved);
		Matrix3d robotRoot = new Matrix3d();
		this.getMatrix().get(robotRoot);
		robotRoot.invert();
		FOR.mul(robotRoot);
		
		Matrix3d ifor = new Matrix3d(FOR);
		ifor.invert();
		
		Matrix3d targetMatrixRobotSpaceAfterTransform = new Matrix3d(ifor);
		targetMatrixRobotSpaceAfterTransform.mul(rotation);
		targetMatrixRobotSpaceAfterTransform.mul(FOR);

		Matrix3d m1 = new Matrix3d(ball.targetMatrixSaved);
		targetMatrixRobotSpaceAfterTransform.mul(m1);
		
		// done!
		Vector3d tempPosition = new Vector3d();
		ghost.getTargetMatrix().get(tempPosition);
		Matrix4d m = new Matrix4d();
		m.set(targetMatrixRobotSpaceAfterTransform);
		m.setTranslation(tempPosition);
		setDisablePanel(true);
		ghost.setTargetMatrix(m);
		setDisablePanel(false);
	}

	protected void rollX(double angRadians) {
		// apply transform about the origin,
		Matrix3d temp = new Matrix3d();
		temp.rotX(angRadians);
		rotationInternal(temp);
	}

	protected void rollY(double angRadians) {
		// apply transform about the origin,
		Matrix3d temp = new Matrix3d();
		temp.rotY(angRadians);
		rotationInternal(temp);
	}
	protected void rollZ(double angRadians) {
		Matrix3d temp = new Matrix3d();
		temp.rotZ(angRadians);
		rotationInternal(temp);
	}

	protected void translate(Vector3d v, double amount) {
		Matrix4d m = ghost.getTargetMatrix();
		m.m03 += v.x*amount;
		m.m13 += v.y*amount;
		m.m23 += v.z*amount;
		setDisablePanel(true);
		ghost.setTargetMatrix(m);
		setDisablePanel(false);
	}
	
	@Override
	public void update(double dt) {
		super.update(dt);

		if(connection==null || connection.isOpen()) {
			this.interpolate(dt);
		}
		
		// If the move is illegal then I need a way to rewind. Keep the old pose for
		// rewinding.

		if (inDirectDriveMode() && drawAsSelected) {
			if (driveFromKeyState(dt)) {
				if (panel != null)
					panel.updateGhostEnd();
			}
		}
	}

}
