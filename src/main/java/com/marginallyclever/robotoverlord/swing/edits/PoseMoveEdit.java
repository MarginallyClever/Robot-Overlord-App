package com.marginallyclever.robotoverlord.swing.edits;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.vecmath.Matrix4d;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * An undoable command to make a physical entity move.
 *  
 * @author Dan Royer
 *
 */
public class PoseMoveEdit extends AbstractUndoableEdit {
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
	 * @param name name of the edit
	 */
	public PoseMoveEdit(List<Entity> entities, Matrix4d oldPivot, Matrix4d newPivot,String name) {
		super();

		this.entities.addAll(entities);
		this.prev = oldPivot;
		this.next = newPivot;

		doIt(prev,next);
	}

	@Override
	public String getPresentationName() {
		if(entities.size()==1) {
			return Translator.get("PoseMoveEdit.one", entities.get(0).getName());
		} else {
			return Translator.get("PoseMoveEdit.many", String.valueOf(entities.size()));
		}
	}

	private void doIt(Matrix4d before, Matrix4d after) {
		Matrix4d diff = new Matrix4d(before);
		diff.invert();
		diff.mul(after,diff);

		for(Entity e : entities) {
			PoseComponent pose = e.getComponent(PoseComponent.class);
			if(pose!=null) {
				Matrix4d m = pose.getWorld();
				m.mul(diff,m);
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
			PoseMoveEdit moveEdit = (PoseMoveEdit)anEdit;
			if(new HashSet<>(moveEdit.entities).containsAll(this.entities)) {
				this.next.set(moveEdit.next);
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "{prev="+prev.toString()+", next="+next.toString()+"}";
	}
}
