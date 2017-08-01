package com.marginallyclever.robotOverlord.robot;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jogamp.opengl.GL2;

/**
 * Base class for instructions in a robot program.  Instructions should closely mirror Scratch.
 * - wait for signal X
 * - send signal Y
 * - move to position
 * - send custom instruction to robot
 * - loop over sub-instructions
 * - branch condition
 * - etc...
 * @author danroyer
 *
 */
public interface RobotInstruction {
	public String getName();
	
	/**
	 * @return A GUI element to put in a timeline (devoid of control parameters) 
	 */
	public JComponent getIcon();
	
	/**
	 * @return a panel with control parameters to tweak this instruction. 
	 */
	public JPanel getPanel();
	
	/**
	 * visualize the instruction's in 3D
	 * @param gl2 the render context
	 */
	public void render(GL2 gl2);
	
	/**
	 * run this instruction forward in time
	 * @param dt how far to run this instruction in time
	 */
	public void executeForward(float dt);

	/**
	 * run this instruction backward in time
	 * @param dt how far to run this instruction in time
	 */
	public void executeBackward(float dt);

	/**
	 * 
	 * @return 0..1 how done is this instruction?
	 */
	public float getProgress();
}
