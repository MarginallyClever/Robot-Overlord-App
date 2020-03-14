package com.marginallyclever.robotOverlord.entity.robot.sixi2.sixi2ControlBox;


import com.marginallyclever.robotOverlord.engine.model.ModelFactory;
import com.marginallyclever.robotOverlord.entity.modelEntity.ModelEntity;

/**
 * 
 * @author Dan Royer
 */
public class Sixi2ControlBox extends ModelEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2297549245663793571L;
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
}
