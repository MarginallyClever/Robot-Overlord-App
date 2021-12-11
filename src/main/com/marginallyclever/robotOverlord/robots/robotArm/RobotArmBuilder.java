package com.marginallyclever.robotOverlord.robots.robotArm;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;

/**
 * {@code RobotArmBuilder} contains the panel that edit a {@code RobotArmFK}.
 * @author aggra
 *
 */
public class RobotArmBuilder extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private RobotArmFK arm;
	private JSpinner numBones = new JSpinner(new SpinnerNumberModel(6,1,6,1));
	private JPanel selectNumBones = new JPanel(new BorderLayout());
	private JTabbedPane tabs = new JTabbedPane();
	
	public RobotArmBuilder(RobotArmFK a) throws CloneNotSupportedException {
		super(new BorderLayout());
		
		this.arm=a;

		a.showLineage.set(true);  // does not change things back when done!
		
		int m = arm.getNumBones();
		m = m>1?m:1;
		numBones = new JSpinner(new SpinnerNumberModel(m,1,6,1));
		
		selectNumBones.add(new JLabel("# bones"),BorderLayout.LINE_START);
		selectNumBones.add(numBones,BorderLayout.CENTER);
		
		this.add(selectNumBones,BorderLayout.PAGE_START);
		this.add(tabs,BorderLayout.CENTER);
		
		numBones.addChangeListener((e)-> updateNumBones());
		updateNumBones();
	}
	
	public RobotArmBuilder() throws CloneNotSupportedException {
		this(new RobotArmFK());
	}
	
	private void updateNumBones() {
		rebuildArm();
		rebuildAllTabs();
	}
	
	private void rebuildArm() {
		RobotArmFK old = arm;
		arm = new RobotArmFK();
		int newNumBones = ((Number)numBones.getValue()).intValue();
		int stopAt = (int)Math.min(old.getNumBones(), newNumBones);
		for(int i=0;i<stopAt;++i) {
			arm.addBone(old.getBone(i));
		}
		while(stopAt<newNumBones) {
			stopAt++;
			arm.addBone(new RobotArmBone());
		}
	}

	/**
	 * build one tab for each RobotArmBone in RobotArmFK.
	 * include d,r,alpha,theta,thetaMax,thetaMin,and shapeFilename.
	 */
	private void rebuildAllTabs() {
		tabs.removeAll();
		
		int c = arm.getNumBones();
		for(int i=0;i<c;++i) {
			JPanel bonePanel = new JPanel(new GridLayout(0,2));
			tabs.addTab(Integer.toString(i),null,bonePanel,"fill me "+i);
			
			RobotArmBone bone = arm.getBone(i);
			buildBoneTab(bonePanel,bone);
		}
	}
	
	private void buildBoneTab(JPanel bonePanel, RobotArmBone bone) {
		String shapeName="";
		if(bone.getShape()!=null) shapeName = bone.getShape().getModelFilename();
				
		JTextField nameField = new JTextField(bone.getName());
		JTextField dField = new JTextField(StringHelper.formatDouble(bone.getD()));
		JTextField rField = new JTextField(StringHelper.formatDouble(bone.getR()));
		JTextField aField = new JTextField(StringHelper.formatDouble(bone.getAlpha()));
		JTextField thetaField = new JTextField(StringHelper.formatDouble(bone.getTheta()));
		JTextField angleMinField = new JTextField(StringHelper.formatDouble(bone.getAngleMin()));
		JTextField angleMaxField = new JTextField(StringHelper.formatDouble(bone.getAngleMax()));
		JTextField shapeNameField = new JTextField(shapeName);
		
		bonePanel.add(new JLabel("Name"));				bonePanel.add(nameField);
		bonePanel.add(new JLabel("D"));					bonePanel.add(dField);
		bonePanel.add(new JLabel("R"));					bonePanel.add(rField);
		bonePanel.add(new JLabel("Alpha"));				bonePanel.add(aField);
		bonePanel.add(new JLabel("Theta"));				bonePanel.add(thetaField);
		bonePanel.add(new JLabel("Theta max"));			bonePanel.add(angleMinField);
		bonePanel.add(new JLabel("Theta min"));			bonePanel.add(angleMaxField);
		bonePanel.add(new JLabel("Shape fileName"));	bonePanel.add(shapeNameField);
	}
	
	// TEST 
	
	public static void main(String[] args) throws CloneNotSupportedException {
		Log.start();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		
		ArrayList<Double> list = new ArrayList<Double>();
		for(int i=0;i<250;++i) {
			list.add(Math.random()*500);
		}

		JFrame frame = new JFrame(RobotArmBuilder.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new RobotArmBuilder());
		frame.pack();
		frame.setVisible(true);		
	}
}
