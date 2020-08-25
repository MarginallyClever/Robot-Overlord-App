/**
 * 
 */
package com.marginallyclever.robotOverlord.entity.scene.recording2;


import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * @author Dan Royer
 *
 */
public class RobotTask extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DoubleEntity feedrate=new DoubleEntity("Feedrate",25);  // max speed towards this pose
	public DoubleEntity acceleration=new DoubleEntity("Acceleration",5);  // acceleration towards this pose
	public StringEntity extra=new StringEntity("Extra","");  // additional commands to execute
	public DoubleEntity wait=new DoubleEntity("Wait",5);  // wait time at this pose
	
	public RobotTask() {
		super("Task");
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Ta", "Task");
		view.add(feedrate);
		view.add(acceleration);
		view.addStaticText("Feedrate + accel to reach this pose.");
		view.add(extra);
		view.addStaticText("Extra happens on arrival at pose.");
		view.add(wait);
		view.addStaticText("Wait occurs after extra.");
		view.popStack();
		super.getView(view);
	}
}
