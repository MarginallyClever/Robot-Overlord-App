package com.marginallyclever.robotOverlord.gridEntity;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.robotOverlord.CollapsiblePanel;
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

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.weighty=1;
		c.anchor=GridBagConstraints.NORTHWEST;
		c.fill=GridBagConstraints.HORIZONTAL;

		CollapsiblePanel oiwPanel = new CollapsiblePanel("Grid");
		this.add(oiwPanel,c);
		JPanel contents = oiwPanel.getContentPane();		
		
		contents.setBorder(new EmptyBorder(0,0,0,0));
		contents.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		
		contents.add(gridWidth=new UserCommandSelectNumber(gui,"Width",(float)grid.width),c);
		c.gridy++;
		contents.add(gridHeight=new UserCommandSelectNumber(gui,"Depth",(float)grid.height),c);
		c.gridy++;
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
