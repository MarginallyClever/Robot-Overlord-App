package com.marginallyclever.robotOverlord.swingInterface.actions;

import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.entity.AbstractEntity;

/**
 * Undoable action to select a Vector3d.
 * <p>
 * Some Entities have Vector3d (x,y,z) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class ActionChangeVector3d extends ActionChangeAbstractEntity<Vector3d> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ActionChangeVector3d(AbstractEntity<Vector3d> e, Vector3d newValue) {
		super(e, newValue);
	}
}
