package com.marginallyclever.robotOverlord.robot;

import java.util.ArrayList;
import java.util.List;

/**
 * the list of tasks each robot should perform
 * @author danroyer
 *
 */
public class RobotProgram {
	List<RobotInstruction> instructions;
	int currentIndex;
	
	public RobotProgram() {
		instructions = new ArrayList<RobotInstruction>();
		currentIndex=0;
	}
	
	/**
	 * insert a new instruction to the end of the list
	 * @param e
	 */
	public void addInstruction(RobotInstruction element) {
		instructions.add(element);
	}
	
	/**
	 * insert a new instruction at index.  anything at or after this point will be pushed back on the list.
	 * @param index
	 * @param element
	 */
	public void addInstruction(int index, RobotInstruction element) {
		instructions.add(index, element);
	}
	
	public int size() {
		return instructions.size();
	}
	
	public RobotInstruction get(int index) {
		return instructions.get(index);
	}
	
	public RobotInstruction getCurrentInstruction() {
		return get(currentIndex);
	}
	
	public void setCurrentInstruction(int index) {
		assert( index >= 0 );
		assert( index < instructions.size() );
		currentIndex = index;
	}
	
	public int getCurrentIndex() {
		return currentIndex;
	}
	
	public boolean hasNext() {
		return currentIndex < instructions.size();
	}
	
	public RobotInstruction next() {
		if(currentIndex >= instructions.size() ) return null;

		RobotInstruction i = getCurrentInstruction();
		currentIndex++;
		
		return i;
	}
}
