package com.marginallyclever.ro3.mesh.shapes;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

/**
 * <p>Panel for editing a {@link Capsule}.</p>
 */
public class CylinderPanel extends JPanel {
    private final Cylinder cylinder;
    private final JFormattedTextField height;
    private final JFormattedTextField radius0;
    private final JFormattedTextField radius1;


    public CylinderPanel() {
        this(new Cylinder());
    }

    public CylinderPanel(Cylinder cylinder) {
        super(new GridLayout(0,2));
        this.cylinder = cylinder;
        setName(Box.class.getSimpleName());

        height = PanelHelper.addNumberField("Height", cylinder.height);
        radius0 = PanelHelper.addNumberField("Radius0", cylinder.radius0);
        radius1 = PanelHelper.addNumberField("Radius1", cylinder.radius1);

        PanelHelper.addLabelAndComponent(this,"Height",height);
        PanelHelper.addLabelAndComponent(this,"Radius0",radius0);
        PanelHelper.addLabelAndComponent(this,"Radius1",radius1);

        // these have to be after creating the fields to reduce the number of property change calls.
        height.addPropertyChangeListener("value",(e)->updateSize());
        radius0.addPropertyChangeListener("value",(e)->updateSize());
        radius1.addPropertyChangeListener("value",(e)->updateSize());
    }

    private void updateSize() {
        cylinder.height = ((Number)height.getValue()).floatValue();
        cylinder.radius0 = ((Number)radius0.getValue()).floatValue();
        cylinder.radius1 = ((Number)radius1.getValue()).floatValue();
        cylinder.updateModel();
    }
}
