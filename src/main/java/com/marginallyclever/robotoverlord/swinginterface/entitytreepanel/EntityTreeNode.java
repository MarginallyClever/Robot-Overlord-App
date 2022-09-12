package com.marginallyclever.robotoverlord.swinginterface.entitytreepanel;

import com.marginallyclever.robotoverlord.Entity;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This version of DefaultMutableTreeNode exists only to override the toString() method.
 * @author Dan Royer
 *
 */
public class EntityTreeNode extends DefaultMutableTreeNode {
	public EntityTreeNode(Object obj) {
		super(obj);
	}

	@Override
	public String toString() {
		return ((Entity)userObject).getName();
	}
}
