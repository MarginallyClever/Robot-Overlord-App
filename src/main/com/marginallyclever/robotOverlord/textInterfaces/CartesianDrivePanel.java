package com.marginallyclever.robotOverlord.textInterfaces;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.robots.sixi3.ApproximateJacobian;
import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3IK;

public class CartesianDrivePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private ButtonGroup buttonGroup = new ButtonGroup();
	private JRadioButton x = makeRadioButton(buttonGroup,"X");
	private JRadioButton y = makeRadioButton(buttonGroup,"Y");
	private JRadioButton z = makeRadioButton(buttonGroup,"Z");
	private JRadioButton roll = makeRadioButton(buttonGroup,"roll");
	private JRadioButton pitch = makeRadioButton(buttonGroup,"pitch");
	private JRadioButton yaw = makeRadioButton(buttonGroup,"yaw");
	private Dial dial = new Dial();

	public CartesianDrivePanel(Sixi3IK sixi3) {
		super();

		x.setSelected(true);
		
		dial.addActionListener( (evt)-> onDialTurn(sixi3) );

		this.setBorder(BorderFactory.createTitledBorder("CartesianDrive"));
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=0;
		c.weighty=0;
		c.gridheight=1;
		c.gridwidth=1;
		c.anchor=GridBagConstraints.NORTHWEST;
		
		this.add(x,c);
		c.gridy++;
		this.add(y,c);
		c.gridy++;
		this.add(z,c);
		c.gridy++;
		this.add(roll,c);
		c.gridy++;
		this.add(pitch,c);
		c.gridy++;
		this.add(yaw,c);
		
		c.gridx=2;
		c.gridy=0;
		c.weightx=1;
		c.weighty=1;
		c.gridwidth=6;
		c.gridheight=6;
		c.anchor=GridBagConstraints.EAST;
		dial.setPreferredSize(new Dimension(120,120));
		this.add(dial,c);
	}

	private JRadioButton makeRadioButton(ButtonGroup group, String label) {
		JRadioButton rb = new JRadioButton(label);
		rb.setActionCommand(label);
		group.add(rb);
		return rb;
	}
	
	private void onDialTurn(Sixi3IK sixi3) {
		double v_mm = dial.getChange()*0.1;
		Matrix4d m4 = sixi3.getEndEffectorTarget();

		Vector3d p=new Vector3d();
		Matrix3d m3 = new Matrix3d(); 
		m4.get(p);
		m4.get(m3);
		
		if(x.isSelected()) m4.m03 += v_mm;
		if(y.isSelected()) m4.m13 += v_mm;
		if(z.isSelected()) m4.m23 += v_mm;
		
		if(roll.isSelected()) {
			Matrix3d rot = new Matrix3d();
			rot.rotZ(v_mm);
			m3.mul(rot);
			m4.set(m3);
			m4.setTranslation(p);
		}
		
		if(pitch.isSelected()) {
			Matrix3d rot = new Matrix3d();
			rot.rotX(v_mm);
			m3.mul(rot);
			m4.set(m3);
			m4.setTranslation(p);
		}
		
		if(yaw.isSelected()) {
			Matrix3d rot = new Matrix3d();
			rot.rotY(v_mm);
			m3.mul(rot);
			m4.set(m3);
			m4.setTranslation(p);
		}
		
		iterateNewtonRaphson(sixi3,m4,20);
	}

	private void iterateNewtonRaphson(Sixi3IK sixi3, Matrix4d m4,int i) {
		System.out.println("iterateNewtonRaphson begins");
		Sixi3IK temp = new Sixi3IK();
		temp.setAngles(sixi3.getAngles());
		temp.setEndEffectorTarget(m4);
		try {
			while(i-->=0) {
				newtonRaphson(temp);
				if(temp.getDistanceToTarget(m4)<0.001) {
					System.out.println("iterateNewtonRaphson hit");
					sixi3.setAngles(temp.getAngles());
					sixi3.setEndEffectorTarget(m4);
					break;
				}
			}
		} catch(Exception e) {
			System.out.println("iterateNewtonRaphson exception: "+e.getLocalizedMessage());
			e.printStackTrace();
		}
		System.out.println("iterateNewtonRaphson ends ("+i+")");
	}

	// http://motion.pratt.duke.edu/RoboticSystems/InverseKinematics.html#mjx-eqn-eqNewtonRaphson
	private void newtonRaphson(Sixi3IK sixi3) throws Exception {
		Matrix4d m0=sixi3.getEndEffector();
		Matrix4d m1=sixi3.getEndEffectorTarget();
		System.out.print("m0="+m0);
		System.out.print("m1="+m1);
		double [] cartesianDistance = MatrixHelper.getCartesianBetweenTwoMatrixes(m0, m1);	
		System.out.println("cartesianDistance="+Arrays.toString(cartesianDistance));
		ApproximateJacobian aj = sixi3.getApproximateJacobian();
		double [] jointDistance=aj.getJointFromCartesian(cartesianDistance);
		double [] angles = sixi3.getAngles();
		for(int i=0;i<angles.length;++i) {
			angles[i]+=jointDistance[i];
		}
		sixi3.setAngles(angles);
	}
}
