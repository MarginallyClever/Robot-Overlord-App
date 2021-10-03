package com.marginallyclever.robotOverlord.robotArmInterface.robotArmEditor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.batik.ext.swing.GridBagConstants;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.robots.robotArm.RobotArmBone;

public class RobotArmBoneEditorPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private RobotArmBone myBone;

	private JTextField name = new JTextField();
	private JTextField alpha = new JTextField();
	private JTextField theta = new JTextField();
	private JTextField d = new JTextField();
	private JTextField r = new JTextField();
	private JTextField tMax = new JTextField();
	private JTextField tMin = new JTextField();
	private JTextField shape = new JTextField();
	
	public RobotArmBoneEditorPanel(RobotArmBone bone) {
		super();
		myBone = bone;
		
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.gridwidth=1;
		c.gridheight=1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(1, 2, 1, 2);
		
		addNewRow("Name",name,c);
		addNewRow("D (real number)",d,c);
		addNewRow("R (real number)",r,c);
		addNewRow("Alpha (0...360)",alpha,c);
		addNewRow("Theta (0...360)",theta,c);
		addNewRow("Theta max",tMax,c);
		addNewRow("Theta min",tMin,c);
		addNewRow("Model filename",shape,c);
		
		c.gridy++;
		c.gridwidth=2;
		c.fill = GridBagConstants.BOTH;
		c.weightx=1;
		c.weighty=1;
		this.add(new JLabel(),c);
		
		name.setText(bone.getName());
		d.setText(Double.toString(bone.getD()));
		r.setText(Double.toString(bone.getR()));
		alpha.setText(Double.toString(bone.getAlpha()));
		theta.setText(Double.toString(bone.getTheta()));
		tMax.setText(Double.toString(bone.getAngleMax()));
		tMin.setText(Double.toString(bone.getAngleMin()));
		if(bone.getShape()!=null) {
			shape.setText(bone.getShape().getModelFilename());
		}
		
		addDocumentListenerToTextField(name);
		addDocumentListenerToTextField(d);
		addDocumentListenerToTextField(r);
		addDocumentListenerToTextField(alpha);
		addDocumentListenerToTextField(theta);
		addDocumentListenerToTextField(tMax);
		addDocumentListenerToTextField(tMin);
		addDocumentListenerToTextField(shape);
	}
	
	private void addDocumentListenerToTextField(JTextField f) {
		f.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				System.out.println("insert");
				onEditAction();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				System.out.println("remove");
				onEditAction();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				System.out.println("changed");
				onEditAction();
			}
		});
	}

	private void addNewRow(String label,JComponent field,GridBagConstraints c) {
		c.gridx=0;
		c.weightx=0;
		c.weighty=0;
		this.add(new JLabel(label+" "),c);
		c.gridx=1;
		c.weightx=1;
		this.add(field,c);
		c.gridy++;
	}

	private void onEditAction() {
		try {
			myBone.setName(name.getText());
			String name1 = name.getText();
			double dd = Double.valueOf(d.getText());
			double rr = Double.valueOf(r.getText());
			double aa = Double.valueOf(alpha.getText());
			double tt = Double.valueOf(theta.getText());
			double aMax = Double.valueOf(tMax.getText());
			double aMin = Double.valueOf(tMin.getText());
			String shapeFilename = shape.getText();
			myBone.set(name1, dd, rr, aa, tt, aMax, aMin, shapeFilename);
		} catch(Exception e) {}
	}

	public static void main(String[] args) {
		Log.start();
		JFrame frame = new JFrame("RobotArmBoneEditorPanel");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new RobotArmBoneEditorPanel(new RobotArmBone()));
		frame.pack();
		frame.setVisible(true);
	}
}
