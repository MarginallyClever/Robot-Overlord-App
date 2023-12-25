package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.apps.dialogs.TextureFactoryDialog;
import com.marginallyclever.ro3.texture.TextureWithMetadata;
import com.marginallyclever.robotoverlord.swing.CollapsiblePanel;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * {@link Material} contains properties for rendering a surface.  The first use case is to apply a texture to a mesh.
 */
public class Material extends Node {
    private TextureWithMetadata texture;
    private Color diffuseColor = new Color(255,255,255);
    private Color specularColor = new Color(255,255,255);
    private Color ambientColor = new Color(0,0,0);

    public Material() {
        this("Material");
    }

    public Material(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JComponent> list) {
        CollapsiblePanel panel = new CollapsiblePanel(Material.class.getSimpleName());
        list.add(panel);
        JPanel pane = panel.getContentPane();

        pane.setLayout(new GridLayout(0, 2));

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
        addLabelAndComponent(pane,"Texture",button);

        if(texture!=null) {
            BufferedImage smaller = scaleImage(texture.getImage(),64);
            addLabelAndComponent(pane,"Size",new JLabel(texture.getWidth()+"x"+texture.getHeight()));
            addLabelAndComponent(pane,"Preview",new JLabel(new ImageIcon(smaller)));
        }

        // diffuse
        JButton selectColorDiffuse = new JButton();
        selectColorDiffuse.setBackground(diffuseColor);
        selectColorDiffuse.addActionListener(e -> {
            setDiffuseColor(JColorChooser.showDialog(panel,"Diffuse Color",getDiffuseColor()));
            selectColorDiffuse.setBackground(diffuseColor);
        });
        addLabelAndComponent(pane,"Diffuse",selectColorDiffuse);

        // specular
        JButton selectColorSpecular = new JButton();
        selectColorSpecular.setBackground(specularColor);
        selectColorSpecular.addActionListener(e -> {
            setSpecularColor(JColorChooser.showDialog(panel,"Specular Color",getSpecularColor()));
            selectColorSpecular.setBackground(specularColor);
        });
        addLabelAndComponent(pane,"Specular",selectColorSpecular);

        // ambient
        JButton selectColorAmbient = new JButton();
        selectColorAmbient.setBackground(ambientColor);
        selectColorAmbient.addActionListener(e -> {
            setAmbientLightColor(JColorChooser.showDialog(panel,"Ambient Color",getAmbientLightColor()));
            selectColorAmbient.setBackground(ambientColor);
        });
        addLabelAndComponent(pane,"Ambient",selectColorAmbient);

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
        json.put("ambientColor", ambientColor.getRGB());
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        if(from.has("texture")) texture = Registry.textureFactory.load(from.getString("texture"));
        if(from.has("diffuseColor")) diffuseColor = new Color(from.getInt("diffuseColor"),true);
        if(from.has("specularColor")) specularColor = new Color(from.getInt("specularColor"),true);
        if(from.has("ambientColor")) ambientColor = new Color(from.getInt("ambientColor"),true);
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

    public Color getAmbientLightColor() {
        return ambientColor;
    }

    public void setAmbientLightColor(Color color) {
        ambientColor = color;
    }
}
