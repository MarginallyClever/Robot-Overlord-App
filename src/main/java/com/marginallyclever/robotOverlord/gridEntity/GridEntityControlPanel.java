package com.marginallyclever.robotOverlord.gridEntity;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.commands.UserCommandAddEntity;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectNumber;

public class GridEntityControlPanel extends JPanel implements ChangeListener {
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

	public GridEntityControlPanel(RobotOverlord gui,GridEntity grid) {
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
		this.setLayout(new GridBagLayout());
		this.setBorder(new EmptyBorder(0,0,0,0));
		
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=0;
		con1.weighty=0;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.FIRST_LINE_START;
		
		this.add(gridWidth=new UserCommandSelectNumber(gui,"Width",(float)grid.width),con1);
		con1.gridy++;
		con1.weighty=1;  // last item only
		this.add(gridHeight=new UserCommandSelectNumber(gui,"Depth",(float)grid.height),con1);
		con1.gridy++;
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
