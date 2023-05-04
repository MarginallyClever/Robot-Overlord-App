package com.marginallyclever.robotoverlord.systems.robot.robotarm;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.EntityManager;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.systems.render.mesh.load.MeshFactory;
import com.marginallyclever.robotoverlord.parameters.StringParameter;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementFilename;
import com.marginallyclever.robotoverlord.systems.OriginAdjustSystem;

import java.util.List;
import javax.swing.*;
import java.awt.*;

/**
 * Make an industrial 6 axis robot arm.  Every joint is stacked on the previous joint in a serially-linked design.
 *
 * @author Dan Royer
 * @since 2.5.7
 */
public class EditArm6Panel extends JPanel {
    private static final int NUM_JOINTS = 6;
    private static final String[] labels = {"Joint", "D", "R", "Alpha", "Theta", "Max", "Min", "Home", "Mesh"};
    public static final int COLS = labels.length-1;
    private final EntityManager entityManager;
    private final Entity rootEntity;
    private final Entity[] joints = new Entity[NUM_JOINTS];
    private final JPanel dhTable = new JPanel(new GridLayout(NUM_JOINTS+1, labels.length,2,2));

    public EditArm6Panel(Entity rootEntity, EntityManager entityManager) {
        super(new BorderLayout());
        this.add(dhTable,BorderLayout.CENTER);
        this.entityManager = entityManager;
        this.rootEntity = rootEntity;
        createComponents();
        setupPanel();
    }

    private boolean firstChildHasNoShape(Entity entity) {
        List<Entity> children = entity.getChildren();
        if(children.size()==0) return true;
        Entity firstChild = children.get(0);
        ShapeComponent mesh = firstChild.getComponent(ShapeComponent.class);
        return mesh == null;
    }

    private boolean secondChildHasDHComponent(Entity entity) {
        List<Entity> children = entity.getChildren();
        if(children.size()<2) return false;
        Entity secondChild = children.get(1);
        DHComponent dh = secondChild.getComponent(DHComponent.class);
        return dh!=null;
    }

    private void createComponents() {
        if(firstChildHasNoShape(rootEntity)) {
            // Add Entity with MeshFromFile for the base of the arm
            Entity baseMeshEntity = new Entity();
            entityManager.addEntityToParent(baseMeshEntity, rootEntity);
            baseMeshEntity.addComponent(new MeshFromFile());
            baseMeshEntity.addComponent(new OriginAdjustComponent());
        }

        Entity parent = rootEntity;
        for (int i = 0; i < NUM_JOINTS; i++) {
            Entity jointEntity;

            if(secondChildHasDHComponent(parent)) {
                jointEntity = parent.getChildren().get(1);
            } else {
                // Add child
                jointEntity = new Entity("J" + i);
                jointEntity.addComponent(new DHComponent());
                entityManager.addEntityToParent(jointEntity, parent);
            }

            if(firstChildHasNoShape(jointEntity)) {
                // Add mesh
                Entity meshEntity = new Entity();
                entityManager.addEntityToParent(meshEntity, jointEntity);
                meshEntity.addComponent(new MeshFromFile());
                meshEntity.addComponent(new OriginAdjustComponent());
            }

            // Keep going
            joints[i] = jointEntity;
            parent = jointEntity;
        }

        // Add EndEffectorComponent to the last joint entity
        if(joints[NUM_JOINTS-1].getComponent(ArmEndEffectorComponent.class)==null) {
            joints[NUM_JOINTS-1].addComponent(new ArmEndEffectorComponent());
        }
    }

    private void setupPanel() {
        setupGeneralSettings();
        setupDHTable();
    }

    private void setupDHTable() {
        for (String label : labels) {
            dhTable.add(new JLabel(label));
        }

        for (int i = 0; i < NUM_JOINTS; i++) {
            dhTable.add(new JLabel(String.valueOf(i)));
            for (int j = 0; j < COLS; j++) {
                if (j < COLS - 1) {
                    final int paramIndex = j;
                    DHComponent dhComponent = joints[i].getComponent(DHComponent.class);

                    JTextField dhInput = new JTextField(7);

                    double v = switch (paramIndex) {
                        case 0 -> dhComponent.getD();
                        case 1 -> dhComponent.getR();
                        case 2 -> dhComponent.getAlpha();
                        case 3 -> dhComponent.getTheta();
                        case 4 -> dhComponent.getJointMax();
                        case 5 -> dhComponent.getJointMin();
                        case 6 -> dhComponent.getJointHome();
                        default -> Double.NaN;
                    };
                    dhInput.setText(StringHelper.formatDouble(v));
                    dhInput.addActionListener(e -> {
                        double value = Double.parseDouble(dhInput.getText());

                        switch (paramIndex) {
                            case 0 -> dhComponent.setD(value);
                            case 1 -> dhComponent.setR(value);
                            case 2 -> dhComponent.setAlpha(value);
                            case 3 -> dhComponent.setTheta(value);
                            case 4 -> dhComponent.setJointMax(value);
                            case 5 -> dhComponent.setJointMin(value);
                            case 6 -> dhComponent.setJointHome(value);
                        }
                        updatePoses();
                    });
                    dhTable.add(dhInput);
                } else {
                    Entity firstChild = rootEntity.getChildren().get(0);
                    if(firstChild.getComponent(MeshFromFile.class)!=null) {
                        MeshFromFile meshFromFile = joints[i].getChildren().get(0).getComponent(MeshFromFile.class);
                        StringParameter filenameParameter = meshFromFile.filename;
                        ViewElementFilename viewElementFilename = new ViewElementFilename(filenameParameter);
                        viewElementFilename.addFileFilters(MeshFactory.getAllExtensions());
                        dhTable.add(viewElementFilename);
                    } else {
                        // is a ShapeComponent
                        dhTable.add(new JLabel("Joint model is Shape"));
                    }
                }
            }
        }
    }

    // general settings
    private void setupGeneralSettings() {
        JPanel generalPanel = new JPanel();
        generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.Y_AXIS));

        // texture
        MaterialComponent material = rootEntity.getChildren().get(0).getComponent(MaterialComponent.class);
        StringParameter textureParameter = material.texture;
        ViewElementFilename textureFilename = new ViewElementFilename(textureParameter);
        textureFilename.addFileFilters(MeshFactory.getAllExtensions());
        textureFilename.setAlignmentX(Component.LEFT_ALIGNMENT);
        textureFilename.setMaximumSize(textureFilename.getPreferredSize());
        generalPanel.add(textureFilename);
        textureParameter.addPropertyChangeListener(evt -> {
            String filename = textureParameter.get();
            // set texture of all meshes
            for (Entity joint : joints) {
                Entity meshEntity = joint.getChildren().get(0);
                MaterialComponent meshMaterial = meshEntity.getComponent(MaterialComponent.class);
                meshMaterial.texture.set(filename);
            }
        });

        // base mesh
        Entity firstChild = rootEntity.getChildren().get(0);
        if(firstChild.getComponent(MeshFromFile.class)!=null) {
            MeshFromFile meshFromFile = rootEntity.getChildren().get(0).getComponent(MeshFromFile.class);
            StringParameter filenameParameter = meshFromFile.filename;
            ViewElementFilename baseMeshFilename = new ViewElementFilename(filenameParameter);
            baseMeshFilename.addFileFilters(MeshFactory.getAllExtensions());
            baseMeshFilename.setAlignmentX(Component.LEFT_ALIGNMENT);
            baseMeshFilename.setMaximumSize(baseMeshFilename.getPreferredSize());
            generalPanel.add(baseMeshFilename);
        } else {
            // probably a ShapeComponent
            generalPanel.add(new JLabel("Base is Shape"));
        }

        // show DH
        JCheckBox showDH = new JCheckBox("Show DH");
        showDH.setSelected(false);
        generalPanel.add(showDH);
        showDH.addActionListener(e -> {
            for (int i = 0; i < NUM_JOINTS; i++) {
                joints[i].getComponent(DHComponent.class).setVisible(showDH.isSelected());
            }
        });

        this.add(generalPanel, BorderLayout.NORTH);
    }

    private void updatePoses() {
        OriginAdjustSystem.adjustOne(rootEntity.getChildren().get(0));
        for(Entity entity : joints) {
            OriginAdjustSystem.adjustOne(entity.getChildren().get(0));
        }
    }
}
