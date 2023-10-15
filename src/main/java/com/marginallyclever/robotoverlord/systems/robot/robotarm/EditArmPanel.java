package com.marginallyclever.robotoverlord.systems.robot.robotarm;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.parameters.StringParameter;
import com.marginallyclever.robotoverlord.parameters.TextureParameter;
import com.marginallyclever.robotoverlord.parameters.swing.ViewElementBoolean;
import com.marginallyclever.robotoverlord.parameters.swing.ViewElementDouble;
import com.marginallyclever.robotoverlord.parameters.swing.ViewElementFilename;
import com.marginallyclever.robotoverlord.robots.Robot;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.translator.Translator;
import com.marginallyclever.robotoverlord.systems.OriginAdjustSystem;
import com.marginallyclever.robotoverlord.systems.render.mesh.load.MeshFactory;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import java.awt.Component;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Make an industrial 6 axis robot arm.  Every joint is stacked on the previous joint in a serially-linked design.
 *
 * @author Dan Royer
 * @since 2.5.7
 */
public class EditArmPanel extends JPanel implements PropertyChangeListener {
    public static final int MAX_JOINTS = 6;
    private int numJoints = 1;
    private static final String [] labels = {"Joint", "D", "R", "Alpha", "Theta", "Max", "Min", "Home", "Mesh","Show"};
    private static final int [] widths = {30,30,30,30,30,30,30,30,30,30};
    public static final int COLS = labels.length-1;
    private final EntityManager entityManager;
    private final Entity rootEntity;
    private final List<Entity> joints = new ArrayList<>();
    private final JPanel dhTable = new JPanel(new GridBagLayout());
    private final JPanel centerContainer = new JPanel(new BorderLayout());
    private final JCheckBox adjustOrigins = new JCheckBox("All meshes have origin at root of robot");
    private final RobotComponent robotComponent;

    public EditArmPanel(Entity rootEntity, EntityManager entityManager) {
        super();
        this.entityManager = entityManager;
        this.rootEntity = rootEntity;
        this.robotComponent = rootEntity.getComponent(RobotComponent.class);

        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        Dimension d = new Dimension(760,350);
        setMinimumSize(d);
        setPreferredSize(d);

        addMenu();
        countJoints();
        createComponents();
        setupPanel();
        add(centerContainer);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        for(Entity bone : joints) {
            removeMyPropertyChangeListener(bone.getComponent(DHComponent.class));
        }
    }

    private void addMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setMaximumSize(new Dimension(Short.MAX_VALUE,Short.MAX_VALUE));
        JMenu menu = new JMenu(Translator.get("RobotOverlord.Menu.Edit"));
        menuBar.add(menu);
        menu.add(new JMenuItem(UndoSystem.getCommandUndo()));
        menu.add(new JMenuItem(UndoSystem.getCommandRedo()));

        this.add(menuBar);
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
            if(parent.getComponent(ArmEndEffectorComponent.class)!=null) {
                // remove end effector component from the parent
                parent.removeComponent(parent.getComponent(ArmEndEffectorComponent.class));
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
            DHComponent dhComponent = joints.get(i).getComponent(DHComponent.class);

            ViewElementDouble[] elements = new ViewElementDouble[7];
            int j=0;
            c.gridx=1;
            dhTable.add(elements[j++] = new ViewElementDouble(dhComponent.myD),c);  c.gridx++;
            dhTable.add(elements[j++] = new ViewElementDouble(dhComponent.myR),c);  c.gridx++;
            dhTable.add(elements[j++] = new ViewElementDouble(dhComponent.alpha),c);  c.gridx++;
            dhTable.add(elements[j++] = new ViewElementDouble(dhComponent.theta),c);  c.gridx++;
            dhTable.add(elements[j++] = new ViewElementDouble(dhComponent.jointMax),c);  c.gridx++;
            dhTable.add(elements[j++] = new ViewElementDouble(dhComponent.jointMin),c);  c.gridx++;
            dhTable.add(elements[j++] = new ViewElementDouble(dhComponent.jointHome),c);  c.gridx++;
            for(j=0;j<elements.length;++j) {
                elements[j].setLabel("");
            }
            addMyPropertyChangeListener(dhComponent);

            // mesh selection
            c.weightx=1;
            ShapeComponent shape = findChildShapeComponent(joints.get(i));
            if(shape!=null) {
                if (shape instanceof MeshFromFile) {
                    MeshFromFile meshFromFile = (MeshFromFile) shape;
                    StringParameter filenameParameter = meshFromFile.filename;
                    ViewElementFilename viewElementFilename = new ViewElementFilename(filenameParameter);
                    viewElementFilename.setLabel("");
                    viewElementFilename.addFileFilters(MeshFactory.getAllExtensions());
                    dhTable.add(viewElementFilename,c);
                } else {
                    // is a ShapeComponent
                    dhTable.add(new JLabel("Joint model is Shape"),c);
                }
            } else {
                dhTable.add(new JLabel("Joint model is missing"),c);
            }
            c.gridx++;

            // show DH?

            // show DH
            ViewElementBoolean viewElementShowDH = new ViewElementBoolean(dhComponent.isVisible);
            viewElementShowDH.setLabel("");
            c.weightx=0;
            dhTable.add(viewElementShowDH,c);
        }
    }

    private void addMyPropertyChangeListener(DHComponent dhComponent) {
        dhComponent.myD.addPropertyChangeListener(this);
        dhComponent.myR.addPropertyChangeListener(this);
        dhComponent.alpha.addPropertyChangeListener(this);
        dhComponent.theta.addPropertyChangeListener(this);
        dhComponent.jointMax.addPropertyChangeListener(this);
        dhComponent.jointMin.addPropertyChangeListener(this);
        dhComponent.jointHome.addPropertyChangeListener(this);
    }

    private void removeMyPropertyChangeListener(DHComponent dhComponent) {
        dhComponent.myD.removePropertyChangeListener(this);
        dhComponent.myR.removePropertyChangeListener(this);
        dhComponent.alpha.removePropertyChangeListener(this);
        dhComponent.theta.removePropertyChangeListener(this);
        dhComponent.jointMax.removePropertyChangeListener(this);
        dhComponent.jointMin.removePropertyChangeListener(this);
        dhComponent.jointHome.removePropertyChangeListener(this);
    }

    /**
     * This method gets called when a bound property is changed.
     *
     * @param evt A PropertyChangeEvent object describing the event source
     *            and the property that has changed.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updatePoses();
        updateTarget();
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
        textureFilename.setLabel("");
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
            baseMeshFilename.setLabel("");
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

        // adjust origins
        c.gridy++;
        adjustOrigins.setSelected(true);
        generalPanel.add(adjustOrigins,c);
        adjustOrigins.addActionListener(e -> updatePoses());

        this.add(generalPanel);
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
            frame.setSize(800,640);
            frame.add(new EditArmPanel(entityManager.getRoot(),entityManager));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
