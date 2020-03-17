package com.marginallyclever.robotOverlord.swingInterface.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.ColorEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.Vector3dEntity;

public class ViewPanel extends JPanel implements View {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    GridBagConstraints gbc = new GridBagConstraints();
	RobotOverlord ro;

	public ViewPanel(RobotOverlord ro) {
		super();
		this.setLayout(new GridBagLayout());
		this.ro=ro;
		//setBorder(new LineBorder(Color.RED));
		//setBorder(new EmptyBorder(5,5,5,5));
		gbc.weightx=1;
		gbc.gridx=0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets.top=5;
		gbc.insets.left=5;
		gbc.insets.right=5;
	}

	@Override
	public void addReadOnly(String s) {
		JLabel label = new JLabel(s,JLabel.LEADING);
		this.add(label,gbc);
	}
	
	@Override
	public void addReadOnly(Entity e) {
		JLabel label = new JLabel(e.toString(),JLabel.LEADING);
		this.add(label,gbc);
	}
	
	@Override
	public void addBoolean(BooleanEntity e) {
		ViewPanelBoolean b = new ViewPanelBoolean(ro,e);
		e.addObserver(b);
		this.add(b,gbc);
	}
	
	@Override
	public void addEnum(IntEntity e,String [] listOptions) {
		ViewPanelComboBox b = new ViewPanelComboBox(ro, e, listOptions);
		e.addObserver(b);
		this.add(b,gbc);
	}

	@Override
	public void addFilename(StringEntity e,ArrayList<FileNameExtensionFilter> f) {
		ViewPanelFilename b = new ViewPanelFilename(ro, e);
		for( FileNameExtensionFilter fi : f ) {
			b.addChoosableFileFilter( fi );
		}
		
		e.addObserver(b);
		this.add(b,gbc);
	}

	@Override
	public void addColor(ColorEntity e) {
		ViewPanelColorRGBA b = new ViewPanelColorRGBA(ro, e);
		e.addObserver(b);
		this.add(b,gbc);
	}

	@Override
	public void addVector3(Vector3dEntity e) {
		ViewPanelVector3d b = new ViewPanelVector3d(ro, e);
		e.addObserver(b);
		this.add(b,gbc);
	}

	@Override
	public void addInt(IntEntity e) {
		ViewPanelInt b = new ViewPanelInt(ro, e);
		e.addObserver(b);
		this.add(b,gbc);
	}

	@Override
	public void addDouble(DoubleEntity e) {
		ViewPanelDouble b = new ViewPanelDouble(ro, e);
		e.addObserver(b);
		this.add(b,gbc);
	}
}
