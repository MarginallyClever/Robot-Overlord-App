package com.marginallyclever.robotOverlord.uiElements.view;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.ColorEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.Vector3dEntity;
import com.marginallyclever.robotOverlord.uiElements.undoRedo.commands.UserCommandSelectBoolean;
import com.marginallyclever.robotOverlord.uiElements.undoRedo.commands.UserCommandSelectColorRGBA;
import com.marginallyclever.robotOverlord.uiElements.undoRedo.commands.UserCommandSelectComboBox;
import com.marginallyclever.robotOverlord.uiElements.undoRedo.commands.UserCommandSelectFile;
import com.marginallyclever.robotOverlord.uiElements.undoRedo.commands.UserCommandSelectInt;
import com.marginallyclever.robotOverlord.uiElements.undoRedo.commands.UserCommandSelectDouble;
import com.marginallyclever.robotOverlord.uiElements.undoRedo.commands.UserCommandSelectVector3d;

public class ViewPanel extends JPanel implements View {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	RobotOverlord ro;

	public ViewPanel(RobotOverlord ro) {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.ro=ro;
		//setBorder(new LineBorder(Color.RED));
		setBorder(new EmptyBorder(5,5,5,5));
	}

	@Override
	public void addReadOnly(String s) {
		JLabel label = new JLabel(s,JLabel.LEADING);
		this.add(label);
	}
	
	@Override
	public void addReadOnly(Entity e) {
		JLabel label = new JLabel(e.toString(),JLabel.LEADING);
		this.add(label);
		
		// this label needs to observe e.  When e changes the label is notified.
	}
	
	@Override
	public void addBoolean(BooleanEntity e) {
		UserCommandSelectBoolean b = new UserCommandSelectBoolean(ro,e);
		e.addObserver(b);
		this.add(b);
		
		// this label needs to observe e.  When e changes the label is notified.
	}
	
	@Override
	public void addEnum(IntEntity e,String [] listOptions) {
		UserCommandSelectComboBox b = new UserCommandSelectComboBox(ro, e, listOptions);
		e.addObserver(b);
		this.add(b);
		
		// this label needs to observe e.  When e changes the label is notified.
	}

	@Override
	public void addFilename(StringEntity e) {
		UserCommandSelectFile b = new UserCommandSelectFile(ro, e);
		e.addObserver(b);
		this.add(b);
	}

	@Override
	public void addColor(ColorEntity e) {
		UserCommandSelectColorRGBA b = new UserCommandSelectColorRGBA(ro, e);
		e.addObserver(b);
		this.add(b);
	}

	@Override
	public void addVector3(Vector3dEntity e) {
		UserCommandSelectVector3d b = new UserCommandSelectVector3d(ro, e);
		e.addObserver(b);
		this.add(b);
	}

	@Override
	public void addNumber(IntEntity e) {
		UserCommandSelectInt b = new UserCommandSelectInt(ro, e);
		e.addObserver(b);
		this.add(b);
	}

	@Override
	public void addNumber(DoubleEntity e) {
		UserCommandSelectDouble b = new UserCommandSelectDouble(ro, e);
		e.addObserver(b);
		this.add(b);
	}
}
