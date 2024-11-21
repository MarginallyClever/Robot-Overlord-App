package com.marginallyclever.ro3.apps.viewport.viewporttools.move;

import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A Command to translate a list of {@link Pose}s.  Being a Command means that it can be undone and redone.
 * An extra challenge is that some {@link Pose} are children of other {@link Pose}s, so the translation
 * of a parent is automatically applied to the children.
 */
public class TranslatePoseCommand extends AbstractUndoableEdit {
    private final List<Pose> subjects = new ArrayList<>();
    private final Vector3d v = new Vector3d();

    /**
     * Create a new command to translate a list of {@link Pose}s.
     * @param subjects the list of {@link Pose}s to translate.
     * @param v the translation vector is relative to the start of the user's move action.
     */
    TranslatePoseCommand(List<Pose> subjects, Vector3d v) {
        this.subjects.addAll(subjects);
        this.v.set(v);
        moveAllSubjects(v);
    }

    @Override
    public String getPresentationName() {
        int count = subjects.size();
        return count>1? "Translate "+count+" Nodes" : "Translate node";
    }

    @Override
    public void redo() {
        super.redo();
        moveAllSubjects(v);
    }

    @Override
    public void undo() {
        super.undo();
        Vector3d v = new Vector3d(this.v);
        v.scale(-1);
        moveAllSubjects(v);
    }

    private void moveAllSubjects(Vector3d v) {
        for(Pose pose : subjects) {
            Matrix4d m = pose.getWorld();
            m.m03 += v.x;
            m.m13 += v.y;
            m.m23 += v.z;
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
        if (anEdit instanceof TranslatePoseCommand other) {
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
