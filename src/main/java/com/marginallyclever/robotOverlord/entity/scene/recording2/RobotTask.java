/**
 * 
 */
package com.marginallyclever.robotOverlord.entity.scene.recording2;


import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * @author Dan Royer
 *
 */
public class RobotTask extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DoubleEntity time=new DoubleEntity("Time",5);  // time to reach the next pose
	public StringEntity extra=new StringEntity("Extra","");  // additional commands to execute
	protected Memento memento;

	public RobotTask() {
		super("Task");
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Ta", "Task");
		view.add(time);
		view.addStaticText("time to reach this pose.");
		view.add(extra);
		view.addStaticText("Extra command happens at start of task.");
		view.popStack();
		super.getView(view);
	}
	
	public Memento getMemento() {
		return memento;
	}

	public void setMemento(Memento memento) {
		this.memento = memento;
	}
}
