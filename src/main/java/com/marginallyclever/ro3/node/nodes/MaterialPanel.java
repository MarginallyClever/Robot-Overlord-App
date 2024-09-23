package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.texture.TextureChooserDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class MaterialPanel extends JPanel {
    private static final int THUMBNAIL_SIZE = 64;
    private final Material material;
    private final JLabel sizeLabel = new JLabel();
    private final JLabel imgLabel = new JLabel();

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
        gbc.fill = GridBagConstraints.BOTH;

        var texture = material.getTexture();

        JButton button = new JButton();
        setTextureButtonLabel(button);
        button.addActionListener(e -> {
            var textureChooserDialog = new TextureChooserDialog();
            textureChooserDialog.setSelectedItem(texture);
            int result = textureChooserDialog.run(this);
            if(result == JFileChooser.APPROVE_OPTION) {
                material.setTexture(textureChooserDialog.getSelectedItem());
                setTextureButtonLabel(button);
                updatePreview();
            }
        });
        PanelHelper.addLabelAndComponent(this,"Texture",button,gbc);
        PanelHelper.addLabelAndComponent(this,"Size",sizeLabel,gbc);
        PanelHelper.addLabelAndComponent(this,"Preview",imgLabel,gbc);

        updatePreview();

        PanelHelper.addColorChooser(this,"Diffuse",material.getDiffuseColor(),material::setDiffuseColor,gbc);
        PanelHelper.addColorChooser(this,"Specular",material.getSpecularColor(),material::setSpecularColor,gbc);
        PanelHelper.addColorChooser(this,"Emissive",material.getEmissionColor(),material::setEmissionColor,gbc);

        // lit
        JToggleButton isLitButton = new JToggleButton("Lit",material.isLit());
        isLitButton.addActionListener(e -> material.setLit(isLitButton.isSelected()));
        PanelHelper.addLabelAndComponent(this,"Lit",isLitButton,gbc);

        // shininess
        gbc.gridy++;
        gbc.gridwidth=2;
        this.add(createShininessSlider(),gbc);
        gbc.gridy++;
        this.add(createSpecularStrengthSlider(),gbc);
    }

    private void updatePreview() {
        var texture = material.getTexture();
        if(texture!=null) {
            sizeLabel.setText(texture.getWidth()+"x"+texture.getHeight());
            imgLabel.setIcon(new ImageIcon(scaleImage(texture.getImage())));
            imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        } else {
            sizeLabel.setText("");
            imgLabel.setIcon(null);
        }
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

    private void setTextureButtonLabel(JButton button) {
        var texture = material.getTexture();
        button.setText((texture==null)
                ? "..."
                : texture.getSource().substring(texture.getSource().lastIndexOf(java.io.File.separatorChar)+1));
    }

    private BufferedImage scaleImage(BufferedImage sourceImage) {
        Image tmp = sourceImage.getScaledInstance(THUMBNAIL_SIZE, THUMBNAIL_SIZE, Image.SCALE_SMOOTH);
        BufferedImage scaledImage = new BufferedImage(THUMBNAIL_SIZE, THUMBNAIL_SIZE, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = scaledImage.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return scaledImage;
    }
}
