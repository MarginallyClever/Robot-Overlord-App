package com.marginallyclever.robotOverlord.entity.world;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JList;
import javax.swing.JPanel;
import com.marginallyclever.convenience.PanelHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandAddEntity;

public class WorldPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private RobotOverlord gui;
	
	@SuppressWarnings("unused")
	private World world;
	
	protected JList<?> entityList;
	protected UserCommandAddEntity addButton;

	public WorldPanel(RobotOverlord gui,World world) {
		super();
		
		this.gui = gui;
		this.world = world;

		this.setName("World");
		this.setLayout(new GridBagLayout());

		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();
		
		addButton = new UserCommandAddEntity(gui);
		this.add(addButton,con1);
	    
		PanelHelper.ExpandLastChild(this, con1);
	}

}
