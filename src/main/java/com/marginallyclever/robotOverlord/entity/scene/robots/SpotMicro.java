package com.marginallyclever.robotOverlord.entity.scene.robots;

import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.shape.Shape;

/**
 * Spot Micro simulation
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class SpotMicro extends RobotEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5916361555293772951L;

	public class Leg {
		public DHLink hip = new DHLink(),
				thigh = new DHLink(), 
				foot = new DHLink();
		Shape hipModel = new Shape();
		Shape thighModel = new Shape();
		Shape footModel = new Shape();
		
		public Leg() {
		}
	}
	Leg [] legs = new Leg[4];
	Shape torso = new Shape();
	
	public SpotMicro() {
		super();
		setName("Spot Micro");
		addChild(torso);
		
		torso.setShapeFilename("/SpotMicro/torso.obj");
		torso.getMaterial().setTextureFilename("/SpotMicro/sixi.png");
		for(int i=0;i<4;++i) {
			legs[i] = new Leg();
		}
		//legs[0].getModel().
	}
	
	@Override
	public Memento createKeyframe() {
		// TODO Auto-generated method stub
		return null;
	}
}
