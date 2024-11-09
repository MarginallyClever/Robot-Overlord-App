package com.marginallyclever.ro3.mesh.shapes;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

/**
 * <p>Panel for editing a {@link Decal}.</p>
 */
public class DecalPanel extends JPanel {
    private final Decal decal;
    private final JFormattedTextField widthField;
    private final JFormattedTextField heightField;
    private final JFormattedTextField wPartsField;
    private final JFormattedTextField hPartsField;
    private final JFormattedTextField textureScaleField;

    public DecalPanel() {
        this(new Decal());
    }

    public DecalPanel(Decal decal) {
        super(new GridLayout(0, 2));
        this.decal = decal;
        setName(Decal.class.getSimpleName());

        widthField = PanelHelper.addNumberField("Width", decal.width);
        heightField = PanelHelper.addNumberField("Height", decal.height);
        wPartsField = PanelHelper.addNumberField("Width Parts", decal.wParts);
        hPartsField = PanelHelper.addNumberField("Height Parts", decal.hParts);
        textureScaleField = PanelHelper.addNumberField("Texture Scale", decal.textureScale);

        PanelHelper.addLabelAndComponent(this, "Width", widthField);
        PanelHelper.addLabelAndComponent(this, "Height", heightField);
        PanelHelper.addLabelAndComponent(this, "Width Parts", wPartsField);
        PanelHelper.addLabelAndComponent(this, "Height Parts", hPartsField);
        PanelHelper.addLabelAndComponent(this, "Texture Scale", textureScaleField);

        // these have to be after creating the fields to reduce the number of property change calls.
        widthField.addPropertyChangeListener("value", (e) -> updateDecal());
        heightField.addPropertyChangeListener("value", (e) -> updateDecal());
        wPartsField.addPropertyChangeListener("value", (e) -> updateDecal());
        hPartsField.addPropertyChangeListener("value", (e) -> updateDecal());
        textureScaleField.addPropertyChangeListener("value", (e) -> updateDecal());
    }

    private void updateDecal() {
        decal.width = ((Number) widthField.getValue()).floatValue();
        decal.height = ((Number) heightField.getValue()).floatValue();
        decal.wParts = ((Number) wPartsField.getValue()).intValue();
        decal.hParts = ((Number) hPartsField.getValue()).intValue();
        decal.textureScale = ((Number) textureScaleField.getValue()).floatValue();
        decal.updateModel();
    }
}