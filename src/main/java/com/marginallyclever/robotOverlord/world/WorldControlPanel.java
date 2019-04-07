package com.marginallyclever.robotOverlord.world;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.commands.UserCommandAddEntity;
import com.marginallyclever.robotOverlord.commands.UserCommandSelectNumber;
import com.marginallyclever.robotOverlord.entity.Entity;

public class WorldControlPanel extends JPanel implements ChangeListener, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private RobotOverlord gui;
	private World world;
	protected JList<?> entityList;
	protected UserCommandAddEntity addButton;
	protected transient UserCommandSelectNumber gridWidth;
	protected transient UserCommandSelectNumber gridHeight;

	public WorldControlPanel(RobotOverlord gui,World world) {
		super();
		
		this.gui = gui;
		this.world = world;

		// A list of all the elements in the world.
		// TODO add a search feature?
		updateEntityList();
	}
	
	public void updateEntityList() {
		this.removeAll();

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.weighty=0;
		c.anchor=GridBagConstraints.NORTHWEST;
		c.fill=GridBagConstraints.HORIZONTAL;
		
		this.add(addButton = new UserCommandAddEntity(gui),c);
		addButton.addActionListener(this);
		c.gridy++;

		// (re)create the entity list
		Iterator<Entity> ie = world.getChildren().iterator();
		List<EntityListItem> localEntityList = new ArrayList<EntityListItem>(); 
		while(ie.hasNext()) {
			Entity e = ie.next();
			localEntityList.add(new EntityListItem(e));
		}
		entityList = new JList<EntityListItem>(new Vector<EntityListItem>(localEntityList));
		entityList.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		c.weighty=1;
		this.add(entityList,c);
		c.gridy++;
		
		entityList.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
		        JList<?> list = (JList<?>)evt.getSource();
		        if (evt.getClickCount() == 2) {

		            // Double-click detected
		            int index = list.locationToIndex(evt.getPoint());
		            Entity e = localEntityList.get(index).entity;
		            gui.pickEntity(e);
		        }
		    }
		});
		
		this.add(gridWidth=new UserCommandSelectNumber(gui,"Grid Width",world.gridWidth),c);
		c.gridy++;
		this.add(gridHeight=new UserCommandSelectNumber(gui,"Grid Depth",world.gridHeight),c);
		c.gridy++;
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() == gridWidth) {
			world.gridWidth = (int)gridWidth.getValue();
		}
		if(e.getSource() == gridHeight) {
			world.gridWidth = (int)gridWidth.getValue();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() == addButton) {
			
		}
	}

}
