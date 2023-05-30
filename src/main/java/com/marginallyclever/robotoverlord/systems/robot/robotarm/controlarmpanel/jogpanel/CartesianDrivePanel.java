package com.marginallyclever.robotoverlord.systems.robot.robotarm.controlarmpanel.jogpanel;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.swing.Dial;
import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.robots.Robot;

import javax.swing.*;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.io.Serial;

/**
 * {@link CartesianDrivePanel} displays a dial that can adjust one cartesian movement at a time.
 * It also displays radio buttons for translation and rotation.
 * It also displays a level of detail combobox to refine movements more than the default (1 mm/deg)
 * When the dial is turned it calls on the Jacobian Newton Raphson iterator to move the arm.
 * @author Dan Royer
 *
 */
public class CartesianDrivePanel extends JPanel {
	@Serial
	private static final long serialVersionUID = 1L;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private final JRadioButton x = makeRadioButton(buttonGroup,"X");
	private final JRadioButton y = makeRadioButton(buttonGroup,"Y");
	private final JRadioButton z = makeRadioButton(buttonGroup,"Z");
	private final JRadioButton roll = makeRadioButton(buttonGroup,"roll");
	private final JRadioButton pitch = makeRadioButton(buttonGroup,"pitch");
	private final JRadioButton yaw = makeRadioButton(buttonGroup,"yaw");
	private final ScalePanel stepScale = new ScalePanel();
	private final JComboBox<String> frameOfReference = getFramesOfReference();
	private final Dial dial = new Dial();

	public CartesianDrivePanel(Robot robot) {
		super(new GridBagLayout());

		x.setSelected(true);
		
		dial.addActionListener( (e)-> onDialTurn(robot) );

		this.setBorder(BorderFactory.createTitledBorder(CartesianDrivePanel.class.getSimpleName()));
		
		JPanel referenceFrameSelection = new JPanel(new FlowLayout(FlowLayout.LEFT));
		referenceFrameSelection.add(new JLabel("Reference frame"));
		referenceFrameSelection.add(frameOfReference);

		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.weighty=0;
		c.gridheight=1;
		c.anchor=GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth=2;
		this.add(referenceFrameSelection,c);
		
		c.gridy++;
		this.add(stepScale,c);

		c.gridwidth=1;
		c.gridy++;
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
		
		c.gridx=1;
		c.gridy=2;
		c.weightx=1;
		c.weighty=1;
		c.gridwidth=1;
		c.gridheight=6;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.EAST;
		dial.setPreferredSize(new Dimension(120,120));
		this.add(dial,c);
	}

	private JComboBox<String> getFramesOfReference() {
		JComboBox<String> FOR = new JComboBox<>();
		FOR.addItem("World");
		FOR.addItem("First joint");
		FOR.addItem("End effector");
		
		return FOR;
	}

	/**
	 * Get the frame of reference matrix for the selected frame of reference.
	 * @param robot
	 * @return
	 */
	private Matrix4d getFrameOfReferenceMatrix(Robot robot) {
		Matrix4d mFor;

		switch (frameOfReference.getSelectedIndex()) {
			case 0 -> mFor = MatrixHelper.createIdentityMatrix4();
			case 1 -> {
				mFor = (Matrix4d)robot.get(Robot.POSE);
				robot.set(Robot.ACTIVE_JOINT,0);
				mFor.mul((Matrix4d)robot.get(Robot.JOINT_POSE));
			}
			case 2 -> mFor = (Matrix4d)robot.get(Robot.TOOL_CENTER_POINT);
			default -> throw new UnsupportedOperationException("frame of reference selection");
		}
		
		return mFor;
	}

	private JRadioButton makeRadioButton(ButtonGroup group, String label) {
		JRadioButton rb = new JRadioButton(label);
		rb.setActionCommand(label);
		group.add(rb);
		return rb;
	}
	
	private double getMovementStepSize() {
		double d = stepScale.getScale();
		double scale = Math.pow(10.0, -d);
		return dial.getChange()*scale;
	}
	
	private void onDialTurn(Robot robot) {
		double v_mm = getMovementStepSize();
		Matrix4d m4 = getEndEffectorMovedInFrameOfReference(robot,v_mm);
		robot.set(Robot.END_EFFECTOR_TARGET,m4);
	}

	/**
	 * Move the end effector in the frame of reference.
	 * @param robot the robot to move
	 * @param amount translations in mm, rotations in degrees.
	 * @return the new end effector matrix
	 */
	private Matrix4d getEndEffectorMovedInFrameOfReference(Robot robot, double amount) {
		Matrix4d m4 = (Matrix4d)robot.get(Robot.END_EFFECTOR_TARGET);
		Matrix4d mFor = getFrameOfReferenceMatrix(robot);
		
		Vector3d p = new Vector3d();
		Matrix3d mA = new Matrix3d(); 
		m4.get(p);
		m4.get(mA);
		
		if(x.isSelected()) {
			translateMatrix(m4,MatrixHelper.getXAxis(mFor),amount);
		} else if(y.isSelected()) {
			translateMatrix(m4,MatrixHelper.getYAxis(mFor),amount);
		} else if(z.isSelected()) {
			translateMatrix(m4,MatrixHelper.getZAxis(mFor),amount);
		} else {
			Matrix3d rot = new Matrix3d();
			Matrix3d mB = new Matrix3d();
			mFor.get(mB);
			if(roll.isSelected()) {
				rot.rotZ(amount);
			} else if(pitch.isSelected()) {
				rot.rotY(amount);
			} else if(yaw.isSelected()) {
				rot.rotX(amount);
			}
			Matrix3d mBi = new Matrix3d(mB);
			mBi.invert();
			mA.mul(mBi);
			mA.mul(rot);
			mA.mul(mB);
			
			m4.set(mA);
			m4.setTranslation(p);
		}
		
		return m4;
	}

	private void translateMatrix(Matrix4d m4, Vector3d v, double v_mm) {
		v.scale(v_mm);
		m4.m03 += v.x;
		m4.m13 += v.y;
		m4.m23 += v.z;
	}

	// TEST

	public static void main(String[] args) {
		Log.start();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) {}

		JFrame frame = new JFrame(CartesianDrivePanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new CartesianDrivePanel(new RobotComponent()));
		frame.pack();
		frame.setVisible(true);
	}
}
