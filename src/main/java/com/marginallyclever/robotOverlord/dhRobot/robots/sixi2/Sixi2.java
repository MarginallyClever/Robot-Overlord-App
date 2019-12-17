package com.marginallyclever.robotOverlord.dhRobot.robots.sixi2;

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
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.dhRobot.DHIKSolver;
import com.marginallyclever.robotOverlord.dhRobot.DHKeyframe;
import com.marginallyclever.robotOverlord.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.dhRobot.DHRobot;
import com.marginallyclever.robotOverlord.dhRobot.solvers.DHIKSolver_RTTRTR;
import com.marginallyclever.robotOverlord.material.Material;
import com.marginallyclever.robotOverlord.model.ModelFactory;


public class Sixi2 extends DHRobot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean isFirstTime;
	public Material material;
	public Material materialGhost;
	public boolean once = false;
	protected double feedrate;
	protected double acceleration;
	
	DHKeyframe receivedKeyframe;
	protected Sixi2Panel sixi2Panel;
	
	
	public Sixi2() {
		super();
		setDisplayName("Sixi 2");
		isFirstTime=true;
		receivedKeyframe = (DHKeyframe)createKeyframe();
		feedrate=40;
		acceleration=20;
	}
	
	
	@Override
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		
		sixi2Panel = new Sixi2Panel(gui,this);
		list.add(sixi2Panel);
		
		return list;
	}
	
	@Override
	protected void setupLinks() {
		setNumLinks(8);
		// roll anchor
		links.get(0).d=13.44;
		links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(0).rangeMin=-120;
		links.get(0).rangeMax=120;
		// tilt shoulder
		links.get(1).theta=90;
		links.get(1).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(1).rangeMin=-72;
		// tilt elbow
		links.get(2).d=44.55;
		links.get(2).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(2).rangeMin=-83.369;
		links.get(2).rangeMax=86;
		// interim point
		links.get(3).d=4.7201;
		links.get(3).alpha=90;
		links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		// roll ulna
		links.get(4).d=28.805;
		links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(4).rangeMin=-175;
		links.get(4).rangeMax=175;

		// tilt picassobox
		links.get(5).d=11.8;
		links.get(5).alpha=25;
		links.get(5).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(5).rangeMin=-120;
		links.get(5).rangeMax=120;
		// roll hand
		links.get(6).d=3.9527;
		links.get(6).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(6).rangeMin=-180;
		links.get(6).rangeMax=180;
		
		links.get(7).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
	}
	
	public void setupModels() {
		material = new Material();
		material.setDiffuseColor(1,217f/255f,33f/255f,1);
		
		materialGhost = new Material();
		materialGhost.setDiffuseColor(113f/255f, 211f/255f, 226f/255f,0.5f);
		
		try {
			links.get(0).model = ModelFactory.createModelFromFilename("/Sixi2/anchor2.stl",0.1f);
			links.get(1).model = ModelFactory.createModelFromFilename("/Sixi2/shoulder2.stl",0.1f);
			links.get(2).model = ModelFactory.createModelFromFilename("/Sixi2/bicep2.stl",0.1f);
			links.get(3).model = ModelFactory.createModelFromFilename("/Sixi2/forearm2.stl",0.1f);
			links.get(5).model = ModelFactory.createModelFromFilename("/Sixi2/tuningFork2.stl",0.1f);
			links.get(6).model = ModelFactory.createModelFromFilename("/Sixi2/picassoBox2.stl",0.1f);
			links.get(7).model = ModelFactory.createModelFromFilename("/Sixi2/hand2.stl",0.1f);

			double ELBOW_TO_ULNA_Y = -28.805;
			double ELBOW_TO_ULNA_Z = 4.7201;
			double ULNA_TO_WRIST_Y = -11.800;
			double ULNA_TO_WRIST_Z = 0;
			double ELBOW_TO_WRIST_Y = ELBOW_TO_ULNA_Y + ULNA_TO_WRIST_Y;  //-40.605
			double ELBOW_TO_WRIST_Z = ELBOW_TO_ULNA_Z + ULNA_TO_WRIST_Z;  // 4.7201
			//double WRIST_TO_HAND = 8.9527;

			links.get(0).model.adjustOrigin(new Vector3d(0, 0, 5.15));
			links.get(1).model.adjustOrigin(new Vector3d(0, 0, -5.3));
			links.get(2).model.adjustOrigin(new Vector3d(-1.82, 0, 9));
			links.get(3).model.adjustOrigin(new Vector3d(0,ELBOW_TO_WRIST_Y,ELBOW_TO_WRIST_Z));
			links.get(5).model.adjustOrigin(new Vector3d(0, 0, -ULNA_TO_WRIST_Y));
			links.get(7).model.adjustOrigin(new Vector3d(0,0,-3.9527));

			links.get(0).model.adjustRotation(new Vector3d(90,180,0));
			links.get(1).model.adjustRotation(new Vector3d(90,-90,0));
			links.get(2).model.adjustRotation(new Vector3d(90,0,0));
			links.get(3).model.adjustRotation(new Vector3d(-90,0,180));
			links.get(5).model.adjustRotation(new Vector3d(0,180,0));
			links.get(6).model.adjustRotation(new Vector3d(180,0,180));
			links.get(7).model.adjustRotation(new Vector3d(180,0,180));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void render(GL2 gl2) {
		if( isFirstTime ) {
			isFirstTime=false;
			setupModels();
		}
		
		material.render(gl2);

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
		
		super.render(gl2);
	}

	@Override
	public void drawTargetPose(GL2 gl2) {
		// is there a valid ghost pose?
    	DHIKSolver solver = this.getSolverIK();
    	DHKeyframe keyframe = (DHKeyframe)this.createKeyframe();
    	//solver.solve(this,targetMatrix,keyframe);
    	solver.solveWithSuggestion(this,targetMatrix,keyframe,poseNow);
    	if(solver.solutionFlag==DHIKSolver.ONE_SOLUTION) {
    		// save the live pose
    		DHKeyframe saveKeyframe = this.getRobotPose();
    		// set the ghost pose
    		this.setDisablePanel(true);
    		this.setLivePose(keyframe);
    		// draw the ghost pose
    		materialGhost.render(gl2);
    		gl2.glPushMatrix();
				MatrixHelper.applyMatrix(gl2, this.getMatrix());
				
				// Draw models
				gl2.glPushMatrix();
					Iterator<DHLink> i = links.iterator();
					while(i.hasNext()) {
						DHLink link = i.next();
						//if(showAngles) link.renderAngles(gl2);
						link.renderModel(gl2);
			    		materialGhost.render(gl2);
						link.setAngleColorByRange(gl2);
					}
					if(dhTool!=null) {
						dhTool.render(gl2);
						dhTool.dhLinkEquivalent.renderAngles(gl2);
					}
				gl2.glPopMatrix();
			gl2.glPopMatrix();
			// reset the live pose
			this.setLivePose(saveKeyframe);
			this.setDisablePanel(false);
    	}
    	super.drawTargetPose(gl2);
	}
	
	@Override
	public DHIKSolver getSolverIK() {
		return new DHIKSolver_RTTRTR();
	}
	
	@Override
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

						this.setLivePose(receivedKeyframe);

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
		
		// get the end matrix, which includes any tool.
		liveMatrix.get(m1, t1);
		
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
	 * The text, if parsed ok, will set the current end matrix.
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
				m1 = MatrixHelper.eulerToMatrix(e1);
				// changing the target pose will direct the live robot to that position.
				targetMatrix.set(m1);
				targetMatrix.setTranslation(t1);
			}
			
			if(dhTool!=null) {
				dhTool.parseGCode(line);
			}

			moveToTargetPose();
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
		setLivePose(keyframe);
		Matrix4d T = new Matrix4d(getLiveMatrix());
		
		// for all joints
		int i,j;
		for(i=0;i<6;++i) {
			// use anglesB to get the hand matrix after a tiiiiny adjustment on one joint.
			for(j=0;j<6;++j) {
				keyframe2.fkValues[j]=keyframe.fkValues[j];
			}
			keyframe2.fkValues[i]+=ANGLE_STEP_SIZE_DEGREES;

			setLivePose(keyframe2);
			Matrix4d Tnew = new Matrix4d(getLiveMatrix());
			
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
			Matrix3d T3 = new Matrix3d(
					T.m00,T.m01,T.m02,
					T.m10,T.m11,T.m12,
					T.m20,T.m21,T.m22);
			Matrix3d dT3 = new Matrix3d(
					dT.m00,dT.m01,dT.m02,
					dT.m10,dT.m11,dT.m12,
					dT.m20,dT.m21,dT.m22);
			Matrix3d skewSymmetric = new Matrix3d();
			
			T3.transpose();  // inverse of a rotation matrix is its transpose
			skewSymmetric.mul(dT3,T3);
			
			jacobian[i][3]=skewSymmetric.m12;
			jacobian[i][4]=skewSymmetric.m20;
			jacobian[i][5]=skewSymmetric.m01;
		}
		
		// return the live matrix where it was
		setLivePose(keyframe);
		
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
}
