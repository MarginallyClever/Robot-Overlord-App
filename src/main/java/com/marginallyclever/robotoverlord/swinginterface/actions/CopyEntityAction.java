package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;

/**
 * Makes a deep copy of the selected {@link com.marginallyclever.robotoverlord.Entity}.
 */
public class CopyEntityAction extends AbstractAction {
    protected final RobotOverlord ro;

    public CopyEntityAction(RobotOverlord ro) {
        super();
        this.ro=ro;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<Entity> list = ro.getSelectedEntities();
        List<Entity> deepCopy = new LinkedList<>();
        for(Entity entity : list) {
            deepCopy.add(makeDeepCopy(entity));
        }
        ro.setCopiedEntities(deepCopy);
    }

    private Entity makeDeepCopy(Entity entity) {
        throw new UnsupportedOperationException();
    }
}
