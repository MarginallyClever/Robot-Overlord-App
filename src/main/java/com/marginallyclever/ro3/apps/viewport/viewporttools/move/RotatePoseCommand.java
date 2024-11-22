package com.marginallyclever.ro3.apps.viewport.viewporttools.move;

import com.marginallyclever.ro3.node.nodes.pose.Pose;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>A Command to rotate a list of {@link Pose}s.  Being a Command means that it can be undone and redone.</p>
 * <p>An extra challenge is that some {@link Pose} are children of other {@link Pose}s, so the rotation
 * of a parent is automatically applied to the children.</p>
 * <p>An extra challenge is the collective point of rotation for a group of {@link Pose}s.</p>
 *
 */
public class RotatePoseCommand extends AbstractUndoableEdit {
    private final List<Pose> subjects = new ArrayList<>();
    private final Vector3d v = new Vector3d();

    /**
     * Create a new command to rotate a list of {@link Pose}s.
     * @param subjects the list of {@link Pose}s to rotate.
     * @param v the rotation vector is relative to the start of the user's move action.
     */
    public RotatePoseCommand(List<Pose> subjects, Vector3d v) {
        this.subjects.addAll(subjects);
        this.v.set(v);
        moveAllSubjects(v,1);
    }

    @Override
    public String getPresentationName() {
        int count = subjects.size();
        return count>1? "Rotate "+count+" Nodes" : "Rotate node";
    }

    @Override
    public void redo() {
        super.redo();
        moveAllSubjects(v,1);
    }

    @Override
    public void undo() {
        super.undo();
        moveAllSubjects(v,-1);
    }

    private void moveAllSubjects(Vector3d v,double scale) {
        for(Pose pose : subjects) {
            Matrix4d m = pose.getWorld();
            m.m03 += v.x * scale;
            m.m13 += v.y * scale;
            m.m23 += v.z * scale;
            pose.setWorld(m);
        }
    }

    /**
     * This default implementation returns false.
     *
     * @param anEdit the edit to be added
     * @return false
     *
     * @see UndoableEdit#addEdit
     */
    @Override
    public boolean addEdit(UndoableEdit anEdit) {
        if (anEdit instanceof RotatePoseCommand other) {
            if (subjects.equals(other.subjects)) {
                v.add(other.v);
                // we don't need to moveAllSubjects(other.v) because anEdit constructor already did it.
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canRedo() {
        return true;
    }
}
