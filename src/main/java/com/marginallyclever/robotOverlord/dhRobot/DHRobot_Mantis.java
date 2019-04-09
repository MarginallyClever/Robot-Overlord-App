package com.marginallyclever.robotOverlord.dhRobot;

import java.util.Iterator;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.InputListener;
import com.marginallyclever.robotOverlord.material.Material;
import com.marginallyclever.robotOverlord.model.ModelFactory;

import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

/**
 * D-H robot modified for Andreas Hoelldorfer's MANTIS
 * @author Dan Royer
 * @see https://hackaday.io/project/3800-3d-printable-robot-arm
 *
 */
public class DHRobot_Mantis extends DHRobot implements InputListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Matrix4d targetPose;

	public DHRobot_Mantis() {
		super();
		setDisplayName("Mantis");
	}
	
	@Override
	public void setupLinks() {
		targetPose = new Matrix4d();
		
		setNumLinks(8);

		// roll
		//this.links.get(0).d=13.44;
		this.links.get(0).d=24.5+2.7;
		this.links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		this.links.get(0).rangeMin=-120;
		this.links.get(0).rangeMax=120;

		// tilt
		this.links.get(1).d=0;
		this.links.get(1).alpha=0;
		this.links.get(1).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		this.links.get(1).rangeMin=-72;

		// tilt
		this.links.get(2).d=13.9744 + 8.547;
		this.links.get(2).alpha=0;
		this.links.get(2).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		this.links.get(2).rangeMin=-83.369;
		this.links.get(2).rangeMax=86;
		// interim point
		this.links.get(3).d=0.001;  // TODO explain why this couldn't just be zero.  Solver hates it for some reason.
		this.links.get(3).alpha=90;
		this.links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		// roll
		this.links.get(4).d=8.547;
		this.links.get(4).theta=0;
		this.links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		this.links.get(4).rangeMin=-90;
		this.links.get(4).rangeMax=90;

		// tilt
		this.links.get(5).d=14.6855f;
		this.links.get(5).alpha=0;
		this.links.get(5).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		this.links.get(5).rangeMin=-90;
		this.links.get(5).rangeMax=90;
		// roll
		this.links.get(6).d=5.0f;
		this.links.get(6).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		this.links.get(6).rangeMin=-90;
		this.links.get(6).rangeMax=90;
		
		this.links.get(7).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;

		try {
			this.links.get(0).model = ModelFactory.createModelFromFilename("/AH/rotBaseCase.stl",0.1f);
			this.links.get(1).model = ModelFactory.createModelFromFilename("/AH/Shoulder_r1.stl",0.1f);
			this.links.get(2).model = ModelFactory.createModelFromFilename("/AH/Elbow.stl",0.1f);
			this.links.get(3).model = ModelFactory.createModelFromFilename("/AH/Forearm.stl",0.1f);
			this.links.get(5).model = ModelFactory.createModelFromFilename("/AH/Wrist_r1.stl",0.1f);
			this.links.get(6).model = ModelFactory.createModelFromFilename("/AH/WristRot.stl",0.1f);
			
			this.links.get(0).model.adjustOrigin(new Vector3d(0,0,2.7));
			this.links.get(1).model.adjustRotation(new Vector3d(0,0,90));
			this.links.get(1).model.adjustOrigin(new Vector3d(0,0,0));
			this.links.get(2).model.adjustRotation(new Vector3d(90,90,90));
			this.links.get(2).model.adjustOrigin(new Vector3d(0,0.476,2.7+(13.9744 + 8.547)/2));
			this.links.get(3).model.adjustRotation(new Vector3d(180,90,90));
			this.links.get(3).model.adjustOrigin(new Vector3d(0,-5.7162,0));//0.3488,0.3917
			this.links.get(5).model.adjustOrigin(new Vector3d(0,0,0));
			this.links.get(6).model.adjustRotation(new Vector3d(-180,90,0));

		} catch(Exception e) {
			e.printStackTrace();
		}
		
		this.refreshPose();
		targetPose.set(endMatrix);
	}
	
	@Override
	public void pick() {
		this.refreshPose();
		targetPose.set(endMatrix);
		drawSkeleton=true;
	}
	
	@Override
	public void unPick() {
		drawSkeleton=false;
	}
	
	@Override
	public void render(GL2 gl2) {
		
		Material material = new Material();
		
		gl2.glPushMatrix();
			Vector3d position = this.getPosition();
			gl2.glTranslated(position.x, position.y, position.z);
			
			// Draw models
			float r=0.5f;
			float g=0.5f;
			float b=0.5f;
			material.setDiffuseColor(r,g,b,1);
			material.render(gl2);
			
			gl2.glPushMatrix();
				Iterator<DHLink> i = links.iterator();
				while(i.hasNext()) {
					DHLink link = i.next();
					link.renderModel(gl2);
				}
			gl2.glPopMatrix();
		
			if(drawSkeleton) {
				// draw targetPose
				gl2.glPushMatrix();
				
				double[] mat = new double[16];
				mat[ 0] = targetPose.m00;
				mat[ 1] = targetPose.m10;
				mat[ 2] = targetPose.m20;
				mat[ 3] = targetPose.m30;
				mat[ 4] = targetPose.m01;
				mat[ 5] = targetPose.m11;
				mat[ 6] = targetPose.m21;
				mat[ 7] = targetPose.m31;
				mat[ 8] = targetPose.m02;
				mat[ 9] = targetPose.m12;
				mat[10] = targetPose.m22;
				mat[11] = targetPose.m32;
				mat[12] = targetPose.m03;
				mat[13] = targetPose.m13;
				mat[14] = targetPose.m23;
				mat[15] = targetPose.m33;
				gl2.glMultMatrixd(mat, 0);
		
				boolean isDepth = gl2.glIsEnabled(GL2.GL_DEPTH_TEST);
				boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
				gl2.glDisable(GL2.GL_DEPTH_TEST);
				gl2.glDisable(GL2.GL_LIGHTING);
				MatrixHelper.drawMatrix(gl2, 
						new Vector3d(0,0,0),
						new Vector3d(1,0,0),
						new Vector3d(0,1,0),
						new Vector3d(0,0,1));
				if(isDepth) gl2.glEnable(GL2.GL_DEPTH_TEST);
				if(isLit) gl2.glEnable(GL2.GL_LIGHTING);
				gl2.glPopMatrix();
			}

		gl2.glPopMatrix();
		
		super.render(gl2);
	}
	
	public DHIKSolver getSolverIK() {
		return new DHIKSolver_RTTRTR();
	}

	@Override
	public void inputUpdate() {
		Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
		boolean isDirty=false;
		double scale=1.0;
		double scaleTurn=0.15;
		double deadzone=0.1;
		
        for(int i=0;i<ca.length;i++){
        	if(ca[i].getType()!=Controller.Type.STICK) continue;

        	Component[] components = ca[i].getComponents();
            for(int j=0;j<components.length;j++){
            	if(!components[j].isAnalog()) continue;

            	if(components[j].getIdentifier()==Identifier.Axis.Z) {
            		// right analog stick, + is right -1 is left
            		double v = components[j].getPollData();
            		if(Math.abs(v)<deadzone) continue;
            		isDirty=true;
            		Matrix4d temp = new Matrix4d();
            		temp.rotY(v*scaleTurn);
            		targetPose.mul(temp);
            	}
            	if(components[j].getIdentifier()==Identifier.Axis.RZ) {
            		// right analog stick, + is down -1 is up
            		double v = components[j].getPollData();
            		if(Math.abs(v)<deadzone) continue;
            		isDirty=true;
            		Matrix4d temp = new Matrix4d();
            		temp.rotX(v*scaleTurn);
            		targetPose.mul(temp);
            	}
            	
            	if(components[j].getIdentifier()==Identifier.Axis.RY) {
            		// right trigger, +1 is pressed -1 is unpressed
            		double v = components[j].getPollData();
            		if(Math.abs(v)<-1+deadzone) continue;
            		isDirty=true;
            		targetPose.m23-=((v+1)/2)*scale;
            	}
            	if(components[j].getIdentifier()==Identifier.Axis.RX) {
            		// left trigger, +1 is pressed -1 is unpressed
            		double v = components[j].getPollData();
            		if(Math.abs(v)<-1+deadzone) continue;
            		isDirty=true;
            		targetPose.m23+=((v+1)/2)*scale;
            	}
            	if(components[j].getIdentifier()==Identifier.Axis.X) {
            		// left analog stick, +1 is right -1 is left
            		double v = components[j].getPollData();
            		if(Math.abs(v)<deadzone) continue;
            		isDirty=true;
            		targetPose.m13+=v*scale;
            	}
            	if(components[j].getIdentifier()==Identifier.Axis.Y) {
            		// left analog stick, -1 is up +1 is down
            		double v = components[j].getPollData();
            		if(Math.abs(v)<deadzone) continue;
            		isDirty=true;
            		targetPose.m03+=v*scale;
            	}
            	/*System.out.print("\t"+components[j].getName()+
            			":"+(components[j].isAnalog()?"Abs":"Rel")+
            			":"+(components[j].isAnalog()?"An":"Di")+
               			":"+(components[j].getPollData()));*/
        	}
        }

        if(isDirty) {
        	// set the new target pose
        	this.links.get(7).poseCumulative.set(targetPose);
        	// attempt to solve IK
        	DHIKSolver_RTTRTR solver = (DHIKSolver_RTTRTR)this.getSolverIK();
        	solver.solve(this);
        	if(solver.solutionFlag==DHIKSolver_RTTRTR.ONE_SOLUTION) {
        		// solved!  update robot pose with fk.
	        	this.links.get(0).theta = solver.theta0;
	        	this.links.get(1).alpha = solver.alpha1;
	        	this.links.get(2).alpha = solver.alpha2;
	        	this.links.get(4).theta = solver.theta4;
	        	this.links.get(5).alpha = solver.alpha5;
	        	this.links.get(6).theta = solver.theta6;
        	}
        	this.refreshPose();
        }
	}
}
