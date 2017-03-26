package com.marginallyclever.robotOverlord.makelangeloRobot.settings.hardwareProperties;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.makelangeloRobot.MakelangeloRobot;

/**
 * Properties of each Makelangelo version.  These are non-configurable features unique to each machine.
 * @author Dan Royer
 *
 */
public interface MakelangeloHardwareProperties {
	public int getVersion();
	public String getName();
	
	public boolean canChangeMachineSize();
	
	public boolean canAccelerate();
	
	public boolean canChangePulleySize();

	public boolean canInvertMotors();
	
	public boolean canAutoHome();
	
	public float getWidth();
	public float getHeight();
	
	/**
	 * custom look and feel for each version
	 * @param gl2 the opengl context to call when rendering this hardware
	 * @param robot the instance of the robot that has these hardware properties
	 */
	public void render(GL2 gl2,MakelangeloRobot robot);
	
	public void doAbout();
}
