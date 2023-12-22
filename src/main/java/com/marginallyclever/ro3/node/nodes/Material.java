package com.marginallyclever.ro3.node.nodes;

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
        json.put("texture",texture.getSource());
        return json;
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        String textureSource = from.getString("texture");
        texture = Registry.textureFactory.load(textureSource);
    }
}
