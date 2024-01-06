package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.apps.dialogs.TextureFactoryDialog;
import com.marginallyclever.ro3.texture.TextureWithMetadata;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * <p>{@link Material} contains properties for rendering a surface.  The first use case is to apply a texture to a
 * {@link com.marginallyclever.ro3.mesh.Mesh}.</p>
 *
 * <p>This class provides several functionalities:</p>
 * <ul>
 * <li>It can set and get the texture.</li>
 * <li>It can set and get the diffuse color.</li>
 * <li>It can set and get the specular color.</li>
 * <li>It can set and get the emission color.</li>
 * <li>It can set and get the shininess.</li>
 * <li>It can set and get the lit status.</li>
 * <li>It can serialize and deserialize itself to and from JSON format.</li>
 * </ul>
 */
public class Material extends Node {
    private TextureWithMetadata texture;
    private Color diffuseColor = new Color(255,255,255);
    private Color specularColor = new Color(255,255,255);
    private Color emissionColor = new Color(0,0,0);
    private int shininess = 10;
    private boolean isLit = true;

    public Material() {
        this("Material");
    }

    public Material(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        JPanel pane = new JPanel(new GridBagLayout());
        list.add(pane);
        pane.setName(Material.class.getSimpleName());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.fill = GridBagConstraints.BOTH;

        JButton button = new JButton();
        setTextureButtonLabel(button);
        button.addActionListener(e -> {
            TextureFactoryDialog textureFactoryDialog = new TextureFactoryDialog();
            int result = textureFactoryDialog.run();
            if(result == JFileChooser.APPROVE_OPTION) {
                texture = textureFactoryDialog.getTexture();
                setTextureButtonLabel(button);
            }
        });
        addLabelAndComponent(pane,"Texture",button,gbc);

        if(texture!=null) {
            BufferedImage smaller = scaleImage(texture.getImage(),64);
            addLabelAndComponent(pane,"Size",new JLabel(texture.getWidth()+"x"+texture.getHeight()),gbc);
            addLabelAndComponent(pane,"Preview",new JLabel(new ImageIcon(smaller)),gbc);
        }

        // diffuse
        JButton selectColorDiffuse = new JButton();
        selectColorDiffuse.setBackground(diffuseColor);
        selectColorDiffuse.addActionListener(e -> {
            Color color = JColorChooser.showDialog(pane,"Diffuse Color",getDiffuseColor());
            if(color!=null) setDiffuseColor(color);
            selectColorDiffuse.setBackground(diffuseColor);
        });
        addLabelAndComponent(pane,"Diffuse",selectColorDiffuse,gbc);

        // specular
        JButton selectColorSpecular = new JButton();
        selectColorSpecular.setBackground(specularColor);
        selectColorSpecular.addActionListener(e -> {
            Color color = JColorChooser.showDialog(pane,"Specular Color",getSpecularColor());
            if(color!=null) setSpecularColor(color);
            selectColorSpecular.setBackground(specularColor);
        });
        addLabelAndComponent(pane,"Specular",selectColorSpecular,gbc);

        // emissive
        JButton selectColorEmission = new JButton();
        selectColorEmission.setBackground(emissionColor);
        selectColorEmission.addActionListener(e -> {
            Color color = JColorChooser.showDialog(pane,"Emissive Color",getEmissionColor());
            if(color!=null) setEmissionColor(color);
            selectColorEmission.setBackground(emissionColor);
        });
        addLabelAndComponent(pane,"Emissive",selectColorEmission,gbc);

        // shininess
        JSlider shininessSlider = new JSlider(0,128,getShininess());
        shininessSlider.addChangeListener(e -> setShininess(shininessSlider.getValue()));
        addLabelAndComponent(pane,"Shininess",shininessSlider,gbc);

        // lit
        JToggleButton isLitButton = new JToggleButton("Lit",isLit());
        isLitButton.addActionListener(e -> setLit(isLitButton.isSelected()));
        addLabelAndComponent(pane,"Lit",isLitButton,gbc);

        super.getComponents(list);

    }

    public BufferedImage scaleImage(BufferedImage sourceImage,int size) {
        Image tmp = sourceImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        BufferedImage scaledImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = scaledImage.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return scaledImage;
    }

    private void setTextureButtonLabel(JButton button) {
        button.setText((texture==null) ? "..." : texture.getSource().substring(texture.getSource().lastIndexOf(java.io.File.separatorChar)+1));
    }

    public TextureWithMetadata getTexture() {
        return texture;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        if(texture!=null) json.put("texture",texture.getSource());
        json.put("diffuseColor", diffuseColor.getRGB());
        json.put("specularColor", specularColor.getRGB());
        json.put("emissionColor", emissionColor.getRGB());
        json.put("shininess", shininess);
        json.put("isLit", isLit);
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("texture")) texture = Registry.textureFactory.load(from.getString("texture"));
        if(from.has("diffuseColor")) diffuseColor = new Color(from.getInt("diffuseColor"),true);
        if(from.has("specularColor")) specularColor = new Color(from.getInt("specularColor"),true);
        if(from.has("emissionColor")) emissionColor = new Color(from.getInt("emissionColor"),true);
        if(from.has("shininess")) shininess = from.getInt("shininess");
        if(from.has("isLit")) isLit = from.getBoolean("isLit");
    }

    public Color getDiffuseColor() {
        return diffuseColor;
    }

    public void setDiffuseColor(Color color) {
        diffuseColor = color;
    }

    public Color getSpecularColor() {
        return specularColor;
    }

    public void setSpecularColor(Color color) {
        specularColor = color;
    }

    public Color getEmissionColor() {
        return emissionColor;
    }

    public void setEmissionColor(Color color) {
        emissionColor = color;
    }

    public void setShininess(int arg0) {
        shininess = Math.max(arg0, 0);
    }

    public int getShininess() {
        return shininess;
    }

    public boolean isLit() {
        return isLit;
    }

    public void setLit(boolean isLit) {
        this.isLit = isLit;
    }
}
