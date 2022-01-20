package com.marginallyclever.robotOverlord.robots.robotArm;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.robots.robotArm.robotArmInterface.RobotArmInterface;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewElementButton;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * {@link RobotArmIK} is a {@link RobotArmFK} with added Inverse Kinematics.  
 * @see <a href='https://en.wikipedia.org/wiki/Inverse_kinematics'>Inverse Kinematics</a>
 * @author Dan Royer
 * @since 2021-02-24
 */
public class RobotArmIK extends RobotArmFK {
	private static final long serialVersionUID = -7778520191789995554L;

	private PoseEntity eeTarget = new PoseEntity("Target");
	
	public RobotArmIK(String name) {
		super(name);
		addChild(eeTarget);
		setEndEffectorTarget(getEndEffector());
	}
		
	public RobotArmIK() {
		this(RobotArmIK.class.getSimpleName());
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		RobotArmIK b = (RobotArmIK)super.clone();
		b.eeTarget = (PoseEntity)eeTarget.clone();
		
		return b;
	}

	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
		
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, getPose());
		drawEndEffectorPathToTarget(gl2);
		gl2.glPopMatrix();
	}
	
	private void drawEndEffectorPathToTarget(GL2 gl2) {
		boolean isTex = OpenGLHelper.disableTextureStart(gl2);
		int depthWasOn = OpenGLHelper.drawAtopEverythingStart(gl2);
		boolean lightWasOn = OpenGLHelper.disableLightingStart(gl2);
		
		Matrix4d start = this.getEndEffector();
		Matrix4d end = eeTarget.getPose();
		Matrix4d interpolated = new Matrix4d();
		
		Vector3d a = new Vector3d();
		Vector3d b = new Vector3d();
		start.get(a);
		end.get(b);
		a.sub(b);
		a.length();
		
		double STEPS=a.length();
		for(double d=1;d<STEPS;d++) {
			MatrixHelper.interpolate(start,end,d/STEPS,interpolated);
			MatrixHelper.drawMatrix(gl2, interpolated, 1.0);
		}
		
		OpenGLHelper.disableLightingEnd(gl2,lightWasOn);
		OpenGLHelper.drawAtopEverythingEnd(gl2, depthWasOn);
		OpenGLHelper.disableTextureEnd(gl2,isTex);
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("IK","Inverse Kinematics");

		ViewElementButton bOpen = view.addButton("Open control panel");
		bOpen.addActionEventListener((evt)-> onOpenAction() );

		ViewElementButton bResetGoto = view.addButton("Reset GoTo");
		bResetGoto.addActionEventListener((evt)-> onResetGotoAction() );

		//ViewElementButton bRunTest = view.addButton("Run test");
		//bRunTest.addPropertyChangeListener((evt) -> onRunTest() );
		view.popStack();
		
		super.getView(view);
	}

	private void onResetGotoAction() {
		setEndEffectorTarget(this.getEndEffector());
	}

	private void onOpenAction() {
		JFrame parent = null;
		
		Entity e = this.getRoot();
		if(e instanceof RobotOverlord) {
			parent = ((RobotOverlord)e).getMainFrame();
		}
		
		final RobotArmIK me = this;
		final JFrame parentFrame = parent;
		
        new Thread(new Runnable() {
            @Override
			public void run() {
            	JDialog frame = new JDialog(parentFrame,getName());
        		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        		frame.add(new RobotArmInterface(me));
        		frame.pack();
        		frame.setVisible(true);
            }
        }).start();
	}

	public Matrix4d getEndEffectorTarget() {
		return eeTarget.getPose();
	}

	/**
	 * Update the target end effector and fire a {@link PropertyChangeEvent} notice.  
	 * The {@link PropertyChangeEvent.propertyName} will be "eeTarget".
	 * @param m1 the new end effector target.
	 */
	public void setEndEffectorTarget(Matrix4d m1) {
		Matrix4d m0 = eeTarget.getPoseWorld();
		eeTarget.setPose(m1);
		
		notifyPropertyChangeListeners(new PropertyChangeEvent(this,"eeTarget",m0,m1));
	}

	public ApproximateJacobian getApproximateJacobian() {
		return new ApproximateJacobian(this);
	}
}
