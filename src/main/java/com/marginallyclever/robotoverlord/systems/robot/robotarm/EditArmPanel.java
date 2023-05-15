package com.marginallyclever.robotoverlord.systems.robot.robotarm;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.entityManager.EntityManager;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.parameters.TextureParameter;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementSlider;
import com.marginallyclever.robotoverlord.systems.render.mesh.load.MeshFactory;
import com.marginallyclever.robotoverlord.parameters.StringParameter;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementFilename;
import com.marginallyclever.robotoverlord.systems.OriginAdjustSystem;

import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * Make an industrial 6 axis robot arm.  Every joint is stacked on the previous joint in a serially-linked design.
 *
 * @author Dan Royer
 * @since 2.5.7
 */
public class EditArmPanel extends JPanel {
    public static final int MAX_JOINTS = 6;
    private int numJoints = 1;
    private static final String[] labels = {"Joint", "D", "R", "Alpha", "Theta", "Max", "Min", "Home", "Mesh"};
    public static final int COLS = labels.length-1;
    private final EntityManager entityManager;
    private final Entity rootEntity;
    private final List<Entity> joints = new ArrayList<>();
    private JPanel dhTable = new JPanel(new GridLayout(numJoints +1, labels.length,2,2));
    private JPanel centerContainer = new JPanel(new BorderLayout());
    private final JCheckBox adjustOrigins = new JCheckBox("All meshes have origin at root of robot");

    public EditArmPanel(Entity rootEntity, EntityManager entityManager) {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(centerContainer,BorderLayout.CENTER);
        this.entityManager = entityManager;
        this.rootEntity = rootEntity;
        countJoints();
        createComponents();
        setupPanel();
    }

    private void countJoints() {
        boolean found;
        numJoints = 0;
        Entity current = rootEntity;
        do {
            found = false;
            DHComponent dh = findChildDHComponent(current);
            if(dh!=null) {
                found = true;
                numJoints++;
                current = dh.getEntity();
            }
        } while(found);
    }

    private boolean noChildHasAShape(Entity entity) {
        List<Entity> children = entity.getChildren();
        if(children.size()==0) return true;
        for( Entity child : children) {
            ShapeComponent mesh = child.getComponent(ShapeComponent.class);
            if(mesh!=null) return false;
        }
        return true;
    }

    private DHComponent findChildDHComponent(Entity entity) {
        List<Entity> children = entity.getChildren();
        for( Entity child : children) {
            DHComponent dh = child.getComponent(DHComponent.class);
            if(dh!=null) return dh;
        }
        return null;
    }

    private void createComponents() {
        joints.clear();

        if(noChildHasAShape(rootEntity)) {
            // Add Entity with MeshFromFile for the base of the arm
            Entity baseMeshEntity = new Entity();
            entityManager.addEntityToParent(baseMeshEntity, rootEntity);
            baseMeshEntity.addComponent(new MeshFromFile());
            baseMeshEntity.addComponent(new OriginAdjustComponent());
        }

        Entity parent = rootEntity;
        for (int i = 0; i < numJoints; i++) {
            Entity jointEntity;

            DHComponent dh = findChildDHComponent(parent);
            if(dh!=null) {
                jointEntity = dh.getEntity();
            } else {
                // Add child
                jointEntity = new Entity("J" + i);
                jointEntity.addComponent(new DHComponent());
                entityManager.addEntityToParent(jointEntity, parent);
            }

            if(noChildHasAShape(jointEntity)) {
                // Add mesh
                Entity meshEntity = new Entity();
                entityManager.addEntityToParent(meshEntity, jointEntity);
                meshEntity.addComponent(new MeshFromFile());
                meshEntity.addComponent(new OriginAdjustComponent());
            }

            // Keep going
            joints.add(jointEntity);
            parent = jointEntity;
        }

        // Add EndEffectorComponent to the last joint entity
        Entity lastJoint = joints.get(numJoints-1);
        if(lastJoint.getComponent(ArmEndEffectorComponent.class)==null) {
            lastJoint.addComponent(new ArmEndEffectorComponent());
        }
    }

    private void setupPanel() {
        setupGeneralSettings();
        setupDHTable();
    }

    private void setupDHTable() {
        centerContainer.removeAll();
        dhTable = new JPanel(new GridLayout(numJoints +1, labels.length,2,2));
        dhTable.setBorder(BorderFactory.createTitledBorder("DH Parameters"));
        centerContainer.add(dhTable,BorderLayout.CENTER);

        for (String label : labels) {
            dhTable.add(new JLabel(label));
        }

        for (int i = 0; i < numJoints; i++) {
            dhTable.add(new JLabel(String.valueOf(i)));
            for (int j = 0; j < COLS; j++) {
                if (j < COLS - 1) {
                    final int paramIndex = j;
                    DHComponent dhComponent = joints.get(i).getComponent(DHComponent.class);

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
                    dhInput.getDocument().addDocumentListener(new DocumentListener() {
                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            onChange(e);
                        }

                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            onChange(e);
                        }

                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            onChange(e);
                        }

                        private void onChange(DocumentEvent e) {
                            String text = dhInput.getText();
                            double value;

                            try {
                                value = (text == null) ? 0 : Double.parseDouble(text);
                            } catch (NumberFormatException ex) {
                                value = 0;
                            }

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
                        }
                    });

                    dhTable.add(dhInput);
                } else {
                    Entity firstChild = rootEntity.getChildren().get(0);
                    if(firstChild.getComponent(MeshFromFile.class)!=null) {
                        MeshFromFile meshFromFile = joints.get(i).getChildren().get(0).getComponent(MeshFromFile.class);
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

    private void setupGeneralSettings() {
        JPanel generalPanel = new JPanel();
        generalPanel.setBorder(BorderFactory.createTitledBorder("General"));
        generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.Y_AXIS));

        // num joints
        IntParameter numJointsParameter = new IntParameter("# Joints", numJoints);
        ViewElementSlider numJointSlider = new ViewElementSlider(numJointsParameter,MAX_JOINTS,1);
        generalPanel.add(numJointSlider);
        numJointsParameter.addPropertyChangeListener(e->{
            int value = numJointsParameter.get();
            if(value!=numJoints) {
                numJoints = value;
                createComponents();
                setupDHTable();
            }
        });
        numJointSlider.setAlignmentX(Component.LEFT_ALIGNMENT);

        // texture
        StringParameter textureParameter;
        MaterialComponent material = rootEntity.getChildren().get(0).getComponent(MaterialComponent.class);
        if(material!=null) {
            textureParameter = material.texture;
        } else {
            textureParameter = new TextureParameter("texture", "");
        }
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
                if(meshMaterial!=null) meshMaterial.texture.set(filename);
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
            for (int i = 0; i < numJoints; i++) {
                joints.get(i).getComponent(DHComponent.class).setVisible(showDH.isSelected());
            }
        });

        // adjust origins
        adjustOrigins.setSelected(false);
        generalPanel.add(adjustOrigins);
        adjustOrigins.addActionListener(e -> {
            updatePoses();
        });

        this.add(generalPanel, BorderLayout.NORTH);
    }

    private void updatePoses() {
        adjustChildWithOriginAdjuster(rootEntity);
        for(Entity entity : joints) {
            adjustChildWithOriginAdjuster(entity);
        }
    }

    private void adjustChildWithOriginAdjuster(Entity entity) {
        for(Entity child : entity.getChildren()) {
            if(child.getComponent(OriginAdjustComponent.class)!=null) {
                if(adjustOrigins.isSelected()) {
                    OriginAdjustSystem.adjustOne(child);
                } else {
                    PoseComponent pose = child.getComponent(PoseComponent.class);
                    pose.setLocalMatrix4(MatrixHelper.createIdentityMatrix4());
                }
            }
        }
    }
}
