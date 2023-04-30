package com.marginallyclever.robotoverlord.components.robot.robotarm.makerobotarmpanel;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.EntityManager;
import com.marginallyclever.robotoverlord.components.ArmEndEffectorComponent;
import com.marginallyclever.robotoverlord.components.DHComponent;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.OriginAdjustComponent;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.components.shapes.mesh.load.MeshFactory;
import com.marginallyclever.robotoverlord.parameters.StringParameter;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementFilename;

import java.util.List;
import javax.swing.*;
import java.awt.*;

public class EditArm6 extends JPanel {
    private static final int NUM_JOINTS = 6;
    private static final String[] labels = {"Joint", "D", "R", "Alpha", "Theta", "Max", "Min", "Home", "Mesh"};
    public static final int COLS = labels.length-1;
    private final EntityManager entityManager;
    private final Entity armEntity;
    private final Entity[] joints = new Entity[6];
    private final JPanel gridContainer = new JPanel(new GridLayout(NUM_JOINTS+1, labels.length,2,2));

    public EditArm6(Entity armEntity, EntityManager entityManager) {
        super(new BorderLayout());
        this.add(gridContainer,BorderLayout.CENTER);
        this.entityManager = entityManager;
        this.armEntity = armEntity;
        createArmComponents();
        setupPanel();
    }

    private boolean firstChildHasNoMesh(Entity entity) {
        List<Entity> children = entity.getChildren();
        if(children.size()==0) return true;
        Entity firstChild = children.get(0);
        MeshFromFile mesh = firstChild.findFirstComponent(MeshFromFile.class);
        return mesh == null;
    }

    private boolean secondChildHasDHComponent(Entity entity) {
        List<Entity> children = entity.getChildren();
        if(children.size()<2) return false;
        Entity secondChild = children.get(1);
        DHComponent dh = secondChild.findFirstComponent(DHComponent.class);
        return dh!=null;
    }

    private void createArmComponents() {
        if(firstChildHasNoMesh(armEntity)) {
            // Add Entity with MeshFromFile for the base of the arm
            Entity baseMeshEntity = new Entity();
            entityManager.addEntityToParent(baseMeshEntity, armEntity);
            baseMeshEntity.addComponent(new MeshFromFile());
            baseMeshEntity.addComponent(new OriginAdjustComponent());
        }

        Entity parent = armEntity;
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

            if(firstChildHasNoMesh(jointEntity)) {
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
        if(joints[NUM_JOINTS-1].findFirstComponent(ArmEndEffectorComponent.class)==null) {
            joints[NUM_JOINTS-1].addComponent(new ArmEndEffectorComponent());
        }
    }

    private void setupPanel() {
        {
            // general settings
            JPanel basePanel = new JPanel();
            basePanel.setLayout(new BoxLayout(basePanel, BoxLayout.Y_AXIS));

            // texture
            MaterialComponent material = armEntity.getChildren().get(0).findFirstComponent(MaterialComponent.class);
            StringParameter textureParameter = material.texture;
            ViewElementFilename textureFilename = new ViewElementFilename(textureParameter, entityManager);
            textureFilename.addFileFilters(MeshFactory.getAllExtensions());
            textureFilename.setAlignmentX(Component.LEFT_ALIGNMENT);
            textureFilename.setMaximumSize(textureFilename.getPreferredSize());
            basePanel.add(textureFilename);
            textureParameter.addPropertyChangeListener(evt -> {
                String filename = textureParameter.get();
                // set texture of all meshes
                for (Entity joint : joints) {
                    Entity meshEntity = joint.getChildren().get(0);
                    MaterialComponent meshMaterial = meshEntity.findFirstComponent(MaterialComponent.class);
                    meshMaterial.texture.set(filename);
                }
            });

            // base mesh
            MeshFromFile meshFromFile = armEntity.getChildren().get(0).findFirstComponent(MeshFromFile.class);
            StringParameter filenameParameter = meshFromFile.filename;
            ViewElementFilename meshFilename = new ViewElementFilename(filenameParameter, entityManager);
            meshFilename.addFileFilters(MeshFactory.getAllExtensions());
            meshFilename.setAlignmentX(Component.LEFT_ALIGNMENT);
            meshFilename.setMaximumSize(meshFilename.getPreferredSize());
            basePanel.add(meshFilename);

            // show DH
            JCheckBox showDH = new JCheckBox("Show DH");
            showDH.setSelected(false);
            basePanel.add(showDH);
            showDH.addActionListener(e -> {
                for (int i = 0; i < NUM_JOINTS; i++) {
                    joints[i].findFirstComponent(DHComponent.class).setVisible(showDH.isSelected());
                }
            });

            this.add(basePanel, BorderLayout.NORTH);
        }

        for (String label : labels) {
            gridContainer.add(new JLabel(label));
        }

        for (int i = 0; i < NUM_JOINTS; i++) {
            gridContainer.add(new JLabel(String.valueOf(i)));
            for (int j = 0; j < COLS; j++) {
                if (j < COLS - 1) {
                    final int paramIndex = j;
                    DHComponent dhComponent = joints[i].findFirstComponent(DHComponent.class);

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
                    });
                    gridContainer.add(dhInput);
                } else {
                    MeshFromFile meshFromFile = joints[i].getChildren().get(0).findFirstComponent(MeshFromFile.class);
                    StringParameter filenameParameter = meshFromFile.filename;
                    ViewElementFilename viewElementFilename = new ViewElementFilename(filenameParameter,entityManager);
                    viewElementFilename.addFileFilters(MeshFactory.getAllExtensions());

                    gridContainer.add(viewElementFilename);
                }
            }
        }
    }

    public Entity getArmEntity() {
        return armEntity;
    }
}
