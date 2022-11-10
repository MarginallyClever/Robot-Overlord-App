package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.components.PoseComponent;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.vecmath.Matrix4d;
import java.io.Serial;
import java.util.LinkedList;
import java.util.List;

/**
 * An undoable command to make a physical entity move.
 *  
 * @author Dan Royer
 *
 */
public class PoseMoveEdit extends AbstractUndoableEdit {
	@Serial
	private static final long serialVersionUID = 1L;
		
	private final List<Entity> entities = new LinkedList<>();
	private final Matrix4d next;
	private final Matrix4d prev;
	
	/**
	 * 
	 * @param entity who
	 * @param oldPivot pivot point before move
	 * @param newPivot pivot point after move
	 */
	public PoseMoveEdit(Entity entity, Matrix4d oldPivot, Matrix4d newPivot) {
		super();

		this.entities.add(entity);
		this.prev = oldPivot;
		this.next = newPivot;

		doIt(prev,next);
	}

	/**
	 *
	 * @param entities who
	 * @param oldPivot pivot point before move
	 * @param newPivot pivot point after move
	 */
	public PoseMoveEdit(List<Entity> entities, Matrix4d oldPivot, Matrix4d newPivot) {
		super();

		this.entities.addAll(entities);
		this.prev = oldPivot;
		this.next = newPivot;

		doIt(prev,next);
	}

	private void doIt(Matrix4d before,Matrix4d after) {
		Matrix4d diff = new Matrix4d(before);
		diff.invert();
		diff.mul(after);

		for(Entity e : entities) {
			PoseComponent pose = e.findFirstComponent(PoseComponent.class);
			if(pose!=null) {
				Matrix4d m = pose.getWorld();
				m.mul(m,diff);
				pose.setWorld(m);
			}
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt(prev,next);
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		doIt(next,prev);
	}
	
	@Override
	public boolean addEdit(UndoableEdit anEdit) {
		if(anEdit instanceof PoseMoveEdit) {
			PoseMoveEdit APEM = (PoseMoveEdit)anEdit;
			if(APEM.entities ==this.entities) return true;
		}
		return super.addEdit(anEdit);
	}
}
