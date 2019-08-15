package com.marginallyclever.robotOverlord.dhRobot;

import java.util.Iterator;
import java.util.StringTokenizer;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.convenience.AnsiColors;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.material.Material;
import com.marginallyclever.robotOverlord.model.ModelFactory;


public class DHRobot_Sixi2 extends DHRobot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// translate the values between the robot and the software
	// TODO these values are adjusted per-robot and should live in the firmware.
	private static double ADJUST0 = -140;
	private static double ADJUST1 = 90;
	private static double ADJUST2 = -62.5;
	private static double ADJUST3 = 180+150;
	private static double ADJUST4 = 135+90;
	private static double ADJUST5 = 90+30;

	// scale the values between the robot and the software
	// TODO these values are adjusted per-robot and should live in the firmware.
	private static double SCALE_0=-1;
	private static double SCALE_1=1;
	private static double SCALE_2=1;
	private static double SCALE_3=-1;
	private static double SCALE_4=1;
	private static double SCALE_5=-1;
	

	public boolean isFirstTime;
	public Material material;
	public Material materialGhost;
	public boolean once = false;
	
	DHKeyframe receivedKeyframe;
	
	public DHRobot_Sixi2() {
		super();
		setDisplayName("Sixi 2");
		isFirstTime=true;
		receivedKeyframe = (DHKeyframe)createKeyframe();
	}
	
	@Override
	public void setupLinks() {
		setNumLinks(8);
		// roll
		links.get(0).d=13.44;
		links.get(0).theta=0;
		links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(0).rangeMin=-120;
		links.get(0).rangeMax=120;
		// tilt
		links.get(1).alpha=0;
		links.get(1).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(1).rangeMin=-72;
		// tilt
		links.get(2).d=44.55;
		links.get(2).alpha=0;
		links.get(2).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(2).rangeMin=-83.369;
		links.get(2).rangeMax=86;
		// interim point
		links.get(3).d=4.7201;
		links.get(3).alpha=90;
		links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		// roll
		links.get(4).d=28.805;
		links.get(4).theta=0;
		links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(4).rangeMin=-170;
		links.get(4).rangeMax=170;

		// tilt
		links.get(5).d=11.8;
		links.get(5).alpha=0;
		links.get(5).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(5).rangeMin=-120;
		links.get(5).rangeMax=120;
		// roll
		links.get(6).d=3.9527;
		links.get(6).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(6).rangeMin=-180;
		links.get(6).rangeMax=180;
		
		links.get(7).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		
		this.refreshPose();
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

			double ELBOW_TO_ULNA_Y = -28.805f;
			double ELBOW_TO_ULNA_Z = 4.7201f;
			double ULNA_TO_WRIST_Y = -11.800f;
			double ULNA_TO_WRIST_Z = 0;
			double ELBOW_TO_WRIST_Y = ELBOW_TO_ULNA_Y + ULNA_TO_WRIST_Y;
			double ELBOW_TO_WRIST_Z = ELBOW_TO_ULNA_Z + ULNA_TO_WRIST_Z;
			//double WRIST_TO_HAND = 8.9527;

			links.get(0).model.adjustOrigin(new Vector3d(0, 0, 5.150f));
			links.get(1).model.adjustOrigin(new Vector3d(0, 0, 8.140f-13.44f));
			links.get(2).model.adjustOrigin(new Vector3d(-1.82f, 0, 9));
			links.get(3).model.adjustOrigin(new Vector3d(0, (float)ELBOW_TO_WRIST_Y, (float)ELBOW_TO_WRIST_Z));
			links.get(5).model.adjustOrigin(new Vector3d(0, 0, (float)-ULNA_TO_WRIST_Y));
			links.get(7).model.adjustOrigin(new Vector3d(0,0,-3.9527f));

			links.get(0).model.adjustRotation(new Vector3d(90,90,0));
			links.get(1).model.adjustRotation(new Vector3d(90,0,0));
			links.get(2).model.adjustRotation(new Vector3d(90,0,0));
			links.get(3).model.adjustRotation(new Vector3d(90,180,0));
			links.get(5).model.adjustRotation(new Vector3d(180,0,180));
			links.get(6).model.adjustRotation(new Vector3d(180,0,180));
			links.get(7).model.adjustRotation(new Vector3d(180,0,180));
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
			Vector3d position = this.getPosition();
			gl2.glTranslated(position.x, position.y, position.z);
			
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
    	solver.solve(this,targetPose,keyframe);
    	if(solver.solutionFlag==DHIKSolver.ONE_SOLUTION) {
    		// save the live pose
    		DHKeyframe saveKeyframe = this.getRobotPose();
    		// set the ghost pose
    		this.setRobotPose(keyframe);
    		// draw the ghost pose
    		materialGhost.render(gl2);
    		gl2.glPushMatrix();
				Vector3d position = this.getPosition();
				gl2.glTranslated(position.x, position.y, position.z);
				
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
			// reset the live pose
			this.setRobotPose(saveKeyframe);
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
			sendLineToRobot("D20");
		}
		
		double [] fk = new double[6];
		ADJUST4 = 135;
		
		fk[0] =(keyframe.fkValues[0]-ADJUST0)/SCALE_0;
		fk[1] =(keyframe.fkValues[1]-ADJUST1)/SCALE_1;
		fk[2] =(keyframe.fkValues[2]-ADJUST2)/SCALE_2;
		fk[3] =(keyframe.fkValues[3]-ADJUST3)/SCALE_3;
		fk[4] =(keyframe.fkValues[4]-ADJUST4)/SCALE_4;
		fk[5] =(keyframe.fkValues[5]-ADJUST5)/SCALE_5;
		
		for(int i=0;i<keyframe.fkValues.length;++i) {
			double v = fk[i];
			while(v<0) v+=360;
			while(v>360) v-=360;
			fk[i]=v;
		}

		String message = "G0"
	    		+" X"+(StringHelper.formatDouble(fk[0]))
	    		+" Y"+(StringHelper.formatDouble(fk[1]))
	    		+" Z"+(StringHelper.formatDouble(fk[2]))
	    		+" U"+(StringHelper.formatDouble(fk[3]))
	    		+" V"+(StringHelper.formatDouble(fk[4]))
	    		//+" W"+(StringHelper.formatDouble(fk[5]))
	    		;
		
		if(dhTool!=null) {
			double t=dhTool.getAdjustableValue();
			message += " T"+(180-t);
		}
				
		//System.out.println(AnsiColors.BLUE+message+AnsiColors.RESET);
		
		sendLineToRobot(message);
	}

	/**
	 * read D17 values from sixi robot
	 */
	@Override
	public void dataAvailable(NetworkConnection arg0,String data) {
		if(data.contains("D17")) {
			if(data.startsWith(">")) {
				data=data.substring(1).trim();
			}

			if(data.startsWith("D17")) {
				String [] dataParts = data.split("\\s");
				if(dataParts.length>=7) {
					try {
						// original message from robot
						
						receivedKeyframe.fkValues[0]=Double.parseDouble(dataParts[1])*SCALE_0+ADJUST0;
						receivedKeyframe.fkValues[1]=Double.parseDouble(dataParts[2])*SCALE_1+ADJUST1;
						receivedKeyframe.fkValues[2]=Double.parseDouble(dataParts[3])*SCALE_2+ADJUST2;
						receivedKeyframe.fkValues[3]=Double.parseDouble(dataParts[4])*SCALE_3+ADJUST3;
						receivedKeyframe.fkValues[4]=Double.parseDouble(dataParts[5])*SCALE_4+ADJUST4;
						receivedKeyframe.fkValues[5]=Double.parseDouble(dataParts[6])*SCALE_5+ADJUST5;
						
						
						for(int i=0;i<receivedKeyframe.fkValues.length;++i) {
							double v = receivedKeyframe.fkValues[i];
							while(v<-180) v+=360;
							while(v> 180) v-=360;
							receivedKeyframe.fkValues[i]=v;
						}
						/*
						String message = "D17"
					    		+" X"+(StringHelper.formatDouble((receivedKeyframe.fkValues[0])))
					    		+" Y"+(StringHelper.formatDouble((receivedKeyframe.fkValues[1])))
					    		+" Z"+(StringHelper.formatDouble((receivedKeyframe.fkValues[2])))
					    		+" U"+(StringHelper.formatDouble((receivedKeyframe.fkValues[3])))
					    		+" V"+(StringHelper.formatDouble((receivedKeyframe.fkValues[4])))
					    		+" W"+(StringHelper.formatDouble((receivedKeyframe.fkValues[5])));

						// angles after adjusting for scale and offset.
						System.out.println(AnsiColors.PURPLE+data+"\t>>\t"+message+AnsiColors.RESET);
						//*/
						if(data.endsWith("\n")) {
							data = data.substring(0,data.length()-1);
						}
						System.out.println(AnsiColors.PURPLE+data+AnsiColors.RESET);
						
						/*
						//smoothing to new position
						DHKeyframe inter = (DHKeyframe)createKeyframe();
						inter.interpolate(poseNow,receivedKeyframe, 0.5);
						this.setRobotPose(inter);
						/*/
						this.setRobotPose(receivedKeyframe);
						//*/
					} catch(Exception e) {}
				}
			}
			return;
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
		
		// calculate the matrix for the wrist, not the tool.
		this.links.get(6).poseCumulative.get(m1, t1);
		
		Vector3d e1 = MatrixHelper.matrixToEuler(m1);
		
		String message = "G0"
				+" X"+StringHelper.formatDouble(t1.x)
				+" Y"+StringHelper.formatDouble(t1.y)
				+" Z"+StringHelper.formatDouble(t1.z)
				+" I"+StringHelper.formatDouble(e1.x)
				+" J"+StringHelper.formatDouble(e1.y)
				+" K"+StringHelper.formatDouble(e1.z)
				;
		if(dhTool!=null) {
			message += " T"+StringHelper.formatDouble(dhTool.getAdjustableValue());
		}
		return message;
	}
	
	/**
	 * End matrix (the pose of the robot arm gripper) is stored on the PC in the text format "G0 X.. Y.. Z.. I.. J.. K.. T..\n"
	 * where XYZ are translation values and IJK are euler rotations of the matrix.
	 * The text, if parsed ok, will set the current end matrix.
	 * @param str the text format of one pose.
	 */
	public void parseGCode(String str) {
		Vector3d t1 = new Vector3d();
		Vector3d e1 = new Vector3d();
		
		StringTokenizer tokens = new StringTokenizer(str);
		while(tokens.hasMoreTokens()) {
			String token = tokens.nextToken();
			
			switch(token.charAt(0)) {
			case 'X':  t1.x = Double.parseDouble(token.substring(1));  break;
			case 'Y':  t1.y = Double.parseDouble(token.substring(1));  break;
			case 'Z':  t1.z = Double.parseDouble(token.substring(1));  break;
			case 'I':  e1.x = Double.parseDouble(token.substring(1));  break;
			case 'J':  e1.y = Double.parseDouble(token.substring(1));  break;
			case 'K':  e1.z = Double.parseDouble(token.substring(1));  break;
			default:  break;
			}
		}
		Matrix3d m1 = MatrixHelper.eulerToMatrix(e1);
		endMatrix.set(m1, t1, 1.0);

		if(dhTool!=null) {
			dhTool.parseGCode(str);
		}
	}
}
