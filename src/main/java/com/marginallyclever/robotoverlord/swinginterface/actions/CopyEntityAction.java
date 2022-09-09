package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.EditorAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Makes a deep copy of the selected {@link com.marginallyclever.robotoverlord.Entity}.
 */
public class CopyEntityAction extends AbstractAction implements EditorAction {
    protected final RobotOverlord ro;

    public CopyEntityAction(String name,RobotOverlord ro) {
        super(name);
        this.ro=ro;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<Entity> list = ro.getSelectedEntities();
        Entity container = new Entity();
        for(Entity entity : list) {
            container.addEntity(entity);
        }
        ro.setCopiedEntities(container.deepCopy());
    }

    private Entity makeDeepCopy(Entity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateEnableStatus() {
        setEnabled(!ro.getSelectedEntities().isEmpty());
    }
}
