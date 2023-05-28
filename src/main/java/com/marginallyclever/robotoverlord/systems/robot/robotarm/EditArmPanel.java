package com.marginallyclever.robotoverlord.systems.robot.robotarm;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.robotoverlord.components.shapes.Grid;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.parameters.TextureParameter;
import com.marginallyclever.robotoverlord.robots.Robot;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementSlider;
import com.marginallyclever.robotoverlord.systems.render.mesh.load.MeshFactory;
import com.marginallyclever.robotoverlord.parameters.StringParameter;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementFilename;
import com.marginallyclever.robotoverlord.systems.OriginAdjustSystem;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.vecmath.Matrix4d;
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
    private static final String [] labels = {"Joint", "D", "R", "Alpha", "Theta", "Max", "Min", "Home", "Mesh"};
    private static final int [] widths = {30,30,30,30,30,30,30,30,25};
    public static final int COLS = labels.length-1;
    private final EntityManager entityManager;
    private final Entity rootEntity;
    private final List<Entity> joints = new ArrayList<>();
    private final JPanel dhTable = new JPanel(new GridBagLayout());
    private final JPanel centerContainer = new JPanel(new BorderLayout());
    private final JCheckBox adjustOrigins = new JCheckBox("All meshes have origin at root of robot");
    private final RobotComponent robotComponent;

    public EditArmPanel(Entity rootEntity, EntityManager entityManager) {
        super(new BorderLayout());

        Dimension d = new Dimension(700,320);
        setMinimumSize(d);
        setPreferredSize(d);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(centerContainer,BorderLayout.CENTER);
        this.entityManager = entityManager;
        this.rootEntity = rootEntity;
        this.robotComponent = rootEntity.getComponent(RobotComponent.class);
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

        numJoints = Math.max(1,numJoints);
    }

    private ShapeComponent findChildShapeComponent(Entity entity) {
        for( Entity child : entity.getChildren()) {
            ShapeComponent found = child.getComponent(ShapeComponent.class);
            if(found!=null) return found;
        }
        return null;
    }

    private DHComponent findChildDHComponent(Entity entity) {
        for( Entity child : entity.getChildren()) {
            DHComponent found = child.getComponent(DHComponent.class);
            if(found!=null) return found;
        }
        return null;
    }

    private void createComponents() {
        joints.clear();

        if(rootEntity.getComponent(ShapeComponent.class)==null && findChildShapeComponent(rootEntity)==null) {
            // Add Entity with MeshFromFile for the base of the arm
            rootEntity.addComponent(new MeshFromFile());
        }

        // recursively add the joints
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

            if(findChildShapeComponent(jointEntity)==null) {
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

        // Add end effector target entity to the root entity
        updateTarget();

        robotComponent.findBones();
    }

    private void updateTarget() {
        Entity target = robotComponent.getChildTarget();
        if(target==null) {
            target = new Entity(RobotComponent.TARGET_NAME);
            entityManager.addEntityToParent(target, rootEntity);
        }

        PoseComponent targetPose = target.getComponent(PoseComponent.class);
        Matrix4d endEffector = (Matrix4d)robotComponent.get(Robot.END_EFFECTOR);
        Matrix4d root = (Matrix4d)robotComponent.get(Robot.POSE);
        endEffector.mul(root,endEffector);
        targetPose.setWorld(endEffector);
    }

    private void setupPanel() {
        setupGeneralSettings();
        setupDHTable();
    }

    private void setupDHTable() {

        centerContainer.removeAll();
        dhTable.removeAll();
        dhTable.setBorder(BorderFactory.createTitledBorder("DH Parameters"));
        centerContainer.add(dhTable,BorderLayout.NORTH);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(1,1,1,1);
        c.fill = GridBagConstraints.HORIZONTAL;

        for(int i=0;i<labels.length;++i) {
            c.gridx=i;
            c.gridy=0;
            JLabel label = new JLabel(labels[i]);
            int height = label.getPreferredSize().height;
            Dimension d = new Dimension(widths[i],height);
            label.setMinimumSize(d);
            label.setPreferredSize(d);
            if(i<labels.length-1) label.setHorizontalAlignment(SwingConstants.RIGHT);
            dhTable.add(label,c);
        }

        for (int i = 0; i < numJoints; i++) {
            c.gridy=i+1;
            c.gridx=0;
            c.ipadx=0;
            c.weightx=0;
            JLabel label = new JLabel(String.valueOf(i));
            label.setHorizontalAlignment(SwingConstants.RIGHT);
            dhTable.add(label,c);
            for (int j = 0; j < COLS; j++) {
                c.gridx=1+j;
                if (j < COLS - 1) {
                    final int paramIndex = j;
                    DHComponent dhComponent = joints.get(i).getComponent(DHComponent.class);

                    JTextField dhInput = new JTextField(7);
                    dhInput.setHorizontalAlignment(SwingConstants.RIGHT);

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
                            updateTarget();
                        }
                    });

                    dhTable.add(dhInput,c);
                } else {
                    c.weightx=1;
                    // last column mesh selection
                    ShapeComponent shape = findChildShapeComponent(joints.get(i));
                    if(shape!=null) {
                        if (shape instanceof MeshFromFile) {
                            MeshFromFile meshFromFile = (MeshFromFile) shape;
                            StringParameter filenameParameter = meshFromFile.filename;
                            ViewElementFilename viewElementFilename = new ViewElementFilename(filenameParameter);
                            viewElementFilename.addFileFilters(MeshFactory.getAllExtensions());
                            dhTable.add(viewElementFilename,c);
                        } else {
                            // is a ShapeComponent
                            dhTable.add(new JLabel("Joint model is Shape"),c);
                        }
                    } else {
                        dhTable.add(new JLabel("Joint model is missing"),c);
                    }
                }
            }
        }
    }

    private void setupGeneralSettings() {
        JPanel generalPanel = new JPanel(new GridBagLayout());
        generalPanel.setBorder(BorderFactory.createTitledBorder("General"));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy=0;
        c.gridx=0;

        final EditArmPanel editArmPanel = this;
        // num joints
        c.weightx=0.1;
        generalPanel.add(new JLabel("# Joints"),c);
        JSlider slider = new JSlider(1,MAX_JOINTS,numJoints);
        c.gridx=1;
        c.weightx=0.9;
        generalPanel.add(slider,c);
        slider.setPaintLabels(true);
        slider.setMajorTickSpacing(1);

        slider.setAlignmentX(Component.LEFT_ALIGNMENT);
        slider.addChangeListener(e->{
            int value = slider.getValue();
            if(value!=numJoints) {
                numJoints = value;
                createComponents();
                setupDHTable();
                SwingUtilities.getWindowAncestor(this).pack();
                editArmPanel.revalidate();
            }
        });

        // texture
        c.gridy++;
        c.weightx=0.1;
        c.gridx=0;
        generalPanel.add(new JLabel("Texture"),c);

        StringParameter textureParameter;
        MaterialComponent material = rootEntity.getChildren().get(0).getComponent(MaterialComponent.class);
        if(material!=null) {
            textureParameter = material.texture;
        } else {
            textureParameter = new TextureParameter("", "");
        }
        ViewElementFilename textureFilename = new ViewElementFilename(textureParameter);
        textureFilename.addFileFilters(MeshFactory.getAllExtensions());
        textureFilename.setAlignmentX(Component.LEFT_ALIGNMENT);
        textureFilename.setMaximumSize(textureFilename.getPreferredSize());
        c.weightx = 0.9;
        c.gridx=1;
        generalPanel.add(textureFilename,c);
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
        ShapeComponent shape = rootEntity.getComponent(MeshFromFile.class);
        if(shape==null) {
            shape = findChildShapeComponent(rootEntity);
            if(!(shape instanceof MeshFromFile)) shape=null;
        }
        if(shape!=null) {
            MeshFromFile meshFromFile = (MeshFromFile)shape;
            StringParameter filenameParameter = meshFromFile.filename;
            ViewElementFilename baseMeshFilename = new ViewElementFilename(filenameParameter);
            baseMeshFilename.addFileFilters(MeshFactory.getAllExtensions());
            baseMeshFilename.setAlignmentX(Component.LEFT_ALIGNMENT);
            baseMeshFilename.setMaximumSize(baseMeshFilename.getPreferredSize());

            c.gridy++;
            c.weightx=0.1;
            c.gridx=0;
            generalPanel.add(new JLabel("Mesh"),c);
            c.weightx = 0.9;
            c.gridx=1;
            generalPanel.add(baseMeshFilename,c);
        }

        // show DH
        JCheckBox showDH = new JCheckBox("Show DH");
        showDH.setSelected(false);
        showDH.addActionListener(e -> {
            for (int i = 0; i < numJoints; i++) {
                joints.get(i).getComponent(DHComponent.class).setVisible(showDH.isSelected());
            }
        });

        c.gridy++;
        c.weightx=1;
        c.gridx=0;
        c.gridwidth=2;
        generalPanel.add(showDH,c);
        c.gridy++;

        // adjust origins
        adjustOrigins.setSelected(false);
        generalPanel.add(adjustOrigins,c);
        adjustOrigins.addActionListener(e -> updatePoses());

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

    // test display
    public static void main(String[] args) {
        EntityManager entityManager = new EntityManager();
        entityManager.getRoot().addComponent(new RobotComponent());

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(EditArmPanel.class.getSimpleName());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800,600);
            frame.add(new EditArmPanel(entityManager.getRoot(),entityManager));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
