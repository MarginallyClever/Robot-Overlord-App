package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.AbstractEntity;

import javax.vecmath.Vector3d;

/**
 * Undoable action to select a Vector3d.
 * <p>
 * Some Entities have Vector3d (x,y,z) parameters.  This class ensures changing those parameters is undoable.
 *  
 * @author Dan Royer
 *
 */
public class Vector3dEdit extends AbstractEntityEdit<Vector3d> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Vector3dEdit(AbstractEntity<Vector3d> e, Vector3d newValue) {
		super(e, newValue);
	}
}
