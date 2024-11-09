package com.marginallyclever.ro3.mesh.shapes;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

/**
 * <p>Panel for editing a {@link Box}.</p>
 */
public class BoxPanel extends JPanel {
    private final Box box;
    private final JFormattedTextField tx;
    private final JFormattedTextField ty;
    private final JFormattedTextField tz;

    public BoxPanel() {
        this(new Box());
    }

    public BoxPanel(Box box) {
        super(new GridLayout(0,2));
        this.box = box;
        setName(Box.class.getSimpleName());

        tx = PanelHelper.addNumberField("Width",box.width);
        ty = PanelHelper.addNumberField("Height",box.height);
        tz = PanelHelper.addNumberField("Length",box.length);

        PanelHelper.addLabelAndComponent(this,"Width",tx);
        PanelHelper.addLabelAndComponent(this,"Height",ty);
        PanelHelper.addLabelAndComponent(this,"Length",tz);

        // these have to be after creating the fields to reduce the number of property change calls.
        tx.addPropertyChangeListener("value",(e)->updateSize());
        ty.addPropertyChangeListener("value",(e)->updateSize());
        tz.addPropertyChangeListener("value",(e)->updateSize());
    }

    private void updateSize() {
        box.width = ((Number)tx.getValue()).doubleValue();
        box.height = ((Number)ty.getValue()).doubleValue();
        box.length = ((Number)tz.getValue()).doubleValue();
        box.updateModel();
    }
}
