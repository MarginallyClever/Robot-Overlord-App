package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.swing.NumberFormatHelper;
import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.texture.TextureChooserDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

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

        PanelHelper.addTextureField(this,"Texture",material::getDiffuseTexture,material::setDiffuseTexture,gbc);
        gbc.gridy++;

        PanelHelper.addColorChooser(this,"Diffuse",material::getDiffuseColor,material::setDiffuseColor,gbc);
        gbc.gridy++;
        PanelHelper.addColorChooser(this,"Specular",material::getSpecularColor,material::setSpecularColor,gbc);
        gbc.gridy++;
        PanelHelper.addColorChooser(this,"Emissive",material::getEmissionColor,material::setEmissionColor,gbc);
        gbc.gridy++;

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
        gbc.gridwidth=2;
        this.add(createShininessSlider(),gbc);
        gbc.gridy++;
        this.add(createSpecularStrengthSlider(),gbc);
        gbc.gridy++;

        var iorField = PanelHelper.addNumberFieldDouble("IOR",material.getIOR());
        iorField.addPropertyChangeListener("value",e->material.setIOR(((Number)e.getNewValue()).doubleValue()));
        PanelHelper.addLabelAndComponent(this,"IOR",iorField,gbc);
        gbc.gridy++;

        var reflectivityField = PanelHelper.addNumberFieldDouble("reflectivity",material.getReflectivity());
        reflectivityField.addPropertyChangeListener("value",e->material.setReflectivity(((Number)e.getNewValue()).doubleValue()));
        PanelHelper.addLabelAndComponent(this,"Reflectivity",reflectivityField,gbc);
        gbc.gridy++;
    }

    private JComponent createShininessSlider() {
        JPanel container = new JPanel(new BorderLayout());

        JSlider slider = new JSlider(0,128,material.getShininess());
        slider.addChangeListener(e -> material.setShininess(slider.getValue()));

        // Make the slider fill the available horizontal space
        slider.setMaximumSize(new Dimension(Integer.MAX_VALUE, slider.getPreferredSize().height));
        slider.setMinimumSize(new Dimension(50, slider.getPreferredSize().height));

        container.add(new JLabel("Shininess"), BorderLayout.LINE_START);
        container.add(slider, BorderLayout.CENTER); // Add slider to the center of the container

        return container;
    }

    private JComponent createSpecularStrengthSlider() {
        JPanel container = new JPanel(new BorderLayout());

        var specularStrength = material.getSpecularStrength();
        JSlider slider = new JSlider(0,100,(int)(specularStrength*100));
        slider.addChangeListener(e -> material.setSpecularStrength(slider.getValue()/100.0));

        // Make the slider fill the available horizontal space
        slider.setMaximumSize(new Dimension(Integer.MAX_VALUE, slider.getPreferredSize().height));
        slider.setMinimumSize(new Dimension(50, slider.getPreferredSize().height));

        container.add(new JLabel("Specular strength"), BorderLayout.LINE_START);
        container.add(slider, BorderLayout.CENTER); // Add slider to the center of the container

        return container;
    }

}
