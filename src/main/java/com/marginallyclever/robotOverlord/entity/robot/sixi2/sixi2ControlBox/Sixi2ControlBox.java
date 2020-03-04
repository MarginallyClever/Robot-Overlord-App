package com.marginallyclever.robotOverlord.entity.robot.sixi2.sixi2ControlBox;


import java.util.ArrayList;

import javax.swing.JPanel;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.model.ModelFactory;
import com.marginallyclever.robotOverlord.entity.modelInWorld.ModelInWorld;

/**
 * 
 * @author Dan Royer
 */
public class Sixi2ControlBox extends ModelInWorld {
	Sixi2ControlBoxPanel panel;
	
	public Sixi2ControlBox() {
		super();
		
		setName("Sixi2ControlBox");
		
		try {
			this.model = ModelFactory.createModelFromFilename("/Sixi2/box.stl",0.1f);
			this.setModelRotation(90, 0, 90);
			this.setModelOrigin(0,0,0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		
		// remove material panel
		list.remove(list.size()-1);
		// remove model panel
		list.remove(list.size()-1);

		panel = new Sixi2ControlBoxPanel(gui,this);
		list.add(panel);
		
		return list;
	}
}
