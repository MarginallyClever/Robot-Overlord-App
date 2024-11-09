package com.marginallyclever.ro3.mesh.shapes;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

/**
 * <p>Panel for editing a {@link Sphere}.</p>
 */
public class SpherePanel extends JPanel {
    private final Sphere sphere;
    private final JFormattedTextField radiusField;
    private final JFormattedTextField detailField;

    public SpherePanel() {
        this(new Sphere());
    }

    public SpherePanel(Sphere sphere) {
        super(new GridLayout(0, 2));
        this.sphere = sphere;
        setName(Sphere.class.getSimpleName());

        radiusField = PanelHelper.addNumberField("Radius", sphere.radius);
        detailField = PanelHelper.addNumberField("Detail", sphere.detail);

        PanelHelper.addLabelAndComponent(this, "Radius", radiusField);
        PanelHelper.addLabelAndComponent(this, "Detail", detailField);

        // these have to be after creating the fields to reduce the number of property change calls.
        radiusField.addPropertyChangeListener("value", (e) -> updateSphere());
        detailField.addPropertyChangeListener("value", (e) -> updateSphere());
    }

    private void updateSphere() {
        sphere.radius = ((Number) radiusField.getValue()).floatValue();
        sphere.detail = ((Number) detailField.getValue()).intValue();
        sphere.updateModel();
    }
}