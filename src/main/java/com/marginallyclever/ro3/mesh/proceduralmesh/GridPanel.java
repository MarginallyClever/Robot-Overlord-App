package com.marginallyclever.ro3.mesh.proceduralmesh;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

/**
 * <p>Panel for editing a {@link Grid}.</p>
 */
public class GridPanel extends JPanel {
    private final Grid grid;
    private final JFormattedTextField widthField;
    private final JFormattedTextField heightField;
    private final JFormattedTextField spacingField;

    public GridPanel() {
        this(new Grid());
    }

    public GridPanel(Grid grid) {
        super(new GridLayout(0, 2));
        this.grid = grid;
        setName(Grid.class.getSimpleName());

        widthField = PanelHelper.addNumberField("Width", grid.width);
        heightField = PanelHelper.addNumberField("Height", grid.length);
        spacingField = PanelHelper.addNumberField("Spacing", grid.spacing);

        PanelHelper.addLabelAndComponent(this, "Width", widthField);
        PanelHelper.addLabelAndComponent(this, "Height", heightField);
        PanelHelper.addLabelAndComponent(this, "X Parts", spacingField);

        // these have to be after creating the fields to reduce the number of property change calls.
        widthField.addPropertyChangeListener("value", (e) -> updateSize());
        heightField.addPropertyChangeListener("value", (e) -> updateSize());
        spacingField.addPropertyChangeListener("value", (e) -> updateSize());
    }

    private void updateSize() {
        grid.width = ((Number) widthField.getValue()).floatValue();
        grid.length = ((Number) heightField.getValue()).floatValue();
        grid.spacing = ((Number) spacingField.getValue()).intValue();
        grid.updateModel();
    }
}