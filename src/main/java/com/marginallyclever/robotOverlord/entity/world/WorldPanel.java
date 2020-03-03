package com.marginallyclever.robotOverlord.entity.world;

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
import javax.swing.border.EmptyBorder;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandAddEntity;
import com.marginallyclever.robotOverlord.entity.Entity;

public class WorldPanel extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private RobotOverlord gui;
	private World world;
	protected JList<?> entityList;
	protected UserCommandAddEntity addButton;

	public WorldPanel(RobotOverlord gui,World world) {
		super();
		
		this.gui = gui;
		this.world = world;

		// A list of all the elements in the world.
		// TODO add a search feature?
		buildPanel();
	}
	
	public void buildPanel() {
		this.removeAll();

		this.setName("World");
		this.setLayout(new GridBagLayout());
		this.setBorder(new EmptyBorder(0,0,0,0));

		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=0;
		con1.weighty=0;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.FIRST_LINE_START;
		
		this.add(addButton = new UserCommandAddEntity(gui),con1);
		addButton.addActionListener(this);
		con1.gridy++;

		// (re)create the entity list
		Iterator<Entity> ie = world.getChildren().iterator();
		List<EntityListItem> localEntityList = new ArrayList<EntityListItem>(); 
		while(ie.hasNext()) {
			Entity e = ie.next();
			localEntityList.add(new EntityListItem(e));
		}
		entityList = new JList<EntityListItem>(new Vector<EntityListItem>(localEntityList));
		entityList.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		con1.weighty=1;  // last item gets weight 1.
		this.add(entityList,con1);
		con1.gridy++;

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
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() == addButton) {
			
		}
	}

}
