package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.apps.viewport.MaterialLayers;

import com.marginallyclever.ro3.texture.TextureWithMetadata;

import javax.swing.*;
import java.awt.*;

/**
 * Detail panel for an instance of {@link Material}.
 */
public class MaterialPanel extends JPanel {
    private final Material material;

    public MaterialPanel() {
        this(new Material());
    }

    public MaterialPanel(Material material) {
        super(new GridBagLayout());
        this.material = material;
        this.setName(Material.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        MaterialLayers[] list = MaterialLayers.values();
        for (MaterialLayers i : list) {
            if (i == MaterialLayers.ALBEDO || i == MaterialLayers.METALLIC || i == MaterialLayers.EMISSIVE) {
                PanelHelper.addColorSourceControl(
                        this,
                        i.getName(),
                        () -> material.isTextureMode(i),
                        () -> switch (i) {
                            case ALBEDO -> material.getDiffuseColor();
                            case METALLIC -> material.getSpecularColor();
                            case EMISSIVE -> material.getEmissionColor();
                            default -> Color.WHITE;
                        },
                        () -> material.getTexture(i.getIndex()),
                        (isTexture, value) -> {
                            material.setTextureMode(i, isTexture);
                            if (isTexture) {
                                material.setTexture(i.getIndex(), (TextureWithMetadata) value);
                            } else {
                                switch (i) {
                                    case ALBEDO -> material.setDiffuseColor((Color) value);
                                    case METALLIC -> material.setSpecularColor((Color) value);
                                    case EMISSIVE -> material.setEmissionColor((Color) value);
                                }
                            }
                        },
                        gbc);
            } else {
                PanelHelper.addTextureField(
                        this,
                        i.getName(),
                        () -> material.getTexture(i.getIndex()),
                        e -> material.setTexture(i.getIndex(), e),
                        gbc);
            }
            gbc.gridy++;
        }

        // emission strength
        var nfPos = NumberFormatHelper.getNumberFormatterDouble();
        nfPos.setMinimum(0);
        var esField = PanelHelper.addNumberField("Emission strength",material.getEmissionStrength(), nfPos);
        esField.addPropertyChangeListener("value",e->material.setEmissionStrength(((Number)e.getNewValue()).doubleValue()));
        PanelHelper.addLabelAndComponent(this,"Emission strength",esField,gbc);
        gbc.gridy++;

        // lit
        JToggleButton isLitButton = new JToggleButton("Lit",material.isLit());
        isLitButton.addActionListener(e -> material.setLit(isLitButton.isSelected()));
        PanelHelper.addLabelAndComponent(this,"Lit",isLitButton,gbc);
        gbc.gridy++;

        // shininess
        JSlider shininessSlider = new JSlider(0,128,material.getShininess());
        shininessSlider.addChangeListener(e -> material.setShininess(shininessSlider.getValue()));
        // Make the slider fill the available horizontal space
        shininessSlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, shininessSlider.getPreferredSize().height));
        shininessSlider.setMinimumSize(new Dimension(50, shininessSlider.getPreferredSize().height));
        PanelHelper.addLabelAndComponent(this,"Shininess",shininessSlider,gbc);

        gbc.gridy++;

        var specularStrength = material.getSpecularStrength();
        JSlider specularSlider = new JSlider(0,100,(int)(specularStrength*100));
        specularSlider.addChangeListener(e -> material.setSpecularStrength(specularSlider.getValue()/100.0));
        // Make the slider fill the available horizontal space
        specularSlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, specularSlider.getPreferredSize().height));
        specularSlider.setMinimumSize(new Dimension(50, specularSlider.getPreferredSize().height));
        PanelHelper.addLabelAndComponent(this,"Specular strength",specularSlider,gbc);
        gbc.gridy++;

        var iorField = PanelHelper.createSlider(5.0, 1.0, Math.max(1,material.getIOR()), material::setIOR);
        PanelHelper.addLabelAndComponent(this,"IOR",iorField,gbc);
        gbc.gridy++;

        var reflectivity = PanelHelper.createSlider(1.0, 0.0, Math.clamp(material.getReflectivity(),0,1), material::setReflectivity);
        PanelHelper.addLabelAndComponent(this,"Reflectivity",reflectivity,gbc);
        gbc.gridy++;
    }
}
