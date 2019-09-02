package com.marginallyclever.robotOverlord.dhRobot.robots.sixi2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JPanel;
import javax.vecmath.Matrix3d;
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
import com.marginallyclever.robotOverlord.dhRobot.DHRobotPanel;
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
	
	DHKeyframe receivedKeyframe;
	protected Sixi2Panel sixi2Panel;
	
	
	public Sixi2() {
		super();
		setDisplayName("Sixi 2");
		isFirstTime=true;
		receivedKeyframe = (DHKeyframe)createKeyframe();
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
		// roll
		links.get(0).d=13.44;
		links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(0).rangeMin=-120;
		links.get(0).rangeMax=120;
		// tilt
		links.get(1).theta=90;
		links.get(1).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(1).rangeMin=-72;
		// tilt
		links.get(2).d=44.55;
		links.get(2).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(2).rangeMin=-83.369;
		links.get(2).rangeMax=86;
		// interim point
		links.get(3).d=4.7201;
		links.get(3).alpha=90;
		links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		// roll
		links.get(4).d=28.805;
		links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(4).rangeMin=-170;
		links.get(4).rangeMax=170;

		// tilt
		links.get(5).d=11.8;
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
			links.get(5).model.adjustRotation(new Vector3d(180,0,0));
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
			MatrixHelper.applyMatrix(gl2, this.getPose());
			
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
    	solver.solve(this,targetMatrix,keyframe);
    	if(solver.solutionFlag==DHIKSolver.ONE_SOLUTION) {
    		// save the live pose
    		DHKeyframe saveKeyframe = this.getRobotPose();
    		// set the ghost pose
    		DHRobotPanel pTemp = this.panel;
    		this.panel=null;
    		this.setRobotPose(keyframe);
    		// draw the ghost pose
    		materialGhost.render(gl2);
    		gl2.glPushMatrix();
				MatrixHelper.applyMatrix(gl2, this.getPose());
				
				// Draw models
				gl2.glPushMatrix();
					Iterator<DHLink> i = links.iterator();
					while(i.hasNext()) {
						DHLink link = i.next();
						if(showAngles) {
							link.renderAngles(gl2);
						}
						link.renderModel(gl2);
					}
					if(dhTool!=null) {
						dhTool.render(gl2);
					}
				gl2.glPopMatrix();
			gl2.glPopMatrix();
			// reset the live pose
			this.setRobotPose(saveKeyframe);
			this.panel=pTemp;
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
	    		;

		if(dhTool!=null) {
			double t=dhTool.getAdjustableValue();
			message += " T"+(180-t);
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

						this.setRobotPose(receivedKeyframe);

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
		
		Vector3d e1 = MatrixHelper.matrixToEuler(m1);
		
		String message = "G0"
				+" X"+StringHelper.formatDouble(t1.x)
				+" Y"+StringHelper.formatDouble(t1.y)
				+" Z"+StringHelper.formatDouble(t1.z)
				+" I"+StringHelper.formatDouble(Math.toDegrees(e1.x))
				+" J"+StringHelper.formatDouble(Math.toDegrees(e1.y))
				+" K"+StringHelper.formatDouble(Math.toDegrees(e1.z))
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
			
			while(tokens.hasMoreTokens()) {
				String token = tokens.nextToken();
				
				switch(token.charAt(0)) {
				case 'X':  t1.x = Double.parseDouble(token.substring(1));  break;
				case 'Y':  t1.y = Double.parseDouble(token.substring(1));  break;
				case 'Z':  t1.z = Double.parseDouble(token.substring(1));  break;
				case 'I':  e1.x = Math.toRadians(Double.parseDouble(token.substring(1)));  break;
				case 'J':  e1.y = Math.toRadians(Double.parseDouble(token.substring(1)));  break;
				case 'K':  e1.z = Math.toRadians(Double.parseDouble(token.substring(1)));  break;
				case 'F':  this.sendLineToRobot("G0 F"+token.substring(1));  break;
				case 'A':  this.sendLineToRobot("G0 A"+token.substring(1));  break;
				default:  break;
				}
			}
			m1 = MatrixHelper.eulerToMatrix(e1);
	
			// changing the target pose will direct the live robot to that position.
			targetMatrix.set(m1);
			targetMatrix.setTranslation(t1);
			
			if(dhTool!=null) {
				dhTool.parseGCode(line);
			}

			moveToTargetPose();
		}
	}
}
