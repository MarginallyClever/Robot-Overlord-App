package com.marginallyclever.robotOverlord.robot;

import java.util.LinkedList;
import java.util.List;

/**
 * The list of tasks each robot should perform
 * @author danroyer
 *
 */
@Deprecated
public class RobotProgram {
	List<RobotKeyframe> keyframes;
	
	public RobotProgram() {
		keyframes = new LinkedList<RobotKeyframe>();
	}
	
	/**
	 * insert a new RobotKeyframe to the end of the list
	 * @param element a RobotKeyframe
	 */
	public void addKeyframe(RobotKeyframe element) {
		keyframes.add(element);
	}
	
	/**
	 * insert a new RobotKeyframe at index.  anything at or after this point will be pushed back on the list.
	 * @param index the target index
	 * @param element a RobotKeyframe
	 */
	public void addKeyframe(int index, RobotKeyframe element) {
		keyframes.add(index, element);
	}
	
	public void removeKeyframe(int index) {
		keyframes.remove(index);
	}
	
	public int size() {
		return keyframes.size();
	}
	
	public RobotKeyframe get(int index) {
		return keyframes.get(index);
	}
}
