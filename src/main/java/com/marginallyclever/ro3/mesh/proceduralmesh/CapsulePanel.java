package com.marginallyclever.ro3.mesh.proceduralmesh;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

/**
 * <p>Panel for editing a {@link Capsule}.</p>
 */
public class CapsulePanel extends JPanel {
    private final Capsule capsule;
    private final JFormattedTextField height;
    private final JFormattedTextField radius;

    public CapsulePanel() {
        this(new Capsule());
    }

    public CapsulePanel(Capsule capsule) {
        super(new GridLayout(0,2));
        this.capsule = capsule;
        setName(Box.class.getSimpleName());

        height = PanelHelper.addNumberFieldDouble("Height", capsule.height);
        radius = PanelHelper.addNumberFieldDouble("Radius", capsule.radius);

        PanelHelper.addLabelAndComponent(this,"Height",height);
        PanelHelper.addLabelAndComponent(this,"Radius",radius);

        // these have to be after creating the fields to reduce the number of property change calls.
        height.addPropertyChangeListener("value",(e)->updateSize());
        radius.addPropertyChangeListener("value",(e)->updateSize());
    }

    private void updateSize() {
        capsule.height = ((Number)height.getValue()).floatValue();
        capsule.radius = ((Number)radius.getValue()).floatValue();
        capsule.updateModel();
    }
}
