package com.marginallyclever.ro3.apps.dheditor;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.node.nodes.DHParameter;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import com.marginallyclever.ro3.mesh.proceduralmesh.Cylinder;
import com.marginallyclever.ro3.mesh.proceduralmesh.Box;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * <p>{@link DHEditor} is an {@link App} that allows users to edit Denavit-Hartenberg parameters for robotic arms.
 * It provides a toolbar to select the number of axes and dynamically generates input fields
 * for each DH parameter (D, R, Alpha, Theta) along with "From Pose" and "To Pose" buttons.
 * Changes in parameters update the "Registry.scene" hierarchy and the meshes therein.</p>
 * <p>Each row corresponds to one axis and contains formatted number fields for D, R, Alpha, Theta.
 * Changing these values should update the meshes in each joint of the robot. If the DRAT values change, the view in the Registry.scene should update accordingly by adjusting each Pose and its
 * contained mesh.</p>
 * <p>The D/theta component is represented by a {@link Pose} containing a {@link Cylinder} mesh with the flat
 * face stretched along the Z axis.</p>
 * <p>The R component is represented by a {@link Pose} containing a {@link Box} mesh stretched along the X axis.</p>
 * <p>The R Pose of a joint is stacked inside the D Pose of the same joint.  Each joint is stacked inside its parent. An
 * "End Effector" Pose is stacked inside the last joint. A "Base" Pose contains the entire assembly.</p>
 * <p>If the combo box changes value, the center panel is cleared and repopulated with the appropriate number of rows
 * the data in each row should persist.  The view in the "Registry.scene" should update accordingly.</p>
 * <p>Note that the Cylinder and Box meshes have their origin at their center and so their
 * {@link MeshInstance} will have to use an offset to compensate.</p>
 * <p>See also <a href="https://www.youtube.com/watch?v=rA9tm0gTln8">Denavit-Hartenberg Reference Frame Layout</a></p>
 */
public class DHEditor extends App {
    private final JComboBox<String> comboBox;
    private final JPanel centerPanel;

    static class Joint {
        DHParameter dhParameter;

        Pose containerPose;

        Pose dPose;
        MeshInstance dMeshInstance;
        Cylinder dCylinder;

        Pose rPose;
        MeshInstance rMeshInstance;
        Box rBox;

        public Joint(DHParameter dhParameter, Pose container, Pose dPose, Pose rPose, Box rBox, Cylinder dCylinder,MeshInstance dMeshInstance, MeshInstance rMeshInstance) {
            this.dhParameter = dhParameter;
            this.containerPose = container;
            this.dPose = dPose;
            this.rPose = rPose;
            this.rBox = rBox;
            this.dCylinder = dCylinder;
            this.dMeshInstance = dMeshInstance;
            this.rMeshInstance = rMeshInstance;
        }
    }
    private final List<Joint> joints = new ArrayList<>();
    private final Pose basePose; // Base Pose as the root node for the robot
    private final Material meshMaterial = new Material(); // Material for all meshes

    public DHEditor() {
        super(new BorderLayout());
        setName("DHEditor");

        // Initialize the Base Pose in the Registry Scene
        basePose = new Pose("Base");
        basePose.addChild(meshMaterial);

        // Toolbar with ComboBox for number of axes
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] options = {"1", "2", "3", "4", "5", "6"}; // Options for axes count
        comboBox = new JComboBox<>(options);
        comboBox.addActionListener(e -> updateAxes(comboBox.getSelectedIndex() + 1));
        toolbar.add(new JLabel("Number of Axes:"));
        toolbar.add(comboBox);

        // Center panel for DH parameter rows
        centerPanel = new JPanel(new GridLayout(0, 4, 5, 2)); // 4 columns for D, R, Alpha, Theta
        centerPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(centerPanel), BorderLayout.CENTER);

        // Initialize with one axis
        updateAxes(1);
    }

    @Override
    public void addNotify() {
        super.addNotify();

        var scene = Registry.getScene();
        if(!scene.getChildren().contains(basePose)) {
            Registry.getScene().addChild(basePose);
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        Registry.getScene().removeChild(basePose);
    }

    private void updateAxes(int numAxes) {
        // Adjust the number of DH parameters
        while (joints.size() < numAxes) {
            DHParameter dhParameter = new DHParameter();
            addJointToHierarchy(dhParameter);
        }
        while (joints.size() > numAxes) {
            removeLastJointFromHierarchy();
        }

        // Rebuild the UI
        updateCenterPanel();
    }

    private void updateCenterPanel() {
        centerPanel.removeAll();
        centerPanel.add(new JLabel("D"));
        centerPanel.add(new JLabel("Theta"));
        centerPanel.add(new JLabel("R"));
        centerPanel.add(new JLabel("Alpha"));
        for (Joint joint : joints) {
            centerPanel.add(createTextField("D", joint.dhParameter.getD(), value -> {
                joint.dhParameter.setD(value);
                updatePose(joint);
            }));
            centerPanel.add(createTextField("Theta", joint.dhParameter.getTheta(), value -> {
                joint.dhParameter.setTheta(value);
                updatePose(joint);
            }));
            centerPanel.add(createTextField("R", joint.dhParameter.getR(), value -> {
                joint.dhParameter.setR(value);
                updatePose(joint);
            }));
            centerPanel.add(createTextField("Alpha", joint.dhParameter.getAlpha(), value -> {
                joint.dhParameter.setAlpha(value);
                updatePose(joint);
            }));
        }
        revalidate();
        repaint();
    }

    private JComponent createTextField(String label, double value, Consumer<Double> setter) {
        var field = PanelHelper.addNumberFieldDouble(label,value);
        field.addPropertyChangeListener("value", evt -> {
            setter.accept( ((Number)evt.getNewValue()).doubleValue());
        });
        return field;
    }

    public double startingSize = 5;

    private void addJointToHierarchy(DHParameter dhParameter) {
        int jointIndex = joints.size()+1;

        Pose parentPose = basePose;
        if (!joints.isEmpty()) {
            parentPose = joints.getLast().rPose;
        }

        Pose containerPose = new Pose("Joint " + jointIndex);
        parentPose.addChild(containerPose);

        double scaleA = 1.0-(jointIndex*2 * 0.05);
        // Create and attach D Pose with Cylinder mesh
        Pose dPose = new Pose("DTheta" + jointIndex);
        containerPose.addChild(dPose);
        containerPose.addChild(dhParameter);

        MeshInstance dMeshInstance = new MeshInstance("Cylinder "+jointIndex);
        dPose.addChild(dMeshInstance);
        Cylinder cylinder = new Cylinder(); // Procedural mesh
        dMeshInstance.setMesh(cylinder);
        cylinder.setLength(1.0); // Default dimensions
        cylinder.setRadius(startingSize * scaleA); // Default radius

        double scaleB = 1.0-((jointIndex*2+1) * 0.05);
        // Create and attach R Pose with Box mesh
        Pose rPose = new Pose("RAlpha" + jointIndex);
        dPose.addChild(rPose);
        MeshInstance rMeshInstance = new MeshInstance("Box "+jointIndex);
        rPose.addChild(rMeshInstance);
        Box box = new Box(1.0f, startingSize * 2 * scaleB, startingSize * 2 * scaleB); // Procedural Box mesh
        rMeshInstance.setMesh(box);
        rMeshInstance.setPosition(new Vector3d(box.length / 2, 0, 0)); // Offset for Box

        joints.add(new Joint(dhParameter, containerPose, dPose, rPose, box, cylinder, dMeshInstance, rMeshInstance));
        updatePose(joints.getLast());
    }

    private void removeLastJointFromHierarchy() {
        if (!joints.isEmpty()) {
            Joint lastJoint = joints.removeLast();
            if (joints.isEmpty()) {
                basePose.removeChild(lastJoint.containerPose);
            } else {
                joints.getLast().rPose.removeChild(lastJoint.containerPose);
            }
        }
    }

    private void updatePose(Joint joint) {
        var scene = Registry.getScene();
        if(!scene.getChildren().contains(basePose)) {
            Registry.getScene().addChild(basePose);
        }

        Matrix4d rot = new Matrix4d();

        int index = joints.indexOf(joint);
        double scale = 1.0-((index+1) * 0.1);

        double d = joint.dhParameter.getD();
        double theta = Math.toRadians(joint.dhParameter.getTheta());
        double r = joint.dhParameter.getR();
        double alpha = Math.toRadians(joint.dhParameter.getAlpha());

        // Update D Pose transform and Cylinder size
        rot.rotZ(theta);
        rot.setTranslation(new Vector3d(0, 0, d));
        joint.dPose.setLocal(rot);
        if(d>0.1) {
            joint.dCylinder.setLength(d);
            joint.dCylinder.updateModel();
            joint.dMeshInstance.setActive(true);
        } else joint.dMeshInstance.setActive(false);
        joint.dMeshInstance.setPosition(new Vector3d(0, 0, -d / 2)); // Offset due to origin at center

        // Update R Pose transform and Box size
        rot.rotX(alpha);
        joint.rPose.setLocal(rot);
        joint.rPose.setPosition(new Vector3d(r, 0, 0));
        if(r>0.1) {
            joint.rBox.setSize(Math.max(0.1, r), joint.rBox.height, joint.rBox.length);
            joint.rMeshInstance.setActive(true);
        } else joint.rMeshInstance.setActive(false);
        joint.rMeshInstance.setPosition(new Vector3d(-r/2, 0, 0)); // Offset for Box
    }
}