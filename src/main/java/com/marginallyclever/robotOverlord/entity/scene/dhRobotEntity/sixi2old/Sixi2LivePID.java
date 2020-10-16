package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2old;

import javax.vecmath.Matrix4d;

import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.PoseFK;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2.Sixi2FirmwareSettings;
import com.marginallyclever.robotOverlord.log.Log;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * Simulation of a Sixi Live arm driven by PIDs instead of Marlin
 * @author Dan Royer
 * @since 1.6.0
 *
 */
@Deprecated
public class Sixi2LivePID extends Sixi2Model {
	protected PoseFK receivedKeyframe;

	class StepperMotor {
		// only a whole number of steps is possible.
		public long steps;
		// only a whole number of steps is possible.
		public long target;
		
		// steps to degrees ratio (gearbox)
		public double ratio;
				
		// current error
		// PID values
		public double kp=5,ki=0.001,kd=0.00001;
		public double error;
		public double error_i;
		public double error_last;
		
		public double timeSinceLastStep;
		public double stepDelay;
		public double velocity;

		
		public StepperMotor() {
			steps=0;
			target=0;
			error=0;
			error_i=0;
			error_last=0;
		}
		
		public void update(double dt) {
			// PID calculation
			error = target - steps;
			error_i += error * dt;					
			double error_d = (error - error_last) / dt;
			velocity = kp * ( error + ki * error_i + kd * error_d );
			error_last = error;

			//Log.message("("+error+","+velocity+")\t");
			steps += velocity*dt;
		}
		
		public double getDegrees() {
			return MathHelper.wrapDegrees( steps*ratio, 0 );
		}
		
		public void setPID(double p,double i,double d) {
			kp=p;
			ki=i;
			kd=d;
		}
	};
	
	protected StepperMotor [] motors;

	// IK
	protected Matrix4d mFrom = new Matrix4d();
	protected Matrix4d mTarget = new Matrix4d();
	
	
	public Sixi2LivePID() {
		super();
		setName("Live");

		int numAdjustableLinks = links.size();		
		motors = new StepperMotor[numAdjustableLinks];
		for( int i=0; i<numAdjustableLinks; ++i ) {
			motors[i]=new StepperMotor();
		}

		motors[0].ratio = Sixi2FirmwareSettings.DEGREES_PER_STEP_0;
		motors[1].ratio = Sixi2FirmwareSettings.DEGREES_PER_STEP_1;
		motors[2].ratio = Sixi2FirmwareSettings.DEGREES_PER_STEP_2;
		motors[3].ratio = Sixi2FirmwareSettings.DEGREES_PER_STEP_3;
		motors[4].ratio = Sixi2FirmwareSettings.DEGREES_PER_STEP_4;
		motors[5].ratio = Sixi2FirmwareSettings.DEGREES_PER_STEP_5;
		/*
		motors[0].setPID(500,20,0);       
		motors[1].setPID(1000,30000,0);   
		motors[2].setPID(1000,30000,0);   
		motors[3].setPID(500,20,0);       
		motors[4].setPID(500,20,0);       
		motors[5].setPID(500,20,0.00);
		*/
		
		for( int i=0; i<numAdjustableLinks; ++i ) {
			motors[i].steps = (long) Math.floor( links.get(i).getAdjustableValue() / motors[i].ratio );
			motors[i].target = motors[i].steps; 
			Log.message(i+"="+motors[i].steps);
		}

		readyForCommands=true;
		
		// set green
	    for( DHLink link : links ) {
	    	link.getMaterial().setDiffuseColor(122f/255f,161/255f,128f/255f,1);
	    }
	}

	@Override
	public void update(double dt) {
		// Sixi2LivePID runs PIDs to step motors
		for( int i=0; i<links.size(); ++i) {
			motors[i].update(dt);
			links.get(i).setTheta(motors[i].getDegrees());
		}
	}
	
	@Override
	public void sendCommand(String command) {
		if(command==null) return;  // no more commands.

		// parse the command and update the model immediately.
		String [] tok = command.split("\\s+");
		for( String t : tok ) {
			if( t.startsWith("G")) {
				int newGMode = Integer.parseInt(t.substring(1));
				switch(newGMode) {
				case 0: gMode=0;	break;  // move
				case 1: gMode=1;	break;  // rapid
				case 2: gMode=2;	break;  // arc cw
				case 3: gMode=3;	break;  // arc ccw
				case 4: gMode=4;	break;  // dwell
				case 90: relativeMode=false;	break;
				case 91: relativeMode=true;    break;
				default:  break;
				}
			}			
		}
		
		if(gMode==0) {
			// linear move
	        PoseFK poseFKTarget = ikSolver.createPoseFK();

			int i=0;
			for( DHLink link : links ) {
				if(link.flags == LinkAdjust.NONE) continue;
				
				// set the target position
				// if a value is omitted, use the current pose.
				motors[i].target = motors[i].steps;
				
				for( String t : tok ) {
					String letter = t.substring(0,1); 
					if(link.getLetter().equalsIgnoreCase(letter)) {
						//Log.message("link "+link.getLetter()+" matches "+letter);
						double degrees = StringHelper.parseNumber(t.substring(1));
						poseFKTarget.fkValues[i] = degrees;
						motors[i].target = (long) Math.floor( degrees / motors[i].ratio );
					}
				}
				++i;
			}
			
			for( String t : tok ) {
				String letter = t.substring(0,1); 
				if(letter.equalsIgnoreCase("F")) {
					feedRate.set(StringHelper.parseNumber(t.substring(1)));
				} else if(letter.equalsIgnoreCase("A")) {
					acceleration.set(StringHelper.parseNumber(t.substring(1)));
				}
			}

			
			if(dhTool!=null) {
				dhTool.sendCommand(command);
			}
	        
	        // set the live and from matrixes
	        mFrom.set(endEffector.getPoseWorld());

	        // get the target matrix
	        PoseFK oldPose = getPoseFK();
	        PoseFK newPose = ikSolver.createPoseFK();
	        newPose.set(poseFKTarget);
	        setPoseFK(newPose);
	        mTarget.set(endEffector.getPoseWorld());
	        setPoseFK(oldPose);


			double dMax=0;
	        double dp=0;
			for(i=0; i<poseFKTarget.fkValues.length; ++i) {
				double dAbs = Math.abs(poseFKTarget.fkValues[i] - motors[i].getDegrees());
				dp+=dAbs;
				if(dMax<dAbs) dMax=dAbs;
			}
	        if(dp==0) return;
	        
	        //double travelS = dMax/(double)feedRate.get();
	        
	        // wait for reply
	        readyForCommands=true;
		} else if(gMode==4) {/*
			TODO implement later?
			// dwell
			//double dwellTimeS=0;
			for( String t : tok ) {
				if(t.startsWith("P")) {
					dwellTimeS+=Double.parseDouble(t.substring(1))*0.001;
				}
				if(t.startsWith("S")) {
					dwellTimeS+=Double.parseDouble(t.substring(1));
				}
			}*/
		}
	    // wait for reply
	    readyForCommands=true;
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Sp", "Sixi with PIDs");
		
		view.popStack();
		endEffector.getView(view);
	}
}
