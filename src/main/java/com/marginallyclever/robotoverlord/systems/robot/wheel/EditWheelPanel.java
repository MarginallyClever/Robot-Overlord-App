package com.marginallyclever.robotoverlord.systems.robot.crab;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.demo.CrabRobotComponent;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.systems.OriginAdjustSystem;

import javax.swing.*;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.List;

/**
 * This panel is used to edit the dog robot.
 *
 * @author Dan Royer
 * @since 2.5.7
 */
public class EditWheelPanel extends JPanel {
    private final EntityManager entityManager;
    private final Entity rootEntity;
    private final WheeledRobotSystem system;
    private final WheeledRobotComponent robot;

    public EditWheelPanel(Entity rootEntity, EntityManager entityManager, WheeledRobotSystem system) {
        super(new BorderLayout());
        this.entityManager = entityManager;
        this.rootEntity = rootEntity;
        this.system = system;
        this.robot = rootEntity.getComponent(WheeledRobotComponent.class);

    }
}
