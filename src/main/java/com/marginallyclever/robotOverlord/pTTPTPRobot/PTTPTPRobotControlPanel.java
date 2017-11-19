package com.marginallyclever.robotOverlord.pTTPTPRobot;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.robotOverlord.CollapsiblePanel;
import com.marginallyclever.robotOverlord.HTMLDialogBox;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectFile;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectNumber;
import com.marginallyclever.robotOverlord.model.ModelLoadAndSave;

public class PTTPTPRobotControlPanel extends JPanel implements ActionListener, ChangeListener {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 257878994328366520L;

	private final double [] speedOptions = {0.1, 0.2, 0.5, 
			                                1, 2, 5, 
			                                10, 20, 50};
	
	private JButton arm5Apos;
	private JButton arm5Aneg;
	private JButton arm5Bpos;
	private JButton arm5Bneg;
	private JButton arm5Cpos;
	private JButton arm5Cneg;
	private JButton arm5Dpos;
	private JButton arm5Dneg;
	private JButton arm5Epos;
	private JButton arm5Eneg;
	private JButton arm5Fpos;
	private JButton arm5Fneg;
	
	private JButton arm5Xpos;
	private JButton arm5Xneg;
	private JButton arm5Ypos;
	private JButton arm5Yneg;
	private JButton arm5Zpos;
	private JButton arm5Zneg;
	
	private JButton arm5Upos;
	private JButton arm5Uneg;
	private JButton arm5Vpos;
	private JButton arm5Vneg;
	private JButton arm5Wpos;
	private JButton arm5Wneg;
	
	public JLabel xPos,yPos,zPos,uPos,vPos,wPos;
	public JLabel a1,b1,c1,d1,e1,f1;
	private JLabel speedNow;
	private JLabel uid;
	private JSlider speedControl;
	
	private JButton about;
	
	private UserCommandSelectFile partA;
	private UserCommandSelectFile partB;
	private UserCommandSelectFile partC;
	private UserCommandSelectFile partD;
	private UserCommandSelectFile partE;
	private UserCommandSelectFile partF;
	private UserCommandSelectFile partG;
	private UserCommandSelectNumber partScale;
	
	private PTTPTPRobot robot=null;
	
	
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}


	public PTTPTPRobotControlPanel(RobotOverlord gui,PTTPTPRobot robot) {
		super();

		JPanel p;
		
		this.robot = robot;

		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTH;

		CollapsiblePanel partsPanel = createPartsPanel(gui,robot);
		this.add(partsPanel, con1);
		con1.gridy++;
		
		CollapsiblePanel speedPanel = createSpeedPanel();
		this.add(speedPanel,con1);
		con1.gridy++;

		CollapsiblePanel fkPanel = new CollapsiblePanel("Forward Kinematics");
		this.add(fkPanel,con1);
		con1.gridy++;
		
		// used for fk 
		a1 = new JLabel("0.00");
		b1 = new JLabel("0.00");
		c1 = new JLabel("0.00");
		d1 = new JLabel("0.00");
		e1 = new JLabel("0.00");
		f1 = new JLabel("0.00");

		
		p = new JPanel(new GridLayout(6,3));
		fkPanel.getContentPane().add(p);

		p.add(arm5Apos = createButton("BA+"));		p.add(a1);		p.add(arm5Aneg = createButton("BA-"));
		p.add(arm5Bpos = createButton("CB+"));		p.add(b1);		p.add(arm5Bneg = createButton("CB-"));
		p.add(arm5Cpos = createButton("DC+"));		p.add(c1);		p.add(arm5Cneg = createButton("DC-"));
		p.add(arm5Dpos = createButton("ED+"));		p.add(d1);		p.add(arm5Dneg = createButton("ED-"));
		p.add(arm5Epos = createButton("FE+"));		p.add(e1);		p.add(arm5Eneg = createButton("FE-"));
		p.add(arm5Fpos = createButton("GF+"));		p.add(f1);		p.add(arm5Fneg = createButton("GF-"));
		
		CollapsiblePanel ikPanel = new CollapsiblePanel("Inverse Kinematics");
		this.add(ikPanel, con1);
		con1.gridy++;

		// used for ik 
		xPos = new JLabel("0.00");
		yPos = new JLabel("0.00");
		zPos = new JLabel("0.00");
		uPos = new JLabel("0.00");
		vPos = new JLabel("0.00");
		wPos = new JLabel("0.00");
		
		p = new JPanel(new GridLayout(6,3));
		ikPanel.getContentPane().add(p);
		
		p.add(arm5Xpos = createButton("X+"));		p.add(xPos);		p.add(arm5Xneg = createButton("X-"));
		p.add(arm5Ypos = createButton("Y+"));		p.add(yPos);		p.add(arm5Yneg = createButton("Y-"));
		p.add(arm5Zpos = createButton("Z+"));		p.add(zPos);		p.add(arm5Zneg = createButton("Z-"));
		p.add(arm5Upos = createButton("U+"));		p.add(uPos);		p.add(arm5Uneg = createButton("U-"));
		p.add(arm5Vpos = createButton("V+"));		p.add(vPos);		p.add(arm5Vneg = createButton("V-"));
		p.add(arm5Wpos = createButton("W+"));		p.add(wPos);		p.add(arm5Wneg = createButton("W-"));
		
		about = createButton("About this robot");
		this.add(about, con1);
		con1.gridy++;
	}
	
	protected CollapsiblePanel createPartsPanel(RobotOverlord gui,PTTPTPRobot robot) {
		CollapsiblePanel panel = new CollapsiblePanel("Parts");
		GridBagConstraints con2 = new GridBagConstraints();
		con2.gridx=0;
		con2.gridy=0;
		con2.fill=GridBagConstraints.HORIZONTAL;
		con2.anchor=GridBagConstraints.NORTHWEST;
		con2.weighty=1;
		con2.weightx=0.25;
		
		partA = new UserCommandSelectFile(gui,"Part A",robot.getPart(6));
		partB = new UserCommandSelectFile(gui,"Part B",robot.getPart(5));
		partC = new UserCommandSelectFile(gui,"Part C",robot.getPart(4));
		partD = new UserCommandSelectFile(gui,"Part D",robot.getPart(3));
		partE = new UserCommandSelectFile(gui,"Part E",robot.getPart(2));
		partF = new UserCommandSelectFile(gui,"Part F",robot.getPart(1));
		partG = new UserCommandSelectFile(gui,"Part G",robot.getPart(0));
		partScale = new UserCommandSelectNumber(gui, "Part scale", robot.getPartScale());
		
		panel.getContentPane().add(partA,con2);  con2.gridy++;
		panel.getContentPane().add(partB,con2);  con2.gridy++;
		panel.getContentPane().add(partC,con2);  con2.gridy++;
		panel.getContentPane().add(partD,con2);  con2.gridy++;
		panel.getContentPane().add(partE,con2);  con2.gridy++;
		panel.getContentPane().add(partF,con2);  con2.gridy++;
		panel.getContentPane().add(partG,con2);  con2.gridy++;
		panel.getContentPane().add(partScale,con2);  con2.gridy++;

		// Find all the serviceLoaders for loading files.
		ServiceLoader<ModelLoadAndSave> loaders = ServiceLoader.load(ModelLoadAndSave.class);
		Iterator<ModelLoadAndSave> i = loaders.iterator();
		while(i.hasNext()) {
			ModelLoadAndSave loader = i.next();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(loader.getEnglishName(), loader.getValidExtensions());
			partA.addChoosableFileFilter(filter);
			partB.addChoosableFileFilter(filter);
			partC.addChoosableFileFilter(filter);
			partD.addChoosableFileFilter(filter);
			partE.addChoosableFileFilter(filter);
			partF.addChoosableFileFilter(filter);
			partG.addChoosableFileFilter(filter);
		}
		partA.addChangeListener(this);
		partB.addChangeListener(this);
		partC.addChangeListener(this);
		partD.addChangeListener(this);
		partE.addChangeListener(this);
		partF.addChangeListener(this);
		partG.addChangeListener(this);
		partScale.addChangeListener(this);
		
		return panel;
	}
	
	protected CollapsiblePanel createSpeedPanel() {
		double speed=robot.getSpeed();
		int speedIndex;
		for(speedIndex=0;speedIndex<speedOptions.length;++speedIndex) {
			if( speedOptions[speedIndex] >= speed )
				break;
		}
		speedNow = new JLabel(Double.toString(speedOptions[speedIndex]),JLabel.CENTER);
		java.awt.Dimension dim = speedNow.getPreferredSize();
		dim.width = 50;
		speedNow.setPreferredSize(dim);

		CollapsiblePanel speedPanel = new CollapsiblePanel("Speed");
		
		GridBagConstraints con2 = new GridBagConstraints();
		con2.gridx=0;
		con2.gridy=0;
		con2.fill=GridBagConstraints.HORIZONTAL;
		con2.anchor=GridBagConstraints.NORTHWEST;
		con2.weighty=1;
		con2.weightx=0.25;
		speedPanel.getContentPane().add(speedNow,con2);

		speedControl = new JSlider(0,speedOptions.length-1,speedIndex);
		speedControl.addChangeListener(this);
		speedControl.setMajorTickSpacing(speedOptions.length-1);
		speedControl.setMinorTickSpacing(1);
		speedControl.setPaintTicks(true);
		con2.anchor=GridBagConstraints.NORTHEAST;
		con2.fill=GridBagConstraints.HORIZONTAL;
		con2.weightx=0.75;
		con2.gridx=1;
		speedPanel.getContentPane().add(speedControl,con2);
		
		return speedPanel;
	}

	protected void setSpeed(double speed) {
		robot.setSpeed(speed);
		speedNow.setText(Double.toString(robot.getSpeed()));
	}
	
	public void stateChanged(ChangeEvent e) {
		Object subject = e.getSource();
		if( subject == speedControl ) {
			int i=speedControl.getValue();
			setSpeed(speedOptions[i]);
		}
		if( subject == partA ) robot.setPart(6,partA.getFilename());
		if( subject == partB ) robot.setPart(5,partB.getFilename());
		if( subject == partC ) robot.setPart(4,partC.getFilename());
		if( subject == partD ) robot.setPart(3,partD.getFilename());
		if( subject == partE ) robot.setPart(2,partE.getFilename());
		if( subject == partF ) robot.setPart(1,partF.getFilename());
		if( subject == partG ) robot.setPart(0,partG.getFilename());
		if( subject == partScale ) robot.setPartScale(partScale.getValue());
	}
	
	
	// arm5 controls
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();			
		
		if( subject == arm5Apos ) robot.moveA(-1);
		if( subject == arm5Aneg ) robot.moveA(1);
		if( subject == arm5Bpos ) robot.moveB(1);
		if( subject == arm5Bneg ) robot.moveB(-1);
		if( subject == arm5Cpos ) robot.moveC(1);
		if( subject == arm5Cneg ) robot.moveC(-1);
		if( subject == arm5Dpos ) robot.moveD(-1);
		if( subject == arm5Dneg ) robot.moveD(1);
		if( subject == arm5Epos ) robot.moveE(1);
		if( subject == arm5Eneg ) robot.moveE(-1);
		if( subject == arm5Fpos ) robot.moveF(1);
		if( subject == arm5Fneg ) robot.moveF(-1);
		
		if( subject == arm5Xpos ) robot.moveX(1);
		if( subject == arm5Xneg ) robot.moveX(-1);
		if( subject == arm5Ypos ) robot.moveY(1);
		if( subject == arm5Yneg ) robot.moveY(-1);
		if( subject == arm5Zpos ) robot.moveZ(1);
		if( subject == arm5Zneg ) robot.moveZ(-1);
		
		if( subject == arm5Upos ) robot.moveU(1);
		if( subject == arm5Uneg ) robot.moveU(-1);
		if( subject == arm5Vpos ) robot.moveV(1);
		if( subject == arm5Vneg ) robot.moveV(-1);
		if( subject == arm5Wpos ) robot.moveW(1);
		if( subject == arm5Wneg ) robot.moveW(-1);
		
		if( subject == about ) doAbout();
	}
	
	protected void doAbout() {
		HTMLDialogBox box = new HTMLDialogBox();
		box.display(this.getRootPane(), "<html><body>"
				+"<h1>PTTPTP Robot</h1>"
				+"<p>Created by Dan Royer (dan@marginallyclever.com).</p><br>"
				+"<p>A pan tilt tilt pan tilt pan style six axis manipulator. </p><br>"
				+"</body></html>", "About "+this.robot.getDisplayName());
	}
	
	
	public void setUID(long id) {
		if(uid!=null) {
			uid.setText("Evil Minion #"+Long.toString(id));
		}
	}
}
