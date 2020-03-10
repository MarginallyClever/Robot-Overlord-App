package com.marginallyclever.robotOverlord.entity.gridEntity;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.convenience.PanelHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandAddEntity;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSelectNumber;

public class GridEntityPanel extends JPanel implements ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private RobotOverlord gui;
	private GridEntity grid;
	protected JList<?> entityList;
	protected UserCommandAddEntity addButton;
	protected transient UserCommandSelectNumber gridWidth;
	protected transient UserCommandSelectNumber gridHeight;

	public GridEntityPanel(RobotOverlord gui,GridEntity grid) {
		super();
		
		this.gui = gui;
		this.grid = grid;

		// A list of all the elements in the world.
		// TODO add a search feature?
		buildPanel();
	}
	
	public void buildPanel() {
		this.removeAll();

		this.setName("Grid");
		this.setBorder(new EmptyBorder(5,5,5,5));
		this.setLayout(new GridBagLayout());

		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();
		
		this.add(gridWidth=new UserCommandSelectNumber(gui,"Width",(float)grid.width),con1);
		
		con1.gridy++;
		this.add(gridHeight=new UserCommandSelectNumber(gui,"Depth",(float)grid.height),con1);

		PanelHelper.ExpandLastChild(this, con1);
		gridWidth.addChangeListener(this);
		gridHeight.addChangeListener(this);
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() == gridWidth) {
			grid.width = (int)gridWidth.getValue();
		}
		if(e.getSource() == gridHeight) {
			grid.height = (int)gridHeight.getValue();
		}
	}
}
