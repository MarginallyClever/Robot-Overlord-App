package com.marginallyclever.robotOverlord.entity.world;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.marginallyclever.convenience.PanelHelper;
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

		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();
		
		this.add(addButton = new UserCommandAddEntity(gui),con1);
		addButton.addActionListener(this);
		con1.gridy++;

	    DefaultMutableTreeNode top = createTreeNodes(world);
		JTree tree = new JTree(top);
		JScrollPane entityList = new JScrollPane(tree);
		con1.weighty=1;  // last item gets weight 1.
		this.add(entityList,con1);
		con1.gridy++;

	    tree.getSelectionModel().setSelectionMode
	            (TreeSelectionModel.SINGLE_TREE_SELECTION);
	    
	    entityList.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	    
	    // See https://docs.oracle.com/javase/7/docs/api/javax/swing/JTree.html
	    tree.addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent e) {
		        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		        if(selPath != null) {
		            if(e.getClickCount() == 1) {
		                //mySingleClick(selRow, selPath);
		            }
		            else if(e.getClickCount() == 2) {
		                //myDoubleClick(selRow, selPath);
		            	DefaultMutableTreeNode o = (DefaultMutableTreeNode)selPath.getLastPathComponent();
		            	gui.pickEntity((Entity)(o.getUserObject()));
		            }
		        }
		    }
		});
	}
	
	protected DefaultMutableTreeNode createTreeNodes(Entity e) {
		DefaultMutableTreeNode parent = new DefaultMutableTreeNode(e);
		for(Entity child : e.getChildren() ) {
			parent.add(createTreeNodes(child));
		}
		return parent;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() == addButton) {
			
		}
	}

}
