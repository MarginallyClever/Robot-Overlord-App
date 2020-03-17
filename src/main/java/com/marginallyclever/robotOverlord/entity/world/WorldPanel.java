package com.marginallyclever.robotOverlord.entity.world;

import javax.swing.JPanel;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.swingInterface.commands.CommandAddEntity;

public class WorldPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected CommandAddEntity addButton;

	public WorldPanel(RobotOverlord gui,World world) {
		super();
		
		this.setName("World");

		addButton = new CommandAddEntity(gui);
		this.add(addButton);
	}
}